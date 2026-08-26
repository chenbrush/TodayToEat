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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.todaytoeat.R;
import com.example.todaytoeat.beans.SettingsBean;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class SettingAdapter extends BaseAdapter {
    private final Context mContext;
    private List<SettingsBean> mSettingBeanList = new ArrayList<>();
    private final OnSettingItemClickListener mOnSettingItemClickListener;

    public SettingAdapter(Context mContext, List<SettingsBean> mSettingBeanList,
                          OnSettingItemClickListener onSettingItemClickListener) {
        this.mContext = mContext;
        this.mSettingBeanList = mSettingBeanList;
        this.mOnSettingItemClickListener = onSettingItemClickListener;
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
            view = LayoutInflater.from(mContext).inflate(R.layout.setting_item, viewGroup, false);

            holder = new ViewHolder();
            holder.iv_icon = view.findViewById(R.id.iv_icon);
            holder.tv_list = view.findViewById(R.id.tv_list);
            holder.iv_next = view.findViewById(R.id.iv_next);
            holder.iv_red_dot = view.findViewById(R.id.iv_red_dot);
            holder.cardView = view.findViewById(R.id.card_root);

            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        SettingsBean settingsBean = mSettingBeanList.get(i);

        holder.tv_list.setText(settingsBean.name);
        holder.iv_icon.setImageResource(settingsBean.icon);
        holder.iv_next.setImageResource(R.drawable.baseline_arrow_forward_ios_black_24);

        if (settingsBean.icon == R.drawable.baseline_info_24){
            SharedPreferences sharedPreferences = mContext.getSharedPreferences("setting", MODE_PRIVATE);
            holder.iv_red_dot.setVisibility(sharedPreferences.getBoolean("checkUpdate", false) ? VISIBLE : INVISIBLE);
        }

        ViewHolder finalHolder = holder;

        // 设置触摸效果
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MaterialCardView card = finalHolder.cardView;
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
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

        // 由卡片自身处理点击，既保留按压状态层，也避免与 ListView 的条目点击冲突
        view.setOnClickListener(v -> {
            if (mOnSettingItemClickListener != null) {
                mOnSettingItemClickListener.onSettingItemClick(i);
            }
        });

        return view;
    }

    /**
     * 设置项点击回调，由页面负责具体的页面跳转
     */
    public interface OnSettingItemClickListener {
        void onSettingItemClick(int position);
    }

    public static final class ViewHolder{
        public ImageView iv_icon;
        public TextView tv_list;
        public ImageView iv_next;
        public ImageView iv_red_dot;
        public MaterialCardView cardView;
    }
}
