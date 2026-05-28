package com.example.todaytoeat.beans;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import com.example.todaytoeat.R;
import com.example.todaytoeat.utils.AppConstantsUtils;
import com.example.todaytoeat.utils.FileUtil;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
        List<HistoryBean> list = new ArrayList<>();
        LocalDate localDate = LocalDate.now();

        // 对时间进行判断
        LocalTime localTime = LocalTime.now();
        boolean isAfter21 = localTime.getHour() >= 21;
        localDate = isAfter21 ? localDate.plusDays(1) : localDate;

        for (int i = 0; i < 8; i++) {
            // 初始化文件名
            String date = localDate.minusDays(i).toString();
            String fileName = AppConstantsUtils.getDateFilePath(context, date);
            String Context = FileUtil.openText(fileName);
            String amEatHis = context.getString(R.string.no_record);
            String pmEatHis = context.getString(R.string.no_record);

            File file = new File(fileName);
            // 如果文件不存在
            // 一般来讲文件存在，里面都是有数据的
            if (file.exists()){
                String[] eatArr = Context.split("：|aaa");
                // 这个if代码适用于文件保存时长度不同的问题
                if (eatArr.length == 4){
                    amEatHis = eatArr[1];
                    pmEatHis = eatArr[3];
                } else if (eatArr.length == 2) {
                    amEatHis = eatArr[0];
                    pmEatHis = eatArr[1];
                }
            }
            list.add(new HistoryBean(date, pmEatHis, amEatHis));
        }

        return list;
    }

}
