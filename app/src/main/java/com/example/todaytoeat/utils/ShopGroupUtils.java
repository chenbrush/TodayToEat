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
package com.example.todaytoeat.utils;

/**
 * 商铺分组工具类。
 * 分组信息通过店名末尾的“#分组名”表示（例如“兰州拉面#面食”），
 * 不影响原有纯店名数据的读取，未设置分组的商铺归入默认“未分组”。
 */
public final class ShopGroupUtils {

    /** 店名与分组之间的分隔符 */
    public static final String GROUP_SEPARATOR = "#";

    private ShopGroupUtils() {
        // 工具类，不允许实例化
    }

    /**
     * 去掉分组标记后的商铺显示名。
     *
     * @param rawName 可能带“#分组名”标记的原始店名
     * @return 纯店名
     */
    public static String getShopName(String rawName) {
        if (rawName == null) {
            return "";
        }
        int index = rawName.lastIndexOf(GROUP_SEPARATOR);
        if (index > 0 && index < rawName.length() - 1) {
            return rawName.substring(0, index);
        }
        return rawName;
    }

    /**
     * 获取店名末尾的分组名。
     *
     * @param rawName 可能带“#分组名”标记的原始店名
     * @return 分组名；未设置分组时返回 null
     */
    public static String getGroupName(String rawName) {
        if (rawName == null) {
            return null;
        }
        int index = rawName.lastIndexOf(GROUP_SEPARATOR);
        if (index > 0 && index < rawName.length() - 1) {
            return rawName.substring(index + 1);
        }
        return null;
    }

    /**
     * 组合店名与分组。
     *
     * @param shopName  店名
     * @param groupName 分组名，为空时直接返回店名
     * @return 带分组标记的完整店名
     */
    public static String buildShopName(String shopName, String groupName) {
        if (shopName == null) {
            return "";
        }
        if (groupName == null || groupName.trim().isEmpty()) {
            return shopName;
        }
        return shopName + GROUP_SEPARATOR + groupName.trim();
    }
}
