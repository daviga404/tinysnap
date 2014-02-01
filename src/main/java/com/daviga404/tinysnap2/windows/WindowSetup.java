package com.daviga404.tinysnap2.windows;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import com.daviga404.tinysnap2.TinySnap;
import com.daviga404.tinysnap2.util.ErrorHandler;

public class WindowSetup extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1126533286606942332L;

	private JTextField ftpUser = new JTextField(),
					   ftpPass = new JPasswordField(),
					   ftpHost = new JTextField(),
					   ftpPort = new JTextField("21"),
					   url     = new JTextField("http://");
	
	private JCheckBox classicMode = new JCheckBox();
	
	private JButton submit = new JButton("Login!"),
			        exit   = new JButton("Bye.");
	
	private File configFile;
	
	private boolean initialSetup = false;
	
	private TinySnap tinySnap;
	
	public WindowSetup(File configFile, TinySnap tinySnap, boolean initialSetup) {
		
		this.configFile = configFile;
		this.tinySnap = tinySnap;
		this.initialSetup = initialSetup;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Setup - TinySnap");
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());
		
		JPanel p = new JPanel();
		MigLayout layout = new MigLayout("wrap 6", "[35][35]10[35][35][35][35]", "[]10[]10[]10[]10[]");
		p.setLayout(layout);
		p.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		
		p.add(new JLabel("<html>Welcome to TinySnap! I'm not fucking psychic,<br>therefore I require some credentials and shit.</html>"), "span 6, wrap");
		
		p.add(new JLabel("FTP Host: "), "span 2");
		p.add(ftpHost, "span 4, grow, wrap, h 25");
		
		p.add(new JLabel("FTP User: "), "span 2");
		p.add(ftpUser, "span 4, grow, wrap, h 25");
		
		p.add(new JLabel("FTP Pass: "), "span 2");
		p.add(ftpPass, "span 4, grow, wrap, h 25");
		
		p.add(new JLabel("FTP Port: "), "span 2");
		p.add(ftpPort, "span 4, grow, wrap, h 25");
		
		p.add(new JLabel("URL: "), "span 2");
		p.add(url, "span 4, grow, wrap, h 25");
		
		p.add(new JLabel("Classic Mode:"));
		p.add(classicMode, "span 4, grow, wrap, h 25");
		
		p.add(submit, "span 4, grow");
		p.add(exit, "span 2, grow");
		
		this.add(p, BorderLayout.CENTER);
		
		submit.setName("submit");
		submit.addActionListener(this);
		exit.setName("exit");
		exit.addActionListener(this);
		
		this.pack();
		this.setVisible(true);
		this.setLocationRelativeTo(null);
		
	}
	
	public void actionPerformed(ActionEvent e) {
		if (((JButton)e.getSource()).getName().equalsIgnoreCase("submit")) {
			
			Properties p = new Properties();
			
			if (ftpHost.getText().length() == 0
					|| ftpUser.getText().length() == 0
					|| ftpPass.getText().length() == 0
					|| ftpPort.getText().length() == 0) {
				JOptionPane.showMessageDialog(null, "Please fill in all of the boxes!");
				return;
			}
			
			try {
				InetAddress.getByName(ftpHost.getText());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, "The host... has to be a host.");
				return;
			}
			
			try {
				Integer.parseInt(ftpPort.getText());
			} catch (Exception e1) {
				JOptionPane.showMessageDialog(null, "The port has to be a port (numerical).");
			}
			
			p.setProperty("ftpHost", ftpHost.getText());
			p.setProperty("ftpUser", ftpUser.getText());
			p.setProperty("ftpPass", ftpPass.getText());
			p.setProperty("ftpPort", ftpPort.getText());
			p.setProperty("url", url.getText().charAt(url.getText().length() - 1) == '/' ? url.getText() : url.getText() + '/');
			p.setProperty("classicMode", classicMode.isSelected() ? "true" : "false");
			
			try {
				p.store(new FileOutputStream(configFile), "TinySnap 2 Config");
			} catch (Exception e1) {
				ErrorHandler.handle(e1);
			}
			
			this.dispose();
			tinySnap.reloadConfig(initialSetup);
			
		} else {
			
			if (initialSetup)
				configFile.delete();
			System.exit(0);
			
		}
	}
	
}
