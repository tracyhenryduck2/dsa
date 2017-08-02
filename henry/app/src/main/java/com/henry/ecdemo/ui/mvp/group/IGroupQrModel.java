package com.henry.ecdemo.ui.mvp.group;

import android.graphics.Bitmap;

import com.google.zxing.WriterException;

public interface IGroupQrModel{


    Bitmap getGroupQrBitmap(String content) throws WriterException;



}
