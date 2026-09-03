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
 * SharedPreferences 常量类。
 * 统一维护项目中所有 SharedPreferences 的文件名与 key，
 * 后续读写配置时一律从这里取常量，避免魔法字符串分散在各处。
 */
public final class PreferenceKeys {

    private PreferenceKeys() {
        // 工具类，不允许实例化
    }

    /** 应用配置 SharedPreferences 文件名 */
    public static final String PREFS_NAME = "setting";

    // ==================== 随机选择 ====================

    /** 去重开关：是否避免与今天已选的早/晚餐重复（boolean） */
    public static final String KEY_REPETITION_STATUS = "repStatus";

    /** 相似店名去重开关（boolean） */
    public static final String KEY_SIMILAR = "similar";

    /** 被屏蔽（隐藏）的店铺名称集合（StringSet） */
    public static final String KEY_HIDE_SHOPS = "HideShops";

    // ==================== 历史记录 ====================

    /** 历史记录保留天数（int，默认 7） */
    public static final String KEY_HISTORY_DAYS = "historyDays";

    // ==================== 版本更新 ====================

    /** 是否有可用的新版本（boolean） */
    public static final String KEY_CHECK_UPDATE = "checkUpdate";

    /** 上次检查更新的日期（String） */
    public static final String KEY_LAST_UPDATE_DATE = "lastUpdateDate";

    // ==================== 主题设置 ====================

    /** 主题模式：0 浅色、1 深色、2 跟随系统（int） */
    public static final String KEY_THEME_MODE = "theme_mode";

    /** 是否开启动态配色（boolean） */
    public static final String KEY_DYNAMIC_STATUS = "dynamic_status";

    /** 主题版本号：主题模式、动态配色或卡片高度变化时自增，用于通知页面重建（int） */
    public static final String KEY_THEME_VERSION = "theme_version";

    /** 卡片高度档位（int） */
    public static final String KEY_CARD_ELEVATION = "card_elevation";
}
