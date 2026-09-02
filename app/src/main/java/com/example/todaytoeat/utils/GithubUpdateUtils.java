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
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class GithubUpdateUtils {

    /**
     * 从 GitHub 获取最新版本号
     */
    public static String getGithubUpdate() throws IOException {
        String[] getApiData = getGithub();

        String githubVersion = "";
        for (String getApiDatum : getApiData) {
            String[] getApiDataContext = getApiDatum.split(":");
            Log.d("githubContent", "getGithubUpdate: " + getApiDataContext[0]);
            if (getApiDataContext[0].equals("\"name\"")) {
                githubVersion = getApiDataContext[1];
                break;
            }
        }

        String[] githubVersionSplit = githubVersion.split("[\":v]");
        return githubVersionSplit[githubVersionSplit.length - 1];
    }

    @NonNull
    private static String[] getGithub() throws IOException {
        final String githubApi = "https://api.github.com/repos/chenbrush/TodayToEat/releases/latest";
        URL url = new URL(githubApi);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        InputStream inputStream = connection.getInputStream();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder stringBuilder = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }

        Log.d("github", stringBuilder.toString());
        return stringBuilder.toString().split(",");
    }

    /**
     * version1 是 GitHub 最新版本，version2 是当前版本
     * 如果 version1 > version2 返回 true（有新版本）
     */
    public static boolean compareVersion(String version1, String version2, Context context) {
        String[] version1Split = version1.split("\\.");
        String[] version2Split = version2.split("\\.");

        for (int i = 0; i < Math.min(version1Split.length, version2Split.length); i++) {
            int version1Int = 0;
            try {
                version1Int = Integer.parseInt(version1Split[i]);
            } catch (NumberFormatException e) {
                Toast.makeText(context, "若需要检查更新，请到项目主页面进行检查！", Toast.LENGTH_SHORT).show();
            }
            int version2Int = Integer.parseInt(version2Split[i]);

            if (version1Int > version2Int) {
                return true;
            } else if (version1Int < version2Int) {
                return false;
            }
        }

        // 如果前面都相同，位数多的为大
        return version1Split.length > version2Split.length;
    }
}
