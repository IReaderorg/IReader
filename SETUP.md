# IReader Setup Guide

## Prerequisites

- JDK 17 or higher
- Android Studio (for Android development)
- For Desktop TTS features: Windows/Linux/macOS with native library support

## Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd IReader
   ```

2. **Build the project**
   ```bash
   ./gradlew build
   ```

3. **Run Android app**
   ```bash
   ./gradlew :android:installDebug
   ```

4. **Run Desktop app**
   ```bash
   ./gradlew :desktop:run
   ```

## Desktop TTS Setup (Optional)

The desktop version includes Text-to-Speech functionality using Piper TTS. Native libraries are **not included** in the repository and must be downloaded or built.

**Note:** If you don't download/build the libraries, the app will still work but TTS will run in **simulation mode** (timing-based without actual audio). The app will not crash.

### Option 1: Download Pre-built Libraries (Recommended)

Run the download script:

**Windows:**
```powershell
.\native\download_piper.ps1
```

**Linux/macOS:**
```bash
./native/scripts/download_piper.sh
```

This will download the required Piper libraries to `native/libs/`.

### Option 2: Build from Source

See [native/BUILD_GUIDE.md](native/BUILD_GUIDE.md) for detailed instructions on building the native libraries from source.

### Voice Models

Voice models are downloaded automatically when you first use TTS. They are stored in:
- Windows: `%APPDATA%/IReader/voice_models/`
- Linux: `~/.config/IReader/voice_models/`
- macOS: `~/Library/Application Support/IReader/voice_models/`

## Project Structure

```
IReader/
├── android/          # Android app module
├── desktop/          # Desktop app module  
├── presentation/     # UI layer (Compose Multiplatform)
├── domain/           # Business logic
├── data/             # Data layer
├── core/             # Core utilities
├── source-api/       # Content source API
├── native/           # Native TTS libraries (not in repo)
│   ├── libs/         # Downloaded/built libraries (gitignored)
│   └── src/          # Native source code
└── docs/             # Documentation

```

## Files Not in Repository

The following files/directories are excluded from version control and must be downloaded or generated:

### Native Libraries (Desktop TTS)
- `native/libs/*.dll` (Windows)
- `native/libs/*.so` (Linux)
- `native/libs/*.dylib` (macOS)

### Voice Models
- `*.onnx` - Neural network models
- `*.onnx.json` - Model configuration files

### Build Artifacts
- `build/` directories
- `*.apk` files
- Gradle cache

## Troubleshooting

### Desktop TTS not working (no audio)
**Symptom:** TTS controls work but no audio plays

**Cause:** Native libraries not loaded - app is running in simulation mode

**Solution:**
1. Download native libraries: Run `.\native\download_piper.ps1` (Windows) or `./native/scripts/download_piper.sh` (Linux/macOS)
2. Check `native/libs/` directory contains `.dll`/`.so`/`.dylib` files
3. Restart the application
4. Download a voice model from TTS settings
5. Check logs for "Failed to load Piper native libraries" errors

### TTS works but no word highlighting
- Word highlighting only works in the dedicated TTS screen (not the main reader)
- Open a chapter and use the TTS screen to see word-by-word highlighting

### Build failures
1. Clean the project: `./gradlew clean`
2. Invalidate caches in Android Studio
3. Ensure JDK 17+ is being used

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.

## License

See [LICENSE](LICENSE) for license information.
