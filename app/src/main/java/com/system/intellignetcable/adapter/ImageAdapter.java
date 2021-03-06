package com.system.intellignetcable.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.system.intellignetcable.R;
import com.system.intellignetcable.bean.PicStringBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zydu on 2018/11/30.
 */

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private List<PicStringBean> list;
    private LayoutInflater inflater;
    private boolean isEdit;

    public ImageAdapter(Context context, List<PicStringBean> list, boolean isEdit) {
        this.context = context;
        this.list = list;
        this.inflater = LayoutInflater.from(context);
        this.isEdit = isEdit;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        convertView = inflater.inflate(R.layout.item_imageview, parent, false);
        viewHolder = new ViewHolder(convertView);
        convertView.setTag(viewHolder);
        if (isEdit) {
            if (position == list.size() - 1) {
                viewHolder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.add_photo));
                viewHolder.deleteIv.setVisibility(View.GONE);
                viewHolder.image.setEnabled(true);
                viewHolder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAddOrDeleteImageClickListener.addImageClick();
                    }
                });
            } else {
                Glide.with(context).load(list.get(position).getPath()).into(viewHolder.image);
                viewHolder.image.setEnabled(false);
                viewHolder.deleteIv.setVisibility(View.VISIBLE);
                viewHolder.deleteIv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAddOrDeleteImageClickListener.deleteImageClick(position);
                    }
                });
            }
        } else {
            viewHolder.deleteIv.setVisibility(View.GONE);
            Glide.with(context).load(list.get(position).getPath()).into(viewHolder.image);
            viewHolder.image.setEnabled(true);
            viewHolder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(onAddOrDeleteImageClickListener != null){
                        onAddOrDeleteImageClickListener.onClick(position);
                    }
                }
            });
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.delete_iv)
        ImageView deleteIv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    private OnAddOrDeleteImageClickListener onAddOrDeleteImageClickListener;

    public void setOnDeleteImageClickListener(OnAddOrDeleteImageClickListener onAddOrDeleteImageClickListener) {
        this.onAddOrDeleteImageClickListener = onAddOrDeleteImageClickListener;
    }

    public interface OnAddOrDeleteImageClickListener {
        void deleteImageClick(int pos);

        void addImageClick();

        void onClick(int position);
    }

}
