package com.example.todaytoeat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.net.ssl.HttpsURLConnection;

public class VersionActivity extends AppCompatActivity implements View.OnClickListener {

    private String currentVersion;
    private TextView tv_auto_update;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_version);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);

        // 设置当前版本信息
        TextView tv_current_vision = findViewById(R.id.tv_current_version);
        PackageManager pm = this.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(this.getPackageName(), 0);
            currentVersion = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        tv_current_vision.setText(currentVersion);

        findViewById(R.id.btn_check_update).setOnClickListener(this);
        findViewById(R.id.ib_back).setOnClickListener(this);
        tv_auto_update = findViewById(R.id.tv_auto_update);
        tv_auto_update.setText(sharedPreferences.getString("lastUpdateDate", ""));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_check_update) {
            new Thread(new Runnable() {
                @SuppressLint("CommitPrefEdits")
                @Override
                public void run() {
                    Log.d("version", "run");
                    try {
                        String githubLatestVersion = getGithubUpdate();
                        // 确认是否有更新
                        if (compareVersion(githubLatestVersion, currentVersion)) {
                            Log.d("version", githubLatestVersion);
                            // 当前版本为旧版本
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 设置更新提示框
                                    new MaterialAlertDialogBuilder(VersionActivity.this)
                                            .setTitle(R.string.notice)
                                            .setMessage(R.string.version_new_version_publish)
                                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    Intent intent = new Intent(Intent.ACTION_VIEW,
                                                            Uri.parse("https://github.com/chenbrush/TodayToEat/releases/latest"));
                                                    startActivity(intent);
                                                }
                                            })
                                            .setNegativeButton(R.string.cancel, null)
                                            .create()
                                            .show();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @SuppressLint("CommitPrefEdits")
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            R.string.version_notice_newst_version,
                                            Toast.LENGTH_LONG).show();

                                }
                            });
                        }

                        // 获取当地时间，并记录最后点击更新时间
                        LocalDateTime localDateTime = LocalDateTime.now();
                        String currentTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        String lastCheckUpdate = getString(R.string.version_last_check_update) + currentTime;
                        tv_auto_update.setText(lastCheckUpdate);
                        sharedPreferences.edit().
                                putString("lastUpdateDate", lastCheckUpdate).
                                apply();

                        // 检查更新传递到BottomNavigation中
                        sharedPreferences.edit().
                                putBoolean("checkUpdate", compareVersion(githubLatestVersion, currentVersion)).
                                apply();
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        R.string.version_notice_network_request_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();

        } else if (view.getId() == R.id.ib_back) {
            finish();
        }
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
