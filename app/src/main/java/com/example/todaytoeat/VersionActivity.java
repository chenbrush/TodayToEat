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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todaytoeat.utils.GithubUpdateUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        findViewById(R.id.btn_enter_program).setOnClickListener(this);
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
                        String githubLatestVersion = GithubUpdateUtils.getGithubUpdate();
                        // 确认是否有更新
                        if (GithubUpdateUtils.compareVersion(githubLatestVersion, currentVersion)) {
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tv_auto_update.setText(lastCheckUpdate);
                            }
                        });
                        sharedPreferences.edit().
                                putString("lastUpdateDate", lastCheckUpdate).
                                apply();

                        // 检查更新传递到BottomNavigation中
                        sharedPreferences.edit().
                                putBoolean("checkUpdate", GithubUpdateUtils.compareVersion(githubLatestVersion, currentVersion)).
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
        } else if (view.getId() == R.id.btn_enter_program) {
            String url = "https://github.com/chenbrush/TodayToEat";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        }
    }
}