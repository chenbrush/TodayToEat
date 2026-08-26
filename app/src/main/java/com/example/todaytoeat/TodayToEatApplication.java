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

import android.app.Application;

import com.google.android.material.color.DynamicColors;

/**
 * 应用入口 Application。
 * 在所有 Activity 创建前注册动态配色回调，确保首屏（尤其是深色模式）也能应用系统动态配色。
 */
public class TodayToEatApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 注册 ActivityLifecycleCallbacks，在所有 Activity 的 PreCreated 阶段应用动态配色覆盖层
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
