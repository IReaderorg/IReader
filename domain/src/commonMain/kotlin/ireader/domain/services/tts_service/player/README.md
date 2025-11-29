# Gradio TTS Player System

A complete, clean architecture TTS player system for Gradio-based text-to-speech engines.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         APPLICATION LAYER                                    │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │              GradioTTSServiceIntegration                             │    │
│  │  • Book/Chapter context management                                   │    │
│  │  • Auto-next chapter functionality                                   │    │
│  │  • Sleep timer integration                                           │    │
│  │  • Reading position tracking                                         │    │
│  └────────────────────────────────┬────────────────────────────────────┘    │
│                                   │                                          │
└───────────────────────────────────┼──────────────────────────────────────────┘
                                    │
┌───────────────────────────────────┼──────────────────────────────────────────┐
│                         MANAGER LAYER                                        │
├───────────────────────────────────┼──────────────────────────────────────────┤
│                                   │                                          │
│  ┌────────────────────────────────▼────────────────────────────────────┐    │
│  │                GradioTTSPlayerManager                                │    │
│  │  • Engine selection and switching                                    │    │
│  │  • Player lifecycle management                                       │    │
│  │  • Content management                                                │    │
│  │  • Event forwarding                                                  │    │
│  └────────────────────────────────┬────────────────────────────────────┘    │
│                                   │                                          │
└───────────────────────────────────┼──────────────────────────────────────────┘
                                    │
┌───────────────────────────────────┼──────────────────────────────────────────┐
│                         PLAYER LAYER                                         │
├───────────────────────────────────┼──────────────────────────────────────────┤
│                                   │                                          │
│  ┌────────────────────────────────▼────────────────────────────────────┐    │
│  │                    GradioTTSPlayer                                   │    │
│  │  ┌─────────────────────────────────────────────────────────────┐    │    │
│  │  │  Command Channel (Thread-safe command processing)            │    │    │
│  │  └─────────────────────────────────────────────────────────────┘    │    │
│  │                              │                                       │    │
│  │  ┌───────────────────────────┼───────────────────────────────┐      │    │
│  │  │                           ▼                               │      │    │
│  │  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐   │      │    │
│  │  │  │ Playback    │  │ Prefetch    │  │ Cache           │   │      │    │
│  │  │  │ Loop        │  │ Manager     │  │ Manager         │   │      │    │
│  │  │  └─────────────┘  └─────────────┘  └─────────────────┘   │      │    │
│  │  └───────────────────────────────────────────────────────────┘      │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
                                    │
┌───────────────────────────────────┼──────────────────────────────────────────┐
│                         ADAPTER LAYER                                        │
├───────────────────────────────────┼──────────────────────────────────────────┤
│                                   │                                          │
│  ┌────────────────────────────────▼────────────────────────────────────┐    │
│  │                  GradioTTSEngineAdapter                              │    │
│  │  implements: GradioAudioGenerator, GradioAudioPlayback               │    │
│  │  • Gradio API communication                                          │    │
│  │  • Audio generation                                                  │    │
│  │  • Playback control delegation                                       │    │
│  └────────────────────────────────┬────────────────────────────────────┘    │
│                                   │                                          │
└───────────────────────────────────┼──────────────────────────────────────────┘
                                    │
┌───────────────────────────────────┼──────────────────────────────────────────┐
│                         PLATFORM LAYER                                       │
├───────────────────────────────────┼──────────────────────────────────────────┤
│                                   │                                          │
│    ┌──────────────────────────────┴──────────────────────────────┐           │
│    │                                                              │           │
│    ▼                                                              ▼           │
│  ┌─────────────────────────────────┐    ┌─────────────────────────────────┐  │
│  │         ANDROID                  │    │            DESKTOP               │  │
│  │  AndroidGradioAudioPlayer        │    │  DesktopGradioAudioPlayer        │  │
│  │  (MediaPlayer)                   │    │  (Java Sound API)                │  │
│  └─────────────────────────────────┘    └─────────────────────────────────┘  │
│                                                                               │
└───────────────────────────────────────────────────────────────────────────────┘
```

## Key Components

### 1. GradioTTSPlayer

The core player class that handles:
- Queue-based paragraph reading (one at a time, in order)
- Pre-caching next N paragraphs for smooth playback
- Play/pause/stop/skip controls
- Settings changes (speed, pitch)
- Thread-safe state management via command channel

### 2. GradioTTSPlayerManager

High-level manager that handles:
- Engine selection and switching
- Player lifecycle management
- Content management
- Event forwarding

### 3. GradioTTSServiceIntegration

Integration layer that connects with the TTS service:
- Book/Chapter context management
- Auto-next chapter functionality
- Sleep timer integration
- Reading position tracking

### 4. GradioTTSEngineAdapter

Adapter that implements the player interfaces:
- `GradioAudioGenerator` - for generating audio from text
- `GradioAudioPlayback` - for playing audio

## Usage Examples

### Basic Usage

```kotlin
// Create the player
val factory = GradioTTSPlayerFactory(httpClient, audioPlayer)
val player = factory.create(config)

// Set content
player.setContent(listOf("Paragraph 1", "Paragraph 2", "Paragraph 3"))

// Control playback
player.play()
player.pause()
player.next()
player.previous()
player.stop()

// Adjust settings
player.setSpeed(1.5f)
player.setPitch(1.2f)

// Observe state
player.isPlaying.collect { isPlaying -> /* update UI */ }
player.currentParagraph.collect { index -> /* highlight paragraph */ }

// Observe events
player.events.collect { event ->
    when (event) {
        is GradioTTSPlayerEvent.PlaybackStarted -> { /* ... */ }
        is GradioTTSPlayerEvent.ChapterFinished -> { /* ... */ }
        is GradioTTSPlayerEvent.Error -> { /* handle error */ }
    }
}

// Cleanup
player.release()
```

### Using the Manager

```kotlin
// Create the manager
val manager = GradioTTSPlayerManager(
    httpClient = httpClient,
    audioPlayer = audioPlayer,
    gradioTTSManager = gradioTTSManager
)

// Select an engine
manager.selectEngine("coqui-tts")

// Set content
manager.setContent(paragraphs)

// Control playback
manager.play()
manager.pause()

// Cleanup
manager.release()
```

### Using the Service Integration

```kotlin
// Create the integration
val integration = GradioTTSServiceIntegration(
    playerManager = manager,
    onChapterFinished = { chapter -> loadNextChapter(chapter) },
    onSleepTimerExpired = { /* handle sleep timer */ }
)

// Start reading
integration.startReading(book, chapter, paragraphs)

// Control playback
integration.play()
integration.pause()

// Set sleep timer
integration.setSleepTimer(30) // 30 minutes

// Enable auto-next chapter
integration.setAutoNextChapter(true)

// Get current position for saving
val position = integration.getCurrentPosition()

// Cleanup
integration.release()
```

## State Management

### GradioTTSPlayerState

Immutable data class that captures all player state:

```kotlin
data class GradioTTSPlayerState(
    val isPlaying: Boolean,
    val isPaused: Boolean,
    val isLoading: Boolean,
    val currentParagraph: Int,
    val totalParagraphs: Int,
    val cachedParagraphs: Set<Int>,
    val loadingParagraphs: Set<Int>,
    val speed: Float,
    val pitch: Float,
    val error: String?,
    val hasContent: Boolean,
    val paragraphProgress: Float,
    val engineName: String
) {
    val canPlay: Boolean
    val canNext: Boolean
    val canPrevious: Boolean
    val isActive: Boolean
    val cacheProgress: Float
}
```

### Events

```kotlin
sealed class GradioTTSPlayerEvent {
    object PlaybackStarted
    object PlaybackPaused
    object PlaybackStopped
    object PlaybackResumed
    data class ParagraphChanged(val index: Int, val text: String)
    data class ParagraphCached(val index: Int)
    object ChapterFinished
    data class Error(val message: String, val recoverable: Boolean)
    data class SpeedChanged(val speed: Float)
    data class PitchChanged(val pitch: Float)
    data class ContentLoaded(val paragraphCount: Int)
    object CacheCleared
    object Releasing
}
```

## Thread Safety

The player uses a command channel pattern for thread-safe state management:

1. All public methods send commands to a channel
2. A single coroutine processes commands sequentially
3. State mutations only happen in the command processor
4. Audio cache is protected by a mutex

## Testing

Comprehensive tests are provided for all components:

- `GradioTTSPlayerTest` - Player functionality tests
- `GradioTTSPlayerStateTest` - State data class tests
- `GradioTTSPlayerFactoryTest` - Factory tests

Mock implementations are provided for testing:
- `MockAudioGenerator`
- `MockAudioPlayback`
- `MockGradioAudioPlayer`
