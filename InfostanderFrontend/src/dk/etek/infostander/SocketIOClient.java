package dk.etek.infostander;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * SocketIOClient class.
 */
public class SocketIOClient implements IOCallback {
	private Worker worker;
	private SocketIO socket;
	private String token;
	private URL url;
	
	/**
	 * Constructor 
	 * @param worker Worker that has launched this SocketIOClient
	 * @param serverURL The server URL.
	 * @param token The token.
	 * @param secure Whether or not to use SSL.
	 * @param sslContext The SSLContext to use if using SSL.
	 * @throws MalformedURLException
	 * @throws NoSuchAlgorithmException
	 */
	public SocketIOClient(Worker worker, String serverURL, String token, boolean secure, SSLContext sslContext) throws MalformedURLException, NoSuchAlgorithmException {
		this.worker = worker;
		this.token = token;
		
		if (secure) {
			SocketIO.setDefaultSSLSocketFactory(sslContext);
		}
				
		url = new URL(serverURL + "?token=" + token);
		socket = new SocketIO(url, this);
	}
		
	/**
	 * Implementation of on method.
	 */
	@Override
	public void on(String event, IOAcknowledge ack, Object... args) {
		System.out.println("Server triggered event '" + event + "'");
		
		if (event.equals("channelPush")) {
			JSONObject json = (JSONObject) args[0];
			worker.processChannel(json);
		} else if (event.equals("reload")) {
			socket.disconnect();
			socket = new SocketIO(url, this);
		} else if (event.equals("ready")) {
			JSONObject json = (JSONObject) args[0];
			try {
				int statusCode = (Integer) json.get("statusCode");
				if (statusCode == 404) {
					System.out.println("404: booted!");
					worker.gotBooted();
				}
			} catch (JSONException e) {}
		}
	}

	/**
	 * Terminate the socket connection.
	 */
	public void terminate() {
		socket.disconnect();
		socket = null;
	}
	
	/**
	 * Implementation of onConnect.
	 * Emit ready to server on connect.
	 */
	@Override
	public void onConnect() {
		JSONObject json = new JSONObject();
		try {
			json.put("token", token);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		socket.emit("ready", json);
	}

	/**
	 * Implementation of onDisconnect.
	 */
	@Override
	public void onDisconnect() {
		System.out.println("Connection terminated.");
	}

	/**
	 * Implementation of onError.
	 * Wait 30 seconds then try to reestablish connection.
	 */
	@Override
	public void onError(SocketIOException socketIOException) {
		System.out.println("SocketIO exception: Restarting socket in 30 seconds...");
		socketIOException.printStackTrace();
		
		socket.disconnect();
		
		try {
			Thread.sleep(30000);
		} catch (InterruptedException e) {
			// Not important.
		}
		
		socket = new SocketIO(url, this);
	}

	@Override
	public void onMessage(String data, IOAcknowledge ack) {
		// Not used.
	}

	@Override
	public void onMessage(JSONObject json, IOAcknowledge ack) {
		// Not used.
	}
}
