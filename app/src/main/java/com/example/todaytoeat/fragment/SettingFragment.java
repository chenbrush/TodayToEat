package com.example.todaytoeat.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.todaytoeat.HistoryActivity;
import com.example.todaytoeat.ListActivity;
import com.example.todaytoeat.R;
import com.example.todaytoeat.VersionActivity;
import com.example.todaytoeat.adapter.SettingAdapter;
import com.example.todaytoeat.beans.SettingsBean;

import java.util.List;


public class SettingFragment extends Fragment implements AdapterView.OnItemClickListener {

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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView lv_setting = view.findViewById(R.id.lv_setting);
        lv_setting.setOnItemClickListener(this);
        List<SettingsBean> settingsBeanList = SettingsBean.getDefaultList(getContext());
        SettingAdapter adapter = new SettingAdapter(getContext(), settingsBeanList);
        lv_setting.setAdapter(adapter);
    }

    // 查询哪个按钮被点击
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i){
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
