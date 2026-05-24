package com.example.todaytoeat;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todaytoeat.utils.FileUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Random;

// 闲着没事就做了一个手机版的“今天吃什么”，核心代码没怎么动，算是学以致用
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static String amEat = "";
    static String pmEat = "";
    static String nowEat = "";
    static String noClick = "点击按钮，输出结果";
    static String[] shop;
    TextView tvResult_first;
    TextView tvResult_second;
    private LocalTime lt;
    Random r = new Random();
    private String pathLast;
    private String pathNow;
    private String directory;
    boolean shopsListExist = true;
    private SharedPreferences sharedPreferences;
    private boolean repStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.top_root_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EdgeToEdge.enable(this);

        // 初始化设置
        findViewById(R.id.btn_all_day).setOnClickListener(this);
        findViewById(R.id.btn_next_time).setOnClickListener(this);
        ImageButton ib_setting = findViewById(R.id.ib_setting);
        ib_setting.setOnClickListener(this);
        tvResult_first = findViewById(R.id.tv_result_first_line);
        tvResult_second = findViewById(R.id.tv_result_second_line);

        //定义文件存储位置和名称
        directory = Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)) + "/files";
        //这是加载之前的数据
        reload();
    }

    @Override
    protected void onStart() {
        super.onStart();
        reload();
    }

    // 数据加载
    private void reload() {
        reloadHistory();
        reloadShow();
        reloadShop();
        reloadSettings();

    }

    // 恢复设置内容
    // 只有两行可以写到reload里面，但是为了防止后期有其他的设置，所以就做了一个单独的方法
    private void reloadSettings() {
        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        repStatus = sharedPreferences.getBoolean("repStatus", false);
        Log.d("kskbl",  repStatus + "");
    }

    // 恢复显示界面
    @SuppressLint("SetTextI18n")
    private void reloadShow() {
        Button allDay = findViewById(R.id.btn_all_day);
        String content = FileUtil.openText(pathNow);

        // 判断文件读取的长度，根据长度对显示的内容进行判断
        String line1 = "";
        String line2 = "";
        if (content.isEmpty() || content.contains("null：没有记录")) {
            line1 = noClick;
        } else {
            String[] lines = content.split("：");
            if (lines.length == 2) {
                if (content.contains("仅吃")){
                    line1 = "等会吃：" + lines[1];
                }else {
                    line1 = "中饭吃：" + lines[0];
                    line2 = "晚饭吃：" + lines[1];
                }
            } else if (lines.length == 4) {
                line1 = "中饭吃：" + lines[1];
                line2 = "晚饭吃：" + lines[3];
            }
        }
        tvResult_first.setText(line1);
        tvResult_second.setText(line2);

        // 判断按钮显示方式
        if (lt.getHour() >= 21) {
            allDay.setText("明天全天吃");
        } else {
            allDay.setText("今天全天吃");
        }
    }

    // 提取商铺列表
    private void reloadShop() {
        String pathShop = directory + File.pathSeparatorChar + "shop_list.txt";
        // 确认文件是否存在
        File fileShop = new File(pathShop);
        if (!fileShop.exists() || FileUtil.openText(pathShop).isEmpty() || FileUtil.openText(pathShop).equals("未添加任何商铺")) {
            shopsListExist = false;
            noticeToAddShops();
            return;
        }

        shopsListExist = true;
        // 对获取的商铺文件进行读取，并且裁剪
        shop = FileUtil.openText(pathShop).split("[,，、]");

    }

    // 历史记录恢复
    private void reloadHistory() {
        lt = LocalTime.now();
        java.time.LocalDate todayDate = java.time.LocalDate.now();
        boolean isAfter21 = lt.getHour() >= 21;
        java.time.LocalDate belongDate = isAfter21 ? todayDate.plusDays(1) : todayDate;

        String fileName = belongDate + ".txt";
        pathNow = directory + File.pathSeparatorChar + fileName;
        pathLast = directory + File.pathSeparatorChar + belongDate.plusDays(-1) + ".txt";

        File fileHistory = new File(pathLast);
        if (!fileHistory.exists()) {
            FileUtil.saveText(pathLast, "null：没有记录：null：没有记录");
        }
    }


    // 下一次吃模块
    @SuppressLint("SetTextI18n")
    private void nextTime() {
        String historyEat = FileUtil.openText(pathLast);
        String amEaten = "";
        String pmEaten = "";
        // if判断是保险机制
        if (!historyEat.isEmpty()) {
            String[] eatArr = historyEat.split("：|aaa");
            // 这个if代码适用于文件保存时长度不同的问题
            if (eatArr.length == 4) {
                amEaten = eatArr[1];
                pmEaten = eatArr[3];
            } else if (eatArr.length == 2) {
                amEaten = eatArr[0];
                pmEaten = eatArr[1];
            }
        }

        // 是否允许每天吃的和昨天的重复
        if (repStatus){
            while (!nowEat.equals(amEaten) && !nowEat.equals(pmEaten)) {
                nowEat = shop[r.nextInt(shop.length)];

            }
        }

        tvResult_first.setText("等会吃：" + nowEat);
        tvResult_second.setText("");

        // 判断下一餐的点击时间，并根据点击时间进行存储
        // 这么做是为了符合现实情况，对文件存储以及文件调用没有任何影响
        lt = LocalTime.now();
        String desc;
        if (lt.getHour() <= 14) {
            desc = "仅吃中饭";
        } else {
            if (lt.getHour() >= 21) {
                desc = "仅吃中饭";
            } else {
                desc = "仅吃晚饭";
            }
        }

        FileUtil.saveText(pathNow, desc + "：" + nowEat);

    }

    // 全天吃模块
    @SuppressLint("SetTextI18n")
    private void allDay() {
        String historyEat = FileUtil.openText(pathLast);
        String amEaten = "";
        String pmEaten = "";
        // if判断是保险机制
        if (!historyEat.isEmpty()) {
            String[] eatArr = historyEat.split("：|aaa");
            // 这个if代码适用于文件保存时长度不同的问题
            if (eatArr.length == 4) {
                amEaten = eatArr[1];
                pmEaten = eatArr[3];
            } else if (eatArr.length == 2) {
                amEaten = eatArr[0];
                pmEaten = eatArr[1];
            }
        }

        while (true) {
            int zw = r.nextInt(shop.length);
            amEat = shop[zw];
            int ws = r.nextInt(shop.length);
            pmEat = shop[ws];

            if (repStatus){
                // 判断商铺文件是否大于3，否则会出现死循环导致程序崩溃
                if (shop.length > 3){
                    //如果随机选择出来的和前面的内容有相同，就再选一次
                    if (amEaten.equals(amEat) || amEaten.equals(pmEat) || pmEaten.equals(pmEat) || pmEaten.equals(amEat)) {
                        continue;
                    }
                }else {
                    // 所以这里直接强制更改
                    sharedPreferences.edit().putBoolean("repStatus", false).apply();
                }
            }

            //不过两顿一样的几率比较少，这是为了防止运气极佳的情况，即在程序正确的情况下，多次循环两顿都是一样的
            if (zw != ws) {
                break;
            }

        }

        //定义两个当前吃什么的变量，方便后期存储数据
        String amNowEat = "中饭吃：" + amEat;
        String pmNowEat = "晚饭吃：" + pmEat;

        //toast弹窗仅用于测试
        //Toast.makeText(this, pathNow, Toast.LENGTH_SHORT).show();

        //这里发现了界面有点错乱的问题，稍微改了一下，加了一行
        tvResult_first.setText(amNowEat);
        tvResult_second.setText(pmNowEat);

        //存储相关文件
        FileUtil.saveText(pathNow, amNowEat + "：" + pmNowEat);
    }



    // 当某个按钮被点击时
    @Override
    public void onClick(View view) {
        //下一餐按钮
        if (view.getId() == R.id.btn_next_time) {
            // 判断商铺文件是否存在
            if (!shopsListExist) {
                noticeToAddShops();
                return;
            }
            nextTime();
        }

        //全天吃按钮
        if (view.getId() == R.id.btn_all_day) {
            if (!shopsListExist) {
                noticeToAddShops();
                return;
            }
            if (shop.length <= 2) {
                noticeAddLessShopDialog();
                return;
            }
            //增加逻辑模块
            if (lt.getHour() > 14 && lt.getHour() <= 20) {
                noticeAfterDialog();
                return;

            }
            allDay();

        }


        // 进入设置按钮
        if (view.getId() == R.id.ib_setting) {
            Log.d("setting", "onClick: true");
            Intent turnToSettingIntent = new Intent();
            turnToSettingIntent.setClass(this, SettingsActivity.class);
            startActivity(turnToSettingIntent);
        }
    }

    // 创建建议下一餐的提示框
    private void noticeAfterDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
                .setTitle("友情提醒")
                .setMessage("再过一会就可以吃晚饭了，点下一餐吧")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        nextTime();

                    }
                })
                .setNegativeButton("我要把晚饭当宵夜吃！！！", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        allDay();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 创建添加的商铺很少的提示框
    private void noticeAddLessShopDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("友情提醒")
                .setMessage("添加的商铺有点少，是否前去添加？")
                .setPositiveButton("Let's goooooo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setClass(getApplication(), ListActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("还是点下一餐吧", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        nextTime();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();

        // 取消按钮强制大写功能
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setAllCaps(false);

    }

    // 创建添加商铺提示框
    private void noticeToAddShops() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("提示")
                .setMessage("还未添加任何店铺，是否前去添加？")
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent();
                        intent.setClass(getApplication(), ListActivity.class);
                        startActivity(intent);
                    }
                })
                .show();
    }

}