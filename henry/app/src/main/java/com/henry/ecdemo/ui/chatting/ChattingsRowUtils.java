
package com.henry.ecdemo.ui.chatting;


import android.text.TextUtils;

import com.henry.ecdemo.ui.chatting.model.ChattingRowType;
import com.henry.ecdemo.ui.chatting.redpacketutils.CheckRedPacketMessageUtil;
import com.yuntongxun.ecsdk.ECMessage;

public class ChattingsRowUtils {

    /**
     *
     * @param
     * @return
     */
	public static int getChattingMessageType(ECMessage msg,String data) {
		ECMessage.Type type = msg.getType();
		if(type == ECMessage.Type.TXT) {
			if(!TextUtils.isEmpty(data)&&data.startsWith("yuntongxun009")){
				return 110;
			}
			if (CheckRedPacketMessageUtil.isRedPacketMessage(msg) != null) {
				return 7000;
			} else if (CheckRedPacketMessageUtil.isRedPacketAckMessage(msg) != null) {
				return 8000;
			}

			return 2000;
		} else if (type == ECMessage.Type.VOICE) {
			return 60;
		} else if (type == ECMessage.Type.FILE) {
			return 1024;
		} else if (type == ECMessage.Type.IMAGE) {
			return 200;
		}else if(type==ECMessage.Type.VIDEO){
			return 1024;
		}else if(type==ECMessage.Type.LOCATION){
			return 2200;
		}else if(type== ECMessage.Type.CALL){
			return  2400;
		}else if(type==ECMessage.Type.RICH_TEXT){
			return 2600;
		}
		return 2000;
	}

	/**
	 * 
	 * @param iMessage
	 * @return
	 */
	public static Integer getMessageRowType(ECMessage iMessage) {
		ECMessage.Type type = iMessage.getType();
		ECMessage.Direction direction = iMessage.getDirection();
		if(type == ECMessage.Type.TXT) {
			if(direction == ECMessage.Direction.RECEIVE) {
				return ChattingRowType.DESCRIPTION_ROW_RECEIVED.getId();
			}
			return ChattingRowType.DESCRIPTION_ROW_TRANSMIT.getId();
		} else if (type == ECMessage.Type.VOICE) {
			if(direction == ECMessage.Direction.RECEIVE) {
				return ChattingRowType.VOICE_ROW_RECEIVED.getId();
			}
			return ChattingRowType.VOICE_ROW_RECEIVED.getId();
		} else if (type == ECMessage.Type.FILE||type==ECMessage.Type.VIDEO) {
			if(direction == ECMessage.Direction.RECEIVE) {
				return ChattingRowType.FILE_ROW_RECEIVED.getId();
			}
			return ChattingRowType.FILE_ROW_RECEIVED.getId();
		} else if (type == ECMessage.Type.IMAGE) {
			if(direction == ECMessage.Direction.RECEIVE) {
				return ChattingRowType.IMAGE_ROW_RECEIVED.getId();
			}
			return ChattingRowType.IMAGE_ROW_RECEIVED.getId();
		}else if(type==ECMessage.Type.LOCATION){
			if(direction == ECMessage.Direction.RECEIVE) {
				return ChattingRowType.LOCATION_ROW_RECEIVED.getId();
			}
			return ChattingRowType.LOCATION_ROW_TRANSMIT.getId();
			
		}
		return -1;
	}
}
