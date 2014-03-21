package dk.etek.infostander;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Worker implements Runnable {
	private Infostander infostander;
	private boolean flip;
	
	/**
	 * Constructor
	 * 
	 * @param infostander
	 *            The UI class
	 */
	public Worker(Infostander infostander) {
		this.infostander = infostander;
	}

	@Override
	public void run() {
		flip = true;
		while (true) {
			BufferedImage img;
			try {
				if (flip)
					img = ImageIO.read(new File("Owl.jpg"));
				else
					img = ImageIO.read(new File("Fisk.jpg"));

				infostander.setNewImage(img);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			flip = !flip;

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// Ignore.
			}
		}
	}
}
