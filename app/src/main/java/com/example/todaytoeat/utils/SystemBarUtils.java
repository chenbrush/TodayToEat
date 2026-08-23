package com.example.todaytoeat.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;

/**
 * 系统栏工具类：统一处理各页面系统导航栏的显示效果
 */
public class SystemBarUtils {

    /**
     * 将系统导航栏设置为透明，使导航栏区域直接透出页面自身背景色。
     * 兼容处理说明：
     * 1. EdgeToEdge 在 Android 15 以下（API 28/29）会给导航栏叠加半透明遮罩，这里显式恢复透明；
     * 2. 关闭对比度遮罩（API 29+），避免系统在三键导航下再叠加半透明色块；
     * 3. Android 15（API 35）起系统强制边到边，导航栏本身即为透明，无需额外设置。
     */
    @SuppressWarnings("deprecation")
    public static void setTransparentNavigationBar(Activity activity) {
        // 关闭系统导航栏对比度遮罩，避免三键导航叠加半透明色块（API 29+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.getWindow().setNavigationBarContrastEnforced(false);
        }
        // Android 15 起忽略该颜色设置，导航栏天然透明；低版本显式恢复透明
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
    }
}
