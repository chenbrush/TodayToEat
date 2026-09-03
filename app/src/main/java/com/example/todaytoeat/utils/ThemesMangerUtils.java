package com.example.todaytoeat.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.card.MaterialCardView;

public class ThemesMangerUtils {
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
        return context.getApplicationContext()
                .getSharedPreferences(PreferenceKeys.PREFS_NAME, MODE_PRIVATE);
    }

    /**
     * 获取主题颜色选项
     */
    public static int getColorThemesChoice(Context context) {
        return getSharedPreferences(context).getInt(PreferenceKeys.KEY_THEME_MODE, SYSTEM);
    }

    /**
     * 保存主题颜色选项，并递增主题版本号，用于通知其他页面重建。
     */
    public static void setColorThemesChoice(Context context, int mode) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        int nextVersion = sharedPreferences.getInt(PreferenceKeys.KEY_THEME_VERSION, 0) + 1;
        sharedPreferences.edit()
                .putInt(PreferenceKeys.KEY_THEME_MODE, mode)
                .putInt(PreferenceKeys.KEY_THEME_VERSION, nextVersion)
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
        return getSharedPreferences(context).getBoolean(PreferenceKeys.KEY_DYNAMIC_STATUS, true);
    }

    /**
     * 保存动态配色开关状态，并递增主题版本号，用于通知其他页面重建。
     */
    public static void setDynamicColorStatus(Context context, boolean enabled) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        int nextVersion = sharedPreferences.getInt(PreferenceKeys.KEY_THEME_VERSION, 0) + 1;
        sharedPreferences.edit()
                .putBoolean(PreferenceKeys.KEY_DYNAMIC_STATUS, enabled)
                .putInt(PreferenceKeys.KEY_THEME_VERSION, nextVersion)
                .apply();
    }

    /**
     * 获取当前主题版本号。主题模式或动态配色变化时该值会递增。
     */
    public static int getThemeVersion(Context context) {
        return getSharedPreferences(context).getInt(PreferenceKeys.KEY_THEME_VERSION, 0);
    }

    /**
     * 设置 root（页面根布局）内所有卡片的高度值。
     * 递归遍历子 View，页面后续新增卡片时无需再逐个修改调用处。
     */
    public static void applyAllCardElevation(Context context, View root){
        int elevation = getCardElevationValue(context) * CARD_ELEVATION_NORMAL_VALUE;
        Log.d("elevation", "applyAllCardElevation: " + elevation);
        applyCardElevation(root, elevation);
    }

    /**
     * 递归遍历 View 树，为所有 MaterialCardView 应用卡片高度值。
     */
    private static void applyCardElevation(View view, int elevation) {
        if (view instanceof MaterialCardView) {
            ((MaterialCardView) view).setCardElevation(elevation);
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                applyCardElevation(viewGroup.getChildAt(i), elevation);
            }
        }
    }

    /**
     * 获取当前card all elevation's value
     * */
    public static int getCardElevationValue(Context context){
        return getSharedPreferences(context).getInt(PreferenceKeys.KEY_CARD_ELEVATION, 1);
    }

    /**
     * 设置所有 card 的高度值
     * @param value 设置的高度值
     * */
    public static void putAllCardElevation(Context context, int value){
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        // 同时递增主题版本号，通知停留在后台的其他页面回到前台时重建并应用新高度
        int nextVersion = sharedPreferences.getInt(PreferenceKeys.KEY_THEME_VERSION, 0) + 1;
        sharedPreferences.edit()
                .putInt(PreferenceKeys.KEY_CARD_ELEVATION, value)
                .putInt(PreferenceKeys.KEY_THEME_VERSION, nextVersion)
                .apply();
    }
}
