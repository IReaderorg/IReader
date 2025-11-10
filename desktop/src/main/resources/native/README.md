# Piper TTS Native Libraries

This directory contains platform-specific native libraries for Piper TTS integration.

## Directory Structure

```
native/
├── windows-x64/       # Windows 64-bit libraries
│   ├── piper_jni.dll
│   └── onnxruntime.dll
├── macos-x64/         # macOS Intel libraries
│   ├── libpiper_jni.dylib
│   └── libonnxruntime.dylib
├── macos-arm64/       # macOS Apple Silicon libraries
│   ├── libpiper_jni.dylib
│   └── libonnxruntime.dylib
└── linux-x64/         # Linux 64-bit libraries
    ├── libpiper_jni.so
    └── libonnxruntime.so
```

## Required Libraries

### 1. Piper JNI Library (`piper_jni`)
This is the JNI wrapper library that bridges Java/Kotlin code with the Piper C++ library.

**Building from source:**
- Clone the Piper repository: https://github.com/rhasspy/piper
- Build the JNI wrapper (requires CMake and a C++ compiler)
- Copy the resulting library to the appropriate platform directory

### 2. ONNX Runtime Library (`onnxruntime`)
Piper uses ONNX Runtime for neural network inference.

**Download pre-built binaries:**
- Visit: https://github.com/microsoft/onnxruntime/releases
- Download the appropriate version for your platform
- Extract and copy the library files to the platform directories

## Platform-Specific Notes

### Windows
- Libraries must be 64-bit DLLs
- Ensure Visual C++ Redistributable is installed on target systems
- Libraries are loaded from the temporary directory at runtime

### macOS
- Libraries must be signed and notarized for distribution
- Support both Intel (x64) and Apple Silicon (arm64) architectures
- Use `otool -L` to verify library dependencies

### Linux
- Libraries should be built with glibc compatibility in mind
- Test on multiple distributions (Ubuntu, Fedora, etc.)
- Use `ldd` to check library dependencies

## Fallback Behavior

If native libraries are not found or fail to load:
1. The application will log a warning
2. TTS will fall back to simulation mode
3. Users will be notified that offline TTS is unavailable

## Development

During development, you can test without native libraries:
- The application will detect missing libraries
- Simulation mode will be used automatically
- Check logs for detailed error messages

## License

Ensure compliance with:
- Piper TTS license (MIT)
- ONNX Runtime license (MIT)
- Any platform-specific requirements

## Support

For issues with native libraries:
1. Check the application logs for detailed error messages
2. Verify the correct libraries are in the correct directories
3. Ensure your platform is supported (Windows/macOS/Linux x64)
4. Report issues with platform information from the debug output


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
- `piper_jni.dll` - Piper JNI wrapper library
- `onnxruntime.dll` - ONNX Runtime for Windows
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
- `libpiper_jni.dylib` - Piper JNI wrapper
- `libonnxruntime.dylib` - ONNX Runtime for macOS

**Code Signing Requirements:**
Libraries must be signed for Gatekeeper compatibility:

```bash
# For distribution (requires Developer ID):
codesign --force --sign "Developer ID Application: Your Name" libpiper_jni.dylib
codesign --force --sign "Developer ID Application: Your Name" libonnxruntime.dylib

# For development (ad-hoc signing):
codesign --force --sign - libpiper_jni.dylib
codesign --force --sign - libonnxruntime.dylib
```

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
- `libpiper_jni.so` - Piper JNI wrapper
- `libonnxruntime.so` - ONNX Runtime for Linux

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
