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

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 临时屏蔽商铺工具类。
 * 临时屏蔽只在「当天」有效，跨天后自动解除；「当天」的划分沿用项目规则：
 * 晚上 21 点及以后算新的一天，与 HistoryManager 中就餐日的判断保持一致。
 */
public final class TemporaryBlockUtils {

    private TemporaryBlockUtils() {
        // 工具类，不允许实例化
    }

    /**
     * 获取当前被临时屏蔽的商铺名。
     * 读取时若发现记录已跨天，会自动清除失效记录。
     *
     * @return 被临时屏蔽的商铺名，没有临时屏蔽或已跨天时返回 null
     */
    public static String getTempBlockedShop(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PreferenceKeys.PREFS_NAME, Context.MODE_PRIVATE);
        String shopName = sp.getString(PreferenceKeys.KEY_TEMP_HIDE_SHOP, null);
        if (shopName == null) {
            return null;
        }

        // 与记录时保存的就餐日比较，不是同一天说明临时屏蔽已经过期
        String savedDate = sp.getString(PreferenceKeys.KEY_TEMP_HIDE_SHOP_DATE, null);
        if (!getNowBelongDate().toString().equals(savedDate)) {
            clearTempBlock(context);
            return null;
        }
        return shopName;
    }

    /**
     * 临时屏蔽商铺，同时保存该记录所属的就餐日
     *
     * @param shopName 需要临时屏蔽的商铺名
     */
    public static void setTempBlockedShop(Context context, String shopName) {
        context.getSharedPreferences(PreferenceKeys.PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(PreferenceKeys.KEY_TEMP_HIDE_SHOP, shopName)
                .putString(PreferenceKeys.KEY_TEMP_HIDE_SHOP_DATE, getNowBelongDate().toString())
                .apply();
    }

    /**
     * 解除临时屏蔽
     */
    public static void clearTempBlock(Context context) {
        context.getSharedPreferences(PreferenceKeys.PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(PreferenceKeys.KEY_TEMP_HIDE_SHOP)
                .remove(PreferenceKeys.KEY_TEMP_HIDE_SHOP_DATE)
                .apply();
    }

    /**
     * 判断商铺当前是否处于临时屏蔽状态
     *
     * @param shopName 需要判断的商铺名
     */
    public static boolean isTempBlocked(Context context, String shopName) {
        return shopName != null && shopName.equals(getTempBlockedShop(context));
    }

    /**
     * 获取当前的就餐日：晚上 21 点及以后算新的一天
     */
    public static LocalDate getNowBelongDate() {
        LocalDate today = LocalDate.now();
        return LocalTime.now().getHour() >= 21 ? today.plusDays(1) : today;
    }
}
