# Piper TTS Native Libraries

This directory contains platform-specific native libraries for Piper TTS integration.

## Overview

The application uses **piper-jni** for direct JNI access to the Piper TTS engine, providing high-performance, offline text-to-speech synthesis.

## Library Management

**Important:** The application now uses the **piper-jni** library (version 1.2.0-a0f09cd) as a Gradle dependency, which automatically includes all required native libraries for supported platforms.

```kotlin
// domain/build.gradle.kts
implementation("io.github.givimad:piper-jni:1.2.0-a0f09cd")
```

The piper-jni library bundles:
- Piper JNI wrapper libraries (for all platforms)
- ONNX Runtime libraries (for all platforms)
- All necessary dependencies

**No manual library management is required** - the libraries are automatically extracted and loaded at runtime by the piper-jni library.

## Directory Structure

```
native/
├── windows-x64/       # Windows 64-bit support libraries
│   ├── onnxruntime.dll
│   ├── onnxruntime_providers_shared.dll
│   ├── piper_phonemize.dll
│   └── espeak-ng.dll
├── macos-x64/         # macOS Intel (currently empty - handled by piper-jni)
├── macos-arm64/       # macOS Apple Silicon (currently empty - handled by piper-jni)
└── linux-x64/         # Linux 64-bit (currently empty - handled by piper-jni)
```

## Required Libraries

### 1. Piper JNI Library (Automatic via Gradle)
The JNI wrapper library that bridges Java/Kotlin code with the Piper C++ library is provided by the piper-jni dependency.

**Included platforms:**
- ✅ Windows x64
- ✅ macOS x64 (Intel)
- ✅ macOS arm64 (Apple Silicon)
- ✅ Linux x64

**Source:** https://github.com/GiviMAD/piper-jni

### 2. ONNX Runtime Library (Automatic via Gradle)
ONNX Runtime for neural network inference is bundled with piper-jni.

**Additional Windows Support Libraries (Manual)**
The Windows platform requires additional support libraries that are stored in this directory:
- `onnxruntime.dll` - Core ONNX Runtime
- `onnxruntime_providers_shared.dll` - ONNX Runtime providers
- `piper_phonemize.dll` - Phonemization library
- `espeak-ng.dll` - eSpeak NG for text processing

These are loaded alongside the piper-jni libraries for full Windows functionality.

## Platform-Specific Notes

### Windows
- piper-jni automatically handles core library loading
- Additional support libraries (espeak-ng.dll, piper_phonemize.dll) are loaded from this directory
- Ensure Visual C++ Redistributable 2015-2022 is installed on target systems
- Libraries are loaded from the temporary directory at runtime

### macOS
- piper-jni includes signed libraries for both Intel and Apple Silicon
- No manual library management required
- Support both Intel (x64) and Apple Silicon (arm64) architectures
- Use `otool -L` to verify library dependencies if troubleshooting

### Linux
- piper-jni handles library loading automatically
- Ensure required system dependencies are installed (see Linux Audio Backend section)
- Test on multiple distributions (Ubuntu, Fedora, etc.)
- Use `ldd` to check library dependencies if troubleshooting

## Fallback Behavior

If native libraries are not found or fail to load:
1. The application will log a warning
2. TTS will fall back to simulation mode
3. Users will be notified that offline TTS is unavailable

## Development

The piper-jni library simplifies development:
- No manual library extraction or management needed
- Libraries are automatically loaded from the JAR
- Gradle dependency resolution handles all platforms
- Check logs for detailed error messages if issues occur

## License

Ensure compliance with:
- piper-jni license (Apache 2.0)
- Piper TTS license (MIT - pre-1.3.0 version used by piper-jni)
- ONNX Runtime license (MIT)
- Any platform-specific requirements

## Support

For issues with native libraries:
1. Check the application logs for detailed error messages
2. Verify the piper-jni dependency is resolved: `./gradlew :domain:dependencies`
3. Ensure your platform is supported (Windows/macOS/Linux x64, macOS arm64)
4. Check piper-jni GitHub issues: https://github.com/GiviMAD/piper-jni/issues
5. Report issues with platform information from the debug output


## Platform-Specific Audio Configurations

### Windows Audio Backend (WASAPI)

**Configuration:** `WindowsAudioConfig`

The Windows implementation uses the Windows Audio Session API (WASAPI) for low-latency audio playback.

**Features:**
- Optimized buffer size: 4KB (lower latency than default 8KB)
- Prefers DirectSound mixers when available
- Native little-endian audio format support
- Automatic mixer detection and selection

**Required DLLs:**
- `piper_jni.dll` - Piper JNI wrapper library (provided by piper-jni dependency)
- `onnxruntime.dll` - ONNX Runtime for Windows (stored in this directory)
- `onnxruntime_providers_shared.dll` - ONNX Runtime providers (stored in this directory)
- `piper_phonemize.dll` - Phonemization library (stored in this directory)
- `espeak-ng.dll` - eSpeak NG for text processing (stored in this directory)
- Visual C++ Runtime (usually pre-installed):
  - `msvcp140.dll`
  - `vcruntime140.dll`
  - `vcruntime140_1.dll` (x64 only)

**System Requirements:**
- Windows 10 or later (recommended for WASAPI)
- Windows 7/8 supported with DirectSound fallback
- Visual C++ Redistributable 2015-2022

**Troubleshooting:**
- If audio fails, install Visual C++ Redistributable
- Check Windows Defender hasn't quarantined DLLs
- Verify DirectSound is available in Device Manager

### macOS Audio Backend (Core Audio)

**Configuration:** `MacOSAudioConfig`

The macOS implementation uses the Core Audio framework for native audio playback.

**Features:**
- Optimized buffer size: 4KB for low-latency
- Native Core Audio integration
- Support for both Intel (x64) and Apple Silicon (arm64)
- Automatic audio device selection

**Required Libraries:**
- `libpiper_jni.dylib` - Piper JNI wrapper (provided by piper-jni dependency)
- `libonnxruntime.dylib` - ONNX Runtime for macOS (provided by piper-jni dependency)

**Code Signing:**
The piper-jni library includes pre-signed libraries for macOS. No additional signing is required for development or distribution.

**System Requirements:**
- macOS 10.14 (Mojave) or later
- Core Audio framework (included with macOS)

**Troubleshooting:**
- Run `codesign --verify --verbose libpiper_jni.dylib` to check signing
- Allow the app in System Preferences > Security & Privacy
- Check Console.app for detailed error messages
- Verify library architecture matches system: `file libpiper_jni.dylib`

### Linux Audio Backend (ALSA/PulseAudio)

**Configuration:** `LinuxAudioConfig`

The Linux implementation supports both ALSA and PulseAudio, automatically detecting the available audio server.

**Features:**
- Automatic detection of ALSA vs PulseAudio
- Standard buffer size: 8KB (balanced for compatibility)
- Fallback to ALSA if PulseAudio unavailable
- Support for various audio server configurations

**Required Libraries:**
- `libpiper_jni.so` - Piper JNI wrapper (provided by piper-jni dependency)
- `libonnxruntime.so` - ONNX Runtime for Linux (provided by piper-jni dependency)

**System Dependencies:**
- **ALSA:** `libasound2` (usually pre-installed)
- **PulseAudio:** `libpulse0` (common on desktop distributions)
- **C++ Runtime:** `libstdc++6`

**Installation Commands:**

```bash
# Debian/Ubuntu
sudo apt-get install libasound2 libpulse0 libstdc++6

# Fedora/RHEL
sudo dnf install alsa-lib pulseaudio-libs libstdc++

# Arch Linux
sudo pacman -S alsa-lib libpulse gcc-libs
```

**System Requirements:**
- Linux kernel 3.10 or later
- ALSA 1.0.16 or later, or PulseAudio 5.0 or later
- glibc 2.17 or later

**Troubleshooting:**
- Check audio server status: `systemctl --user status pulseaudio`
- Verify library dependencies: `ldd libpiper_jni.so`
- Test ALSA: `aplay -l` (list audio devices)
- Test PulseAudio: `pactl info`
- Check permissions: user must be in `audio` group

## Audio Configuration System

The application uses a platform-specific audio configuration system implemented in:
- `PlatformAudioConfig.kt` - Base interface
- `WindowsAudioConfig.kt` - Windows/WASAPI implementation
- `MacOSAudioConfig.kt` - macOS/Core Audio implementation
- `LinuxAudioConfig.kt` - Linux/ALSA/PulseAudio implementation

The appropriate configuration is automatically selected at runtime based on the detected platform.

## Using piper-jni

### Gradle Dependency

The piper-jni library is included in the domain module:

```kotlin
// domain/build.gradle.kts
desktopMain {
    dependencies {
        implementation("io.github.givimad:piper-jni:1.2.0-a0f09cd")
    }
}
```

### Automatic Library Loading

The piper-jni library automatically:
1. Detects the current platform (Windows/macOS/Linux, x64/arm64)
2. Extracts the appropriate native libraries from the JAR
3. Loads them into the JVM
4. Provides a clean Kotlin/Java API for Piper TTS

### Verification

To verify the piper-jni integration:

```bash
# Check that the dependency is resolved
./gradlew :domain:dependencies | grep piper-jni

# Build the project
./gradlew :desktop:build

# Run the application and check logs for:
# - "Piper TTS initialized successfully" (success)
# - "Piper TTS unavailable" (failure - check error details)
```

### API Usage

The application uses piper-jni through the `PiperSpeechSynthesizer` class:

```kotlin
// Initialize Piper with a voice model
val piper = PiperJNI(modelPath, configPath)

// Synthesize speech
val audioData = piper.synthesize(text)

// Play the audio
audioPlayer.play(audioData)
```

## Resources

- **piper-jni GitHub**: https://github.com/GiviMAD/piper-jni
- **Piper TTS**: https://github.com/rhasspy/piper
- **Voice Models**: https://huggingface.co/rhasspy/piper-voices
- **ONNX Runtime**: https://github.com/microsoft/onnxruntime

## Troubleshooting

### Library Loading Issues

If you encounter library loading errors:

1. **Check Gradle sync**: Ensure `./gradlew :domain:dependencies` shows piper-jni
2. **Verify platform support**: piper-jni supports Windows x64, macOS x64/arm64, Linux x64
3. **Check system dependencies**: 
   - Windows: Visual C++ Redistributable 2015-2022
   - macOS: No additional dependencies
   - Linux: ALSA/PulseAudio libraries (see Linux Audio Backend section)
4. **Review logs**: Check application logs for detailed error messages
5. **Clean build**: Try `./gradlew clean build`

### Windows-Specific Issues

If Windows support libraries fail to load:
1. Verify all DLLs are present in `windows-x64/` directory
2. Check Windows Defender hasn't quarantined the DLLs
3. Ensure Visual C++ Redistributable is installed
4. Try running as administrator (for testing only)

### Performance Issues

Expected performance with piper-jni:
- **Initialization**: 500ms - 2s (one-time, per voice model)
- **Synthesis**: 50-200ms per sentence (depends on length and model)
- **Memory**: ~200-500MB per loaded voice model

If performance is significantly worse:
1. Check CPU usage - synthesis is CPU-intensive
2. Verify model file is on fast storage (SSD recommended)
3. Consider using smaller/faster voice models
4. Check for antivirus interference
