
package com.henry.ecdemo.ui.chatting.model;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.henry.ecdemo.R;
import com.henry.ecdemo.ui.chatting.holder.BaseHolder;
import com.henry.ecdemo.ui.chatting.holder.SystemViewHolder;
import com.yuntongxun.ecsdk.ECMessage;


public class ChattingSystemRow extends BaseChattingRow {

	public ChattingSystemRow(int type) {
		super(type);
	}

	@Override
	public View buildChatView(LayoutInflater inflater, View convertView) {
		// we have a don't have a converView so we'll have to create a new one
		if (convertView == null || convertView.getTag() == null || ((BaseHolder)convertView.getTag()).getType() != mRowType) {
			convertView =  inflater.inflate(R.layout.chatting_item_system, null);

			// use the view holder pattern to save of already looked up subviews
			SystemViewHolder holder = new SystemViewHolder(mRowType);
			holder.setChattingTime((TextView) convertView.findViewById(R.id.chatting_time_tv));
			holder.mSystemView = (TextView) convertView.findViewById(R.id.chatting_content_itv);
			convertView.setTag(holder);
		} 
		return convertView;
	}

	@Override
	public void buildChattingData(Context context, BaseHolder baseHolder,
			ECMessage detail, int position) {

		SystemViewHolder holder = (SystemViewHolder) baseHolder;
		// actually setup the view
		ECMessage iMessage = detail;
		if(iMessage != null) {
//			holder.mSystemView.setText(iMessage.getUserData());
//			holder.mSystemView.invalidate(); yuntongxun009
			holder.getChattingTime().setText(iMessage.getUserData().substring(13,iMessage.getUserData().length()));
		}
	}
	

	@Override
	public int getChatViewType() {

		return ChattingRowType.CHATTING_SYSTEM.ordinal();
	}

	@Override
	public boolean onCreateRowContextMenu(ContextMenu contextMenu,
			View targetView, ECMessage detail) {

		return false;
	}
}
