package com.henry.ecdemo.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.henry.ecdemo.R;
import com.henry.ecdemo.common.CCPAppManager;
import com.henry.ecdemo.common.dialog.ECListDialog;
import com.henry.ecdemo.common.dialog.ECProgressDialog;
import com.henry.ecdemo.common.utils.ECPreferenceSettings;
import com.henry.ecdemo.common.utils.ECPreferences;
import com.henry.ecdemo.common.utils.LogUtil;
import com.henry.ecdemo.common.utils.ToastUtil;
import com.henry.ecdemo.common.view.NetWarnBannerView;
import com.henry.ecdemo.storage.ConversationSqlManager;
import com.henry.ecdemo.storage.GroupSqlManager;
import com.henry.ecdemo.storage.IMessageSqlManager;
import com.henry.ecdemo.ui.chatting.CustomerServiceHelper;
import com.henry.ecdemo.ui.chatting.model.Conversation;
import com.henry.ecdemo.ui.contact.ContactLogic;
import com.henry.ecdemo.ui.group.GroupNoticeActivity;
import com.henry.ecdemo.ui.group.GroupService;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.im.ECGroupOption;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import java.util.ArrayList;

/**
 * 会话界面
 */
public class ConversationListFragment extends TabFragment implements CCPListAdapter.OnListAdapterCallBackListener {

    private static final String TAG = "henry_dianying.ConversationListFragment";


    /**会话消息列表ListView*/
    private ListView mListView;
    private NetWarnBannerView mBannerView;
    private ConversationAdapter mAdapter;
    private OnUpdateMsgUnreadCountsListener mAttachListener;
    private ECProgressDialog mPostingdialog;

    final private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View visew, int position,
                                long id) {

            if(mAdapter != null) {
                int headerViewsCount = mListView.getHeaderViewsCount();
                if(position < headerViewsCount) {
                    return;
                }
                int _position = position - headerViewsCount;

                if(mAdapter == null || mAdapter.getItem(_position) == null) {
                    return ;
                }
                Conversation conversation = mAdapter.getItem(_position);
                     int  type =  conversation.getMsgType();
                if(type == 1000) {
                    Intent intent = new Intent(getActivity() , GroupNoticeActivity.class);
                    startActivity(intent);
                    return ;
                }
                if(ContactLogic.isCustomService(conversation.getSessionId())) {
                    showProcessDialog();
                    dispatchCustomerService(conversation.getSessionId());
                    return ;
                }

                CCPAppManager.startChattingAction(getActivity() , conversation.getSessionId() , conversation.getUsername());
            }
        }
    };

    /**
     * 处理在线客服界面请求
     * @param sessionId
     */
    private void dispatchCustomerService(String sessionId) {
        CustomerServiceHelper.startService(sessionId, new CustomerServiceHelper.OnStartCustomerServiceListener() {
            @Override
            public void onServiceStart(String event) {
                dismissPostingDialog();
                CCPAppManager.startCustomerServiceAction(getActivity() , event);
            }

            @Override
            public void onError(ECError error) {
                dismissPostingDialog();
            }
        });
    }

    private final AdapterView.OnItemLongClickListener mOnLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if(mAdapter != null) {
                int headerViewsCount = mListView.getHeaderViewsCount();
                if (position < headerViewsCount) {
                    return false;
                }
                int _position = position - headerViewsCount;

                if (mAdapter == null || mAdapter.getItem(_position) == null) {
                    return false;
                }
                Conversation conversation = mAdapter.getItem(_position);
                final int itemPosition = position;
                final String[] menu = buildMenu(conversation);
                ECListDialog dialog = new ECListDialog(getActivity() , /*new String[]{getString(R.string.main_delete)}*/menu);
                dialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
                    @Override
                    public void onDialogItemClick(Dialog d, int position) {
                        handleContentMenuClick(itemPosition ,position);
                    }
                });
                dialog.setTitle(conversation.getUsername());
                dialog.show();
                return true;
            }
            return false;
        }
    };


    private String[] buildMenu(Conversation conversation) {//设置长按条目 2*2
        if(conversation != null && conversation.getSessionId() != null) {
            boolean isTop = ConversationSqlManager.querySessionisTopBySessionId(conversation.getSessionId());//支持单人、群组
            if(conversation.getSessionId().toLowerCase().startsWith("g")) {
                ECGroup ecGroup = GroupSqlManager.getECGroup(conversation.getSessionId());
                boolean isNotice =ecGroup.isNotice();
                if(ecGroup == null || !GroupSqlManager.getJoinState(ecGroup.getGroupId())) {
                    return new String[]{getString(R.string.main_delete)};
                }
                if(ecGroup.isNotice()) {
                    if(isTop) {
                        return new String[]{getString(R.string.main_delete) ,getString(R.string.cancel_top),getString(R.string.menu_mute_notify)};

                    }else {
                        return new String[]{getString(R.string.main_delete) ,getString(R.string.set_top),getString(R.string.menu_mute_notify)};
                    }
                }else {
                    if(isTop){
                        return new String[]{getString(R.string.main_delete) ,getString(R.string.cancel_top),getString(R.string.menu_notify)};
                    }else {
                        return new String[]{getString(R.string.main_delete) ,getString(R.string.set_top),getString(R.string.menu_notify)};

                    }

                }
            }else {
                if(isTop){
                    return new String[]{getString(R.string.main_delete) ,getString(R.string.cancel_top)};
                }else {
                    return new String[]{getString(R.string.main_delete) ,getString(R.string.set_top)};

                }

            }
        }
        return new String[]{getString(R.string.main_delete)};
    }

    @Override
    protected void onTabFragmentClick() {

    }

    @Override
    protected void onReleaseTabUI() {

    }


    private void setcancelTopSession(ArrayList<String> arrayList ,String item){
        if(!arrayList.contains(item)){
            ConversationSqlManager.updateSessionToTop(item,false);
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        registerReceiver(new String[]{GroupService.ACTION_SYNC_GROUP, IMessageSqlManager.ACTION_SESSION_DEL});

    }

    @Override
    public void onResume() {
        super.onResume();
        updateConnectState();
        IMessageSqlManager.registerMsgObserver(mAdapter);
        mAdapter.notifyChange();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mAttachListener = (OnUpdateMsgUnreadCountsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnUpdateMsgUnreadCountsListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        IMessageSqlManager.unregisterMsgObserver(mAdapter);
    }

    /**
     *
     */
    private void initView() {
        if(mListView != null) {
            mListView.setAdapter(null);

            if(mBannerView != null) {
                mListView.removeHeaderView(mBannerView);
            }
        }

        mListView = (ListView) findViewById(R.id.main_chatting_lv);
        View mEmptyView = findViewById(R.id.empty_conversation_tv);
        mListView.setEmptyView(mEmptyView);
        mListView.setDrawingCacheEnabled(false);
        mListView.setScrollingCacheEnabled(false);

        mListView.setOnItemLongClickListener(mOnLongClickListener);
        mListView.setOnItemClickListener(mItemClickListener);
        mBannerView = new NetWarnBannerView(getActivity());
        mBannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reTryConnect();
            }
        });
        mListView.addHeaderView(mBannerView);
        mAdapter = new ConversationAdapter(getActivity() , this);
        mListView.setAdapter(mAdapter);

        registerForContextMenu(mListView);
    }
    
    
    private String getAutoRegistAccount() {
		SharedPreferences sharedPreferences = ECPreferences
				.getSharedPreferences();
		ECPreferenceSettings registAuto = ECPreferenceSettings.SETTINGS_REGIST_AUTO;
		String registAccount = sharedPreferences.getString(registAuto.getId(),
				(String) registAuto.getDefaultValue());
		return registAccount;
	}

    private void reTryConnect() {
        ECDevice.ECConnectState connectState = SDKCoreHelper.getConnectState();
        if(connectState == null || connectState == ECDevice.ECConnectState.CONNECT_FAILED) {
        	
        	if(!TextUtils.isEmpty(getAutoRegistAccount())){
            SDKCoreHelper.init(getActivity());
        	}
        }
    }

    public void  updateConnectState() {
        if(!isAdded()) {
            return;
        }
        ECDevice.ECConnectState connect = SDKCoreHelper.getConnectState();
        if(connect == ECDevice.ECConnectState.CONNECTING) {
            mBannerView.setNetWarnText(getString(R.string.connecting_server));
            mBannerView.reconnect(true);
        } else if (connect == ECDevice.ECConnectState.CONNECT_FAILED) {
            mBannerView.setNetWarnText(getString(R.string.connect_server_error));
            mBannerView.reconnect(false);
        } else if (connect == ECDevice.ECConnectState.CONNECT_SUCCESS) {
            mBannerView.hideWarnBannerView();
        }
        LogUtil.d(TAG, "updateConnectState connect :" + connect.name());
    }


    private Boolean handleContentMenuClick(int convresion ,int position) {
            if(mAdapter != null) {
            int headerViewsCount = mListView.getHeaderViewsCount();
            if (convresion < headerViewsCount) {
                return false;
            }
            int _position = convresion - headerViewsCount;

            if (mAdapter == null || mAdapter.getItem(_position) == null) {
                return false;
            }
            final Conversation conversation = mAdapter.getItem(_position);
            switch (position) {
                case 0:
                    showProcessDialog();
                    ECHandlerHelper handlerHelper = new ECHandlerHelper();
                    handlerHelper.postRunnOnThead(new Runnable() {
                        @Override
                        public void run() {
                            IMessageSqlManager.deleteChattingMessage(conversation.getSessionId());
                            ToastUtil.showMessage(R.string.clear_msg_success);
                            ConversationListFragment.this.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dismissPostingDialog();
                                    mAdapter.notifyChange();
                                }
                            });
                        }
                    });
                    break;
                case 2:
                    showProcessDialog();
                    final boolean notify = GroupSqlManager.isGroupNotify(conversation.getSessionId());
                    ECGroupOption option = new ECGroupOption();
                    option.setGroupId(conversation.getSessionId());
                    option.setRule(notify ? ECGroupOption.Rule.SILENCE :ECGroupOption.Rule.NORMAL);
                    GroupService.setGroupMessageOption(option, new GroupService.GroupOptionCallback() {
                        @Override
                        public void onComplete(String groupId) {
                            if(mAdapter != null) {
                                mAdapter.notifyChange();
                            }
                            ToastUtil.showMessage(notify?R.string.new_msg_mute_notify : R.string.new_msg_notify);
                            dismissPostingDialog();
                        }

                        @Override
                        public void onError(ECError error) {
                            dismissPostingDialog();
                            ToastUtil.showMessage("设置失败");
                        }
                    });
                    break;

                case 1 :
                    showProcessDialog();
                    final boolean isTop = ConversationSqlManager.querySessionisTopBySessionId(conversation.getSessionId());
                    ECChatManager chatManager = SDKCoreHelper.getECChatManager();
                    if(chatManager ==null){
                        return null;
                    }
                    chatManager.setSessionToTop(conversation.getSessionId(), !isTop, new ECChatManager.OnSetContactToTopListener() {
                        @Override
                        public void onSetContactResult(ECError error, String contact) {

                            dismissPostingDialog();
                            if(error.errorCode == SdkErrorCode.REQUEST_SUCCESS){
                                ConversationSqlManager.updateSessionToTop(conversation.getSessionId(),!isTop);
                                mAdapter.notifyChange();
                                ToastUtil.showMessage("设置成功");
                            }else {
                                ToastUtil.showMessage("设置失败");
                            }
                        }
                    });
                    break;
                default:
                    break;
            }
        }
        return null;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.conversation;
    }

    @Override
    public void OnListAdapterCallBack() {
        if(mAttachListener != null) {
            mAttachListener.OnUpdateMsgUnreadCounts();
        }
    }

    public interface OnUpdateMsgUnreadCountsListener {
        void OnUpdateMsgUnreadCounts();
    }

    @Override
    protected void handleReceiver(Context context, Intent intent) {
        super.handleReceiver(context, intent);
        if(GroupService.ACTION_SYNC_GROUP.equals(intent.getAction())
                || IMessageSqlManager.ACTION_SESSION_DEL.equals(intent.getAction())) {
            if(mAdapter != null) {
                mAdapter.notifyChange();
            }
        }
    }

    void showProcessDialog() {
        mPostingdialog = new ECProgressDialog(ConversationListFragment.this.getActivity(), R.string.login_posting_submit);
        mPostingdialog.show();
    }

    /**
     * 关闭对话框
     */
    private void dismissPostingDialog() {
        if(mPostingdialog == null || !mPostingdialog.isShowing()) {
            return ;
        }
        mPostingdialog.dismiss();
        mPostingdialog = null;
    }
}

