package com.henry.ecdemo.ui.mvp.group;

import android.graphics.Bitmap;

import com.google.zxing.WriterException;
import com.google.zxing.client.android.encode.QRCodeEncoder;

public class GroupQrModelImpl implements IGroupQrModel{


    @Override
    public Bitmap getGroupQrBitmap(String content)throws WriterException{

        return QRCodeEncoder.encodeAsBitmap(content, 400);
    }
}
