# TTS Engine Manager Integration Guide

## Overview

The TTS Engine Manager screen allows users to install and manage TTS engines (Piper and Kokoro) directly from the app settings.

## Files Created

1. **TTSEngineManagerScreen.kt** - Main UI for managing TTS engines
   - Location: `presentation/src/desktopMain/kotlin/ireader/presentation/ui/settings/`
   - Features:
     - Visual status indicators for each engine
     - One-click installation
     - Installation progress logs
     - Feature comparison
     - Test functionality

## How to Add to Settings Menu

### Option 1: Add to Main Settings Screen

Add a new settings item in your main settings screen:

```kotlin
// In your settings screen composable
SettingsItem(
    title = "TTS Engine Manager",
    subtitle = "Install and manage text-to-speech engines",
    icon = Icons.Default.RecordVoiceOver,
    onClick = {
        navigator.push(TTSEngineManagerScreen())
    }
)
```

### Option 2: Add as Voyager Screen

If using Voyager navigation:

```kotlin
// Create a screen class
class TTSEngineManagerScreenSpec : VoyagerScreen() {
    override val key: ScreenKey = "TTS_ENGINE_MANAGER"
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TTSEngineManagerScreen(
            onNavigateBack = { navigator.pop() }
        )
    }
}

// Navigate to it
navigator.push(TTSEngineManagerScreenSpec())
```

### Option 3: Add to Settings Navigation

If you have a settings navigation structure:

```kotlin
// In your settings navigation
sealed class SettingsDestination {
    object Main : SettingsDestination()
    object TTSEngines : SettingsDestination()
    // ... other destinations
}

// In your navigation handler
when (destination) {
    SettingsDestination.TTSEngines -> {
        TTSEngineManagerScreen(
            onNavigateBack = { /* navigate back */ }
        )
    }
}
```

## Features

### Engine Status Display

Each engine shows:
- ✅ **Installed** - Engine is ready to use
- ❌ **Not Installed** - Engine needs installation
- ⚠️ **Error** - Installation or initialization failed
- ⏳ **Checking...** - Verifying engine status

### Installation Process

#### Piper TTS
1. Checks system requirements (Windows x64)
2. Notes that pre-built libraries are required
3. Guides user to download full release package

#### Kokoro TTS
1. Checks for Python 3.8+
2. Clones Kokoro repository from GitHub
3. Installs Python dependencies
4. Verifies installation
5. Shows real-time progress

### Installation Log

Real-time log display showing:
- Progress messages
- Success/failure indicators
- Error details
- Scrollable output

## User Experience

### Before Installation

```
┌─────────────────────────────────────┐
│ Piper TTS                           │
│ Status: Not Installed               │
│                                     │
│ Features:                           │
│ ✓ Very fast synthesis               │
│ ✓ 30+ voices                        │
│ ✓ 20+ languages                     │
│                                     │
│ [Install]                           │
└─────────────────────────────────────┘
```

### During Installation

```
┌─────────────────────────────────────┐
│ Kokoro TTS                          │
│ Status: Installing...               │
│                                     │
│ Installation Log:                   │
│ ✓ Python found                      │
│ ✓ Cloning repository                │
│ → Installing dependencies...        │
│                                     │
│ [Installing...]                     │
└─────────────────────────────────────┘
```

### After Installation

```
┌─────────────────────────────────────┐
│ Kokoro TTS                          │
│ Status: ✓ Installed                 │
│                                     │
│ Kokoro engine ready                 │
│                                     │
│ [Uninstall]  [Test]                 │
└─────────────────────────────────────┘
```

## Requirements Display

The screen shows a help card with requirements:

```
ℹ️ Need Help?

• Piper requires native libraries (provided in releases)
• Kokoro requires Python 3.8+ and Git
• Both engines work offline after installation
• You can use both engines simultaneously
```

## Testing

After installation, users can test engines:

```kotlin
// Test button action
Button(onClick = {
    scope.launch {
        val result = ttsService.synthesize(
            text = "Hello, this is a test.",
            engine = TTSEngine.KOKORO
        )
        // Play audio or show result
    }
}) {
    Text("Test")
}
```

## Error Handling

The screen handles various error scenarios:

1. **Python Not Found** (Kokoro)
   - Shows error message
   - Provides link to python.org
   - Suggests adding Python to PATH

2. **Git Not Found** (Kokoro)
   - Shows error message
   - Provides link to git-scm.com
   - Suggests manual installation

3. **Native Libraries Missing** (Piper)
   - Explains JNI requirement
   - Directs to release page
   - Shows build instructions link

4. **Network Errors**
   - Retry button
   - Manual installation instructions
   - Offline package option

## Customization

### Styling

Customize colors and spacing:

```kotlin
// In TTSEngineManagerScreen.kt
val cardElevation = 4.dp  // Adjust card elevation
val spacing = 24.dp       // Adjust spacing between elements
```

### Features List

Add or modify engine features:

```kotlin
features = listOf(
    "Your custom feature",
    "Another feature",
    // ...
)
```

### Installation Scripts

Customize installation logic in:
- `installPiper()` function
- `installKokoro()` function

## Integration with TTS Service

The screen integrates with `DesktopTTSService`:

```kotlin
val ttsService: DesktopTTSService = koinInject()

// Check engine availability
val piperAvailable = ttsService.synthesizer.isInitialized()
val kokoroAvailable = ttsService.kokoroAvailable

// Switch engines
ttsService.currentEngine = TTSEngine.KOKORO
```

## Future Enhancements

Potential additions:
- [ ] Download progress bars with percentage
- [ ] Automatic engine selection based on availability
- [ ] Voice preview/testing
- [ ] Engine benchmarking
- [ ] Automatic updates
- [ ] Offline installation packages
- [ ] Custom engine configuration

## Support

For issues:
1. Check installation logs
2. Verify system requirements
3. Check application logs
4. Consult engine-specific documentation

## License

This integration follows the same license as the main application.
