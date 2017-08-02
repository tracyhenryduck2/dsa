
package com.henry.ecdemo.ui.chatting.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public abstract class ChatFooterPanel extends LinearLayout {

	protected EmojiGrid.OnEmojiItemClickListener mItemClickListener;
	
	/**
	 * @param context
	 * @param attrs
	 */
	public ChatFooterPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	 /**
     * Register a callback to be invoked when an item in this EmojiGird View has
     * been clicked.
     *
     * @param listener The callback that will be invoked.
     */
	protected void setOnEmojiItemClickListener(EmojiGrid.OnEmojiItemClickListener listener) {
    	mItemClickListener = listener;
    }

    /**
     * @return The callback to be invoked with an item in this EmojiGird View has
     *         been clicked, or null id no callback has been set.
     */
    public final EmojiGrid.OnEmojiItemClickListener getOnEmojiItemClickListener() {
        return mItemClickListener;
    }

	public void onDestroy() {
		
	}
	
	public abstract void setChatFooterPanelHeight(int height);
	
	/**
	 * {@link com.henry.ecdemo.ui.chatting.view.ChatFooterPanel} onPause
	 */
	public abstract void onPause();

	/**
	 * {@link com.henry.ecdemo.ui.chatting.view.ChatFooterPanel} onResume
	 */
	public abstract void onResume();

	/**
	 * {@link com.henry.ecdemo.ui.chatting.view.ChatFooterPanel} reset
	 */
	public abstract void reset();
}

