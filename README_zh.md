# 📖 IReader

<div align="center">

[![许可证](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![GitHub 发布](https://img.shields.io/github/v/release/IReaderorg/IReader)](https://github.com/IReaderorg/IReader/releases)

**免费开源的 Android 和桌面端小说阅读器**

[下载](https://github.com/IReaderorg/IReader/releases) • [文档](docs/README.md) • [English](README.md)

</div>

一款跨平台小说阅读器，通过扩展和 JavaScript 插件支持多种书源。可离线阅读您喜爱的网络小说，并享受可自定义的阅读体验。

## ✨ 功能特点

- 📱 支持 Android 和桌面端
- 📚 通过扩展和 JavaScript 插件支持多种书源
- 📥 下载章节离线阅读
- 🔍 跨多个书源搜索
- 📖 书库管理，支持分类和筛选
- 🌙 深色模式和可自定义主题
- 🔊 AI 文字转语音（桌面端）
- 🚫 无广告、无追踪

## 📲 安装

### Android
从 [GitHub Releases](https://github.com/IReaderorg/IReader/releases) 下载最新 APK

**系统要求：** Android 7.0 或更高版本

### 桌面端
从 [GitHub Releases](https://github.com/IReaderorg/IReader/releases) 下载最新版本

**支持系统：** Windows、macOS、Linux

## 🚀 快速开始

### 使用 LNReader 书源（推荐）

IReader 现已支持 LNReader 书源！请按以下步骤操作：

1. **启用 JavaScript 插件**
   - 前往 **设置 → 通用**
   - 开启 **"启用 Javascript 插件"**

2. **添加 LNReader 仓库**
   - 前往 **设置 → 仓库**
   - 点击 **添加图标 (+)**
   - 选择 **快速添加热门仓库**
   - 选择 **LNReader**
   - 保存并在书源页面刷新远程书源

3. **存储权限（可选）**
   - 默认情况下，LNReader 书源保存到应用缓存（无需权限）
   - 如需将书源保存到外部存储以便访问，请在 设置 → 通用 中关闭 **"将书源保存到缓存"**
   - 如使用外部存储，请在提示时授予存储权限

4. **开始阅读**
   - 浏览书源查找小说
   - 将书籍添加到书库
   - 下载章节离线阅读

> **注意：** IReader 不再支持应用内默认仓库。您必须在设置中手动添加仓库。

## 📖 文档

- **[📚 完整文档索引](docs/README.md)** - 从这里开始查看所有指南
- [文字转语音指南](docs/guides/tts.md)
- [书源与扩展](docs/guides/sources.md)
- [同步与备份](docs/guides/sync_backup.md)
- [开发者文档](docs/developer/README.md)

## 🌍 参与贡献

欢迎贡献！请查看 [CONTRIBUTING.md](CONTRIBUTING.md) 了解贡献指南。

- 翻译：[Weblate](https://hosted.weblate.org/projects/ireader/ireader/)
- 扩展：[IReader Extensions](https://github.com/IReaderorg/IReader-extensions)

## 📄 许可证

基于 [Apache 2.0 许可证](LICENSE) 开源

## 🙏 致谢

- [LNReader](https://github.com/LNReader/lnreader) – 感谢维护插件
- [Tachiyomi](https://github.com/tachiyomiorg/tachiyomi) – 架构灵感来源
- [Piper TTS](https://github.com/rhasspy/piper) – 文字转语音引擎
- 所有 [贡献者](https://github.com/IReaderorg/IReader/graphs/contributors)

## 截图

| 书库 | 书籍详情 | 阅读器 |
| ---- | -------- | ------ |
| ![书库](screenshots/library-screen.jpg) | ![书籍](screenshots/book-screen.jpg) | ![阅读器](screenshots/reader-screen.jpg) |

| 设置 | 主题 | 文字转语音 |
| ---- | ---- | ---------- |
| ![设置](screenshots/settting-screen.jpg) | ![主题](screenshots/theme-screen.jpg) | ![文字转语音](screenshots/tts-screen.jpg) |

| 统计 | 排行榜 | 翻译 |
| ---- | ------ | ---- |
| ![统计](screenshots/static-screen.jpg) | ![排行榜](screenshots/leaderboard-screen.jpg) | ![翻译](screenshots/translation.jpg) |
