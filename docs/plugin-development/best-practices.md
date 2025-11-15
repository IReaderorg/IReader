# Plugin Development Best Practices

Guidelines for creating high-quality, performant, and secure plugins.

## Code Quality

### 1. Follow Kotlin Conventions

```kotlin
// Good: Clear naming and structure
class MyTranslationPlugin : TranslationPlugin {
    private val apiClient = HttpClient()
    
    override suspend fun translate(text: String, from: String, to: String): Result<String> {
        return runCatching {
            apiClient.translate(text, from, to)
        }
    }
}

// Bad: Poor naming and structure
class mtp : TranslationPlugin {
    var c = HttpClient()
    
    override suspend fun translate(t: String, f: String, to: String): Result<String> {
        return Result.success(c.t(t, f, to))
    }
}
```

### 2. Use Proper Error Handling

```kotlin
// Good: Comprehensive error handling
override suspend fun translate(text: String, from: String, to: String): Result<String> {
    return try {
        validateLanguages(from, to)
        val response = apiClient.post(endpoint) {
            json { "text" to text, "from" to from, "to" to to }
        }
        
        if (response.isSuccess) {
            Result.success(response.body.translated)
        } else {
            Result.failure(TranslationException("API returned ${response.code}"))
        }
    } catch (e: NetworkException) {
        Result.failure(TranslationException("Network error: ${e.message}", e))
    } catch (e: Exception) {
        Result.failure(TranslationException("Unexpected error: ${e.message}", e))
    }
}

// Bad: Swallowing errors
override suspend fun translate(text: String, from: String, to: String): Result<String> {
    try {
        return Result.success(apiClient.translate(text))
    } catch (e: Exception) {
        return Result.success("") // Don't do this!
    }
}
```

### 3. Implement Proper Cleanup

```kotlin
// Good: Clean up resources
class MyPlugin : Plugin {
    private val httpClient = HttpClient()
    private val cache = mutableMapOf<String, String>()
    
    override fun cleanup() {
        httpClient.close()
        cache.clear()
    }
}

// Bad: Resource leaks
class MyPlugin : Plugin {
    private val httpClient = HttpClient()
    
    override fun cleanup() {
        // Nothing - client stays open!
    }
}
```

## Performance

### 1. Cache Aggressively

```kotlin
class MyTranslationPlugin : TranslationPlugin {
    private val cache = LruCache<String, String>(maxSize = 1000)
    
    override suspend fun translate(text: String, from: String, to: String): Result<String> {
        val cacheKey = "$text:$from:$to"
        
        // Check cache first
        cache[cacheKey]?.let { return Result.success(it) }
        
        // Fetch and cache
        return apiClient.translate(text, from, to).also { result ->
            result.getOrNull()?.let { cache[cacheKey] = it }
        }
    }
}
```

### 2. Use Batch Operations

```kotlin
// Good: Batch translation
override suspend fun translateBatch(
    texts: List<String>,
    from: String,
    to: String
): Result<List<String>> {
    return apiClient.translateBatch(texts, from, to)
}

// Bad: Individual requests
override suspend fun translateBatch(
    texts: List<String>,
    from: String,
    to: String
): Result<List<String>> {
    return Result.success(texts.map { translate(it, from, to).getOrThrow() })
}
```

### 3. Lazy Load Resources

```kotlin
class MyThemePlugin : ThemePlugin {
    // Good: Lazy loading
    private val backgroundImage by lazy {
        loadImageFromResources("background.png")
    }
    
    // Bad: Eager loading
    private val eagerBackground = loadImageFromResources("background.png")
}
```

### 4. Avoid Blocking Operations

```kotlin
// Good: Async operations
override suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream> {
    return withContext(Dispatchers.IO) {
        synthesizeSpeech(text, voice)
    }
}

// Bad: Blocking main thread
override suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream> {
    return synthesizeSpeech(text, voice) // Blocks!
}
```

## Security

### 1. Validate All Inputs

```kotlin
override suspend fun translate(text: String, from: String, to: String): Result<String> {
    // Validate inputs
    if (text.length > MAX_TEXT_LENGTH) {
        return Result.failure(IllegalArgumentException("Text too long"))
    }
    
    if (!isValidLanguageCode(from) || !isValidLanguageCode(to)) {
        return Result.failure(IllegalArgumentException("Invalid language code"))
    }
    
    // Proceed with translation
    return performTranslation(text, from, to)
}
```

### 2. Sanitize User Data

```kotlin
// Good: Sanitize before using
fun saveUserNote(note: String) {
    val sanitized = note
        .take(MAX_NOTE_LENGTH)
        .replace(Regex("[<>]"), "")
    
    preferences.putString("note", sanitized)
}

// Bad: Direct use of user input
fun saveUserNote(note: String) {
    preferences.putString("note", note)
}
```

### 3. Use HTTPS Only

```kotlin
// Good: HTTPS endpoints
private const val API_ENDPOINT = "https://api.example.com"

// Bad: HTTP endpoints
private const val API_ENDPOINT = "http://api.example.com"
```

### 4. Store Secrets Securely

```kotlin
// Good: Use secure storage
override fun configureApiKey(key: String) {
    context.preferences.putString("api_key", key)
}

// Bad: Hardcoded secrets
private const val API_KEY = "sk_live_abc123..." // Never do this!
```

## User Experience

### 1. Provide Clear Error Messages

```kotlin
// Good: Helpful error messages
sealed class TranslationError : Exception() {
    object NetworkError : TranslationError() {
        override val message = "Unable to connect. Check your internet connection."
    }
    
    object QuotaExceeded : TranslationError() {
        override val message = "Translation quota exceeded. Please try again later."
    }
    
    data class InvalidLanguage(val code: String) : TranslationError() {
        override val message = "Language '$code' is not supported."
    }
}

// Bad: Generic errors
throw Exception("Error") // Not helpful!
```

### 2. Show Progress for Long Operations

```kotlin
override suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream> {
    return withContext(Dispatchers.IO) {
        // Show progress
        context.showProgress("Generating speech...")
        
        val audio = synthesizeSpeech(text, voice)
        
        context.hideProgress()
        
        Result.success(audio)
    }
}
```

### 3. Respect User Preferences

```kotlin
class MyTTSPlugin : TTSPlugin {
    override suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream> {
        // Use user's preferred speed
        val userSpeed = context.preferences.getFloat("speech_speed", 1.0f)
        val adjustedVoice = voice.copy(speed = userSpeed)
        
        return synthesizeSpeech(text, adjustedVoice)
    }
}
```

## Testing

### 1. Write Unit Tests

```kotlin
class MyTranslationPluginTest {
    private lateinit var plugin: MyTranslationPlugin
    private lateinit var mockContext: PluginContext
    
    @Before
    fun setup() {
        mockContext = MockPluginContext()
        plugin = MyTranslationPlugin()
        plugin.initialize(mockContext)
    }
    
    @Test
    fun `translate returns success for valid input`() = runTest {
        val result = plugin.translate("Hello", "en", "es")
        
        assertTrue(result.isSuccess)
        assertEquals("Hola", result.getOrNull())
    }
    
    @Test
    fun `translate returns failure for invalid language`() = runTest {
        val result = plugin.translate("Hello", "invalid", "es")
        
        assertTrue(result.isFailure)
    }
}
```

### 2. Test Edge Cases

```kotlin
@Test
fun `translate handles empty text`() = runTest {
    val result = plugin.translate("", "en", "es")
    assertTrue(result.isSuccess)
    assertEquals("", result.getOrNull())
}

@Test
fun `translate handles very long text`() = runTest {
    val longText = "a".repeat(10000)
    val result = plugin.translate(longText, "en", "es")
    // Should either succeed or fail gracefully
    assertNotNull(result)
}

@Test
fun `translate handles special characters`() = runTest {
    val text = "Hello! @#$%^&*()"
    val result = plugin.translate(text, "en", "es")
    assertTrue(result.isSuccess)
}
```

## Monetization

### 1. Implement Fair Trials

```kotlin
class MyPremiumPlugin : Plugin {
    private val trialEndDate: Long
        get() = context.preferences.getLong("trial_end", 0)
    
    fun isTrialActive(): Boolean {
        if (trialEndDate == 0L) {
            // Start trial
            val endDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
            context.preferences.putLong("trial_end", endDate)
            return true
        }
        
        return System.currentTimeMillis() < trialEndDate
    }
}
```

### 2. Graceful Feature Locking

```kotlin
// Good: Clear messaging
fun usePremiumFeature() {
    if (!isPurchased) {
        context.showDialog(
            title = "Premium Feature",
            message = "This feature requires a premium upgrade.",
            actions = listOf("Upgrade", "Cancel")
        )
        return
    }
    
    // Use feature
}

// Bad: Silent failure
fun usePremiumFeature() {
    if (!isPurchased) return // User doesn't know why
    // Use feature
}
```

## Documentation

### 1. Document Public APIs

```kotlin
/**
 * Translates text from one language to another.
 *
 * @param text The text to translate (max 5000 characters)
 * @param from Source language code (ISO 639-1)
 * @param to Target language code (ISO 639-1)
 * @return Result containing translated text or error
 *
 * @throws IllegalArgumentException if text exceeds max length
 * @throws NetworkException if network request fails
 */
override suspend fun translate(
    text: String,
    from: String,
    to: String
): Result<String>
```

### 2. Provide Usage Examples

```kotlin
/**
 * Example usage:
 * ```kotlin
 * val plugin = MyTranslationPlugin()
 * plugin.initialize(context)
 *
 * val result = plugin.translate("Hello", "en", "es")
 * when (result) {
 *     is Result.Success -> println(result.value) // "Hola"
 *     is Result.Failure -> println(result.exception.message)
 * }
 * ```
 */
class MyTranslationPlugin : TranslationPlugin
```

## Versioning

### 1. Follow Semantic Versioning

- **MAJOR**: Breaking API changes
- **MINOR**: New features, backward compatible
- **PATCH**: Bug fixes, backward compatible

```kotlin
// Version 1.0.0 -> 1.1.0 (new feature)
fun newFeature() { }

// Version 1.1.0 -> 1.1.1 (bug fix)
fun fixBug() { }

// Version 1.1.1 -> 2.0.0 (breaking change)
fun breakingChange() { }
```

### 2. Maintain Changelog

Keep a CHANGELOG.md in your plugin:

```markdown
# Changelog

## [1.1.0] - 2024-01-15
### Added
- Support for 10 new languages
- Batch translation API

### Fixed
- Memory leak in cache

## [1.0.0] - 2024-01-01
### Added
- Initial release
- Basic translation functionality
```

## Platform Compatibility

### 1. Use Expect/Actual for Platform-Specific Code

```kotlin
// commonMain
expect fun getPlatformName(): String

// androidMain
actual fun getPlatformName(): String = "Android"

// iosMain
actual fun getPlatformName(): String = "iOS"

// desktopMain
actual fun getPlatformName(): String = "Desktop"
```

### 2. Test on All Target Platforms

Ensure your plugin works on all platforms you claim to support.

## Summary

- Write clean, maintainable code
- Handle errors gracefully
- Optimize for performance
- Prioritize security
- Focus on user experience
- Test thoroughly
- Document well
- Version properly
- Support multiple platforms
