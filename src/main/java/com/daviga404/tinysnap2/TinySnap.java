package com.daviga404.tinysnap2;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;

import com.daviga404.tinysnap2.util.ErrorHandler;
import com.daviga404.tinysnap2.windows.WindowLoaded;
import com.daviga404.tinysnap2.windows.WindowSetup;
import com.melloware.jintellitype.JIntellitype;

public class TinySnap {
	
	public static String ftpHost;
	public static String ftpUser;
	public static String ftpPass;
	public static String url;
	public static int    ftpPort;
	public static boolean classicMode;
	
	private File configFile;	
	public TinySnapHotkeyListener hotkeyListener;
	
	public static void main(String[] args) {
		
		try {
			new TinySnap();
		} catch (IOException e) {
			ErrorHandler.handle(e);
		}
		
	}
	
	public TinySnap() throws IOException {
		
		configFile = new File("./config.properties");
		
		if (configFile.createNewFile())
			showSetupDialog(true);
		else
			reloadConfig(true);
		
	}
	
	public void reloadConfig(boolean initialSetup) {
		
		try {
			
			Properties p = new Properties();
			p.load(new FileInputStream(configFile));
			
			if (!p.containsKey("ftpUser")
				|| !p.containsKey("ftpPass")
				|| !p.containsKey("ftpHost")
				|| !p.containsKey("ftpPort")
				|| !p.containsKey("url")
				|| !p.containsKey("classicMode")) {
				
				showSetupDialog(true);
				return;
				
			}
			
			ftpUser = p.getProperty("ftpUser");
			ftpPass = p.getProperty("ftpPass");
			ftpHost = p.getProperty("ftpHost");
			ftpPort = Integer.parseInt(p.getProperty("ftpPort"));
			url = p.getProperty("url");
			classicMode = p.getProperty("classicMode").equalsIgnoreCase("true");
			
			if (initialSetup)
				finishInit();
			
		} catch (Exception e) {
			ErrorHandler.handle(e);
		}
		
	}
	
	private void finishInit() {
		
		hotkeyListener = new TinySnapHotkeyListener();
		
		JIntellitype j = JIntellitype.getInstance();
		j.registerHotKey(1, JIntellitype.MOD_CONTROL, (int)'1');
		j.addHotKeyListener(hotkeyListener);
		
		@SuppressWarnings("unused")
		WindowLoaded w = new WindowLoaded();
		
		try {
			
			TrayIcon trayIcon = new TrayIcon(ImageIO.read(TinySnap.class.getResourceAsStream("/icon.gif")));
			SystemTray tray = SystemTray.getSystemTray();
			PopupMenu popup = new PopupMenu();
			
			MenuItem setup = new MenuItem("Setup");
			setup.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					
					showSetupDialog(false);
				
				}
				
			});
			
			MenuItem exit  = new MenuItem("Exit");
			exit.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
				
					System.exit(0);
					
				}			
				
			});
			
			popup.add(setup);
			popup.addSeparator();
			popup.add(exit);
			
			trayIcon.setToolTip("TinySnap 2, Bitches!");
			trayIcon.setPopupMenu(popup);
			tray.add(trayIcon);
			
		} catch (Exception e) {
			ErrorHandler.handle(e);
		}
		
	}
	
	public void showSetupDialog(boolean initialSetup) {
		
		@SuppressWarnings("unused")
		WindowSetup ws = new WindowSetup(configFile, this, initialSetup);
		
	}
	
}
