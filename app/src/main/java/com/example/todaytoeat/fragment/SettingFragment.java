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

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.todaytoeat.HistoryActivity;
import com.example.todaytoeat.ListActivity;
import com.example.todaytoeat.R;
import com.example.todaytoeat.VersionActivity;
import com.example.todaytoeat.adapter.SettingAdapter;
import com.example.todaytoeat.beans.SettingsBean;

import java.util.List;


public class SettingFragment extends Fragment {

    public SettingFragment() {
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
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.setting_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        RecyclerView lv_setting = view.findViewById(R.id.lv_setting);
        // RecyclerView 需要设置纵向布局管理器
        lv_setting.setLayoutManager(new LinearLayoutManager(getContext()));
        // 单独处理列表底部导航栏 Insets，防止最后一条被系统手势区域遮挡
        ViewCompat.setOnApplyWindowInsetsListener(lv_setting, (v, insets) -> {
            Insets navigationBars = insets.getInsets(WindowInsetsCompat.Type.systemGestures());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), navigationBars.bottom);
            return insets;
        });
        List<SettingsBean> settingsBeanList = SettingsBean.getDefaultList(getContext());
        SettingAdapter adapter = new SettingAdapter(getContext(), settingsBeanList, this::openSettingPage);
        lv_setting.setAdapter(adapter);
    }

    // 处理设置项点击，根据位置跳转到对应页面
    private void openSettingPage(int position) {
        switch (position){
            case 0:
                // 历史记录
                Intent intentHistory = new Intent();
                intentHistory.setClass(requireContext(), HistoryActivity.class);
                startActivity(intentHistory);
                break;

            case 1:
                // 商铺列表
                Intent intentList = new Intent();
                intentList.setClass(requireContext(), ListActivity.class);
                startActivity(intentList);
                break;

            default:
                // 版本信息
                Intent intentVersion = new Intent();
                intentVersion.setClass(requireContext(), VersionActivity.class);
                startActivity(intentVersion);
                break;
        }

    }
}
