package com.example.todaytoeat;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.todaytoeat.adapter.ListAdapter;
import com.example.todaytoeat.utils.FileUtil;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ListActivity extends AppCompatActivity implements View.OnClickListener {
    private final List<String> shopList = new ArrayList<>();
    private String path;
    private ListAdapter adapter;
    private SharedPreferences sharedPreferences;

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

        sharedPreferences = getSharedPreferences("setting", MODE_PRIVATE);
        ListView lv_list = findViewById(R.id.lv_list);
        CheckBox cb_repetition = findViewById(R.id.cb_repetition);

        loadShop();
        boolean repStatus = sharedPreferences.getBoolean("repStatus", false);

        adapter = new ListAdapter(this, shopList);
        lv_list.setAdapter(adapter);

        findViewById(R.id.ib_back).setOnClickListener(this);
        findViewById(R.id.ib_edit).setOnClickListener(this);

        // 设置初始状态，在设置监听之前，避免触发 onCheckedChanged
        cb_repetition.setChecked(repStatus);

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
                                // dialog结束后对效果进行恢复
                                card.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                                card.setCardBackgroundColor(MaterialColors.getColor(card, com.google.android.material.R.attr.colorSurface));
                            }
                        })
                        .show();

                return true;
            }
        });
    }

    // 加载商铺名称
    private void loadShop() {
        shopList.clear();
        path = Objects.requireNonNull(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)) + "/files" + File.separatorChar + "shop_list.txt";

        // 检测文件是否存在
        File file = new File(path);
        if (!file.exists()){
            FileUtil.saveText(path, getString(R.string.none_shops));
        }

        String context = FileUtil.openText(path);
        String[] arr = context.split("[,，、]");
        List<String> checkList = new ArrayList<>();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].isEmpty()){
                continue;
            }
            checkList.add(arr[i]);
        }
        String[] checkArr = new String[checkList.size()];
        for (int i = 0; i < checkList.size(); i++) {
            checkArr[i] = checkList.get(i);
        }
        shopList.addAll(Arrays.asList(checkArr));
        Log.d("shop", arr[0]);

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
                    .setView(dialogView)  // 绑定带EditText的布局
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
        for (int i = 0; i < shopList.length; i++) {
            if (shopList[i].isEmpty()){
                continue;
            }
            checkShop.append(shopList[i]).append(",");
        }
        FileUtil.saveText(path, checkShop.toString());
    }

}