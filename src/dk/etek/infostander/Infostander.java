package dk.etek.infostander;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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

		// Add JLabel to JFrame
		imagePanel = new ImageJPanel();
		add(imagePanel);
		
		// Set window size.
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		
		// Place window in top left corner.
		setLocation(0, 0);
		
		setBackground(Color.BLACK);
		
		// Make sure the window is always on top.
		setAlwaysOnTop(true);

		// Hide cursor when inside window.
		setCursor(getToolkit().createCustomCursor(new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "null"));

		// Close when hitting.
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public void setNewImage(BufferedImage image) {
		// Fade out old image to black.
		imagePanel.fadeToImage(image);
	}

	/**
	 * Main method. Launches window and starts worker thread.
	 * @param args
	 */
	public static void main(String[] args) {
		// Launch new thread for UI.
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ui = new Infostander();
				ui.setVisible(true);
				
				worker = new Worker(ui);
				(new Thread(worker)).start();
			}
		});
	}
}
