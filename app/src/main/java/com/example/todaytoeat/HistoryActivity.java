package com.example.todaytoeat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todaytoeat.adapter.HistoryAdapter;
import com.example.todaytoeat.beans.HistoryBean;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RadioGroup rgDateFilter;
    private RadioButton rb7Days;
    private RadioButton rb10Days;
    private RadioButton rb15Days;
    private RadioButton rb30Days;

    private ListView lvHistory;
    private HistoryAdapter historyAdapter;

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        rgDateFilter = findViewById(R.id.rg_date_filter);
        rb7Days = findViewById(R.id.rb_7days);
        rb10Days = findViewById(R.id.rb_10days);
        rb15Days = findViewById(R.id.rb_15days);
        rb30Days = findViewById(R.id.rb_30days);

        SharedPreferences sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        int historyDays = sharedPreferences.getInt("historyDays", 7);

        // 查询默认选中状态
        if (historyDays == 7){
            rb7Days.setChecked(true);
        } else if (historyDays == 10){
            rb10Days.setChecked(true);
        } else if (historyDays == 15){
            rb15Days.setChecked(true);
        } else if (historyDays == 30){
            rb30Days.setChecked(true);
        }

        rgDateFilter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int days;
                if (checkedId == R.id.rb_7days) {
                    days = 7;
                } else if (checkedId == R.id.rb_10days) {
                    days = 10;
                } else if (checkedId == R.id.rb_15days) {
                    days = 15;
                } else {
                    days = 30;
                }
                sharedPreferences.edit().putInt("historyDays", days).apply();
                loadHistoryData();
            }
        });


        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EdgeToEdge.enable(this);

        lvHistory = findViewById(R.id.lv_history);
        loadHistoryData();

        // 返回按钮
        findViewById(R.id.ib_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void loadHistoryData() {
        List<HistoryBean> list = HistoryBean.getDefaultList(this);
        if (historyAdapter == null) {
            historyAdapter = new HistoryAdapter(this, list);
            lvHistory.setAdapter(historyAdapter);
        } else {
            historyAdapter.refreshData(list);
        }
    }

}




