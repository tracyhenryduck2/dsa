
package com.henry.ecdemo.common.utils;

public enum ECPreferenceSettings {

    /**
     * Whether is the first use of the application
     *
     */
    SETTINGS_FIRST_USE("com.henry.ecdemo_first_use" , Boolean.TRUE),
    /**坚持云通讯登陆账号*/
    SETTINGS_YUNTONGXUN_ACCOUNT("com.henry.ecdemo_yun_account" , ""),
    /**检查是否需要自动登录*/
    SETTINGS_REGIST_AUTO("com.henry.ecdemo_account" , ""),
    /**是否使用回车键发送消息*/
    SETTINGS_ENABLE_ENTER_KEY("com.henry.ecdemo_sendmessage_by_enterkey" , Boolean.TRUE),
    /**聊天键盘的高度*/
    SETTINGS_KEYBORD_HEIGHT("com.henry.ecdemo_keybord_height" , 0),
    /**新消息声音*/
    SETTINGS_NEW_MSG_SOUND("com.henry.ecdemo_new_msg_sound" , true),
    /**新消息震动*/
    SETTINGS_NEW_MSG_SHAKE("com.henry.ecdemo_new_msg_shake" , true),
    SETTING_CHATTING_CONTACTID("com.henry.ecdemo_chatting_contactid" , ""),
    /**图片缓存路径*/
    SETTINGS_CROPIMAGE_OUTPUTPATH("com.henry.ecdemo_CropImage_OutputPath" , ""),



//   SETTINGS_APPKEY("com.henry.ecdemo_appkey" , "8a2af988536458c301537d7197320004"),//xiaomi
//   SETTINGS_TOKEN("com.henry.ecdemo_token" , "0f26f16e4a8d4680a586c6eb2a9f4e03"),

//   SETTINGS_APPKEY("com.henry.ecdemo_appkey" , "0000000056f9cb300157046a9f83000f"),//guangdian
//   SETTINGS_TOKEN("com.henry.ecdemo_token" , "4314c1a15687416d0365f8f813158c84"),

//   SETTINGS_APPKEY("com.henry.ecdemo_appkey", "2c90d08f50eb633c0150eb6dbe640001"),
//   SETTINGS_TOKEN("com.henry.ecdemo_token" , "2d302f6ea4f6da5c61621510949e23dd"),

//    SETTINGS_APPKEY("com.henry.ecdemo_appkey" , "ff8081814794589e01479523f2220000"),
//    SETTINGS_TOKEN("com.henry.ecdemo_token" , "f1b605d24a4111e58ecf0050568e62f2"),

    SETTINGS_APPKEY("com.henry.ecdemo_appkey" , "8a216da85d7dbf78015d82f7f0330265"),
    SETTINGS_TOKEN("com.henry.ecdemo_token" , "725f58a88c03579b105ddb23b798d667"),


//    SETTINGS_APPKEY("com.henry.ecdemo_appkey" , "ff8080814e760cd0014e760e70d00000"),//live
//    SETTINGS_TOKEN("com.henry.ecdemo_token" , "0d1c9a812593f292ae7324af77e610f5"),

//    SETTINGS_APPKEY("com.henry.ecdemo_appkey" , "ff8081813d0dcad6013d0f2cd56f0000"),
//   SETTINGS_TOKEN("com.henry.ecdemo_token" , "178e5bb672884cbf81ded69606aae841"),//new meeting

//   SETTINGS_APPKEY("com.henry.ecdemo_appkey" , "00000000598745e201598792f7c60005"),//qr
//   SETTINGS_TOKEN("com.henry.ecdemo_token" , "78ee14257c846fb608269f2aba494c99"),

//   SETTINGS_APPKEY("com.henry.ecdemo_appkey" , "00000000414f610f01414f971be10005"),
//   SETTINGS_TOKEN("com.henry.ecdemo_token" , "d722664e0dae11e5ac73ac853d9f54f2"),


//   SETTINGS_APPKEY("com.henry.ecdemo_appkey" , "ff8080814e760cd0014e760e70d00000"),
//   SETTINGS_TOKEN("com.henry.ecdemo_token" , "0d1c9a812593f292ae7324af77e610f5"),



    SETTINGS_ABSOLUTELY_EXIT("com.henry.ecdemo_absolutely_exit" , Boolean.FALSE),
    SETTINGS_FULLY_EXIT("com.henry.ecdemo_fully_exit" , Boolean.FALSE),
    SETTINGS_PREVIEW_SELECTED("com.henry.ecdemo_preview_selected" , Boolean.FALSE),
    SETTINGS_OFFLINE_MESSAGE_VERSION("com.henry.ecdemo_offline_version" , 0),
    /**设置是否是匿名聊天*/
    SETTINGS_SHOW_CHATTING_NAME("com.henry.ecdemo_show_chat_name" , false),
    
    SETTINGS_CUSTOM_APPKEY("com.henry.ecdemo_custom_appkey" , ""),
    SETTINGS_CUSTOM_TOKEN("com.henry.ecdemo_custom_token" , ""),
    SETTINGS_SERVER_CUSTOM("com.henry.ecdemo_setserver" , false),
    SETTINGS_NOTICE_CUSTOM("com.henry.ecdemo_notice" , Boolean.FALSE),
    SETTINGS_RATIO_CUSTOM("com.henry.ecdemo_ratio" , ""),
    SETTINGS_AT("com.henry.ecdemo_at_self" , "");


    private final String mId;
    private final Object mDefaultValue;

    /**
     * Constructor of <code>CCPPreferenceSettings</code>.
     * @param id
     *            The unique identifier of the setting
     * @param defaultValue
     *            The default value of the setting
     */
    private ECPreferenceSettings(String id, Object defaultValue) {
        this.mId = id;
        this.mDefaultValue = defaultValue;
    }

    /**
     * Method that returns the unique identifier of the setting.
     * @return the mId
     */
    public String getId() {
        return this.mId;
    }

    /**
     * Method that returns the default value of the setting.
     *
     * @return Object The default value of the setting
     */
    public Object getDefaultValue() {
        return this.mDefaultValue;
    }

    /**
     * Method that returns an instance of {@link com.henry.ecdemo.common.utils.ECPreferenceSettings} from
     * its. unique identifier
     *
     * @param id
     *            The unique identifier
     * @return CCPPreferenceSettings The navigation sort mode
     */
    public static ECPreferenceSettings fromId(String id){
        ECPreferenceSettings[] values = values();
        int cc = values.length;
        for (int i = 0; i < cc; i++) {
            if (values[i].mId == id) {
                return values[i];
            }
        }
        return null;
    }
}
