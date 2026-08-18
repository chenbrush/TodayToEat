# TodayToEat 项目开发规则

本项目为 Android 纯 Java 项目（应用源码目录：`app/src/main`）。以下规则适用于本项目的任何聊天与需求，每次编写代码前都必须遵循：

1. **编码格式**：所有文件统一使用 UTF-8 编码，且不带 BOM。
2. **Git 提交**：每完成一个小功能更新后，及时提交到 git，提交信息使用中文编写（例如“新增某某功能”“修复某某问题”）。
3. **代码注释**：代码中要添加相关注释，注释使用中文，内容保持简洁。
4. **开发语言**：项目使用纯 Java 开发。后期可能会使用 Kotlin，但在用户明确允许或告知可以使用 Kotlin 之前，一律使用 Java 编写代码。

## 项目简介

- 项目名称：Today To Eat（今天吃什么）
- 包名：`com.example.todaytoeat`，应用版本 1.9.2，minSdk 26 / targetSdk 37，Java 11
- 主要功能：随机选择早/晚餐店铺、商铺列表管理（添加/屏蔽/删除）、历史记录查看与修改、检查 GitHub 新版本
- 页面结构：`MainActivity` 底部导航 + ViewPager2 承载主页与设置页；另有商铺列表页、历史记录页、版本信息页
- 数据存储：店铺列表与每日就餐记录保存在应用外部存储目录的 `files` 目录下（`shop_list.txt`、`日期.txt`），配置保存在 SharedPreferences（`setting`）
- 仓库地址：https://github.com/chenbrush/TodayToEat
