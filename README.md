# TodayToEat - 今天吃什么

一款简洁、高颜值、完全本地离线的「随机干饭」工具，专为解决「选择困难、今天不知道吃什么」而设计。

## 项目介绍

TodayToEat 是一款纯本地运行的 Android 小工具。你可以自由维护自己的商铺库，软件会智能随机生成今日中饭、今日晚饭与下一餐推荐，全程无广告、无后台请求，数据全部保存在本地。

## 功能特性

- **一键随机**：随机生成今日中饭 + 今日晚饭，或只抽「下一餐」
- **时间智能判断**：根据当前时间动态切换文案与推荐策略，晚上 21 点后自动变为「明天全天吃」
- **昨日去重**：开启「避免重复」后，不会连续两天抽到同一家店（店铺不足 3 家时自动关闭该功能）
- **相似店名过滤**：开启「相似过滤」后，自动避开与昨日店铺名相似的店铺
- **商铺屏蔽**：屏蔽不想吃的店铺，随机选择时自动排除
- **商铺库管理**：自由添加、编辑、删除商铺
- **历史记录**：按 7 / 10 / 15 / 30 天查看就餐历史，长按可修改单日记录
- **更新检查**：自动检查 GitHub 最新版本并提示更新

## 页面结构

| 页面 | 说明 |
| --- | --- |
| 主页 | 随机推荐入口，展示今日早 / 晚餐结果 |
| 设置页 | 历史记录、商铺列表、版本信息入口 |
| 商铺列表页 | 添加 / 编辑 / 屏蔽 / 删除商铺 |
| 历史记录页 | 查看与修改历史就餐记录 |
| 版本信息页 | 版本号、更新日志、检查更新 |

## 技术栈

- 纯 Java 开发（Java 11）
- minSdk 28 / targetSdk 37 / compileSdk 37
- Android Gradle Plugin 9.1.0
- 主要依赖：AndroidX（AppCompat、Activity、ConstraintLayout、ViewPager2）、Material Components、Intuit sdp

## 数据存储

- 商铺列表：应用外部存储目录下的 `files/shop_list.txt`
- 每日就餐记录：应用外部存储目录下的 `files/日期.txt`（如 `2026-08-26.txt`）
- 配置项：SharedPreferences（`setting`），包括屏蔽列表、去重 / 相似过滤开关、历史天数等

## 构建运行

使用 Android Studio 打开项目，等待 Gradle 同步完成后直接运行 `app` 模块即可。

## 下载

最新 APK 请前往 GitHub Releases 获取：

[![Android APK](https://img.shields.io/badge/Android-APK-green?logo=android)](https://github.com/chenbrush/TodayToEat/releases/latest)

## 更新日志

历史版本更新内容见 [版本更新日志.txt](./版本更新日志.txt)。

## 开源协议

本项目基于 [Apache License 2.0](./LICENSE) 开源，第三方依赖的许可证声明见 [THIRD_PARTY_LICENSES.md](./THIRD_PARTY_LICENSES.md)。