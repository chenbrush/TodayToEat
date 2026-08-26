/*
 * Copyright (c) 2026 chenbrush
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.todaytoeat.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.todaytoeat.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Set;

public class ListAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<String> mShopList; // 店铺列表
    // 屏蔽商铺时的文字颜色（从主题解析出的实际颜色值）
    private final int blockedTextColor;
    // 默认文字颜色（首次创建视图时记录，解除屏蔽后恢复）
    private int defaultTextColor;

    // 构造方法：传入上下文 + 店铺数据
    public ListAdapter(Context mContext, List<String> mShopList) {
        this.mContext = mContext;
        this.mShopList = mShopList;
        // 从当前主题解析被指定屏蔽颜色对应的实际颜色值(colorOutline)
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue, true);
        blockedTextColor = typedValue.data;
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
            // 记录新创建视图的默认文字颜色，供解除屏蔽后恢复使用
            defaultTextColor = holder.tv_shop.getCurrentTextColor();
        } else {
            holder = (ViewHolder) view.getTag();
        }

        // 设置店铺名称
        String shopNameAndStatus = mShopList.get(i);
        holder.tv_shop.setText(shopNameAndStatus);

        // 设置屏蔽商铺颜色
        SharedPreferences sp = mContext.getSharedPreferences("setting", MODE_PRIVATE);
        Set<String> hideShopsSet = sp.getStringSet("HideShops", null);
        if (hideShopsSet != null && hideShopsSet.contains(shopNameAndStatus)) {
            // 被屏蔽的商铺显示置灰颜色
            holder.tv_shop.setTextColor(blockedTextColor);
        } else {
            // 未屏蔽（含解除屏蔽后）恢复默认颜色，避免复用旧视图导致颜色残留
            holder.tv_shop.setTextColor(defaultTextColor);
        }

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
