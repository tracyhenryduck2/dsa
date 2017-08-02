
package com.henry.ecdemo.ui.chatting;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.henry.ecdemo.R;
import com.henry.ecdemo.common.utils.CoreHandler;
import com.henry.ecdemo.common.utils.DemoUtils;
import com.henry.ecdemo.common.utils.ECPreferenceSettings;
import com.henry.ecdemo.common.utils.ECPreferences;
import com.henry.ecdemo.common.view.TopBarView;
import com.henry.ecdemo.photoview.PhotoView;
import com.henry.ecdemo.ui.ECSuperActivity;
import com.yuntongxun.ecsdk.platformtools.ECHandlerHelper;

import java.io.InvalidClassException;


/**
 * 图片预览
 */
public class ImagePreviewActivity extends ECSuperActivity implements View.OnClickListener{

    private static final String TAG = "ECDemo.ImagePreviewActivity";
    private TopBarView mTopBarView;
    private PhotoView mImageView;
    private Bitmap bitmap;

    private CoreHandler mCoreHandler = new CoreHandler(new CoreHandler.HandlerCallbck() {
        @Override
        public boolean dispatchMessage() {
            mImageView.setImageBitmap(bitmap);
            mImageView.invalidate();
            mTopBarView.setRightBtnEnable(true);
            return false;
        }
    }, false);

    @Override
    protected int getLayoutId() {
        return R.layout.image_preview_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTopBarView = getTopBarView();
        mTopBarView.setTopBarToStatus(1, R.drawable.topbar_back_bt,
                R.drawable.btn_style_green, null,
                getString(R.string.dialog_ok_button),
                getString(R.string.app_title_image_preview), null, this);
        mTopBarView.setRightBtnEnable(false);

        initViewUI();
    }

    private void initViewUI() {
        final String path = ECPreferences.getSharedPreferences().
                getString(ECPreferenceSettings.SETTINGS_CROPIMAGE_OUTPUTPATH.getId(),
                        (String) ECPreferenceSettings.SETTINGS_CROPIMAGE_OUTPUTPATH.getDefaultValue());
        if(TextUtils.isEmpty(path)) {
            finish();
            return ;
        }
        mImageView = (PhotoView) findViewById(R.id.image);
        ECHandlerHelper handlerHelper = new ECHandlerHelper();
        handlerHelper.postRunnOnThead(new Runnable() {

            @Override
            public void run() {
                bitmap = DemoUtils.getSuitableBitmap(path);
                mCoreHandler.sendEmptyMessageDelayed(200);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_left:
                hideSoftKeyboard();
                finish();
                break;
            case R.id.text_right:
                try {
                    ECPreferences.savePreference(ECPreferenceSettings.SETTINGS_PREVIEW_SELECTED, Boolean.TRUE, true);
                    setResult(RESULT_OK);
                } catch (InvalidClassException e) {
                    e.printStackTrace();
                }
                finish();
                break;
            default:
                break;
        }
    }

}
