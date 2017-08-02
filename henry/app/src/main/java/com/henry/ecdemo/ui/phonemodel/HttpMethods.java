package com.henry.ecdemo.ui.phonemodel;

import android.content.Context;

import com.henry.ecdemo.common.utils.Base64;
import com.henry.ecdemo.ui.RestServerDefines;

import org.json.JSONObject;

import io.reactivex.Observer;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
//import rx.Subscriber;
//import rx.schedulers.Schedulers;

public class HttpMethods{

    private static final int DEFAULT_TIMEOUT = 20;

    private Retrofit retrofit;

    private BaseApiService movieService;

    private static String time;

    //构造方法私有
    private HttpMethods(){
        //手动创建一个OkHttpClient并设置超时时间



    }

    public  String getAuth(){
        String s = RestServerDefines.APPKER+":"+ time;
        return Base64.encode(s.getBytes());
    }

    //在访问HttpMethods时创建单例
        private static final HttpMethods INSTANCE = new HttpMethods();

    //获取单例
    public static HttpMethods getInstance(String sTime){
        time = sTime;
        return INSTANCE;
    }


    public void postSms(final Observer subscriber,String appid, String url, RequestBody map){

    }
    public void register(final Observer subscriber,String appid, String url, RequestBody map){

//
    }
    public void login(final Observer subscriber,String appid, String url, RequestBody map){



    }
    public void update(final Observer subscriber,String appid, String url, RequestBody map){


    }



    public static JSONObject buildSmsBody(String mobilenum,Context c,String time){

        JSONObject map = new JSONObject();


        return map;
    }
    public static JSONObject buildRegister(String mobilenum,Context c,String time,String sms,String pwd){

        JSONObject map = new JSONObject();

        return map;
    }
    public static JSONObject buildLogin(String mobilenum,String pwd){

        JSONObject map = new JSONObject();

        return map;
    }
    public static JSONObject buildNewPwd(String mobilenum,String pwd,String sms){

        JSONObject map = new JSONObject();

        return map;
    }
}