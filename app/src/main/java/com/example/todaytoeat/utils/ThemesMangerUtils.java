package com.example.todaytoeat.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.card.MaterialCardView;

public class ThemesMangerUtils {
    private static final String PREFS_NAME = "setting";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_DYNAMIC_STATUS = "dynamic_status";
    private static final String KEY_THEME_VERSION = "theme_version";
    private static final String KEY_CARD_ELEVATION = "card_elevation";
    public static final int CARD_ELEVATION_NORMAL_VALUE = 3;

    /**
     * 0 -> light
     * 1 -> dark
     * 2 -> allow system
     * */
    public static final int LIGHT = 0;
    public static final int DARK = 1;
    public static final int SYSTEM = 2;

    /**
     * 获取应用配置 SharedPreferences。
     * 统一使用 applicationContext，避免 Activity 生命周期导致的内存泄漏。
     */
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    /**
     * 获取主题颜色选项
     */
    public static int getColorThemesChoice(Context context) {
        return getSharedPreferences(context).getInt(KEY_THEME_MODE, SYSTEM);
    }

    /**
     * 保存主题颜色选项，并递增主题版本号，用于通知其他页面重建。
     */
    public static void setColorThemesChoice(Context context, int mode) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        int nextVersion = sharedPreferences.getInt(KEY_THEME_VERSION, 0) + 1;
        sharedPreferences.edit()
                .putInt(KEY_THEME_MODE, mode)
                .putInt(KEY_THEME_VERSION, nextVersion)
                .apply();
    }

    /**
     * 设置主题颜色
     * @param mode 设置颜色
     */
    public static void setColorTheme(int mode) {
        switch (mode) {
            case LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;

            case DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;

            case SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    /**
     * 动态配色只在 Android 12（API 31）及以上可用。
     */
    public static boolean isDynamicColorSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    /**
     * 读取用户是否开启了动态配色。
     */
    public static boolean getDynamicColorStatus(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_DYNAMIC_STATUS, true);
    }

    /**
     * 保存动态配色开关状态，并递增主题版本号，用于通知其他页面重建。
     */
    public static void setDynamicColorStatus(Context context, boolean enabled) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        int nextVersion = sharedPreferences.getInt(KEY_THEME_VERSION, 0) + 1;
        sharedPreferences.edit()
                .putBoolean(KEY_DYNAMIC_STATUS, enabled)
                .putInt(KEY_THEME_VERSION, nextVersion)
                .apply();
    }

    /**
     * 获取当前主题版本号。主题模式或动态配色变化时该值会递增。
     */
    public static int getThemeVersion(Context context) {
        return getSharedPreferences(context).getInt(KEY_THEME_VERSION, 0);
    }

    /**
     * 设置当前 activity 中 card 高度设置
     */
    public static void setAllCardElevation(Context context, MaterialCardView... cardViews){
        for (MaterialCardView cardView : cardViews) {
            int elevation = getCardElevationValue(context) * CARD_ELEVATION_NORMAL_VALUE;
            Log.d("elevation", "setAllCardElevation: " + elevation);
            cardView.setCardElevation(elevation);
        }
    }

    /**
     * 获取当前card all elevation's value
     * */
    public static int getCardElevationValue(Context context){
        return getSharedPreferences(context).getInt(KEY_CARD_ELEVATION, 1);
    }

    /**
     * 设置所有 card 的高度值
     * @param value 设置的高度值
     * */
    public static void putAllCardElevation(Context context, int value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        sharedPreferences.edit().putInt(KEY_CARD_ELEVATION, value).apply();
    }
}
