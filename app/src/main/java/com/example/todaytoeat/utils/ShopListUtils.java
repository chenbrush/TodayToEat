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
import android.content.res.Configuration;

import com.example.todaytoeat.R;

import java.util.Locale;

/**
 * 商铺列表工具类
 * 统一判断商铺列表文件的内容是否为空，
 * 避免切换系统语言后认不出旧数据里写入的其它语种提示文字
 */
public class ShopListUtils {

    // 应用内置的文案语种（对应 res/values 与 res/values-zh）
    private static final Locale[] TEXT_LOCALES = {Locale.ENGLISH, Locale.SIMPLIFIED_CHINESE};

    /**
     * 判断商铺列表内容是否为空
     * 内容为空、全是空白字符，或者等于任意内置语种的“未添加任何商铺”提示文字，都视为没有商铺
     *
     * @param context 上下文
     * @param content 商铺列表文件读取到的原始内容
     * @return true 表示当前没有任何商铺
     */
    public static boolean isEmptyContent(Context context, String content) {
        if (content == null || content.trim().isEmpty()) {
            return true;
        }

        String value = content.trim();
        for (Locale locale : TEXT_LOCALES) {
            if (value.equals(getNoShopsText(context, locale))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前语言下的“未添加任何商铺”提示文字
     */
    public static String getNoShopsText(Context context) {
        return context.getString(R.string.none_shops);
    }

    /**
     * 获取指定语言下的“未添加任何商铺”提示文字
     */
    private static String getNoShopsText(Context context, Locale locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration).getString(R.string.none_shops);
    }
}
