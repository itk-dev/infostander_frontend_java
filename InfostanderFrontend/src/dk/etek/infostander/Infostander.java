package dk.etek.infostander;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Infostander main class. Launches UI and Worker.
 * @author Troels Ugilt Jensen
 */
public class Infostander extends JFrame {
	private static final long serialVersionUID = 8956048705298831657L;
	
	private static final int WINDOW_WIDTH = 256;
	private static final int WINDOW_HEIGHT = 384;
	private static Infostander ui;
	private static Worker worker;
	private static ImageJPanel imagePanel;
	
	/**
	 * Constructor
	 * Launches a window at (0,0) with size (256x384).
	 */
	public Infostander() {
		// Window title.
		setTitle("Infostander");
		
		// Remove top bar.
		setUndecorated(true);
		
		// Add JLabel to JFrame.
		imagePanel = new ImageJPanel();
		imagePanel.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		add(imagePanel);
		
		// Set window size.
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		
		// Place window in top left corner.
		setLocation(0, 0);
		
		// Set background color to black.
		setBackground(Color.BLACK);
		
		// Make sure the window is always on top.
		setAlwaysOnTop(true);

		// Hide cursor when inside window.
		setCursor(getToolkit().createCustomCursor(new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "null"));

		// Close when hitting.
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	/**
	 * Sets new image.
	 * @param image
	 */
	public void setNewImage(BufferedImage image) {
		// Fade out old image to black.
		imagePanel.fadeToImage(image);
	}

	/**
	 * Call this when the worker thread terminates.
	 * Fades image to black and asks for new 
	 */
	public void workerDone() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// No problem.
		}
		
		imagePanel.fadeToImage(new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_ARGB));
		String activationCode = JOptionPane.showInputDialog(null ,"Enter acctivation code:", "Not connected", JOptionPane.WARNING_MESSAGE);
		
		worker = new Worker(ui);
		while(!worker.setToken(activationCode)) {
			activationCode = JOptionPane.showInputDialog(null ,"Enter acctivation code:", "Wrong activation code!", JOptionPane.ERROR_MESSAGE);
		}
		(new Thread(worker)).start();
	}
		
	/**
	 * Main method. Launches window and starts worker thread.
	 * @param args
	 */
	public static void main(String[] args) {
		// Launch new threads for UI and Worker.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// UI thread.
				ui = new Infostander();
				ui.setVisible(true);
				
				// Worker thread.
				worker = new Worker(ui);
				
				if (worker.hasToken()) {
					(new Thread(worker)).start();
				} else {
					String activationCode = JOptionPane.showInputDialog(null ,"Enter acctivation code:", "Not connected", JOptionPane.WARNING_MESSAGE);
					while(!worker.setToken(activationCode)) {
						activationCode = JOptionPane.showInputDialog(null ,"Enter acctivation code:", "Wrong activation code!", JOptionPane.ERROR_MESSAGE);
					}
					(new Thread(worker)).start();
				}
			}
		});
	}
}
