package com.example.todaytoeat.beans;

import android.content.Context;

import com.example.todaytoeat.utils.HistoryManager;

import java.util.List;

public class HistoryBean {
    public String amEatHis;
    public String pmEatHis;
    public String date;

    public HistoryBean(String date, String pmEatHis, String amEatHis) {
        this.date = date;
        this.pmEatHis = pmEatHis;
        this.amEatHis = amEatHis;
    }

    public static List<HistoryBean> getDefaultList(Context context){
        return HistoryManager.getHistoryList(context);
    }
}
