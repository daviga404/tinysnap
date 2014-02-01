package com.daviga404.tinysnap2;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;

import com.daviga404.tinysnap2.util.ErrorHandler;
import com.daviga404.tinysnap2.windows.WindowProgress;

public class TinySnapUploader extends Thread {

	private BufferedImage snap;
	
	public TinySnapUploader(BufferedImage snap) {
		
		this.snap = snap;
		
	}
	
	private File generateRandomFile() {

		// Generate an 8-char random name
		char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
		String randomName = "";
		Random r = new Random();
		
		for (int i = 0; i < 8; i++) {
			randomName += chars[r.nextInt(36)];
		}
		
		randomName += ".png";
		
		// Check if it already exists
		File tempFile = new File(new File(System.getProperty("java.io.tmpdir")), randomName);
		if (tempFile.exists())
			return generateRandomFile();
		else
			return tempFile;
		
	}
	
	public void run() {

		try {
			
			final File f = generateRandomFile();
			f.createNewFile();
			ImageIO.write(snap, "png", f);
			
			FileInputStream fis = new FileInputStream(f);
			
			final WindowProgress progressWindow;
			
			progressWindow = TinySnap.classicMode ? null : new WindowProgress(snap, f.getName(), this);
				
			FTPClient c = new FTPClient();
			try {
				
				c.connect(TinySnap.ftpHost, TinySnap.ftpPort);
				
			} catch (Exception e1) {
				
				if (c.isConnected()) {
					try {
						c.disconnect();
					} catch (Exception e2) {}
				}
				
				if (!TinySnap.classicMode)
					progressWindow.frame.dispose();
				
				ErrorHandler.handle(e1);
				
			}
			
			if (!c.login(TinySnap.ftpUser, TinySnap.ftpPass)) {
				
				Exception e = new Exception("Failed to login to FTP server!");
				fis.close();
				throw e;
				
			}
			
			c.setFileType(FTPClient.BINARY_FILE_TYPE);
			c.enterLocalPassiveMode();
			c.setBufferSize(1048576);
			
			if (!TinySnap.classicMode) {
				
				c.setCopyStreamListener(new CopyStreamListener() {
					
					private long lastTime = System.currentTimeMillis();
					
					public void bytesTransferred(CopyStreamEvent e) {
						bytesTransferred(e.getTotalBytesTransferred(), e.getBytesTransferred(), e.getStreamSize());
					}
	
					public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
						if (System.currentTimeMillis() - lastTime >= 50 || totalBytesTransferred == f.length()) { // Prevent frequent updates
							progressWindow.setProgress((int)totalBytesTransferred, (int)f.length());
							lastTime = System.currentTimeMillis();
						}
					}
					
				});
				
			}
			
			c.storeFile(f.getName(), fis);
			
			if (TinySnap.classicMode) {
				
				try {
					AudioInputStream audioIn = AudioSystem.getAudioInputStream(TinySnap.class.getResource("/chime.wav"));
					Clip clip = AudioSystem.getClip();
					clip.open(audioIn);
					clip.start();
				} catch (Exception e1) {}
				
				StringSelection selection = new StringSelection(TinySnap.url + f.getName());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
				
			}
			
			fis.close();
			f.delete();
			
		} catch (Exception e) {
			ErrorHandler.handle(e);
		}
		
	}
	
}
