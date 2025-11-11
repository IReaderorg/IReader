# Kokoro TTS Integration

## Overview

Kokoro is a high-quality neural TTS engine that runs as a subprocess. This integration provides an alternative to Piper TTS without requiring JNI compilation.

**GitHub**: https://github.com/hexgrad/kokoro

## Features

✅ **High-Quality Neural Voices** - Natural-sounding speech synthesis  
✅ **Multiple Languages** - Support for various accents and languages  
✅ **Fast Inference** - Quick audio generation  
✅ **No JNI Required** - Runs as subprocess, no C++ compilation needed  
✅ **Easy Setup** - Automatic installation with Python  

## Available Voices

### American English
- **af_bella** (Female) - Warm and friendly
- **af_sarah** (Female) - Professional and clear
- **am_adam** (Male) - Deep and authoritative
- **am_michael** (Male) - Casual and natural

### British English
- **bf_emma** (Female) - Elegant and refined
- **bf_isabella** (Female) - Sophisticated
- **bm_george** (Male) - Distinguished
- **bm_lewis** (Male) - Articulate and clear

## Requirements

### System Requirements
- **Python 3.8+** - Required for running Kokoro
- **Git** - For cloning the Kokoro repository
- **pip** - Python package manager

### Python Dependencies
Automatically installed during initialization:
- torch
- numpy
- scipy
- soundfile

## Installation

### Automatic Installation (Recommended)

The Kokoro engine will automatically initialize when the TTS service starts:

1. **Python Detection** - Finds Python 3.8+ on your system
2. **Repository Clone** - Downloads Kokoro from GitHub
3. **Dependency Installation** - Installs required Python packages
4. **Verification** - Tests the installation

### Manual Installation

If automatic installation fails, you can set up Kokoro manually:

```bash
# 1. Install Python 3.8+ from python.org

# 2. Clone Kokoro repository
cd <app_data_dir>/kokoro
git clone https://github.com/hexgrad/kokoro.git kokoro-tts

# 3. Install dependencies
cd kokoro-tts
pip install -r requirements.txt

# 4. Test installation
python kokoro.py --help
```

## Usage

### Automatic Engine Selection

The TTS service automatically selects the best available engine:

1. **Piper** (if JNI libraries available)
2. **Kokoro** (if Python and dependencies available)
3. **Simulation** (fallback mode)

### Manual Engine Selection

```kotlin
// Switch to Kokoro
ttsService.currentEngine = TTSEngine.KOKORO

// Check if Kokoro is available
if (ttsService.kokoroAvailable) {
    // Use Kokoro
}
```

### Synthesize with Kokoro

```kotlin
val result = kokoroAdapter.synthesize(
    text = "Hello, world!",
    voice = "af_bella",
    speed = 1.0f
)

result.onSuccess { audioData ->
    audioEngine.play(audioData)
}
```

## Architecture

```
┌─────────────────────────────────────┐
│     DesktopTTSService               │
│  (Main TTS Service)                 │
└──────────┬──────────────────────────┘
           │
           ├─────────────┬─────────────┐
           │             │             │
    ┌──────▼──────┐ ┌───▼────────┐ ┌──▼──────────┐
    │   Piper     │ │  Kokoro    │ │ Simulation  │
    │   (JNI)     │ │(Subprocess)│ │   (Mock)    │
    └─────────────┘ └────────────┘ └─────────────┘
```

## Components

### KokoroTTSEngine
Core engine that manages the Kokoro subprocess:
- Python detection
- Repository management
- Dependency installation
- Audio synthesis

### KokoroTTSAdapter
Adapter that integrates Kokoro with the TTS service:
- WAV to AudioData conversion
- Voice management
- Error handling

## Configuration

### Voice Selection

Users can select Kokoro voices from the TTS settings:

```kotlin
val voices = kokoroAdapter.getAvailableVoices()
// Returns list of KokoroVoice objects
```

### Speed Control

Adjust speech speed (0.5x to 2.0x):

```kotlin
kokoroAdapter.synthesize(
    text = text,
    voice = "af_bella",
    speed = 1.5f  // 1.5x speed
)
```

## Troubleshooting

### Python Not Found

**Error**: "Python 3.8+ not found"

**Solution**:
1. Install Python from python.org
2. Ensure Python is in system PATH
3. Restart the application

### Git Not Found

**Error**: "Failed to clone Kokoro repository"

**Solution**:
1. Install Git from git-scm.com
2. Ensure Git is in system PATH
3. Or manually clone the repository

### Dependency Installation Failed

**Error**: "Dependency installation had issues"

**Solution**:
```bash
cd <app_data_dir>/kokoro/kokoro-tts
pip install --upgrade pip
pip install -r requirements.txt
```

### Synthesis Timeout

**Error**: "Synthesis timeout after 30s"

**Solution**:
- Check system resources
- Try shorter text segments
- Reduce synthesis quality if available

## Performance

### Synthesis Speed
- **Short text** (<100 chars): ~1-2 seconds
- **Medium text** (100-500 chars): ~2-5 seconds
- **Long text** (>500 chars): ~5-10 seconds

### Memory Usage
- **Base**: ~200-300 MB (Python + PyTorch)
- **Per synthesis**: ~50-100 MB

### Disk Space
- **Kokoro repository**: ~50 MB
- **Python dependencies**: ~500 MB - 1 GB
- **Voice models**: Included in repository

## Comparison: Piper vs Kokoro

| Feature | Piper | Kokoro |
|---------|-------|--------|
| **Setup** | Requires JNI compilation | Python + pip install |
| **Speed** | Very fast (~100ms) | Fast (~1-2s) |
| **Quality** | High | Very High |
| **Voices** | 30+ voices | 8 voices |
| **Languages** | 20+ languages | English (US/UK) |
| **Memory** | Low (~50 MB) | Medium (~300 MB) |
| **Dependencies** | Native libraries | Python + PyTorch |

## Future Enhancements

- [ ] Add more Kokoro voices
- [ ] Support for additional languages
- [ ] Voice cloning capabilities
- [ ] Real-time streaming synthesis
- [ ] GPU acceleration support
- [ ] Custom voice training

## Support

For issues specific to Kokoro TTS, visit:
- **Kokoro GitHub**: https://github.com/hexgrad/kokoro
- **Issues**: https://github.com/hexgrad/kokoro/issues

For integration issues, check the application logs:
```
<app_data_dir>/logs/tts_service.log
```

## License

Kokoro TTS is licensed under its own terms. Check the Kokoro repository for details.
