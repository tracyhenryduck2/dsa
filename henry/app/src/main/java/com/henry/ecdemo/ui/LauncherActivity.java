
package com.henry.ecdemo.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.henry.ecdemo.ECApplication;
import com.henry.ecdemo.R;
import com.henry.ecdemo.common.CCPAppManager;
import com.henry.ecdemo.common.ECContentObservers;
import com.henry.ecdemo.common.base.CCPCustomViewPager;
import com.henry.ecdemo.common.base.CCPLauncherUITabView;
import com.henry.ecdemo.common.base.OverflowAdapter;
import com.henry.ecdemo.common.base.OverflowAdapter.OverflowItem;
import com.henry.ecdemo.common.base.OverflowHelper;
import com.henry.ecdemo.common.dialog.ECAlertDialog;
import com.henry.ecdemo.common.dialog.ECListDialog;
import com.henry.ecdemo.common.dialog.ECProgressDialog;
import com.henry.ecdemo.common.utils.CrashHandler;
import com.henry.ecdemo.common.utils.DemoUtils;
import com.henry.ecdemo.common.utils.ECNotificationManager;
import com.henry.ecdemo.common.utils.ECPreferenceSettings;
import com.henry.ecdemo.common.utils.ECPreferences;
import com.henry.ecdemo.common.utils.FileAccessor;
import com.henry.ecdemo.common.utils.LogUtil;
import com.henry.ecdemo.common.utils.ToastUtil;
import com.henry.ecdemo.common.view.NetWarnBannerView;
import com.henry.ecdemo.core.ClientUser;
import com.henry.ecdemo.core.ContactsCache;
import com.henry.ecdemo.storage.ContactSqlManager;
import com.henry.ecdemo.storage.ConversationSqlManager;
import com.henry.ecdemo.storage.GroupNoticeSqlManager;
import com.henry.ecdemo.storage.GroupSqlManager;
import com.henry.ecdemo.storage.IMessageSqlManager;
import com.henry.ecdemo.ui.account.LoginActivity;
import com.henry.ecdemo.ui.chatting.ChattingActivity;
import com.henry.ecdemo.ui.chatting.CustomerServiceHelper;
import com.henry.ecdemo.ui.chatting.IMChattingHelper;
import com.henry.ecdemo.ui.chatting.model.Conversation;
import com.henry.ecdemo.ui.contact.ContactLogic;
import com.henry.ecdemo.ui.contact.ECContacts;
import com.henry.ecdemo.ui.group.GroupNoticeActivity;
import com.henry.ecdemo.ui.group.GroupService;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.ECInitParams;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.im.ECGroupOption;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
/**
 * 主界面（消息会话界面、联系人界面、群组界面）
 */
@ActivityTransition(3)
public class LauncherActivity extends ECFragmentActivity implements CCPListAdapter.OnListAdapterCallBackListener {

	private static final String TAG = "LauncherActivity";
	/**
	 * 当前ECLauncherUI 实例
	 */
	public static LauncherActivity mLauncherUI;

	private InternalReceiver internalReceiver;


	private OverflowHelper mOverflowHelper;

	private OverflowAdapter.OverflowItem[] mItems;
	private ImageView imageView_lv2,imageView_lv,imageView_hong,imageView_hong2,imageView_huang,imageView_cheng;
	private ImageView imageView_zi,imageView_you_and_me;
	private AnimationSet set,set2,set3,set4,set5,set6,set_zi,set_you;

	/**会话消息列表ListView*/
	private ListView mListView;
	private NetWarnBannerView mBannerView;
	private ConversationAdapter mAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		Intent intentGroup =new Intent();
		intentGroup.setAction("com.henry.ecdemo.inited");
		sendBroadcast(intentGroup);
		if (mLauncherUI != null) {
			LogUtil.i(LogUtil.getLogUtilsTag(LauncherActivity.class),
					"finish last LauncherUI");
			mLauncherUI.finish();
		}
		mLauncherUI = this;
		super.onCreate(savedInstanceState);
		initWelcome();
		mOverflowHelper = new OverflowHelper(this);
		// 设置页面默认为竖屏
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		ECContentObservers.getInstance().initContentObserver();
	}

	@Override
	protected boolean isEnableSwipe() {
		return false;
	}

	private boolean mInit = false;

	private Runnable initRunnable = new Runnable() {
		@Override
		public void run() {
			mInit = false;
			initLauncherUIView();
		}
	};

	private void initWelcome(){

		Intent intentGroup =new Intent();
		intentGroup.setAction("com.henry.ecdemo.inited");
		sendBroadcast(intentGroup);

		if (!mInit) {
			mInit = true;
			setContentView(R.layout.activity_splash);
			initView();
			initAnimation();
			initAnimation2();
			initAnimation3();
			initAnimation4();
			initAnimation5();
			initAnimation6();
			initAnimationzi();
			initAnimationyou();
			handler.sendEmptyMessageDelayed(1,0);

		}
	}

	/**
	 * 初始化主界面UI视图
	 */
	private void initLauncherUIView() {
		setContentView(getLayoutInflater().inflate(R.layout.main_tab, null));
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
		mBannerView = new NetWarnBannerView(this);
		mBannerView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				reTryConnect();
			}
		});
		mListView.addHeaderView(mBannerView);
		mAdapter = new ConversationAdapter(this , this);
		mListView.setAdapter(mAdapter);

		registerForContextMenu(mListView);
		Intent intent = getIntent();
		if (intent != null && intent.getIntExtra("launcher_from", -1) == 1) {
			// 检测从登陆界面过来，判断是不是第一次安装使用
			checkFirstUse();
		}

		// 如果是登陆过来的
		doInitAction();

		try {
			updateConnectState();
			IMessageSqlManager.registerMsgObserver(mAdapter);
			mAdapter.notifyChange();
		}catch (Exception e){
			e.printStackTrace();
		}

	}



	/**
	 * 检测离线消息
	 */
	private void checkOffineMessage() {
		if (SDKCoreHelper.getConnectState() != ECDevice.ECConnectState.CONNECT_SUCCESS) {
			return;
		}
		ECHandlerHelper handlerHelper = new ECHandlerHelper();
		handlerHelper.postDelayedRunnOnThead(new Runnable() {
			@Override
			public void run() {
				boolean result = IMChattingHelper.isSyncOffline();
				if (!result) {
					ECHandlerHelper.postRunnOnUI(new Runnable() {
						@Override
						public void run() {
							disPostingLoading();
						}
					});
					IMChattingHelper.checkDownFailMsg();
				}
			}
		}, 1000);
	}

	private boolean isFirstUse(){
		boolean firstUse = ECPreferences.getSharedPreferences().getBoolean(
				ECPreferenceSettings.SETTINGS_FIRST_USE.getId(),
				((Boolean) ECPreferenceSettings.SETTINGS_FIRST_USE
						.getDefaultValue()).booleanValue());
		return firstUse;
	}

	private void checkFirstUse() {
		boolean firstUse = isFirstUse();

		// Display the welcome message?
		if (firstUse) {
			if (IMChattingHelper.isSyncOffline()) {
				mPostingdialog = new ECProgressDialog(this,
						R.string.tab_loading);
				mPostingdialog.setCanceledOnTouchOutside(false);
				mPostingdialog.setCancelable(false);
				mPostingdialog.show();
			}
			// Don't display again this dialog
			try {
				ECPreferences.savePreference(
						ECPreferenceSettings.SETTINGS_FIRST_USE, Boolean.FALSE,
						true);
			} catch (Exception e) {
				/** NON BLOCK **/
			}
		}
	}
	/**
	 * 根据底层库是否支持voip加载相应的子菜单
	 */
	void initOverflowItems() {
		if (mItems == null) {
			if (SDKCoreHelper.getInstance().isSupportMedia()) {
				mItems = new OverflowAdapter.OverflowItem[8];
				mItems[0] = new OverflowAdapter.OverflowItem(getString(R.string.main_plus_inter_phone));
				mItems[1] = new OverflowAdapter.OverflowItem( getString(R.string.main_plus_meeting_voice));
				mItems[2] = new OverflowAdapter.OverflowItem( getString(R.string.main_plus_meeting_video));
				mItems[3] = new OverflowAdapter.OverflowItem( getString(R.string.main_plus_groupchat));
				mItems[4] = new OverflowAdapter.OverflowItem( getString(R.string.main_plus_querygroup));
				mItems[5] = new OverflowAdapter.OverflowItem( getString(R.string.create_discussion));
				mItems[6] = new OverflowAdapter.OverflowItem( getString(R.string.main_plus_settings));
//				mItems[7] = new OverflowAdapter.OverflowItem( getString(R.string.main_plus_sharemeeting));
				mItems[7] = new OverflowAdapter.OverflowItem( getString(R.string.main_plus_live));
			} else {
				mItems = new OverflowAdapter.OverflowItem[4];
				mItems[0] = new OverflowAdapter.OverflowItem( getString(R.string.main_plus_groupchat));
				mItems[1] = new OverflowAdapter.OverflowItem( getString(R.string.main_plus_querygroup));
				mItems[2] = new OverflowAdapter.OverflowItem( getString(R.string.create_discussion));
				mItems[3] = new OverflowAdapter.OverflowItem( getString(R.string.main_plus_settings));
			}
		}

	}


	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		LogUtil.d(LogUtil.getLogUtilsTag(LauncherActivity.class), " onKeyDown");

		if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
				&& event.getAction() == KeyEvent.ACTION_UP) {
			// dismiss PlusSubMenuHelper
			if (mOverflowHelper != null && mOverflowHelper.isOverflowShowing()) {
				mOverflowHelper.dismiss();
				return true;
			}
		}

		// 这里可以进行设置全局性的menu菜单的判断
		if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			doTaskToBackEvent();
		}

		try {

			return super.dispatchKeyEvent(event);
		} catch (Exception e) {
			LogUtil.e(LogUtil.getLogUtilsTag(LauncherActivity.class),
					"dispatch key event catch exception " + e.getMessage());
		}

		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (internalReceiver != null) {
			unregisterReceiver(internalReceiver);
		}
	}

	@Override
	protected void onStart(){
		super.onStart();
		Intent intentGroup =new Intent();
		intentGroup.setAction("com.henry.ecdemo.inited");
		sendBroadcast(intentGroup);

	}

	@Override
	protected void onResume(){
		LogUtil.i(LogUtil.getLogUtilsTag(LauncherActivity.class),
				"onResume start");
		super.onResume();

		Intent intentGroup =new Intent();
		intentGroup.setAction("com.henry.ecdemo.inited");
		sendBroadcast(intentGroup);
		
		CrashHandler.getInstance().setContext(this);

		boolean fullExit = ECPreferences.getSharedPreferences().getBoolean(
				ECPreferenceSettings.SETTINGS_FULLY_EXIT.getId(), false);
		if (fullExit){
			try {
				ECHandlerHelper.removeCallbacksRunnOnUI(initRunnable);
				ECPreferences.savePreference(
						ECPreferenceSettings.SETTINGS_FULLY_EXIT, false, true);
				ContactsCache.getInstance().stop();
				CCPAppManager.setClientUser(null);
				ECDevice.unInitial();
				finish();
				
				android.os.Process.killProcess(android.os.Process.myPid());
				System.exit(0);
				
				return;
			} catch (InvalidClassException e) {
				e.printStackTrace();
			}
		}

			String account = getAutoRegistAccount();
		Log.i("ceshi","登录的账号信息:"+account);
//			if (TextUtils.isEmpty(account)) {
//				startActivity(new Intent(this, LoginActivity.class));
//				finish();
//				return;
//			}


			// 注册第一次登陆同步消息
			registerReceiver(new String[] {
					IMChattingHelper.INTENT_ACTION_SYNC_MESSAGE,
					SDKCoreHelper.ACTION_SDK_CONNECT,GroupService.ACTION_SYNC_GROUP, IMessageSqlManager.ACTION_SESSION_DEL });
			ClientUser user = new ClientUser("").from(account);

		   ClientUser c =	 CCPAppManager.getClientUser();
			if(c!=null){
				user.setpVersion(c.getpVersion());
			}else {

				user = new ClientUser("2");
				user.setAppKey(FileAccessor.getAppKey());
				user.setAppToken(FileAccessor.getAppToken());
				user.setLoginAuthType(ECInitParams.LoginAuthType.NORMAL_AUTH);
			}
			CCPAppManager.setClientUser(user);
			if (!ContactSqlManager.hasContact(user.getUserId())) {
				ECContacts contacts = new ECContacts();
				contacts.setClientUser(user);
				ContactSqlManager.insertContact(contacts);
			}
			
			if (SDKCoreHelper.getConnectState() != ECDevice.ECConnectState.CONNECT_SUCCESS
					&& !SDKCoreHelper.isKickOff()) {

				 SDKCoreHelper.init(this);
			}
			// 初始化主界面Tab资源
			if (!mInit) {
				initLauncherUIView();
			}
		getTopContacts();

		try {
			updateConnectState();
			IMessageSqlManager.registerMsgObserver(mAdapter);
			mAdapter.notifyChange();
		}catch (Exception e){
            e.printStackTrace();
		}


	}

	private void getTopContacts(){

		final ArrayList<String> arrayList =ConversationSqlManager.getInstance().qureyAllSession();
		ECChatManager chatManager =ECDevice.getECChatManager();
		if(chatManager ==null){
			return;
		}
		chatManager.getSessionsOfTop(new ECChatManager.OnGetSessionsOfTopListener() {
			@Override
			public void onGetSessionsOfTopResult(ECError error, String[] sessionsArr) {
				if (error.errorCode == SdkErrorCode.REQUEST_SUCCESS) {
					for (String item : sessionsArr) {
						ConversationSqlManager.updateSessionToTop(item, true);
					}
					List<String> list = Arrays.asList(sessionsArr);
					for (String a : arrayList) {
						if (!list.contains(a)) {
							ConversationSqlManager.updateSessionToTop(a, false);
						}
					}
				}
			}
		});
	}

	public void handlerKickOff(String kickoffText){
		if (isFinishing()) {
			return;
		}
		ECAlertDialog buildAlert = ECAlertDialog.buildAlert(this, kickoffText,
				getString(R.string.dialog_btn_confim),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ECNotificationManager.getInstance()
								.forceCancelNotification();
						restartAPP();
					}
				});
		buildAlert.setTitle("异地登陆");
		buildAlert.setCanceledOnTouchOutside(false);
		buildAlert.setCancelable(false);
		buildAlert.show();
	}

	public void restartAPP(){
		
		ECDevice.unInitial();
		Intent intent = new Intent(this, LauncherActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * 检查是否需要自动登录
	 *
	 * @return
	 */
	private String getAutoRegistAccount(){
		SharedPreferences sharedPreferences = ECPreferences
				.getSharedPreferences();
		ECPreferenceSettings registAuto = ECPreferenceSettings.SETTINGS_REGIST_AUTO;
		String registAccount = sharedPreferences.getString(registAuto.getId(),
				(String) registAuto.getDefaultValue());
		return registAccount;
	}


	/**
	 * 返回隐藏到后台
	 */
	public void doTaskToBackEvent() {
		moveTaskToBack(true);

	}

	/**
	 * 网络注册状态改变
	 *
	 * @param connect
	 */
	public void onNetWorkNotify(ECDevice.ECConnectState connect) {
			updateConnectState();
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		Intent actionIntent = intent;
		String userName = actionIntent.getStringExtra("Main_FromUserName");
		String mSession = actionIntent.getStringExtra("Main_Session");
		ECContacts contacts = ContactSqlManager
				.getContactLikeUsername(userName);
		if (contacts != null) {
			LogUtil.d(LogUtil.getLogUtilsTag(getClass()),
					"[onNewIntent] userName = " + userName + " , contact_id "
							+ contacts.getContactid());

			if (GroupNoticeSqlManager.CONTACT_ID
					.equals(contacts.getContactid())) {
				Intent noticeintent = new Intent(this,
						GroupNoticeActivity.class);
				startActivity(noticeintent);
				return;
			}

			Intent chatIntent = new Intent(this, ChattingActivity.class);
			String recipinets;
			String username;
			if (!TextUtils.isEmpty(mSession) && mSession.startsWith("g")) {
				ECGroup ecGroup = GroupSqlManager.getECGroup(mSession);
				if (ecGroup == null) {
					return;
				}
				recipinets = mSession;
				username = ecGroup.getName();
			} else {
				recipinets = contacts.getContactid();
				username = contacts.getNickname();
			}
			startActivity(chatIntent);

			CCPAppManager.startChattingAction(this, recipinets, username);
			return;
		}
	}


	/**
	 * 注册广播
	 * 
	 * @param actionArray
	 */
	protected final void registerReceiver(String[] actionArray) {
		if (actionArray == null) {
			return;
		}
		IntentFilter intentfilter = new IntentFilter();
		for (String action : actionArray) {
			intentfilter.addAction(action);
		}
		if (internalReceiver == null) {
			internalReceiver = new InternalReceiver();
		}
		registerReceiver(internalReceiver, intentfilter);
	}

	private class InternalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			int error = intent.getIntExtra("error", -1);
			if (intent == null || TextUtils.isEmpty(intent.getAction())) {
				return;
			}
			LogUtil.d(TAG, "[onReceive] action:" + intent.getAction());
			if (IMChattingHelper.INTENT_ACTION_SYNC_MESSAGE.equals(intent
					.getAction())) {
				disPostingLoading();
			} else if (SDKCoreHelper.ACTION_SDK_CONNECT.equals(intent
					.getAction())) {
				doInitAction();
				try {
					updateConnectState();
				}catch (Exception e){
					e.printStackTrace();
				}


				// 初始注册结果，成功或者失败
				if (SDKCoreHelper.getConnectState() == ECDevice.ECConnectState.CONNECT_SUCCESS
						&& error == SdkErrorCode.REQUEST_SUCCESS) {
					try {
						ClientUser user = CCPAppManager.getClientUser();
						ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_REGIST_AUTO,user.toString(), true);
					} catch (InvalidClassException e) {
						e.printStackTrace();
					}
					return;
				}
				if (intent.hasExtra("error")) {
					if (SdkErrorCode.CONNECTTING == error) {
						return;
					}
					if (error == -1) {
						ToastUtil.showMessage("请检查登陆参数是否正确[" + error + "]");
					}
					ToastUtil.showMessage("登录失败，请稍后重试[" + error + "]");
				}


			} else if (SDKCoreHelper.ACTION_KICK_OFF.equals(intent.getAction())) {
				String kickoffText = intent.getStringExtra("kickoffText");
				handlerKickOff(kickoffText);
			}
		}
	}

	private boolean mInitActionFlag;

	/**
	 * 处理一些初始化操作
	 */
	private void doInitAction(){
		if (SDKCoreHelper.getConnectState() == ECDevice.ECConnectState.CONNECT_SUCCESS
				&& !mInitActionFlag) {

			// 检测当前的版本
			SDKCoreHelper.SoftUpdate mSoftUpdate = SDKCoreHelper.mSoftUpdate;
			if (mSoftUpdate != null) {
				if (DemoUtils.checkUpdater(mSoftUpdate.version)) {
					boolean force = mSoftUpdate.force;
					showUpdaterTips(mSoftUpdate.desc , force);
					if (force) {
						return;
					}
				}
			}


			String account = getAutoRegistAccount();
			if (!TextUtils.isEmpty(account)) {
				ClientUser user = new ClientUser("").from(account);
				CCPAppManager.setClientUser(user);
			}
			// 检测离线消息
			checkOffineMessage();
			mInitActionFlag = true;
		}
	}






	private void disPostingLoading() {
		if (mPostingdialog != null && mPostingdialog.isShowing()) {
			mPostingdialog.dismiss();
		}
	}

	ECAlertDialog showUpdaterTipsDialog = null;

	private void showUpdaterTips(String updateDesc ,final boolean force) {
		if (showUpdaterTipsDialog != null) {
			return;
		}
		String negativeText = getString(force ? R.string.settings_logout : R.string.update_next);
		String msg = getString(R.string.new_update_version);
		if(!TextUtils.isEmpty(updateDesc)) {
			msg = updateDesc;
		}
		showUpdaterTipsDialog = ECAlertDialog.buildAlert(this, msg,
				negativeText, getString(R.string.app_update),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						showUpdaterTipsDialog = null;
						if (force) {
							try {
								ECPreferences
										.savePreference(
												ECPreferenceSettings.SETTINGS_FULLY_EXIT,
												true, true);
							} catch (InvalidClassException e) {
								e.printStackTrace();
							}
							restartAPP();
						}
					}
				}, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						CCPAppManager.startUpdater(LauncherActivity.this);
						// restartAPP();
						showUpdaterTipsDialog = null;
					}
				});

		showUpdaterTipsDialog.setTitle(R.string.app_tip);
		showUpdaterTipsDialog.setDismissFalse();
		showUpdaterTipsDialog.setCanceledOnTouchOutside(false);
		showUpdaterTipsDialog.setCancelable(false);
		showUpdaterTipsDialog.show();
	}

	private ECProgressDialog mPostingdialog;

	void showProcessDialog() {
		mPostingdialog = new ECProgressDialog(LauncherActivity.this,
				R.string.login_posting_submit);
		mPostingdialog.show();
	}

	/**
	 * 关闭对话框
	 */
	private void dismissPostingDialog() {
		if (mPostingdialog == null || !mPostingdialog.isShowing()) {
			return;
		}
		mPostingdialog.dismiss();
		mPostingdialog = null;
	}


	private void initView() {
		setContentView(R.layout.activity_splash);
		imageView_lv2 = (ImageView)findViewById(R.id.lv2);
		imageView_lv = (ImageView)findViewById(R.id.lv);
		imageView_hong = (ImageView)findViewById(R.id.hong);
		imageView_hong2 = (ImageView)findViewById(R.id.hong2);
		imageView_huang = (ImageView)findViewById(R.id.huang);
		imageView_cheng = (ImageView)findViewById(R.id.cheng);
		imageView_zi = (ImageView)findViewById(R.id.zi);
		imageView_you_and_me = (ImageView)findViewById(R.id.you_and_me);
		imageView_lv2.setVisibility(View.INVISIBLE);
		imageView_lv.setVisibility(View.INVISIBLE);
		imageView_hong.setVisibility(View.INVISIBLE);
		imageView_hong2.setVisibility(View.INVISIBLE);
		imageView_huang.setVisibility(View.INVISIBLE);
		imageView_cheng.setVisibility(View.INVISIBLE);
		imageView_zi.setVisibility(View.INVISIBLE);
		imageView_you_and_me.setVisibility(View.INVISIBLE);

	}

	private void initAnimation() {
		DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
		set = new AnimationSet(false);
		RotateAnimation rtAnimation = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rtAnimation.setDuration(1500);
		rtAnimation.setFillAfter(true);
		rtAnimation.setInterpolator(decelerateInterpolator);

		ScaleAnimation scAnimation = new ScaleAnimation(0, 1, 0, 1,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		scAnimation.setDuration(1500);
		scAnimation.setFillAfter(true);
		scAnimation.setInterpolator(decelerateInterpolator);

		AlphaAnimation alAnimation = new AlphaAnimation(0, 1);
		alAnimation.setDuration(1500);
		alAnimation.setFillAfter(true);
		alAnimation.setInterpolator(decelerateInterpolator);

		TranslateAnimation translateAnimation = new TranslateAnimation(0f,0f,500f,0f);
		translateAnimation.setDuration(1500);
		translateAnimation.setInterpolator(decelerateInterpolator);
		translateAnimation.setFillAfter(true);

		set.addAnimation(rtAnimation);
		set.addAnimation(scAnimation);
		set.addAnimation(alAnimation);
		set.addAnimation(translateAnimation);
		set.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {

			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
//                startActivity(new Intent(SplashActivity.this,
//                        MainActivity.class));
//                finish();
			}
		});

	}


	private void initAnimation2() {
		DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
		set2 = new AnimationSet(false);
		RotateAnimation rtAnimation = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rtAnimation.setDuration(1500);
		rtAnimation.setFillAfter(true);
		rtAnimation.setInterpolator(decelerateInterpolator);

		ScaleAnimation scAnimation = new ScaleAnimation(0, 1, 0, 1,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		scAnimation.setDuration(1500);
		scAnimation.setFillAfter(true);
		scAnimation.setInterpolator(decelerateInterpolator);

		AlphaAnimation alAnimation = new AlphaAnimation(0, 1);
		alAnimation.setDuration(1500);
		alAnimation.setFillAfter(true);
		alAnimation.setInterpolator(decelerateInterpolator);

		TranslateAnimation translateAnimation = new TranslateAnimation(0f,0f,500f,0f);
		translateAnimation.setDuration(1500);
		translateAnimation.setInterpolator(decelerateInterpolator);
		translateAnimation.setFillAfter(true);

		set2.addAnimation(rtAnimation);
		set2.addAnimation(scAnimation);
		set2.addAnimation(alAnimation);
		set2.addAnimation(translateAnimation);
		set2.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {

			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
//                startActivity(new Intent(SplashActivity.this,
//                        MainActivity.class));
//                finish();
			}
		});

	}

	private void initAnimation3() {
		DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
		set3 = new AnimationSet(false);
		RotateAnimation rtAnimation = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rtAnimation.setDuration(1500);
		rtAnimation.setFillAfter(true);
		rtAnimation.setInterpolator(decelerateInterpolator);

		ScaleAnimation scAnimation = new ScaleAnimation(0, 1, 0, 1,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		scAnimation.setDuration(1500);
		scAnimation.setFillAfter(true);
		scAnimation.setInterpolator(decelerateInterpolator);

		AlphaAnimation alAnimation = new AlphaAnimation(0, 1);
		alAnimation.setDuration(1500);
		alAnimation.setFillAfter(true);
		alAnimation.setInterpolator(decelerateInterpolator);

		TranslateAnimation translateAnimation = new TranslateAnimation(0f,0f,500f,0f);
		translateAnimation.setDuration(1500);
		translateAnimation.setInterpolator(decelerateInterpolator);
		translateAnimation.setFillAfter(true);

		set3.addAnimation(rtAnimation);
		set3.addAnimation(scAnimation);
		set3.addAnimation(alAnimation);
		set3.addAnimation(translateAnimation);
		set3.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {

			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
//                startActivity(new Intent(SplashActivity.this,
//                        MainActivity.class));
//                finish();
			}
		});

	}


	private void initAnimation4() {
		DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
		set4 = new AnimationSet(false);
		RotateAnimation rtAnimation = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rtAnimation.setDuration(1500);
		rtAnimation.setFillAfter(true);
		rtAnimation.setInterpolator(decelerateInterpolator);

		ScaleAnimation scAnimation = new ScaleAnimation(0, 1, 0, 1,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		scAnimation.setDuration(1500);
		scAnimation.setFillAfter(true);
		scAnimation.setInterpolator(decelerateInterpolator);

		AlphaAnimation alAnimation = new AlphaAnimation(0, 1);
		alAnimation.setDuration(1500);
		alAnimation.setFillAfter(true);
		alAnimation.setInterpolator(decelerateInterpolator);

		TranslateAnimation translateAnimation = new TranslateAnimation(0f,0f,500f,0f);
		translateAnimation.setDuration(1500);
		translateAnimation.setInterpolator(decelerateInterpolator);
		translateAnimation.setFillAfter(true);

		set4.addAnimation(rtAnimation);
		set4.addAnimation(scAnimation);
		set4.addAnimation(alAnimation);
		set4.addAnimation(translateAnimation);
		set4.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {

			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
//                startActivity(new Intent(SplashActivity.this,
//                        MainActivity.class));
//                finish();
			}
		});

	}

	private void initAnimation5() {
		DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
		set5 = new AnimationSet(false);
		RotateAnimation rtAnimation = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rtAnimation.setDuration(1500);
		rtAnimation.setFillAfter(true);
		rtAnimation.setInterpolator(decelerateInterpolator);

		ScaleAnimation scAnimation = new ScaleAnimation(0, 1, 0, 1,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		scAnimation.setDuration(1500);
		scAnimation.setFillAfter(true);
		scAnimation.setInterpolator(decelerateInterpolator);

		AlphaAnimation alAnimation = new AlphaAnimation(0, 1);
		alAnimation.setDuration(1500);
		alAnimation.setFillAfter(true);
		alAnimation.setInterpolator(decelerateInterpolator);

		TranslateAnimation translateAnimation = new TranslateAnimation(0f,0f,500f,0f);
		translateAnimation.setDuration(1500);
		translateAnimation.setInterpolator(decelerateInterpolator);
		translateAnimation.setFillAfter(true);

		set5.addAnimation(rtAnimation);
		set5.addAnimation(scAnimation);
		set5.addAnimation(alAnimation);
		set5.addAnimation(translateAnimation);
		set5.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {

			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
//                startActivity(new Intent(SplashActivity.this,
//                        MainActivity.class));
//                finish();
			}
		});

	}



	private void initAnimation6() {
		DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
		set6 = new AnimationSet(false);
		RotateAnimation rtAnimation = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rtAnimation.setDuration(1500);
		rtAnimation.setFillAfter(true);
		rtAnimation.setInterpolator(decelerateInterpolator);

		ScaleAnimation scAnimation = new ScaleAnimation(0, 1, 0, 1,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		scAnimation.setDuration(1500);
		scAnimation.setFillAfter(true);
		scAnimation.setInterpolator(decelerateInterpolator);

		AlphaAnimation alAnimation = new AlphaAnimation(0, 1);
		alAnimation.setDuration(1500);
		alAnimation.setFillAfter(true);
		alAnimation.setInterpolator(decelerateInterpolator);

		TranslateAnimation translateAnimation = new TranslateAnimation(0f,0f,500f,0f);
		translateAnimation.setDuration(1500);
		translateAnimation.setInterpolator(decelerateInterpolator);
		translateAnimation.setFillAfter(true);

		set6.addAnimation(rtAnimation);
		set6.addAnimation(scAnimation);
		set6.addAnimation(alAnimation);
		set6.addAnimation(translateAnimation);
		set6.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {

			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
//                startActivity(new Intent(SplashActivity.this,
//                        MainActivity.class));
//                finish();
			}
		});

	}

	private void initAnimationzi() {
		DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
		set_zi = new AnimationSet(false);
		RotateAnimation rtAnimation = new RotateAnimation(0, 360,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rtAnimation.setDuration(1500);
		rtAnimation.setFillAfter(true);
		rtAnimation.setInterpolator(decelerateInterpolator);

		ScaleAnimation scAnimation = new ScaleAnimation(0, 1, 0, 1,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		scAnimation.setDuration(1500);
		scAnimation.setFillAfter(true);
		scAnimation.setInterpolator(decelerateInterpolator);

		AlphaAnimation alAnimation = new AlphaAnimation(0, 1);
		alAnimation.setDuration(1500);
		alAnimation.setFillAfter(true);
		alAnimation.setInterpolator(decelerateInterpolator);

		set_zi.addAnimation(rtAnimation);
		set_zi.addAnimation(scAnimation);
		set_zi.addAnimation(alAnimation);
		set_zi.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {

			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
//                startActivity(new Intent(SplashActivity.this,
//                        MainActivity.class));
//                finish();
			}
		});

	}


	private void initAnimationyou() {
		DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
		set_you = new AnimationSet(false);

		AlphaAnimation alAnimation = new AlphaAnimation(0, 1);
		alAnimation.setDuration(1500);
		alAnimation.setFillAfter(true);
		alAnimation.setInterpolator(decelerateInterpolator);
		set_you.addAnimation(alAnimation);
		set_you.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {

			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
			}
		});

	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					imageView_lv2.setVisibility(View.VISIBLE);
					imageView_lv2.startAnimation(set);
					handler.sendEmptyMessageDelayed(2,100);
					break;
				case 2:
					imageView_lv.setVisibility(View.VISIBLE);
					imageView_lv.startAnimation(set2);
					handler.sendEmptyMessageDelayed(3,100);
					break;
				case 3:
					imageView_hong.setVisibility(View.VISIBLE);
					imageView_hong.startAnimation(set3);
					handler.sendEmptyMessageDelayed(4,100);
					break;
				case 4:
					imageView_hong2.setVisibility(View.VISIBLE);
					imageView_hong2.startAnimation(set4);
					handler.sendEmptyMessageDelayed(5,100);
					break;
				case 5:
					imageView_huang.setVisibility(View.VISIBLE);
					imageView_huang.startAnimation(set5);
					handler.sendEmptyMessageDelayed(6,100);
					break;
				case 6:
					imageView_cheng.setVisibility(View.VISIBLE);
					imageView_you_and_me.setVisibility(View.VISIBLE);
					imageView_zi.setVisibility(View.VISIBLE);
					imageView_cheng.startAnimation(set6);
					imageView_you_and_me.startAnimation(set_you);
					imageView_zi.startAnimation(set_zi);
					ECHandlerHelper.postDelayedRunnOnUI(initRunnable, 5000);
					break;
			}
		}
	};





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
					Intent intent = new Intent(LauncherActivity.this , GroupNoticeActivity.class);
					startActivity(intent);
					return ;
				}
				if(ContactLogic.isCustomService(conversation.getSessionId())) {
					showProcessDialog();
					dispatchCustomerService(conversation.getSessionId());
					return ;
				}

				CCPAppManager.startChattingAction(LauncherActivity.this , conversation.getSessionId() , conversation.getUsername());
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
				CCPAppManager.startCustomerServiceAction(LauncherActivity.this , event);
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
				ECListDialog dialog = new ECListDialog(LauncherActivity.this, /*new String[]{getString(R.string.main_delete)}*/menu);
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



	private void setcancelTopSession(ArrayList<String> arrayList ,String item){
		if(!arrayList.contains(item)){
			ConversationSqlManager.updateSessionToTop(item,false);
		}

	}


	@Override
	public void onPause() {
		super.onPause();
		IMessageSqlManager.unregisterMsgObserver(mAdapter);
	}



	private void reTryConnect() {
		ECDevice.ECConnectState connectState = SDKCoreHelper.getConnectState();
		if(connectState == null || connectState == ECDevice.ECConnectState.CONNECT_FAILED) {

			if(!TextUtils.isEmpty(getAutoRegistAccount())){
				SDKCoreHelper.init(this);
			}
		}
	}

	public void  updateConnectState() {

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
							LauncherActivity.this.runOnUiThread(new Runnable() {
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
	public void OnListAdapterCallBack() {

	}





}
