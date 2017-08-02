package com.henry.ecdemo.ui;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

import com.henry.ecdemo.common.AudioManagerTools;
import com.henry.ecdemo.common.utils.LogUtil;

public abstract class BaseFragment extends CCPFragment {

    /**当前CCPFragment所承载的FragmentActivity实例*/
    private FragmentActivity mActionBarActivity;
    private AudioManager mAudioManager;

    /**AudioManager.STREAM_MUSIC类型的音量最大值*/
    private int mMusicMaxVolume;

    /**
     * 设置ActionBarActivity实例
     * @param activity
     */
    public void setActionBarActivity(FragmentActivity activity) {
        this.mActionBarActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioManager = AudioManagerTools.getInstance().getAudioManager();
        mMusicMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    /**
     * 自定义页面方法,处理上下音量键按下事件
     * @param keyCode
     * @param event
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP)
                && mAudioManager != null) {
            int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (streamVolume >= mMusicMaxVolume) {
                LogUtil.d(LogUtil.getLogUtilsTag(BaseFragment.class),
                        "has set the max volume");
                return true;
            }
            int mean = mMusicMaxVolume / 7;
            if (mean == 0) {
                mean = 1;
            }
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    streamVolume + mean, AudioManager.FLAG_PLAY_SOUND
                            | AudioManager.FLAG_SHOW_UI);
            return true;
        }
        if ((event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
                && mAudioManager != null) {
            int streamVolume = mAudioManager .getStreamVolume(AudioManager.STREAM_MUSIC);
            int mean = mMusicMaxVolume / 7;
            if (mean == 0) {
                mean = 1;
            }
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    streamVolume - mean, AudioManager.FLAG_PLAY_SOUND
                            | AudioManager.FLAG_SHOW_UI);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}

