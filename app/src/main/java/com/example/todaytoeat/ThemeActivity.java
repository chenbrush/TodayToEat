package com.example.todaytoeat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todaytoeat.utils.SystemBarUtils;
import com.example.todaytoeat.utils.ThemesMangerUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ThemeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_theme);
        // 保持系统导航栏透明，避免关闭动态配色后出现对比度遮罩
        SystemBarUtils.setTransparentNavigationBar(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.theme_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ThemesMangerUtils.setAllCardElevation(ThemeActivity.this,
                findViewById(R.id.card_color_manager),
                findViewById(R.id.card_change_card_elevation),
                findViewById(R.id.card_dark_or_light));

        SwitchMaterial sw_color_manager = findViewById(R.id.sw_color_manager);
        MaterialCardView card_color_manager = findViewById(R.id.card_color_manager);
        RadioGroup rg_color_mode_manager = findViewById(R.id.rg_color_mode_manager);
        MaterialRadioButton rb_light = findViewById(R.id.rb_light);
        MaterialRadioButton rb_dark = findViewById(R.id.rb_dark);
        MaterialRadioButton rb_system = findViewById(R.id.rb_system);

        // 根据设置的默认主题对RadioButton 进行设置
        // 为防止出现奇怪的问题，default 和 SYSTEM 一起设置
        int themeMode = ThemesMangerUtils.getColorThemesChoice(ThemeActivity.this);
        switch (themeMode){
            case ThemesMangerUtils.LIGHT:
                rb_light.setChecked(true);
                rb_dark.setChecked(false);
                rb_system.setChecked(false);
                break;

            case ThemesMangerUtils.DARK:
                rb_light.setChecked(false);
                rb_dark.setChecked(true);
                rb_system.setChecked(false);
                break;

            case ThemesMangerUtils.SYSTEM:
            default:
                rb_light.setChecked(false);
                rb_dark.setChecked(false);
                rb_system.setChecked(true);
                break;

        }

        rg_color_mode_manager.setOnCheckedChangeListener((radioGroup, i) -> {
            // 读取改变按钮
            int themeModeChange;
            if (i == R.id.rb_light){
                themeModeChange = ThemesMangerUtils.LIGHT;
            }else if (i == R.id.rb_dark){
                themeModeChange = ThemesMangerUtils.DARK;
            }else {
                themeModeChange = ThemesMangerUtils.SYSTEM;
            }

            // 保存主题模式，并立即更新全局深浅色模式
            ThemesMangerUtils.setColorThemesChoice(ThemeActivity.this, themeModeChange);
            ThemesMangerUtils.setColorTheme(themeModeChange);
            // 使用淡入淡出动画重启当前页面，让主题切换更平滑
            restartWithFade();
        });

        // 动态配色仅在 Android 12 及以上可用，低版本直接隐藏该配置
        if (ThemesMangerUtils.isDynamicColorSupported()) {
            card_color_manager.setVisibility(View.VISIBLE);
            sw_color_manager.setChecked(ThemesMangerUtils.getDynamicColorStatus(ThemeActivity.this));
            sw_color_manager.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ThemesMangerUtils.setDynamicColorStatus(ThemeActivity.this, isChecked);
                restartWithFade();
            });
        } else {
            card_color_manager.setVisibility(View.GONE);
        }

        findViewById(R.id.ib_back).setOnClickListener(view -> finish());

        SeekBar sb_change_color_elevation = findViewById(R.id.sb_change_card_elevation);
        TextView tv_change_card_result = findViewById(R.id.tv_change_card_result);

        int progressValue = ThemesMangerUtils.getCardElevationValue(ThemeActivity.this);
        sb_change_color_elevation.setProgress(progressValue);
        tv_change_card_result.setText(progressValue + "");

        sb_change_color_elevation.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.d("seekbar", "onProgressChanged: " + i);
                tv_change_card_result.setText(i + "");
                ThemesMangerUtils.putAllCardElevation(ThemeActivity.this, i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("seekbar", "stop");
                restartWithFade();
            }
        });

    }

    /**
     * 以淡入淡出动画重启主题管理页。
     * 相比直接 recreate，能避免切换深浅色时出现生硬闪烁。
     */
    @SuppressWarnings("deprecation")
    private void restartWithFade() {
        finish();
        startActivity(new Intent(this, ThemeActivity.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                    OVERRIDE_TRANSITION_OPEN,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.color.transparent
            );
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}
