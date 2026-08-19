package com.example.todaytoeat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.todaytoeat.R;
import com.google.android.material.card.MaterialCardView;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.list_item, viewGroup, false);

            holder = new ViewHolder();
            holder.tv_shop = view.findViewById(R.id.tv_shop);
            holder.cardView = view.findViewById(R.id.card_root);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // 设置店铺名称
        String shopNameAndStatus = mShopList.get(i);
        holder.tv_shop.setText(shopNameAndStatus);

        // 按压效果：点击/长按时卡片缩小，随后恢复
        ViewHolder finalHolder = holder;

        // 短按按压效果
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialCardView cardView = finalHolder.cardView;
                cardView.animate()
                        .scaleX(0.97f)
                        .scaleY(0.97f)
                        .setDuration(80)
                        .start();

                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cardView.animate()
                                .scaleX(1.00f)
                                .scaleY(1.00f)
                                .setDuration(80)
                                .start();
                    }
                }, 200);
            }
        });

        // 长按按压效果
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                MaterialCardView cardView = finalHolder.cardView;
                cardView.animate()
                        .scaleX(0.97f)
                        .scaleY(0.97f)
                        .setDuration(80)
                        .start();

                return false;
            }
        });
        return view;
    }

    public static class ViewHolder {
        public TextView tv_shop;
        public MaterialCardView cardView;
    }
}
