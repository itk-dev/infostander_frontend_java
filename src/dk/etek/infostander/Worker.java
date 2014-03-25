package dk.etek.infostander;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

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

	private Infostander infostander;
	private List<BufferedImage> images;
	private Properties prop;
	private SocketIOClient socket;
	private String connectionString;
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

		// Load token.dat file.
		try {
			String output = new Scanner(new File("files/token.dat")).useDelimiter("\\Z").next();
			token = output;
		} catch (FileNotFoundException e1) {}
		
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
	}

	/**
	 * Checks if a token is saved to disk.
	 * 
	 * @return Whether or not the worker has a token.
	 */
	public boolean hasToken() {
		if (token == null) {
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
		// Initial get of channel.
		images = getInitialChannel();

		int index = 0;

		while (true) {
			// Display next image in images array.
			infostander.setNewImage(images.get(index));

			// Get the next index.
			index = (index + 1) % images.size();

			// Record time before updating channel.
			Long beforeChannelGetTime = new Date().getTime();

			// If next index is first, update channel.
			if (index == 0) {
				List<BufferedImage> newlist;
				try {
					newlist = getChannel();

					images = newlist;
				} catch (ChannelGetException e) {
					// If exception, do not update list. Use previous and report
					// error.
				}
			}

			// Record time after updating channel.
			Long afterChannelGetTime = new Date().getTime();

			// Wait for next event.
			try {
				Thread.sleep(60000 - (afterChannelGetTime - beforeChannelGetTime));
			} catch (InterruptedException e) {

			}
		}
	}

	/**
	 * Get the initial channel.
	 * 
	 * @return List<BufferedImage> List of images from channel else from cache.
	 */
	private List<BufferedImage> getInitialChannel() {
		List<BufferedImage> list = new ArrayList<BufferedImage>();
		try {
			List<BufferedImage> newList = getChannel();
			list = newList;
		} catch (Exception e) {
			// Get cached channel.
			list = getCachedChannel();
		}
		return list;
	}

	/**
	 * Get the cached channel.
	 * 
	 * @return
	 */
	private List<BufferedImage> getCachedChannel() {
		List<BufferedImage> res = new ArrayList<BufferedImage>();

		// TODO: Get list from disk.

		if (res.size() == 0) {
			// TODO: Report error.
		}

		return res;
	}

	/**
	 * Get the channel.
	 * 
	 * @return
	 * @throws ChannelGetException
	 */
	private List<BufferedImage> getChannel() throws ChannelGetException {
		// TODO: Get channel.

		// TODO: Save images to disk.

		// TODO: Cache channel.

		// TODO: Generate image list.

		List<BufferedImage> res = new ArrayList<BufferedImage>();
		try {
			res.add(ImageIO.read(new File("files/Owl.jpg")));
			res.add(ImageIO.read(new File("files/Fish.jpg")));
		} catch (IOException e) {
			// TODO: Report error.

			// TODO: Throw exception.
			throw new ChannelGetException();
		}
		return res;
	}
}
