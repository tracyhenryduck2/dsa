package com.henry.ecdemo.common.view;

import android.content.Context;

import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

public class CCPButton extends CCPImageButton {

	public CCPButton(Context context) {
		this(context , null , 0);
	}

	public CCPButton(Context context, AttributeSet attrs) {
		this(context, attrs , 0);
	}

	public CCPButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init();
		
	}

	private void init() {
		FrameLayout.LayoutParams fLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT
				,FrameLayout.LayoutParams.WRAP_CONTENT);
		fLayoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		
		mImageView.setLayoutParams(fLayoutParams);
	}
	

	/**
	 * 
	 * @param resId
	 */
	public final void setCCPButtonBackground(int resId) {
		if(resId < 0) {
			return;
		}
		setBackgroundResource(resId);
	}
	
	/**
	 * 
	 * @param resId
	 */
	public final void setCCPButtonImageResource(int resId) {
		Drawable drawable = getResources().getDrawable(resId);
		setCCPButtonImageDrawable(drawable);
	}
	
	public final void setCCPButtonImageDrawable(Drawable drawable) {
		if(mImageView != null) {
			mImageView.setImageDrawable(drawable);
		}
	}
	
}
