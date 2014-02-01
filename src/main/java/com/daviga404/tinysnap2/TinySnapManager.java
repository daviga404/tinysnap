package com.daviga404.tinysnap2;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class TinySnapManager {

	public static List<TinySnapUploader> uploading = new ArrayList<TinySnapUploader>();
	
	public static void uploadSnap(BufferedImage snap) {
		
		TinySnapUploader t = new TinySnapUploader(snap);
		t.start();
		uploading.add(t);
		
	}
	
}
