package com.daviga404.tinysnap2.util;

import javax.swing.JOptionPane;

public class ErrorHandler {

	public static void handle(Exception e) {
		e.printStackTrace();
		StringBuilder stackTrace = new StringBuilder();
		for (StackTraceElement ste : e.getStackTrace()) {
			stackTrace.append(ste.getClassName())
					  .append(":")
					  .append(ste.getLineNumber())
					  .append(" [")
					  .append(ste.getMethodName())
					  .append("]\r\n");
						
		}
		JOptionPane.showMessageDialog(null, "Shit! TinySnap has died. If it's of any use to you, here's the error:\r\n\r\n" + e.getMessage() + "\r\n\r\nStack trace:\r\n" + stackTrace.toString());
		System.exit(0);
	}
	
}
