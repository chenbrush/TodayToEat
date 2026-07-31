package com.example.todaytoeat.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.todaytoeat.ListActivity;
import com.example.todaytoeat.R;
import com.example.todaytoeat.utils.FileUtil;
import com.example.todaytoeat.utils.HistoryManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.time.LocalTime;
import java.util.Random;

public class MainFragment extends Fragment implements View.OnClickListener {

    private String amEat = "";
    private String pmEat = "";
    private String noClick;
    private String[] shop;
    TextView tvResult_first;
    TextView tvResult_second;
    private LocalTime lt;
    Random r = new Random();
    private String directory;
    boolean shopsListExist = true;
    private SharedPreferences sharedPreferences;
    private boolean repStatus;
    private boolean similar;

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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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

        // 为两条结果TextView添加长按监听（修改当天记录）
        tvResult_first.setOnLongClickListener(this::onResultLongClick);
        tvResult_second.setOnLongClickListener(this::onResultLongClick);

        // 定义文件位置及名称
        directory = requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/files";

        // 校验昨日历史记录文件，不存在则自动创建
        HistoryManager.ensureYesterdayFileExists(requireContext());

        // 加载全部数据：历史、UI、店铺列表、配置
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
        // 获取是否禁止昨日重复店铺，默认关闭false
        repStatus = sharedPreferences.getBoolean("repStatus", false);
        // 获取是否过滤相似店名，默认关闭false
        similar = sharedPreferences.getBoolean("similar", false);
        Log.d("get settings rep status", repStatus + "");
        Log.d("get settings similar", similar + "");
    }

    // 恢复显示界面
    @SuppressLint("SetTextI18n")
    private void reloadShow() {
        assert getView() != null;
        Button allDay = getView().findViewById(R.id.btn_all_day);
        String content = FileUtil.openText(HistoryManager.getTodayFilePath(requireContext()));

        // 判断文件读取长度
        String line1 = "";
        String line2 = "";
        if (content.isEmpty() || content.contains("null")) {
            line1 = noClick;
        } else {
            String[] lines = content.split("：");
            if (lines.length == 2) {
                if (content.contains(getString(R.string.only_am))) {
                    line1 = getString(R.string.am_eat) + "："+ lines[1];
                }else if (content.contains(getString(R.string.only_pm))){
                    line1 = getString(R.string.only_pm) + "：" + lines[1];
                } else {
                    line1 = getString(R.string.am_eat) + "："+ lines[0];
                    line2 = getString(R.string.pm_eat) + "："+ lines[1];
                }
            }
            else if (lines.length == 4) {
                line1 = getString(R.string.am_eat) + "：" + lines[1];
                line2 = getString(R.string.pm_eat) + "：" + lines[3];
            }
        }
        tvResult_first.setText(line1);
        tvResult_second.setText(line2);

        // 根据内容控制单行/双行布局
        if (line2.isEmpty()) {
            adjustSingleLineCenter();
        } else {
            adjustDoubleLineLayout();
        }

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
        // 校验文件有效性
        if (!fileShop.exists() || FileUtil.openText(pathShop).isEmpty() || FileUtil.openText(pathShop).equals(getString(R.string.none_shops))) {
            shopsListExist = false;
            noticeToAddShops();
            return;
        }

        shopsListExist = true;
        // 逗号分割店铺数组
        shop = FileUtil.openText(pathShop).split(",");
    }

    // 历史记录恢复
    private void reloadHistory() {
        lt = LocalTime.now();
        HistoryManager.ensureYesterdayFileExists(requireContext());
    }

    /**
     * 下一餐吃
     */
    @SuppressLint("SetTextI18n")
    private void nextTime() {
        // 获取昨天就餐记录
        HistoryManager.Record yesterday = HistoryManager.getYesterdayHistory(requireContext());
        String amEaten = yesterday.amEat;
        String pmEaten = yesterday.pmEat;

        String nowEat;
        int maxAttempts = 100;
        int attempts = 0;
        while (true) {
            attempts++;
            // 尝试100次仍无合适店铺，直接随机返回一个
            if (attempts > maxAttempts) {
                nowEat = shop[r.nextInt(shop.length)];
                break;
            }
            nowEat = shop[r.nextInt(shop.length)];

            // 开启重复过滤：当前店铺不能等于昨日早/晚餐
            if (repStatus && (nowEat.equals(amEaten) || nowEat.equals(pmEaten))) {
                Log.d("eat", nowEat);
                continue;
            }

            // 开启相似店名过滤：当前店铺不能与昨日店内餐相似
            if (similar && (checkShopNameSimilar(amEaten, nowEat) || checkShopNameSimilar(pmEaten, nowEat))) {
                Log.d("eat", nowEat);
                continue;
            }

            // 通过所有过滤条件
            break;
        }

        // 根据时间自动判断用户是否需要选择，并且显示出相应的内容
        lt = LocalTime.now();
        if (lt.getHour() > 14 && lt.getHour() < 21){
            showNextTimeResult(getString(R.string.pm_eat) + "：" + nowEat);
        }else {
            // 做一个提示框，让用户决定这个下一餐什么时候吃
            String finalNowEat = nowEat;
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.notice))
                    .setMessage(R.string.notice_what_time_to_eat_next_time)
                    .setPositiveButton(R.string.time_mid, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showNextTimeResult(getString(R.string.only_am) + "：" + finalNowEat);
                        }
                    })
                    .setNegativeButton(R.string.time_night, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showNextTimeResult(getString(R.string.only_pm) + "：" + finalNowEat);
                        }
                    })
                    .show();
        }

    }

    /**
     * 显示下一餐结果并保存
     */
    @SuppressLint("SetTextI18n")
    private void showNextTimeResult(String desc) {
        // 这些是用来显示界面以及存储的
        HistoryManager.saveTodayRecord(requireContext(), desc);
        String[] lines = desc.split("：");
        if (lines[0].equals(getString(R.string.only_am))){
            tvResult_first.setText(getString(R.string.am_eat) + "："+ lines[1]);
        }else {
            tvResult_first.setText(getString(R.string.pm_eat) + "："+ lines[1]);
        }

        tvResult_second.setText("");
        adjustSingleLineCenter();
    }

    /**
     * 全天吃
     */
    @SuppressLint("SetTextI18n")
    private void allDay() {
        // 获取昨日就餐记录
        HistoryManager.Record yesterday = HistoryManager.getYesterdayHistory(requireContext());
        String amEaten = yesterday.amEat;
        String pmEaten = yesterday.pmEat;
        Log.d("all day", "allDay: " + amEaten + pmEaten);

        int maxAttempts = 100;
        int attempts = 0;
        while (true) {
            attempts++;
            // 尝试100次强制退出
            if (attempts > maxAttempts) break;

            // 随机早、晚餐下标
            int zw = r.nextInt(shop.length);
            amEat = shop[zw];
            int ws = r.nextInt(shop.length);
            pmEat = shop[ws];

            // 开启昨日重复过滤
            if (repStatus) {
                // 店铺总数大于3才执行重复校验
                if (shop.length > 3) {
                    // 和昨天早/晚餐重复则重新随机
                    if (amEaten.equals(amEat) || amEaten.equals(pmEat) || pmEaten.equals(pmEat) || pmEaten.equals(amEat)) {
                        continue;
                    }
                } else {
                    // 店铺不足3家，自动关闭重复过滤开关
                    sharedPreferences.edit().putBoolean("repStatus", false).apply();
                }
            }

            // 开启相似店名过滤
            if (similar) {
                // 任意一餐和昨日店铺相似则重抽
                if (checkShopNameSimilar(amEaten, amEat) || checkShopNameSimilar(pmEaten, amEat) ||
                        checkShopNameSimilar(amEaten, pmEat) || checkShopNameSimilar(pmEaten, pmEat)) {
                    continue;
                }
            }

            // 早晚店铺不能一致，满足则退出循环
            if (!amEat.equals(pmEat)) break;
        }

        // 更新页面双行结果
        String amNowEat = getString(R.string.am_eat) + "：" + amEat;
        String pmNowEat = getString(R.string.pm_eat) + "：" + pmEat;
        tvResult_first.setText(amNowEat);
        tvResult_second.setText(pmNowEat);
        adjustDoubleLineLayout();
        // 写入今日历史文件
        HistoryManager.saveTodayRecord(requireContext(), amNowEat + "：" + pmNowEat);
    }

    @Override
    public void onClick(View view) {
        // 下一餐随机按钮
        if (view.getId() == R.id.btn_next_time) {
            // 无店铺列表弹窗提示添加
            if (!shopsListExist) {
                noticeToAddShops();
                return;
            }
            // 店铺少于等于2家且开启重复过滤，弹窗提示扩充店铺
            if (shop.length <= 2 && repStatus) {
                noticeAddLessShopDialog();
                return;
            }
            nextTime();
        }

        // 全天随机按钮
        if (view.getId() == R.id.btn_all_day) {
            if (!shopsListExist) {
                noticeToAddShops();
                return;
            }
            // 全天随机至少需要3家店铺
            if (shop.length <= 2) {
                noticeAddLessShopDialog();
                return;
            }
            // 14点-20点区间，弹出选择弹窗
            if (lt.getHour() > 14 && lt.getHour() <= 20) {
                noticeAfterDialog();
                return;
            }
            allDay();
        }
    }

    /**
     * 下一餐的对话框
     */
    private void noticeAfterDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.notice)
                .setMessage(R.string.notice_nexttime_message)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> nextTime())
                .setNegativeButton(R.string.notice_nagative_button, (dialogInterface, i) -> allDay());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * 店铺数量不足弹窗
     * 跳转店铺管理页面添加店铺，或直接抽下一餐
     */
    private void noticeAddLessShopDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.notice))
                .setMessage(R.string.notice_low_shops_message)
                .setPositiveButton(R.string.let_s_goooooo, (dialogInterface, i) -> {
                    // 跳转店铺列表管理页
                    Intent intent = new Intent();
                    intent.setClass(requireActivity(), ListActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton(R.string.notice_low_shop_nageative_botton, (dialogInterface, i) -> nextTime());

        AlertDialog dialog = builder.create();
        dialog.show();
        // 取消按钮不全部大写
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setAllCaps(false);
    }

    /**
     * 未添加任何店铺弹窗，跳转店铺管理页面
     */
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


    /**
     * 长按结果文本弹出修改弹窗（修改当天记录）
     * 逻辑：读取当天历史记录回显到输入框 -> 用户修改 -> 保存并刷新UI
     */
    private boolean onResultLongClick(View view) {
        // 加载修改弹窗布局（复用 history_dialog.xml）
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.history_dialog, null);
        EditText etInputAmEat = dialogView.findViewById(R.id.et_input_am_eat);
        EditText etInputPmEat = dialogView.findViewById(R.id.et_input_pm_eat);

        // 读取当天历史记录回显到输入框
        String todayFilePath = HistoryManager.getTodayFilePath(requireContext());
        String content = FileUtil.openText(todayFilePath);
        HistoryManager.Record todayRecord = HistoryManager.parseHistory(content);

        if (!todayRecord.amEat.isEmpty() && !todayRecord.amEat.equals(getString(R.string.no_record))) {
            etInputAmEat.setText(todayRecord.amEat);
        }
        if (!todayRecord.pmEat.isEmpty() && !todayRecord.pmEat.equals(getString(R.string.no_record))) {
            etInputPmEat.setText(todayRecord.pmEat);
        }

        // 构建修改弹窗
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.main_change)
                .setMessage(R.string.history_enter_change)
                .setView(dialogView)
                .setPositiveButton(R.string.history_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String amEditEat = String.valueOf(etInputAmEat.getText());
                        String pmEditEat = String.valueOf(etInputPmEat.getText());

                        String changeHistory;
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

                        // 保存到当天记录（注意：MainFragment操作的是当天，与HistoryActivity不同）
                        HistoryManager.saveTodayRecord(requireContext(), changeHistory);
                        // 刷新UI
                        reloadShow();
                    }
                })
                .setNegativeButton(getString(R.string.history_cancel_change), null)
                .show();

        return true;
    }

    /**
     * 调整第一行垂直居中（仅单餐时使用）
     */
    private void adjustSingleLineCenter() {
        tvResult_second.setVisibility(View.GONE);
        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams) tvResult_first.getLayoutParams();
        params.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        params.verticalBias = 0.5f;
        tvResult_first.setLayoutParams(params);
    }

    /**
     * 恢复双行布局（双餐时使用）
     */
    private void adjustDoubleLineLayout() {
        tvResult_second.setVisibility(View.VISIBLE);
        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams) tvResult_first.getLayoutParams();
        params.bottomToTop = R.id.tv_result_second_line;
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        params.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
        params.verticalBias = 0.5f;
        tvResult_first.setLayoutParams(params);
    }

     /** 校验两个店名是否相似（最长公共子串长度≥3判定相似）
     * @param str1 店名1
     * @param str2 店名2
     */
    private boolean checkShopNameSimilar(String str1, String str2){
        // DP二维数组：dp[i][j]代表str1前i位、str2前j位连续匹配长度
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];

        int maxLength = 0;
        for (int i = 1; i <= str1.length(); i++){
            for (int j = 1; j <= str2.length(); j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)){
                    // 当前字符匹配，连续长度+1
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    maxLength = Math.max(maxLength, dp[i][j]);
                }else {
                    // 字符不匹配，连续长度重置0
                    dp[i][j] = 0;
                }
            }
        }
        // 最长公共连续字符≥3视为相似店名
        return maxLength >= 3;
    }
}
