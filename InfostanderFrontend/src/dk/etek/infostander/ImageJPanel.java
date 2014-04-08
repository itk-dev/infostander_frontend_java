package dk.etek.infostander;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * JPanel to display image ind. 
 */
public class ImageJPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -2952195227257913443L;
	private static JLabel label;
	
	private static final int FADE_TIME = 2500;
	private static final int FADE_STEPLENGTH = 50;
    
	private static final float DELTA = - 1.0f / ((float)FADE_TIME / (float)FADE_STEPLENGTH);
    private static final Timer timer = new Timer(FADE_STEPLENGTH, null);
    private float alpha = 1.0f;
    private boolean fadeDown;
	
    /**
     * Constructor. Starts with black screen.
     */
	public ImageJPanel() {
		// Make sure background is black
		setBackground(Color.BLACK);
		
		((FlowLayout)getLayout()).setVgap(0);
		label = new JLabel();
		label.setBackground(Color.BLACK);
		add(label);
		fadeDown = true;
		timer.setInitialDelay(0);
        timer.addActionListener(this);
	}
	
	/**
	 * Fade old image out, and new image in.
	 * @param image
	 */
	public void fadeToImage(BufferedImage image) {
        timer.start();

        while(timer.isRunning()) {
        	Thread.yield();
        }
        
		ImageIcon icon = new ImageIcon(image);
		label.setIcon(icon);

        timer.start();
        
        while(timer.isRunning()) {
        	Thread.yield();
        }
	}
	
	/**
	 * Set image to new image.
	 * @param image
	 */
	public void setImage(BufferedImage image) {
		ImageIcon icon = new ImageIcon(image);
		label.setIcon(icon);
	}
	
	/**
	 * Paint component with alpha set to alpha variable.
	 */
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_IN, alpha));
    }
	
	/**
	 * Updates the alpha value.
	 */
	@Override
    public void actionPerformed(ActionEvent e) {
		if (fadeDown) {
			alpha += DELTA;
	        if (alpha <= 0.0f) {
	            alpha = 0.0f;
	            timer.stop();
	            fadeDown = false;
	        }
		} else {
			alpha -= DELTA;
	        if (alpha >= 1.0f) {
	            alpha = 1.0f;
	            timer.stop();
	            fadeDown = true;
	        }	
		}
        repaint();
    }
}
