package com.example.todaytoeat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todaytoeat.utils.SystemBarUtils;
import com.example.todaytoeat.utils.ThemesMangerUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ThemeActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
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

        // 应用用户设置的卡片高度值到本页所有卡片
        ThemesMangerUtils.applyAllCardElevation(ThemeActivity.this, findViewById(R.id.theme_activity));

        SwitchMaterial sw_color_manager = findViewById(R.id.sw_color_manager);
        MaterialCardView card_color_manager = findViewById(R.id.card_color_manager);
        MaterialButtonToggleGroup tg_color_mode_manager = findViewById(R.id.tg_color_mode_manager);

        // 根据设置的默认主题对按钮组进行设置
        // 为防止出现奇怪的问题，default 和 SYSTEM 一起设置
        int themeMode = ThemesMangerUtils.getColorThemesChoice(ThemeActivity.this);
        switch (themeMode){
            case ThemesMangerUtils.LIGHT:
                tg_color_mode_manager.check(R.id.btn_light);
                break;

            case ThemesMangerUtils.DARK:
                tg_color_mode_manager.check(R.id.btn_dark);
                break;

            case ThemesMangerUtils.SYSTEM:
            default:
                tg_color_mode_manager.check(R.id.btn_system);
                break;

        }

        tg_color_mode_manager.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            // 只在按钮被选中时处理，避免取消选中时重复触发
            if (!isChecked) {
                return;
            }
            // 读取改变按钮
            int themeModeChange;
            if (checkedId == R.id.btn_light){
                themeModeChange = ThemesMangerUtils.LIGHT;
            }else if (checkedId == R.id.btn_dark){
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

        // 顶部工具栏返回按钮：关闭当前页面
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(view -> finish());

        Slider sb_change_color_elevation = findViewById(R.id.sb_change_card_elevation);
        TextView tv_change_card_result = findViewById(R.id.tv_change_card_result);

        // 读取已保存的卡片高度（1~20），并同步到滑块
        int progressValue = ThemesMangerUtils.getCardElevationValue(ThemeActivity.this);
        sb_change_color_elevation.setValue(progressValue);
        tv_change_card_result.setText(getString(R.string.card_height_current) + progressValue);
        MaterialCardView card_change_card_elevation = findViewById(R.id.card_change_card_elevation);

        // 监听滑块值变化，实时更新预览卡片高度并保存
        sb_change_color_elevation.addOnChangeListener((slider, value, fromUser) -> {
            int elevation = (int) value;
            Log.d("slider", "onValueChange: " + elevation);
            tv_change_card_result.setText(getString(R.string.card_height_current) + elevation);
            card_change_card_elevation.setCardElevation(elevation * ThemesMangerUtils.CARD_ELEVATION_NORMAL_VALUE);
            ThemesMangerUtils.putAllCardElevation(ThemeActivity.this, elevation);
        });

    }

    /**
     * 以淡入淡出动画重启主题管理页。
     * 相比直接 recreate，能避免切换深浅色时出现生硬闪烁。
     */
    @SuppressLint("ResourceAsColor")
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
