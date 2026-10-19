package com.example.todaytoeat.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;

public class JsonReader {
    // 工具类，不允许实例化
    public JsonReader(){}
    /** 创建 Gson 示例 **/
    private Gson gson = new Gson();
    /** 文件名称 **/
    private final String FILE_NAME = "shop_groups.json";
    /** 文件路径 **/
    private File FILE_PATH;
    /** json log **/
    private final String TAG = "JsonReader";

    /**
     * 读取json文件中的内容
     * */
    public void readJsonContext(Context context){
        FILE_PATH = AppConstantsUtils.getJsonInDataFilePath(context);
        File jsonFilePath = new File(FILE_PATH + FILE_NAME);
        if (jsonFilePath.exists()){
            FileUtil.saveText(jsonFilePath.getAbsolutePath(), "");
        }

        // 读取json文件并且实现文件逻辑
        String readJsonFileContext = FileUtil.openText(jsonFilePath.getAbsolutePath());
        Log.d(TAG, "readJsonContext: " + readJsonFileContext);
    }
}
