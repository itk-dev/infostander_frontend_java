package dk.etek.infostander;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
				} catch (Exception e) {
					// If exception, do not update list. Use previous and report error.
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
	
	private List<BufferedImage> getCachedChannel() {
		return new ArrayList<BufferedImage>();
	}
	
	/**
	 * Get the channel.
	 * @return
	 */
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
