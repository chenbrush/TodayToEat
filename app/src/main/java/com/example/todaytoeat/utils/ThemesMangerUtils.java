package com.example.todaytoeat.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemesMangerUtils {
    /**
     * 0 -> light
     * 1 -> dark
     * 2 -> allow system
     * */
    public static final int LIGHT = 0;
    public static final int DARK = 1;
    public static final int SYSTEM = 2;
    public static int getThemesChoice(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("setting", MODE_PRIVATE);
        return sharedPreferences.getInt("theme_mode", SYSTEM);
    }

    public static void setTheme(int mode){
        switch (mode){
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
}
