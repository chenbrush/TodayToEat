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
package com.example.todaytoeat;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todaytoeat.utils.AppConstantsUtils;
import com.example.todaytoeat.utils.FileUtil;
import com.example.todaytoeat.utils.PreferenceKeys;
import com.example.todaytoeat.utils.ShopListUtils;
import com.example.todaytoeat.utils.SystemBarUtils;
import com.example.todaytoeat.utils.ThemesMangerUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;

/**
 * 选择设置页：集中管理随机选择相关的开关。
 * 原本放在商铺列表页顶部的两个复选框迁移到本页，
 * 并改用 Material 3 的 SwitchMaterial 展示。
 */
public class SelectionSettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SwitchMaterial swRepetition;
    private SwitchMaterial swSimilar;
    private SwitchMaterial swGrouping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        // 系统导航栏设为透明（Android 文档推荐做法）
        SystemBarUtils.setTransparentNavigationBar(this);
        setContentView(R.layout.activity_selection_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.selection_settings_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 应用用户设置的卡片高度到本页所有卡片
        ThemesMangerUtils.applyAllCardElevation(this, findViewById(R.id.selection_settings_activity));

        sharedPreferences = getSharedPreferences(PreferenceKeys.PREFS_NAME, MODE_PRIVATE);
        // 只统计真实商铺数量，不包含“未添加任何商铺”占位卡片
        int shopCount = loadShopCount();

        swRepetition = findViewById(R.id.sw_repetition);
        swSimilar = findViewById(R.id.sw_similar);
        swGrouping = findViewById(R.id.sw_grouping);

        // 先设置初始状态再注册监听，避免初始化时触发 onCheckedChanged
        swRepetition.setChecked(sharedPreferences.getBoolean(PreferenceKeys.KEY_REPETITION_STATUS, false));
        swSimilar.setChecked(sharedPreferences.getBoolean(PreferenceKeys.KEY_SIMILAR, false));
        swGrouping.setChecked(sharedPreferences.getBoolean(PreferenceKeys.KEY_SHOP_GROUPING, false));

        // 不允许每日重复
        swRepetition.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(PreferenceKeys.KEY_REPETITION_STATUS, isChecked).apply();
            // 商铺少于4家时开启会导致去重条件无法满足，弹窗提示并回退
            if (isChecked && shopCount < 4) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.notice))
                        .setMessage(R.string.notice_repeat_boom_message)
                        .setPositiveButton(R.string.notice_repeat_boom_positive_botton, (dialog, which) -> {
                            sharedPreferences.edit().putBoolean(PreferenceKeys.KEY_REPETITION_STATUS, false).apply();
                            swRepetition.setChecked(false);
                        })
                        .show();
            }
        });

        // 不允许出现相似店名
        swSimilar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                sharedPreferences.edit().putBoolean(PreferenceKeys.KEY_SIMILAR, false).apply();
                return;
            }

            // 商铺少于3家时开启会导致过滤条件无法满足，弹窗提示并回退
            if (shopCount < 3) {
                sharedPreferences.edit().putBoolean(PreferenceKeys.KEY_SIMILAR, false).apply();
                swSimilar.setChecked(false);
                new MaterialAlertDialogBuilder(this)
                        .setTitle(getString(R.string.notice))
                        .setMessage(R.string.notice_repeat_boom_message)
                        .setPositiveButton(R.string.notice_repeat_boom_positive_botton, null)
                        .show();
                return;
            }

            // 开启前先提示用户，相似过滤可能会误伤部分店名
            new MaterialAlertDialogBuilder(this)
                    .setTitle(getString(R.string.notice))
                    .setMessage(R.string.list_notice_similar_message)
                    .setPositiveButton(getString(R.string.ok), (dialog, which) ->
                            sharedPreferences.edit().putBoolean(PreferenceKeys.KEY_SIMILAR, true).apply())
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
        });

        // 允许使用商铺分组
        swGrouping.setOnCheckedChangeListener((buttonView, isChecked) ->
                sharedPreferences.edit().putBoolean(PreferenceKeys.KEY_SHOP_GROUPING, isChecked).apply());

        // 顶部工具栏返回按钮：关闭当前页面
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());
    }

    /**
     * 统计当前有效的商铺数量。
     * 文件不存在或内容仍是“未添加任何商铺”提示文字时返回 0。
     */
    private int loadShopCount() {
        String path = AppConstantsUtils.getAppDownloadDirectory(this) + File.separatorChar + "shop_list.txt";
        File file = new File(path);
        if (!file.exists()) {
            return 0;
        }

        String content = FileUtil.openText(path);
        if (ShopListUtils.isEmptyContent(this, content)) {
            return 0;
        }

        int count = 0;
        for (String name : content.split("[,，、]")) {
            if (!name.trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
