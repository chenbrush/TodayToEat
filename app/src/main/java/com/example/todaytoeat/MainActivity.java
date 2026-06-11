package com.example.todaytoeat;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;

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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.todaytoeat.fragment.MainFragment;
import com.example.todaytoeat.fragment.SettingFragment;
import com.example.todaytoeat.utils.GithubUpdateUtils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.IOException;

// 没事做一个手机版的今天吃什么，核心代码没怎么动，算是学以致用
public class MainActivity extends AppCompatActivity {
    private final MainFragment mainFragment = new MainFragment();
    private final SettingFragment settingFragment = new SettingFragment();
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fl_main_content, mainFragment);
            transaction.commit();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.nav_home) {
                    switchFragment(mainFragment);
                } else if (menuItem.getItemId() == R.id.nav_settings) {
                    switchFragment(settingFragment);
                }

                return true;
            }
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
                    // 更新完后立即刷新 Badge 显示状态
                    BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_settings);
                    badgeDrawable.setVisible(hasNew);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_main_content, fragment);
        transaction.setTransition(TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }
}
