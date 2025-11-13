# 📖 IReader

**IReader** is a free and open-source novel reader available for both Android and Desktop platforms. It offers a customizable reading experience with features like multiple reading directions and extensive personalization options.

---

## 🚀 Features

- 📱 **Cross-Platform** – Android & Desktop support
- ⚙️ **Customizable Reading Experience** – Multiple reading directions, fonts, themes
- 🔌 **Extension Support** – Add or create new sources
- 📥 **Offline Reading** – Download novels and read without internet
- 🚫 **Ad-Free** – Clean and distraction-free reading
- 🔒 **Privacy-Focused** – No tracking or account required
- 🌙 **Dark Mode** – Comfortable reading in low light
- 🔊 **Text-to-Speech** – Listen to novels with natural AI voices (Desktop)

---

## 🛠 Installation

### 📲 Android

1. **Download the APK**
   - From [GitHub Releases](https://github.com/IReaderorg/IReader/releases)
   - Or from the [Official Website](https://ireaderorg.netlify.app/download/)

2. **Install the APK**
   - Open the downloaded file on your Android device
   - Enable "Install from Unknown Sources" if prompted

### 💻 Desktop

> 🧪 *Desktop version is still under development.*
> Stay tuned to the [GitHub repo](https://github.com/IReaderorg/IReader) for future releases.

**Supported Platforms:**
- Windows 10 or later (x64)
- macOS 10.15 (Catalina) or later (x64)
- Linux (x64) with ALSA or PulseAudio

**System Requirements:**
- 4GB RAM minimum (8GB recommended)
- 500MB free disk space (plus additional space for TTS voice models)
- Working audio output device (for TTS features)

---

## 📚 Usage

1. **Launch the App**
2. **Add Novels**
   - Use the in-app browser to find and download novels
3. **Customize**
   - Open settings to change reading direction, themes, fonts, etc.
4. **Install Extensions**
   - Use [IReader Extensions](https://github.com/IReaderorg/IReader-extensions) to access more content

---

## 🔊 Text-to-Speech (Desktop)

IReader Desktop features **Piper TTS** - an advanced neural text-to-speech system that brings your books to life with natural-sounding AI voices.

### ✨ Key Features

- **🎯 High-Quality Neural Voices** – Natural, expressive speech using AI technology
- **🌐 Offline-First** – Works completely offline once voice models are downloaded
- **🔒 Privacy-Focused** – All processing happens locally, no data sent to external servers
- **🌍 Multi-Language Support** – Voices available in English, Spanish, French, German, and more
- **⚡ Responsive Controls** – Pause, resume, and navigate with <200ms response time
- **📍 Word Highlighting** – Follow along visually as each word is spoken
- **🎚️ Customizable** – Adjust speech rate, volume, and pitch to your preference
- **💾 Efficient** – Optimized resource usage with lazy model loading

### 🚀 Getting Started with TTS

1. **Open a Book** in the reader
2. **Click the TTS Button** (speaker icon) in the toolbar
3. **Download a Voice Model** from the Voice Model Manager
4. **Select Your Voice** and start listening!

### 📥 Voice Models

Voice models are neural networks that generate speech. Each model has different characteristics:

- **Languages**: English (US/UK), Spanish, French, German, Italian, Portuguese, and more
- **Quality Levels**: 
  - Low (~20-30MB) - Fast, good quality
  - Medium (~40-60MB) - Balanced, natural sound
  - High (~80-120MB) - Most expressive and natural
- **Voice Types**: Male, Female, and Neutral voices available

**Recommended Starting Models:**
- English (US): `en_US-lessac-medium` (Female, high quality)
- English (UK): `en_GB-alan-medium` (Male, natural)

### 🎮 Playback Controls

- **Play/Pause** – Start or pause reading
- **Stop** – End playback and return to beginning
- **Next/Previous Paragraph** – Navigate through text
- **Next/Previous Chapter** – Jump between chapters
- **Speed Control** – Adjust from 0.5x to 2.0x speed
- **Volume Control** – Independent TTS volume

### 🛠️ Technical Details

**Powered by Piper TTS:**
- Open-source neural TTS using VITS architecture
- ONNX Runtime for cross-platform compatibility
- Hardware acceleration support (CPU SIMD, GPU when available)
- Memory-efficient with <500MB usage cap

**Audio Backend:**
- Windows: WASAPI (low-latency)
- macOS: Core Audio
- Linux: ALSA/PulseAudio

### 📖 Documentation

- **[TTS Setup Guide](docs/TTS_Setup_Guide.md)** – Complete guide to downloading and configuring voice models
- **[TTS Troubleshooting Guide](docs/TTS_Troubleshooting_Guide.md)** – Solutions to common TTS issues

### 🔄 Fallback Mode

If TTS encounters issues (no models, audio device unavailable, etc.), it automatically switches to simulation mode, allowing you to continue using the app while you resolve the issue.

---

## 📖 Documentation

Check out our comprehensive wiki for detailed guides:

- [Getting Started](docs/wiki/Getting-Started.md) - Quick setup guide for new users
- [Installing Extensions](docs/wiki/Installing-Extensions.md) - How to add new sources
- [Adding Books](docs/wiki/Adding-Books.md) - Finding and adding books to your library
- [Categories](docs/wiki/Categories.md) - Organizing your book collection
- [Reading Books](docs/wiki/Reading-Books.md) - Customize your reading experience
- [Backup and Restore](docs/wiki/Backup-and-Restore.md) - Protecting your library data
- [Troubleshooting](docs/wiki/Troubleshooting.md) - Solutions to common issues
- [FAQ](docs/wiki/FAQ.md) - Frequently asked questions

### Text-to-Speech Documentation

- [TTS Setup Guide](docs/TTS_Setup_Guide.md) - How to download and configure voice models
- [TTS Troubleshooting Guide](docs/TTS_Troubleshooting_Guide.md) - Solutions to common TTS issues

### Developer Documentation

- [UI Improvements Guide](docs/UI_Improvements_Guide.md) - Comprehensive guide to the enhanced UI components and improvements

---

## 📖 Developer Documentation

Comprehensive documentation for developers is available in the [`docs/`](docs/) directory:

### Architecture & Design
- **[Architecture Guide](docs/ARCHITECTURE.md)** - Clean architecture principles, module structure, and development guidelines
- **[Module Dependencies](docs/MODULE_DEPENDENCIES.md)** - Detailed dependency graph and module relationships
- **[Build Optimization](docs/BUILD_OPTIMIZATION.md)** - Build configuration, dependency management, and performance tips

### Quick Links
- [Documentation Index](docs/README.md) - Complete documentation overview
- [Quick Start for Developers](docs/ARCHITECTURE.md#quick-start)
- [Adding New Features](docs/ARCHITECTURE.md#adding-a-new-feature)

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

- Join our [Discord Server](https://discord.gg/your-discord-invite) for help, updates, and community discussions

---

## 📄 License

This project is licensed under the [Apache 2.0 License](https://github.com/IReaderorg/IReader/blob/master/LICENSE)

---

🔗 **Website:** [https://ireaderorg.netlify.app/](https://ireaderorg.netlify.app/)

## Disclaimer

The developer of this application does not have any affiliation with the content providers available.

## Screenshots :camera:

| Views    | Dark                                                       | Light                                                        |
| -------- | ---------------------------------------------------------- | ------------------------------------------------------------ |
| Library  | ![library_view_dark](screenshots/library-dark.png)         | ![library_view_light](screenshots/library-light.png)         |
| Book     | ![book_view_dark](screenshots/detail-dark.png)               | ![book_view_light](screenshots/detail-light.png)               |
| Reader     | ![book_view_dark](screenshots/reader-dark.png)               | ![book_view_light](screenshots/reader-light.png)               |



