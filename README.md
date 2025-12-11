# 📖 IReader

<div align="center">

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/IReaderorg/IReader)](https://github.com/IReaderorg/IReader/releases)

**A free and open-source novel reader for Android and Desktop**

[Download](https://github.com/IReaderorg/IReader/releases) • [Documentation](docs/README.md) • [中文](README_zh.md)

</div>

A cross-platform novel reader with support for multiple sources through extensions and JavaScript plugins. Read your favorite web novels offline with a customizable reading experience.

## ✨ Features

- 📱 Android and Desktop support
- 📚 Multiple sources via extensions and JavaScript plugins
- 📥 Offline reading with chapter downloads
- 🔍 Search across multiple sources
- 📖 Library management with categories and filters
- 🌙 Dark mode and customizable themes
- 🔊 AI Text-to-Speech (Desktop)
- 🚫 No ads or tracking

## 📲 Installation

**Requirements:** Android 7.0 or higher

### Desktop
Download the latest release from [GitHub Releases](https://github.com/IReaderorg/IReader/releases)

**Supported:** Windows, macOS, Linux

### Android
Download the latest APK from 

[<img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" alt='Get it on GitHub' height="80">](https://github.com/IReaderorg/IReader/releases)

[<img src="https://raw.githubusercontent.com/zapstore/zapstore/0b2eb07b700c477cf49b4d90cd521e1217d6a126/assets/images/badge.png" alt="Get it on Zapstore" height="60">](https://zapstore.dev/apps/naddr1qvzqqqr7pvpzq7xwd748yfjrsu5yuerm56fcn9tntmyv04w95etn0e23xrczvvraqqwxju3wddsh5etdvdhkgetn9e5kuenfde5hg7tjv4skgetjjjktdl)

[<img src="https://raw.githubusercontent.com/solkin/appteka-android/b46cf2280599f93732bb1ee76a9c12364f313444/art/badge.png" alt="Get it on Appteka" height="60">](https://appteka.store/app/936r194079)

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://github.com/IReaderorg/IReader/releases)

[<img src="https://www.openapk.net/images/openapk-badge.png" alt="Get it on OpenAPK" height="80">](https://github.com/IReaderorg/IReader/releases)

[<img src="https://www.androidfreeware.net/images/androidfreeware-badge.png" alt="Get it on AndroidFreeware" height="80">](https://github.com/IReaderorg/IReader/releases)

[<img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyOnDroid" height="80">](https://github.com/IReaderorg/IReader/releases)
</div>

[<img src="https://raw.githubusercontent.com/ImranR98/Obtainium/refs/heads/main/assets/graphics/badge_obtainium.png" alt='Get it on Obtainium' height="60">](https://github.com/IReaderorg/IReader/releases)

[<img src="https://static.apkpure.com/www/static/imgs/logo_new@2x.png" alt="Get it on Apkpure" height="40">](https://github.com/IReaderorg/IReader/releases)

[<img src="https://gitlab.com/-/project/6922885/uploads/dc268935d9bb381bdf17fc7d92fc19e0/badge.png" alt="Get it on Aurora Store" height="70">](https://github.com/IReaderorg/IReader/releases)

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png" alt="Get it on Google Play" height="80">](https://github.com/IReaderorg/IReader/releases)

[<img src="https://accrescent.app/badges/get-it-on.png" alt="Get it on Accrescent" height="80">](https://github.com/IReaderorg/IReader/releases)

## 🚀 Quick Start

### Using LNReader Sources (Recommended)

IReader now supports LNReader sources! Follow these steps:

1. **Enable JavaScript Plugins**
   - Go to **Settings → General**
   - Toggle **"Enable Javascript Plugin"**

2. **Add LNReader Repository**
   - Go to **Settings → Repository**
   - Tap the **Add icon (+)**
   - Select **Quick Add Popular Repo**
   - Choose **LNReader**
   - Save and refresh remote sources in the Source screen

3. **Storage Permissions (Optional)**
   - By default, LNReader sources are saved to app cache (no permissions needed)
   - To save sources to external storage for easier access, disable **"Saved Sources to Cache"** in Settings → General
   - If using external storage, grant storage permissions when prompted

4. **Start Reading**
   - Browse sources to find novels
   - Add books to your library
   - Download chapters for offline reading

> **Note:** IReader no longer supports the in-app default repository. You must add repositories manually in Settings.

## 📖 Documentation

- **[📚 Full Documentation Index](docs/README.md)** - Start here for all guides
- [Text-to-Speech Guide](docs/guides/tts.md)
- [Sources & Extensions](docs/guides/sources.md)
- [Sync & Backup](docs/guides/sync_backup.md)
- [Developer Documentation](docs/developer/README.md)

## 🌍 Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

- Translations: [Weblate](https://hosted.weblate.org/projects/ireader/ireader/)
- Extensions: [IReader Extensions](https://github.com/IReaderorg/IReader-extensions)

### 🍎 iOS Developer Needed

We're looking for a developer to help wire the iOS module to the main app. All the groundwork is done — the iOS module exists and the core components are ready — but the final integration work remains. @kazemcodes is unable to continue this work due to not having access to a Mac.

The app can also leverage IReader's existing sources since they are compiled to Native/JS, making the integration straightforward.

If you have iOS/macOS development experience and want to help bring IReader to iOS, please reach out!

## 📄 License

Licensed under [Apache 2.0 License](LICENSE)

## 🙏 Acknowledgments

- [LNReader](https://github.com/LNReader/lnreader) – Thank you for maintaing the plugins
- [Tachiyomi](https://github.com/tachiyomiorg/tachiyomi) – Architecture inspiration
- [Piper TTS](https://github.com/rhasspy/piper) – Text-to-speech engine
- All our [contributors](https://github.com/IReaderorg/IReader/graphs/contributors)

## Screenshots

| Library | Book Detail | Reader |
| ------- | ----------- | ------ |
| ![library](screenshots/library-screen.jpg) | ![book](screenshots/book-screen.jpg) | ![reader](screenshots/reader-screen.jpg) |

| Settings | Theme | TTS |
| -------- | ----- | --- |
| ![settings](screenshots/settting-screen.jpg) | ![theme](screenshots/theme-screen.jpg) | ![tts](screenshots/tts-screen.jpg) |

| Statistics | Leaderboard | Translation |
| ---------- | ----------- | ----------- |
| ![statistics](screenshots/static-screen.jpg) | ![leaderboard](screenshots/leaderboard-screen.jpg) | ![translation](screenshots/translation.jpg) |



