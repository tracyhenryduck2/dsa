package com.henry.ecdemo.ui;

import android.view.KeyEvent;

/**
 * 自定义三个TabView 的Fragment ,将三个TabView共同属性方法
 * 统一处理
 * 三个TabView 滑动页面需要继承该基类，
 */
public abstract class TabFragment extends BaseFragment {

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            return false;
        }

        return super.onKeyDown(keyCode, event);
    }


    @Override
    public void onDestroy() {
        onReleaseTabUI();
        super.onDestroy();
    }

    /**
     * 当前TabFragment被点击
     */
    protected abstract void onTabFragmentClick();

    /**
     * 当前TabFragment被释放
     */
    protected abstract void onReleaseTabUI();

    @Override
    public int getTitleLayoutId() {
        return -1;
    }
}

