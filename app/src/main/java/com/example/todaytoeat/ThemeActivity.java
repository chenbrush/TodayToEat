package com.example.todaytoeat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

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

        // 对主题模式进行管理
        int themeMode = ThemesMangerUtils.getColorThemesChoice(ThemeActivity.this);
        ThemesMangerUtils.setColorTheme(themeMode);
        setContentView(R.layout.activity_theme);
        // 保持系统导航栏透明，避免关闭动态配色后出现对比度遮罩
        SystemBarUtils.setTransparentNavigationBar(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.theme_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        SwitchMaterial sw_color_manager = findViewById(R.id.sw_color_manager);
        MaterialCardView card_color_manager = findViewById(R.id.card_color_manager);
        RadioGroup rg_color_mode_manager = findViewById(R.id.rg_color_mode_manager);
        MaterialRadioButton rb_light = findViewById(R.id.rb_light);
        MaterialRadioButton rb_dark = findViewById(R.id.rb_dark);
        MaterialRadioButton rb_system = findViewById(R.id.rb_system);

        // 根据设置的默认主题对RadioButton 进行设置
        // 为防止出现奇怪的问题，default 和 SYSTEM 一起设置
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
            SharedPreferences sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
            // 读取改变按钮
            int themeModeChange;
            if (i == R.id.rb_light){
                themeModeChange = ThemesMangerUtils.LIGHT;
            }else if (i == R.id.rb_dark){
                themeModeChange = ThemesMangerUtils.DARK;
            }else {
                themeModeChange = ThemesMangerUtils.SYSTEM;
            }

            // 将改变的数据存入sharedPreferences
            sharedPreferences.edit().putInt("theme_mode", themeModeChange).apply();
            // 重新创建当前页面，让新的主题模式立即生效
            recreate();
        });

        // 动态配色仅在 Android 12 及以上可用，低版本直接隐藏该配置
        if (ThemesMangerUtils.isDynamicColorSupported()) {
            card_color_manager.setVisibility(View.VISIBLE);
            sw_color_manager.setChecked(ThemesMangerUtils.getDynamicColorStatus(ThemeActivity.this));
            sw_color_manager.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ThemesMangerUtils.setDynamicColorStatus(ThemeActivity.this, isChecked);
                recreate();
            });
        } else {
            card_color_manager.setVisibility(View.GONE);
        }

    }
}
