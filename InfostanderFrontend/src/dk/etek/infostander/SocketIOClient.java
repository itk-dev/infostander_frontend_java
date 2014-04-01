package dk.etek.infostander;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

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
	
	public SocketIOClient(Worker worker, String serverURL, String token, boolean secure, boolean selfsigned) throws MalformedURLException, NoSuchAlgorithmException {
		this.worker = worker;
		this.token = token;
		
		if (secure) {
			if (!selfsigned) {
				SSLContext sslContext = SSLContext.getDefault();
				SocketIO.setDefaultSSLSocketFactory(sslContext);
			} else {
				KeyStore keyStore;
				try {
					keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
					InputStream readStream = new FileInputStream("files/mykeystore.jks");
					keyStore.load(readStream, ("Fisk.1").toCharArray());
					TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					tmf.init(keyStore);
					
					SSLContext sslContext = SSLContext.getInstance("TLS");
					sslContext.init(null, tmf.getTrustManagers(), null);			
				    SocketIO.setDefaultSSLSocketFactory(sslContext);
				} catch (KeyStoreException e) {
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (CertificateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (KeyManagementException e) {
					e.printStackTrace();
				}
			}
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

	public void terminate() {
		socket.disconnect();
		socket = null;
	}
	
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

	@Override
	public void onDisconnect() {
		System.out.println("Connection terminated.");
	}

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
