package com.henry.ecdemo.ui.chatting;

import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;

import java.util.HashMap;

public class SmallVideoHelper {


    private static final String TAG = "SmallVideoHelper";
    private MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

    private HashMap<String,AnimationDrawable> map = new HashMap<String, AnimationDrawable>();



    public AnimationDrawable get(String url){

        return map.get(url);
    }

    private static SmallVideoHelper helper = new SmallVideoHelper();

    public static SmallVideoHelper getInstance(){
        return  helper;
    }


   public void decoder(String url){
      AnimationDrawable d =   get(url);

      if(d!=null){
          return;
      }
       mediaMetadataRetriever.setDataSource(url);

       AnimationDrawable drawable =  new AnimationDrawable();
       for (int i = 0; i < 9*1000*1000; i+=1*1000*1000) {
           Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime(i, MediaMetadataRetriever.OPTION_CLOSEST);
           BitmapDrawable bi =  new BitmapDrawable(bitmap);
           drawable.addFrame(bi,1000);

       }
       map.put(url,drawable);
   }









}
