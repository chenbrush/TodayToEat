package com.example.todaytoeat;

import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_FADE;
import static androidx.fragment.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN;

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
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

// 没事做一个手机版的今天吃什么，核心代码没怎么动，算是学以致用
public class MainActivity extends AppCompatActivity {
    private final MainFragment mainFragment = new MainFragment();
    private final SettingFragment settingFragment = new SettingFragment();

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
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
                String latestVersion = getGithubUpdate();
                assert finalVersion != null;
                boolean hasNew = compareVersion(latestVersion, finalVersion);

                // 操作 SharedPreferences 切回主线程
                runOnUiThread(() -> {
                    sharedPreferences.edit()
                            .putBoolean("checkUpdate", hasNew)
                            .apply();
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // 通过更新设置BottomNavigation是否有红点
        BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.nav_settings);
        badgeDrawable.setVisible(sharedPreferences.getBoolean("checkUpdate", false));

    }

    // 切换fragment
    public void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fl_main_content, fragment);
        transaction.setTransition(TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    // 从GitHub上获取最新版本
    public String getGithubUpdate() throws IOException {
        // 获取分割后的数据
        String[] getApiData = getGithub();

        // 提取分割之后的版本号
        String githubVersion = "";
        for (int i = 0; i < getApiData.length; i++) {
            if (getApiData[i].contains("tag_name")){
                githubVersion = getApiData[i];
                break;
            }
        }

        // 对分割后的版本号进行进一步分割，提取纯粹的版本号
        String[] githubVersionSplit = githubVersion.split("[\":v]");
        return githubVersionSplit[githubVersionSplit.length - 1];
    }

    @NonNull
    private static String[] getGithub() throws IOException {
        // 初始化访问数据
        final String githubApi = "https://api.github.com/repos/chenbrush/TodayToEat/releases/latest";
        URL url = new URL(githubApi);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        InputStream inputStream = connection.getInputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null){
            stringBuilder.append(line);
        }

        // 通过访问得到的数据进行分割
        return stringBuilder.toString().split(",");
    }


    /* 版本比较
    // version1 是当前最新版本，version2 是目前版本
    // 为什么这么写？
    // 写的时候回味中午吃的炸鸡了，开始写的时候没反应过来
    */
    private boolean compareVersion(String version1, String version2){
        // 将版本号变成字符串数组，里面全是字符串的数字
        String[] version1Split = version1.split("\\.");
        String[] version2Split = version2.split("\\.");

        // 核对版本号
        for (int i = 0; i < Math.min(version1Split.length, version2Split.length); i++) {
            int version1Int = Integer.parseInt(version1Split[i]);
            int version2Int = Integer.parseInt(version2Split[i]);

            if (version1Int > version2Int){
                return true;
            }
        }

        return false;
    }
}
