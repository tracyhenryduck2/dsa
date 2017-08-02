package com.henry.ecdemo.ui;

import android.view.View;

public class CCPActivityImpl extends CCPActivityBase {

    final private ECSuperActivity mActivity;

    public CCPActivityImpl(ECSuperActivity activity) {
        mActivity  = activity;
    }

    @Override
    protected void onInit() {
        mActivity.onActivityInit();
    }

    @Override
    protected int getLayoutId() {
        return mActivity.getLayoutId();
    }

    public int getTitleLayout() {
        return mActivity.getTitleLayout();
    }

    @Override
    protected View getContentLayoutView() {
        return null;
    }

    @Override
    protected String getClassName() {
        return mActivity.getClass().getName();
    }

    @Override
    protected void dealContentView(View contentView) {
        mActivity.onBaseContentViewAttach(contentView);
    }
}
