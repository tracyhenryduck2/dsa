package  com.henry.ecdemo.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;

import com.mob.tools.network.SSLSocketFactoryEx;
import com.henry.ecdemo.common.CCPAppManager;
import com.henry.ecdemo.ui.RestServerDefines;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 网络工具类
 *
 */

public class NetUtils implements Runnable{
    /**
     * 对网络连接状态进行判断
     *
     * @return true, 可用； false， 不可用
     */
    public static boolean isOpenNetwork(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager.getActiveNetworkInfo() != null) {
            return connManager.getActiveNetworkInfo().isAvailable();
        }

        return false;
    }


    private  static  NetUtils sInstance= new NetUtils();

    public  static NetUtils getInstance(){

        return sInstance;
    }


    /**
     * get请求
     *
     * @param urlString
     * @param params
     * @return
     */
    public static String getRequest(String urlString, Map<String, String> params) {
        try {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(urlString);

            if (null != params) {

                urlBuilder.append("?");

                Iterator<Entry<String, String>> iterator = params.entrySet()
                        .iterator();

                while (iterator.hasNext()) {
                    Entry<String, String> param = iterator.next();
                    urlBuilder
                            .append(URLEncoder.encode(param.getKey(), "UTF-8"))
                            .append('=')
                            .append(URLEncoder.encode(param.getValue(), "UTF-8"));
                    if (iterator.hasNext()) {
                        urlBuilder.append('&');
                    }
                }
            }
            // 创建HttpClient对象
            HttpClient client = getNewHttpClient();
            // 发送get请求创建HttpGet对象
            HttpGet getMethod = new HttpGet(urlBuilder.toString());
            HttpResponse response = client.execute(getMethod);
            // 获取状态码
            int res = response.getStatusLine().getStatusCode();
            if (res == 200) {

                StringBuilder builder = new StringBuilder();
                // 获取响应内容
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                for (String s = reader.readLine(); s != null; s = reader
                        .readLine()) {
                    builder.append(s);
                }
                return builder.toString();
            }
        } catch (Exception e) {

        }

        return null;
    }

    /**
     * post请求
     *
     * @param params
     * @return
     */
    public static String postRequest(
                                     StringEntity params) {

        try {
            // 1. 创建HttpClient对象
            HttpClient client = getNewHttpClient();
            // 2. 发get请求创建HttpGet对象
            HttpPost postMethod = new HttpPost(getUrl());
            postMethod.setHeader("Authorization",getAuth());
            postMethod.setHeader("accept","application/json");
            postMethod.setHeader("Content-Type","application/json; charset=utf-8");

            postMethod.setEntity(params);
            HttpResponse response = client.execute(postMethod);
            int statueCode = response.getStatusLine().getStatusCode();
            if (statueCode == 200) {
                System.out.println(statueCode);
                return EntityUtils.toString(response.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }


    public static String getAuth(){
        String s =CCPAppManager.getClientUser().getAppKey()+":"+DateUtil.formatNowDate(new Date());
        return Base64.encode(s.getBytes());
    }
    public static String getUrl(){
        String s = RestServerDefines.SERVER+"/2016-08-15/Application/"+CCPAppManager.getClientUser().getAppKey()+"/IMPlus/MessageReceipt?sig="+getSig();
        return s;
    }
    public static String getSig(){
        String s =CCPAppManager.getClientUser().getAppKey()+CCPAppManager.getClientUser().getAppToken()+DateUtil.formatNowDate(new Date());
        return getMessageDigest(s.getBytes());
    }

    public static String getMessageDigest(byte[] input) {
        char[] source = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(input);
            byte[] digest = mDigest.digest();
            int length = digest.length;
            char[] result = new char[length * 2];
            int j = 0;
            for (byte l : digest) {
                result[(j++)] = source[(l >>> 4 & 0xF)];
                result[(j++)] = source[(l & 0xF)];
            }
            return new String(result);
        } catch (Exception e) {
        }
        return null;
    }


    // 保存时+当时的秒数，
    public static long expires(String second) {
        Long l = Long.valueOf(second);

        return l * 1000L + System.currentTimeMillis();
    }

    private static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory
                    .getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(
                    params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    @Override
    public void run() {
        if(mlist!=null){
            String result = postRequest(mlist);
            if(callBack!=null){
                callBack.onSuccess(result);
            }
        }


    }


    public static void init(StringEntity s,ResultCallBack c){
        mlist =s;
        callBack=c;
    }



    private static StringEntity mlist;
    private static ResultCallBack callBack;

    public static void release(){
        if(mlist!=null){
            mlist =null;
        }
        if(callBack!=null){
            callBack =null;
        }

    }


    public static interface  ResultCallBack{

        void onSuccess(String reslut);

    }






}