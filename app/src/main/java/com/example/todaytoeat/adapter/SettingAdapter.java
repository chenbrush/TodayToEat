package com.example.todaytoeat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
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
            view = LayoutInflater.from(mContext).inflate(R.layout.setting_item, viewGroup, false);

            holder = new ViewHolder();
            holder.iv_icon = view.findViewById(R.id.iv_icon);
            holder.tv_list = view.findViewById(R.id.tv_list);
            holder.iv_next = view.findViewById(R.id.iv_next);
            holder.cardView = view.findViewById(R.id.card_root);

            // 解析themes颜色
            TypedValue typedValue = new TypedValue();
            mContext.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
            holder.normalColor = typedValue.data;

            mContext.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryVariant, typedValue, true);
            holder.pressColor = typedValue.data;

            // 设置默认颜色
            holder.cardView.setCardBackgroundColor(holder.normalColor);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        SettingsBean settingsBean = mSettingBeanList.get(i);

        holder.tv_list.setText(settingsBean.name);
        holder.iv_icon.setImageResource(settingsBean.icon);
        holder.iv_next.setImageResource(R.drawable.baseline_arrow_forward_ios_black_24);

        ViewHolder finalHolder = holder;

        // 设置触摸效果
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                MaterialCardView card = finalHolder.cardView;
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    card.setCardBackgroundColor(finalHolder.pressColor);
                    card.animate()
                            .scaleX(0.97f)
                            .scaleY(0.97f)
                            .setDuration(80)
                            .start();

                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            card.setCardBackgroundColor(finalHolder.normalColor);
                            card.animate()
                                    .scaleX(1.00f)
                                    .scaleY(1.00f)
                                    .setDuration(80)
                                    .start();
                        }
                    }, 200);
                }
                return false;
            }
        });

        return view;
    }

    public static final class ViewHolder{
        public ImageView iv_icon;
        public TextView tv_list;
        public ImageView iv_next;
        public int normalColor;
        public int pressColor;
        public MaterialCardView cardView;
    }
}
