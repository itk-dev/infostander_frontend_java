package dk.etek.infostander;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class ImageJPanel extends JPanel implements ActionListener {
	private static final long serialVersionUID = -2952195227257913443L;
	private static JLabel label;
	
    private static final float DELTA = -0.01f;
    private static final Timer timer = new Timer(100, null);
    private float alpha = 1f;
    private boolean fadeDown;
	
	public ImageJPanel() {
		((FlowLayout)getLayout()).setVgap(0);
		label = new JLabel();
		label.setBackground(Color.BLACK);
		add(label);
		fadeDown = true;
		timer.setInitialDelay(0);
        timer.addActionListener(this);
        try {
			label.setIcon(new ImageIcon(ImageIO.read(new File("Fisk.jpg"))));
		} catch (IOException e) {}
	}
	
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
	
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_IN, alpha));
    }
	
	@Override
    public void actionPerformed(ActionEvent e) {
		if (fadeDown) {
			alpha += DELTA;
	        if (alpha <= 0) {
	            alpha = 0;
	            timer.stop();
	            fadeDown = false;
	        }
		} else {
			alpha -= DELTA;
	        if (alpha >= 1) {
	            alpha = 1;
	            timer.stop();
	            fadeDown = true;
	        }	
		}
        repaint();
    }
}
