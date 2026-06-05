package com.example.todaytoeat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.todaytoeat.R;
import com.example.todaytoeat.beans.HistoryBean;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends BaseAdapter {
    private Context mContext;
    private List<HistoryBean> mHistoryBeanList = new ArrayList<>();

    public HistoryAdapter(Context mContext, List<HistoryBean> list) {
        this.mContext = mContext;
   this.mHistoryBeanList = list;
   }
   
    public void refreshData(List<HistoryBean> list) {
        this.mHistoryBeanList = list;
        notifyDataSetChanged();
    }

   @Override
   public int getCount() {
       return mHistoryBeanList.size();
    }

    @Override
    public Object getItem(int i) {
        return mHistoryBeanList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.history_item, viewGroup, false);

            holder = new ViewHolder();
            holder.tv_his_date = view.findViewById(R.id.tv_his_date);
            holder.tv_his_amEat = view.findViewById(R.id.tv_his_amEat);
            holder.tv_his_pmEat = view.findViewById(R.id.tv_his_pmEat);

            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }

        HistoryBean historyBean = mHistoryBeanList.get(i);

        holder.tv_his_date.setText(historyBean.date);
        holder.tv_his_amEat.setText(historyBean.amEatHis);
        holder.tv_his_pmEat.setText(historyBean.pmEatHis);

        return view;
    }

    public final class ViewHolder{
        public TextView tv_his_date;
        public TextView tv_his_amEat;
        public TextView tv_his_pmEat;
    }
}
