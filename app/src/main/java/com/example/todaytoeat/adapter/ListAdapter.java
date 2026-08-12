package com.example.todaytoeat.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import java.util.Set;

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

            mContext.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryContainer, typedValue, true);
            holder.hideColor = typedValue.data;

            // 设置默认颜色
            holder.cardView.setCardBackgroundColor(holder.normalColor);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // 设置店铺名称
        String shopNameAndStatus = mShopList.get(i);
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("setting", MODE_PRIVATE);
        Set<String> hideShopsSet = sharedPreferences.getStringSet("HideShops", null);

        // 判断当前商铺是否处于屏蔽状态
        boolean isBlocked = hideShopsSet != null && hideShopsSet.contains(shopNameAndStatus);

        // 每次绑定都强制设置一次背景色：已屏蔽 -> 屏蔽色；未屏蔽 -> 正常色
        // 避免 ListView 复用 item 视图时残留上一次的背景色
        holder.cardView.setCardBackgroundColor(isBlocked ? holder.hideColor : holder.normalColor);

        holder.tv_shop.setText(shopNameAndStatus);

        // Material按压效果：改用 OnTouchListener 实现并返回 false（不消费事件），
        // 否则 item 会拦截触摸事件，导致 ListView 的 onItemClick / onItemLongClick 无法触发
        ViewHolder finalHolder = holder;
        final boolean finalBlocked = isBlocked;
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MaterialCardView cardView = finalHolder.cardView;
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        cardView.setCardBackgroundColor(finalHolder.pressColor);
                        cardView.animate()
                                .scaleX(0.97f)
                                .scaleY(0.97f)
                                .setDuration(80)
                                .start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 抬起后恢复颜色：已屏蔽的商铺恢复为屏蔽色，未屏蔽的恢复为正常色
                        cardView.setCardBackgroundColor(finalBlocked ? finalHolder.hideColor : finalHolder.normalColor);
                        cardView.animate()
                                .scaleX(1.00f)
                                .scaleY(1.00f)
                                .setDuration(80)
                                .start();
                        break;
                }
                // 返回 false，把触摸事件继续交给 ListView 处理
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
        public int hideColor;
    }
}