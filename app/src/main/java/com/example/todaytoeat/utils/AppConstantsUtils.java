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
package com.example.todaytoeat.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class AppConstantsUtils {
    // 你指定的外部存储路径
    // getExternalFilesDir(DIRECTORY_DOWNLOADS) + "/files"
    public static String getAppDownloadDirectory(Context context) {
        File downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        // 拼接成你要的目录：.../Download/files
        File directory = new File(downloadDir, "files");

        // 如果目录不存在，自动创建
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return directory.getAbsolutePath();
    }

    // 获取 日期.txt 文件完整路径（你最需要的！）
    public static String getDateFilePath(Context context, String date) {
        String dir = getAppDownloadDirectory(context);
        return dir + File.separatorChar + date + ".txt";
    }
}
