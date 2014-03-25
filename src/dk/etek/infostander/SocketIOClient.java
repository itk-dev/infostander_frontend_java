package dk.etek.infostander;

import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.net.ssl.SSLContext;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketIOClient implements IOCallback {
	private Worker worker;
	private SocketIO socket;
	
	public SocketIOClient(Worker worker, String serverURL, String token) throws MalformedURLException, NoSuchAlgorithmException {
		this.worker = worker;
		System.out.println(serverURL);
		//SocketIO.setDefaultSSLSocketFactory(SSLContext.getDefault());
		
		socket = new SocketIO();
		
		Properties props = new Properties();
		props.put("token", token);
		
		System.out.println(props.toString());
		socket = new SocketIO(serverURL, props);
		socket.connect(this);
		// Sends a string to the server.
		socket.send("Hello Server");
	}
	
	@Override
	public void on(String event, IOAcknowledge ack, Object... args) {
		System.out.println("Server triggered event '" + event + "'");
	}

	@Override
	public void onConnect() {
		System.out.println("Connection established.");	
	}

	@Override
	public void onDisconnect() {
		System.out.println("Connection terminated.");
	}

	@Override
	public void onError(SocketIOException socketIOException) {
		System.out.println("An Error occured");
		socketIOException.printStackTrace();
	}

	@Override
	public void onMessage(String data, IOAcknowledge ack) {
		System.out.println("Server said: " + data);
	}

	@Override
	public void onMessage(JSONObject json, IOAcknowledge ack) {
		try {
			System.out.println("Server said:" + json.toString(2));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
