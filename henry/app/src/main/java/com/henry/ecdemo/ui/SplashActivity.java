package com.henry.ecdemo.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.henry.ecdemo.R;

/**
 * Created by Administrator on 2017/7/29.
 */

public class SplashActivity extends Activity {

    private ImageView imageView_lv2,imageView_lv,imageView_hong,imageView_hong2,imageView_huang,imageView_cheng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initAnimation();
    }

    private void initView() {
        setContentView(R.layout.activity_splash);
        imageView_lv2 = (ImageView)findViewById(R.id.lv2);
        imageView_lv = (ImageView)findViewById(R.id.lv);
        imageView_hong = (ImageView)findViewById(R.id.hong);
        imageView_hong2 = (ImageView)findViewById(R.id.hong2);
        imageView_huang = (ImageView)findViewById(R.id.huang);
        imageView_cheng = (ImageView)findViewById(R.id.cheng);
    }

    private void initAnimation() {
        DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();
        AnimationSet set = new AnimationSet(false);
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

        imageView_lv2.startAnimation(set);
    }



}
