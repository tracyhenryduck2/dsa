package com.henry.ecdemo.ui;

import android.view.View;

public class CCPFragmentImpl extends CCPActivityBase {
    final private CCPFragment mFragment;

    public CCPFragmentImpl(CCPFragment fragment) {
        mFragment  = fragment;
    }

    @Override
    protected void onInit() {
        mFragment.onFragmentInit();
    }

    @Override
    protected int getLayoutId() {
        return mFragment.getLayoutId();
    }

    @Override
    protected View getContentLayoutView() {
        return null;
    }

    @Override
    protected String getClassName() {
        return mFragment.getClass().getName();
    }

    @Override
    protected void dealContentView(View contentView) {
        mFragment.onBaseContentViewAttach(contentView);
    }

    @Override
    public int getTitleLayout() {
        return mFragment.getTitleLayoutId();
    }

}
