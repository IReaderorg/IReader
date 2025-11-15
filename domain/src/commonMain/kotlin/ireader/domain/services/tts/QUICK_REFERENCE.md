# TTS Plugin Integration - Quick Reference

## Quick Start

### Get TTS Manager
```kotlin
val pluginTTSManager: PluginTTSManager = get() // Koin injection
```

### Get Available Voices
```kotlin
// All voices (built-in + plugins)
val voices = pluginTTSManager.getAvailableVoices()

// Only plugin voices
val pluginVoices = pluginTTSManager.getPluginVoices()

// Voices by language
val englishVoices = pluginTTSManager.getVoicesByLanguage("en")
```

### Speak Text
```kotlin
val result = pluginTTSManager.speak(
    text = "Hello world",
    voiceId = "en-us-amy-low",
    speed = 1.0f,
    pitch = 1.0f,
    volume = 1.0f
)

result.onSuccess { output ->
    // Handle TTS output
}.onFailure { error ->
    // Handle error
}
```

### Preview Voice
```kotlin
val result = pluginTTSManager.previewVoice("en-us-amy-low")
```

### Find Plugin for Voice
```kotlin
val plugin = pluginTTSManager.findPluginForVoice("custom-voice-id")
```

## Voice Configuration

```kotlin
val config = VoiceConfiguration(
    voiceId = "en-us-amy-low",
    speed = 1.2f,      // 0.5 - 2.0
    pitch = 1.0f,      // 0.5 - 2.0
    volume = 0.8f,     // 0.0 - 1.0
    enableStreaming = true
)

// Validate (clamps to valid ranges)
val validConfig = config.validate()
```

## Voice Filtering

```kotlin
val filter = VoiceFilter(
    language = "en",
    gender = "FEMALE",
    quality = "HIGH",
    sourceType = VoiceSourceType.PLUGIN,
    searchQuery = "natural"
)

val state = VoiceSelectionState(
    availableVoices = voices,
    filter = filter
)

val filtered = state.getFilteredVoices()
```

## Error Handling

```kotlin
val errorHandler = TTSErrorHandler()

try {
    val result = pluginTTSManager.speak(text, voiceId)
    result.onFailure { error ->
        val ttsError = errorHandler.exceptionToError(error as Exception)
        val errorResult = errorHandler.handleError(ttsError)
        
        when (errorResult) {
            is TTSErrorResult.Fallback -> {
                // Use built-in TTS
            }
            is TTSErrorResult.Retry -> {
                // Retry operation
            }
            is TTSErrorResult.Fatal -> {
                // Show error to user
            }
        }
    }
} catch (e: Exception) {
    val message = errorHandler.getUserMessage(
        errorHandler.exceptionToError(e)
    )
}
```

## Audio Stream Handling

```kotlin
val audioStreamHandler = AudioStreamHandler()

// Read full stream
val audioData = audioStreamHandler.readFullStream(audioStream)

// Stream audio
audioStreamHandler.streamAudio(
    audioStream = audioStream,
    onChunk = { buffer, bytesRead ->
        // Play chunk
    },
    onComplete = {
        // Done
    },
    onError = { error ->
        // Handle error
    }
)
```

## UI Integration

### Using PluginVoiceSelector
```kotlin
@Composable
fun MyScreen() {
    val viewModel: VoiceSelectionViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    
    PluginVoiceSelector(
        state = state,
        onVoiceSelected = { viewModel.selectVoice(it) },
        onPreviewVoice = { viewModel.previewVoice(it) },
        onConfigurationChanged = { viewModel.updateConfiguration(it) }
    )
}
```

### Using VoiceSelectionViewModel
```kotlin
class MyViewModel(
    private val voiceViewModel: VoiceSelectionViewModel
) : ViewModel() {
    
    fun speakText(text: String) {
        voiceViewModel.speak(text)
    }
    
    fun loadVoices() {
        voiceViewModel.loadVoices()
    }
}
```

## Voice Source Types

```kotlin
when (val source = voiceWithSource.source) {
    is VoiceSource.BuiltIn -> {
        // Built-in voice from VoiceCatalog
    }
    is VoiceSource.Plugin -> {
        // Plugin voice
        val pluginId = source.pluginId
        val pluginName = source.pluginName
    }
}
```

## TTS Output Types

```kotlin
when (output) {
    is TTSOutput.Plugin -> {
        val audioStream = output.audioStream
        val audioFormat = output.audioFormat
        val supportsStreaming = output.supportsStreaming
        val pluginId = output.pluginId
    }
    is TTSOutput.BuiltIn -> {
        val utteranceId = output.utteranceId
    }
}
```

## Common Patterns

### Check if voice supports streaming
```kotlin
if (pluginTTSManager.supportsStreaming(voiceId)) {
    // Use streaming
} else {
    // Use buffered playback
}
```

### Get all TTS plugins
```kotlin
val ttsPlugins = pluginTTSManager.getTTSPlugins()
val enabledPlugins = pluginTTSManager.getEnabledTTSPlugins()
```

### Observe plugin changes
```kotlin
pluginTTSManager.observePlugins().collect { plugins ->
    // Reload voices when plugins change
}
```

### Reset configuration
```kotlin
viewModel.resetConfiguration()
```

### Clear errors
```kotlin
viewModel.clearError()
```

## Constants

```kotlin
// Speed
VoiceConfiguration.MIN_SPEED = 0.5f
VoiceConfiguration.MAX_SPEED = 2.0f
VoiceConfiguration.DEFAULT_SPEED = 1.0f

// Pitch
VoiceConfiguration.MIN_PITCH = 0.5f
VoiceConfiguration.MAX_PITCH = 2.0f
VoiceConfiguration.DEFAULT_PITCH = 1.0f

// Volume
VoiceConfiguration.MIN_VOLUME = 0.0f
VoiceConfiguration.MAX_VOLUME = 1.0f
VoiceConfiguration.DEFAULT_VOLUME = 1.0f
```

## Koin Dependencies

```kotlin
// Inject in ViewModel
class MyViewModel(
    private val pluginTTSManager: PluginTTSManager,
    private val audioStreamHandler: AudioStreamHandler,
    private val errorHandler: TTSErrorHandler
) : ViewModel()

// Inject in Composable
@Composable
fun MyScreen() {
    val pluginTTSManager: PluginTTSManager = koinInject()
}
```

## Testing

```kotlin
class MyTest {
    private lateinit var pluginTTSManager: PluginTTSManager
    
    @Before
    fun setup() {
        val pluginManager = mockk<PluginManager>()
        pluginTTSManager = PluginTTSManager(pluginManager)
    }
    
    @Test
    fun `test voice loading`() {
        val voices = pluginTTSManager.getAvailableVoices()
        assertTrue(voices.isNotEmpty())
    }
}
```

## See Also

- **INTEGRATION_GUIDE.md** - Comprehensive integration guide
- **PLUGIN_EXAMPLE.md** - Example TTS plugin implementation
- **IMPLEMENTATION_SUMMARY.md** - Implementation overview
