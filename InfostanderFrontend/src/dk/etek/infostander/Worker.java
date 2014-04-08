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
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Worker class for Infostander.
 */
public class Worker implements Runnable {
	public static final int SLIDE_TIME = 60000;
	
	private static final String CHANNEL_DAT_PATH = "files/channel.dat";
	private static final String TOKEN_DAT_PATH   = "files/token.dat";
	
	private Infostander infostander;
	private List<BufferedImage> images;
	private List<BufferedImage> nextImages;
	private Properties prop;
	private SocketIOClient socket;
	private String token;
	private boolean secure;
	private String truststoreFilename;
	private String truststorePassword;
	private SSLContext sslContext;
	private volatile boolean running = true;

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
		
		secure = Boolean.parseBoolean((String) prop.get("secure"));
		truststoreFilename = (String) prop.get("truststorefilename");
		truststorePassword = (String) prop.get("truststorepassword");
		
		if (secure) {
			try {
				KeyStore keyStore;
				keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
				InputStream readStream = new FileInputStream("files/" + truststoreFilename);
				keyStore.load(readStream, (truststorePassword).toCharArray());
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
				tmf.init(keyStore);
				
				sslContext = SSLContext.getInstance("TLS");
				sslContext.init(null, tmf.getTrustManagers(), null);
			} catch (Exception e) {
				try {
					sslContext = SSLContext.getDefault();
				} catch (NoSuchAlgorithmException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Handle the event that the server booted the screen.
	 */
	public void gotBooted() {
		socket.terminate();
		running = false;
		clearData();
	}
	
	/**
	 * Process a json channel.
	 * @param json
	 */
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
			System.out.println("Error processesing channel.");
			return;
		}
		
		List<BufferedImage> newList = new ArrayList<BufferedImage>();
		List<String> imageFilenameList = new ArrayList<String>();
		
		// Get images from server and save to disk, if they do not exist already locally.
		for (String imagePath : imagePaths) {
			try {
			    URL url = new URL(imagePath);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				if (secure) {
					((HttpsURLConnection)connection).setSSLSocketFactory(sslContext.getSocketFactory());
				}

			    String filename = imagePath.substring(imagePath.lastIndexOf('/') + 1, imagePath.length());

				File f = new File("files/" + filename);

				// If file does not exist locally, save it to disk.
				if(!f.exists() || f.isDirectory()) {
					InputStream is = connection.getInputStream();
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
				System.out.println("Error processing file (ignoring)");
			}
		}
		
		// Save channel.dat
		PrintWriter out;
		try {
			out = new PrintWriter(CHANNEL_DAT_PATH);
			for (String imageName : imageFilenameList) {
				out.println(imageName);
			}
			
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("Error saving channel.dat file (ignoring).");
		}
		
		// Set next channel list.
		nextImages = newList;
	}
	
	/**
	 * Delete cached files.
	 */
	public void clearData() {
		token = null;
		File file = new File(TOKEN_DAT_PATH);
		file.delete();
		file = new File(CHANNEL_DAT_PATH);
		file.delete();
	}
	
	/**
	 * Checks if a token is saved to disk.
	 * 
	 * @return Whether or not the worker has a token.
	 */
	public boolean hasToken() {
		// Load token.dat file.
		try {
			String output = new Scanner(new File(TOKEN_DAT_PATH)).useDelimiter("\\Z").next();
			token = output;
		} catch (FileNotFoundException e) {
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
		try {
			URL url;

			// Create request body
			JSONObject body = new JSONObject();
			body.put("activationCode", activationCode);

			// Create connection
			url = new URL(prop.getProperty("proxy") + "/activate");

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			if (secure) {
				((HttpsURLConnection)connection).setSSLSocketFactory(sslContext.getSocketFactory());
			} 		
			
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
			PrintWriter out = new PrintWriter(TOKEN_DAT_PATH);
			out.print(token);
			out.close();
		} catch (IOException e) {
			System.out.println("IOException");
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			System.out.println("Error reading json token.");
			e.printStackTrace();
			return false;
		}
			
		return true;
	}

	/**
	 * Implementation of run.
	 */
	@Override
	public void run() {
		// Initial get of channel.
		images = new ArrayList<BufferedImage>();
		
		Scanner scanner;
		try {
			scanner = new Scanner(new File(CHANNEL_DAT_PATH));
			String path;
			while(scanner.hasNext() && !(path = scanner.nextLine()).equals("")) {
				System.out.println(path);
				images.add(ImageIO.read(new File(path)));
			}
		} catch (FileNotFoundException e) {
			System.out.println("No channel.dat found. Starting with empty screen.");
			// No file found, no problem. 
		} catch (IOException e) {
			System.out.println("Error reading channel.dat");
			// Not problem.
		}

		int index = 0;

		// Setup Socket IO connection. 
		try {
			socket = new SocketIOClient(this, prop.getProperty("ws"), token, secure, sslContext);
		} catch (MalformedURLException e) {
			System.out.println("Error: Malformed url to middleware. Fix configuration and restart application.");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error: SSL algorithm not found. Restart application.");
		} 
		
		while (running) {
			if (images.size() > 0) {
				// Display next image in images array.
				infostander.setNewImage(images.get(index));
			}
			
			index++;

			// If reached end of array.
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
				// Not important.
			}
		}
		infostander.workerDone();
	}
}
