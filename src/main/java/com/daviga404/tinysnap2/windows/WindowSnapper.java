package com.daviga404.tinysnap2.windows;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.daviga404.tinysnap2.TinySnapManager;
import com.daviga404.tinysnap2.util.ErrorHandler;

public class WindowSnapper extends Canvas implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4880204248840495030L;
	private Thread t;
	private boolean isRunning = false;
	private BufferedImage screenshot;
	private BufferStrategy bs;
	
	private int mouseX = -1;
	private int mouseY = -1;
	
	private final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
	private final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
	public WindowSnapper() {
		
		final JFrame frame = new JFrame();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setPreferredSize(Toolkit.getDefaultToolkit().getScreenSize());
		frame.setLayout(new BorderLayout());
		frame.setAlwaysOnTop(true);
		
		this.setIgnoreRepaint(true);
		
		this.addMouseListener(new MouseListener() {
			
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			
			public void mousePressed(MouseEvent e) {
				
				if (mouseX > -1 && mouseY > -1 && !creatingSnap) {
					creatingSnap = true;
					snapStartX = mouseX;
					snapStartY = mouseY;
					snapEndX = mouseX;
					snapEndY = mouseY;
				}
				
			}

			public void mouseReleased(MouseEvent e) {
				
				if (creatingSnap) {
					creatingSnap = false;
					if (snapStartX != snapEndX && snapStartY != snapEndY) {
						
						stop();
						frame.setVisible(false);
						int aX, aY, bX, bY;
						
						aX = snapStartX > snapEndX ? snapEndX : snapStartX;
						bX = snapStartX > snapEndX ? snapStartX : snapEndX;
						aY = snapStartY > snapEndY ? snapEndY : snapStartY;
						bY = snapStartY > snapEndY ? snapStartY : snapEndY;
						
						TinySnapManager.uploadSnap(screenshot.getSubimage(aX, aY, bX - aX, bY - aY));
						
					}
				}
				
			}
			
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {

			public void mouseDragged(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();
				if (creatingSnap) {
					snapEndX = mouseX;
					snapEndY = mouseY;
				}
			}

			public void mouseMoved(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();
				if (creatingSnap) {
					snapEndX = mouseX;
					snapEndY = mouseY;
				}
			}
			
		});
		
		try {
			screenshot = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
		} catch (Exception e1) {
			ErrorHandler.handle(e1);
		}
		
		frame.add(this, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
		
		createBufferStrategy(2);
		bs = getBufferStrategy();
		
		this.start();
		
	}
	
	public synchronized void start() {
		isRunning = true;
		t = new Thread(this);
		t.start();
	}
	
	public synchronized void stop() {
		isRunning = false;
		try {
			t.join();
		} catch (InterruptedException e) {
			ErrorHandler.handle(e);
		}
	}
	
	public void run() {
		
		while (isRunning) {
			
			render();
			
			// Not a great game loop, but this is a screenshot program! What do you expect?
			try { Thread.sleep(10); } catch (Exception e) {}
			
		}
		
	}
	
	Color translucentBlack = new Color(0, 0, 0, 100);
	
	public void render() {
		
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		
		// Render
		g.drawImage(screenshot, 0, 0, null);
		g.setColor(translucentBlack);
		if (!creatingSnap) {
			g.fillRect(0, 0, WIDTH, HEIGHT);
		} else {
			int aX, aY, bX, bY;
				
			aX = snapStartX > snapEndX ? snapEndX : snapStartX;
			bX = snapStartX > snapEndX ? snapStartX : snapEndX;
			aY = snapStartY > snapEndY ? snapEndY : snapStartY;
			bY = snapStartY > snapEndY ? snapStartY : snapEndY;
			
			if (aX == bX && aY == bY) {
				
				g.fillRect(0, 0, WIDTH, HEIGHT);
				
			} else {
				
				if (aX > 0)
					g.fillRect(0, 0, aX, HEIGHT);
				
				if (aY > 0)
					g.fillRect(aX, 0, bX - aX, aY);
				
				if (bX < WIDTH)
					g.fillRect(bX, 0, WIDTH - bX, HEIGHT);
				
				if (bY < HEIGHT)
					g.fillRect(aX, bY, bX - aX, HEIGHT - bY);
				
			}
			
		}
				
		g.dispose();
		//g.drawImage(img, 0, 0, WIDTH, HEIGHT, null);
		bs.show();
		
	}
	
	boolean creatingSnap = false;
	int snapStartX = -1;
	int snapStartY = -1;
	int snapEndX = -1;
	int snapEndY = -1;
	
}
