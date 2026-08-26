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

import android.app.Activity;
import android.os.Build;

/**
 * 系统栏工具类：按 Android 官方文档实现透明系统导航栏
 */
public class SystemBarUtils {

    /**
     * 将系统导航栏设为透明（Android 文档推荐做法）。
     * 说明：
     * 1. 调用 enableEdgeToEdge() 后，手势导航下系统导航栏本身即为透明；
     * 2. 三键导航下系统默认会在导航栏背后叠加半透明遮罩，
     *    文档建议通过 setNavigationBarContrastEnforced(false) 移除该遮罩；
     * 3. 应用内容（如底部导航栏）应通过 WindowInsets 延伸到系统导航栏后方绘制。
     * 4. 主题中已通过 android:enforceNavigationBarContrast=false 提前关闭遮罩
     *    （窗口创建即生效），此处代码调用作为补充，避免切换页面时导航栏短暂变黑。
     */
    public static void setTransparentNavigationBar(Activity activity) {
        // 关闭三键导航的对比度遮罩，让导航栏真正透明（API 29+；API 35 起该调用被忽略，无副作用）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.getWindow().setNavigationBarContrastEnforced(false);
        }
    }
}
