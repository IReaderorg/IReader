# TTS UI Components Integration Guide

## Overview
This guide explains how to integrate the TTS UI components into the IReader application.

## Components Overview

### 1. Voice Selection Screen
**Location:** `ireader.presentation.ui.settings.screens.VoiceSelectionScreen`

**Purpose:** Allows users to browse, search, and select TTS voices from the catalog.

**Usage:**
```kotlin
// Navigate to voice selection screen
navigator.push(VoiceSelectionScreen())
```

**Features:**
- Browse 20+ languages with 30+ voices
- Filter by language
- Search by name, language, or tags
- Download voices with progress tracking
- Preview voices before downloading
- Delete installed voices

### 2. Voice Card Component
**Location:** `ireader.presentation.ui.settings.components.VoiceCard`

**Purpose:** Displays individual voice information with actions.

**Usage:**
```kotlin
VoiceCard(
    voice = voiceModel,
    isSelected = selectedVoiceId == voiceModel.id,
    isDownloaded = installedVoices.contains(voiceModel.id),
    downloadProgress = downloadProgress,
    onSelect = { /* Handle selection */ },
    onDownload = { /* Handle download */ },
    onPreview = { /* Handle preview */ },
    onDelete = { /* Handle deletion */ }
)
```

### 3. TTS Control Panel
**Location:** `ireader.presentation.ui.reader.components.TTSControlPanel`

**Purpose:** Provides playback controls for TTS in the reader.

**Usage:**
```kotlin
val ttsViewModel = getScreenModel<TTSViewModel>()
val isPlaying by ttsViewModel.isPlaying.collectAsState()
val currentPosition by ttsViewModel.currentPosition.collectAsState()
val duration by ttsViewModel.duration.collectAsState()
val speechRate by ttsViewModel.speechRate.collectAsState()

TTSControlPanel(
    isPlaying = isPlaying,
    currentPosition = currentPosition,
    duration = duration,
    speechRate = speechRate,
    onPlayPause = { ttsViewModel.togglePlayPause() },
    onSkipBackward = { ttsViewModel.skipBackward() },
    onSkipForward = { ttsViewModel.skipForward() },
    onSpeechRateChanged = { ttsViewModel.setSpeechRate(it) }
)
```

### 4. Text Highlighting
**Location:** `ireader.presentation.ui.reader.components.TTSTextHighlight`

**Purpose:** Highlights currently playing text in the reader.

**Usage:**
```kotlin
TTSHighlightedText(
    text = chapterText,
    highlightedRange = calculateCurrentSentenceRange(text, currentPosition),
    highlightColor = MaterialTheme.colorScheme.primaryContainer
)
```

### 5. Accessibility Features
**Location:** `ireader.presentation.ui.reader.components.TTSAccessibility`

**Components:**
- `TTSScreenReaderAnnouncement`: Screen reader support
- `TTSWaveformVisualizer`: Visual audio feedback
- `TTSKeyboardShortcuts`: Keyboard control support
- `rememberHighContrastColors`: High contrast mode

**Usage:**
```kotlin
// Screen reader announcements
TTSScreenReaderAnnouncement(
    isPlaying = isPlaying,
    currentText = currentText,
    speechRate = speechRate
)

// Waveform visualizer
TTSWaveformVisualizer(
    isPlaying = isPlaying,
    amplitude = currentAmplitude
)

// Keyboard shortcuts
TTSKeyboardShortcuts(
    onPlayPause = { ttsViewModel.togglePlayPause() },
    onSkipForward = { ttsViewModel.skipForward() },
    onSkipBackward = { ttsViewModel.skipBackward() },
    onIncreaseSpeed = { ttsViewModel.setSpeechRate(speechRate + 0.1f) },
    onDecreaseSpeed = { ttsViewModel.setSpeechRate(speechRate - 0.1f) },
    onStop = { ttsViewModel.stop() }
)
```

## Integration Steps

### Step 1: Add Voice Selection to Settings
Add a navigation item in the settings screen:

```kotlin
// In SettingsScreen.kt
SettingsItem(
    title = "Text-to-Speech Voices",
    subtitle = "Manage TTS voices",
    onClick = { navigator.push(VoiceSelectionScreen()) }
)
```

### Step 2: Add TTS Controls to Reader
Integrate the control panel in the reader screen:

```kotlin
// In ReaderScreen.kt
@Composable
fun ReaderScreen() {
    val ttsViewModel = getScreenModel<TTSViewModel>()
    val showTTSControls by remember { mutableStateOf(false) }
    
    Scaffold(
        bottomBar = {
            if (showTTSControls) {
                TTSControlPanel(
                    isPlaying = ttsViewModel.isPlaying.collectAsState().value,
                    currentPosition = ttsViewModel.currentPosition.collectAsState().value,
                    duration = ttsViewModel.duration.collectAsState().value,
                    speechRate = ttsViewModel.speechRate.collectAsState().value,
                    onPlayPause = { ttsViewModel.togglePlayPause() },
                    onSkipBackward = { ttsViewModel.skipBackward() },
                    onSkipForward = { ttsViewModel.skipForward() },
                    onSpeechRateChanged = { ttsViewModel.setSpeechRate(it) }
                )
            }
        }
    ) {
        // Reader content
    }
}
```

### Step 3: Connect to TTS Service
Wire up the ViewModels to the actual TTS service:

```kotlin
// In VoiceSelectionViewModel.kt
fun downloadVoice(voice: VoiceModel) {
    screenModelScope.launch {
        try {
            val repository = get<VoiceModelRepository>()
            repository.downloadVoice(voice.id) { progress ->
                _downloadProgress.value = DownloadProgress(voice.id, progress)
            }
            _installedVoices.update { it + voice.id }
        } catch (e: Exception) {
            // Handle error
        }
    }
}

// In TTSViewModel.kt
fun play() {
    _isPlaying.value = true
    screenModelScope.launch {
        val ttsService = get<DesktopTTSService>()
        ttsService.speak(currentText)
    }
}
```

### Step 4: Add Keyboard Shortcuts
Register keyboard shortcuts in the reader:

```kotlin
// In ReaderScreen.kt
TTSKeyboardShortcuts(
    onPlayPause = { ttsViewModel.togglePlayPause() },
    onSkipForward = { ttsViewModel.skipForward() },
    onSkipBackward = { ttsViewModel.skipBackward() },
    onIncreaseSpeed = { 
        val current = ttsViewModel.speechRate.value
        ttsViewModel.setSpeechRate((current + 0.1f).coerceAtMost(2.0f))
    },
    onDecreaseSpeed = { 
        val current = ttsViewModel.speechRate.value
        ttsViewModel.setSpeechRate((current - 0.1f).coerceAtLeast(0.5f))
    },
    onStop = { ttsViewModel.stop() }
)
```

## Dependency Injection Setup

Add the ViewModels to your DI module:

```kotlin
// In PresentationModule.kt
single { VoiceSelectionViewModel(get()) }
single { TTSViewModel() }
```

## Preferences Setup

The TTS preferences are already added to `UiPreferences`:
- `selectedVoiceId()`: Currently selected voice
- `ttsEnabled()`: TTS feature enabled/disabled
- `ttsSpeechRate()`: Preferred speech rate
- `ttsAutoPlay()`: Auto-play on chapter load

Access them like:
```kotlin
val preferences = get<UiPreferences>()
val selectedVoice = preferences.selectedVoiceId().get()
preferences.ttsSpeechRate().set(1.5f)
```

## Keyboard Shortcuts Reference

| Shortcut | Action |
|----------|--------|
| Space or K | Play/Pause |
| → or L | Skip forward 10 seconds |
| ← or J | Skip backward 10 seconds |
| Shift + > | Increase speed |
| Shift + < | Decrease speed |
| Esc | Stop playback |

## Accessibility Features

### Screen Reader Support
All components include proper semantic labels:
- Buttons have descriptive content descriptions
- Progress is announced via live regions
- State changes are communicated

### High Contrast Mode
Enable high contrast colors:
```kotlin
val colors = rememberHighContrastColors(isHighContrast = true)
```

### Visual Feedback
The waveform visualizer provides visual feedback for users who are deaf or hard of hearing.

## Testing

### Manual Testing Checklist
- [ ] Voice selection screen loads with all voices
- [ ] Language filtering works correctly
- [ ] Search finds voices by name/language/tags
- [ ] Voice cards display correct information
- [ ] Download progress shows correctly
- [ ] TTS controls respond to clicks
- [ ] Keyboard shortcuts work
- [ ] Speed slider adjusts playback
- [ ] Text highlighting updates during playback
- [ ] Screen reader announces state changes

### Automated Testing
See `TASK_7_SUMMARY.md` for recommended unit and integration tests.

## Troubleshooting

### Voice Selection Screen is Empty
- Check that `VoiceCatalog.getAllVoices()` returns voices
- Verify the ViewModel is properly initialized

### TTS Controls Don't Respond
- Ensure TTSViewModel is properly injected
- Check that state flows are being collected
- Verify the TTS service is initialized

### Keyboard Shortcuts Don't Work
- Platform-specific implementation may be needed
- Check that the component is in focus
- Verify key event handlers are registered

## Future Enhancements

1. **Voice Preview**: Implement actual TTS preview with sample text
2. **Download Management**: Add pause/resume for downloads
3. **Voice Recommendations**: Suggest voices based on content language
4. **Playback History**: Track recently used voices
5. **Custom Voice Import**: Allow users to add custom voice models

## Support

For issues or questions:
1. Check the implementation in the source files
2. Review the design document at `.kiro/specs/piper-jni-production/design.md`
3. See requirements at `.kiro/specs/piper-jni-production/requirements.md`

---

**Last Updated:** 2025-11-10
**Version:** 1.0.0
