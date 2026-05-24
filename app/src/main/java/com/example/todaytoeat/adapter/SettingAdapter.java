package com.example.todaytoeat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.todaytoeat.R;
import com.example.todaytoeat.beans.SettingsBean;

import java.util.ArrayList;
import java.util.List;

public class SettingAdapter extends BaseAdapter {
    private Context mContext;
    private List<SettingsBean> mSettingBeanList = new ArrayList<>();

    public SettingAdapter(Context mContext, List<SettingsBean> mSettingBeanList) {
        this.mContext = mContext;
        this.mSettingBeanList = mSettingBeanList;
    }

    @Override
    public int getCount() {
        return mSettingBeanList.size();
    }

    @Override
    public Object getItem(int i) {
        return mSettingBeanList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.setting_item, null);

            holder = new ViewHolder();
            holder.iv_icon = view.findViewById(R.id.iv_icon);
            holder.tv_list = view.findViewById(R.id.tv_list);
            holder.iv_next = view.findViewById(R.id.iv_next);

            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        SettingsBean settingsBean = mSettingBeanList.get(i);

        holder.tv_list.setText(settingsBean.name);
        holder.iv_icon.setImageResource(settingsBean.icon);
        holder.iv_next.setImageResource(R.drawable.baseline_arrow_forward_ios_black_24);

        return view;
    }

    public static final class ViewHolder{
        public ImageView iv_icon;
        public TextView tv_list;
        public ImageView iv_next;
    }
}
