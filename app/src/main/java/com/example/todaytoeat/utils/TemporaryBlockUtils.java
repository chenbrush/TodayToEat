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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 临时屏蔽商铺工具类。
 * 临时屏蔽只在「当天」有效，当天可以屏蔽多家商铺，跨天后自动全部解除；「当天」的划分沿用项目规则：
 * 晚上 21 点及以后算新的一天，与 HistoryManager 中就餐日的判断保持一致。
 */
public final class TemporaryBlockUtils {

    private TemporaryBlockUtils() {
        // 工具类，不允许实例化
    }

    /**
     * 获取当天被临时屏蔽的商铺集合。
     * 读取时若发现记录已跨天，会自动清除失效记录。
     *
     * @return 被临时屏蔽的商铺集合，没有临时屏蔽或已跨天时返回空集合（不可修改）
     */
    public static Set<String> getTempBlockedShops(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PreferenceKeys.PREFS_NAME, Context.MODE_PRIVATE);
        // getStringSet 返回的是 SharedPreferences 内部持有的实例，只能读不能改，后面统一复制一份使用
        Set<String> shops = sp.getStringSet(PreferenceKeys.KEY_TEMP_HIDE_SHOPS, null);
        if (shops == null || shops.isEmpty()) {
            return Collections.emptySet();
        }

        // 与记录时保存的就餐日比较，不是同一天说明临时屏蔽已经过期
        String savedDate = sp.getString(PreferenceKeys.KEY_TEMP_HIDE_SHOPS_DATE, null);
        if (!getNowBelongDate().toString().equals(savedDate)) {
            clearTempBlock(context);
            return Collections.emptySet();
        }
        return new HashSet<>(shops);
    }

    /**
     * 临时屏蔽商铺，同时记录该记录所属的就餐日
     * 同一天可以屏蔽多家商铺，重复屏蔽同一家不会产生多余记录
     *
     * @param shopName 需要临时屏蔽的商铺名
     */
    public static void addTempBlockedShop(Context context, String shopName) {
        if (shopName == null || shopName.isEmpty()) {
            return;
        }

        // 复制一份再修改，避免直接修改 getStringSet 返回的内部实例
        Set<String> shops = new HashSet<>(getTempBlockedShops(context));
        shops.add(shopName);

        context.getSharedPreferences(PreferenceKeys.PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(PreferenceKeys.KEY_TEMP_HIDE_SHOPS, shops)
                .putString(PreferenceKeys.KEY_TEMP_HIDE_SHOPS_DATE, getNowBelongDate().toString())
                .apply();
    }

    /**
     * 解除指定商铺的临时屏蔽
     *
     * @param shopName 需要解除临时屏蔽的商铺名
     */
    public static void removeTempBlockedShop(Context context, String shopName) {
        Set<String> shops = new HashSet<>(getTempBlockedShops(context));
        // 本来就没有被临时屏蔽，不需要处理
        if (!shops.remove(shopName)) {
            return;
        }

        SharedPreferences sp = context.getSharedPreferences(PreferenceKeys.PREFS_NAME, Context.MODE_PRIVATE);
        if (shops.isEmpty()) {
            // 已经没有临时屏蔽的商铺了，就餐日记录也一起清掉
            clearTempBlock(context);
        } else {
            sp.edit().putStringSet(PreferenceKeys.KEY_TEMP_HIDE_SHOPS, shops).apply();
        }
    }

    /**
     * 解除全部临时屏蔽
     */
    public static void clearTempBlock(Context context) {
        context.getSharedPreferences(PreferenceKeys.PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(PreferenceKeys.KEY_TEMP_HIDE_SHOPS)
                .remove(PreferenceKeys.KEY_TEMP_HIDE_SHOPS_DATE)
                .apply();
    }

    /**
     * 判断商铺当前是否处于临时屏蔽状态
     *
     * @param shopName 需要判断的商铺名
     */
    public static boolean isTempBlocked(Context context, String shopName) {
        return shopName != null && getTempBlockedShops(context).contains(shopName);
    }

    /**
     * 获取当前的就餐日：晚上 21 点及以后算新的一天
     */
    public static LocalDate getNowBelongDate() {
        LocalDate today = LocalDate.now();
        return LocalTime.now().getHour() >= 21 ? today.plusDays(1) : today;
    }
}
