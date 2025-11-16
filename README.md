# 📖 IReader

<div align="center">

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![GitHub release](https://img.shields.io/github/v/release/IReaderorg/IReader)](https://github.com/IReaderorg/IReader/releases)
[![Discord](https://img.shields.io/discord/YOUR_DISCORD_ID?label=Discord&logo=discord)](https://discord.gg/your-discord-invite)

**A free and open-source novel reader for Android and Desktop**

[Download](https://ireaderorg.netlify.app/download/) • [Documentation](docs/README.md) • [Discord](https://discord.gg/your-discord-invite) • [Contributing](#-contributing)

</div>

IReader is a powerful, cross-platform novel reader that brings your favorite web novels to life. With support for hundreds of sources through extensions and JavaScript plugins, offline reading, and advanced features like AI-powered text-to-speech, IReader delivers a premium reading experience without ads or tracking.

---

## ✨ Features

### Core Features
- 📱 **Cross-Platform** – Native Android and Desktop (Windows, macOS, Linux) support
- 📚 **Extensive Source Support** – Access hundreds of novel sources via extensions and JavaScript plugins
- 📥 **Offline Reading** – Download chapters and read anywhere, anytime
- 🔍 **Smart Search** – Find novels across multiple sources simultaneously
- 📖 **Library Management** – Organize with categories, filters, and sorting options
- 🔄 **Auto-Updates** – Automatic chapter updates for your library
- 💾 **Backup & Restore** – Protect your library data with cloud or local backups

### Reading Experience
- ⚙️ **Highly Customizable** – Multiple reading directions, fonts, themes, and layouts
- 🌙 **Dark Mode** – AMOLED-friendly dark theme for comfortable night reading
- 🎨 **Theme Engine** – Create and share custom color schemes
- 📏 **Reading Settings** – Adjust font size, line spacing, margins, and more
- 🔖 **Bookmarks** – Mark important passages and scenes
- 📍 **Reading Progress** – Automatic progress tracking and sync

### Advanced Features
- 🔊 **AI Text-to-Speech** (Desktop) – Natural-sounding voices powered by Piper TTS
- 🔌 **JavaScript Plugin System** – Extensible plugin architecture for maximum flexibility
- 🌐 **Multi-Language** – Interface available in multiple languages
- 🚫 **Ad-Free & Privacy-Focused** – No ads, tracking, or account required
- ⚡ **Performance Optimized** – Fast loading and smooth scrolling

---

## 🛠 Installation

### 📲 Android

**Minimum Requirements:** Android 7.0 (API 24) or higher

1. **Download the APK**
   - [GitHub Releases](https://github.com/IReaderorg/IReader/releases) (recommended)
   - [Official Website](https://ireaderorg.netlify.app/download/)

2. **Install the APK**
   - Open the downloaded file on your Android device
   - Enable "Install from Unknown Sources" if prompted
   - Follow the on-screen installation instructions

### 💻 Desktop

> 🧪 *Desktop version is under development.*

**Supported:** Windows 10+, macOS 10.15+, Linux (x64)
**Requirements:** 4GB RAM, 500MB disk space

---

## 🚀 Quick Start

### First Time Setup

1. **Launch IReader** on your device
2. **Install Extensions or Plugins**
   - Browse to **Settings → Extensions** to install [IReader Extensions](https://github.com/IReaderorg/IReader-extensions)
   - Or enable **JavaScript Plugins** for extended source support (see [JS Plugin Guide](#-javascript-plugins))
3. **Browse Catalogs** to discover novels from your installed sources
4. **Add to Library** by tapping the bookmark icon on any novel
5. **Start Reading** and customize your experience in reader settings

### Tips
- Pull down to update chapters
- Long-press books to organize into categories  
- Tap settings while reading to customize
- Use global search across all sources

---

## 🔊 Text-to-Speech (Desktop)

IReader Desktop features **Piper TTS** - neural text-to-speech with natural AI voices.

- **High-Quality Voices** – Natural speech in multiple languages
- **Offline-First** – Works completely offline once models are downloaded
- **Privacy-Focused** – All processing happens locally
- **Customizable** – Adjust speech rate, volume, and pitch

### Getting Started
1. Open a book in the reader
2. Click the TTS button (speaker icon)
3. Download a voice model
4. Start listening!

For detailed setup, see [TTS Setup Guide](docs/TTS_Setup_Guide.md).


## � JaevaScript Plugins

IReader supports JavaScript plugins for extended source access.

### Getting Started
1. Enable plugins in **Settings → JavaScript Plugins**
2. Place `.js` files in the plugins directory
3. Restart IReader to load new plugins

For detailed setup and troubleshooting, see [JS Plugin System](docs/js-plugin-system.md).

---

## 📖 Documentation

- [Getting Started](docs/wiki/Getting-Started.md) - Setup guide
- [Installing Extensions](docs/wiki/Installing-Extensions.md) - Add new sources
- [Reading Books](docs/wiki/Reading-Books.md) - Customize your experience
- [Backup and Restore](docs/wiki/Backup-and-Restore.md) - Protect your data
- [Troubleshooting](docs/wiki/Troubleshooting.md) - Common issues
- [Architecture Guide](docs/ARCHITECTURE.md) - For developers

---

## 🌍 Contributing

### 🌐 Translations

- Help translate the app via [Weblate](https://hosted.weblate.org/projects/ireader/ireader/)

### 📦 Create a New Source

- See [Contributing Guide](https://github.com/IReaderorg/IReader-extensions/blob/master/tutorial/CONTRIBUTING.md)

### 💻 Code Contributions

1. Read the [Architecture Guide](docs/ARCHITECTURE.md) to understand the project structure
2. Follow clean architecture principles
3. Write tests for new features
4. Document public APIs with KDoc
5. Submit a pull request

---

## 💬 Community & Support

- 💬 **Discord** – Join our [Discord Server](https://discord.gg/your-discord-invite) for help, updates, and discussions
- 🐛 **Bug Reports** – [Open an issue](https://github.com/IReaderorg/IReader/issues/new) on GitHub
- 💡 **Feature Requests** – Share your ideas in [GitHub Discussions](https://github.com/IReaderorg/IReader/discussions)
- 📖 **Documentation** – Check our [comprehensive docs](docs/README.md) for guides and tutorials

---

## 📄 License

This project is licensed under the [Apache 2.0 License](https://github.com/IReaderorg/IReader/blob/master/LICENSE)

---



## 🙏 Acknowledgments

- [Tachiyomi](https://github.com/tachiyomiorg/tachiyomi) – Inspiration for the architecture and design
- [Piper TTS](https://github.com/rhasspy/piper) – Neural text-to-speech engine
- All our [contributors](https://github.com/IReaderorg/IReader/graphs/contributors) and community members

---



<div align="center">

🔗 **Website:** [https://ireaderorg.netlify.app/](https://ireaderorg.netlify.app/)

Made with ❤️ by the IReader community

</div>

## Screenshots :camera:

| Views    | Dark                                                       | Light                                                        |
| -------- | ---------------------------------------------------------- | ------------------------------------------------------------ |
| Library  | ![library_view_dark](screenshots/library-dark.png)         | ![library_view_light](screenshots/library-light.png)         |
| Book     | ![book_view_dark](screenshots/detail-dark.png)               | ![book_view_light](screenshots/detail-light.png)               |
| Reader     | ![book_view_dark](screenshots/reader-dark.png)               | ![book_view_light](screenshots/reader-light.png)               |



