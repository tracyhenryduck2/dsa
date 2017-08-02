
package com.henry.ecdemo.ui.chatting.model;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;

import com.henry.ecdemo.R;
import com.henry.ecdemo.ui.chatting.ChattingActivity;
import com.henry.ecdemo.ui.chatting.holder.BaseHolder;
import com.henry.ecdemo.ui.chatting.holder.DescriptionViewHolder;
import com.henry.ecdemo.ui.chatting.view.ChattingItemContainer;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.im.ECCallMessageBody;


public class CallTxRow extends BaseChattingRow {

	
	public CallTxRow(int type){
		super(type);
	}
	
	@Override
	public View buildChatView(LayoutInflater inflater, View convertView) {
        //we have a don't have a converView so we'll have to create a new one
        if (convertView == null ) {
            convertView = new ChattingItemContainer(inflater, R.layout.chatting_item_to);

            
            //use the view holder pattern to save of already looked up subviews
            DescriptionViewHolder holder = new DescriptionViewHolder(mRowType);
            convertView.setTag(holder.initBaseHolder(convertView, true));
        } 
		return convertView;
	}

	@Override
	public void buildChattingData(final Context context, BaseHolder baseHolder,
			ECMessage detail, int position) {
		
		DescriptionViewHolder holder = (DescriptionViewHolder) baseHolder;
		ECMessage message = detail;
		if(message != null&&message.getType()== ECMessage.Type.CALL) {
			ECCallMessageBody textBody = (ECCallMessageBody) message.getBody();
			holder.getDescTextView().setText(textBody.getCallText());
			holder.getDescTextView().setMovementMethod(LinkMovementMethod.getInstance());
			OnClickListener onClickListener = ((ChattingActivity) context).mChattingFragment.getChattingAdapter().getOnClickListener();
			getMsgStateResId(position, holder, detail, onClickListener);
		}
	}

	@Override
	public int getChatViewType() {

		return ChattingRowType.CALL_ROW_TO.ordinal();
	}

	@Override
	public boolean onCreateRowContextMenu(ContextMenu contextMenu,
			View targetView, ECMessage detail) {

		return false;
	}
}
