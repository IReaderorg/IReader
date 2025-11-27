# Unified TTS Screen

This module provides a unified Text-to-Speech (TTS) screen that works across all platforms (Android, Desktop, iOS) with adaptive UI for different screen sizes.

## Architecture

### Core Components

1. **CommonTTSScreenState** - Platform-agnostic state data class
2. **CommonTTSActions** - Interface for TTS actions
3. **UnifiedTTSScreen** - Main composable that renders the TTS UI
4. **TTSContentDisplay** - Content display with paragraph highlighting
5. **TTSMediaControls** - Playback controls (play/pause, next/prev)
6. **TTSSettingsPanelCommon** - Settings dialog for customization

### Platform Adapters

- **AndroidTTSScreenAdapter** - Bridges Android TTSViewModel to unified screen
- **DesktopTTSScreenAdapter** - Bridges Desktop TTSService to unified screen

## Usage

### Android

```kotlin
@Composable
fun MyTTSScreen(vm: TTSViewModel, context: Context) {
    AndroidTTSScreenAdapter(
        vm = vm,
        context = context,
        onNavigateBack = { /* handle navigation */ },
        isTabletOrDesktop = false // or check screen size
    )
}
```

### Desktop

```kotlin
@Composable
fun MyTTSScreen(ttsService: DesktopTTSService) {
    DesktopTTSScreenAdapter(
        ttsService = ttsService,
        onNavigateBack = { /* handle navigation */ }
    )
}
```

### Custom Implementation

```kotlin
@Composable
fun CustomTTSScreen() {
    val state = CommonTTSScreenState(
        currentReadingParagraph = 0,
        isPlaying = false,
        content = listOf("Paragraph 1", "Paragraph 2"),
        // ... other state
    )
    
    val actions = ttsActions {
        onPlay { /* start playback */ }
        onPause { /* pause playback */ }
        onNextParagraph { /* go to next */ }
        // ... other actions
    }
    
    UnifiedTTSScreen(
        state = state,
        actions = actions,
        onNavigateBack = { /* handle back */ },
        isTabletOrDesktop = false
    )
}
```

## Features

### Mobile (Phone)
- Compact media controls
- Floating translation toggle
- Fullscreen mode with floating controls
- Touch-friendly paragraph selection

### Tablet/Desktop
- Expanded media controls with speed slider
- Engine and voice selection buttons
- Download chapter feature (desktop only)
- Side-by-side bilingual display
- Settings panel with more options

### Common Features
- Paragraph highlighting during playback
- Auto-scroll to current paragraph
- Translation support (original, translated, bilingual)
- Custom colors and fonts
- Sleep timer
- Speed control (0.5x - 2.0x)
- Auto-next chapter

## State Management

The unified screen uses `CommonTTSScreenState` which is a simple data class. Platform adapters are responsible for:
1. Collecting state from platform-specific sources (ViewModel, Service)
2. Converting to `CommonTTSScreenState`
3. Implementing `CommonTTSActions` to handle user interactions

## Customization

### Colors
- Use `useCustomColors` to enable custom background/text colors
- Default: Uses MaterialTheme colors

### Typography
- `fontSize`: 12-32sp
- `textAlignment`: Start, Center, End, Justify
- `lineHeight`: Configurable
- `fontWeight`: Configurable

### Playback
- `speechSpeed`: 0.5x - 2.0x
- `autoNextChapter`: Auto-play next chapter
- `sleepTimer`: Auto-stop after X minutes
