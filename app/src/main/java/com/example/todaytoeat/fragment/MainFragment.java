package com.example.todaytoeat.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.todaytoeat.ListActivity;
import com.example.todaytoeat.MainActivity;
import com.example.todaytoeat.R;
import com.example.todaytoeat.utils.FileUtil;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Random;

public class MainFragment extends Fragment implements View.OnClickListener {

    static String amEat = "";
    static String pmEat = "";
    static String nowEat = "";
    static String noClick;
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

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.top_root_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        noClick = getString(R.string.result);

        // 初始化按钮
        view.findViewById(R.id.btn_all_day).setOnClickListener(this);
        view.findViewById(R.id.btn_next_time).setOnClickListener(this);
        tvResult_first = view.findViewById(R.id.tv_result_first_line);
        tvResult_second = view.findViewById(R.id.tv_result_second_line);

        // 定义文件保存位置和名称
        directory = requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/files";
        // 加载之前的数据
        reload();
    }

    @Override
    public void onStart() {
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
    private void reloadSettings() {
        sharedPreferences = requireActivity().getSharedPreferences("setting", MODE_PRIVATE);
        repStatus = sharedPreferences.getBoolean("repStatus", false);
        Log.d("kskbl", repStatus + "");
    }

    // 恢复显示界面
    @SuppressLint("SetTextI18n")
    private void reloadShow() {
        assert getView() != null;
        Button allDay = getView().findViewById(R.id.btn_all_day);
        String content = FileUtil.openText(pathNow);

        // 判断文件读取长度
        String line1 = "";
        String line2 = "";
        if (content.isEmpty() || content.contains("null")) {
            line1 = noClick;
        } else {
            String[] lines = content.split("：");
            if (lines.length == 2) {
                if (content.contains(getString(R.string.only_eat))) {
                    line1 = getString(R.string.after_eat) + "：" + lines[1];
                } else {
                    line1 = getString(R.string.am_eat) + "：" + lines[0];
                    line2 = getString(R.string.pm_eat) + "：" + lines[1];
                }
            } else if (lines.length == 4) {
                line1 = getString(R.string.am_eat) + "：" + lines[1];
                line2 = getString(R.string.pm_eat) + "：" + lines[3];
            }
        }
        tvResult_first.setText(line1);
        tvResult_second.setText(line2);

        // 判断按钮显示
        if (lt.getHour() >= 21) {
            allDay.setText(R.string.tomorrow_all_day);
        } else {
            allDay.setText(R.string.today_all_day);
        }
    }

    // 读取店铺列表
    private void reloadShop() {
        String pathShop = directory + File.separatorChar + "shop_list.txt";
        File fileShop = new File(pathShop);
        if (!fileShop.exists() || FileUtil.openText(pathShop).isEmpty() || FileUtil.openText(pathShop).equals(getString(R.string.none_shops))) {
            shopsListExist = false;
            noticeToAddShops();
            return;
        }

        shopsListExist = true;
        shop = FileUtil.openText(pathShop).split(",");
    }

    // 历史记录恢复
    private void reloadHistory() {
        lt = LocalTime.now();
        LocalDate todayDate = LocalDate.now();
        boolean isAfter21 = lt.getHour() >= 21;
        LocalDate belongDate = isAfter21 ? todayDate.plusDays(1) : todayDate;

        String fileName = belongDate + ".txt";
        pathNow = directory + File.separatorChar + fileName;
        pathLast = directory + File.separatorChar + belongDate.plusDays(-1) + ".txt";

        File fileHistory = new File(pathLast);
        if (!fileHistory.exists()) {
            FileUtil.saveText(pathLast, "null：无记录：null：无记录");
        }
    }

    // 下一餐吃
    @SuppressLint("SetTextI18n")
    private void nextTime() {
        String historyEat = FileUtil.openText(pathLast);
        String amEaten = "";
        String pmEaten = "";
        if (!historyEat.isEmpty()) {
            String[] eatArr = historyEat.split("：");
            if (eatArr.length == 4) {
                amEaten = eatArr[1];
                pmEaten = eatArr[3];
            } else if (eatArr.length == 2) {
                amEaten = eatArr[0];
                pmEaten = eatArr[1];
            }
        }

        if (repStatus) {
            int nextMaxAttempts = 100;
            int nextAttempts = 0;
            while (true) {
                nextAttempts++;
                if (nextAttempts > nextMaxAttempts) {
                    nowEat = shop[r.nextInt(shop.length)];
                    break;
                }
                nowEat = shop[r.nextInt(shop.length)];
                if (!nowEat.equals(amEaten) && !nowEat.equals(pmEaten)) {
                    break;
                }
                Log.d("eat", nowEat);
            }
        } else {
            nowEat = shop[r.nextInt(shop.length)];
            Log.d("eat", nowEat);
        }

        tvResult_first.setText(getString(R.string.after_eat) + "：" + nowEat);
        tvResult_second.setText("");

        lt = LocalTime.now();
        String desc;
        if (lt.getHour() <= 14) {
            desc = getString(R.string.only_am);
        } else {
            if (lt.getHour() >= 21) {
                desc = getString(R.string.only_am);
            } else {
                desc = getString(R.string.only_pm);
            }
        }

        FileUtil.saveText(pathNow, desc + "：" + nowEat);
    }

    // 全天吃
    @SuppressLint("SetTextI18n")
    private void allDay() {
        String historyEat = FileUtil.openText(pathLast);
        String amEaten = "";
        String pmEaten = "";
        if (!historyEat.isEmpty()) {
            String[] eatArr = historyEat.split("：");
            if (eatArr.length == 4) {
                amEaten = eatArr[1];
                pmEaten = eatArr[3];
            } else if (eatArr.length == 2) {
                amEaten = eatArr[0];
                pmEaten = eatArr[1];
            }
        }

        int maxAttempts = 100;
        int attempts = 0;
        while (true) {
            attempts++;
            if (attempts > maxAttempts) break;

            int zw = r.nextInt(shop.length);
            amEat = shop[zw];
            int ws = r.nextInt(shop.length);
            pmEat = shop[ws];

            if (repStatus) {
                if (shop.length > 3) {
                    if (amEaten.equals(amEat) || amEaten.equals(pmEat) || pmEaten.equals(pmEat) || pmEaten.equals(amEat)) {
                        continue;
                    }
                } else {
                    sharedPreferences.edit().putBoolean("repStatus", false).apply();
                }
            }

            if (zw != ws) break;
        }

        String amNowEat = getString(R.string.am_eat) + "：" + amEat;
        String pmNowEat = getString(R.string.pm_eat) + "：" + pmEat;

        tvResult_first.setText(amNowEat);
        tvResult_second.setText(pmNowEat);
        FileUtil.saveText(pathNow, amNowEat + "：" + pmNowEat);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_next_time) {
            if (!shopsListExist) {
                noticeToAddShops();
                return;
            }
            if (shop.length <= 2 && repStatus) {
                noticeAddLessShopDialog();
                return;
            }
            nextTime();
        }

        if (view.getId() == R.id.btn_all_day) {
            if (!shopsListExist) {
                noticeToAddShops();
                return;
            }
            if (shop.length <= 2) {
                noticeAddLessShopDialog();
                return;
            }
            if (lt.getHour() > 14 && lt.getHour() <= 20) {
                noticeAfterDialog();
                return;
            }
            allDay();
        }

    }

    // 下一餐的对话框
    private void noticeAfterDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.notice)
                .setMessage(R.string.notice_nexttime_message)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> nextTime())
                .setNegativeButton(R.string.notice_nagative_button, (dialogInterface, i) -> allDay());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // 商铺少的对话框
    private void noticeAddLessShopDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.notice))
                .setMessage(R.string.notice_low_shops_message)
                .setPositiveButton(R.string.let_s_goooooo, (dialogInterface, i) -> {
                    Intent intent = new Intent();
                    intent.setClass(requireActivity(), ListActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.notice_low_shop_nageative_botton, (dialogInterface, i) -> nextTime());

        AlertDialog dialog = builder.create();
        dialog.show();
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setAllCaps(false);
    }

    // 提示添加商铺的对话框
    private void noticeToAddShops() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.notice))
                .setMessage(R.string.notice_none_shops_message)
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                    Intent intent = new Intent();
                    intent.setClass(requireActivity(), ListActivity.class);
                    startActivity(intent);
                })
                .show();
    }
}