package com.system.intellignetcable.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.system.intellignetcable.R;
import com.uhf.scanlable.UHfData;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by adu on 2018/11/25.
 */

public class ScanAdapter extends BaseAdapter {
    private List<UHfData.InventoryTagMap> list;

    public ScanAdapter(List<UHfData.InventoryTagMap> list) {
        this.list = list;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
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
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_scan_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.nameTv.setText(list.get(position).strEPC);
        viewHolder.confirmTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnBtnClickListener != null && list.size() != 0){
                    mOnBtnClickListener.onClick(list.get(position).strEPC);
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.name_tv)
        TextView nameTv;
        @BindView(R.id.confirm_tv)
        Button confirmTv;
        @BindView(R.id.status_tv)
        TextView statusTv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public void setData(List<UHfData.InventoryTagMap> data) {
        this.list = data;
        notifyDataSetChanged();
    }

    public void clearData() {
        list.clear();
        notifyDataSetChanged();
    }

    private onBtnClickListener mOnBtnClickListener;

    public void setOnBtnClickListener(onBtnClickListener mOnBtnClickListener) {
        this.mOnBtnClickListener = mOnBtnClickListener;
    }

    public interface onBtnClickListener {
        void onClick(String epcId);
    }
}
