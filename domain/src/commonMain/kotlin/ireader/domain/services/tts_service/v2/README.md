# TTS v2 Architecture

A clean, simplified TTS architecture following the command pattern and single source of truth principles.

## Quick Start - Testing v2

1. Go to **Settings → TTS Engine Manager**
2. Enable **"TTS V2 Architecture"** toggle (at the bottom)
3. Open any book and tap the **TTS/Play button** in the reader
4. The v2 TTS screen will open with CommonTTSScreen UI

## Features (Full Feature Parity with v1)

- **Translation Support**: Auto-loads translations, toggle between original/translated/bilingual
- **Sentence Highlighting**: Time-based sentence highlighting with rolling WPM calibration
- **Sleep Timer**: Auto-stop playback after set duration with dialog UI
- **Chunk Mode**: Text merging for efficient remote TTS (Gradio)
- **Preferences Integration**: Auto-loads/saves TTS settings
- **Notification Support**: Media-style notifications with controls
- **Background Service (Android)**: Continue playback when app is in background with media controls
- **Chapter Drawer**: Navigate between chapters without leaving TTS screen
- **Settings Panel**: Full settings panel with all TTS options
- **Engine Settings**: Platform-specific TTS engine configuration
- **Voice Selection**: Platform-specific voice selection
- **Translate Chapter**: One-tap translation of current chapter
- **Auto-Translate Next**: Automatically translate next chapter when current completes
- **Fullscreen Mode**: Distraction-free reading with floating controls
- **Custom Colors**: Customizable background and text colors
- **Font Settings**: Adjustable font size and text alignment

## Design Principles

1. **Single Source of Truth**: `TTSState` is the only state, managed by `TTSController`
2. **Command Pattern**: All interactions go through `TTSCommand` sealed class
3. **Event-Driven**: One-time events via `TTSEvent` for UI notifications
4. **Separation of Concerns**: Engine only handles speech, Controller handles coordination
5. **Easy to Debug**: All state changes logged with `Log.warn`, immutable state

## Components

### Core Components

| Component | Description |
|-----------|-------------|
| `TTSState` | Immutable state data class - single source of truth |
| `TTSCommand` | Sealed class for all operations |
| `TTSEvent` | One-time events for UI notifications |
| `TTSEngine` | Platform-specific speech synthesis interface |
| `TTSController` | Central coordinator processing commands |
| `TTSContentLoader` | Content loading abstraction |
| `TTSViewModelAdapter` | UI-friendly adapter for Compose |

### Chunk Mode Components (for Remote TTS)

| Component | Description |
|-----------|-------------|
| `TTSTextMergerV2` | Merges paragraphs into chunks |
| `TTSCacheUseCase` | Chunk-based audio caching |
| `TTSChunkPlayer` | Manages chunk playback with cache |

### Additional Use Cases

| Component | Description |
|-----------|-------------|
| `TTSNotificationUseCase` | Manages playback notifications |
| `TTSSleepTimerUseCase` | Auto-stop playback after duration |
| `TTSPreferencesUseCase` | Bridges v2 with existing preferences |

## TTSState

Immutable data class representing complete TTS state:

```kotlin
data class TTSState(
    // Playback
    val playbackState: PlaybackState,
    val currentParagraphIndex: Int,
    val totalParagraphs: Int,
    
    // Content
    val book: Book?,
    val chapter: Chapter?,
    val paragraphs: List<String>,
    
    // Settings
    val speed: Float,
    val pitch: Float,
    val autoNextChapter: Boolean,
    
    // Engine
    val engineType: EngineType,
    val isEngineReady: Boolean,
    
    // Chunk mode
    val chunkModeEnabled: Boolean,
    val currentChunkIndex: Int,
    val totalChunks: Int,
    val cachedChunks: Set<Int>,
    
    // Error
    val error: TTSError?
)
```

## TTSCommand

All operations go through commands:

```kotlin
sealed class TTSCommand {
    // Playback
    object Play, Pause, Stop, Resume
    
    // Navigation
    object NextParagraph, PreviousParagraph
    data class JumpToParagraph(val index: Int)
    object NextChapter, PreviousChapter
    
    // Content
    data class LoadChapter(val bookId: Long, val chapterId: Long, val startParagraph: Int)
    data class SetContent(val paragraphs: List<String>)
    
    // Settings
    data class SetSpeed(val speed: Float)
    data class SetPitch(val pitch: Float)
    data class SetAutoNextChapter(val enabled: Boolean)
    data class SetEngine(val type: EngineType)
    data class SetGradioConfig(val config: GradioConfig)
    
    // Chunk mode
    data class EnableChunkMode(val targetWordCount: Int)
    object DisableChunkMode
    object NextChunk, PreviousChunk
    data class JumpToChunk(val index: Int)
    
    // Lifecycle
    object Initialize, Cleanup
}
```

## Usage

### Basic Usage with ViewModel Adapter

```kotlin
class TTSViewModel(controller: TTSController) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    val adapter = TTSViewModelAdapter(controller, scope)
    
    init {
        adapter.initialize()
    }
    
    fun loadChapter(bookId: Long, chapterId: Long) {
        adapter.loadChapter(bookId, chapterId)
    }
    
    fun onPlayPause() = adapter.togglePlayPause()
    fun onNext() = adapter.nextParagraph()
    fun onPrevious() = adapter.previousParagraph()
}
```

### Compose UI

```kotlin
@Composable
fun TTSScreen(adapter: TTSViewModelAdapter) {
    val isPlaying by adapter.isPlaying.collectAsState()
    val progress by adapter.progress.collectAsState()
    val currentParagraph by adapter.currentParagraph.collectAsState()
    
    Column {
        LinearProgressIndicator(progress = progress)
        
        Text("Paragraph ${currentParagraph + 1}")
        
        Row {
            IconButton(onClick = { adapter.previousParagraph() }) {
                Icon(Icons.Default.SkipPrevious, "Previous")
            }
            IconButton(onClick = { adapter.togglePlayPause() }) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    if (isPlaying) "Pause" else "Play"
                )
            }
            IconButton(onClick = { adapter.nextParagraph() }) {
                Icon(Icons.Default.SkipNext, "Next")
            }
        }
    }
}
```

### Using Gradio TTS with Chunk Mode

```kotlin
// Configure Gradio TTS
val gradioConfig = GradioConfig(
    id = "coqui",
    name = "Coqui TTS",
    spaceUrl = "https://your-gradio-space.hf.space",
    apiName = "predict",
    enabled = true
)

// Enable Gradio with chunk mode
adapter.useGradioTTSWithChunks(gradioConfig, targetWordCount = 50)

// Load and play
adapter.loadChapter(bookId, chapterId)
adapter.play()
```

### Handling Events

```kotlin
LaunchedEffect(Unit) {
    adapter.events.collectLatest { event ->
        when (event) {
            is TTSEvent.ChapterCompleted -> showToast("Chapter finished")
            is TTSEvent.Error -> showError(event.error)
            else -> {}
        }
    }
}
```

### Using Sleep Timer

```kotlin
// Inject the sleep timer use case
val sleepTimer: TTSSleepTimerUseCase by inject()

// Initialize with controller
sleepTimer.initialize(controller, scope)

// Start timer (stops playback after 30 minutes)
sleepTimer.start(30)

// Add more time to existing timer
sleepTimer.addTime(15)

// Cancel timer
sleepTimer.cancel()

// Observe state in Compose
val timerState by sleepTimer.state.collectAsState()
if (timerState.isEnabled) {
    Text("Sleep in: ${timerState.formatRemaining()}")
}
```

### Using Notifications

```kotlin
// Inject the notification use case
val notifications: TTSNotificationUseCase by inject()

// Start observing controller state
notifications.start(controller, scope)

// Notifications auto-update based on TTSState
// Manual control if needed:
notifications.showNotification()
notifications.hideNotification()

// Cleanup when done
notifications.cleanup()
```

### Using Preferences

```kotlin
// Inject the preferences use case
val preferences: TTSPreferencesUseCase by inject()

// Initialize with controller (loads saved settings automatically)
preferences.initialize(controller, scope)

// Get saved preferences
val speed = preferences.getSpeed()
val pitch = preferences.getPitch()
val mergeWords = preferences.getMergeWordsRemote()

// Set preferences (also updates controller)
preferences.setSpeed(1.5f)
preferences.setPitch(1.0f)
preferences.setAutoNextChapter(true)

// Preferences are auto-saved when controller state changes
// Cleanup when done
preferences.cleanup()
```

## Platform Implementations

### Android
- `AndroidNativeTTSEngineV2` - Wraps Android TTS
- `AndroidGradioTTSEngineV2` - HTTP-based Gradio TTS
- `TTSV2Service` - Background service with MediaSession

### Desktop
- `DesktopPiperTTSEngineV2` - Piper TTS
- `DesktopGradioTTSEngineV2` - HTTP-based Gradio TTS

### iOS
- `IosTTSEngineV2` - AVSpeechSynthesizer

## Gradio TTS Presets

The following Gradio TTS presets are available in `GradioTTSPresets`:

| Preset | Description | Languages |
|--------|-------------|-----------|
| Coqui TTS (IReader) | High-quality fast_pitch model | English |
| Edge TTS | Microsoft Edge voices | Multiple |
| XTTS v2 | Coqui's multilingual with voice cloning | 15+ languages |
| StyleTTS 2 | Expressive TTS with style control | English |
| Silero TTS | Fast and lightweight | 9 languages |
| OpenVoice | Voice cloning with emotion control | 6 languages |
| Fish Speech | Fast multilingual with natural prosody | 4 languages |
| Parler TTS | Describe the voice you want | English |
| MMS TTS (Meta) | 1000+ languages support | 1000+ |
| Tortoise TTS | High quality, slower | English |
| Bark TTS | Generative TTS by Suno | English |
| Persian Piper | Persian language TTS | Persian |

Usage:
```kotlin
// Get a preset
val config = GradioTTSPresets.COQUI_IREADER

// Convert to v2 GradioConfig
val v2Config = GradioConfig(
    id = config.id,
    name = config.name,
    spaceUrl = config.spaceUrl,
    apiName = config.apiName,
    enabled = true
)

// Use with v2 adapter
adapter.useGradioTTS(v2Config)
```

## DI Setup (Koin)

The `ttsV2Module` is automatically included in platform DomainModules:

```kotlin
// Already included in DomainModule
includes(ttsV2Module)

// Inject in your code
val controller: TTSController by inject()
val adapter: TTSViewModelAdapter by inject { parametersOf(scope) }
```

## Migration from v1

### Option 1: Use TTSV2CommonScreen (Recommended)

Use `TTSV2CommonScreen` - a ready-to-use screen with CommonTTSScreen UI and v2 backend:

```kotlin
@Composable
fun MyTTSScreen(viewModel: TTSV2ViewModel) {
    val sleepTimerState by viewModel.sleepTimerState?.collectAsState() 
        ?: remember { mutableStateOf(null) }
    
    TTSV2CommonScreen(
        adapter = viewModel.adapter,
        sleepTimerState = sleepTimerState,
        onSleepTimerStart = { viewModel.startSleepTimer(it) },
        onSleepTimerCancel = { viewModel.cancelSleepTimer() },
        onBack = { navController.popBackStack() },
        onOpenSettings = { /* navigate to settings */ },
        // Customize display
        fontSize = 18,
        isTabletOrDesktop = false
    )
}
```

### Option 2: Manual Integration with CommonTTSScreen

For more control, use the adapter functions directly:

```kotlin
@Composable
fun MyCustomTTSScreen(viewModel: TTSV2ViewModel) {
    // Convert v2 state to CommonTTSScreenState
    val commonState = rememberTTSV2StateAdapter(
        adapter = viewModel.adapter,
        sleepTimerState = viewModel.sleepTimerState?.collectAsState()?.value
    )
    
    // Create actions that dispatch to v2
    val actions = rememberTTSV2Actions(adapter = viewModel.adapter)
    
    // Use existing CommonTTSScreen components
    Column {
        TTSContentDisplay(state = commonState, actions = actions)
        TTSMediaControls(state = commonState, actions = actions)
    }
}
```

### Option 3: Use TTSV2Screen (Standalone UI)

Use `TTSV2Screen` for a simpler standalone implementation:

```kotlin
TTSV2Screen(
    adapter = viewModel.adapter,
    sleepTimerState = viewModel.sleepTimerState,
    onSleepTimerStart = { viewModel.startSleepTimer(it) },
    onSleepTimerCancel = { viewModel.cancelSleepTimer() },
    onBack = { navController.popBackStack() }
)
```

### Migration Steps

1. The v2 module is already included in DomainModule
2. Create `TTSV2ViewModel` with injected dependencies
3. Use `TTSStateAdapter` to bridge with existing UI
4. Test v2 independently alongside v1
5. Gradually migrate screens
6. Remove old implementation when complete

## Android Background Service

The `TTSV2Service` provides background playback with media controls:

```kotlin
// Start the service with a chapter
val intent = TTSV2Service.createIntent(context, bookId, chapterId, startParagraph)
context.startService(intent)

// Or bind to get controller access
val connection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as TTSV2Service.LocalBinder
        val controller = binder.getController()
        // Use controller to manage playback
    }
    
    override fun onServiceDisconnected(name: ComponentName?) {}
}
context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
```

Features:
- MediaSession integration for lock screen controls
- Audio focus handling
- Media-style notification with play/pause/next/previous/stop
- Automatic state synchronization with TTSController

**Note**: Register the service in AndroidManifest.xml:
```xml
<service
    android:name="ireader.domain.services.tts_service.v2.TTSV2Service"
    android:exported="false"
    android:foregroundServiceType="mediaPlayback" />
```

## File Structure

```
domain/src/commonMain/kotlin/ireader/domain/services/tts_service/v2/
├── TTSState.kt           # Immutable state
├── TTSCommand.kt         # Commands and events
├── TTSEngine.kt          # Engine interface
├── TTSController.kt      # Central coordinator
├── TTSContentLoaderImpl.kt # Content loading
├── TTSTextMergerV2.kt    # Text merging
├── TTSCacheUseCase.kt    # Chunk caching
├── TTSChunkPlayer.kt     # Chunk playback
├── TTSModule.kt          # Koin module
├── TTSViewModelAdapter.kt # UI adapter
├── TTSNotificationUseCase.kt # Notification management
├── TTSSleepTimerUseCase.kt   # Sleep timer
├── TTSPreferencesUseCase.kt  # Preferences bridge
└── README.md             # This file

domain/src/androidMain/kotlin/ireader/domain/services/tts_service/v2/
├── AndroidTTSEngineFactory.kt # Android engine implementations
├── TTSV2Service.kt            # Background service with MediaSession
└── TTSV2ActionReceiver.kt     # Broadcast receiver for notification actions

domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/v2/
└── DesktopTTSEngineFactory.kt # Desktop engine implementations

domain/src/iosMain/kotlin/ireader/domain/services/tts_service/v2/
└── IosTTSEngineFactory.kt     # iOS engine implementations

presentation/src/commonMain/kotlin/ireader/presentation/ui/home/tts/v2/
├── TTSV2Screen.kt        # Standalone UI with SleepTimerDialog
├── TTSV2ViewModel.kt     # ViewModel with all use cases
└── TTSStateAdapter.kt    # Bridge to CommonTTSScreen (TTSV2CommonScreen)

presentation/src/commonMain/kotlin/ireader/presentation/core/ui/
└── TTSV2ScreenSpec.kt    # Full-featured navigation screen spec
```

## v2 vs v1 Feature Comparison

| Feature | v1 (TTSScreenSpec) | v2 (TTSV2ScreenSpec) |
|---------|-------------------|---------------------|
| Translation support | ✅ | ✅ |
| Sentence highlighting | ✅ | ✅ |
| WPM calibration | ✅ Rolling average | ✅ Rolling average |
| Sleep timer | ✅ | ✅ |
| Chapter drawer | ✅ | ✅ |
| Settings panel | ✅ | ✅ |
| Engine settings | ✅ | ✅ |
| Voice selection | ✅ | ✅ |
| Translate chapter | ✅ | ✅ |
| Auto-translate next | ✅ | ✅ |
| Fullscreen mode | ✅ | ✅ |
| Custom colors | ✅ | ✅ |
| Download chapter audio | ✅ | ✅ |
| Background service | ✅ (Android) | ✅ (TTSV2Service) |
| Clean architecture | ❌ | ✅ |
| Single source of truth | ❌ | ✅ |
| Command pattern | ❌ | ✅ |
| Easy to debug | ❌ | ✅ |

## TODO

- [x] Download chapter audio button (for Gradio TTS caching) - Fully implemented with TTSEngine.generateAudioForText
- [x] Background service for Android - `TTSV2Service` with MediaSession and notification controls
- [x] More Gradio TTS presets - Added StyleTTS 2, Tortoise, Silero, OpenVoice, Fish Speech (12 total)
- [x] Register TTSV2Service in AndroidManifest.xml - Service and action receiver registered
- [ ] iOS Gradio TTS support

## Navigation

Navigate to v2 TTS screen:

```kotlin
// From reader or book detail
navController.navigateTo(
    TTSV2ScreenSpec(
        bookId = book.id,
        chapterId = chapter.id,
        sourceId = book.sourceId,
        readingParagraph = currentParagraph
    )
)

// Or use route directly
navController.navigate(NavigationRoutes.ttsV2(bookId, chapterId, sourceId, readingParagraph))
```
