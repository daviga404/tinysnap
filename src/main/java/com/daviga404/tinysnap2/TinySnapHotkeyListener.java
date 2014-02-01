package com.daviga404.tinysnap2;

import com.daviga404.tinysnap2.windows.WindowSnapper;
import com.melloware.jintellitype.HotkeyListener;

public class TinySnapHotkeyListener implements HotkeyListener {

	public void onHotKey(int id) {

		if (id == 1) {
			
			new WindowSnapper();
			
		}
		
	}
	
	
	
}
