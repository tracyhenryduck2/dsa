
package com.henry.ecdemo.ui.chatting.view;

import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;


public class RecordPopupWindow extends PopupWindow {

	public RecordPopupWindow(View contentView) {
		super(contentView);
	}

	public RecordPopupWindow(View contentView, int width, int height) {
		super(contentView, width, height, false);
	}

	public RecordPopupWindow(View contentView, int width, int height,
			boolean focusable) {
		super(contentView, width, height, focusable);
	}
	
	public void dismiss() {

		try {
			super.dismiss();
		} catch (Exception e) {
			Log.d("MicroMsg.MMPopupWindow", "dismiss exception, e = " + e.getMessage());
			e.printStackTrace();
		}
    }
}
