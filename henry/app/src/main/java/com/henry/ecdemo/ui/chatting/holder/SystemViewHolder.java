
package com.henry.ecdemo.ui.chatting.holder;

import android.widget.TextView;

/**
 * <p>Title: ChattingSystemViewHolder.java</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2014</p>
 * <p>Company: Beijing Speedtong Information Technology Co.,Ltd</p>
 * @author Jorstin Chan
 * @date 2014-8-4
 * @version 1.0
 */
public class SystemViewHolder extends BaseHolder {

	public TextView mSystemView;
	/**
	 * @param type
	 */
	public SystemViewHolder(int type) {
		super(type);
	}

	@Override
	public TextView getReadTv() {
		return null;
	}

}
