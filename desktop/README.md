# IReader Desktop Application

## 🚀 Quick Start

### Linux Users (AppImage - Recommended)

**Install via AM Package Manager:**
```bash
am -i ireader
```

**Or download directly:**
```bash
wget https://github.com/IReaderorg/IReader/releases/latest/download/IReader-x86_64.AppImage
chmod +x IReader-x86_64.AppImage
./IReader-x86_64.AppImage
```

**Update with delta updates (70-90% smaller):**
```bash
appimageupdatetool IReader-x86_64.AppImage
```

See [AppImage Setup Guide](APPIMAGE_SETUP_GUIDE.md) for details.

---

## Installation Requirements

### Option 1: Installer Version (Recommended for most users)
**✅ No Java installation required!**

The installer versions include a bundled JRE (Java Runtime Environment), so you don't need to install Java separately.

Download and install:
- **Windows**: `IReader-x.x.x.msi` or `IReader-x.x.x.exe`
- **macOS**: `IReader-x.x.x.dmg`
- **Linux**: `IReader-x.x.x.deb` or `IReader-x.x.x.rpm`

Just run the installer and you're ready to go!

### Option 2: JAR Version (For advanced users)
**⚠️ Requires Java 17 or later**

If you download the JAR version, you need to install Java manually:

1. Install Java 17 or later:
   - Windows/macOS/Linux: [Adoptium OpenJDK](https://adoptium.net/)
   - Or use your system's package manager

2. Launch IReader:
   - Windows: Use the provided `IReader_launcher.bat` file
   - macOS/Linux: Run `java -Xmx2G -jar IReader-x.x.x.jar`

## Troubleshooting

### AppImage Issues

See the [AppImage Setup Guide](APPIMAGE_SETUP_GUIDE.md#troubleshooting) for AppImage-specific troubleshooting.

### Application won't start

#### Windows
1. Check if Java is installed by opening Command Prompt and typing:
   ```
   java -version
   ```
2. If you get "not recognized" error, install Java or use the standalone version

#### macOS
1. Check if Java is installed by opening Terminal and typing:
   ```
   java -version
   ```
2. If you get "command not found", install Java or use the standalone version

#### Linux
1. Check if Java is installed:
   ```
   java -version
   ```
2. Install Java if needed:
   - Ubuntu/Debian: `sudo apt install openjdk-17-jre`
   - Fedora: `sudo dnf install java-17-openjdk`
   - Arch: `sudo pacman -S jre17-openjdk`

### Other Issues

If you experience issues with resources not loading or translation errors:
1. Make sure you have the latest version
2. Try running with console output to see error messages:
   ```
   java -Xmx2G -jar IReader-x.x.x.jar
   ```
3. Report issues on GitHub with the console output 


---

## 🔧 Building from Source

### Standard Builds

```bash
# Windows installer
./gradlew packageMsi

# macOS installer
./gradlew packageDmg

# Linux packages
./gradlew packageDeb
./gradlew packageRpm
```

### AppImage with Delta Updates (Linux only)

**Prerequisites:**
```bash
# Install required tools
sudo apt install zsync  # Ubuntu/Debian
sudo dnf install zsync  # Fedora
sudo pacman -S zsync    # Arch

# Install appimagetool
wget https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage
chmod +x appimagetool-x86_64.AppImage
sudo mv appimagetool-x86_64.AppImage /usr/local/bin/appimagetool
```

**Build:**
```bash
# Build AppImage with delta update support
./gradlew buildAppImageWithZsync

# Output:
# - desktop/build/compose/binaries/main/IReader-x86_64.AppImage
# - desktop/build/compose/binaries/main/IReader-x86_64.AppImage.zsync
```

**Verify:**
```bash
# Verify update information is embedded
./gradlew verifyAppImageUpdateInfo

# Test delta update mechanism
./gradlew testAppImageDeltaUpdate
```

See [AppImage Setup Guide](APPIMAGE_SETUP_GUIDE.md) for complete build instructions.

---

## 📚 Documentation

- **[AppImage Setup Guide](APPIMAGE_SETUP_GUIDE.md)** - Complete guide for AppImage building, distribution, and usage
- **[AppImage Delta Updates](APPIMAGE_DELTA_UPDATES.md)** - Implementation details and architecture
- **[AM Integration](am-script/README.md)** - AM package manager integration guide
- **[Deployment Checklist](../APPIMAGE_DEPLOYMENT_CHECKLIST.md)** - Testing and deployment checklist

---

## 🎯 Features

### Desktop-Specific Features
- Native file picker for local novels
- System tray integration
- Desktop notifications
- Keyboard shortcuts
- Multi-window support

### AppImage Features
- **Delta Updates**: 70-90% smaller update downloads
- **Self-contained**: No installation required
- **Portable**: Run from anywhere
- **Integrated**: Works with AppImageLauncher
- **Managed**: Install via AM package manager

---

## 🤝 Contributing

### Building AppImages

When contributing AppImage-related changes:

1. Follow TDD methodology (see tests in `src/test/kotlin/ireader/desktop/update/`)
2. Test on multiple Linux distributions
3. Verify delta updates work
4. Update documentation

### Testing

```bash
# Run tests
./gradlew :desktop:test

# Run specific test
./gradlew :desktop:test --tests "AppImageUpdateInfoTest"
```

---

## 📄 License

Mozilla Public License v2.0
