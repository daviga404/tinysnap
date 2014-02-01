package com.daviga404.tinysnap2.windows;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;

import com.daviga404.tinysnap2.TinySnap;
import com.daviga404.tinysnap2.TinySnapManager;
import com.daviga404.tinysnap2.TinySnapUploader;
import com.daviga404.tinysnap2.util.ErrorHandler;

public class WindowProgress extends Canvas implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9189237910476874744L;

	public JFrame frame;
	
	private Thread t;
	private boolean isRunning = false;
	private BufferStrategy bs;
	private final int WIDTH = 220;
	private final int HEIGHT = 45;
	private boolean canClick = false;
	private boolean timingOut = false;
	private TinySnapUploader uploader;
	
	private Thread fadeOut = new Thread() {
		
		public void run() {
			
			if (timingOut)
				timingOut = false;
		
			while (frame.getOpacity() > 0) {
				frame.setOpacity(frame.getOpacity() - 0.05f < 0 ? 0 : frame.getOpacity() - 0.05f);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {}
			}
			
			TinySnapManager.uploading.remove(uploader);
			frame.dispose();
			
		}
		
	};
	
	final WindowProgress thiss = this;
	
	private Thread timeoutThread = new Thread() {
		
		public void run() {
			
			long start = System.currentTimeMillis();
			long now = start;
			
			while (now - start < 30000 && timingOut) {
				
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {}
				now = System.currentTimeMillis();
				
			}
			
			if (timingOut) {
				thiss.stop();
				fadeOut.start();
			}
			
		}
		
	};
	
	public WindowProgress(BufferedImage snap, final String snapName, final TinySnapUploader uploader) {
		
		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setUndecorated(true);
		frame.setLayout(null);
		frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		frame.setAlwaysOnTop(true);
		
		int offsetHeight = (TinySnapManager.uploading.size() - 1) * (HEIGHT + 10);
		
		frame.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width - (WIDTH + 10), GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height - (HEIGHT + 10) - offsetHeight);
		
		this.setBackground(Color.BLACK);
		frame.setBackground(Color.BLACK);
		frame.setOpacity(0.7f);

		this.setIgnoreRepaint(true);
		this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.setSize(new Dimension(WIDTH, HEIGHT));
		this.setLocation(0, 0);
		this.snapThumb = snap.getSubimage(0, 0, Math.min(snap.getWidth(), snap.getHeight()), Math.min(snap.getWidth(), snap.getHeight()));
		
		this.uploader = uploader;
		
		this.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mouseEntered(MouseEvent arg0) {}
			
			public void mouseClicked(MouseEvent e) {
				
				if (canClick) {
					
					if (e.getButton() == MouseEvent.BUTTON1) {
						
						StringSelection selection = new StringSelection(TinySnap.url + snapName);
						Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
						stop();
						fadeOut.start();
						
					}
				}
				
			}
			
		});
		frame.add(this);
		
		frame.pack();
		frame.setVisible(true);
		
		createBufferStrategy(2);
		bs = this.getBufferStrategy();
		
		start();
		
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
			
			try { Thread.sleep(10); } catch (Exception e) {}
			
		}
		
	}
	
	Color translucentBlack = new Color(50, 50, 50, 100);
	BufferedImage snapThumb;
	BufferedImage canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
	
	// Here comes the ugly... horrible.. render method.
	public void render() {
		
		// Get graphics & enable anti-aliasing.
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Fill the background
		g.setColor(new Color(50, 50, 50));
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		if (progressString == null || progressPercent == null)
			return;
		
		// Fill the progress bar on the fake canvas
		Graphics2D cg = (Graphics2D) canvas.getGraphics();
		cg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		cg.setColor(Color.BLACK);
		cg.fillRect(0, 0, WIDTH, HEIGHT);

		// Cut the progress bar to the progress
		int progressWidth;
		if ((progressWidth = ((int)(WIDTH * progress))) > 0)
			g.drawImage(canvas.getSubimage(0, 0, progressWidth, HEIGHT), 0, 0, null);
		
		// Draw the snap thumbnail
		g.drawImage(snapThumb, 7, 7, 31, 31, null);
		
		float fontSize = 40;
		int fontBase = (int) (fontSize - (g.getFontMetrics().getDescent()) - g.getFontMetrics().getLeading());
		
		// Draw the percentage
		g.setColor(new Color(255, 255, 255));
		g.setFont(g.getFont().deriveFont(fontSize).deriveFont(Font.BOLD));
		g.drawString(progressPercent, 45, fontBase);
		
		// Resize font
		fontSize = 15;
		fontBase = (int) (fontSize - (g.getFontMetrics().getDescent()) - g.getFontMetrics().getLeading());
		g.setFont(g.getFont().deriveFont(fontSize).deriveFont(Font.PLAIN));
		
		// Draw [units] sent and total [units].
		g.drawString(progressString.split(":")[0], WIDTH - g.getFontMetrics().stringWidth(progressString.split(":")[0]) - 7, fontBase + 14);
		g.drawString(progressString.split(":")[1], WIDTH - g.getFontMetrics().stringWidth(progressString.split(":")[1]) - 7, fontBase + 14 + g.getFontMetrics().getHeight());
		
		g.dispose();
		bs.show();
		
	}
	
	String progressString;
	double progress = 0.0;
	String progressPercent;
	
	public void setProgress(int bytesTransferred, int totalBytes) {
		
		if (bytesTransferred == totalBytes) {
			
			// Make it clickable (to copy)
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			canClick = true;
			
			// Start the timeout of doom
			timingOut = true;
			timeoutThread.start();
			
			// Play the sound
			try {
				AudioInputStream audioIn = AudioSystem.getAudioInputStream(TinySnap.class.getResource("/chime.wav"));
				Clip clip = AudioSystem.getClip();
				clip.open(audioIn);
				clip.start();
			} catch (Exception e1) {}
			
		}
		
		Unit u = totalBytes >= 1024 * 1024 * 1024 ? Unit.GB :
				 totalBytes >= 1024 * 1024        ? Unit.MB :
				 totalBytes >= 1024               ? Unit.KB :
					 								Unit.B;
		
		double transferredInUnit,
			   totalInUnit;
		
		switch (u) {
			case GB:
				transferredInUnit = bytesTransferred / (1024d * 1024d * 1024d);
				totalInUnit = totalBytes / (1024d * 1024d * 1024d);
				break;
			case MB:
				transferredInUnit = bytesTransferred / (1024d * 1024d);
				totalInUnit = totalBytes / (1024d * 1024d);
				break;
			case KB:
				transferredInUnit = bytesTransferred / 1024d;
				totalInUnit = totalBytes / (1024d);
				break;
			case B:
			default:
				transferredInUnit = bytesTransferred;
				totalInUnit = totalBytes;
		}
		
		double transferred, total;
		transferred = Math.round(transferredInUnit * 100) / 100d;
		total = Math.round(totalInUnit * 100) / 100d;
		
		progressString = transferred + u.name() + ":" + total + u.name();
		progressPercent = Math.round((double)bytesTransferred / (double)totalBytes * 100d) + "%";
		progress = (double)bytesTransferred / (double)totalBytes;
				 
	}
	
	public enum Unit {
		B, KB, MB, GB
	}
	
}
