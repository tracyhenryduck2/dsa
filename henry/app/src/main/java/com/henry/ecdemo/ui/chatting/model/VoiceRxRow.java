
package com.henry.ecdemo.ui.chatting.model;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;

import com.henry.ecdemo.R;
import com.henry.ecdemo.ui.chatting.ChattingActivity;
import com.henry.ecdemo.ui.chatting.holder.BaseHolder;
import com.henry.ecdemo.ui.chatting.holder.VoiceRowViewHolder;
import com.henry.ecdemo.ui.chatting.view.ChattingItemContainer;
import com.yuntongxun.ecsdk.ECMessage;

public class VoiceRxRow extends BaseChattingRow {

	public VoiceRxRow(int type) {
		super(type);
	}
	
	@Override
	public View buildChatView(LayoutInflater inflater, View convertView) {
        //we have a don't have a converView so we'll have to create a new one
        if (convertView == null) {
            convertView = new ChattingItemContainer(inflater, R.layout.chatting_item_from_voice);
            
            //use the view holder pattern to save of already looked up subviews
            VoiceRowViewHolder holder = new VoiceRowViewHolder(mRowType);
            convertView.setTag(holder.initBaseHolder(convertView, true));
        } 
		return convertView;
	}

	@Override
	public void buildChattingData(Context context, BaseHolder baseHolder,
			final ECMessage detail, int position) {
		
		VoiceRowViewHolder holder = (VoiceRowViewHolder) baseHolder;
        if(detail != null) {
        	VoiceRowViewHolder.initVoiceRow(holder, detail, position, (ChattingActivity) context, true);
        	holder.voiceAnim.setVoiceFrom(true);
        }
	}
	

	@Override
	public int getChatViewType() {

		return ChattingRowType.VOICE_ROW_RECEIVED.ordinal();
	}

	@Override
	public boolean onCreateRowContextMenu(ContextMenu contextMenu,
			View targetView, ECMessage detail) {
		// TODO Auto-generated method stub
		return false;
	}

}
