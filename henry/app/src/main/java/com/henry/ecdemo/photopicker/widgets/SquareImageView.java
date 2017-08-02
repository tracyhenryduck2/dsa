package com.henry.ecdemo.photopicker.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.henry.ecdemo.photopicker.utils.PhotoUtils;

/**
 * 图片显示
 */
public class SquareImageView extends ImageView {

    Context mContext;
    public String key;
    int mWidth;
    public SquareImageView(Context context) {
        this(context, null);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        int screenWidth = PhotoUtils.getWidthInPx(mContext);
        mWidth = (screenWidth - PhotoUtils.dip2px(mContext, 4))/3;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mWidth);
    }

}
