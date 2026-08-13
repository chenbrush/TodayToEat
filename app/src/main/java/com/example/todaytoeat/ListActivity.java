package com.example.todaytoeat;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todaytoeat.adapter.ListAdapter;
import com.example.todaytoeat.utils.FileUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ListActivity extends AppCompatActivity implements View.OnClickListener {
    private final List<String> shopList = new ArrayList<>();
    private String path;
    private ListAdapter adapter;
    private SharedPreferences sharedPreferences;
    Set<String> hideShopsSet = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.list_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // shared preferences 相关设置
        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        // 加载历史屏蔽记录到内存集合，避免重启后丢失（getStringSet 可能为 null，需要判空）
        Set<String> savedHideShops = sharedPreferences.getStringSet("HideShops", null);
        if (savedHideShops != null) {
            hideShopsSet.addAll(savedHideShops);
        }

        ListView lv_list = findViewById(R.id.lv_list);
        CheckBox cb_repetition = findViewById(R.id.cb_repetition);
        CheckBox cb_similar = findViewById(R.id.cb_similar);

        loadShop();
        
        adapter = new ListAdapter(this, shopList);
        lv_list.setAdapter(adapter);

        findViewById(R.id.ib_back).setOnClickListener(this);
        findViewById(R.id.ib_edit).setOnClickListener(this);

        // 设置初始状态，在设置监听之前，避免触发 onCheckedChanged
        cb_repetition.setChecked(sharedPreferences.getBoolean("repStatus", false));
        cb_similar.setChecked(sharedPreferences.getBoolean("similar", false));

        cb_repetition.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
                sharedPreferences.edit().putBoolean("repStatus", b).apply();
                // 如果商铺小于四个并且单选框为选中状态，会导致程序崩溃，所以要解决这个问题
                if (b && shopList.size() < 4){
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ListActivity.this)
                            .setTitle(getString(R.string.notice))
                            .setMessage(R.string.notice_repeat_boom_message)
                            .setPositiveButton(R.string.notice_repeat_boom_positive_botton, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    sharedPreferences.edit().putBoolean("repStatus", false).apply();
                                    cb_repetition.setChecked(false);
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

        // 设置相似商铺
        cb_similar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean b) {
                // 还有一个取消
                if (!b){
                    sharedPreferences.edit().putBoolean("similar", false).apply();
                    return;
                }

                // 如果商铺小于3个并且单选框为选中状态，会导致程序崩溃，所以要解决这个问题
                if (shopList.size() < 3) {
                    // 直接更改
                    sharedPreferences.edit().putBoolean("similar", false).apply();
                    cb_repetition.setChecked(false);

                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ListActivity.this)
                            .setTitle(getString(R.string.notice))
                            .setMessage(R.string.notice_repeat_boom_message)
                            .setPositiveButton(R.string.notice_repeat_boom_positive_botton, null);

                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return;
                }

                // 得先做一个提示，确保用户知道效果
                new MaterialAlertDialogBuilder(ListActivity.this)
                        .setTitle(getString(R.string.notice))
                        .setMessage(R.string.list_notice_similar_message)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                sharedPreferences.edit().putBoolean("similar", true).apply();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();

            }
        });
                
        // 长按删除文件
        lv_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                MaterialCardView card = view.findViewById(R.id.card_root);

                new MaterialAlertDialogBuilder(ListActivity.this)
                        .setTitle(R.string.notice_delete_shop)
                        .setMessage(R.string.notice_delete_shop_confirm)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i2) {
                                // 获取需要删除的店铺并进行删除
                                String deleteShop = shopList.get(i);
                                shopList.remove(deleteShop);
                                // 同步从屏蔽集合中移除并保存，避免残留屏蔽记录
                                hideShopsSet.remove(deleteShop);
                                sharedPreferences.edit().putStringSet("HideShops", hideShopsSet).apply();

                                // 对删除过后的店铺进行重新整理并且更新文件
                                StringBuilder updateShops = new StringBuilder();
                                // 如果商铺全都没了，那就换回初始值
                                if (shopList.isEmpty()){
                                    updateShops.append(getString(R.string.none_shops));
                                }else {
                                    for (int i1 = 0; i1 < shopList.size(); i1++) {
                                        updateShops.append(shopList.get(i1)).append(",");
                                    }
                                }

                                FileUtil.saveText(path, updateShops.toString());
                                loadShop();
                                adapter.notifyDataSetChanged();

                            }
                        })
                        .setNegativeButton(R.string.notice_delete_shop_nagative_bottom, null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                // dialog结束后只恢复按压动画（背景色统一由 ListAdapter 控制）
                                card.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                            }
                        })
                        .show();

                return true;
            }
        });

        // 单点屏蔽商铺
        lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private MaterialCardView card;

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                card = view.findViewById(R.id.card_root);

                // 检查当前商铺是否为被屏蔽商铺
                if (hideShopsSet.contains(shopList.get(i))) {
                    rehideShopNotice(i);
                } else {
                    hideShopNotice(i);
                }

            }

            // 选择屏蔽商铺提示框
            private void hideShopNotice(int i) {
                new MaterialAlertDialogBuilder(ListActivity.this)
                        .setTitle(R.string.block_shop)
                        .setMessage(R.string.block_shop_message)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i2) {
                                hideShopsSet.add(shopList.get(i));
                                sharedPreferences.edit().putStringSet("HideShops", hideShopsSet).apply();
                                loadShop();
                                adapter.notifyDataSetChanged();

                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                // dialog结束后只恢复按压动画（背景色统一由 ListAdapter 控制）
                                card.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                            }
                        })
                        .show();
            }

            // 解除屏蔽提示框
            private void rehideShopNotice(int i) {
                new MaterialAlertDialogBuilder(ListActivity.this)
                        .setTitle(R.string.unblock_shop)
                        .setMessage(R.string.unblock_shop_message)
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i2) {
                                hideShopsSet.remove(shopList.get(i));
                                sharedPreferences.edit().putStringSet("HideShops", hideShopsSet).apply();
                                loadShop();
                                adapter.notifyDataSetChanged();

                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                // dialog结束后只恢复按压动画（背景色统一由 ListAdapter 控制）
                                card.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                            }
                        })
                        .show();
            }
        });
    }

    // 加载商铺名称
    private void loadShop() {
        shopList.clear();
        path = Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)) + "/files" + File.separatorChar + "shop_list.txt";

        // 检测文件是否存在，不存在则创建并写入初始提示文字
        File file = new File(path);
        if (!file.exists()){
            FileUtil.saveText(path, getString(R.string.none_shops));
        }

        String content = FileUtil.openText(path);
        // 文件为空或仍是“未添加任何商铺”的初始提示时，说明列表为空，直接返回
        if (content.isEmpty() || content.equals(getString(R.string.none_shops))){
            return;
        }

        // 用逗号/中文逗号/顿号拆分商铺名，并过滤掉空字符串
        String[] arr = content.split("[,，、]");
        for (String shopName : arr) {
            if (!shopName.isEmpty()) {
                shopList.add(shopName);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ib_back){
            // 传递之后结束这个activity
            finish();
        } else if (view.getId() == R.id.ib_edit) {
            // 加载自定义布局
            View dialogView = LayoutInflater.from(this).inflate(R.layout.list_dialog, null);
            EditText etInput = dialogView.findViewById(R.id.et_input);

            // 构建弹窗
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.add_shop)
                    .setView(dialogView)
                    .setPositiveButton(getString(R.string.ok), (dialog, which) -> {
                        // 获取输入的文本
                        String content = etInput.getText().toString().trim();
                        // 后续：保存到文件、刷新ListView
                        if (content.isEmpty()){
                            return;
                        }

                        addNewShop(content);
                        loadShop();
                        adapter.notifyDataSetChanged();

                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    // 添加店铺
    private void addNewShop(String shopName){
        String shops = FileUtil.openText(path);

        if (shops.equals(getString(R.string.none_shops)) || shops.isEmpty()){
            shops = shopName + ",";
        }else {
            shops += shopName + ",";
        }

        String[] shopList = shops.split(",");
        StringBuilder checkShop = new StringBuilder();
        for (String s : shopList) {
            if (s.isEmpty()) {
                continue;
            }
            checkShop.append(s).append(",");
        }
        FileUtil.saveText(path, checkShop.toString());
    }

}