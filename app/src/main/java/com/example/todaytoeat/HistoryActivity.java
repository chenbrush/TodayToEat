package com.example.todaytoeat;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todaytoeat.adapter.HistoryAdapter;
import com.example.todaytoeat.beans.HistoryBean;
import com.example.todaytoeat.utils.FileUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView lvHistory;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

            RadioGroup rgDateFilter = findViewById(R.id.rg_date_filter);
            RadioButton rb7Days = findViewById(R.id.rb_7days);
            RadioButton rb10Days = findViewById(R.id.rb_10days);
            RadioButton rb15Days = findViewById(R.id.rb_15days);
            RadioButton rb30Days = findViewById(R.id.rb_30days);

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

        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lvHistory = findViewById(R.id.lv_history);
        loadHistoryData();

        // 返回按钮
        findViewById(R.id.ib_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 设置长按历史记录达到修改的功能
        lvHistory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                MaterialCardView cardView = view.findViewById(R.id.card_root);
                View dialogView = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.history_dialog, null);
                EditText et_input_am_eat = dialogView.findViewById(R.id.et_input_am_eat);
                EditText et_input_pm_eat = dialogView.findViewById(R.id.et_input_pm_eat);

                new MaterialAlertDialogBuilder(HistoryActivity.this)
                        .setTitle(R.string.history_change)
                        .setMessage(R.string.history_enter_change)
                        .setView(dialogView)
                        .setPositiveButton(R.string.history_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i2) {
                                // 获取输入信息
                                String amEditEat = String.valueOf(et_input_am_eat.getText());
                                String pmEditEat = String.valueOf(et_input_pm_eat.getText());

                                // 读取需要修改的日期
                                TextView tv_his_date = view.findViewById(R.id.tv_his_date);
                                String getEditDate = tv_his_date.getText().toString();

                                // 通过读取到的日期找到相应文件进行修改
                                String directory = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/files";
                                String fileName = directory + File.separatorChar + getEditDate + ".txt";
                                Log.d("date", getEditDate);
                                File fileShop = new File(fileName);

                                String changeHistory = "";
                                // 判断输入信息是否为空
                                if (amEditEat.isEmpty() && pmEditEat.isEmpty()){
                                    changeHistory = "null：没有记录：null：没有记录";
                                }else if (amEditEat.isEmpty()){
                                    changeHistory = getString(R.string.only_pm) + "：" + pmEditEat;
                                }else if (pmEditEat.isEmpty()) {
                                    changeHistory = getString(R.string.only_am) + "：" + amEditEat;
                                }else {
                                    changeHistory = getString(R.string.am_eat) + "：" + amEditEat + "：" + getString(R.string.pm_eat) + "：" + pmEditEat;
                                }

                                FileUtil.saveText(fileName, changeHistory);
                                loadHistoryData();
                            }
                        })
                        .setNegativeButton(getString(R.string.history_cancel_change), null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                cardView.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                                cardView.setCardBackgroundColor(MaterialColors.getColor(cardView, com.google.android.material.R.attr.colorSurface));
                            }
                        })
                        .show();
                return false;
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




