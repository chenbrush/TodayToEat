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
package com.example.todaytoeat.beans;

import com.example.todaytoeat.R;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class SettingsBean {
    public int icon;
    public String name;

    public SettingsBean(int iconList, String name) {
        this.icon = iconList;
        this.name = name;
    }

    private static final int[] iconArr = {
            R.drawable.baseline_history_black_24,
            R.drawable.baseline_format_list_bulleted_24,
            R.drawable.baseline_color_lens_24,
            R.drawable.baseline_info_24
    };

    private static final int[] nameResArr = {
            R.string.history,
            R.string.list,
            R.string.theme,
            R.string.version
    };

    // 传入 Context，动态获取字符串
    public static List<SettingsBean> getDefaultList(Context context) {
        List<SettingsBean> settingsBeanList = new ArrayList<>();
        for (int i = 0; i < iconArr.length; i++) {
            String name = context.getString(nameResArr[i]);
            settingsBeanList.add(new SettingsBean(iconArr[i], name));
        }
        return settingsBeanList;
    }
}
