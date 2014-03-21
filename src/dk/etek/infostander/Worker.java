package dk.etek.infostander;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class Worker implements Runnable {
	private Infostander infostander;
	private List<BufferedImage> images;
	
	/**
	 * Constructor
	 * 
	 * @param infostander
	 *            The UI class
	 */
	public Worker(Infostander infostander) {
		this.infostander = infostander;
		this.images = new ArrayList<BufferedImage>();
	}

	@Override
	public void run() {
		// Initial get of channel.
		images = getChannel();
		
		int index = 0;
		
		while (true) {
			infostander.setNewImage(images.get(index));
			
			index = (index + 1) % images.size();
			
			// Wait for next event.
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {}
		}
	}
	
	private List<BufferedImage> getChannel() {
		// Get channel.
		
		// Save images to disk.
		
		// Cache channel.
		
		// Generate image list.
		List<BufferedImage> res = new ArrayList<BufferedImage>();
		try {
			res.add(ImageIO.read(new File("Owl.jpg")));
			res.add(ImageIO.read(new File("Fish.jpg")));
		} catch (IOException e) {
			// TODO: Handle errors.
		}
		return res;
	}
}
