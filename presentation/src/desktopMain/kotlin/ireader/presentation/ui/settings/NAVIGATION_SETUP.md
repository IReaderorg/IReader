# TTS Engine Manager Navigation Setup

## What Was Done

Added "TTS Engine Manager" menu item to the MoreScreen (Settings menu).

## Location

The menu item appears in the main settings screen after "Settings" and before "Information & Support" section.

## To Complete Integration

Find where `MoreScreen` is called and add the navigation callback:

### Example Integration

```kotlin
// In your navigation/screen that calls MoreScreen
MoreScreen(
    vm = viewModel,
    onDownloadScreen = { /* ... */ },
    onBackupScreen = { /* ... */ },
    onCategory = { /* ... */ },
    onSettings = { /* ... */ },
    onAbout = { /* ... */ },
    onHelp = { /* ... */ },
    onDonation = { /* ... */ },
    onTTSEngineManager = {
        // Navigate to TTS Engine Manager
        navigator.push(TTSEngineManagerScreenSpec())
    }
)
```

### Create Screen Spec (if using Voyager)

```kotlin
// In presentation/src/desktopMain/kotlin/ireader/presentation/ui/settings/

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
```

### Alternative: Direct Navigation

If not using Voyager screen specs:

```kotlin
onTTSEngineManager = {
    // Show as dialog or navigate directly
    showTTSEngineManager = true
}

// Then in your composable:
if (showTTSEngineManager) {
    TTSEngineManagerScreen(
        onNavigateBack = { showTTSEngineManager = false }
    )
}
```

## Menu Item Details

- **Title**: "TTS Engine Manager"
- **Description**: "Install and manage text-to-speech engines"
- **Icon**: RecordVoiceOver (microphone icon)
- **Position**: After "Settings", before "Information & Support"

## Testing

1. Run the app
2. Navigate to Settings/More screen
3. Look for "TTS Engine Manager" menu item
4. Click it to open the TTS Engine Manager screen
5. Test installing Kokoro TTS (requires Python 3.8+)

## Features Available

Once navigation is wired up, users can:
- ✅ View status of Piper and Kokoro TTS engines
- ✅ Install Kokoro TTS with one click
- ✅ View installation progress in real-time
- ✅ Test installed engines
- ✅ Uninstall engines
- ✅ See system requirements and help

## Files Modified

1. `MoreScreen.kt` - Added menu item and callback parameter
2. `TTSEngineManagerScreen.kt` - The actual manager screen (already created)

## Next Steps

1. Find where `MoreScreen` is instantiated in your navigation code
2. Add the `onTTSEngineManager` callback
3. Navigate to `TTSEngineManagerScreen` or create a screen spec
4. Test the navigation flow
