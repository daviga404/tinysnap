package com.daviga404.tinysnap2.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.daviga404.tinysnap2.util.ErrorHandler;

public class WindowLoaded extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3549042064711018237L;
	private Image introImage;
	
	public WindowLoaded() {
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Setup - TinySnap");
		this.setPreferredSize(new Dimension(300, 150));
		this.setUndecorated(true);
		this.setBackground(new Color(0, 0, 0, 0));
		
		try {
			this.introImage = ImageIO.read(WindowLoaded.class.getResourceAsStream("/intro.png"));
		} catch (Exception e1) {
			ErrorHandler.handle(e1);
		}
		
		this.repaint();
		final WindowLoaded wl = this;
		Thread t = new Thread(new Runnable() {
			
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					ErrorHandler.handle(e);
				}
				wl.dispose();
			}
			
		});
		t.start();

		this.pack();
		this.setVisible(true);
		this.setLocationRelativeTo(null);
		
	}

	public void paint(Graphics g) {
		
		super.paint(g);
		
		Graphics2D graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        graphics.setColor(new Color(0, 0, 0, 150));
        graphics.fillRoundRect(0, 0, 300, 150, 15, 15);
		
        graphics.drawImage(introImage, 0, 0, null);
        
	}
	
}
