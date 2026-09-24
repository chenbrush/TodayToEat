/*
 * Copyright (c) 2026 chenbrush
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.todaytoeat.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.todaytoeat.ListActivity;
import com.example.todaytoeat.R;
import com.example.todaytoeat.utils.FileUtil;
import com.example.todaytoeat.utils.HistoryManager;
import com.example.todaytoeat.utils.PreferenceKeys;
import com.example.todaytoeat.utils.ShopListUtils;
import com.example.todaytoeat.utils.TemporaryBlockUtils;
import com.example.todaytoeat.utils.ThemesMangerUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
    private boolean allShopsBlocked;
    private SharedPreferences sharedPreferences;
    private boolean repStatus;
    private boolean similar;
    // 缓存的被屏蔽商铺集合（在 loadAvailableShops 中读取一次，供 isShopBlocked 使用）
    private Set<String> hideShopsSet;
    // 缓存的当天临时屏蔽商铺集合（同样在 reloadShop 中读取，跨天后会自动失效）
    private Set<String> tempBlockedShops = Collections.emptySet();

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
        tvResult_first.setOnLongClickListener(this::onTextLongClickChoice);
        tvResult_second.setOnLongClickListener(this::onTextLongClickChoice);

        // 定义文件位置及名称
        directory = requireActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/files";

        // 校验昨日历史记录文件，不存在则自动创建
        HistoryManager.ensureYesterdayFileExists(requireContext());

        // 应用卡片高度设置到主页卡片
        ThemesMangerUtils.applyAllCardElevation(requireContext(), view);

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
        sharedPreferences = requireActivity().getSharedPreferences(PreferenceKeys.PREFS_NAME, MODE_PRIVATE);
        // 获取是否禁止昨日重复店铺，默认关闭false
        repStatus = sharedPreferences.getBoolean(PreferenceKeys.KEY_REPETITION_STATUS, false);
        // 获取是否过滤相似店名，默认关闭false
        similar = sharedPreferences.getBoolean(PreferenceKeys.KEY_SIMILAR, false);
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
                if (lines[0].equals(getString(R.string.only_am)) || lines[0].equals(getString(R.string.am_eat))) {
                    line1 = getString(R.string.am_eat) + "："+ lines[1];
                }else if (lines[0].equals(getString(R.string.only_pm)) || lines[0].equals(getString(R.string.pm_eat))){
                    line1 = getString(R.string.pm_eat) + "：" + lines[1];
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
        allDay.setText(lt.getHour() >= 21 ? R.string.tomorrow_all_day : R.string.today_all_day);
    }

    // 读取店铺列表
    private void reloadShop() {
        refreshAvailableShops();

        // 重算后没有可抽取的商铺，提示用户去列表页处理
        if (!shopsListExist) {
            if (allShopsBlocked) {
                noticeAllShopsBlocked();
            } else {
                noticeToAddShops();
            }
        }
    }

    /**
     * 重新计算可抽取的商铺，结果写入 shop、shopsListExist、allShopsBlocked
     * 屏蔽或删除商铺后调用它可以立即刷新随机队列，且不弹任何提示
     */
    private void refreshAvailableShops() {
        List<String> availableShops = loadAvailableShops();

        // 店铺文件不存在或内容无效，视为没有店铺
        if (availableShops == null) {
            shopsListExist = false;
            allShopsBlocked = false;
            shop = null;
            return;
        }

        // 所有商铺都被屏蔽，随机队列为空
        if (availableShops.isEmpty()) {
            shopsListExist = false;
            allShopsBlocked = true;
            shop = null;
            return;
        }

        shopsListExist = true;
        allShopsBlocked = false;
        shop = availableShops.toArray(new String[0]);
    }

    /**
     * 读取店铺文件，过滤掉空店名、永久屏蔽以及当天临时屏蔽的商铺
     *
     * @return 可抽取的商铺列表；店铺文件不存在或内容无效时返回 null
     */
    private List<String> loadAvailableShops() {
        String pathShop = directory + File.separatorChar + "shop_list.txt";
        File fileShop = new File(pathShop);
        String content = FileUtil.openText(pathShop);

        // 校验文件有效性：文件不存在、内容为空或仍是初始提示文字，都视为没有店铺
        // 空内容统一走 ShopListUtils 判断，兼容切换系统语言后旧数据里写入的其它语种提示文字
        if (!fileShop.exists() || ShopListUtils.isEmptyContent(requireContext(), content)) {
            return null;
        }

        // 读取屏蔽数据：永久屏蔽集合（与 ListActivity 保存的键名保持一致）
        // 与当天有效的临时屏蔽集合（跨天的记录会在读取时自动清除）
        SharedPreferences sp = requireActivity().getSharedPreferences(PreferenceKeys.PREFS_NAME, MODE_PRIVATE);
        hideShopsSet = sp.getStringSet(PreferenceKeys.KEY_HIDE_SHOPS, null);
        tempBlockedShops = TemporaryBlockUtils.getTempBlockedShops(requireContext());

        // 使用与 ListActivity 相同的分隔符拆分店名，保证已经屏蔽的商铺不会进入随机选择队列
        List<String> availableShops = new ArrayList<>();
        for (String shopName : content.split("[,，、]")) {
            if (shopName.isEmpty() || isShopBlocked(shopName)) {
                continue;
            }
            availableShops.add(shopName);
        }
        return availableShops;
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

        // 读取今天已经记录的两餐，作为判断"下一餐该记到哪个时间段"的依据
        // 注意：清空记录时写入的是 "null：没有记录：null：没有记录" 这类占位内容，不算真正的一餐
        String todayContent = FileUtil.openText(HistoryManager.getTodayFilePath(requireContext()));
        HistoryManager.Record todayRecord = HistoryManager.parseHistory(requireContext(), todayContent);
        boolean noRecord = todayContent.isEmpty() || todayContent.contains("null");
        String todayAmEat = noRecord ? "" : todayRecord.amEat;
        String todayPmEat = noRecord ? "" : todayRecord.pmEat;

        // 获取时间，判断时间是否在下午（14点之后21点之前，下一餐算晚饭）
        lt = LocalTime.now();
        boolean inAfternoon = lt.getHour() > 14 && lt.getHour() < 21;

        // 不是下午且今天还没有晚饭记录时，沿用原来的弹窗，由用户决定这顿算中饭还是晚饭
        if (!inAfternoon && todayPmEat.isEmpty()) {
            String finalNowEat = randomGetShop(amEaten, pmEaten, "");
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.notice))
                    .setMessage(R.string.notice_what_time_to_eat_next_time)
                    .setPositiveButton(R.string.time_mid, (dialogInterface, i) -> saveAndRefresh(finalNowEat, todayPmEat))
                    .setNegativeButton(R.string.time_night, (dialogInterface, i) -> saveAndRefresh(todayAmEat, finalNowEat))
                    .show();
            return;
        }

        // 下午：下一餐更新晚饭，中饭保持不变；其他时间：下一餐更新中饭，晚饭保持不变
        // 去重只比较"另一时间段"的那一餐，避免下一餐和另一时间段的那一餐同步
        String nextEat = randomGetShop(amEaten, pmEaten, inAfternoon ? todayAmEat : todayPmEat);
        saveAndRefresh(inAfternoon ? todayAmEat : nextEat, inAfternoon ? nextEat : todayPmEat);

    }

    /**
     * 下一餐随机获取商铺
     * 依次过滤：昨日重复、相似店名、已屏蔽商铺，以及当天另一餐已经选过的商铺
     *
     * @param amEaten  昨日中饭店名
     * @param pmEaten  昨日晚饭店名
     * @param otherEat 当天另一餐的店名（为空表示不需要去重）
     * @return 抽到的店铺名
     * */
    private String randomGetShop(String amEaten, String pmEaten, String otherEat) {
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

            // 判断当前选择的商铺是不是已屏蔽的商铺
            if (isShopBlocked(nowEat)) continue;

            // 同一天的两餐不能是同一家，避免下一餐和另一时间段的那一餐同步
            if (!otherEat.isEmpty() && otherEat.equals(nowEat)) continue;

            // 通过所有过滤条件
            break;
        }
        return nowEat;
    }

    /**
     * 保存当天记录并刷新界面
     *
     * @param amEatToday 今天的中饭店名（为空表示没有记录）
     * @param pmEatToday 今天的晚饭店名（为空表示没有记录）
     */
    private void saveAndRefresh(String amEatToday, String pmEatToday) {
        String record;
        if (amEatToday.isEmpty()) {
            // 只有晚饭，按"仅吃晚饭"保存
            record = getString(R.string.only_pm) + "：" + pmEatToday;
        } else if (pmEatToday.isEmpty()) {
            // 只有中饭，按"仅吃中饭"保存
            record = getString(R.string.only_am) + "：" + amEatToday;
        } else {
            // 中饭、晚饭都有，保存完整的两餐
            record = getString(R.string.am_eat) + "：" + amEatToday + "：" + getString(R.string.pm_eat) + "：" + pmEatToday;
        }

        HistoryManager.saveTodayRecord(requireContext(), record);
        // 按保存后的记录刷新界面，单行/双行以及标签都会自动适配
        reloadShow();
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
                    sharedPreferences.edit().putBoolean(PreferenceKeys.KEY_REPETITION_STATUS, false).apply();
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

            // 早晚店铺不能一致且都不是已屏蔽的商铺，满足则退出循环
            if (!amEat.equals(pmEat) && !isShopBlocked(amEat) && !isShopBlocked(pmEat)) break;
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
                if (allShopsBlocked) {
                    noticeAllShopsBlocked();
                } else {
                    noticeToAddShops();
                }
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
                if (allShopsBlocked) {
                    noticeAllShopsBlocked();
                } else {
                    noticeToAddShops();
                }
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
     * 所有商铺都被屏蔽时的提示弹窗，引导用户前往列表页重新启用
     */
    private void noticeAllShopsBlocked() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.notice))
                .setMessage(R.string.notice_all_shops_blocked_message)
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                    // 跳转到商铺列表页，用户可以点击被屏蔽的商铺重新启用
                    Intent intent = new Intent();
                    intent.setClass(requireActivity(), ListActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /**
     * 设置长按选择弹窗，根据弹窗进行操作的选择
     * */
    private boolean onTextLongClickChoice(View view){
        String[] options = {getString(R.string.main_change), getString(R.string.temp_block_menu_item)};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.temp_block_choice_title)
                .setItems(options, (dialogInterface, i) -> {
                    if (i == 0){
                        onResultLongClick();
                    }else {
                        onTemporaryBlockShop();
                    }
                })
                .show();

        return true;
    }

    /**
     * 临时屏蔽商铺
     * */
    private void onTemporaryBlockShop(){
        // 没有可选商铺时（未添加店铺或全部被屏蔽），直接弹提示引导到列表页
        if (shop == null || shop.length == 0) {
            if (allShopsBlocked) {
                noticeAllShopsBlocked();
            } else {
                noticeToAddShops();
            }
            return;
        }

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.temporary_block_dialog, null);
        Spinner sp_temp_block = dialogView.findViewById(R.id.sp_temp_block);

        // shop 已在 reloadShop 中过滤掉被屏蔽的商铺，这里直接作为下拉选项
        List<String> normalShops = new ArrayList<>(Arrays.asList(shop));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, normalShops);
        // 下拉展开时的条目布局
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_temp_block.setAdapter(adapter);

        new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setTitle(R.string.temp_block_title)
                .setMessage(R.string.temp_block_message)
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                    // 读取下拉框中当前选中的商铺
                    Object selectedShop = sp_temp_block.getSelectedItem();
                    if (selectedShop != null) {
                        temporaryBlockShop(selectedShop.toString());
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .setOnDismissListener(null)
                .show();
    }

    /**
     * 执行临时屏蔽
     * 记录商铺名与所属就餐日，跨天后由 TemporaryBlockUtils 自动解除
     *
     * @param shopName 下拉框中选中的商铺名
     * */
    private void temporaryBlockShop(String shopName) {
        Log.d("tempBlock", "临时屏蔽商铺：" + shopName);
        TemporaryBlockUtils.addTempBlockedShop(requireContext(), shopName);
        // 重算随机队列：临时屏蔽的商铺立即从随机选店与下拉框中移除
        refreshAvailableShops();
        // 提示用户已经生效
        Toast.makeText(requireContext(), getString(R.string.temp_block_success, shopName), Toast.LENGTH_SHORT).show();
    }


    /**
     * 长按结果文本弹出修改弹窗（修改当天记录）
     * 逻辑：读取当天历史记录回显到输入框 -> 用户修改 -> 保存并刷新UI
     */
    private void onResultLongClick() {
        // 加载修改弹窗布局（复用 history_dialog.xml）
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.history_dialog, null);
        EditText etInputAmEat = dialogView.findViewById(R.id.et_input_am_eat);
        EditText etInputPmEat = dialogView.findViewById(R.id.et_input_pm_eat);

        // 读取当天历史记录回显到输入框
        String todayFilePath = HistoryManager.getTodayFilePath(requireContext());
        String content = FileUtil.openText(todayFilePath);
        HistoryManager.Record todayRecord = HistoryManager.parseHistory(requireContext(), content);

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
                .setPositiveButton(R.string.history_confirm, (dialogInterface, i) -> {
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
                })
                .setNegativeButton(getString(R.string.history_cancel_change), null)
                .show();

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


    /**
     * 判断商铺是否已被屏蔽（永久屏蔽或当天临时屏蔽）
     *
     * @param shopName 受检查的商铺
     * @return 已屏蔽返回 true，可以参与随机选店返回 false
     * */
    private boolean isShopBlocked(String shopName){
        // 永久屏蔽：只查缓存集合，不做任何修改
        if (hideShopsSet != null && hideShopsSet.contains(shopName)) {
            return true;
        }
        // 临时屏蔽：只查当天有效的缓存集合
        return tempBlockedShops.contains(shopName);
    }
}
