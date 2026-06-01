package com.example.todaytoeat;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todaytoeat.adapter.SettingAdapter;
import com.example.todaytoeat.beans.SettingsBean;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;

import java.util.List;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setting_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EdgeToEdge.enable(this);

        ListView lv_setting = findViewById(R.id.lv_setting);
        lv_setting.setOnItemClickListener(this);
        List<SettingsBean> settingsBeanList = SettingsBean.getDefaultList(this);
        SettingAdapter adapter = new SettingAdapter(this, settingsBeanList);
        lv_setting.setAdapter(adapter);

        // 返回按钮
        findViewById(R.id.ib_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    // 添加记得到 SystemBean里面添加
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i){
            case 0:
                // 历史记录
                Intent intentHistory = new Intent();
                intentHistory.setClass(this, HistoryActivity.class);
                startActivity(intentHistory);
                break;

            case 1:
                // 管理商铺
                Intent intentList = new Intent();
                intentList.setClass(this, ListActivity.class);
                startActivity(intentList);
                break;

            default:
                // 版本查询
                Intent intentVersion = new Intent();
                intentVersion.setClass(this, VersionActivity.class);
                startActivity(intentVersion);
                break;
        }

    }
}