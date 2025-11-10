# Platform-Specific Audio Configuration

This document describes the platform-specific audio backend configurations implemented for Piper TTS integration.

## Overview

The audio playback system now uses platform-optimized configurations to provide the best audio experience on Windows, macOS, and Linux. Each platform has specific audio APIs and requirements that are automatically detected and configured.

## Architecture

### Core Components

1. **PlatformAudioConfig** (Interface)
   - Defines the contract for platform-specific audio configurations
   - Methods: `getBufferSize()`, `getPreferredMixer()`, `configureAudioFormat()`, `initialize()`, `cleanup()`

2. **PlatformAudioConfigFactory**
   - Factory class that detects the current platform
   - Returns the appropriate configuration instance
   - Caches configuration for performance

3. **Platform Implementations**
   - `WindowsAudioConfig` - Windows/WASAPI
   - `MacOSAudioConfig` - macOS/Core Audio
   - `LinuxAudioConfig` - Linux/ALSA/PulseAudio
   - `DefaultAudioConfig` - Fallback for unsupported platforms

4. **AudioPlaybackEngine** (Updated)
   - Now uses platform-specific configurations
   - Automatically selects optimal mixer and buffer size
   - Logs platform information for debugging

## Platform Details

### Windows (WASAPI)

**File:** `WindowsAudioConfig.kt`

**Features:**
- Optimized for Windows Audio Session API (WASAPI)
- Buffer size: 4KB (lower latency)
- Prefers DirectSound mixers when available
- Native little-endian format support

**Mixer Selection Priority:**
1. Primary Sound Driver
2. DirectSound
3. Windows DirectSound
4. Any DirectSound mixer
5. System default

**Requirements:**
- Windows 7 or later
- Visual C++ Redistributable 2015-2022
- DirectSound support (standard on Windows)

### macOS (Core Audio)

**File:** `MacOSAudioConfig.kt`

**Features:**
- Optimized for Core Audio framework
- Buffer size: 4KB (low latency)
- Architecture detection (Intel x64 vs Apple Silicon arm64)
- Gatekeeper compatibility checks

**Mixer Selection Priority:**
1. Default Audio Device
2. Built-in Output
3. Core Audio
4. Any output mixer
5. System default

**Requirements:**
- macOS 10.14 (Mojave) or later
- Signed native libraries for Gatekeeper
- Core Audio framework (included with macOS)

**Code Signing:**
```bash
# For distribution:
codesign --force --sign "Developer ID Application: Your Name" libpiper_jni.dylib

# For development:
codesign --force --sign - libpiper_jni.dylib
```

### Linux (ALSA/PulseAudio)

**File:** `LinuxAudioConfig.kt`

**Features:**
- Automatic detection of ALSA vs PulseAudio
- Buffer size: 8KB (balanced for compatibility)
- Distribution detection for logging
- Audio group membership check

**Audio Server Detection:**
1. Check for PulseAudio process (`pgrep pulseaudio`)
2. Try `pactl info` command
3. Check for ALSA devices in `/dev/snd`
4. Try `aplay -l` command

**Mixer Selection Priority:**
- **PulseAudio:** PulseAudio Mixer, default
- **ALSA:** default, ALSA, PCM
- **Fallback:** Any playback/output mixer

**Requirements:**
- Linux kernel 3.10 or later
- ALSA 1.0.16+ or PulseAudio 5.0+
- User in `audio` group (for some distributions)

**System Dependencies:**
```bash
# Debian/Ubuntu
sudo apt-get install libasound2 libpulse0 libstdc++6

# Fedora/RHEL
sudo dnf install alsa-lib pulseaudio-libs libstdc++

# Arch Linux
sudo pacman -S alsa-lib libpulse gcc-libs
```

## Usage

The platform-specific configuration is automatically applied when `AudioPlaybackEngine` is initialized:

```kotlin
// AudioPlaybackEngine automatically uses platform config
val audioEngine = AudioPlaybackEngine()

// Platform is detected and configured automatically
// No manual configuration needed
```

## Logging

Each platform configuration logs detailed information during initialization:

```
[INFO] AudioPlaybackEngine initialized with Windows (WASAPI)
[INFO] Windows audio: Using mixer 'Primary Sound Driver'
[DEBUG] Available Windows audio mixers (3):
[DEBUG]   [0] Primary Sound Driver - Direct Audio Device
[DEBUG]   [1] Speakers (Realtek High Definition Audio) - Direct Audio Device
[DEBUG]   [2] Java Sound Audio Engine - Software mixer and synthesizer
```

## Testing

To test platform-specific configurations:

1. **Check Platform Detection:**
   ```kotlin
   val config = PlatformAudioConfigFactory.getConfig()
   println(config.getPlatformName())
   ```

2. **Verify Mixer Selection:**
   - Check application logs for mixer information
   - Ensure preferred mixer is selected

3. **Test Audio Playback:**
   - Play audio and verify low latency
   - Test pause/resume functionality
   - Verify buffer size is appropriate

## Troubleshooting

### Windows
- **Issue:** Audio not playing
  - **Solution:** Install Visual C++ Redistributable
  - **Check:** Device Manager > Sound controllers

- **Issue:** High latency
  - **Solution:** Ensure DirectSound is available
  - **Check:** Application logs for mixer selection

### macOS
- **Issue:** Library blocked by Gatekeeper
  - **Solution:** Sign libraries with `codesign`
  - **Check:** System Preferences > Security & Privacy

- **Issue:** No audio output
  - **Solution:** Check System Preferences > Sound > Output
  - **Check:** Console.app for detailed errors

### Linux
- **Issue:** Audio not playing
  - **Solution:** Install ALSA or PulseAudio
  - **Check:** `systemctl --user status pulseaudio`

- **Issue:** Permission denied
  - **Solution:** Add user to audio group: `sudo usermod -a -G audio $USER`
  - **Check:** `groups` command output

- **Issue:** Wrong audio server detected
  - **Solution:** Check logs for detection details
  - **Check:** `pactl info` or `aplay -l`

## Performance Characteristics

| Platform | Buffer Size | Typical Latency | Audio API |
|----------|-------------|-----------------|-----------|
| Windows  | 4KB         | ~10-20ms        | WASAPI/DirectSound |
| macOS    | 4KB         | ~10-20ms        | Core Audio |
| Linux    | 8KB         | ~20-40ms        | PulseAudio/ALSA |

## Future Enhancements

Potential improvements for future versions:

1. **Dynamic Buffer Sizing:** Adjust buffer size based on system performance
2. **Exclusive Mode:** Windows WASAPI exclusive mode for even lower latency
3. **JACK Support:** Add JACK audio server support for Linux pro audio
4. **Audio Device Selection:** Allow users to choose specific output devices
5. **Latency Monitoring:** Real-time latency measurement and reporting

## References

- **Windows WASAPI:** https://docs.microsoft.com/en-us/windows/win32/coreaudio/wasapi
- **macOS Core Audio:** https://developer.apple.com/documentation/coreaudio
- **Linux ALSA:** https://www.alsa-project.org/
- **PulseAudio:** https://www.freedesktop.org/wiki/Software/PulseAudio/
- **Java Sound API:** https://docs.oracle.com/javase/8/docs/technotes/guides/sound/
