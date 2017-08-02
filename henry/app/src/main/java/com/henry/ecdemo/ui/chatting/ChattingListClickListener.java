
package com.henry.ecdemo.ui.chatting;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.henry.ecdemo.common.CCPAppManager;
import com.henry.ecdemo.common.utils.CheckUtil;
import com.henry.ecdemo.common.utils.FileAccessor;
import com.henry.ecdemo.common.utils.MediaPlayTools;
import com.henry.ecdemo.storage.ContactSqlManager;
import com.henry.ecdemo.storage.IMessageSqlManager;
import com.henry.ecdemo.storage.ImgInfoSqlManager;
import com.henry.ecdemo.ui.chatting.model.ViewHolderTag;
import com.henry.ecdemo.ui.chatting.redpacketutils.CheckRedPacketMessageUtil;
import com.henry.ecdemo.ui.contact.ECContacts;
import com.henry.ecdemo.ui.settings.WebAboutActivity;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECMessage.Direction;
import com.yuntongxun.ecsdk.ECMessage.Type;
import com.yuntongxun.ecsdk.im.ECFileMessageBody;
import com.yuntongxun.ecsdk.im.ECPreviewMessageBody;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;
import com.yuntongxun.ecsdk.im.ECVideoMessageBody;
import com.yuntongxun.ecsdk.im.ECVoiceMessageBody;
import com.yunzhanghu.redpacketsdk.constant.RPConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utils.RedPacketConstant;
import utils.RedPacketUtil;

/**
 * 处理聊天消息点击事件响应
 */
public class ChattingListClickListener implements View.OnClickListener{

	/**聊天界面*/
	private ChattingActivity mContext;
	
	public ChattingListClickListener(ChattingActivity activity , String userName) {
		mContext = activity;
	}
	
	@Override
	public void onClick(View v) {
		ViewHolderTag holder = (ViewHolderTag) v.getTag();
		ECMessage iMessage = holder.detail;
		
		switch (holder.type) {
		case ViewHolderTag.TagType.TAG_VIEW_FILE:
			if(iMessage.getType()==Type.VIDEO){
				ECVideoMessageBody videoBody=(ECVideoMessageBody) iMessage.getBody();
		        File file =new File(FileAccessor.getFilePathName(),videoBody.getFileName());
		        
		        if(file.exists()){
		        	if(iMessage.getDirection()==Direction.RECEIVE&&CCPAppManager.getClientUser().getUserId().equals(iMessage.getForm())){
		        		CCPAppManager.doViewFilePrevieIntent(mContext, file.getAbsolutePath());
		        	}else {
					CCPAppManager.doViewFilePrevieIntent(mContext, videoBody.getLocalUrl());
		        	}
		        	
		        }else {
		        	mContext.mChattingFragment.showProcessDialog();
					videoBody.setLocalUrl(new File(FileAccessor .getFilePathName(), videoBody.getFileName()) .getAbsolutePath());
		        	ECDevice.getECChatManager().downloadMediaMessage(iMessage, IMChattingHelper.getInstance());
		        }
		        return;
			}
			ECFileMessageBody body = (ECFileMessageBody) holder.detail.getBody();
			CCPAppManager.doViewFilePrevieIntent(mContext, body.getLocalUrl());
			break;

		case ViewHolderTag.TagType.TAG_VOICE:
			if(iMessage == null) {
				return ;
			}
			MediaPlayTools instance = MediaPlayTools.getInstance();
			final ChattingListAdapter2 adapterForce = mContext.mChattingFragment.getChattingAdapter();
			if(instance.isPlaying()) {
				instance.stop();
			}
			if(adapterForce.mVoicePosition == holder.position) {
				adapterForce.mVoicePosition = -1;
				adapterForce.notifyDataSetChanged();
				return ;
			}
			
			instance.setOnVoicePlayCompletionListener(new MediaPlayTools.OnVoicePlayCompletionListener() {
				
				@Override
				public void OnVoicePlayCompletion() {
					adapterForce.mVoicePosition = -1;
					adapterForce.notifyDataSetChanged();
				}
			});
			ECVoiceMessageBody voiceBody = (ECVoiceMessageBody) holder.detail.getBody();
			String fileLocalPath = voiceBody.getLocalUrl();
			instance.playVoice(fileLocalPath, false);
			adapterForce.setVoicePosition(holder.position);
			adapterForce.notifyDataSetChanged();

			break;
			
		case ViewHolderTag.TagType.TAG_VIEW_PICTURE:
			if(iMessage != null) {
				List<String> msgids = IMessageSqlManager.getImageMessageIdSession(mContext.mChattingFragment.getmThread());
				if(msgids == null || msgids.isEmpty()) {
					return ;
				}
				int position = 0;
				ArrayList<ViewImageInfo> urls = (ArrayList<ViewImageInfo>) ImgInfoSqlManager.getInstance().getViewImageInfos(msgids);
				if(urls == null || urls.isEmpty()) {
					return ;
				}
				for(int i = 0 ; i < urls.size() ; i ++) {
					if(urls.get(i) != null&& urls.get(i).getMsgLocalId().equals(iMessage.getMsgId())) {
						position = i;
						break;
					}
				}
				String picMsgId =msgids.get(position);
				CCPAppManager.startChattingImageViewAction(mContext,position , urls,picMsgId);
				ImageGralleryPagerActivity.isFireMsg=IMessageSqlManager.isFireMsg(iMessage.getMsgId());
				msgids.clear();
			}
			break;
			
		case ViewHolderTag.TagType.TAG_RESEND_MSG :
			
			mContext.mChattingFragment.doResendMsgRetryTips(iMessage, holder.position);
			break;
		case ViewHolderTag.TagType.TAG_IM_LOCATION :
			
			CCPAppManager.startShowBaiDuMapAction(mContext,iMessage);
			break;

			case ViewHolderTag.TagType.TAG_IM_RICH_TEXT:
				doClickRichTextAction(iMessage);
				break;

			case ViewHolderTag.TagType.TAG_IM_TEXT:

				ECTextMessageBody textBody = (ECTextMessageBody)iMessage.getBody();
				String content 	= textBody.getMessage();

				if(TextUtils.isEmpty(content)){
					return;
				}
				content = content.trim();
				if(content.startsWith("www.")||content.startsWith("http://")||content.startsWith("https://")){
					startWebActivity(content);
				}

				break;
			case ViewHolderTag.TagType.TAG_IM_REDPACKET:

				JSONObject jsonRedPacket = CheckRedPacketMessageUtil.isRedPacketMessage(iMessage);
				JSONObject jsonObject = new JSONObject();
				String toAvatarUrl = "none";
				String toNickName = CCPAppManager.getClientUser().getUserName();
				toAvatarUrl = TextUtils.isEmpty(toAvatarUrl) ? "none" : toAvatarUrl;
				toNickName = TextUtils.isEmpty(toNickName) ? CCPAppManager.getClientUser().getUserId() : toNickName;
				jsonObject.put(RedPacketConstant.KEY_TO_AVATAR_URL, toAvatarUrl);
				jsonObject.put(RedPacketConstant.KEY_TO_NICK_NAME, toNickName);
				jsonObject.put(RedPacketConstant.KEY_CURRENT_ID, CCPAppManager.getClientUser().getUserId());

				if (iMessage.getDirection() == Direction.RECEIVE) {
					jsonObject.put(RedPacketConstant.KEY_MESSAGE_DIRECT, RPConstant.MESSAGE_DIRECT_RECEIVE);
				} else {
					jsonObject.put(RedPacketConstant.KEY_MESSAGE_DIRECT, RPConstant.MESSAGE_DIRECT_SEND);
				}
				String moneyID = jsonRedPacket.getString(RPConstant.EXTRA_RED_PACKET_ID);
				jsonObject.put(RPConstant.EXTRA_RED_PACKET_ID, moneyID);
				if (mContext.mChattingFragment.isPeerChat()) {
					jsonObject.put("chatType", RPConstant.CHATTYPE_GROUP);
				} else {
					jsonObject.put("chatType", RPConstant.CHATTYPE_SINGLE);
				}
				String specialAvatarUrl = "none";
				String specialNickname = "";
				String packetType = jsonRedPacket.getString(RedPacketConstant.MESSAGE_ATTR_RED_PACKET_TYPE);
				String specialReceiveId = jsonRedPacket.getString(RedPacketConstant.MESSAGE_ATTR_SPECIAL_RECEIVER_ID);
				if (!TextUtils.isEmpty(packetType) && packetType.equals(RedPacketConstant.GROUP_RED_PACKET_TYPE_EXCLUSIVE)) {
					ECContacts contact = ContactSqlManager.getContact(specialReceiveId);
					if (contact != null) {
						specialNickname = contact.getNickname();
					} else {
						specialNickname = specialReceiveId;
					}
				}
				jsonObject.put(RedPacketConstant.MESSAGE_ATTR_SPECIAL_RECEIVER_ID, specialReceiveId);
				jsonObject.put(RedPacketConstant.MESSAGE_ATTR_RED_PACKET_TYPE, packetType);
				jsonObject.put(RedPacketConstant.KEY_SPECIAL_AVATAR_URL, specialAvatarUrl);
				jsonObject.put(RedPacketConstant.KEY_SPECIAL_NICK_NAME, specialNickname);
				RedPacketUtil.openRedPacket(mContext, jsonObject, new RedPacketUtil.OpenRedPacketSuccess() {

					@Override
					public void onSuccess(String senderId, String senderNickname) {
						mContext.mChattingFragment.sendRedPacketAckMessage(senderId, senderNickname);
					}
				});
				break;
			default:
			break;
		}
	}


	private void doClickRichTextAction(ECMessage iMessage) {

		ECPreviewMessageBody body=(ECPreviewMessageBody)iMessage.getBody();
		String url=body.getUrl();
		if(!CheckUtil.isVailUrl(url)){
			return;
		}
		startWebActivity(url);
	}


	private  void startWebActivity(String url){
		Intent intent=new Intent(mContext, WebAboutActivity.class);
		intent.putExtra("url",url);
		mContext.startActivity(intent);

	}


}
