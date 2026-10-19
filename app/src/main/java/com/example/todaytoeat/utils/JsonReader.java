package com.example.todaytoeat.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 * 商铺分组 JSON 文件读写工具
 */
public class JsonReader {
    /** JSON 文件名称 */
    private static final String FILE_NAME = "shop_groups.json";
    /** 日志标签 */
    private static final String TAG = "JsonReader";

    /**
     * 获取 JSON 文件对象
     * 使用 File 构造方法拼接目录和文件名，避免漏掉路径分隔符
     */
    private File getJsonFile(Context context) {
        File directory = AppConstantsUtils.getJsonInDataFilePath(context);
        return new File(directory, FILE_NAME);
    }

    /**
     * 读取json文件中的内容
     */
    public String readJsonContext(Context context){
        File jsonFile = getJsonFile(context);
        if (!jsonFile.exists()) {
            // 文件尚未创建时返回空字符串，由调用方决定是否写入默认内容
            Log.d(TAG, "JSON 文件不存在：" + jsonFile.getAbsolutePath());
            return "";
        }

        // 读取 JSON 原始文本，不在读取过程中修改文件
        String readJsonFileContext = FileUtil.openText(jsonFile.getAbsolutePath());
        Log.d(TAG, "readJsonContext: " + readJsonFileContext);
        return readJsonFileContext;
    }

    /**
     * 写入 JSON 文本
     */
    public void writeJsonContext(Context context, String json){
        File jsonFile = getJsonFile(context);
        FileUtil.saveText(jsonFile.getAbsolutePath(), json);
    }
}
