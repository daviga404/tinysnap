package com.daviga404.tinysnap2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Loader {

	private File javaLocation,
				 jarLocation,
				 tempDirectory;
	
	private List<File> natives = new ArrayList<File>();
	private static final Map<String, String> validNatives;
	
	static {
		
		validNatives = new HashMap<String, String>();
		validNatives.put("JIntellitype.dll", "2f03f2ad54574ec900f1a8c91475071f");
		validNatives.put("JIntellitype64.dll", "956c2705f122a079dbfc23d11fa3fb39");
		
	}

	public Loader() {
		
		try {
			
			javaLocation = new File(System.getProperty("java.home"));
			jarLocation = new File(Loader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
			
			tempDirectory = new File(System.getProperty("java.io.tmpdir"), "tinysnap");
			
			while (tempDirectory.isFile() || tempDirectory.exists()) {
				tempDirectory = new File(System.getProperty("java.io.tmpdir"), "tinysnap" + System.currentTimeMillis());
			}
			
			if (!tempDirectory.exists())
				tempDirectory.mkdir();
			
			if (jarLocation.isDirectory()) {
				
				natives = getNativesFromFolder(jarLocation);
				if (natives.size() == 0 || !verifyNatives(natives)) {
					
					System.out.println("No natives found in class directory or invalid natives! Try launching compiled jar? Closing...");
					System.exit(0);
					
				} else {
					
					launchApplication(tempDirectory);
					
				}
				
			} else {
				
				natives = getNativesFromJar(jarLocation);
				if (natives.size() == 0 || !verifyNatives(natives)) {
					
					System.out.println("No natives found in jar or invalid natives! Please download the required natives and insert into jar. Closing...");
					System.exit(0);
					
				} else {
					
					launchApplication(tempDirectory);
					
				}
				
			}
		
		} catch (Exception e1) {
			
			System.out.println("==========================\r\n" +
							   "TinySnap failed to start with the error: " + e1.getMessage() + "\r\n" +
							   "Please review the stack trace below!\r\n" +
							   "==========================");
			
			e1.printStackTrace();
			
		}
		
	}
	
	private List<File> getNativesFromFolder(File folder) throws IOException {
		
		List<File> natives = new ArrayList<File>();
		
		if (!folder.isDirectory()) return natives;
		
		for (String entryName : folder.list()) {

			File entry = new File(folder, entryName);
			
			if (entry.isFile() && entryName.endsWith(".dll")) {
				
				Path destination = Files.copy(entry.toPath(),
											  new File(tempDirectory, entry.getName()).toPath(),
											  StandardCopyOption.REPLACE_EXISTING);
				natives.add(destination.toFile());
				
			} else if (entry.isDirectory()) {
				
				natives.addAll(getNativesFromFolder(entry));
				
			}
			
		}
		
		return natives;
		
	}
	
	private List<File> getNativesFromJar(File jar) throws IOException {
		
		List<File> natives = new ArrayList<File>();
		
		if (!jar.isFile()) return natives;
		
		JarFile jarFile = new JarFile(jar);
		Enumeration<? extends JarEntry> jarEntries = jarFile.entries();
		
		// Note: jarEntries contains absolute paths for recursive files
		while (jarEntries.hasMoreElements()) {
			
			JarEntry entry = jarEntries.nextElement();
			
			if (entry.getName().endsWith(".dll") && !entry.isDirectory()) {
				
				natives.add(extractJarEntry(jarFile.getInputStream(entry),
											new File(tempDirectory, entry.getName().replaceAll("^(?:.*/)*(.*\\.\\w+)$", "$1"))));
				
			}
			
		}
		
		jarFile.close();
		
		return natives;
		
	}
	
	private File extractJarEntry(InputStream in, File destination) throws IOException {
		
		if (destination.isDirectory()) return null;
		
		OutputStream out = new FileOutputStream(destination);
		while (in.available() > 0)
			out.write(in.read());
		
		out.flush();
		out.close();
		
		return destination;
		
	}
	
	private String getHash(File f) throws NoSuchAlgorithmException, IOException {
		
		InputStream in = new FileInputStream(f);
		MessageDigest md5 = MessageDigest.getInstance("MD5");

		while (in.available() > 0)
			md5.update((byte) in.read());
		
		in.close();
		
		byte[] digest = md5.digest();
		StringBuilder hexDigest = new StringBuilder();
		
		for (byte b : digest)
			hexDigest.append(String.format("%02X", b));
		
		return hexDigest.toString().toLowerCase();
		
	}
	
	private boolean verifyNatives(List<File> natives) throws NoSuchAlgorithmException, IOException {
		
		for (File nativeFile : natives) {
			
			if (!validNatives.containsKey(nativeFile.getName())
				|| !getHash(nativeFile).equalsIgnoreCase(validNatives.get(nativeFile.getName()))) {
			
				System.out.println(nativeFile.getName() + ", " + getHash(nativeFile));
				return false;
				
			}
			
		}
		
		return true;
		
	}
	
	private void deleteFilesInFolder(File folder) {
	
		if (!folder.isDirectory()) return;
		
		for (File f : folder.listFiles()) {
			
			if (f.isDirectory())
				deleteFilesInFolder(folder);
			else
				f.delete();
			
		}
		
		folder.delete();
		
	}
	
	private void launchApplication(File nativesDirectory) {
		
		try {
			
			ProcessBuilder p = new ProcessBuilder(
						new String[] {
								
								new File(javaLocation, "bin\\javaw.exe").getAbsolutePath(),
								new StringBuilder()
									.append("-Djava.library.path=\"")
									.append(nativesDirectory.getAbsolutePath())
									.append("\"")
									.toString(),
								"-cp",
								new StringBuilder()
									.append("\".;")
									.append(jarLocation.getAbsolutePath())
									.append("\"")
									.toString(),
								/*jarLocation.isDirectory() ? "" : "-jar",
								new StringBuilder()
									.append(jarLocation.isDirectory() ? ""
																	  : "\"" + jarLocation.getAbsolutePath() + "\"")
									.toString(),*/
								"com.daviga404.tinysnap2.TinySnap"
								
						}
					);
			
			Process process = p.start();
			
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			String ln;
			
			while ((ln = stdIn.readLine()) != null)
				System.out.println("IN: " + ln);
			
			while ((ln = stdErr.readLine()) != null)
				System.out.println("ERR: " + ln);
			
			
			process.waitFor();
		
		} catch (Exception e1) {
			
			System.out.println("==========================\r\n" +
					   "TinySnap failed to start with the error: " + e1.getMessage() + "\r\n" +
					   "Please review the stack trace below!\r\n" +
					   "==========================");
	
			e1.printStackTrace();
			
		} finally {
			
			deleteFilesInFolder(nativesDirectory);
			
		}
		
	}
	
	public static void main(String[] args) {
		
		new Loader();
		
	
	}
	
}
