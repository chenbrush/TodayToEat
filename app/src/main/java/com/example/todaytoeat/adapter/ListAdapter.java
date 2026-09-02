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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todaytoeat.R;
import com.example.todaytoeat.utils.ThemesMangerUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Set;

/**
 * 商铺列表适配器（RecyclerView 版本）
 * 负责展示商铺名称，提供短按/长按按压效果，长按时回调页面弹出操作菜单
 */
public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    // 长按条目回调：由页面（ListActivity）实现，用于弹出屏蔽/解除屏蔽/删除菜单
    public interface OnItemLongClickListener {
        void onItemLongClick(int position, MaterialCardView cardView);
    }

    private final Context mContext;
    private final List<String> mShopList; // 店铺列表
    // 屏蔽商铺时的文字颜色（从主题解析出的实际颜色值）
    private final int blockedTextColor;
    // 默认文字颜色（首次创建视图时记录，解除屏蔽后恢复）
    private int defaultTextColor;
    private OnItemLongClickListener onItemLongClickListener;

    // 构造方法：传入上下文 + 店铺数据
    public ListAdapter(Context mContext, List<String> mShopList) {
        this.mContext = mContext;
        this.mShopList = mShopList;
        // 从当前主题解析被指定屏蔽颜色对应的实际颜色值(colorOutline)
        TypedValue typedValue = new TypedValue();
        mContext.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue, true);
        blockedTextColor = typedValue.data;
    }

    // 设置长按条目回调
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        // 记录新建视图的默认文字颜色，供解除屏蔽后恢复使用
        defaultTextColor = holder.tv_shop.getCurrentTextColor();
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // 设置店铺名称
        String shopNameAndStatus = mShopList.get(position);
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

        // 短按按压效果：卡片缩小后自动恢复
        holder.itemView.setOnClickListener(v -> {
            holder.cardView.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(80)
                    .start();
            v.postDelayed(() -> holder.cardView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .start(), 200);
        });

        // 长按：卡片缩小并回调页面弹出操作菜单（弹窗关闭时由页面恢复大小）
        holder.itemView.setOnLongClickListener(v -> {
            holder.cardView.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(80)
                    .start();
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(position, holder.cardView);
            }
            return true;
        });

        // 应用用户设置的卡片高度（每次绑定都设置，避免复用旧高度的条目）
        ThemesMangerUtils.applyAllCardElevation(mContext, holder.cardView);
    }

    @Override
    public int getItemCount() {
        return mShopList.size();
    }

    /**
     * 条目 ViewHolder：缓存条目内控件引用
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_shop;
        public MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_shop = itemView.findViewById(R.id.tv_shop);
            cardView = itemView.findViewById(R.id.card_root);
        }
    }
}
