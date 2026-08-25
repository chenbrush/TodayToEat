package com.example.todaytoeat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todaytoeat.R;
import com.example.todaytoeat.beans.HistoryBean;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

/**
 * 历史记录列表适配器（RecyclerView 版本）
 * 负责展示每日早/晚餐记录，提供短按/长按按压效果，长按时回调页面弹出修改弹窗
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    // 长按条目回调：由页面（HistoryActivity）实现，用于弹出修改单日就餐记录弹窗
    public interface OnItemLongClickListener {
        void onItemLongClick(HistoryBean historyBean, ViewHolder viewHolder);
    }

    private final Context mContext;
    private List<HistoryBean> mHistoryBeanList = new ArrayList<>();
    private OnItemLongClickListener onItemLongClickListener;

    public HistoryAdapter(Context context, List<HistoryBean> list) {
        this.mContext = context;
        this.mHistoryBeanList = list;
    }

    // 设置长按条目回调
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.onItemLongClickListener = listener;
    }

    // 刷新列表数据
    public void refreshData(List<HistoryBean> list) {
        this.mHistoryBeanList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.history_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryBean historyBean = mHistoryBeanList.get(position);

        // 绑定日期与早/晚餐记录
        holder.tv_his_date.setText(historyBean.date);
        holder.tv_his_amEat.setText(historyBean.amEatHis);
        holder.tv_his_pmEat.setText(historyBean.pmEatHis);

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

        // 长按：卡片缩小并回调页面弹出修改弹窗（弹窗关闭时由页面恢复大小）
        holder.itemView.setOnLongClickListener(v -> {
            holder.cardView.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(80)
                    .start();
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(historyBean, holder);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mHistoryBeanList.size();
    }

    /**
     * 条目 ViewHolder：缓存条目内控件引用
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_his_date;
        public TextView tv_his_amEat;
        public TextView tv_his_pmEat;
        public MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_his_date = itemView.findViewById(R.id.tv_his_date);
            tv_his_amEat = itemView.findViewById(R.id.tv_his_amEat);
            tv_his_pmEat = itemView.findViewById(R.id.tv_his_pmEat);
            cardView = itemView.findViewById(R.id.card_root);
        }
    }
}
