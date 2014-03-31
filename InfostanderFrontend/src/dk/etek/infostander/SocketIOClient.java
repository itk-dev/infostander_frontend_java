package dk.etek.infostander;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketIOClient implements IOCallback {
	// For development purposes. Set to true to allow all certificates.
	private static final boolean acceptAllCertificates = false;

	private Worker worker;
	private SocketIO socket;
	private String token;
	private URL url;
	
	public SocketIOClient(Worker worker, String serverURL, String token, boolean secure) throws MalformedURLException, NoSuchAlgorithmException {
		this.worker = worker;
		this.token = token;
		
		if (secure) {
			if (!acceptAllCertificates) {
				SSLContext sslContext = SSLContext.getInstance( "TLS" );
				try {
					sslContext.init( null, null, null );
				} catch (KeyManagementException e) {
					sslContext = SSLContext.getDefault();
				}
				SocketIO.setDefaultSSLSocketFactory(sslContext);
			} else {
			    // Create a trust manager that does not validate certificate chains
			    final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			        @Override
			        public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
			        }
			        @Override
			        public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
			        }
			        @Override
			        public X509Certificate[] getAcceptedIssuers() {
			            return null;
			        }
			    } };

			    // Install the all-trusting trust manager
			    final SSLContext sslContext = SSLContext.getInstance("TLS");
			    try {
					sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
				} catch (KeyManagementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    SocketIO.setDefaultSSLSocketFactory(sslContext);
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
