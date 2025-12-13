# Generic Gradio TTS Support

This module provides support for any Gradio-based TTS engine, not just Coqui TTS.

## Architecture

### Core Components

1. **GradioTTSConfig** (`GradioTTSConfig.kt`)
   - Configuration model for any Gradio TTS space
   - Supports customizable API endpoints and parameters
   - Serializable for persistence

2. **GradioTTSPresets** (`GradioTTSPresets.kt`)
   - Predefined configurations for popular TTS spaces:
     - Coqui TTS (IReader)
     - Edge TTS
     - XTTS v2
     - Parler TTS
     - MMS TTS (Meta)
     - Bark TTS
   - Persian TTS options (ordered by quality):
     - Persian Edge TTS (Premium - Microsoft neural voices)
     - Persian Chatterbox (Natural - neural model)
     - Persian XTTS (Voice cloning capable)
     - Persian Piper (Basic - legacy)

3. **GenericGradioTTSEngine** (`GenericGradioTTSEngine.kt`)
   - Universal engine that works with any Gradio TTS configuration
   - Supports multiple Gradio API versions (3.x, 4.x, queue-based)
   - Audio caching for smooth playback

4. **GradioTTSManager** (`GradioTTSManager.kt`)
   - Manages multiple TTS configurations
   - Handles persistence and engine creation
   - Provides preset and custom config management

## Usage

### Adding a New Preset

```kotlin
val myPreset = GradioTTSConfig(
    id = "my_tts",
    name = "My TTS Engine",
    spaceUrl = "https://my-space.hf.space",
    apiName = "/synthesize",
    parameters = listOf(
        GradioParam.textParam("text"),
        GradioParam.speedParam("speed", 1.0f)
    ),
    audioOutputIndex = 0,
    description = "My custom TTS engine"
)
```

### Creating a Custom Configuration

Users can create custom configurations through the UI:
1. Go to Settings → TTS → Gradio TTS Engines
2. Click "Add Custom TTS Engine"
3. Enter Space URL, API name, and parameters
4. Test and save

### Parameter Types

- `STRING` - Text input
- `FLOAT` - Decimal number (e.g., speed)
- `INT` - Integer
- `BOOLEAN` - True/false
- `CHOICE` - Selection from predefined options

### Special Parameter Flags

- `isTextInput = true` - This parameter receives the text to synthesize
- `isSpeedInput = true` - This parameter receives the speed value

## Integration

### Android

The `AndroidTTSService` automatically checks for Gradio TTS configuration:
1. Checks `useGradioTTS` preference
2. Loads active config from `activeGradioConfigId`
3. Creates `AndroidGradioTTSEngine` if configured

### Desktop

The `DesktopTTSService` provides:
- `configureGradio(config)` - Configure with a specific config
- `configureGradioFromPreferences()` - Load from saved preferences
- `gradioAvailable` - Check if Gradio TTS is available

## Preferences

New preferences in `AppPreferences`:
- `useGradioTTS()` - Enable/disable Gradio TTS
- `activeGradioConfigId()` - Currently selected config ID
- `gradioTTSConfigs()` - JSON storage for all configs
- `gradioTTSSpeed()` - Global speed override

## API Compatibility

The engine automatically tries multiple Gradio API formats:
1. Gradio 4.x `/call/{api_name}` with SSE streaming
2. Legacy `/api/predict` (Gradio 3.x)
3. `/run/{api_name}` endpoint
4. Queue-based `/queue/join` for long-running tasks

## Adding Support for a New TTS Space

1. Find the API endpoint (check the space's API tab)
2. Identify the parameters and their types
3. Create a `GradioTTSConfig` with the correct settings
4. Test with the "Test" button in settings
5. Optionally add as a preset in `GradioTTSPresets.kt`

## Troubleshooting

### "Not Supported" on First Try

If Gradio TTS shows "not supported" on the first try but works after returning to the TTS Manager screen:

**Cause**: The Gradio TTS configuration wasn't being loaded from preferences during app initialization.

**Solution**: The `DesktopTTSService.initialize()` method now loads Gradio TTS configuration from preferences on startup. Additionally, the `setEngine(TTSEngine.GRADIO)` method now tries to configure from preferences if not already available.

### Engine Not Available After Configuration

If the engine shows as configured but still not available:

1. Check that `useGradioTTS` preference is set to `true`
2. Verify `activeGradioConfigId` contains a valid config ID
3. Ensure the Space URL is accessible
4. Check logs for any API errors

### Audio Not Playing

If audio is generated but not playing:

1. Check audio format compatibility (WAV, MP3, OGG, FLAC supported)
2. Verify the `audioOutputIndex` matches the API response
3. Check for base64-encoded audio vs URL-based audio
