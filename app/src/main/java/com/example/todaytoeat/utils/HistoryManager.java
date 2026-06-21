package com.example.todaytoeat.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.todaytoeat.beans.HistoryBean;
import com.example.todaytoeat.R;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 历史记录管理工具类
 * 提供读取和操作日常饮食历史记录的统一方法
 */
public class HistoryManager {

    /**
     * 获取历史记录列表
     */
    public static List<HistoryBean> getHistoryList(Context context) {
        List<HistoryBean> list = new ArrayList<>();
        LocalDate localDate = LocalDate.now();

        LocalTime localTime = LocalTime.now();
        boolean isAfter21 = localTime.getHour() >= 21;
        localDate = isAfter21 ? localDate.plusDays(1) : localDate;

        SharedPreferences sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        int historyDays = sharedPreferences.getInt("historyDays", 7);

        for (int i = 0; i <= historyDays; i++) {
            String date = localDate.minusDays(i).toString();
            String fileName = AppConstantsUtils.getDateFilePath(context, date);
            String content = FileUtil.openText(fileName);
            String amEatHis = context.getString(R.string.no_record);
            String pmEatHis = context.getString(R.string.no_record);

            File file = new File(fileName);
            if (file.exists()) {
                Record record = parseHistory(content);
                amEatHis = record.amEat;
                pmEatHis = record.pmEat;
            }
            list.add(new HistoryBean(date, pmEatHis, amEatHis));
        }
        return list;
    }

    /**
     * 解析历史记录字符串（支持 "早餐：A：午餐：B" 和 "早餐：A：午餐：B" 两种格式）
     * @param content 文件读取的原始字符串
     * @return 解析结果
     */
    public static Record parseHistory(String content) {
        String amEat = "";
        String pmEat = "";
        if (content != null && !content.isEmpty()) {
            String[] eatArr = content.split("：");
            if (eatArr.length == 4) {
                amEat = eatArr[1];
                pmEat = eatArr[3];
            } else if (eatArr.length == 2) {
                amEat = eatArr[0];
                pmEat = eatArr[1];
            }
        }
        return new Record(amEat, pmEat);
    }

    /**
     * 解析昨日历史记录
     * @param context 上下文
     * @return 解析结果
     */
    public static Record getHistoryByDate(Context context, String date) {
        String filePath = AppConstantsUtils.getDateFilePath(context, date);
        String content = FileUtil.openText(filePath);
        return parseHistory(content);
    }

    public static Record getYesterdayHistory(Context context) {
        LocalDate today = LocalDate.now();
        boolean isAfter21 = LocalTime.now().getHour() >= 21;
        LocalDate belongDate = isAfter21 ? today.plusDays(1) : today;
        String yesterdayPath = AppConstantsUtils.getDateFilePath(context, belongDate.minusDays(1).toString());
        String content = FileUtil.openText(yesterdayPath);
        return parseHistory(content);
    }

    /**
     * 保存今日饮食记录
     * @param context 上下文
     * @param content 记录内容
     */
    public static void saveTodayRecord(Context context, String content) {
        LocalDate today = LocalDate.now();
        boolean isAfter21 = LocalTime.now().getHour() >= 21;
        LocalDate belongDate = isAfter21 ? today.plusDays(1) : today;
        String pathNow = AppConstantsUtils.getDateFilePath(context, belongDate.toString());
        FileUtil.saveText(pathNow, content);
    }

    /**
     * 获取今日记录的文件路径
     */
    public static String getTodayFilePath(Context context) {
        LocalDate today = LocalDate.now();
        boolean isAfter21 = LocalTime.now().getHour() >= 21;
        LocalDate belongDate = isAfter21 ? today.plusDays(1) : today;
        return AppConstantsUtils.getDateFilePath(context, belongDate.toString());
    }

    /**
     * 获取昨日记录的文件路径
     */
    public static String getYesterdayFilePath(Context context) {
        LocalDate today = LocalDate.now();
        boolean isAfter21 = LocalTime.now().getHour() >= 21;
        LocalDate belongDate = isAfter21 ? today.plusDays(1) : today;
        return AppConstantsUtils.getDateFilePath(context, belongDate.minusDays(1).toString());
    }

    /**
     * 初始化昨日历史记录文件（如果不存在）
     */
    public static void saveHistoryByDate(Context context, String date, String content) {
        String fileName = AppConstantsUtils.getDateFilePath(context, date);
        FileUtil.saveText(fileName, content);
    }

    public static void ensureYesterdayFileExists(Context context) {
        String yesterdayPath = getYesterdayFilePath(context);
        File file = new File(yesterdayPath);
        if (!file.exists()) {
            FileUtil.saveText(yesterdayPath, "null：没有记录：null：没有记录");
        }
    }

    /**
     * 解析结果
     */
    public static class Record {
        public final String amEat;
        public final String pmEat;

        public Record(String amEat, String pmEat) {
            this.amEat = amEat;
            this.pmEat = pmEat;
        }
    }
}
