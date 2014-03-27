package dk.etek.infostander;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
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
	private String token;
	private URL url;
	
	public SocketIOClient(Worker worker, String serverURL, String token, boolean secure) throws MalformedURLException, NoSuchAlgorithmException {
		this.worker = worker;
		this.token = token;
		
		if (secure) {
			SSLContext sslContext = null;
			sslContext = SSLContext.getInstance( "TLS" );
			try {
				sslContext.init( null, null, null );
			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SocketIO.setDefaultSSLSocketFactory(sslContext);
		}
				
		url = new URL(serverURL + "?token=" + token);
		socket = new SocketIO(url, this);
	}
	
	@Override
	public void on(String event, IOAcknowledge ack, Object... args) {
		System.out.println("Server triggered event '" + event + "'");
		
		if (event.equals("channelPush")) {
			JSONObject json = (JSONObject) args[0];
			worker.processChannel(json);
		}
	}

	@Override
	public void onConnect() {
		System.out.println("Connection established.");
		JSONObject json = new JSONObject();
		try {
			json.put("token", token);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		socket.emit("ready", json);
	}

	@Override
	public void onDisconnect() {
		System.out.println("Connection terminated."); 
		
	}

	@Override
	public void onError(SocketIOException socketIOException) {
		System.out.println("An Error occured. Reconnecting...");
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
