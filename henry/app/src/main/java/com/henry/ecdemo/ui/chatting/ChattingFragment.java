package com.henry.ecdemo.ui.chatting;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
import com.henry.ecdemo.R;
import com.henry.ecdemo.common.CCPAppManager;
import com.henry.ecdemo.common.dialog.ECAlertDialog;
import com.henry.ecdemo.common.dialog.ECListDialog;
import com.henry.ecdemo.common.dialog.ECProgressDialog;
import com.henry.ecdemo.common.utils.ClipboardUtils;
import com.henry.ecdemo.common.utils.DemoUtils;
import com.henry.ecdemo.common.utils.ECNotificationManager;
import com.henry.ecdemo.common.utils.ECPreferenceSettings;
import com.henry.ecdemo.common.utils.ECPreferences;
import com.henry.ecdemo.common.utils.EmoticonUtil;
import com.henry.ecdemo.common.utils.FileAccessor;
import com.henry.ecdemo.common.utils.FileUtils;
import com.henry.ecdemo.common.utils.LogUtil;
import com.henry.ecdemo.common.utils.MediaPlayTools;
import com.henry.ecdemo.common.utils.ToastUtil;
import com.henry.ecdemo.core.ClientUser;
import com.henry.ecdemo.core.ECAsyncTask;
import com.henry.ecdemo.photopicker.PhotoPickerActivity;
import com.henry.ecdemo.pojo.ImUserState;
import com.henry.ecdemo.storage.ContactSqlManager;
import com.henry.ecdemo.storage.ConversationSqlManager;
import com.henry.ecdemo.storage.GroupSqlManager;
import com.henry.ecdemo.storage.IMessageSqlManager;
import com.henry.ecdemo.storage.ImgInfoSqlManager;
import com.henry.ecdemo.ui.CCPFragment;
import com.henry.ecdemo.ui.LocationActivity;
import com.henry.ecdemo.ui.LocationInfo;
import com.henry.ecdemo.ui.SDKCoreHelper;
import com.henry.ecdemo.ui.chatting.base.ECPullDownView;
import com.henry.ecdemo.ui.chatting.base.OnListViewBottomListener;
import com.henry.ecdemo.ui.chatting.base.OnListViewTopListener;
import com.henry.ecdemo.ui.chatting.base.OnRefreshAdapterDataListener;
import com.henry.ecdemo.ui.chatting.model.ImgInfo;
import com.henry.ecdemo.ui.chatting.view.CCPChattingFooter2;
import com.henry.ecdemo.ui.chatting.view.SmileyPanel;
import com.henry.ecdemo.ui.contact.AtSomeoneUI;
import com.henry.ecdemo.ui.contact.ContactDetailActivity;
import com.henry.ecdemo.ui.contact.ContactLogic;
import com.henry.ecdemo.ui.contact.ECContacts;
import com.henry.ecdemo.ui.group.GroupInfoActivity;
import com.henry.ecdemo.ui.plugin.FileExplorerActivity;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECChatManager.OnChangeVoiceListener;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECGroupManager;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.ECUserState;
import com.yuntongxun.ecsdk.Parameters;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECFileMessageBody;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.im.ECGroupMember;
import com.yuntongxun.ecsdk.im.ECImageMessageBody;
import com.yuntongxun.ecsdk.im.ECLocationMessageBody;
import com.yuntongxun.ecsdk.im.ECPreviewMessageBody;
import com.yuntongxun.ecsdk.im.ECTextMessageBody;
import com.yuntongxun.ecsdk.im.ECUserStateMessageBody;
import com.yuntongxun.ecsdk.im.ECVideoMessageBody;
import com.yuntongxun.ecsdk.im.ECVoiceMessageBody;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;
import com.yunzhanghu.redpacketsdk.bean.RPUserBean;
import com.yunzhanghu.redpacketui.callback.GroupMemberCallback;
import com.yunzhanghu.redpacketui.callback.NotifyGroupMemberCallback;
import com.yunzhanghu.redpacketui.utils.RPGroupMemberUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.List;

import utils.RedPacketConstant;
import utils.RedPacketUtil;

public class ChattingFragment extends CCPFragment implements
		View.OnClickListener, AbsListView.OnScrollListener,
		IMChattingHelper.OnMessageReportCallback,
		CustomerServiceHelper.OnCustomerServiceListener {

    public static final String TAG = "henry_dianying.ChattingFragment";
    private static final int WHAT_ON_COMPUTATION_TIME = 10000;
    /**request code for tack pic*/
    public static final int REQUEST_CODE_TAKE_PICTURE = 0x3;
    public static final int REQUEST_CODE_TAKE_LOCATION = 0x11;
    public static final int REQUEST_CODE_LOAD_IMAGE = 0x4;
    public static final int REQUEST_CODE_IMAGE_CROP = 0x5;
    /**查看名片*/
    public static final int REQUEST_VIEW_CARD = 0x6;
    /**选择回复联系人*/
    public static final int SELECT_AT_SOMONE = 0xD4;

	/** 会话ID，数据库主键 */
	public final static String THREAD_ID = "thread_id";
	/** 联系人账号 */
	public final static String RECIPIENTS = "recipients";
	/** 联系人名称 */
	public final static String CONTACT_USER = "contact_user";
	public final static String FROM_CHATTING_ACTIVITY = "from_chatting_activity";
	public final static String CUSTOMER_SERVICE = "is_customer_service";
	/** 按键振动时长 */
	public static final int TONE_LENGTH_MS = 200;
	/** 音量值 */
	private static final float TONE_RELATIVE_VOLUME = 100.0F;
	/** 待发送的语音文件最短时长 */
	private static final int MIX_TIME = 1000;
	/** 聊天界面消息适配器 */
	private ChattingListAdapter2 mChattingAdapter;
	/** 界面消息下拉刷新 */
	// private RefreshableView mPullDownView;
	// private long mPageCount;
	/** 历史聊天纪录消息显示View */
	private ListView mListView;
	private View mListViewHeadView;
	/** 聊天界面附加聊天控件面板 */
	private CCPChattingFooter2 mChattingFooter;
	/** 选择图片拍照路径 */
	private String mFilePath;
	/** 会话ID */
	private long mThread = -1;
	/** 会话联系人账号 */
	private String mRecipients;
	/** 联系人名称 */
	private String mUsername;
	/** 计算当前录音时长 */
	private long computationTime = -1L;
	/** 当前语言录制文件的时间长度 */
	private int mVoiceRecodeTime = 0;
	/** 是否使用边录制便传送模式发送语音 */
	private boolean isRecordAndSend = false;
	/** 手机震动API */
	private Vibrator mVibrator;
	private ToneGenerator mToneGenerator;
	/** 录音剩余时间Toast提示 */
	private Toast mRecordTipsToast;
	private ECHandlerHelper mHandlerHelper = new ECHandlerHelper();
	private Handler mHandler = new Handler(Looper.getMainLooper());
	private Handler mVoiceHandler;
	private Looper mChattingLooper;
	/** IM聊天管理工具 */
	private ECChatManager mChatManager;
	/** 聊天底部导航控件通知回调 */
	private OnChattingFooterImpl mChattingFooterImpl = new OnChattingFooterImpl(
			(ChattingActivity) getActivity());
	/** 聊天功能插件接口实现 */
	private OnOnChattingPanelImpl mChattingPanelImpl = new OnOnChattingPanelImpl();
	private ECPullDownView mECPullDownView;
	/** 是否查看消息模式 */
	private boolean isViewMode = false;
	private View mMsgLayoutMask;
	public boolean mAtsomeone = false;
	/** 在线客服 */
	private boolean mCustomerService = false;
	private OnChattingAttachListener mAttachListener;

    @Override
    protected int getLayoutId() {
        return R.layout.chatting_activity;
    }
    
    public ECMessage getTopMsg(){
    	
    	return mChattingAdapter.getItem(0);
    }

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mAttachListener = (OnChattingAttachListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnChattingAttachListener");
		}
	}

	private ChattingActivity getChattingActivity() {
		if (getActivity() instanceof ChattingActivity) {
			return (ChattingActivity) getActivity();
		}
		throw new RuntimeException(getActivity().toString()
				+ " must implement OnChattingAttachListener");
	}
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 初始化联系人信息
		initActivityState(savedInstanceState);
	}

	@Override
	public final View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		View contentView = super.onCreateView(inflater, container,
				savedInstanceState);
		ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
		if (layoutParams == null) {
			layoutParams = new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
		}
		contentView.setLayoutParams(layoutParams);
		return contentView;
	}
	
	
	

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		LogUtil.d(TAG, "onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		// 初始化界面资源
		initView();

		queryUIMessage();

		// 初始化IM聊天工具API
		mChatManager = SDKCoreHelper.getECChatManager();
		HandlerThread thread = new HandlerThread("ChattingVoiceRecord",
				android.os.Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mChattingLooper = thread.getLooper();
		mVoiceHandler = new Handler(mChattingLooper);
		mVoiceHandler.post(new Runnable() {

            @Override
            public void run() {
                doEmojiPanel();
            }
        });

		registerReceiver(new String[] { IMessageSqlManager.ACTION_GROUP_DEL,IMChattingHelper.INTENT_ACTION_CHAT_USER_STATE,IMChattingHelper.INTENT_ACTION_CHAT_EDITTEXT_FOUCU });
	}

	private void queryUIMessage() {
		mListView.post(new Runnable() {

			@Override
			public void run() {
				if (mChattingAdapter.getCount() < 18) {
					mECPullDownView.setIsCloseTopAllowRefersh(true);
					mECPullDownView.setTopViewInitialize(false);
				}
				mListView.clearFocus();
				mChattingAdapter.notifyChange();
				mListView.setSelection(mChattingAdapter.getCount());
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		AppPanelControl.setShowVoipCall(true);
		if (mCustomerService) {
			CustomerServiceHelper.addCustomerServiceListener(null);
			CustomerServiceHelper.finishService(mRecipients);
		}
		if (mChattingLooper != null) {
			mChattingLooper.quit();
			mChattingLooper = null;
		}
		if (mChattingFooter != null) {
			mChattingFooter.onDestory();
			mChattingFooter = null;
		}

		if (mHandlerHelper != null) {
			mHandlerHelper.getTheadHandler().removeCallbacksAndMessages(null);
			mHandlerHelper = null;
		}
		if (mVoiceHandler != null) {
			mVoiceHandler.removeCallbacksAndMessages(null);
			mVoiceHandler = null;
		}
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
			mHandler = null;
		}
		if (mListView != null) {
			mListView.setOnItemLongClickListener(null);
			mListView.setOnItemClickListener(null);
		}
		if (mChattingAdapter != null) {
			mChattingAdapter.onDestroy();
			mListView.setAdapter(null);
		}
		mChatManager = null;
		mOnItemLongClickListener = null;
		mOnListViewBottomListener = null;
		mOnListViewTopListener = null;
		mOnRefreshAdapterDataListener = null;
		if (mChattingFooterImpl != null) {
			mChattingFooterImpl.release();
			mChattingFooterImpl = null;
		}
		mChattingPanelImpl = null;
		mECPullDownView = null;
		setChattingContactId("");
		IMChattingHelper.setOnMessageReportCallback(null);
		System.gc();
		
		
	}

	/**
	 * 初始化聊天界面资源
	 */
	private void initView() {
		mListView = (ListView) findViewById(R.id.chatting_history_lv);
		mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		mListView.setItemsCanFocus(false);
		mListView.setOnScrollListener(this);
		mListView.setKeepScreenOn(false);
		mListView.setStackFromBottom(false);
		mListView.setFocusable(false);
		mListView.setFocusableInTouchMode(false);
		mListView.setOnItemLongClickListener(mOnItemLongClickListener);
		registerForContextMenu(mListView);

		mListViewHeadView = getChattingActivity().getLayoutInflater().inflate(
				R.layout.chatting_list_header, null);
		mListView.addHeaderView(mListViewHeadView);
		mListView.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				hideSoftKeyboard();
				if (mChattingFooter != null) {
					// After the input method you can use the record button.
					// mGroudChatRecdBtn.setEnabled(true);
					// mChatFooter.setMode(1);
					mChattingFooter.hideBottomPanel();
				}
				return false;
			}
		});

		mMsgLayoutMask = findViewById(R.id.message_layout_mask);
		mMsgLayoutMask.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				hideMsgLayoutMask();
				mListView.setSelection(mListView.getCount() - 1);
				return true;
			}
		});
		/************************************************************************************************************/
		mECPullDownView = (ECPullDownView) findViewById(R.id.chatting_pull_down_view);
		mECPullDownView.setTopViewInitialize(true);
		mECPullDownView.setIsCloseTopAllowRefersh(false);
		mECPullDownView.setHasbottomViewWithoutscroll(false);
		mECPullDownView
				.setOnRefreshAdapterDataListener(mOnRefreshAdapterDataListener);
		mECPullDownView.setOnListViewTopListener(mOnListViewTopListener);
		mECPullDownView.setOnListViewBottomListener(mOnListViewBottomListener);

        // 初始化聊天功能面板
        mChattingFooter = (CCPChattingFooter2) findViewById(R.id.nav_footer);
        // 注册聊天面板状态回调通知、包含录音按钮按钮下放开等录音操作
        mChattingFooter.setOnChattingFooterLinstener(mChattingFooterImpl);
        // 注册聊天面板附加功能（图片、拍照、文件）被点击回调通知
        mChattingFooter.setOnChattingPanelClickListener(mChattingPanelImpl);
        // 注册一个聊天面板文本输入框改变监听
        mChattingFooter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LogUtil.d(TAG , "[onTextChanged]");
                String text = String.valueOf(s);
                String value = text.substring(start, start + count);
                if(("@".equals(value) && isPeerChat()) && !text.equals(mChattingFooter.getLastContent()) && !mChattingFooter.isSetAtSomeoneing()) {
                    mChattingFooter.setLastContent(text);
                    mChattingFooter.setInsertPos(start + 1);
                    boolean handler = (text == null || start < 0 || text.length() < start);
                    if(!handler) {
                        Intent action = new Intent();
                        action.setClass(getChattingActivity(), AtSomeoneUI.class);
                        action.putExtra(AtSomeoneUI.EXTRA_GROUP_ID, mRecipients);
                        action.putExtra(AtSomeoneUI.EXTRA_CHAT_USER, CCPAppManager.getClientUser().getUserId());
                        startActivityForResult(action, 212);
                    }
                    return ;
                } else if (!text.equals(mChattingFooter.getLastContent())) {
                    mChattingFooter.setLastContent(text);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mChattingAdapter = new ChattingListAdapter2(getActivity() , ECMessage.createECMessage(ECMessage.Type.NONE) , mRecipients , mThread);
        mListView.setAdapter(mChattingAdapter);
    }

    private void hideBottom() {
        // 隐藏键盘
        hideSoftKeyboard();
        if(mChattingFooter != null) {
            // 隐藏更多的聊天功能面板
            mChattingFooter.hideBottomPanel();
        }
    }

    private Animation mAnimation;
    private void showMsgLayoutMask() {
        if(isViewMode && !mMsgLayoutMask.isShown() ) {
            if(mAnimation == null) {
                mAnimation = AnimationUtils.loadAnimation(getChattingActivity(), R.anim.buttomtip_in);
            }
            mMsgLayoutMask.setVisibility(View.VISIBLE);
            mMsgLayoutMask.startAnimation(mAnimation);
            mAnimation.start();
        }
    }

    private void hideMsgLayoutMask() {
        if(mMsgLayoutMask != null && mMsgLayoutMask.isShown()) {
            mMsgLayoutMask.setVisibility(View.GONE);
        }
    }

    /**
     * 读取聊天界面联系人会话参数信息
     * @param savedInstanceState
     */
    private void initActivityState(Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        mRecipients = intent.getStringExtra(RECIPIENTS);
        mUsername = intent.getStringExtra(CONTACT_USER);

        if(TextUtils.isEmpty(mRecipients)) {
            ToastUtil.showMessage("联系人账号不存在");
            finish();
            return ;
        }
        mCustomerService = ContactLogic.isCustomService(mRecipients);

        if(mUsername == null) {
            ECContacts contact = ContactSqlManager.getContact(mRecipients);
            if(contact != null) {
                mUsername = contact.getNickname();
            } else {
                mUsername = mRecipients;
            }
        }
        if(!isPeerChat()) {
            IMessageSqlManager.checkContact(mRecipients, mUsername);
        }
        getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, isPeerChat() ? R.drawable.actionbar_facefriend_icon : R.drawable.actionbar_particular_icon, mUsername, this);
        setActionBarTitle(mUsername);
        mThread = ConversationSqlManager.querySessionIdForBySessionId(mRecipients);
        //mPageCount =  IMessageSqlManager.qureyIMCountForSession(mThread);
        aysnUserState();
    }

    public void aysnUserState() {
		if(isPeerChat()) {
			return ;
		}
		ECDevice.getUserState(mRecipients, new ECDevice.OnGetUserStateListener() {
			@Override
			public void onGetUserState(ECError ecError, ECUserState userState) {
				if (ecError.errorCode == SdkErrorCode.REQUEST_SUCCESS && userState != null) {
					String subTile = "对方不在线";
					if(userState.isOnline()) {
						subTile = DemoUtils.getDeviceWithType(userState.getDeviceType())
								+ "-" + DemoUtils.getNetWorkWithType(userState.getNetworkType());
					}

					if(getTopBarView()!=null){
					getTopBarView().setTopBarToStatus(1,
							R.drawable.topbar_back_bt,
							isPeerChat() ? R.drawable.actionbar_facefriend_icon : R.drawable.actionbar_particular_icon,
							null, null,
							mUsername,
							subTile,
							ChattingFragment.this);
					}
					return;
				}
				LogUtil.e(TAG, "getUserState fail");
			}
		});
	}

    /**
     * 是否群组
     * @return
     */
    public boolean isPeerChat() {
        return mRecipients != null && mRecipients.toLowerCase().startsWith("g");
    }

    /**
     * 返回聊天消息适配器
     * @return the mChattingAdapter
     */
    public ChattingListAdapter2 getChattingAdapter() {
        return mChattingAdapter;
    }

    public CCPChattingFooter2 getChattingFooter() {
        return mChattingFooter;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
            View topView = mListView.getChildAt(mListView.getFirstVisiblePosition());
            if ((topView != null) && (topView.getTop() == 0)){
                LogUtil.d(LogUtil.getLogUtilsTag(ChattingActivity.class), "doLoadingView auto pull");
                mECPullDownView.startTopScroll();
            }
        }
    }


    private boolean mHandlerDelChar = false;
	private String fileName;
	private String filePath;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            LogUtil.d(TAG, "keycode back , chatfooter mode: " +  mChattingFooter.getMode());
            if(!mChattingFooter.isButtomPanelNotVisibility()) {
                hideBottom();
                return true;
            }
            setIsFinish(true);
        } else if (keyCode == KeyEvent.KEYCODE_DEL) {
            if(event.getAction() == KeyEvent.ACTION_DOWN) {
                mHandlerDelChar = !(mChattingFooter.getCharAtCursor() != (char)(8197));
            }
            if(event.getAction() == KeyEvent.ACTION_UP && mHandlerDelChar) {
                mHandlerDelChar = false;
                CCPChattingFooter2 footer = this.mChattingFooter;
                int selectionStart = footer.getSelectionStart();
                String text = footer.getLastText().substring(0, selectionStart);
                int atIndex = text.lastIndexOf('@');
                if(atIndex < text.length() && atIndex >= 0) {
					delAtSomeBody(text.substring(atIndex, selectionStart));
					String subStartText = text.substring(0, atIndex);
					String subSecondText = footer.getLastText().substring(selectionStart);
					StringBuilder sb = new StringBuilder();
					sb.append(subStartText).append(subSecondText);
					footer.setLastText(sb.toString());
					footer.mEditText.setSelection(atIndex);
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

	private void delAtSomeBody(String text) {
		if(TextUtils.isEmpty(text)) {
			return ;
		}

		this.mChattingFooter.delSomeBody(text.replace("@","").replace((char)(8197),' ').trim());
	}

    @Override  //控制是否能刷新数据top
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

        LogUtil.d(TAG, "[onScroll] firstVisibleItem :" + firstVisibleItem + " ,visibleItemCount:" + visibleItemCount + " ,totalItemCount:" + totalItemCount);
        isViewMode = !((firstVisibleItem + visibleItemCount) == totalItemCount);
        if(mECPullDownView != null ){
            if(!mChattingAdapter.isLimitCount()){
                mECPullDownView.setIsCloseTopAllowRefersh(false);
            }else{
                mECPullDownView.setIsCloseTopAllowRefersh(true);//小于18
            }
        }
        if(!isViewMode) hideMsgLayoutMask();
    }


    @Override
    public void onResume() {
        super.onResume();

        mChattingFooter.switchChattingPanel(SmileyPanel.APP_PANEL_NAME_DEFAULT);
        mChattingFooter.initSmileyPanel();
        IMChattingHelper.setOnMessageReportCallback(this);
        if(mCustomerService) {
            CustomerServiceHelper.addCustomerServiceListener(this);
        }
        // 将所有的未读消息设置为已读
        setChattingSessionRead();
        mChattingAdapter.onResume();
		try {
			ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_AT,"",true);
		} catch (InvalidClassException e) {
		}
		checkPreviewImage();
        setChattingContactId(mRecipients);
        ECNotificationManager.getInstance().forceCancelNotification();

        initSettings(mRecipients);
        if(isPeerChat() && !GroupSqlManager.getJoinState(mRecipients)) {
            getTopBarView().setTopBarToStatus(1, R.drawable.topbar_back_bt, -1, mUsername, this);
            mChattingFooter.setVisibility(View.GONE);
            return ;
        }
        mChattingFooter.setVisibility(View.VISIBLE);
    }

    /**
     * @param mRecipients
     */
    private void initSettings(String mRecipients) {
        if(isPeerChat()) {
            ECGroup ecGroup = GroupSqlManager.getECGroup(mRecipients);
            if(ecGroup != null) {
                setActionBarTitle(ecGroup.getName() != null ? ecGroup.getName() : ecGroup.getGroupId());
                SpannableString charSequence = setNewMessageMute(!ecGroup.isNotice());
                if(charSequence != null) {
                    getTopBarView().setTitle(charSequence);
                }
            }
            ECGroupManager groupManager = ECDevice.getECGroupManager();
            // 调用获取群组成员接口，设置结果回调

			if(ecGroup!=null) {

				groupManager.queryGroupMembers(ecGroup.getGroupId(),
						new ECGroupManager.OnQueryGroupMembersListener() {
							@Override
							public void onQueryGroupMembersComplete(ECError error
									, final List members) {
								if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS
										&& members != null) {
									// 获取群组成员成功
									// 将群组成员信息更新到本地缓存中（sqlite） 通知UI更新

									RPGroupMemberUtil.getInstance().setGroupMemberListener(new NotifyGroupMemberCallback() {
										@Override
										public void getGroupMember(final String groupID, final GroupMemberCallback mCallBack) {

											List<RPUserBean> userBeanList = new ArrayList<RPUserBean>();

											for (int i = 0; i < members.size(); i++) {
												RPUserBean userBean = new RPUserBean();
												ECGroupMember member = (ECGroupMember) members.get(i);
												userBean.userId = member.getVoipAccount();
												if (userBean.userId.equals(CCPAppManager.getUserId())) {
													continue;
												}

												if (member != null) {
													userBean.userAvatar = "none";
													userBean.userNickname = TextUtils.isEmpty(member.getDisplayName()) ? member.getVoipAccount() : member.getDisplayName();
												} else {
													userBean.userNickname = userBean.userId;
													userBean.userAvatar = "none";
												}
												userBeanList.add(userBean);
											}
											mCallBack.setGroupMember(userBeanList);
										}
									});


									return;
								}
								// 群组成员获取失败
								Log.e("henry_dianying", "sync group detail fail " +
										", errorCode=" + error.errorCode);

							}

						}
				);
			}
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handleSendUserStateMessage("0");
		stopPlayVoice();
        setChattingContactId("");

    }

    /**
     * 保存当前的聊天界面所对应的联系人、方便来消息屏蔽通知
     */
    private void setChattingContactId(String contactid) {
        try {
            ECPreferences.savePreference(ECPreferenceSettings.SETTING_CHATTING_CONTACTID, contactid, true);
        } catch (InvalidClassException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查是否有预览带发送图片
     */
    private void checkPreviewImage() {
        if(TextUtils.isEmpty(mFilePath)) {
            return ;
        }
        boolean previewImage = ECPreferences.getSharedPreferences().getBoolean(ECPreferenceSettings.SETTINGS_PREVIEW_SELECTED.getId()
                ,(Boolean)ECPreferenceSettings.SETTINGS_PREVIEW_SELECTED.getDefaultValue());
        if(previewImage){
            try {
                ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_PREVIEW_SELECTED, Boolean.FALSE, true);
                new ChattingAsyncTask(getChattingActivity()).execute(mFilePath);
                mFilePath = null;
            } catch (InvalidClassException e) {
                e.printStackTrace();
            }
        }
    }

    public long getmThread() {
        return mThread;
    }

    private void doEmojiPanel() {
        if(EmoticonUtil.getEmojiSize() == 0) {
            EmoticonUtil.initEmoji();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d(TAG ,"onActivityResult: requestCode=" + requestCode
                + ", resultCode=" + resultCode + ", data=" + data);

        // If there's no data (because the user didn't select a picture and
        // just hit BACK, for example), there's nothing to do.
        
        if (requestCode == 0x2a || requestCode == SELECT_AT_SOMONE) {
            if (data == null) {
                return;
            }
        } else if (resultCode != ChattingActivity.RESULT_OK) {
            LogUtil.d("onActivityResult: bail due to resultCode=" + resultCode);
            isFireMsg=false;
            return;
        }

        if(data != null && 0x2a == requestCode) {
            handleAttachUrl(data.getStringExtra("choosed_file_path"));
            return ;
        }

        if(requestCode == REQUEST_CODE_TAKE_PICTURE
                || requestCode == REQUEST_CODE_LOAD_IMAGE) {
            if(requestCode == REQUEST_CODE_LOAD_IMAGE) {
				ArrayList<String> result = data.getStringArrayListExtra(PhotoPickerActivity.KEY_RESULT);
				if(result != null && !result.isEmpty()) {
					mFilePath = result.get(0);
				} else {
					mFilePath = DemoUtils.resolvePhotoFromIntent(
							this.getActivity(), data,
							FileAccessor.IMESSAGE_IMAGE);
				}
			}
            if(TextUtils.isEmpty(mFilePath)) {
                return ;
            }
            File file = new File(mFilePath);
            if(file == null || !file.exists()) {
            	
                return;
            }
            try {
                ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_CROPIMAGE_OUTPUTPATH, file.getAbsolutePath(), true);
                Intent intent = new Intent(getChattingActivity(), ImagePreviewActivity.class);
                startActivityForResult(intent, REQUEST_CODE_IMAGE_CROP);
            } catch (InvalidClassException e1) {
                e1.printStackTrace();
            }
            return ;
        }
        if(requestCode == REQUEST_VIEW_CARD && data != null) {
            boolean exit = data.getBooleanExtra(GroupInfoActivity.EXTRA_QUEIT , false);
            if(exit) {
                finish();
                return ;
            }
            boolean reload = data.getBooleanExtra(GroupInfoActivity.EXTRA_RELOAD , false);
            if(reload) {
                mThread = mChattingAdapter.setUsername(mRecipients);
                queryUIMessage();
            }
        }

		if (requestCode == SELECT_AT_SOMONE) {
			String selectUser = data
					.getStringExtra(AtSomeoneUI.EXTRA_SELECT_CONV_USER);
			if (TextUtils.isEmpty(selectUser)) {
				mChattingFooter.setAtSomebody("");
				LogUtil.d(TAG, "@ [nobody]");
				return;
			}
			LogUtil.d(TAG, "@ " + selectUser);
			ECContacts contact = ContactSqlManager.getContact(selectUser);
			if (contact == null) {
				return;
			}
			if (TextUtils.isEmpty(contact.getNickname())) {
				contact.setNickname(contact.getContactid());
			}
			mChattingFooter.setAtSomebody(contact.getNickname());
			mChattingFooter.putSomebody(contact);
			postSetAtSome();
			return;
		}
		if (requestCode == GlobalConstant.ACTIVITY_FOR_RESULT_VIDEORECORD) {
			handleVideoRecordSend(data);
		}
		if(requestCode==REQUEST_CODE_TAKE_LOCATION){
			
			locationInfo=(LocationInfo) data.getSerializableExtra("location");
			
			handleSendLocationMessage(locationInfo);
		}
		if (requestCode == REQUEST_CODE_REDPACKET) {
			if (data != null) {
				handlesendRedPacketMessage(data);
			}
		}

	}
    private LocationInfo locationInfo;

    
	private void handleVideoRecordSend(Intent data) {
		if (data.hasExtra("file_name")) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				fileName = extras.getString("file_name");
			}
		}

		if (data.hasExtra("file_url")) {
			Bundle extras = data.getExtras();
			if (extras != null) {
				filePath = extras.getString("file_url");
			}
		}
		File f = new File(filePath);
		if (!f.exists()) {
			return;
		}
		handleSendVideoAttachMessage(f.length(), filePath);

	}

	/**
	 * 处理@某人
	 */
	private void postSetAtSome() {
		String atSomebody = mChattingFooter.getAtSomebody();
		if (!TextUtils.isEmpty(atSomebody)) {
			String text = mChattingFooter.getLastText();
			int someInsertPosition = mChattingFooter.getInsertPos();
			if (someInsertPosition > text.length()) {
				someInsertPosition = text.length();
			}
			String message = text.substring(0, someInsertPosition) + atSomebody
					+ (char) (8197)
					+ text.substring(someInsertPosition, text.length());
			int selectoin = 1 + someInsertPosition + atSomebody.length();
			mChattingFooter.setLastContent(message);
			mChattingFooter.setLastText(message, selectoin, false);
			mChattingFooter.setLastContent(null);
			toggleSoftInput();
		}

    }

    /**
     * 处理附件
     * @param path
     */
    private void handleAttachUrl(final String path) {
        File file = new File(path);
		if(!file.exists()) {
            return ;
        }
        final long length = file.length();
//        if(length > (10 * 1048576.0F)) {
//            ToastUtil.showMessage("文件大小超过限制，最大不能超过10M");
//            return;
//        }
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(getActivity(), getString(R.string.plugin_upload_attach_size_tip , FileUtils.formatFileLength(length)), new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                handleSendFileAttachMessage(length , path);
            }});

        buildAlert.setTitle(R.string.app_tip);
        buildAlert.show();
    }

    /**
     * 处理文本发送方法事件通知
     * @param text
     */
//    private void handleSendTextMessage(CharSequence text) {
//        if(text == null) {
//            return ;
//        }
//        if(text.toString().trim().length() <= 0) {
//            canotSendEmptyMessage();
//            return ;
//        }
//        // 组建一个待发送的ECMessage
//        ECMessage msg = ECMessage.createECMessage(ECMessage.Type.CMD);
//
//        // 设置消息接收者
//        msg.setTo(mRecipients);
//		ECCmdMessageBody msgBody=null;
//		Boolean isBQMMMessage=false;
//		String emojiNames = null;
//		if(text.toString().contains(CCPChattingFooter2.TXT_MSGTYPE)  && text.toString().contains(CCPChattingFooter2.MSG_DATA)){
//			try {
//				JSONObject jsonObject = new JSONObject(text.toString());
//				String emojiType=jsonObject.getString(CCPChattingFooter2.TXT_MSGTYPE);
//				if(emojiType.equals(CCPChattingFooter2.EMOJITYPE) || emojiType.equals(CCPChattingFooter2.FACETYPE)){//说明是含有BQMM的表情
//					isBQMMMessage=true;
//					emojiNames=jsonObject.getString(CCPChattingFooter2.EMOJI_TEXT);
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//		if (isBQMMMessage) {
//			msgBody = new ECCmdMessageBody(emojiNames);
//			msg.setBody(msgBody);
//			msg.setUserData(text.toString());
//		} else {
//			// 创建一个文本消息体，并添加到消息对象中
//			msgBody = new ECCmdMessageBody(text.toString());
//
//			msgBody.setIsOfflinePush(true);
//			msgBody.setIsHint(true);
//			msgBody.setIsSyncMsg(true);
//			msgBody.setIsSave(true);
//			msgBody.setMessage("aa");
//			msg.setBody(msgBody);
//		}
//
//		String[] at = mChattingFooter.getAtSomeBody();
//		msgBody.setAtMembers(at);
//		mChattingFooter.clearSomeBody();
//        try {
//            // 发送消息，该函数见上
//            long rowId = -1;
//            if(mCustomerService) {
//                rowId = CustomerServiceHelper.sendMCMessage(msg);
//            } else {
//                rowId = IMChattingHelper.sendECMessage(msg);
//            }
//            // 通知列表刷新
//            msg.setId(rowId);
//            notifyIMessageListView(msg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    private void handleSendTextMessage(CharSequence text) {
        if(text == null) {
            return ;
        }
        if(text.toString().trim().length() <= 0) {
            canotSendEmptyMessage();
            return ;
        }
        // 组建一个待发送的ECMessage
        ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);

        // 设置消息接收者
        msg.setTo(mRecipients);
		ECTextMessageBody msgBody=null;
		Boolean isBQMMMessage=false;
		String emojiNames = null;
		if(text.toString().contains(CCPChattingFooter2.TXT_MSGTYPE)  && text.toString().contains(CCPChattingFooter2.MSG_DATA)){
			try {
				JSONObject jsonObject = new JSONObject(text.toString());
				String emojiType=jsonObject.getString(CCPChattingFooter2.TXT_MSGTYPE);
				if(emojiType.equals(CCPChattingFooter2.EMOJITYPE) || emojiType.equals(CCPChattingFooter2.FACETYPE)){//说明是含有BQMM的表情
					isBQMMMessage=true;
					emojiNames=jsonObject.getString(CCPChattingFooter2.EMOJI_TEXT);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		if (isBQMMMessage) {
			msgBody = new ECTextMessageBody(emojiNames);
			msg.setBody(msgBody);
			msg.setUserData(text.toString());
		} else {
			// 创建一个文本消息体，并添加到消息对象中
			msgBody = new ECTextMessageBody(text.toString());
			msg.setBody(msgBody);
		}

		String[] at = mChattingFooter.getAtSomeBody();
		msgBody.setAtMembers(at);
		mChattingFooter.clearSomeBody();
        try {
            // 发送消息，该函数见上
            long rowId = -1;
            if(mCustomerService) {
                rowId = CustomerServiceHelper.sendMCMessage(msg);
            } else {
                rowId = IMChattingHelper.sendECMessage(msg);
            }
            // 通知列表刷新
            msg.setId(rowId);
            notifyIMessageListView(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 处理状态消息发送
     * @param text
     */
    private void handleSendUserStateMessage(CharSequence text) {
        if(text == null) {
            return ;
        }
        if(text.toString().trim().length() <= 0) {
            return ;
        }

		if(CCPAppManager.getUserId().equals(mRecipients)){
			return;
		}

        // 组建一个待发送的ECMessage
        ECMessage msg = ECMessage.createECMessage(ECMessage.Type.STATE);
        // 设置消息接收者
        msg.setTo(mRecipients);
        // 创建一个文本消息体，并添加到消息对象中
        ECUserStateMessageBody msgBody = new ECUserStateMessageBody(text.toString());
        msg.setBody(msgBody);
		ECChatManager ecChatManager = ECDevice.getECChatManager();
		if(ecChatManager==null){
			return;
		}
		ecChatManager.sendMessage(msg, new ECChatManager.OnSendMessageListener() {
			@Override
			public void onSendMessageComplete(ECError error, ECMessage message) {

			}

			@Override
			public void onProgress(String msgId, int totalByte, int progressByte) {

			}
		});


    }

	private void handleSendRichTextMessage(String title,String desc,String url) {
		// 组建一个待发送的ECMessage
		ECMessage msg = ECMessage.createECMessage(ECMessage.Type.RICH_TEXT);
		// 设置消息接收者
		msg.setTo(mRecipients);
		// 创建一个文本消息体，并添加到消息对象中
		ECPreviewMessageBody msgBody = new ECPreviewMessageBody();
		msgBody.setTitle(title);
		msgBody.setDescContent(desc);
		msgBody.setLocalUrl(FileAccessor.IMESSAGE_RICH_TEXT + "/" + DemoUtils.md5(url));
		msgBody.setRemoteUrl(url);


		msg.setBody(msgBody);
		try {
			// 发送消息，该函数见上
			long rowId = -1;
			if(mCustomerService) {
				rowId = CustomerServiceHelper.sendMCMessage(msg);
			} else {
				rowId = IMChattingHelper.sendECMessage(msg);
			}
			// 通知列表刷新
			msg.setId(rowId);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


    private void handleSendLocationMessage(LocationInfo locationInfo) {
        if(locationInfo == null) {
            return ;
        }
        String address =locationInfo.getAddress();
        if(TextUtils.isEmpty(address)){
        	return;
        }
        
        
        // 组建一个待发送的ECMessage
        ECMessage msg = ECMessage.createECMessage(ECMessage.Type.LOCATION);
        // 设置消息接收者
        msg.setTo(mRecipients);
        // 创建一个文本消息体，并添加到消息对象中
        ECLocationMessageBody msgBody = new ECLocationMessageBody(locationInfo.getLat(), locationInfo.getLon());
        msgBody.setTitle(locationInfo.getAddress());
        msg.setBody(msgBody);
        try {
            // 发送消息，该函数见上
            long rowId = -1;
            if(mCustomerService) {
                rowId = CustomerServiceHelper.sendMCMessage(msg);
            } else {
                rowId = IMChattingHelper.sendECMessage(msg);
            }
            // 通知列表刷新
            msg.setId(rowId);
            notifyIMessageListView(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 不允许发送空白消息
     */
    private void canotSendEmptyMessage() {

        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(getActivity(), R.string.chatting_empty_message_cant_be_sent ,R.string.dialog_btn_confim, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mChattingFooter.setEditTextNull();
            }
        });
        buildAlert.setTitle(R.string.app_tip);
        buildAlert.setCanceledOnTouchOutside(false);
        buildAlert.show();
    }

    /**
     * 处理发送附件消息
     * @param length
     * @param pathName
     */
    private void handleSendFileAttachMessage(long length, String pathName) {
        if(TextUtils.isEmpty(pathName)) {
            return ;
        }
        // 组建一个待发送的附件ECMessage
        ECMessage msg = ECMessage.createECMessage(ECMessage.Type.FILE);
        // 设置接收者
        msg.setTo(mRecipients);
        // 创建附件消息体
        ECFileMessageBody msgBody  = new ECFileMessageBody();
        // 设置附件名
        msgBody.setFileName(DemoUtils.getFilename(pathName));
        // 设置附件扩展名
        msgBody.setFileExt(DemoUtils.getExtensionName(pathName));
//		msgBody.setIsCompress(false);//selftest
        // 设置附件本地路径
        msgBody.setLocalUrl(pathName);
        // 设置附件长度
        msgBody.setLength(length);
        msg.setBody(msgBody);
        
        try {
            // 调用发送API
            // 发送消息，该函数见上
            long rowId = -1;
            if(mCustomerService) {
                rowId = CustomerServiceHelper.sendMCMessage(msg);
            } else {
                rowId = IMChattingHelper.sendECMessage(msg);
            }
            // 通知列表刷新
            msg.setId(rowId);
            notifyIMessageListView(msg);
        } catch (Exception e) {
        }
    }

	private void handleSendVideoAttachMessage(long length, String pathName) {
		if (TextUtils.isEmpty(pathName)) {
			return;
		}
		// 组建一个待发送的附件ECMessage
		ECMessage msg = ECMessage.createECMessage(ECMessage.Type.VIDEO);
		// 设置接收者
		msg.setTo(mRecipients);
		// 创建附件消息体
		ECVideoMessageBody msgBody = new ECVideoMessageBody();
		// 设置附件名
		msgBody.setFileName(DemoUtils.getFilename(pathName));
		// 设置附件扩展名
		msgBody.setFileExt(DemoUtils.getExtensionName(pathName));
		// 设置附件本地路径
		msgBody.setLocalUrl(pathName);
		// 设置附件长度
		msgBody.setLength(length);
		// 扩展附件名称、对方可以用此名称界面显示
		msg.setBody(msgBody);
		try {
			// 调用发送API
			// 发送消息，该函数见上
			long rowId = -1;
			if (mCustomerService) {
				rowId = CustomerServiceHelper.sendMCMessage(msg);
			} else {
				rowId = IMChattingHelper.sendECMessage(msg);
			}
			// 通知列表刷新
			msg.setId(rowId);
			notifyIMessageListView(msg);
		} catch (Exception e) {
		}
	}

    /**
     * 处理发送图片消息
     * @param imgInfo
     */
    public void handleSendImageMessage(ImgInfo imgInfo) {
        String fileName = imgInfo.getBigImgPath();
        String fileUrl = FileAccessor.getImagePathName() + "/" + fileName;
        if(new File(fileUrl).exists()) {
            // 组建一个待发送的ECMessage
            ECMessage msg = ECMessage.createECMessage(ECMessage.Type.IMAGE);
            // 设置接收者
            msg.setTo(mRecipients);
            // 设置附件包体（图片也是相当于附件）
            ECImageMessageBody msgBody  = new ECImageMessageBody();

			// 设置附件名
			msgBody.setFileName(fileName);
			// 设置附件扩展名
			msgBody.setFileExt(DemoUtils.getExtensionName(fileName));
			// 设置附件本地路径
			msgBody.setLocalUrl(fileUrl);
			msg.setBody(msgBody);
            try {
                long rowId;
                if(mCustomerService) {
                    rowId = CustomerServiceHelper.sendImageMessage(imgInfo ,msg);
                } else{
                    rowId = IMChattingHelper.sendImageMessage(imgInfo ,msg);
                }
                // 通知列表刷新
                msg.setId(rowId);
                notifyIMessageListView(msg);
            } catch (Exception e) {
            }finally{
//            	isFireMsg=false;//重置
            }
        }
    }

    /**
     * 将发送的消息放入消息列表
     * @param message
     */
    public  void notifyIMessageListView(ECMessage message) {
        if(!checkUserThread()) {
            return ;
        }
        mListView.setSelection(mListView.getCount() - 1);
    }

    @Override
    protected void handleReceiver(Context context, Intent intent) {
        super.handleReceiver(context, intent);
        if(IMessageSqlManager.ACTION_GROUP_DEL.equals(intent.getAction()) && intent.hasExtra("group_id")) {
            String id = intent.getStringExtra("group_id");
            if(id != null && id.equals(mRecipients)) {
                setIsFinish(true);
                finish();
            }
        }else if(IMChattingHelper.INTENT_ACTION_CHAT_USER_STATE.equals(intent.getAction())){

			String state= intent.getStringExtra(IMChattingHelper.USER_STATE);
			if(!TextUtils.isEmpty(state)&&Integer.parseInt(state)== ImUserState.WRITE.ordinal()){
				setActionBarTitle("正在输入...");
			}else if(!TextUtils.isEmpty(state)&&Integer.parseInt(state)==ImUserState.RECORDE.ordinal()){
				setActionBarTitle("正在录音...");
			}else {
				setActionBarTitle(mUsername);
			}

		}else if(IMChattingHelper.INTENT_ACTION_CHAT_EDITTEXT_FOUCU.equals(intent.getAction())){

			 boolean hasFoucs =	intent.getBooleanExtra("hasFoucs",false);
			 if(hasFoucs){
				 handleSendUserStateMessage("1");
			 }else {
				 handleSendUserStateMessage("0");
			 }


		}
    }

	private void setTitleDelay(){

		ECHandlerHelper.postDelayedRunnOnUI(new Runnable() {
			@Override
			public void run() {
				setActionBarTitle(mUsername);

			}
		},2000);
	}

    /**
     * 获得最后一条消息的时间
     * @return
     */
    private long getMessageAdapterLastMessageTime() {
        long lastTime = 0;
        if(mChattingAdapter != null && mChattingAdapter.getCount() >0) {
            ECMessage item = mChattingAdapter.getItem(mChattingAdapter.getCount() - 1);
            if(item != null) {
                lastTime = item.getMsgTime();
            }
        }
        return lastTime;
    }

    /**
     * <error code="SdkErrorCode.NON_GROUPMEMBER">文件上传发送者不在群组内</error>
     * <error code="SdkErrorCode.SPEAK_LIMIT_FILE">文件上传接受者被禁言</error>
     * 消息发送报告
     */
    @Override
    public void onMessageReport(ECError error ,ECMessage message) {
        if(mChattingAdapter != null) {
            mChattingAdapter.notifyChange();
        }
        if(error == null) {
            return ;
        }
        if((SdkErrorCode.SPEAK_LIMIT_FILE == error.errorCode || SdkErrorCode.SPEAK_LIMIT_TEXT == error.errorCode)) {
            // 成员被禁言
            showAlertTips(R.string.sendmsg_error_15032);
            return ;
        }
        if(( SdkErrorCode.NON_GROUPMEMBER == error.errorCode)) {
            // 文件上传发送者不在群组内
            showAlertTips(R.string.sendmsg_error_16072);
            return ;
        }

        if(SdkErrorCode.SDK_TEXT_LENGTH_LIMIT == error.errorCode) {
            // 文本长度超过限制
            showAlertTips(R.string.sendmsg_error_170001);
        }

    }

    private void showAlertTips(int message) {
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(getActivity(), message, R.string.dialog_btn_confim ,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        buildAlert.setTitle(R.string.app_tip);
        buildAlert.setCanceledOnTouchOutside(false);
        buildAlert.show();
    }

    public boolean checkUserThread() {
        ChattingListAdapter2 forceAdapter = mChattingAdapter;
        if(forceAdapter == null) {
            return false;
        }
        if(mThread <= 0 || mThread != forceAdapter.getThread()) {
            mThread = forceAdapter.setUsername(mRecipients);
        }
        forceAdapter.notifyChange();
        return true;
    }

    /**
     * 收到新的Push消息
     */
    @Override
    public void onPushMessage(String sid ,List<ECMessage> msgs) {

        if(!mRecipients.equals(sid)) {
            return ;
        }

        if(!checkUserThread()) {
            return ;
        }
        showMsgLayoutMask();
        // 当前是否正在查看消息
        if(!isViewMode)mListView.setSelection(mListView.getCount() - 1);

        setChattingSessionRead();
    }

    /**
     * 更新所有的未读消息
     */
    private void setChattingSessionRead() {
        ConversationSqlManager.setChattingSessionRead(mThread);
    }

    /**
     * 给予客户端震动提示
     */
    protected void readyOperation() {
        computationTime = -1L;
        mRecordTipsToast = null;
        playTone(ToneGenerator.TONE_PROP_BEEP, TONE_LENGTH_MS);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                stopTone();
            }
        }, TONE_LENGTH_MS);
        vibrate(50L);
    }

    private Object mToneGeneratorLock = new Object();
    // 初始化
    private void initToneGenerator() {
        AudioManager mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        if (mToneGenerator == null) {
            try {
                int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                int streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                int volume = (int) (TONE_RELATIVE_VOLUME * (streamVolume / streamMaxVolume));
                mToneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, volume);

            } catch (RuntimeException e) {
                LogUtil.d("Exception caught while creating local tone generator: "
                        + e);
                mToneGenerator = null;
            }
        }
    }

    /**
     * 停止播放声音
     */
    public void stopTone() {
        if(mToneGenerator != null)
            mToneGenerator.stopTone();
    }

    /**
     * 播放提示音
     * @param tone
     * @param durationMs
     */
    public void playTone(int tone ,int durationMs) {
        synchronized(mToneGeneratorLock) {
            initToneGenerator();
            if (mToneGenerator == null) {
                LogUtil.d("playTone: mToneGenerator == null, tone: "+tone);
                return;
            }

            // Start the new tone (will stop any playing tone)
            mToneGenerator.startTone(tone, durationMs);
        }
    }

	/**
	 * 手机震动
	 * 
	 * @param milliseconds
	 */
	public synchronized void vibrate(long milliseconds) {
		Vibrator mVibrator = (Vibrator) getActivity().getSystemService(
				Context.VIBRATOR_SERVICE);
		if (mVibrator == null) {
			return;
		}
		mVibrator.vibrate(milliseconds);
	}

	public void showTakeStyle(final Context ctx) {
		ECListDialog dialog = new ECListDialog(ctx, R.array.take_chat_arr);
		;
		dialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
			@Override
			public void onDialogItemClick(Dialog d, int position) {
				LogUtil.d("onDialogItemClick", "position " + position);

				if (position == 0) {

					handleTackPicture();

				} else if (position == 1) {
					handleVideoRecord();
				} else {
					if (position == 2) {
						File file = new File(Environment.getExternalStorageDirectory() , "DCIM/Camera/VID_20160201_103610.mp4");
						handleSendVideoAttachMessage(file.length(), file.getAbsolutePath());
					} else if (position == 3) {
						File file = new File(Environment.getExternalStorageDirectory() , "DCIM/Camera/VID_20160201_103703.mp4");
						handleSendVideoAttachMessage(file.length(), file.getAbsolutePath());
					} else if (position == 4) {
						File file = new File(Environment.getExternalStorageDirectory() , "DCIM/Camera/VID_20160201_103841.mp4");
						handleSendVideoAttachMessage(file.length(), file.getAbsolutePath());
					} else if (position == 5) {
						File file = new File(Environment.getExternalStorageDirectory() , "DCIM/Camera/VID_20160201_103857.mp4");
						handleSendVideoAttachMessage(file.length(), file.getAbsolutePath());
					}
				}

			}
		});
		dialog.setTitle(R.string.take_title);
		dialog.show();
	}

	private void handleTackPicture() {
		if (!FileAccessor.isExistExternalStore()) {
			return;
		}
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = FileAccessor.getTackPicFilePath();
		if (file != null) {
			Uri uri = Uri.fromFile(file);
			if (uri != null) {
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			}
			mFilePath = file.getAbsolutePath();
		}
		startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
	}

	/**
     *
     */
    private void scrollListViewToLast() {
        if(mListView != null){
            mListView.postDelayed(new Runnable() {

                @Override
                public void run() {
                    int lastVisiblePosition = mListView.getLastVisiblePosition();
                    int count = mListView.getCount() - 1;
                    LogUtil.v(LogUtil.TAG + "ChattingFooterEventImpl", "last visible/adapter=" + lastVisiblePosition + "/" + count);
                    /*if(lastVisiblePosition > count - 1) {
                        mListView.setSelectionFromTop(count - 1, 0);
                    } else {
                        mListView.setSelection(count);
                    }*/

                    if(mListView.getCount() <= 1) {
                        SmoothScrollToPosition.setSelection(mListView , count , true);
                        return ;
                    }
                    SmoothScrollToPosition.setSelectionFromTop(mListView ,count - 1 , 0 , true);
                }
            }, 10L);
        }
    }

    private void handleSelectImageIntent() {
		Intent intent = new Intent(this.getActivity(), PhotoPickerActivity.class);
		intent.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA, true);
		intent.putExtra(PhotoPickerActivity.EXTRA_SELECT_MODE, PhotoPickerActivity.MODE_SINGLE);
		intent.putExtra(PhotoPickerActivity.EXTRA_MAX_MUN, 9);
		startActivityForResult(intent, REQUEST_CODE_LOAD_IMAGE);
    }

    /**
     * 消息重发
     * @param msg
     * @param position
     */
    public void doResendMsgRetryTips(final ECMessage msg , final int position) {
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(getActivity(), R.string.chatting_resend_content, null, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                resendMsg(msg, position);
            }
        });
        buildAlert.setTitle(R.string.chatting_resend_title);
        buildAlert.show();
    }
    
    private void keepScreenOnState(boolean screenOn) {
    	if(mListView != null) {
			mListView.setKeepScreenOn(screenOn);
		}
    }
    
    
    
    /**
     * @param msg
     * @param position
     */
    protected void resendMsg(ECMessage msg, int position) {
        if(msg == null || position < 0 || mChattingAdapter.getItem(position) == null) {
            LogUtil.d(TAG, "ignore resend msg , msg " + msg + " , position " + position);
            return ;
        }
        ECMessage message = mChattingAdapter.getItem(position);
        message.setTo(mRecipients);
        long rowid = IMChattingHelper.reSendECMessage(message);
        if(rowid != -1) {
            mChattingAdapter.notifyDataSetChanged();
        }
    }
    /**
     * 聊天插件功能实现
     */
    private class OnOnChattingPanelImpl implements CCPChattingFooter2.OnChattingPanelClickListener {

		@Override
		public void OnTakingPictureRequest() {
			
			showTakeStyle(getActivity());
			hideBottomPanel();
		}

		@Override
		public void OnSelectImageReuqest() {
			handleSelectImageIntent();
			hideBottomPanel();
		}

		@Override
		public void OnSelectFileRequest() {
			startActivityForResult(new Intent(getActivity(),
					FileExplorerActivity.class), 0x2a);
			hideBottomPanel();
		}

		private void hideBottomPanel() {
			mChattingFooter.hideBottomPanel();
		}

		@Override
		public void OnSelectVoiceRequest() {

			handleVoiceCall();
			hideBottomPanel();

		}

		@Override
		public void OnSelectVideoRequest() {
			handleVideoCall();
			hideBottomPanel();

		}

		@Override
		public void OnSelectFireMsg() {
			showTakeFireStyle(getActivity());
			hideBottomPanel();
		}

		@Override
		public void OnSelectLocationRequest() {//位置
			
			Intent intent =new Intent(getActivity(), LocationActivity.class);
			startActivityForResult(intent, REQUEST_CODE_TAKE_LOCATION);
			hideBottomPanel();
		}

		@Override
		public void OnSelectRedPacketRequest() {

			//传递到sdk里的数据
			com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
			//传递参数到红包sdk：发送者头像url，昵称（缺失则传id）
			String fromAvatarUrl = "none";
			String fromNickName = clientUser.getUserName();
			fromNickName = TextUtils.isEmpty(fromNickName) ? clientUser.getUserId() : fromNickName;
			jsonObject.put(RedPacketConstant.KEY_FROM_AVATAR_URL, fromAvatarUrl);
			jsonObject.put(RedPacketConstant.KEY_FROM_NICK_NAME, fromNickName);
			jsonObject.put(RedPacketConstant.KEY_CURRENT_ID, CCPAppManager.getUserId());
			if (!isPeerChat()) {
				//如果是单聊传递对方id
				jsonObject.put(RedPacketConstant.KEY_USER_ID, mRecipients);
				jsonObject.put(RedPacketConstant.KEY_CHAT_TYPE, 1);
			} else {
				//如果是群聊传递群id和群人数
				ECGroup ecGroup = GroupSqlManager.getECGroup(mRecipients);
				jsonObject.put(RedPacketConstant.KEY_GROUP_ID, ecGroup.getGroupId());
				jsonObject.put(RedPacketConstant.KEY_GROUP_MEMBERS_COUNT, ecGroup.getCount());
				jsonObject.put(RedPacketConstant.KEY_CHAT_TYPE, 2);
			}
			RedPacketUtil.startRedPacketActivityForResult(ChattingFragment.this, jsonObject, REQUEST_CODE_REDPACKET);
			hideBottomPanel();

		}

	}
	ClientUser clientUser = CCPAppManager.getClientUser();
	public static final int REQUEST_CODE_REDPACKET = 99;
    public static  boolean isFireMsg=false;
    public void showTakeFireStyle(final Context ctx) {//阅后即焚
		ECListDialog dialog = new ECListDialog(ctx, R.array.take_chat_fire_msg);
		;
		dialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
			@Override
			public void onDialogItemClick(Dialog d, int position) {
				LogUtil.d("onDialogItemClick", "position " + position);
				isFireMsg=true;
				if (position == 0) {
					handleTackPicture();

				} else if (position == 1) {
					handleSelectImageIntent();
				}

			}
		});
		dialog.setTitle(R.string.take_title);
		dialog.show();
	}

	/**
	 * 聊天功能面板（发送、录音、切换输入选项）
	 */
	private class OnChattingFooterImpl implements
			CCPChattingFooter2.OnChattingFooterLinstener {

		ChattingActivity mActivity;
		protected String mAmrPathName;
		/** 保存当前的录音状态 */
		public int mRecordState = RECORD_IDLE;
		/** 语音录制空闲 */
		public static final int RECORD_IDLE = 0;
		/** 语音录制中 */
		public static final int RECORD_ING = 1;
		/** 语音录制结束 */
		public static final int RECORD_DONE = 2;
		/** 待发的ECMessage消息 */
		private ECMessage mPreMessage;
		
		MediaPlayTools instance;
		public String bianShengFilePath;
		/** 同步锁 */
		Object mLock = new Object();
		private void changeVoiceInSDK(String appendName){
			
			
			final File file =new File(FileAccessor.getVoicePathName().getAbsolutePath()+"/"+appendName+mAmrPathName);
			bianShengFilePath=file.getAbsolutePath();
			if(file!=null&&file.exists()){
				instance.playVoice(file.getAbsolutePath(), false);
			}else {
			  final Parameters parameters =getParameters(appendName);
			  SDKCoreHelper.getECChatManager().changeVoice(parameters, new OnChangeVoiceListener() {
				@Override
				public void onChangeVoice(ECError error, Parameters para) {
					if(error.errorCode==SdkErrorCode.REQUEST_SUCCESS){
						instance.playVoice(parameters.outFileName, false);
					}else {
						file.delete();
					}
				}
			});
	
			}
		}
		
		private Parameters getParameters(String appendName){
			Parameters parameters =new Parameters();
			parameters.inFileName=FileAccessor.getVoicePathName().getAbsolutePath()+"/"+mAmrPathName;
			parameters.outFileName=FileAccessor.getVoicePathName().getAbsolutePath()+"/"+appendName+mAmrPathName;
			if("yuansheng".equals(appendName)){
				
			}else if("luoli".equals(appendName)){
				parameters.pitch=12;  //- 12  12
				parameters.tempo=1;  // -0.05 1
			}else if("dashu".equals(appendName)){
				parameters.pitch=2;
				parameters.tempo=1;
			}else if("jingsong".equals(appendName)){
				parameters.pitch=1;
				parameters.tempo=-3;
			}else if("gaoguai".equals(appendName)){
				parameters.pitch=5;
				parameters.tempo=1;
			}else if("kongling".equals(appendName)){
				parameters.pitch=1;
				parameters.tempo=-1;
			}
			return parameters;
		}
		

		public OnChattingFooterImpl(ChattingActivity ctx) {
			mActivity = ctx;
			 instance= MediaPlayTools.getInstance();
		}

		@Override
		public void OnVoiceRcdInitReuqest() {
			mAmrPathName = DemoUtils.md5(String.valueOf(System
					.currentTimeMillis())) + ".amr";
			if (FileAccessor.getVoicePathName() == null) {
				ToastUtil.showMessage("Path to file could not be created");
				mAmrPathName = null;
				return;
			}
			keepScreenOnState(true);
            if (getRecordState() != RECORD_ING) {
                setRecordState(RECORD_ING);

                // 手指按下按钮，按钮给予振动或者声音反馈
                readyOperation();
                // 显示录音提示框
                mChattingFooter.showVoiceRecordWindow(findViewById(R.id.chatting_bg_ll).getHeight() - mChattingFooter.getHeight());

                final ECChatManager chatManager = SDKCoreHelper.getECChatManager();
                if(chatManager == null) {
                    return ;
                }
                mVoiceHandler.post(new Runnable() {

                    @SuppressWarnings("deprecation")
					@Override
                    public void run() {
                        try {
                            ECMessage message = ECMessage.createECMessage(ECMessage.Type.VOICE);
                            message.setTo(mRecipients);
                            ECVoiceMessageBody messageBody = new ECVoiceMessageBody(new File(FileAccessor.getVoicePathName() ,mAmrPathName ), 0);
                            message.setBody(messageBody);
                            mPreMessage = message;
                            // 仅录制语音消息，录制完成后需要调用发送接口发送消息
							handleSendUserStateMessage("2");
                            chatManager.startVoiceRecording(messageBody, new ECChatManager.OnRecordTimeoutListener() {
                                @Override
                                public void onRecordingTimeOut(long duration) {
									LogUtil.d(TAG , "onRecordingTimeOut");
                                    // 如果语音录制超过最大60s长度,则发送
                                    
                                    if(mChattingFooter.isChangeVoice){
                                    	OnVoiceRcdStopRequest(false);
                                    	mChattingFooter.showBianShengView();
                                    }else {
                                    	doProcesOperationRecordOver(false,true);
                                    }
                                    
                                    
                                }

                                @Override
                                public void onRecordingAmplitude(
                                        double amplitude) {
                                    // 显示声音振幅
                                    if(mChattingFooter != null && getRecordState()  == RECORD_ING) {
                                        mChattingFooter.showVoiceRecording();
                                        mChattingFooter.displayAmplitude(amplitude);
                                    }
                                }

                            });
                        } catch (Exception e) {
                            LogUtil.e(TAG , "请检查录音权限是否被禁止");
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        @Override
        public void OnVoiceRcdStartRequest() {
            // SDK完成初始化底层音频设备、开始采集音频数据
            mHandler.removeMessages(WHAT_ON_COMPUTATION_TIME);
            mHandler.sendEmptyMessageDelayed(WHAT_ON_COMPUTATION_TIME, TONE_LENGTH_MS);
        }

        @Override
        public void OnVoiceRcdCancelRequest() {
			handleSendUserStateMessage("0");
            handleMotionEventActionUp(true,false);
        }

        @Override
        public void OnVoiceRcdStopRequest(boolean isSend) {
			handleSendUserStateMessage("0");
            handleMotionEventActionUp(false,isSend);
        }

        @Override
        public void OnSendTextMessageRequest(CharSequence text) {
            if(text != null && text.toString().trim().startsWith("starttest://")) {

                handleTest(text.toString().substring("starttest://".length()));
                return ;
            } else if (text != null && text.toString().trim().startsWith("endtest://")) {
                debugeTest = false;
                return ;
            }
            handleSendTextMessage(text);
        }

        @Override
        public void OnUpdateTextOutBoxRequest(CharSequence text) {

        }

        @Override
        public void OnSendCustomEmojiRequest(int emojiid, String emojiName) {

        }

        @Override
        public void OnEmojiDelRequest() {

        }

        @Override
        public void OnInEditMode() {
            scrollListViewToLast();
        }

        @Override
        public void onPause() {
            stopPlayVoice();
        }

        @Override
        public void onResume() {

        }

        @Override
        public void release() {
            mActivity = null;
            mPreMessage = null;
            bianShengFilePath=null;
        }

        /**
         * 处理Button 按钮按下抬起事件
         * @param doCancle 是否取消或者停止录制
         */
        private void handleMotionEventActionUp(final boolean doCancle,boolean isSend) {
        	keepScreenOnState(false);
            if(getRecordState()  == RECORD_ING) {
                doVoiceRecordAction(doCancle,isSend);
            }
        }

        /**
         * 处理语音录制结束事件
         * @param doCancle 是否取消或者停止录制
         */
        private void doVoiceRecordAction(boolean doCancle,final boolean isSend) {
            final boolean cancleVoice = doCancle;
            
            if(mChatManager != null) {
                mVoiceHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // 停止或者取消普通模式语音
                        LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "handleMotionEventActionUp stop normal record");
                        mChatManager.stopVoiceRecording(new ECChatManager.OnStopVoiceRecordingListener() {
                            @Override
                            public void onRecordingComplete() {
								LogUtil.d(TAG , "onRecordingComplete");
                                doProcesOperationRecordOver(cancleVoice,isSend);
                            }
                        });
                    }
                });
            }


        }
       
        

        /**
         * 处理录音结束消息是否发送逻辑
         * @param cancle 是否取消发送
         */
        protected void doProcesOperationRecordOver(boolean cancle,boolean isSend) {
            if(getRecordState() == RECORD_ING) {
                // 当前是否有正在录音的操作
                // 定义标志位判断当前所录制的语音文件是否符合发送条件
                // 只有当录制的语音文件的长度超过1s才进行发送语音
                boolean isVoiceToShort = false;
                File amrPathFile = new File(FileAccessor.getVoicePathName() ,mAmrPathName);
                if(amrPathFile.exists()) {
                    mVoiceRecodeTime = DemoUtils.calculateVoiceTime(amrPathFile.getAbsolutePath());
                    if(!isRecordAndSend) {
                        if (mVoiceRecodeTime * 1000 < MIX_TIME) {
                            isVoiceToShort = true;
                        }
                    }
                } else {
                    isVoiceToShort = true;
                }
                // 设置录音空闲状态
                setRecordState(RECORD_IDLE);
                if(mChattingFooter != null ) {
                    if (isVoiceToShort && !cancle) {
                        // 提示语音文件长度太短
                        mChattingFooter.tooShortPopuWindow();
                        return;
                    }
                    
                    if(!isSend&&mChattingFooter.isChangeVoice&&!cancle){
                    	mChattingFooter.showBianShengView();
                    }
                    // 关闭语音录制对话框
                    mChattingFooter.dismissPopuWindow();
                }
                
                
                

                if(!cancle && mPreMessage != null&&isSend) {
                    if(!isRecordAndSend) {
                        // 如果当前的录音模式为非Chunk模式
                        try {
                            ECVoiceMessageBody body = (ECVoiceMessageBody) mPreMessage.getBody();
                            body.setDuration(mVoiceRecodeTime);
                            long rowId;
                            if(mCustomerService) {
                                rowId = CustomerServiceHelper.sendMCMessage(mPreMessage);
                            } else {
                                rowId = IMChattingHelper.sendECMessage(mPreMessage);
                            }
                            mPreMessage.setId(rowId);
                            notifyIMessageListView(mPreMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return ;
                }

                // 删除语音文件
                amrPathFile.deleteOnExit();
                // 重置语音时间长度统计
                mVoiceRecodeTime = 0;
            }
        }

        public int getRecordState() {
            synchronized (mLock) {
                return mRecordState;
            }
        }

        public void setRecordState(int state) {
            synchronized (mLock) {
                this.mRecordState = state;
            }
        }

		@Override
		public void onVoiceChangeRequest(int position) {
			handlerChangeVoice(position);
			
			
		}

		private void handlerChangeVoice(int position) {

		
		switch (position) {
		case 0:
			if(instance!=null){
			    instance.playVoice(FileAccessor.getVoicePathName()+"/"+mAmrPathName, false);
			}
			break;
		case 1:
			changeVoiceInSDK("luoli");
			
			break;
		case 2:
			
			changeVoiceInSDK("dashu");
			break;
		case 3:
			
			changeVoiceInSDK("jingsong");
			break;
		case 4:
			changeVoiceInSDK("gaoguai");
			
			break;
		case 5:
			changeVoiceInSDK("kongling");
			
			break;

		default:
			break;
		}
		
	}

		public void sendChangeVoiceMsg(boolean isSendYuanSheng) {
			if( mPreMessage != null) {
                if(!isRecordAndSend) {
                    // 如果当前的录音模式为非Chunk模式
                    try {
                        ECVoiceMessageBody body = (ECVoiceMessageBody) mPreMessage.getBody();
                        
                        if(isSendYuanSheng){
                        	body.setDuration(DemoUtils.calculateVoiceTime(FileAccessor.getVoicePathName()+"/"+mAmrPathName));
                            body.setLocalUrl(FileAccessor.getVoicePathName()+"/"+mAmrPathName);
                        }else {
                        	body.setDuration(DemoUtils.calculateVoiceTime(bianShengFilePath));
                            body.setLocalUrl(bianShengFilePath);
                        }
                        
                        
                        long rowId;
                        if(mCustomerService) {
                            rowId = CustomerServiceHelper.sendMCMessage(mPreMessage);
                        } else {
                            rowId = IMChattingHelper.sendECMessage(mPreMessage);
                        }
                        mPreMessage.setId(rowId);
                        notifyIMessageListView(mPreMessage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return ;
            }
		}

		@Override
		public void stopVoicePlay() {
			// TODO Auto-generated method stub
			if(instance!=null&&instance.isPlaying()){
				instance.stop();
			}
			
		}

    }

    private void stopPlayVoice() {
        if(mChattingAdapter != null)  {
            // 停止播放语音
            mChattingAdapter.onPause();
            mChattingAdapter.notifyDataSetChanged();
        }
    }

	
	
	
	

	public void handleVideoCall() {
		ECContacts contact = ContactSqlManager.getContact(mRecipients);

		if (contact == null) {
			return;
		}

	}

	public void handleVoiceCall() {
		ECContacts contact = ContactSqlManager.getContact(mRecipients);
		if (contact == null) {
			return;
		}

	}

	private void handleVideoRecord() {

		Intent intent = new Intent();
		intent.setClass(getActivity(), VideoRecordActivity.class);
		startActivityForResult(intent,
				GlobalConstant.ACTIVITY_FOR_RESULT_VIDEORECORD);
	}

	public class ChattingAsyncTask extends ECAsyncTask {

        /**
         * @param context
         */
        public ChattingAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected Object doInBackground(Object... params) {
            ImgInfo createImgInfo ;
            if(((String)params[0]).endsWith(".gif")) {
                createImgInfo = ImgInfoSqlManager.getInstance().createGIFImgInfo((String)params[0]);
            } else {
                createImgInfo = ImgInfoSqlManager.getInstance().createImgInfo((String)params[0]);
            }
            return createImgInfo;
        }

        @Override
        protected void onPostExecute(Object result) {
            if(result instanceof ImgInfo) {
                ImgInfo imgInfo = (ImgInfo) result;
                handleSendImageMessage(imgInfo);
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                setIsFinish(true);
                hideSoftKeyboard();
                finish();
                break;
            case R.id.btn_right:
                if(!isPeerChat()) {
                    // 如果是点对点聊天
                    ECContacts contact = ContactSqlManager.getContact(mRecipients);
                    Intent intent = new Intent(getActivity(), ContactDetailActivity.class);
                    intent.putExtra(ContactDetailActivity.RAW_ID, contact.getId());
                    startActivityForResult(intent, REQUEST_VIEW_CARD);

                    return ;
                }
                // 群组聊天室
                Intent intent = new Intent(getActivity(), GroupInfoActivity.class);
                intent.putExtra(GroupInfoActivity.GROUP_ID, mRecipients);
                startActivityForResult(intent, REQUEST_VIEW_CARD);
                break;
            case R.id.btn_middle:
                if (mListView != null) {
                    getTopBarView().post(new Runnable() {
                        @Override
                        public void run() {
                            mListView.setSelection(mChattingAdapter.getCount());
                        }
                    });
                }
                break;
            default:
                break;
        }
    }



    private OnRefreshAdapterDataListener mOnRefreshAdapterDataListener = new OnRefreshAdapterDataListener() {

        @Override
        public void refreshData() {//刷新数据
            if(getActivity() == null || getActivity().isFinishing()) {
                return ;
            }
            int size = mChattingAdapter.increaseCount();
            mChattingAdapter.checkTimeShower();
            mChattingAdapter.notifyChange();
            int count = mChattingAdapter.getCount() - size;
            LogUtil.d(TAG, "onRefreshing history msg count " + count);
            mListView.setSelectionFromTop(size + 1, mListViewHeadView.getHeight() + mECPullDownView.getTopViewHeight());
        }

    };

    private OnListViewBottomListener mOnListViewBottomListener = new OnListViewBottomListener() {

        @Override
        public boolean getIsListViewToBottom() {
            View lastChildAt = mListView.getChildAt(mListView.getChildCount() - 1) ;
            if(lastChildAt == null) {
                return false;
            }
            if((lastChildAt.getBottom() <= mListView.getHeight())
                    && mListView.getLastVisiblePosition() == mListView.getAdapter().getCount() - 1) {
                return true;
            }
            return false;
        }
    };

    private OnListViewTopListener mOnListViewTopListener = new OnListViewTopListener() {

        @Override
        public boolean getIsListViewToTop() {
            View topChildAt =  mListView.getChildAt(mListView.getFirstVisiblePosition());
            return ((topChildAt != null) && (topChildAt.getTop() == 0));
        }
    };


    private AdapterView.OnItemLongClickListener mOnItemLongClickListener
            = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final int itemPosition = position;
            if(mChattingAdapter != null) {
                int headerViewsCount = mListView.getHeaderViewsCount();
                if (itemPosition < headerViewsCount) {
                    return false;
                }
                int _position = itemPosition - headerViewsCount;

                if (mChattingAdapter == null || mChattingAdapter.getItem(_position) == null) {
                    return false;
                }
                ECMessage item = mChattingAdapter.getItem(_position);
                String title = mUsername;
				int arrRes =R.array.chat_menu_left;
				item.setSessionId(mRecipients);
                if(item.getDirection() == ECMessage.Direction.SEND) {
                    title = CCPAppManager.getClientUser().getUserName();
					if(isPeerChat()){
						if(isVail(item)){
							arrRes=R.array.chat_menu_group;
						}else {
							arrRes=R.array.chat_menu_group_back;
						}
					}else {
						if(isVail(item)){
							arrRes=R.array.chat_menu;
						}else {
							arrRes=R.array.chat_menu_no_back;
						}
					}
                }
                ECListDialog dialog;
                if(item.getType() == ECMessage.Type.TXT) {
                    // 文本有复制功能
					if(isVail(item)){
					}
                    dialog = new ECListDialog(getActivity() , arrRes);
                } else {
					arrRes=R.array.chat_menu_left5;
					if(item.getDirection()== ECMessage.Direction.SEND){
						if(isPeerChat()){
							if(isVail(item)){
								arrRes = R.array.chat_menu_left2;
							}else {
								arrRes = R.array.chat_menu_left3;
							}
						}else {
							if(isVail(item)){
								arrRes = R.array.chat_menu_left4;
							}else {
								arrRes = R.array.chat_menu_left5;
							}
						}
					}
                    dialog = new ECListDialog(getActivity() , arrRes);
                }
				final int arrFial =arrRes;
                dialog.setOnDialogItemClickListener(new ECListDialog.OnDialogItemClickListener() {
                    @Override
                    public void onDialogItemClick(Dialog d, int position) {
                        handleContentMenuClick(itemPosition ,position,arrFial);
                    }
                });
                dialog.setTitle(title);
                dialog.show();
                return true;
            }
            return false;
        }
    };

	private  boolean isVail(ECMessage msg){

		if(msg==null){
			return false;
		}

		long time = System.currentTimeMillis();


		return time-msg.getMsgTime()<=1000*120;
	}







    private Boolean handleContentMenuClick(int convresion ,int position,int arr) {
        if(mChattingAdapter != null) {
            int headerViewsCount = mListView.getHeaderViewsCount();
            if (convresion < headerViewsCount) {
                return false;
            }
            int _position = convresion - headerViewsCount;

            if (mChattingAdapter == null || mChattingAdapter.getItem(_position) == null) {
                return false;
            }
            final ECMessage msg = mChattingAdapter.getItem(_position);
			LogUtil.d(TAG,"ECMessage  get msgId is ="+msg.getMsgId());
			msg.setSessionId(mRecipients);

			String[] resarr= getResources().getStringArray(arr);
			String i =resarr[position];

			if("删除".endsWith(i)){
				doDelMsgTips(msg, _position);

			}else if("撤销".endsWith(i)){
				ECChatManager chatManager = SDKCoreHelper.getECChatManager();
				if(chatManager ==null||msg==null){
					return null;
				}
				if(msg.getDirection()== ECMessage.Direction.SEND){

						chatManager.revokeMessage(msg, new ECChatManager.OnRevokeMessageListener() {
							@Override
							public void onRevokeMessage(ECError error, ECMessage message) {
								if(error.errorCode ==SdkErrorCode.REQUEST_SUCCESS){
									ToastUtil.showMessage("撤回成功");
									IMessageSqlManager.insertSysMessage("你撤回了一条消息",mRecipients);
									IMessageSqlManager.delSingleMsg(msg.getMsgId());
									mChattingAdapter.notifyChange();
								}else {
									ToastUtil.showMessage("撤回失败"+error.errorCode);
								}
							}
						});

				}

			}else if("复制消息".endsWith(i)){

				if(msg.getType() == ECMessage.Type.TXT) {
					ECTextMessageBody body = (ECTextMessageBody) msg.getBody();
					ClipboardUtils.copyFromEdit(getActivity(), getString(R.string.app_pic), body.getMessage());
					ToastUtil.showMessage(R.string.app_copy_ok);
				}else {
					ECChatManager chatManager = SDKCoreHelper.getECChatManager();
					if(chatManager ==null||msg==null){
						return null;
					}

					if(msg.getDirection()== ECMessage.Direction.SEND) {
						chatManager.revokeMessage(msg, new ECChatManager.OnRevokeMessageListener() {
							@Override
							public void onRevokeMessage(ECError error, ECMessage message) {
								if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {

									ToastUtil.showMessage("撤回成功");
									IMessageSqlManager.insertSysMessage("你撤回了一条消息", mRecipients);
									IMessageSqlManager.delSingleMsg(msg.getMsgId());
									mChattingAdapter.notifyChange();
								} else {
									ToastUtil.showMessage("撤回失败" + error.errorCode);
								}
							}
						});
					}

				}

			}else if("查看已读未读".endsWith(i)){
				ECAlertDialog.buildAlert(getActivity(), "请选择", "已读", "未读", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

										Intent action = new Intent();
				action.setClass(getChattingActivity(), ECMessageFeedUI.class);
				msg.setSessionId(mRecipients);
						action.putExtra("type",1);
						ECMessageFeedUI.message =msg;
				startActivity(action);


					}
				}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent action = new Intent();
				action.setClass(getChattingActivity(), ECMessageFeedUI.class);
				msg.setSessionId(mRecipients);
						ECMessageFeedUI.message =msg;
						action.putExtra("type",2);
				startActivity(action);

					}
				}).show();


			}
        }
        return null;
    }





    /**
     *
     * @param msg
     * @param position
     */
    public void doDelMsgTips(final ECMessage msg , final int position) {
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(getActivity(), R.string.app_delete_tips, null, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mHandlerHelper.postRunnOnThead(new Runnable() {
                    @Override
                    public void run() {
                        IMessageSqlManager.delSingleMsg(msg.getMsgId());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mChattingAdapter.notifyChange();
                            }
                        });
                    }
                });
            }
        });
        buildAlert.setTitle(R.string.chatting_resend_title);
        buildAlert.show();
    }

    public static class SmoothScrollToPosition {
        public static void setSelectionFromTop(ListView listview , int position, int y , boolean smooth) {
            if(listview == null) {
                return ;
            }
            LogUtil.i(TAG , "setSelectionFromTop position " + position + " smooth " + smooth);
            listview.setItemChecked(position, true);
            listview.setSelectionFromTop(position , y);
        }


        public static void setSelection(ListView listview , int position, boolean smooth) {
            if(listview == null) {
                return ;
            }
            LogUtil.i(TAG , "setSelection position " + position + " smooth " + smooth);
            listview.setItemChecked(position, true);
            listview.setSelection(position);
        }
    }



    /****************************在线客服****************************/
    @Override
    public void onServiceStart(String event) {
        ToastUtil.showMessage("开启咨询[" + event + "]");
    }

    @Override
    public void onServiceFinish(String even) {

    }

    @Override
    public void onError(ECError error) {

    }
    /****************************在线客服****************************/




    /*******************************************DEBUGE START*********************************************/
    private void handleTest(final String count) {
        if(TextUtils.isEmpty(count) || count.trim().length() == 0) {
            ToastUtil.showMessage("测试协议失败，测试消息条数必须大于0");
            return ;
        }
        final String text = getString(R.string.app_test_message);
        // final String text = getTestText();
        ECAlertDialog buildAlert = ECAlertDialog.buildAlert(getActivity(), "是否开始发送"+count+"条测试消息\n["+text+"]？",  new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mHandlerHelper.postRunnOnThead(new Runnable() {
                    @Override
                    public void run() {
                        debugeTest = true;
                        doStartTest(count, text);
                    }
                });
                mChattingFooter.setEditText("endtest://");
            }
        });
        buildAlert.setTitle("开发模式");
        buildAlert.show();

    }


	/**
	 * 发送红包消息
	 */
	private void handlesendRedPacketMessage(Intent data) {
		String greetings = data.getStringExtra(RedPacketConstant.EXTRA_RED_PACKET_GREETING);
		String moneyID = data.getStringExtra(RedPacketConstant.EXTRA_RED_PACKET_ID);
		String specialReceiveId = data.getStringExtra(RedPacketConstant.EXTRA_RED_PACKET_RECEIVER_ID);
		String redPacketType = data.getStringExtra(RedPacketConstant.EXTRA_RED_PACKET_TYPE);
		String text = "[" + getResources().getString(R.string.ytx_luckymoney) + "]" + greetings;

		com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
		jsonObject.put(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_MESSAGE, true);//是否是红包消息
		jsonObject.put(RedPacketConstant.EXTRA_SPONSOR_NAME, getResources().getString(R.string.ytx_luckymoney));//红包sponsor name
		jsonObject.put(RedPacketConstant.EXTRA_RED_PACKET_GREETING, greetings);//祝福语
		jsonObject.put(RedPacketConstant.EXTRA_RED_PACKET_ID, moneyID);//红包id
		jsonObject.put(RedPacketConstant.MESSAGE_ATTR_RED_PACKET_TYPE, redPacketType);//红包类型，是否是专属红包
		jsonObject.put(RedPacketConstant.MESSAGE_ATTR_SPECIAL_RECEIVER_ID, specialReceiveId);//指定接收者
		// 组建一个待发送的ECMessage
		ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
		// 设置消息接收者
		msg.setTo(mRecipients);
		msg.setUserData(jsonObject.toJSONString());
		// 创建一个文本消息体，并添加到消息对象中
		ECTextMessageBody msgBody = new ECTextMessageBody(text.toString());
		msg.setBody(msgBody);
		String[] at = mChattingFooter.getAtSomeBody();
		msgBody.setAtMembers(at);
		mChattingFooter.clearSomeBody();
		try {
			// 发送消息，该函数见上
			long rowId = -1;
			if (mCustomerService) {
				rowId = CustomerServiceHelper.sendMCMessage(msg);
			} else {
				rowId = IMChattingHelper.sendECMessage(msg);
			}
			// 通知列表刷新
			msg.setId(rowId);
			notifyIMessageListView(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


    private boolean debugeTest = false;
    private void doStartTest(String count , final String text) {
        try {
            final int num = Integer.parseInt(count);
            ECHandlerHelper handlerHelper = new ECHandlerHelper();
            handlerHelper.postRunnOnThead(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showMessage("开始测试.");
                    for (int i = 0; i < num && debugeTest ; i++) {
                        try {
                            ToastUtil.showMessage("正在发送第[" + (i+1) + "]条测试消息");
                            final String pretext = "[第" + (i+1) + "条]\n" + text;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    handleSendTextMessage(pretext);
                                }
                            });
                            Thread.sleep(10);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mChattingFooter.setEditTextNull();
                            ToastUtil.showMessage("测试结束...");
                        }
                    });
                }
            });
        } catch (Exception e) {}
    }

    

    /*******************************************DEBUGE END*********************************************/


    public interface OnChattingAttachListener {
        void onChattingAttach();
    }
    
    private  ECProgressDialog mPostingdialog;
    
    public  void showProcessDialog() {
		if (mPostingdialog != null && mPostingdialog.isShowing()) {
			return;
		}
		mPostingdialog = new ECProgressDialog(getActivity(),
				R.string.downloading);
		mPostingdialog.show();
	}

	

	/**
	 * 关闭对话框
	 */
	public void dismissPostingDialog() {
		if (mPostingdialog == null || !mPostingdialog.isShowing()) {
			return;
		}
		mPostingdialog.dismiss();
		mPostingdialog = null;
		
		ToastUtil.showMessage("下载完成,再次点击即可播放");
	}


	public void sendRedPacketAckMessage(String senderId, String senderNickName) {
		com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
		jsonObject.put(RedPacketConstant.MESSAGE_ATTR_IS_RED_PACKET_ACK_MESSAGE, true);//是否是红包领取消息
		jsonObject.put(RedPacketConstant.EXTRA_RED_PACKET_SENDER_NAME, senderNickName);//发送者昵称
		jsonObject.put(RedPacketConstant.EXTRA_RED_PACKET_SENDER_ID, senderId);//发送者id
		jsonObject.put(RedPacketConstant.EXTRA_RED_PACKET_RECEIVER_NAME, clientUser.getUserName());//接收者昵称
		jsonObject.put(RedPacketConstant.EXTRA_RED_PACKET_RECEIVER_ID, clientUser.getUserId());//接收者id
		String text = getResources().getString(R.string.ytx_luckymoney);
		if (senderId.equals(clientUser.getUserId())) {
			text = this.getResources().getString(R.string.money_msg_take_money);
		} else {
			text = String.format(getResources().getString(R.string.money_msg_take_someone_money), senderNickName);
		}
		// 组建一个待发送的ECMessage
		ECMessage msg = ECMessage.createECMessage(ECMessage.Type.TXT);
		// 设置消息接收者
		msg.setTo(mRecipients);
		msg.setUserData(jsonObject.toJSONString());
		msg.setMsgStatus(ECMessage.MessageStatus.RECEIVE);
		msg.setSessionId(mRecipients);
		// 创建一个文本消息体，并添加到消息对象中
		ECTextMessageBody msgBody = new ECTextMessageBody(text.toString());
		msg.setBody(msgBody);
		String[] at = mChattingFooter.getAtSomeBody();
		msgBody.setAtMembers(at);
		mChattingFooter.clearSomeBody();
		try {
			// 发送消息，该函数见上
			long rowId = -1;
			if (mCustomerService) {
				rowId = CustomerServiceHelper.sendMCMessage(msg);
			} else {
				rowId = IMChattingHelper.sendECMessage(msg);
			}
			// 通知列表刷新
			msg.setId(rowId);
			notifyIMessageListView(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    
    
    
}
