package com.henry.ecdemo.common;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.henry.ecdemo.R;
import com.henry.ecdemo.common.dialog.ECListDialog;
import com.henry.ecdemo.common.utils.ECPreferenceSettings;
import com.henry.ecdemo.common.utils.ECPreferences;
import com.henry.ecdemo.common.utils.LogUtil;
import com.henry.ecdemo.common.utils.MimeTypesTools;
import com.henry.ecdemo.core.ClientUser;
import com.henry.ecdemo.ui.ECSuperActivity;
import com.henry.ecdemo.ui.LocationInfo;
import com.henry.ecdemo.ui.ShowBaiDuMapActivity;
import com.henry.ecdemo.ui.chatting.ChattingActivity;
import com.henry.ecdemo.ui.chatting.ChattingFragment;
import com.henry.ecdemo.ui.chatting.ImageGalleryActivity;
import com.henry.ecdemo.ui.chatting.ImageGralleryPagerActivity;
import com.henry.ecdemo.ui.chatting.ImageMsgInfoEntry;
import com.henry.ecdemo.ui.chatting.ViewImageInfo;
import com.henry.ecdemo.ui.chatting.view.ChatFooterPanel;
import com.yuntongxun.ecsdk.ECMessage;
import com.yuntongxun.ecsdk.im.ECLocationMessageBody;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 存储SDK一些全局性的常量
 */
public class CCPAppManager {

    public static Md5FileNameGenerator md5FileNameGenerator = new Md5FileNameGenerator();
    /**Android 应用上下文*/
    private static Context mContext = null;
    /**包名*/
    public static String pkgName = "com.henry.ecdemo";
    /**SharedPreferences 存储名字前缀*/
    public static final String PREFIX = "com.henry.ecdemo_";
    public static final int FLAG_RECEIVER_REGISTERED_ONLY_BEFORE_BOOT = 0x10000000;
    /**IM功能UserData字段默认文字*/
    public static final String USER_DATA = "yuntongxun.ecdemo";
    public static HashMap<String, Integer> mPhotoCache = new HashMap<String, Integer>();
    public static ArrayList<ECSuperActivity> activities = new ArrayList<ECSuperActivity>();

    public static HashMap<String,String> map = new HashMap<String, String>();

    
    /**IM聊天更多功能面板*/
    private static ChatFooterPanel mChatFooterPanel;
    public static String getPackageName() {
        return pkgName;
    }
    public static ClientUser mClientUser;


    public static void put(String key,String v){
        map.put(key, v);
    }

    public static String get(String k){
        return map.get(k);
    }
    /**
     * 返回SharePreference配置文件名称
     * @return
     */
    public static String getSharePreferenceName() {
        return pkgName + "_preferences";
    }

    public static SharedPreferences getSharePreference(){
        if (mContext != null) {
            return mContext.getSharedPreferences(getSharePreferenceName(), 0);
        }
        return null;
    }

    public static String pwd  ="";

    /**
     * 返回上下文对象
     * @return
     */
    public static Context getContext(){
        return mContext;
    }
    
    public static void sendRemoveMemberBR(){
    	
    	getContext().sendBroadcast(new Intent("com.henry.ecdemo.removemember"));
    }

    /**
     * 设置上下文对象
     * @param context
     */
    public static void setContext(Context context) {
        mContext = context;
        pkgName = context.getPackageName();
        LogUtil.d(LogUtil.getLogUtilsTag(CCPAppManager.class),
                "setup application context for package: " + pkgName);
    }

    public static ChatFooterPanel getChatFooterPanel(Context context) {
        return mChatFooterPanel;
    }

    /**
     * 缓存账号注册信息
     * @param user
     */
    public static void setClientUser(ClientUser user) {
        mClientUser = user;
    }

    public static void setPversion(int version){
        if(mClientUser != null) {
            mClientUser.setpVersion(version);
        }
    }

    /**
     * 保存注册账号信息
     * @return 客户登录信息
     */
    public static ClientUser getClientUser(){
        if(mClientUser != null) {
            return mClientUser;
        }
        String registerAccount = getAutoRegisterAccount();
        if(!TextUtils.isEmpty(registerAccount)) {
            mClientUser = new ClientUser("");
            return mClientUser.from(registerAccount);
        }
        return null;
    }

    public static String getUserId(){
        return getClientUser().getUserId();
    }

    private static String getAutoRegisterAccount() {
        SharedPreferences sharedPreferences = ECPreferences.getSharedPreferences();
        ECPreferenceSettings registerAuto = ECPreferenceSettings.SETTINGS_REGIST_AUTO;
        String registerAccount = sharedPreferences.getString(registerAuto.getId(), (String) registerAuto.getDefaultValue());
        return registerAccount;
    }

    /**
     * @param context
     * @param path
     */
    public static void doViewFilePrevieIntent(Context context ,String path) {
        try {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(android.content.Intent.ACTION_VIEW);
            String type = MimeTypesTools.getMimeType(context, path);
            File file = new File(path);
            intent.setDataAndType(Uri.fromFile(file), type);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(LogUtil.getLogUtilsTag(CCPAppManager.class), "do view file error " + e.getLocalizedMessage());
        }
    }

    /**
     *
     * @param cotnext
     * @param value
     */
    public static void startChattingImageViewAction(Context cotnext, ImageMsgInfoEntry value) {
        Intent intent = new Intent(cotnext, ImageGalleryActivity.class);
        intent.putExtra(ImageGalleryActivity.CHATTING_MESSAGE, value);
        cotnext.startActivity(intent);
    }

    /**
     * 批量查看图片
     * @param ctx
     * @param position
     * @param session
     */
    public static void startChattingImageViewAction(Context ctx , int position , ArrayList<ViewImageInfo> session,String msgId) {
        Intent intent = new Intent(ctx, ImageGralleryPagerActivity.class);
        intent.putExtra(ImageGralleryPagerActivity.EXTRA_IMAGE_INDEX, position);
        intent.putParcelableArrayListExtra(ImageGralleryPagerActivity.EXTRA_IMAGE_URLS, session);
        intent.putExtra(ImageGralleryPagerActivity.EXTRA_IMAGE_URLS_ID, msgId);
        ctx.startActivity(intent);
    }


    /**
     * 批量查看本地的图片
     * @param ctx
     * @param position
     * @param session
     */
    public static void startAvatarImageViewAction(Context ctx , int position , ArrayList<ViewImageInfo> session,String msgId) {
        Intent intent = new Intent(ctx, ImageGralleryPagerActivity.class);
        intent.putExtra(ImageGralleryPagerActivity.EXTRA_IMAGE_INDEX, position);
        intent.putParcelableArrayListExtra(ImageGralleryPagerActivity.EXTRA_IMAGE_URLS, session);
        intent.putExtra(ImageGralleryPagerActivity.EXTRA_IMAGE_URLS_ID, msgId);
        intent.putExtra(ImageGralleryPagerActivity.INNER_RESOURCE,true);
        ctx.startActivity(intent);
    }


    /**
     * 获取应用程序版本名称
     * @return
     */
    public static String getVersion() {
        String version = "0.0.0";
        if(mContext == null) {
            return version;
        }
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }

    /**
     * 获取应用版本号
     * @return 版本号
     */
    public static int getVersionCode() {
        int code = 1;
        if(mContext == null) {
            return code;
        }
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(
                    getPackageName(), 0);
            code = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return code;
    }


    public static void addActivity(ECSuperActivity activity) {
        activities.add(activity);
    }

    public static void clearActivity() {
        for(ECSuperActivity activity : activities) {
            if(activity != null) {
                activity.finish();
                activity = null;
            }
            activities.clear();
        }
    }
    

    /**
     * 打开浏览器下载新版本
     * @param context
     */
    public static void startUpdater(Context context) {
        Uri uri = Uri.parse("http://dwz.cn/F8Amj");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    public static HashMap<String, Object> prefValues = new HashMap<String, Object>();

    /**
     *
     * @param key
     * @param value
     */
    public static void putPref(String key , Object value) {
        prefValues.put(key, value);
    }

    public static Object getPref(String key) {
        return prefValues.remove(key);
    }

    public static void removePref(String key) {
        prefValues.remove(key);
    }

    /**
     * 开启在线客服
     * @param context
     * @param contactid
     */
    public static void startCustomerServiceAction(Context context , String contactid) {
        Intent intent = new Intent(context, ChattingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ChattingFragment.RECIPIENTS, contactid);
        intent.putExtra(ChattingFragment.CONTACT_USER, "在线客服");
        intent.putExtra(ChattingActivity.CONNECTIVITY_SERVICE, true);
        context.startActivity(intent);
    }

    /**
     * 聊天界面
     * @param context
     * @param contactid
     * @param username
     */
    public static void startChattingAction(Context context , String contactid , String username) {
        startChattingAction(context, contactid, username, false);
    }
    public static void startChattingAction2(Context context , String contactid , String username) {
        startChattingAction2(context, contactid, username, false);
    }

    /**
     *
     * @param context
     * @param contactid
     * @param username
     * @param clearTop
     */
    public static void startChattingAction(Context context , String contactid , String username , boolean clearTop) {
        Intent intent = new Intent(context, ChattingActivity.class);
        if(clearTop) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        intent.putExtra(ChattingFragment.RECIPIENTS, contactid);
        intent.putExtra(ChattingFragment.CONTACT_USER, username);
        intent.putExtra(ChattingFragment.CUSTOMER_SERVICE, false);
        context.startActivity(intent);
    }
    public static void startChattingAction2(Context context , String contactid , String username , boolean clearTop) {
        Intent intent = new Intent(context, ChattingActivity.class);
        if(clearTop) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        intent.putExtra(ChattingFragment.RECIPIENTS, contactid);
        intent.putExtra(ChattingFragment.CONTACT_USER, username);
        intent.putExtra(ChattingFragment.CUSTOMER_SERVICE, false);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        context.startActivityForResult( intent,2);

        context.startActivity(intent);

        EventBus.getDefault().post("111");
    }

    /**
     * VoIP呼叫
     * @param nickname 昵称
     * @param contactId 呼出号码
     */
    public static void callVoIPAction(Context ctx , String nickname, String contactId){
        // VoIP呼叫
    }





    /**
     * 多选呼叫菜单
     * @param ctx 上下文
     * @param nickname  昵称
     * @param contactId 号码
     */
    public static void showCallMenu(final Context ctx , final String nickname, final String contactId) {

    }

    static List<Integer> mChecks;

    /**
     * 提示选择呼叫编码设置
     * @param ctx 上下文
     */
    public static void showCodecConfigMenu(final Context ctx) {
        final ECListDialog multiDialog = new ECListDialog(ctx , R.array.Codec_call);


        multiDialog.setOnDialogItemClickListener(false, new ECListDialog.OnDialogItemClickListener() {
            @Override
            public void onDialogItemClick(Dialog d, int position) {
                LogUtil.d("onDialogItemClick", "position " + position);
            }
        });
        if(mChecks == null) {
            mChecks = new ArrayList<Integer>();
        }
        multiDialog.setChecks(mChecks);
        multiDialog.setTitle(R.string.ec_talk_codec_select);
        multiDialog.setButton(ECListDialog.BUTTON_POSITIVE, R.string.dialog_ok_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mChecks = multiDialog.getCheck();
            }
        });
        multiDialog.show();
    }


	public static void startShowBaiDuMapAction(ChattingActivity mContext2,
			ECMessage iMessage) {

			if(iMessage==null||mContext2==null){
				return;
			}
			
		 Intent intent=new Intent(mContext2,ShowBaiDuMapActivity.class);
       	 
		 ECLocationMessageBody body=(ECLocationMessageBody) iMessage.getBody();
       	 LocationInfo locationInfo =new LocationInfo();
       	 locationInfo.setLat(body.getLatitude());
       	 locationInfo.setLon(body.getLongitude());
       	 locationInfo.setAddress(body.getTitle());
       	 intent.putExtra("location", locationInfo);
			
		mContext2.startActivity(intent);
			
	}
}
