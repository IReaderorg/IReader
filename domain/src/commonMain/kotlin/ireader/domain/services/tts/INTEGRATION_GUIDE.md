# TTS Plugin Integration Guide

This guide explains how to integrate TTS plugins with the IReader TTS system.

## Overview

The TTS plugin integration provides:
- Unified access to built-in and plugin TTS voices
- Voice configuration (speed, pitch, volume)
- Audio streaming support
- Error handling with fallback to built-in TTS
- Voice preview functionality

## Architecture

```
PluginTTSManager
├── VoiceCatalog (built-in voices)
├── PluginManager (plugin voices)
├── AudioStreamHandler (audio processing)
└── TTSErrorHandler (error handling)
```

## Requirements Covered

- **5.1**: Get available voices combining built-in and plugin voices
- **5.2**: Route TTS requests to appropriate plugin or built-in TTS
- **5.3**: Voice preview and audio stream handling
- **5.4**: Error handling with fallback to built-in TTS
- **5.5**: Streaming support for real-time playback

## Usage

### 1. Get Available Voices

```kotlin
val pluginTTSManager: PluginTTSManager = get() // Koin injection

// Get all voices (built-in + plugins)
val allVoices: List<VoiceWithSource> = pluginTTSManager.getAvailableVoices()

// Get only plugin voices
val pluginVoices: List<VoiceWithSource> = pluginTTSManager.getPluginVoices()

// Get voices by language
val englishVoices = pluginTTSManager.getVoicesByLanguage("en")

// Check voice source
allVoices.forEach { voiceWithSource ->
    when (val source = voiceWithSource.source) {
        is VoiceSource.BuiltIn -> {
            println("Built-in voice: ${voiceWithSource.voice.name}")
        }
        is VoiceSource.Plugin -> {
            println("Plugin voice: ${voiceWithSource.voice.name} from ${source.pluginName}")
        }
    }
}
```

### 2. Speak Text

```kotlin
// Speak with default settings
val result = pluginTTSManager.speak(
    text = "Hello, this is a test.",
    voiceId = "en-us-amy-low"
)

// Speak with custom configuration
val result = pluginTTSManager.speak(
    text = "Hello, this is a test.",
    voiceId = "en-us-amy-low",
    speed = 1.2f,
    pitch = 1.0f,
    volume = 0.8f
)

// Handle result
result.onSuccess { output ->
    when (output) {
        is TTSOutput.Plugin -> {
            // Plugin TTS output
            val audioStream = output.audioStream
            val audioFormat = output.audioFormat
            val supportsStreaming = output.supportsStreaming
            
            // Process audio stream
            if (supportsStreaming) {
                // Stream audio in real-time
                audioStreamHandler.streamAudio(
                    audioStream = audioStream,
                    onChunk = { buffer, bytesRead ->
                        // Play audio chunk
                    },
                    onComplete = {
                        println("Playback complete")
                    },
                    onError = { error ->
                        println("Playback error: ${error.message}")
                    }
                )
            } else {
                // Read full stream
                val audioData = audioStreamHandler.readFullStream(audioStream)
                audioData.onSuccess { bytes ->
                    // Play audio data
                }
            }
        }
        is TTSOutput.BuiltIn -> {
            // Built-in TTS output
            println("Using built-in TTS with utterance ID: ${output.utteranceId}")
        }
    }
}.onFailure { error ->
    println("TTS failed: ${error.message}")
}
```

### 3. Preview Voice

```kotlin
// Preview a voice with sample text
val result = pluginTTSManager.previewVoice(
    voiceId = "en-us-amy-low",
    sampleText = "This is how I sound."
)

result.onSuccess { output ->
    // Handle preview output (same as speak)
}
```

### 4. Find Plugin for Voice

```kotlin
// Find which plugin provides a specific voice
val plugin: TTSPlugin? = pluginTTSManager.findPluginForVoice("custom-voice-id")

if (plugin != null) {
    println("Voice provided by: ${plugin.manifest.name}")
    println("Supports streaming: ${plugin.supportsStreaming()}")
    println("Audio format: ${plugin.getAudioFormat()}")
}
```

### 5. Check Streaming Support

```kotlin
val supportsStreaming = pluginTTSManager.supportsStreaming("en-us-amy-low")
if (supportsStreaming) {
    println("Voice supports streaming playback")
}
```

### 6. Error Handling

```kotlin
val errorHandler = TTSErrorHandler()

try {
    val result = pluginTTSManager.speak(text, voiceId)
    result.onFailure { error ->
        val ttsError = errorHandler.exceptionToError(error as Exception, voiceId)
        val errorResult = errorHandler.handleError(ttsError)
        
        when (errorResult) {
            is TTSErrorResult.Fallback -> {
                if (errorResult.shouldFallback) {
                    // Try built-in TTS
                    println("Falling back to built-in TTS")
                }
            }
            is TTSErrorResult.Retry -> {
                if (errorResult.shouldRetry) {
                    // Retry the operation
                    println("Retrying...")
                }
            }
            is TTSErrorResult.Fatal -> {
                // Show error to user
                println("Fatal error: ${errorResult.message}")
            }
        }
        
        // Get user-friendly message
        val userMessage = errorHandler.getUserMessage(ttsError)
        println(userMessage)
    }
} catch (e: Exception) {
    val ttsError = errorHandler.exceptionToError(e, voiceId)
    println(errorHandler.getUserMessage(ttsError))
}
```

### 7. Voice Configuration

```kotlin
// Create voice configuration
val config = VoiceConfiguration(
    voiceId = "en-us-amy-low",
    speed = 1.2f,
    pitch = 1.0f,
    volume = 0.8f,
    enableStreaming = true
)

// Validate configuration (clamps values to valid ranges)
val validConfig = config.validate()

// Check if configuration is at defaults
if (config.isDefault()) {
    println("Using default configuration")
}

// Use configuration constants
println("Speed range: ${VoiceConfiguration.MIN_SPEED} - ${VoiceConfiguration.MAX_SPEED}")
println("Pitch range: ${VoiceConfiguration.MIN_PITCH} - ${VoiceConfiguration.MAX_PITCH}")
println("Volume range: ${VoiceConfiguration.MIN_VOLUME} - ${VoiceConfiguration.MAX_VOLUME}")
```

### 8. Voice Filtering

```kotlin
// Create voice selection state
val state = VoiceSelectionState(
    availableVoices = pluginTTSManager.getAvailableVoices(),
    filter = VoiceFilter(
        language = "en",
        sourceType = VoiceSourceType.PLUGIN,
        searchQuery = "female"
    )
)

// Get filtered voices
val filteredVoices = state.getFilteredVoices()

// Get available languages
val languages = state.getAvailableLanguages()

// Get available source types
val sourceTypes = state.getAvailableSourceTypes()
```

## UI Integration

### Using PluginVoiceSelector Composable

```kotlin
@Composable
fun TTSSettingsScreen() {
    val viewModel: VoiceSelectionViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    
    PluginVoiceSelector(
        state = state,
        onVoiceSelected = { voiceId ->
            viewModel.selectVoice(voiceId)
        },
        onPreviewVoice = { voiceId ->
            viewModel.previewVoice(voiceId)
        },
        onConfigurationChanged = { config ->
            viewModel.updateConfiguration(config)
        }
    )
}
```

### Using VoiceSelectionViewModel

```kotlin
class MyTTSScreen : ViewModel() {
    private val voiceSelectionViewModel: VoiceSelectionViewModel = get()
    
    fun speakText(text: String) {
        voiceSelectionViewModel.speak(text)
    }
    
    fun loadVoices() {
        voiceSelectionViewModel.loadVoices()
    }
    
    fun selectVoice(voiceId: String) {
        voiceSelectionViewModel.selectVoice(voiceId)
    }
}
```

## Platform-Specific Implementation

### Built-in TTS Integration

To integrate with platform-specific built-in TTS, implement the `speakWithBuiltIn` method:

```kotlin
// Android
private suspend fun speakWithBuiltIn(
    text: String,
    voice: VoiceModel,
    speed: Float,
    pitch: Float,
    volume: Float
): Result<TTSOutput> {
    val tts = TextToSpeech(context) { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts.setSpeechRate(speed)
            tts.setPitch(pitch)
            // Set voice based on VoiceModel
            val utteranceId = UUID.randomUUID().toString()
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }
    return Result.success(TTSOutput.BuiltIn(utteranceId))
}

// Desktop
private suspend fun speakWithBuiltIn(
    text: String,
    voice: VoiceModel,
    speed: Float,
    pitch: Float,
    volume: Float
): Result<TTSOutput> {
    // Use platform-specific TTS library
    // e.g., Piper TTS, eSpeak, etc.
}
```

## Testing

### Unit Tests

```kotlin
class PluginTTSManagerTest {
    private lateinit var pluginManager: PluginManager
    private lateinit var pluginTTSManager: PluginTTSManager
    
    @Before
    fun setup() {
        pluginManager = mockk()
        pluginTTSManager = PluginTTSManager(pluginManager)
    }
    
    @Test
    fun `getAvailableVoices returns built-in and plugin voices`() {
        // Mock plugin voices
        val mockPlugin = mockk<TTSPlugin>()
        every { mockPlugin.getAvailableVoices() } returns listOf(/* mock voices */)
        every { pluginManager.getEnabledPlugins() } returns listOf(mockPlugin)
        
        val voices = pluginTTSManager.getAvailableVoices()
        
        assertTrue(voices.isNotEmpty())
        assertTrue(voices.any { it.source is VoiceSource.BuiltIn })
        assertTrue(voices.any { it.source is VoiceSource.Plugin })
    }
}
```

## Best Practices

1. **Always validate voice configuration** before using it
2. **Handle errors gracefully** with fallback to built-in TTS
3. **Use streaming** for long text to improve responsiveness
4. **Cache voice lists** to avoid repeated plugin queries
5. **Monitor plugin changes** to update voice lists dynamically
6. **Provide voice preview** to help users choose voices
7. **Show voice source** (built-in vs plugin) in UI
8. **Test with various plugins** to ensure compatibility

## Troubleshooting

### Voice not found
- Check if the plugin is enabled
- Verify the voice ID is correct
- Reload voices after plugin changes

### Audio playback issues
- Check audio format compatibility
- Verify streaming support
- Test with built-in TTS as fallback

### Plugin errors
- Check plugin permissions
- Verify plugin is not disabled
- Check resource usage limits

## Future Enhancements

- Voice caching for offline use
- Voice quality assessment
- Automatic voice selection based on language
- Voice similarity matching
- Multi-voice support for dialogue
