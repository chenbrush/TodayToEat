package com.example.todaytoeat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.todaytoeat.R;

import java.util.List;

public class ListAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<String> mShopList; // 店铺列表

    // 构造方法：传入上下文 + 店铺数据
    public ListAdapter(Context mContext, List<String> mShopList) {
        this.mContext = mContext;
        this.mShopList = mShopList;
    }

    @Override
    public int getCount() {
        return mShopList.size();
    }

    @Override
    public Object getItem(int i) {
        return mShopList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            // 加载条目布局
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item, viewGroup, false);

            holder = new ViewHolder();
            holder.tv_shop = view.findViewById(R.id.tv_shop);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // 获取当前条目的店铺名称
        String shopName = mShopList.get(i);

        // 设置到 TextView
        holder.tv_shop.setText(shopName);

        return view;
    }

    public static class ViewHolder {
        public TextView tv_shop;
    }
}