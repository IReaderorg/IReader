# TTS Plugin Example

This document provides an example of how to create a TTS plugin for IReader.

## Example: Custom TTS Plugin

```kotlin
package com.example.customtts

import ireader.domain.models.tts.VoiceGender
import ireader.domain.models.tts.VoiceModel
import ireader.domain.models.tts.VoiceQuality
import ireader.domain.plugins.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

/**
 * Example TTS plugin that provides custom voices
 */
class CustomTTSPlugin : TTSPlugin {
    
    override val manifest = PluginManifest(
        id = "com.example.customtts",
        name = "Custom TTS Engine",
        version = "1.0.0",
        versionCode = 1,
        description = "High-quality TTS engine with custom voices",
        author = PluginAuthor(
            name = "Example Developer",
            email = "dev@example.com",
            website = "https://example.com"
        ),
        type = PluginType.TTS,
        permissions = listOf(
            PluginPermission.NETWORK,  // For cloud TTS
            PluginPermission.STORAGE   // For caching
        ),
        minIReaderVersion = "1.0.0",
        platforms = listOf(Platform.ANDROID, Platform.DESKTOP),
        monetization = PluginMonetization.Freemium(
            features = listOf(
                PremiumFeature(
                    id = "premium_voices",
                    name = "Premium Voices",
                    description = "Access to high-quality premium voices",
                    price = 4.99,
                    currency = "USD"
                )
            )
        ),
        iconUrl = "https://example.com/icon.png",
        screenshotUrls = listOf(
            "https://example.com/screenshot1.png",
            "https://example.com/screenshot2.png"
        )
    )
    
    private lateinit var context: PluginContext
    private val ttsEngine = CustomTTSEngine() // Your TTS implementation
    
    override fun initialize(context: PluginContext) {
        this.context = context
        
        // Initialize TTS engine
        ttsEngine.initialize()
        
        // Load cached voices
        loadCachedVoices()
    }
    
    override fun cleanup() {
        // Cleanup resources
        ttsEngine.shutdown()
    }
    
    override suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream> {
        return withContext(Dispatchers.IO) {
            try {
                // Check if voice is available
                val voiceModel = getAvailableVoices().find { it.id == voice.voiceId }
                    ?: return@withContext Result.failure(
                        Exception("Voice not found: ${voice.voiceId}")
                    )
                
                // Generate audio
                val audioData = ttsEngine.synthesize(
                    text = text,
                    voiceId = voice.voiceId,
                    speed = voice.speed,
                    pitch = voice.pitch,
                    volume = voice.volume
                )
                
                // Create audio stream
                val audioStream = ByteArrayAudioStream(audioData)
                
                Result.success(audioStream)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override fun getAvailableVoices(): List<VoiceModel> {
        return listOf(
            VoiceModel(
                id = "custom-en-us-sarah",
                name = "Sarah (US English)",
                language = "en",
                locale = "en-US",
                gender = VoiceGender.FEMALE,
                quality = VoiceQuality.HIGH,
                sampleRate = 44100,
                modelSize = 85_000_000,
                downloadUrl = "https://example.com/voices/sarah.model",
                configUrl = "https://example.com/voices/sarah.json",
                checksum = "sha256:abc123...",
                license = "Proprietary",
                description = "Natural and expressive US English female voice",
                tags = listOf("english", "us", "female", "expressive")
            ),
            VoiceModel(
                id = "custom-en-us-james",
                name = "James (US English)",
                language = "en",
                locale = "en-US",
                gender = VoiceGender.MALE,
                quality = VoiceQuality.HIGH,
                sampleRate = 44100,
                modelSize = 90_000_000,
                downloadUrl = "https://example.com/voices/james.model",
                configUrl = "https://example.com/voices/james.json",
                checksum = "sha256:def456...",
                license = "Proprietary",
                description = "Professional US English male voice with deep tone",
                tags = listOf("english", "us", "male", "professional")
            ),
            // Premium voice (requires purchase)
            VoiceModel(
                id = "custom-en-us-premium-emma",
                name = "Emma Premium (US English)",
                language = "en",
                locale = "en-US",
                gender = VoiceGender.FEMALE,
                quality = VoiceQuality.PREMIUM,
                sampleRate = 48000,
                modelSize = 120_000_000,
                downloadUrl = "https://example.com/voices/emma-premium.model",
                configUrl = "https://example.com/voices/emma-premium.json",
                checksum = "sha256:ghi789...",
                license = "Proprietary",
                description = "Ultra-realistic premium voice with emotional expression",
                tags = listOf("english", "us", "female", "premium", "expressive")
            )
        )
    }
    
    override fun supportsStreaming(): Boolean {
        return true // This plugin supports streaming
    }
    
    override fun getAudioFormat(): AudioFormat {
        return AudioFormat(
            encoding = AudioEncoding.WAV,
            sampleRate = 44100,
            channels = 1,
            bitDepth = 16
        )
    }
    
    private fun loadCachedVoices() {
        // Load cached voice data from plugin storage
        val cachedData = context.getPreference("cached_voices", "")
        // Process cached data...
    }
}

/**
 * Custom TTS engine implementation
 */
private class CustomTTSEngine {
    fun initialize() {
        // Initialize TTS engine
    }
    
    fun shutdown() {
        // Cleanup resources
    }
    
    fun synthesize(
        text: String,
        voiceId: String,
        speed: Float,
        pitch: Float,
        volume: Float
    ): ByteArray {
        // Implement TTS synthesis
        // This is where you would call your TTS library/API
        
        // Example: Call cloud TTS API
        // val response = httpClient.post("https://api.example.com/tts") {
        //     body = json {
        //         "text" to text
        //         "voice" to voiceId
        //         "speed" to speed
        //         "pitch" to pitch
        //     }
        // }
        // return response.bodyAsBytes()
        
        // For this example, return empty audio
        return ByteArray(0)
    }
}

/**
 * Simple audio stream implementation
 */
private class ByteArrayAudioStream(
    private val data: ByteArray
) : AudioStream {
    private var position = 0
    
    override suspend fun read(buffer: ByteArray): Int {
        if (position >= data.size) {
            return -1 // End of stream
        }
        
        val bytesToRead = minOf(buffer.size, data.size - position)
        System.arraycopy(data, position, buffer, 0, bytesToRead)
        position += bytesToRead
        
        return bytesToRead
    }
    
    override fun close() {
        // Nothing to close
    }
    
    override fun getDuration(): Long? {
        // Calculate duration based on audio format
        // For 16-bit mono at 44100 Hz:
        // duration = (data.size / 2) / 44100 * 1000
        return (data.size / 2) / 44100L * 1000
    }
}
```

## Plugin Manifest (plugin.json)

```json
{
  "id": "com.example.customtts",
  "name": "Custom TTS Engine",
  "version": "1.0.0",
  "versionCode": 1,
  "description": "High-quality TTS engine with custom voices",
  "author": {
    "name": "Example Developer",
    "email": "dev@example.com",
    "website": "https://example.com"
  },
  "type": "TTS",
  "permissions": [
    "NETWORK",
    "STORAGE"
  ],
  "minIReaderVersion": "1.0.0",
  "platforms": [
    "ANDROID",
    "DESKTOP"
  ],
  "monetization": {
    "type": "FREEMIUM",
    "features": [
      {
        "id": "premium_voices",
        "name": "Premium Voices",
        "description": "Access to high-quality premium voices",
        "price": 4.99,
        "currency": "USD"
      }
    ]
  },
  "iconUrl": "https://example.com/icon.png",
  "screenshotUrls": [
    "https://example.com/screenshot1.png",
    "https://example.com/screenshot2.png"
  ]
}
```

## Building the Plugin

1. **Compile the plugin code**:
```bash
./gradlew :customtts:build
```

2. **Package as .iplugin file**:
```bash
# Create plugin package (ZIP format with .iplugin extension)
zip -r customtts.iplugin \
    plugin.json \
    classes/ \
    resources/
```

3. **Test the plugin**:
```kotlin
// In your test code
val pluginManager: PluginManager = get()
val pluginFile = File("customtts.iplugin")

// Install plugin
pluginManager.installPlugin(pluginFile).onSuccess { pluginInfo ->
    println("Plugin installed: ${pluginInfo.manifest.name}")
    
    // Enable plugin
    pluginManager.enablePlugin(pluginInfo.id).onSuccess {
        println("Plugin enabled")
        
        // Test TTS
        val ttsManager: PluginTTSManager = get()
        val voices = ttsManager.getAvailableVoices()
        println("Available voices: ${voices.size}")
    }
}
```

## Advanced Features

### Streaming Audio

```kotlin
override suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream> {
    return Result.success(StreamingAudioStream(text, voice))
}

private class StreamingAudioStream(
    private val text: String,
    private val voice: VoiceConfig
) : AudioStream {
    private val chunks = mutableListOf<ByteArray>()
    private var currentChunk = 0
    private var positionInChunk = 0
    
    init {
        // Start streaming synthesis in background
        startStreaming()
    }
    
    private fun startStreaming() {
        // Generate audio in chunks
        // This allows playback to start before synthesis is complete
    }
    
    override suspend fun read(buffer: ByteArray): Int {
        // Read from current chunk
        // Move to next chunk when current is exhausted
    }
    
    override fun close() {
        // Stop streaming
    }
    
    override fun getDuration(): Long? {
        return null // Unknown for streaming
    }
}
```

### Voice Caching

```kotlin
private fun cacheVoice(voiceId: String, audioData: ByteArray) {
    val cacheKey = "voice_cache_$voiceId"
    context.putPreference(cacheKey, audioData.encodeBase64())
}

private fun getCachedVoice(voiceId: String): ByteArray? {
    val cacheKey = "voice_cache_$voiceId"
    val cached = context.getPreference(cacheKey, "")
    return if (cached.isNotEmpty()) {
        cached.decodeBase64()
    } else {
        null
    }
}
```

### Error Handling

```kotlin
override suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream> {
    return try {
        // Check permissions
        if (!context.hasPermission(PluginPermission.NETWORK)) {
            return Result.failure(
                Exception("Network permission required for cloud TTS")
            )
        }
        
        // Check if voice requires premium
        val voiceModel = getAvailableVoices().find { it.id == voice.voiceId }
        if (voiceModel?.quality == VoiceQuality.PREMIUM) {
            // Check if user has purchased premium voices
            // This would be handled by the monetization system
        }
        
        // Synthesize audio
        val audioData = ttsEngine.synthesize(text, voice)
        Result.success(ByteArrayAudioStream(audioData))
        
    } catch (e: NetworkException) {
        Result.failure(Exception("Network error: ${e.message}"))
    } catch (e: Exception) {
        Result.failure(Exception("TTS synthesis failed: ${e.message}"))
    }
}
```

## Testing Your Plugin

```kotlin
class CustomTTSPluginTest {
    private lateinit var plugin: CustomTTSPlugin
    private lateinit var context: PluginContext
    
    @Before
    fun setup() {
        context = mockk()
        plugin = CustomTTSPlugin()
        plugin.initialize(context)
    }
    
    @Test
    fun `plugin provides voices`() {
        val voices = plugin.getAvailableVoices()
        assertTrue(voices.isNotEmpty())
        assertTrue(voices.any { it.language == "en" })
    }
    
    @Test
    fun `plugin can synthesize speech`() = runBlocking {
        val result = plugin.speak(
            text = "Hello world",
            voice = VoiceConfig(voiceId = "custom-en-us-sarah")
        )
        
        assertTrue(result.isSuccess)
        val audioStream = result.getOrNull()
        assertNotNull(audioStream)
    }
    
    @Test
    fun `plugin supports streaming`() {
        assertTrue(plugin.supportsStreaming())
    }
}
```

## Distribution

1. **Submit to Plugin Marketplace**:
   - Upload .iplugin file
   - Provide screenshots
   - Write description
   - Set pricing (if premium)

2. **Update Plugin**:
   - Increment version code
   - Update changelog
   - Rebuild and resubmit

3. **Monitor Usage**:
   - Check download statistics
   - Read user reviews
   - Fix reported issues
