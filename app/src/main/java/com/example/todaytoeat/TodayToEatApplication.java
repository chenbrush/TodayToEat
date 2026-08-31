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

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.todaytoeat.utils.ThemesMangerUtils;
import com.google.android.material.color.DynamicColors;

/**
 * 应用入口 Application。
 * 在所有 Activity 创建前根据用户设置应用动态配色或静态配色，
 * 并保证低版本系统不会进入动态配色逻辑。
 */
public class TodayToEatApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 在每个 Activity 创建前根据用户设置应用动态配色或回退到静态配色
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                applyColorTheme(activity);
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });
    }

    /**
     * 应用配色方案：Android 12 及以上才支持动态配色，低版本保持静态主题。
     */
    private void applyColorTheme(Activity activity) {
        if (!ThemesMangerUtils.isDynamicColorSupported()) {
            return;
        }

        if (ThemesMangerUtils.getDynamicColorStatus(activity)) {
            DynamicColors.applyToActivityIfAvailable(activity);
        } else {
            activity.setTheme(R.style.Theme_TodayToEat_Static);
        }
    }
}
