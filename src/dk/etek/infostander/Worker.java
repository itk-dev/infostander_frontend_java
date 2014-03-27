package dk.etek.infostander;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Worker class for Infostander.
 * 
 * @author Troels Ugilt Jensen
 */
public class Worker implements Runnable {
	/**
	 * Exception for when getting a channel fails.
	 * 
	 * @author Troels Ugilt Jensens
	 */
	static class ChannelGetException extends Exception {
		private static final long serialVersionUID = -3458002186764700171L;
	};
	
	public static final int SLIDE_TIME = 6000;

	private Infostander infostander;
	private List<BufferedImage> images;
	private List<BufferedImage> nextImages;
	private Properties prop;
	private SocketIOClient socket;
	private String token;

	/**
	 * Constructor.
	 * 
	 * @param infostander
	 *            The UI class
	 */
	public Worker(Infostander infostander) {
		this.infostander = infostander;
		this.images = new ArrayList<BufferedImage>();

		// Load configuration file
		prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream("config.properties");
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void processChannel(JSONObject json) {
		List<String> imagePaths = new ArrayList<String>();
		try {
			JSONArray arr = json.getJSONArray("slides");
			for (int i = 0; i < arr.length(); i++) {
				JSONObject mediaObj = (JSONObject) ((JSONObject)arr.get(i)).get("media");
				JSONArray imgArr = mediaObj.getJSONArray("image");
				for (int j = 0; j < imgArr.length(); j++) {
					imagePaths.add(imgArr.getString(j));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
		
		List<BufferedImage> newList = new ArrayList<BufferedImage>();
		List<String> imageFilenameList = new ArrayList<String>();
		
		// Get images from server and save to disk, if they do not exist already locally.
		for (String imagePath : imagePaths) {
			try {
			    URL url = new URL(imagePath);
			    String urlStr = url.getPath();
			    String filename = urlStr.substring( urlStr.lastIndexOf('/')+1, urlStr.length() );

				File f = new File("files/" + filename);

				// If file does not exist locally, save it to disk.
				if(!f.exists() || f.isDirectory()) { 
			        InputStream is = url.openStream();
				    OutputStream os = new FileOutputStream("files/" + filename);

			        byte[] b = new byte[2048];
			        int length;

			        while ((length = is.read(b)) != -1) {
			            os.write(b, 0, length);
			        }

			        is.close();
			        os.close();
				}
				
				// Add image to array.
		        newList.add(ImageIO.read(new File("files/" + filename)));
		        imageFilenameList.add("files/" + filename);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Save channel.dat
		
		
		// Set next channel list.
		nextImages = newList;
	}
	
	/**
	 * Checks if a token is saved to disk.
	 * 
	 * @return Whether or not the worker has a token.
	 */
	public boolean hasToken() {
		// Load token.dat file.
		try {
			String output = new Scanner(new File("files/token.dat")).useDelimiter("\\Z").next();
			token = output;
		} catch (FileNotFoundException e1) {
			return false;
		}
		return true;
	}

	/**
	 * Tries to activate screen and get token from middleware.
	 * 
	 * @param activationCode
	 *            The activation code entered by the user.
	 * @return boolean True if the token was set, else false.
	 */
	public boolean setToken(String activationCode) {
		// Call middleware to get token.
		URL url;
		HttpURLConnection connection = null;
		try {
			// Create request body
			JSONObject body = new JSONObject();
			body.put("activationCode", activationCode);

			// Create connection
			url = new URL(prop.getProperty("proxy") + "/activate");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Content-Length",
					"" + Integer.toString(body.toString().getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(body.toString());
			wr.flush();
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();

			JSONObject json = new JSONObject(response.toString());
			token = (String) json.get("token");

			// Save token to disk.
			PrintWriter out = new PrintWriter("files/token.dat");
			out.print(token);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

		return true;
	}

	@Override
	public void run() {
		// Setup Socket IO connection. 
		try {
			socket = new SocketIOClient(this, prop.getProperty("ws"), token);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		// Initial get of channel.
		images = new ArrayList<BufferedImage>();

		int index = 0;

		while (true) {
			if (images.size() > 0) {
				// Display next image in images array.
				infostander.setNewImage(images.get(index));
			}
			
			index++;
			
			if (index >= images.size()) {
				if (nextImages != null) {
					images = nextImages;
					nextImages = null;
				}

				index = 0;
			}
			
			// Wait for next event.
			try {
				Thread.sleep(SLIDE_TIME);
			} catch (InterruptedException e) {

			}
		}
	}
}
