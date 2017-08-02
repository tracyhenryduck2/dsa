/*
 *  Copyright (c) 2015 The CCP project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a Beijing Speedtong Information Technology Co.,Ltd license
 *  that can be found in the LICENSE file in the root of the web site.
 *
 *   http://www.yuntongxun.com
 *
 *  An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */
package com.henry.ecdemo.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;

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
import com.henry.ecdemo.common.dialog.ECProgressDialog;
import com.henry.ecdemo.common.utils.CrashHandler;
import com.henry.ecdemo.common.utils.DemoUtils;
import com.henry.ecdemo.common.utils.ECNotificationManager;
import com.henry.ecdemo.common.utils.ECPreferenceSettings;
import com.henry.ecdemo.common.utils.ECPreferences;
import com.henry.ecdemo.common.utils.LogUtil;
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
import com.henry.ecdemo.ui.contact.ContactLogic;
import com.henry.ecdemo.ui.contact.ECContacts;
import com.henry.ecdemo.ui.contact.MobileContactActivity;
import com.henry.ecdemo.ui.contact.MobileContactSelectActivity;
import com.henry.ecdemo.ui.group.BaseSearch;
import com.henry.ecdemo.ui.group.CreateGroupActivity;
import com.henry.ecdemo.ui.group.ECDiscussionActivity;
import com.henry.ecdemo.ui.group.GroupNoticeActivity;
import com.henry.ecdemo.ui.phonemodel.PhoneRouterUI;
import com.henry.ecdemo.ui.settings.SettingPersionInfoActivity;
import com.henry.ecdemo.ui.settings.SettingsActivity;
import com.yuntongxun.ecsdk.ECChatManager;
import com.yuntongxun.ecsdk.ECDevice;
import com.yuntongxun.ecsdk.ECError;
import com.yuntongxun.ecsdk.SdkErrorCode;
import com.yuntongxun.ecsdk.im.ECGroup;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

//import com.henry.ecdemo.ui.fsmeeting.FSVideoconferenceConversation;

//import com.henry.ecdemo.ui.fsmeeting.FSVideoconferenceConversation;
//import com.henry.ecdemo.ui.settings.SampleActivity;

/**
 * 主界面（消息会话界面、联系人界面、群组界面）
 */
@ActivityTransition(3)
public class LauncherActivity extends ECFragmentActivity implements
		View.OnClickListener, View.OnLongClickListener ,
		ConversationListFragment.OnUpdateMsgUnreadCountsListener {

	private static final String TAG = "LauncherActivity";
	/**
	 * 当前ECLauncherUI 实例
	 */
	public static LauncherActivity mLauncherUI;

	/**
	 * 当前ECLauncherUI实例产生个数
	 */
	public static int mLauncherInstanceCount = 0;

	/**
	 * 当前主界面RootView
	 */
	public View mLauncherView;

	/**
	 * LauncherUI 主界面导航控制View ,包含三个View Tab按钮
	 */
	private CCPLauncherUITabView mLauncherUITabView;
	/**
	 * 三个TabView所对应的三个页面的适配器
	 */
	private CCPCustomViewPager mCustomViewPager;

	/**
	 * 沟通、联系人、群组适配器
	 */
	public LauncherViewPagerAdapter mLauncherViewPagerAdapter;

	private OverflowHelper mOverflowHelper;

	/**
	 * 当前显示的TabView Fragment
	 */
	private int mCurrentItemPosition = -1;

	/**
	 * 会话界面(沟通)
	 */
	private static final int TAB_CONVERSATION = 0;

	/**
	 * 通讯录界面(联系人)
	 */
	private static final int TAB_ADDRESS = 1;

	/**
	 * 群组界面
	 */
	private static final int TAB_GROUP = 2;
	private static final int TAB_DISCUSSION_GROUP = 3;

	/**
	 * {@link CCPLauncherUITabView} 是否已经被初始化
	 */
	private boolean mTabViewInit = false;

	/**
	 * 缓存三个TabView
	 */
	private final HashMap<Integer, Fragment> mTabViewCache = new HashMap<Integer, Fragment>();
	private OverflowAdapter.OverflowItem[] mItems;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		int pid = android.os.Process.myPid();

		Intent intentGroup =new Intent();
		intentGroup.setAction("com.henry.ecdemo.inited");
		sendBroadcast(intentGroup);



//		LogUtil.e(Build.BOARD);xe1102h
		if (mLauncherUI != null) {
			LogUtil.i(LogUtil.getLogUtilsTag(LauncherActivity.class),
					"finish last LauncherUI");
			mLauncherUI.finish();
		}
//		CCPAppManager.addActivityUI(mLauncherUI);
		mLauncherUI = this;
		mLauncherInstanceCount++;
		super.onCreate(savedInstanceState);
		initWelcome();
		mOverflowHelper = new OverflowHelper(this);
		// umeng
//		MobclickAgent.updateOnlineConfig(this);
//		MobclickAgent.setDebugMode(true);
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
			setContentView(R.layout.splash_activity);


			// 程序启动开始创建一个splash用来初始化程序基本数据
			ECHandlerHelper.postDelayedRunnOnUI(initRunnable, 3000);
		}
	}

	/**
	 * 初始化主界面UI视图
	 */
	private void initLauncherUIView() {
		mLauncherView = getLayoutInflater().inflate(R.layout.main_tab, null);
		setContentView(mLauncherView);

		mTabViewInit = true;
		mCustomViewPager = (CCPCustomViewPager) findViewById(R.id.pager);
		mCustomViewPager.setOffscreenPageLimit(4);

		if (mLauncherUITabView != null) {
			mLauncherUITabView.setOnUITabViewClickListener(null);
			mLauncherUITabView.setVisibility(View.VISIBLE);
		}
		mLauncherUITabView = (CCPLauncherUITabView) findViewById(R.id.laucher_tab_top);
		mCustomViewPager.setSlideEnabled(true);
		mLauncherViewPagerAdapter = new LauncherViewPagerAdapter(this,
				mCustomViewPager);
		mLauncherUITabView
				.setOnUITabViewClickListener(mLauncherViewPagerAdapter);

		findViewById(R.id.btn_plus).setOnClickListener(this);
		findViewById(R.id.btn_plus).setOnLongClickListener(this);
		ctrlViewTab(0);

		Intent intent = getIntent();
		if (intent != null && intent.getIntExtra("launcher_from", -1) == 1) {
			// 检测从登陆界面过来，判断是不是第一次安装使用
			checkFirstUse();
		}

		// 如果是登陆过来的
		doInitAction();
	}

	private void settingPersonInfo(){//selftest
		if (isUpSetPersonInfo()) {
			Intent settingAction = new Intent(this,SettingPersionInfoActivity.class);
			settingAction.putExtra("from_regist", true);
			startActivityForResult(settingAction, 0x2a);
		}
	}

	public static boolean isUpSetPersonInfo(){
		ClientUser user = CCPAppManager.getClientUser();

		LogUtil.e("version="+ECApplication.version);
		return ECApplication.version==0;

//		return  (user != null && user.getpVersion() == 0);
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
	 * 根据TabFragment Index 查找Fragment
	 *
	 * @param tabIndex
	 * @return
	 */
	public final BaseFragment getTabView(int tabIndex) {
		LogUtil.d(LogUtil.getLogUtilsTag(LauncherActivity.class),
				"get tab index " + tabIndex);
		if (tabIndex < 0) {
			return null;
		}

		if (mTabViewCache.containsKey(Integer.valueOf(tabIndex))) {
			return (BaseFragment) mTabViewCache.get(Integer.valueOf(tabIndex));
		}

		BaseFragment mFragment = null;
		switch (tabIndex) {
		case TAB_CONVERSATION:
			mFragment = (TabFragment) Fragment.instantiate(this,
					ConversationListFragment.class.getName(), null);
			break;
		case TAB_ADDRESS:
			mFragment = (TabFragment) Fragment
					.instantiate(this,
							MobileContactActivity.MobileContactFragment.class
									.getName(), null);
			break;
		case TAB_GROUP:
			
			
			mFragment = (TabFragment) Fragment.instantiate(this,
					GroupListFragment.class.getName(), null);
			break;
		case TAB_DISCUSSION_GROUP:
			
			mFragment = (TabFragment) Fragment.instantiate(this,
					DiscussionListFragment.class.getName(), null);
			break;

		default:
			break;
		}

		if (mFragment != null) {
			mFragment.setActionBarActivity(this);
		}
		mTabViewCache.put(Integer.valueOf(tabIndex), mFragment);
		return mFragment;
	}

	/**
	 * 根据提供的子Fragment index 切换到对应的页面
	 *
	 * @param index
	 *            子Fragment对应的index
	 */
	public void ctrlViewTab(int index) {

		LogUtil.d(LogUtil.getLogUtilsTag(LauncherActivity.class),
				"change tab to " + index + ", cur tab " + mCurrentItemPosition
						+ ", has init tab " + mTabViewInit
						+ ", tab cache size " + mTabViewCache.size());
		if ((!mTabViewInit || index < 0)
				|| (mLauncherViewPagerAdapter != null && index > mLauncherViewPagerAdapter
						.getCount() - 1)) {
			return;
		}

		if (mCurrentItemPosition == index) {
			return;
		}
		mCurrentItemPosition = index;

		if (mLauncherUITabView != null) {
			mLauncherUITabView.doChangeTabViewDisplay(mCurrentItemPosition);
		}

		if (mCustomViewPager != null) {
			mCustomViewPager.setCurrentItem(mCurrentItemPosition, false);
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
	public boolean onMenuOpened(int featureId, Menu menu) {
		controlPlusSubMenu();
		return false;
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
		if (mLauncherUITabView == null) {
			String account = getAutoRegistAccount();
			if (TextUtils.isEmpty(account)) {
				if(RestServerDefines.QR_APK){
					startActivity(new Intent(this, PhoneRouterUI.class));
				}else {
					startActivity(new Intent(this, LoginActivity.class));
				}
				finish();
				return;
			}


			// 注册第一次登陆同步消息
			registerReceiver(new String[] {
					IMChattingHelper.INTENT_ACTION_SYNC_MESSAGE,
					SDKCoreHelper.ACTION_SDK_CONNECT });
			ClientUser user = new ClientUser("").from(account);

		   ClientUser c =	 CCPAppManager.getClientUser();
			if(c!=null){
				user.setpVersion(c.getpVersion());
				ECApplication.version = c.getpVersion();
			}
			CCPAppManager.setClientUser(user);
			if (!ContactSqlManager.hasContact(user.getUserId())) {
				ECContacts contacts = new ECContacts();
				contacts.setClientUser(user);
				ContactSqlManager.insertContact(contacts);
			}
			
			if (SDKCoreHelper.getConnectState() != ECDevice.ECConnectState.CONNECT_SUCCESS
					&& !SDKCoreHelper.isKickOff()) {
				
				ContactsCache.getInstance().load();
				
				if(!TextUtils.isEmpty(getAutoRegistAccount())){
				 SDKCoreHelper.init(this);
				}
			}
			// 初始化主界面Tab资源
			if (!mInit) {
				initLauncherUIView();
			}
		}
		OnUpdateMsgUnreadCounts();
		getTopContacts();
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

	private void controlPlusSubMenu() {
		if (mOverflowHelper == null) {
			return;
		}

		if (mOverflowHelper.isOverflowShowing()) {
			mOverflowHelper.dismiss();
			return;
		}
		
		if(mItems == null) {
			initOverflowItems();
		}
		
		mOverflowHelper.setOverflowItems(mItems);
		mOverflowHelper .setOnOverflowItemClickListener(mOverflowItemCliclListener);
		mOverflowHelper.showAsDropDown(findViewById(R.id.btn_plus));
	}

	@Override
	protected void onPause() {
		LogUtil.d(LogUtil.getLogUtilsTag(getClass()), "KEVIN Launcher onPause");
		super.onPause();
		// 友盟统计API
//		MobclickAgent.onPause(this);
	}

	/**
	 * 返回隐藏到后台
	 */
	public void doTaskToBackEvent() {
		moveTaskToBack(true);

	}

	@Override
	public void OnUpdateMsgUnreadCounts() {
		int unreadCount = IMessageSqlManager.qureyAllSessionUnreadCount();
		int notifyUnreadCount = IMessageSqlManager.getUnNotifyUnreadCount();
		int count = unreadCount;
		if (unreadCount >= notifyUnreadCount) {
			count = unreadCount - notifyUnreadCount;
		}
		if (mLauncherUITabView != null) {
			mLauncherUITabView.updateMainTabUnread(count);
		}
	}

	@Override
	public boolean onLongClick(View v) {
		CCPAppManager.showCodecConfigMenu(this);
		return true;
	}

	/**
	 * TabView 页面适配器
	 *
	 * @author 容联•云通讯
	 * @version 4.0
	 * @date 2014-12-4
	 */
	private class LauncherViewPagerAdapter extends FragmentStatePagerAdapter
			implements ViewPager.OnPageChangeListener,
			CCPLauncherUITabView.OnUITabViewClickListener {
		/**
         *
         */
		private int mClickTabCounts;
		private GroupListFragment mGroupListFragment;
		private DiscussionListFragment mDissListFragment;

		private final ViewPager mViewPager;
		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		final class TabInfo {
			private final String tag;
			private final Class<?> clss;
			private final Bundle args;

			TabInfo(String _tag, Class<?> _class, Bundle _args) {
				tag = _tag;
				clss = _class;
				args = _args;
			}
		}

		public LauncherViewPagerAdapter(FragmentActivity fm, ViewPager pager) {
			super(fm.getSupportFragmentManager());
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(String tabSpec, Class<?> clss, Bundle args) {
			String tag = tabSpec;

			TabInfo info = new TabInfo(tag, clss, args);
			mTabs.add(info);
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public Fragment getItem(int position) {
			return mLauncherUI.getTabView(position);
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class),
					"onPageScrollStateChanged state = " + state);

			if (state != ViewPager.SCROLL_STATE_IDLE
					|| mGroupListFragment == null) {
				return;
			}
			if (mGroupListFragment != null) {
				mGroupListFragment.onGroupFragmentVisible(true);
				mGroupListFragment = null;
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class),
					"onPageScrolled " + position + " " + positionOffset + " "
							+ positionOffsetPixels);
			if (mLauncherUITabView != null) {
				mLauncherUITabView.doTranslateImageMatrix(position,
						positionOffset);
			}
			if (positionOffset != 0.0F&&position==CCPLauncherUITabView.TAB_VIEW_THIRD) {
				if (mGroupListFragment == null) {
					mGroupListFragment = (GroupListFragment) getTabView(CCPLauncherUITabView.TAB_VIEW_THIRD);
					mGroupListFragment.onGroupFragmentVisible(true);
				}
			}
			if (positionOffset != 0.0F&&position==CCPLauncherUITabView.TAB_VIEW_FOUR) {
				if (mDissListFragment == null) {
					mDissListFragment = (DiscussionListFragment) getTabView(CCPLauncherUITabView.TAB_VIEW_FOUR);
					mDissListFragment.onDisGroupFragmentVisible(true);
				}
			}
		
			
			
			
			
		}

		@Override
		public void onPageSelected(int position) {
			LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class),
					"onPageSelected");
			if (mLauncherUITabView != null) {
				mLauncherUITabView.doChangeTabViewDisplay(position);
				mCurrentItemPosition = position;
			}
		}

		@Override
		public void onTabClick(int tabIndex) {
			if (tabIndex == mCurrentItemPosition) {
				LogUtil.d(
						LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class),
						"on click same index " + tabIndex);
				// Perform a rolling
				TabFragment item = (TabFragment) getItem(tabIndex);
				item.onTabFragmentClick();
				return;
			}

			mClickTabCounts += mClickTabCounts;
			LogUtil.d(LogUtil.getLogUtilsTag(LauncherViewPagerAdapter.class),
					"onUITabView Click count " + mClickTabCounts);
			mViewPager.setCurrentItem(tabIndex);
		}

	}

	/**
	 * 网络注册状态改变
	 *
	 * @param connect
	 */
	public void onNetWorkNotify(ECDevice.ECConnectState connect) {
		BaseFragment tabView = getTabView(TAB_CONVERSATION);
		if (tabView instanceof ConversationListFragment && tabView.isAdded()) {
			((ConversationListFragment) tabView).updateConnectState();
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_plus) {
			controlPlusSubMenu();
		}
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

	private final AdapterView.OnItemClickListener mOverflowItemCliclListener = new AdapterView.OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			controlPlusSubMenu();
			
			OverflowItem overflowItem= mItems[position];
			String title=overflowItem.getTitle();
			
			if (getString(R.string.main_plus_inter_phone).equals(title)) {
				// 实时对讲

			} else if (getString(R.string.main_plus_meeting_voice).equals(title)) {
			} else if (getString(R.string.main_plus_groupchat).equals(title)) {
				// 创建群组
				startActivity(new Intent(LauncherActivity.this, CreateGroupActivity.class));
			} else if (getString(R.string.main_plus_querygroup).equals(title)) {
				// 群组搜索
				startActivity(new Intent(LauncherActivity.this,BaseSearch.class));
			} else if (getString(R.string.main_plus_mcmessage).equals(title)) {
				handleStartServiceEvent();
				
			} else if (getString(R.string.main_plus_settings).equals(title)) {
				// 设置;
				startActivity(new Intent(LauncherActivity.this,SettingsActivity.class));
				
				
			} else if (getString(R.string.main_plus_meeting_video).equals(title)) {

			}else if(getString(R.string.create_discussion).equals(title)){
				
				Intent intent=new Intent(LauncherActivity.this, MobileContactSelectActivity.class);
				intent.putExtra("is_discussion", true);
				intent.putExtra("isFromCreateDiscussion", true);
				intent.putExtra("group_select_need_result", true);
				startActivity(intent);
				
				
			}else if(getString(R.string.query_discussion).equals(title)){
				
				Intent intent=new Intent(LauncherActivity.this, ECDiscussionActivity.class);
				intent.putExtra("is_discussion", true);
				
				startActivity(intent);
				
			}else if(getString(R.string.main_plus_money).equals(title)){

			}else if(getString(R.string.main_plus_sharemeeting).equals(title)){





			}else if(getString(R.string.main_plus_live).equalsIgnoreCase(title)){

			}
		}

	};

	/**
	 * 在线客服
	 */
	private void handleStartServiceEvent() {
		showProcessDialog();
		CustomerServiceHelper.startService(ContactLogic.CUSTOM_SERVICE,
				new CustomerServiceHelper.OnStartCustomerServiceListener() {
					@Override
					public void onError(ECError error) {
						dismissPostingDialog();
					}

					@Override
					public void onServiceStart(String event) {
						dismissPostingDialog();
						CCPAppManager.startCustomerServiceAction(
								LauncherActivity.this, event);
					}
				});
	}

	private InternalReceiver internalReceiver;

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
				// tetstMesge();
				BaseFragment tabView = getTabView(TAB_CONVERSATION);
				if (tabView != null
						&& tabView instanceof ConversationListFragment) {
					((ConversationListFragment) tabView).updateConnectState();
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
			settingPersonInfo();
			IMChattingHelper.getInstance().getPersonInfo();
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
}
