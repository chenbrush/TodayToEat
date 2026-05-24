package com.example.todaytoeat.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 文件操作工具类
 * 提供文本文件的【保存】和【读取】功能
 */
public class FileUtil {

    /**
     * 把字符串写入到指定路径的文本文件中（保存文本）
     * @param path 文件的保存路径
     * @param txt 要写入文件的字符串内容
     */
    public static void saveText(String path, String txt){
        // 声明文件写入流
        BufferedWriter os = null;
        try {
            // 根据文件路径创建字符输出流
            os = new BufferedWriter(new FileWriter(path));
            // 将字符串内容写入文件
            os.write(txt);
        } catch (Exception e) {
            // 捕获文件操作异常并打印
            e.printStackTrace();
        } finally {
            // 无论是否发生异常，都要关闭流
            if (os != null) {
                try {
                    // 关闭输出流，释放资源
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从指定路径的文本文件中读取全部内容（读取文本）
     * @param path 要读取的文件路径
     * @return 读取到的文件内容字符串
     */
    public static String openText(String path) {
        // 声明文件读取流
        BufferedReader is = null;
        // 用于拼接读取到的每行文字
        StringBuilder sb = new StringBuilder();
        try {
            // 根据文件路径创建字符输入流
            is = new BufferedReader(new FileReader(path));
            String line = null;
            // 循环按行读取文件内容，直到读取完毕
            while ((line = is.readLine()) != null) {
                // 将每行内容拼接到字符串构建器中
                sb.append(line);
            }
        } catch (Exception e) {
            // 捕获文件读取异常并打印
            e.printStackTrace();
        } finally {
            // 关闭读取流，释放资源
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // 返回拼接完成的完整文件内容
        return sb.toString();
    }
}