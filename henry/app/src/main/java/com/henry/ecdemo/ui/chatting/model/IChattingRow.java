
package com.henry.ecdemo.ui.chatting.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.henry.ecdemo.ui.chatting.holder.BaseHolder;
import com.yuntongxun.ecsdk.ECMessage;

public interface IChattingRow {

    /**
     * Get a View that displays the data at the specified position in the data set
     * @param convertView
     * @return
     */
    View buildChatView(LayoutInflater inflater, View convertView);

    /**
     *
     * @param context
     * @param detail
     */
    void buildChattingBaseData(Context context, BaseHolder baseHolder, ECMessage detail, int position);

    /**
     * @return
     */
    int getChatViewType();

}
