package com.example.todaytoeat.beans;

import com.example.todaytoeat.R;

import java.util.ArrayList;
import java.util.List;

public class SettingsBean {
    public int icon;
    public String name;

    public SettingsBean(int iconList, String name) {
        this.icon = iconList;
        this.name = name;
    }

    private static final int[] iconArr = {R.drawable.baseline_history_black_24, R.drawable.baseline_format_list_bulleted_24 ,R.drawable.baseline_info_24};
    private static final String[] nameArr = {"历史记录", "管理商铺", "版本信息"};

    public static List<SettingsBean> getDefaultList(){
        List<SettingsBean> settingsBeanList = new ArrayList<>();
        for (int i = 0; i < iconArr.length; i++) {
            settingsBeanList.add(new SettingsBean(iconArr[i], nameArr[i]));
        }
        return settingsBeanList;
    }
}
