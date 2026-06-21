package com.example.todaytoeat;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.todaytoeat.utils.HistoryManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * 历史记录页面
 * 功能：按7/10/15/30天筛选就餐历史、展示历史列表、长按弹窗修改单日就餐记录、返回首页
 */
public class HistoryActivity extends AppCompatActivity {

    // 历史记录列表控件
    private ListView lvHistory;
    // 历史列表适配器
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // 筛选时间段单选组及对应按钮
        RadioGroup rgDateFilter = findViewById(R.id.rg_date_filter);
        RadioButton rb7Days = findViewById(R.id.rb_7days);
        RadioButton rb10Days = findViewById(R.id.rb_10days);
        RadioButton rb15Days = findViewById(R.id.rb_15days);
        RadioButton rb30Days = findViewById(R.id.rb_30days);

        // 读取本地存储的历史展示天数配置
        SharedPreferences sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        int historyDays = sharedPreferences.getInt("historyDays", 7);

        // 根据存储值默认选中对应天数单选框
        if (historyDays == 7) {
            rb7Days.setChecked(true);
        } else if (historyDays == 10) {
            rb10Days.setChecked(true);
        } else if (historyDays == 15) {
            rb15Days.setChecked(true);
        } else if (historyDays == 30) {
            rb30Days.setChecked(true);
        }

        // 单选框切换监听：修改筛选天数并刷新列表
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
                // 保存新的筛选天数到本地配置
                sharedPreferences.edit().putInt("historyDays", days).apply();
                // 重新加载历史数据刷新列表
                loadHistoryData();
            }
        });

        // 全屏沉浸适配
        EdgeToEdge.enable(this);
        // 适配状态栏、导航栏内边距
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 绑定列表控件并加载历史数据
        lvHistory = findViewById(R.id.lv_history);
        loadHistoryData();

        // 返回按钮点击事件：关闭当前页面回到首页
        findViewById(R.id.ib_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // ListView长按条目监听：弹出弹窗修改当日早/晚餐记录
        lvHistory.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                MaterialCardView cardView = view.findViewById(R.id.card_root);
                // 加载修改弹窗布局
                View dialogView = LayoutInflater.from(HistoryActivity.this).inflate(R.layout.history_dialog, null);
                EditText et_input_am_eat = dialogView.findViewById(R.id.et_input_am_eat);
                EditText et_input_pm_eat = dialogView.findViewById(R.id.et_input_pm_eat);

                // 获取长按条目对应的日期，读取当天历史记录回显到输入框
                TextView tv_his_date = view.findViewById(R.id.tv_his_date);
                String getEditDate = tv_his_date.getText().toString();
                HistoryManager.Record getLongPressDay = HistoryManager.getHistoryByDate(HistoryActivity.this, getEditDate);

                // 防止查询时一个不小心把没有记录给加进来了
                if (!getLongPressDay.amEat.equals("没有记录")){
                    et_input_am_eat.setText(getLongPressDay.amEat);
                }

                if (!getLongPressDay.pmEat.equals("没有记录")){
                    et_input_pm_eat.setText(getLongPressDay.pmEat);
                }


                // 构建修改历史记录弹窗
                new MaterialAlertDialogBuilder(HistoryActivity.this)
                        .setTitle(R.string.history_change)
                        .setMessage(R.string.history_enter_change)
                        .setView(dialogView)
                        // 确认修改按钮
                        .setPositiveButton(R.string.history_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i2) {
                                // 获取弹窗输入框的早晚餐内容
                                String amEditEat = String.valueOf(et_input_am_eat.getText());
                                String pmEditEat = String.valueOf(et_input_pm_eat.getText());

                                Log.d("date", getEditDate);
                                String changeHistory;

                                // 拼接修改后的记录文本，区分四种输入场景
                                if (amEditEat.isEmpty() && pmEditEat.isEmpty()) {
                                    // 早晚餐全部清空
                                    changeHistory = "null：没有记录：null：没有记录";
                                } else if (amEditEat.isEmpty()) {
                                    // 仅保留晚餐
                                    changeHistory = getString(R.string.only_pm) + "：" + pmEditEat;
                                } else if (pmEditEat.isEmpty()) {
                                    // 仅保留早餐
                                    changeHistory = getString(R.string.only_am) + "：" + amEditEat;
                                } else {
                                    // 早晚餐均填写完整
                                    changeHistory = getString(R.string.am_eat) + "：" + amEditEat + "：" + getString(R.string.pm_eat) + "：" + pmEditEat;
                                }

                                // 根据日期覆盖保存历史文件，刷新列表
                                HistoryManager.saveHistoryByDate(HistoryActivity.this, getEditDate, changeHistory);
                                loadHistoryData();
                            }
                        })
                        // 取消按钮无操作
                        .setNegativeButton(getString(R.string.history_cancel_change), null)
                        // 弹窗关闭时恢复条目卡片原始样式
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

    /**
     * 加载筛选范围内的就餐历史数据，更新ListView适配器
     */
    private void loadHistoryData() {
        // 根据本地存储天数读取历史记录列表
        List<HistoryBean> list = HistoryBean.getDefaultList(this);
        if (historyAdapter == null) {
            // 首次加载，初始化适配器并绑定列表
            historyAdapter = new HistoryAdapter(this, list);
            lvHistory.setAdapter(historyAdapter);
        } else {
            // 已有适配器，刷新数据源更新UI
            historyAdapter.refreshData(list);
        }
    }
}