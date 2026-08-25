package com.example.todaytoeat;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.todaytoeat.adapter.ViewPagerAdapter;
import com.example.todaytoeat.utils.GithubUpdateUtils;
import com.example.todaytoeat.utils.SystemBarUtils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.IOException;

// 没事做一个手机版的今天吃什么，核心代码没怎么动，算是学以致用
public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // 系统导航栏保持透明（Android 文档推荐做法），底部导航栏背景延伸绘制在其后方
        SystemBarUtils.setTransparentNavigationBar(this);

        viewPager2 = findViewById(R.id.view_pager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 设置 ViewPager2 适配器
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);

        // ViewPager2 页面切换时同步底部导航栏
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                } else if (position == 1) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_settings);
                    checkUpdate();
                }
            }
        });

        // 底部导航栏点击时同步 ViewPager2
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            if (menuItem.getItemId() == R.id.nav_home) {
                viewPager2.setCurrentItem(0, true);
                return true;
            } else if (menuItem.getItemId() == R.id.nav_settings) {
                viewPager2.setCurrentItem(1, true);
                checkUpdate();
                return true;
            }
            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        checkUpdate();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkUpdate();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUpdate();
    }

    private void checkUpdate() {
        // 检查更新
        SharedPreferences sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        PackageManager pm = this.getPackageManager();
        String currentVersion = "";
        try {
            PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
            currentVersion = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }
        // 保留上一次的更新标记，避免网络失败时错误地清除
        final boolean previousHasNew = sharedPreferences.getBoolean("checkUpdate", false);
        final String finalVersion = currentVersion;
        new Thread(() -> {
            try {
                String latestVersion = GithubUpdateUtils.getGithubUpdate();
                assert finalVersion != null;
                boolean hasNew = GithubUpdateUtils.compareVersion(latestVersion, finalVersion);

                runOnUiThread(() -> {
                    sharedPreferences.edit()
                            .putBoolean("checkUpdate", hasNew)
                            .apply();
                    // 更新完毕后立刻刷新 Badge 显示状态
                    BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_settings);
                    badgeDrawable.setVisible(hasNew);
                });
            } catch (IOException e) {
                e.printStackTrace();
                // 网络请求失败时，保留之前已有的更新标记和 Badge 状态，不要错误清除
                runOnUiThread(() -> {
                    BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_settings);
                    badgeDrawable.setVisible(previousHasNew);
                });
            }
        }).start();
    }

    public void switchToPage(int page) {
        viewPager2.setCurrentItem(page, true);
    }
}
