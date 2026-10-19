package com.example.todaytoeat.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 单个商铺分组
 */
public class ShopGroupData {
    /** 分组名称 */
    public String groupName;
    /** 分组内的商铺名称 */
    public List<String> groupShopsList = new ArrayList<>();

    public ShopGroupData() {
    }

    public ShopGroupData(String groupName, List<String> groupShopsList) {
        this.groupName = groupName;
        // 复制一份列表，避免外部修改影响分组数据
        this.groupShopsList = groupShopsList == null
                ? new ArrayList<>()
                : new ArrayList<>(groupShopsList);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getGroupShopsList() {
        return groupShopsList;
    }

    public void setGroupShopsList(List<String> groupShopsList) {
        this.groupShopsList = groupShopsList == null
                ? new ArrayList<>()
                : new ArrayList<>(groupShopsList);
    }
}
