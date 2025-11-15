# Plugin API Reference

Complete reference for the IReader Plugin API.

## Core Interfaces

### Plugin

Base interface that all plugins must implement.

```kotlin
interface Plugin {
    val manifest: PluginManifest
    fun initialize(context: PluginContext)
    fun cleanup()
}
```

#### Properties

- `manifest: PluginManifest` - Plugin metadata and configuration

#### Methods

- `initialize(context: PluginContext)` - Called when plugin is loaded
- `cleanup()` - Called when plugin is unloaded or disabled

### PluginContext

Provides access to app resources and services.

```kotlin
interface PluginContext {
    val preferences: PluginPreferences
    val fileStorage: PluginFileStorage
    val networkClient: PluginNetworkClient
    val permissions: PluginPermissions
}
```

#### Properties

- `preferences` - Key-value storage for plugin settings
- `fileStorage` - File system access within plugin directory
- `networkClient` - HTTP client for network requests
- `permissions` - Permission checking and requesting

## Plugin Types

### ThemePlugin

Interface for theme plugins.

```kotlin
interface ThemePlugin : Plugin {
    fun getColorScheme(isDark: Boolean): ColorScheme
    fun getExtraColors(isDark: Boolean): ExtraColors
    fun getTypography(): Typography?
    fun getBackgroundAssets(): ThemeBackgrounds?
}
```

#### Methods

##### `getColorScheme(isDark: Boolean): ColorScheme`

Returns Material 3 color scheme for the theme.

**Parameters:**
- `isDark` - Whether to return dark or light theme

**Returns:** `ColorScheme` with all required colors

**Example:**
```kotlin
override fun getColorScheme(isDark: Boolean): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = Color(0xFF6200EE),
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF03DAC6),
            // ... more colors
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6200EE),
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF03DAC6),
            // ... more colors
        )
    }
}
```

##### `getExtraColors(isDark: Boolean): ExtraColors`

Returns IReader-specific colors.

**Parameters:**
- `isDark` - Whether to return dark or light colors

**Returns:** `ExtraColors` with custom color definitions

##### `getTypography(): Typography?`

Returns custom typography or null to use default.

**Returns:** `Typography` or `null`

##### `getBackgroundAssets(): ThemeBackgrounds?`

Returns custom background images or null.

**Returns:** `ThemeBackgrounds` or `null`

### TranslationPlugin

Interface for translation plugins.

```kotlin
interface TranslationPlugin : Plugin {
    suspend fun translate(text: String, from: String, to: String): Result<String>
    suspend fun translateBatch(texts: List<String>, from: String, to: String): Result<List<String>>
    fun getSupportedLanguages(): List<LanguagePair>
    fun requiresApiKey(): Boolean
    fun configureApiKey(key: String)
}
```

#### Methods

##### `translate(text: String, from: String, to: String): Result<String>`

Translates a single text string.

**Parameters:**
- `text` - Text to translate
- `from` - Source language code (ISO 639-1)
- `to` - Target language code (ISO 639-1)

**Returns:** `Result<String>` with translated text or error

**Example:**
```kotlin
override suspend fun translate(text: String, from: String, to: String): Result<String> {
    return try {
        val response = networkClient.post("https://api.example.com/translate") {
            json {
                "text" to text
                "from" to from
                "to" to to
            }
        }
        Result.success(response.body.translated)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

##### `translateBatch(texts: List<String>, from: String, to: String): Result<List<String>>`

Translates multiple texts in one request (more efficient).

**Parameters:**
- `texts` - List of texts to translate
- `from` - Source language code
- `to` - Target language code

**Returns:** `Result<List<String>>` with translated texts

##### `getSupportedLanguages(): List<LanguagePair>`

Returns supported language pairs.

**Returns:** List of `LanguagePair` objects

##### `requiresApiKey(): Boolean`

Whether plugin requires API key configuration.

**Returns:** `true` if API key needed

##### `configureApiKey(key: String)`

Sets the API key for the translation service.

**Parameters:**
- `key` - API key string

### TTSPlugin

Interface for text-to-speech plugins.

```kotlin
interface TTSPlugin : Plugin {
    suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream>
    fun getAvailableVoices(): List<VoiceModel>
    fun supportsStreaming(): Boolean
    fun getAudioFormat(): AudioFormat
}
```

#### Methods

##### `speak(text: String, voice: VoiceConfig): Result<AudioStream>`

Converts text to speech.

**Parameters:**
- `text` - Text to speak
- `voice` - Voice configuration

**Returns:** `Result<AudioStream>` with audio data

**Example:**
```kotlin
override suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream> {
    return try {
        val audioData = synthesizeSpeech(text, voice)
        Result.success(AudioStream(audioData, getAudioFormat()))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

##### `getAvailableVoices(): List<VoiceModel>`

Returns list of available voices.

**Returns:** List of `VoiceModel` objects

##### `supportsStreaming(): Boolean`

Whether plugin supports streaming audio.

**Returns:** `true` if streaming supported

##### `getAudioFormat(): AudioFormat`

Returns audio format specification.

**Returns:** `AudioFormat` with codec, sample rate, etc.

### FeaturePlugin

Interface for custom feature plugins.

```kotlin
interface FeaturePlugin : Plugin {
    fun getMenuItems(): List<PluginMenuItem>
    fun getScreens(): List<PluginScreen>
    fun onReaderContext(context: ReaderContext): PluginAction?
    fun getPreferencesScreen(): PluginScreen?
}
```

#### Methods

##### `getMenuItems(): List<PluginMenuItem>`

Returns menu items to add to reader interface.

**Returns:** List of `PluginMenuItem` objects

**Example:**
```kotlin
override fun getMenuItems(): List<PluginMenuItem> {
    return listOf(
        PluginMenuItem(
            id = "export_notes",
            label = "Export Notes",
            icon = "ic_export",
            action = { exportNotes() }
        )
    )
}
```

##### `getScreens(): List<PluginScreen>`

Returns custom screens to register with navigation.

**Returns:** List of `PluginScreen` objects

##### `onReaderContext(context: ReaderContext): PluginAction?`

Called when reader context changes (text selection, chapter change, etc.).

**Parameters:**
- `context` - Current reader context

**Returns:** `PluginAction` to execute or `null`

##### `getPreferencesScreen(): PluginScreen?`

Returns plugin settings screen or null.

**Returns:** `PluginScreen` or `null`

## Data Classes

### PluginManifest

```kotlin
data class PluginManifest(
    val id: String,
    val name: String,
    val version: String,
    val versionCode: Int,
    val description: String,
    val author: PluginAuthor,
    val type: PluginType,
    val permissions: List<PluginPermission>,
    val minIReaderVersion: String,
    val platforms: List<Platform>,
    val monetization: PluginMonetization?,
    val iconUrl: String?,
    val screenshotUrls: List<String>
)
```

### VoiceConfig

```kotlin
data class VoiceConfig(
    val voiceId: String,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val volume: Float = 1.0f
)
```

### ReaderContext

```kotlin
data class ReaderContext(
    val bookId: Long,
    val chapterId: Long,
    val selectedText: String?,
    val currentPosition: Int
)
```

## Enums

### PluginType

```kotlin
enum class PluginType {
    THEME,
    TRANSLATION,
    TTS,
    FEATURE
}
```

### PluginPermission

```kotlin
enum class PluginPermission {
    NETWORK,           // Access to network
    STORAGE,           // Access to local storage
    READER_CONTEXT,    // Access to reading context
    LIBRARY_ACCESS,    // Access to user's library
    PREFERENCES,       // Access to app preferences
    NOTIFICATIONS      // Show notifications
}
```

### Platform

```kotlin
enum class Platform {
    ANDROID,
    IOS,
    DESKTOP
}
```

## Helper Classes

### PluginPreferences

Key-value storage for plugin settings.

```kotlin
interface PluginPreferences {
    fun getString(key: String, default: String = ""): String
    fun putString(key: String, value: String)
    fun getInt(key: String, default: Int = 0): Int
    fun putInt(key: String, value: Int)
    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun remove(key: String)
    fun clear()
}
```

### PluginFileStorage

File system access within plugin directory.

```kotlin
interface PluginFileStorage {
    fun getPluginDir(): File
    fun readFile(path: String): ByteArray
    fun writeFile(path: String, data: ByteArray)
    fun deleteFile(path: String)
    fun listFiles(): List<String>
}
```

### PluginNetworkClient

HTTP client for network requests.

```kotlin
interface PluginNetworkClient {
    suspend fun get(url: String): HttpResponse
    suspend fun post(url: String, body: RequestBody): HttpResponse
    suspend fun download(url: String, destination: File): Result<Unit>
}
```

## Error Handling

All async methods return `Result<T>` for error handling:

```kotlin
when (val result = plugin.translate(text, "en", "es")) {
    is Result.Success -> {
        val translated = result.value
        // Use translated text
    }
    is Result.Failure -> {
        val error = result.exception
        // Handle error
    }
}
```

## Best Practices

1. **Always handle errors**: Use Result types properly
2. **Clean up resources**: Implement cleanup() properly
3. **Be async-friendly**: Use suspend functions for long operations
4. **Respect permissions**: Check permissions before accessing resources
5. **Cache when possible**: Reduce network and computation
6. **Provide feedback**: Use logging for debugging
7. **Test thoroughly**: Use the testing framework
