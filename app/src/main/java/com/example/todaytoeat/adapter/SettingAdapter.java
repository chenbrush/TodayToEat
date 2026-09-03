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
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todaytoeat.R;
import com.example.todaytoeat.beans.SettingsBean;
import com.example.todaytoeat.utils.PreferenceKeys;
import com.example.todaytoeat.utils.ThemesMangerUtils;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置项列表适配器（RecyclerView 版本）
 * 负责展示设置项并回调页面进行页面跳转
 */
public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.ViewHolder> {
    private final Context mContext;
    private List<SettingsBean> mSettingBeanList = new ArrayList<>();
    private final OnSettingItemClickListener mOnSettingItemClickListener;

    public SettingAdapter(Context mContext, List<SettingsBean> mSettingBeanList,
                          OnSettingItemClickListener onSettingItemClickListener) {
        this.mContext = mContext;
        this.mSettingBeanList = mSettingBeanList;
        this.mOnSettingItemClickListener = onSettingItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.setting_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SettingsBean settingsBean = mSettingBeanList.get(position);

        holder.tv_list.setText(settingsBean.name);
        holder.iv_icon.setImageResource(settingsBean.icon);
        holder.iv_next.setImageResource(R.drawable.baseline_arrow_forward_ios_black_24);

        if (settingsBean.icon == R.drawable.baseline_info_24) {
            SharedPreferences sharedPreferences = mContext.getSharedPreferences(PreferenceKeys.PREFS_NAME, MODE_PRIVATE);
            holder.iv_red_dot.setVisibility(sharedPreferences.getBoolean(PreferenceKeys.KEY_CHECK_UPDATE, false) ? VISIBLE : INVISIBLE);
        }

        // 设置触摸按压效果
        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MaterialCardView card = holder.cardView;
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    card.animate()
                            .scaleX(0.97f)
                            .scaleY(0.97f)
                            .setDuration(80)
                            .start();
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP
                        || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    card.animate()
                            .scaleX(1.00f)
                            .scaleY(1.00f)
                            .setDuration(80)
                            .start();
                }
                return false;
            }
        });

        // 设置项点击：回调页面进行页面跳转
        holder.itemView.setOnClickListener(v -> {
            if (mOnSettingItemClickListener != null) {
                mOnSettingItemClickListener.onSettingItemClick(position);
            }
        });

        // 应用用户设置的卡片高度（每次绑定都设置，避免复用旧高度的条目）
        ThemesMangerUtils.applyAllCardElevation(mContext, holder.cardView);
    }

    @Override
    public int getItemCount() {
        return mSettingBeanList.size();
    }

    /**
     * 设置项点击回调，由页面负责具体的页面跳转
     */
    public interface OnSettingItemClickListener {
        void onSettingItemClick(int position);
    }

    /**
     * 条目 ViewHolder：缓存条目内控件引用
     */
    public static final class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv_icon;
        public TextView tv_list;
        public ImageView iv_next;
        public ImageView iv_red_dot;
        public MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.iv_icon);
            tv_list = itemView.findViewById(R.id.tv_list);
            iv_next = itemView.findViewById(R.id.iv_next);
            iv_red_dot = itemView.findViewById(R.id.iv_red_dot);
            cardView = itemView.findViewById(R.id.card_root);
        }
    }
}
