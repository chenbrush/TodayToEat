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

import android.content.Context;

import com.example.todaytoeat.utils.HistoryManager;

import java.util.List;

public class HistoryBean {
    public String amEatHis;
    public String pmEatHis;
    public String date;

    public HistoryBean(String date, String pmEatHis, String amEatHis) {
        this.date = date;
        this.pmEatHis = pmEatHis;
        this.amEatHis = amEatHis;
    }

    public static List<HistoryBean> getDefaultList(Context context){
        return HistoryManager.getHistoryList(context);
    }
}
