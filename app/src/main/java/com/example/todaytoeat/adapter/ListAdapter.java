package com.example.todaytoeat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
            // 立即解析 theme 颜色
            TypedValue typedValue = new TypedValue();
            mContext.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
            holder.normalColor = typedValue.data;

            mContext.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant, typedValue, true);
            holder.pressColor = typedValue.data;

            // 设置默认颜色
            holder.cardView.setCardBackgroundColor(holder.normalColor);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // 设置店铺名称
        holder.tv_shop.setText(mShopList.get(i));

        // Material按压效果
        ViewHolder finalHolder = holder;

        // 单次按下后按压效果
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialCardView cardView = finalHolder.cardView;
                cardView.setCardBackgroundColor(finalHolder.pressColor);
                cardView.animate()
                        .scaleX(0.97f)
                        .scaleY(0.97f)
                        .setDuration(80)
                        .start();

                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cardView.setCardBackgroundColor(finalHolder.normalColor);
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
                cardView.setCardBackgroundColor(finalHolder.pressColor);
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
        public int normalColor;
        public int pressColor;
    }
}