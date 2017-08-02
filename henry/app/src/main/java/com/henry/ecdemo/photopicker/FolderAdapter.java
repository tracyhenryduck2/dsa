package com.henry.ecdemo.photopicker;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.henry.ecdemo.R;
import com.henry.ecdemo.photopicker.model.PhotoDirectory;
import com.henry.ecdemo.photopicker.utils.PhotoUtils;

/**
 * 图片目录适配器
 */
public class FolderAdapter extends BaseAdapter {

    List<PhotoDirectory> mData;
    Context mContext;
    int mWidth;

    public FolderAdapter(Context context, List<PhotoDirectory> mData) {
        this.mData = mData;
        this.mContext = context;
        mWidth = PhotoUtils.dip2px(context, 90);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.picker_item_floder_layout, null);
            holder.photoIV = (ImageView) convertView.findViewById(R.id.imageview_floder_img);
            holder.folderNameTV = (TextView) convertView.findViewById(R.id.textview_floder_name);
            holder.photoNumTV = (TextView) convertView.findViewById(R.id.textview_photo_num);
            holder.selectIV = (ImageView) convertView.findViewById(R.id.imageview_floder_select);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.selectIV.setVisibility(View.GONE);
        holder.photoIV.setImageResource(R.drawable.picker_ic_photo_loading);
        PhotoDirectory folder = mData.get(position);
        if(folder.isSelected()) {
            holder.selectIV.setVisibility(View.VISIBLE);
        }
        holder.folderNameTV.setText(folder.getName());
        holder.photoNumTV.setText(folder.getPhotos().size() + "张");
        Glide.with(mContext).load(folder.getPhotos().get(0).getPath()).dontAnimate()
		.thumbnail(0.1f).into(holder.photoIV);
        return convertView;
    }

    private class ViewHolder {
        private ImageView photoIV;
        private TextView folderNameTV;
        private TextView photoNumTV;
        private ImageView selectIV;
    }

}
