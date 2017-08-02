
package com.henry.ecdemo.ui.chatting.model;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.view.ContextMenu;
import android.view.View;
import android.widget.TextView;

import com.henry.ecdemo.R;
import com.henry.ecdemo.common.utils.LogUtil;
import com.henry.ecdemo.storage.ContactSqlManager;
import com.henry.ecdemo.ui.chatting.ChattingActivity;
import com.henry.ecdemo.ui.chatting.holder.BaseHolder;
import com.henry.ecdemo.ui.chatting.holder.RedPacketAckViewHolder;
import com.henry.ecdemo.ui.contact.ContactDetailActivity;
import com.henry.ecdemo.ui.contact.ContactLogic;
import com.henry.ecdemo.ui.contact.ECContacts;
import com.yuntongxun.ecsdk.ECMessage;

import java.util.HashMap;


public abstract class BaseChattingRow implements IChattingRow {

    public static final String TAG = LogUtil.getLogUtilsTag(BaseChattingRow.class);
    private HashMap<String, String> hashMap = new HashMap<String, String>();
    int mRowType;

    public BaseChattingRow(int  type) {
        mRowType = type;
    }

    /**
     * 处理消息的发送状态设置
     * @param position 消息的列表所在位置
     * @param holder 消息ViewHolder
     * @param l
     */
    protected static void getMsgStateResId(int position , BaseHolder holder , ECMessage msg , View.OnClickListener l){
        if(msg != null && msg.getDirection() == ECMessage.Direction.SEND) {
            ECMessage.MessageStatus msgStatus = msg.getMsgStatus();
            if(msgStatus == ECMessage.MessageStatus.FAILED) {
                holder.getUploadState().setImageResource(R.drawable.msg_state_failed_resend);
                holder.getUploadState().setVisibility(View.VISIBLE);
                if(holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.GONE);
                }
            } else  if (msgStatus == ECMessage.MessageStatus.SUCCESS || msgStatus == ECMessage.MessageStatus.RECEIVE) {
                holder.getUploadState().setImageResource(0);
                holder.getUploadState().setVisibility(View.GONE);
                if(holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.GONE);
                }

            } else  if (msgStatus == ECMessage.MessageStatus.SENDING) {
                holder.getUploadState().setImageResource(0);
                holder.getUploadState().setVisibility(View.GONE);
                if(holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.VISIBLE);
                }
//                if(msg!=null&&msg.getType()==Type.FILE){
//                	holder.getUploadState().setImageResource(R.drawable.msg_state_failed_resend);
//                    holder.getUploadState().setVisibility(View.VISIBLE);
//                    if(holder.getUploadProgressBar() != null) {
//                        holder.getUploadProgressBar().setVisibility(View.GONE);
//                    }
//                }

            } else {
                if(holder.getUploadProgressBar() != null) {
                    holder.getUploadProgressBar().setVisibility(View.GONE);
                }
                LogUtil.d(TAG, "getMsgStateResId: not found this state");
            }

            ViewHolderTag holderTag = ViewHolderTag.createTag(msg, ViewHolderTag.TagType.TAG_RESEND_MSG , position);
            holder.getUploadState().setTag(holderTag);
            holder.getUploadState().setOnClickListener(l);
        }
    }


    public void setAutoLinkForTextView(TextView tv){

        if(tv!=null){
               String text = tv.getText().toString();
               if(!TextUtils.isEmpty(text)){
                   if(text.startsWith("http://")||text.startsWith("https://")||text.startsWith("www.")){
                       tv.setAutoLinkMask(Linkify.WEB_URLS);
                   }
               }
        }
    }

    /**
     *
     * @param contextMenu
     * @param targetView
     * @param detail
     * @return
     */
    public abstract boolean onCreateRowContextMenu(ContextMenu contextMenu , View targetView , ECMessage detail);


    /**
     *
     * @param baseHolder
     * @param displayName
     */
    public static void setDisplayName(BaseHolder baseHolder , String displayName) {
        if(baseHolder == null || baseHolder.getChattingUser() == null) {
            return ;
        }

        if(TextUtils.isEmpty(displayName)) {
            baseHolder.getChattingUser().setVisibility(View.GONE);
            return ;
        }

        if(!(baseHolder instanceof  RedPacketAckViewHolder)){
            baseHolder.getChattingUser().setText(displayName);
            baseHolder.getChattingUser().setVisibility(View.VISIBLE);
        }
    }

    protected abstract void buildChattingData(Context context , BaseHolder baseHolder , ECMessage detail , int position);

    @Override
    public void buildChattingBaseData(Context context, BaseHolder baseHolder, ECMessage detail, int position) {

        // 处理其他使用逻辑
        buildChattingData(context, baseHolder, detail, position);
        setContactPhoto(baseHolder , detail);
        if(((ChattingActivity) context).isPeerChat() && detail.getDirection() == ECMessage.Direction.RECEIVE) {
            ECContacts contact = ContactSqlManager.getContact(detail.getForm());
            if( contact != null) {
                if(TextUtils.isEmpty(contact.getNickname())) {
                    contact.setNickname(contact.getContactid());
                }
                setDisplayName(baseHolder, contact.getNickname());
            } else {
                setDisplayName(baseHolder, detail.getForm());
            }
        }
        setContactPhotoClickListener(context ,baseHolder , detail);
    }

    private void setContactPhotoClickListener(final Context context ,BaseHolder baseHolder, final ECMessage detail) {
        if(baseHolder.getChattingAvatar() != null && detail != null) {
            baseHolder.getChattingAvatar().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ECContacts contact = ContactSqlManager.getContact(detail.getForm());
                    if(contact == null || contact.getId() == -1) {
                        return ;
                    }
                    Intent intent = new Intent(context, ContactDetailActivity.class);
                    intent.putExtra(ContactDetailActivity.RAW_ID, contact.getId());
                    context.startActivity(intent);
                }
            });

            baseHolder.getChattingAvatar().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(context instanceof  ChattingActivity) {
                        final ChattingActivity activity = (ChattingActivity) context;
                        if(activity.isPeerChat() &&  !activity.mChattingFragment.mAtsomeone) {
                            activity.mChattingFragment.mAtsomeone = true;
                            // 群组
                            ECContacts contact = ContactSqlManager.getContact(detail.getForm());
                            if(contact != null) {
                                if(TextUtils.isEmpty(contact.getNickname() )) {
                                    contact.setNickname(contact.getContactid());
                                }
                                activity.mChattingFragment.getChattingFooter().setLastText(activity.mChattingFragment.getChattingFooter().getLastText() + "@" + contact.getNickname() + (char)(8197));
                                activity.mChattingFragment.getChattingFooter().putSomebody(contact);
                                activity.mChattingFragment.getChattingFooter().setMode(1);
                                v.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        activity.mChattingFragment.mAtsomeone = false;
                                    }
                                },2000L);
                            }
                        }
                    }

                    return true;
                }
            });
        }
    }


    /**
     * 添加用户头像
     * @param baseHolder
     * @param detail
     */
    private void setContactPhoto(BaseHolder baseHolder, ECMessage detail) {
        if(baseHolder.getChattingAvatar() != null) {
            try {
                if (TextUtils.isEmpty(detail.getForm())) {
                    return;
                }
                String userUin = "";
                if (hashMap.containsKey(detail.getForm())) {
                    userUin = hashMap.get(detail.getForm());
                } else {
                    userUin = ContactSqlManager.getContact(detail.getForm())
                            .getRemark();
                }
                baseHolder.getChattingAvatar().setImageBitmap(
                        ContactLogic.getPhoto(userUin));
            } catch (Exception e) {
            }
        }
    }

}
