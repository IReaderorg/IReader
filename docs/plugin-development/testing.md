# Plugin Testing Guide

Comprehensive guide for testing your IReader plugins.

## Testing Framework

IReader provides a testing framework with mock implementations for plugin development.

### Setup

Add testing dependencies to your `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.github.ireader:plugin-test-framework:1.0.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            }
        }
    }
}
```

## Mock Plugin Context

The framework provides `MockPluginContext` for testing:

```kotlin
import ireader.domain.plugins.test.MockPluginContext
import kotlin.test.*

class MyPluginTest {
    private lateinit var context: MockPluginContext
    private lateinit var plugin: MyPlugin
    
    @BeforeTest
    fun setup() {
        context = MockPluginContext()
        plugin = MyPlugin()
        plugin.initialize(context)
    }
    
    @AfterTest
    fun teardown() {
        plugin.cleanup()
    }
    
    @Test
    fun testPluginInitialization() {
        assertNotNull(plugin.manifest)
        assertEquals("com.example.myplugin", plugin.manifest.id)
    }
}
```

## Testing Plugin Types

### Theme Plugin Testing

```kotlin
class MyThemePluginTest {
    private lateinit var plugin: MyThemePlugin
    
    @BeforeTest
    fun setup() {
        plugin = MyThemePlugin()
        plugin.initialize(MockPluginContext())
    }
    
    @Test
    fun `getColorScheme returns valid dark theme`() {
        val colorScheme = plugin.getColorScheme(isDark = true)
        
        assertNotNull(colorScheme)
        assertNotNull(colorScheme.primary)
        assertNotNull(colorScheme.secondary)
        // Verify all required colors are set
    }
    
    @Test
    fun `getColorScheme returns valid light theme`() {
        val colorScheme = plugin.getColorScheme(isDark = false)
        
        assertNotNull(colorScheme)
        // Verify light theme colors
    }
    
    @Test
    fun `getExtraColors returns valid colors`() {
        val extraColors = plugin.getExtraColors(isDark = true)
        
        assertNotNull(extraColors)
        assertNotNull(extraColors.bars)
        assertNotNull(extraColors.onBars)
    }
    
    @Test
    fun `getTypography returns valid typography or null`() {
        val typography = plugin.getTypography()
        // Can be null, but if not null, should be valid
        typography?.let {
            assertNotNull(it.displayLarge)
            assertNotNull(it.bodyMedium)
        }
    }
}
```

### Translation Plugin Testing

```kotlin
class MyTranslationPluginTest {
    private lateinit var plugin: MyTranslationPlugin
    private lateinit var context: MockPluginContext
    
    @BeforeTest
    fun setup() {
        context = MockPluginContext()
        plugin = MyTranslationPlugin()
        plugin.initialize(context)
    }
    
    @Test
    fun `translate returns success for valid input`() = runTest {
        val result = plugin.translate("Hello", "en", "es")
        
        assertTrue(result.isSuccess)
        val translated = result.getOrNull()
        assertNotNull(translated)
        assertTrue(translated.isNotEmpty())
    }
    
    @Test
    fun `translate handles empty text`() = runTest {
        val result = plugin.translate("", "en", "es")
        
        assertTrue(result.isSuccess)
        assertEquals("", result.getOrNull())
    }
    
    @Test
    fun `translate returns failure for invalid language`() = runTest {
        val result = plugin.translate("Hello", "invalid", "es")
        
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `translateBatch translates multiple texts`() = runTest {
        val texts = listOf("Hello", "World", "Test")
        val result = plugin.translateBatch(texts, "en", "es")
        
        assertTrue(result.isSuccess)
        val translated = result.getOrNull()
        assertNotNull(translated)
        assertEquals(3, translated.size)
    }
    
    @Test
    fun `getSupportedLanguages returns valid language pairs`() {
        val languages = plugin.getSupportedLanguages()
        
        assertTrue(languages.isNotEmpty())
        languages.forEach { pair ->
            assertTrue(pair.from.length == 2) // ISO 639-1
            assertTrue(pair.to.length == 2)
        }
    }
    
    @Test
    fun `configureApiKey stores key in preferences`() {
        plugin.configureApiKey("test-api-key")
        
        val stored = context.preferences.getString("api_key")
        assertEquals("test-api-key", stored)
    }
}
```

### TTS Plugin Testing

```kotlin
class MyTTSPluginTest {
    private lateinit var plugin: MyTTSPlugin
    
    @BeforeTest
    fun setup() {
        plugin = MyTTSPlugin()
        plugin.initialize(MockPluginContext())
    }
    
    @Test
    fun `speak returns audio stream for valid input`() = runTest {
        val voice = VoiceConfig(voiceId = "en-US-1")
        val result = plugin.speak("Hello world", voice)
        
        assertTrue(result.isSuccess)
        val audio = result.getOrNull()
        assertNotNull(audio)
        assertTrue(audio.data.isNotEmpty())
    }
    
    @Test
    fun `getAvailableVoices returns voice list`() {
        val voices = plugin.getAvailableVoices()
        
        assertTrue(voices.isNotEmpty())
        voices.forEach { voice ->
            assertNotNull(voice.id)
            assertNotNull(voice.name)
            assertNotNull(voice.language)
        }
    }
    
    @Test
    fun `supportsStreaming returns boolean`() {
        val streaming = plugin.supportsStreaming()
        // Should return true or false, not null
        assertNotNull(streaming)
    }
    
    @Test
    fun `getAudioFormat returns valid format`() {
        val format = plugin.getAudioFormat()
        
        assertNotNull(format)
        assertNotNull(format.codec)
        assertTrue(format.sampleRate > 0)
    }
}
```

### Feature Plugin Testing

```kotlin
class MyFeaturePluginTest {
    private lateinit var plugin: MyFeaturePlugin
    
    @BeforeTest
    fun setup() {
        plugin = MyFeaturePlugin()
        plugin.initialize(MockPluginContext())
    }
    
    @Test
    fun `getMenuItems returns menu items`() {
        val items = plugin.getMenuItems()
        
        assertTrue(items.isNotEmpty())
        items.forEach { item ->
            assertNotNull(item.id)
            assertNotNull(item.label)
            assertNotNull(item.action)
        }
    }
    
    @Test
    fun `getScreens returns plugin screens`() {
        val screens = plugin.getScreens()
        
        screens.forEach { screen ->
            assertNotNull(screen.route)
            assertNotNull(screen.title)
            assertNotNull(screen.content)
        }
    }
    
    @Test
    fun `onReaderContext handles context changes`() {
        val context = ReaderContext(
            bookId = 1L,
            chapterId = 1L,
            selectedText = "Test text",
            currentPosition = 100
        )
        
        val action = plugin.onReaderContext(context)
        // Can be null or return an action
        action?.let {
            assertNotNull(it)
        }
    }
    
    @Test
    fun `getPreferencesScreen returns settings screen or null`() {
        val screen = plugin.getPreferencesScreen()
        // Can be null if no settings
        screen?.let {
            assertNotNull(it.route)
            assertNotNull(it.content)
        }
    }
}
```

## Testing Best Practices

### 1. Test Edge Cases

```kotlin
@Test
fun `translate handles very long text`() = runTest {
    val longText = "a".repeat(10000)
    val result = plugin.translate(longText, "en", "es")
    
    // Should either succeed or fail gracefully
    assertNotNull(result)
}

@Test
fun `translate handles special characters`() = runTest {
    val text = "Hello! @#$%^&*() 你好"
    val result = plugin.translate(text, "en", "es")
    
    assertTrue(result.isSuccess)
}

@Test
fun `translate handles null-like strings`() = runTest {
    val texts = listOf("null", "undefined", "NaN")
    texts.forEach { text ->
        val result = plugin.translate(text, "en", "es")
        assertTrue(result.isSuccess)
    }
}
```

### 2. Test Error Handling

```kotlin
@Test
fun `translate handles network errors gracefully`() = runTest {
    // Mock network failure
    context.networkClient.simulateError(NetworkException("Connection failed"))
    
    val result = plugin.translate("Hello", "en", "es")
    
    assertTrue(result.isFailure)
    val exception = result.exceptionOrNull()
    assertNotNull(exception)
}

@Test
fun `plugin cleanup doesn't throw exceptions`() {
    assertDoesNotThrow {
        plugin.cleanup()
    }
}
```

### 3. Test Performance

```kotlin
@Test
fun `translate completes within reasonable time`() = runTest {
    val startTime = System.currentTimeMillis()
    
    plugin.translate("Hello", "en", "es")
    
    val duration = System.currentTimeMillis() - startTime
    assertTrue(duration < 5000, "Translation took too long: ${duration}ms")
}

@Test
fun `plugin initialization is fast`() {
    val startTime = System.currentTimeMillis()
    
    val newPlugin = MyPlugin()
    newPlugin.initialize(MockPluginContext())
    
    val duration = System.currentTimeMillis() - startTime
    assertTrue(duration < 500, "Initialization took too long: ${duration}ms")
}
```

### 4. Test Resource Cleanup

```kotlin
@Test
fun `cleanup releases all resources`() {
    val plugin = MyPlugin()
    plugin.initialize(MockPluginContext())
    
    // Use plugin
    plugin.doSomething()
    
    // Cleanup
    plugin.cleanup()
    
    // Verify resources are released
    // (implementation-specific checks)
}
```

## Integration Testing

### Testing with Mock IReader

```kotlin
class PluginIntegrationTest {
    private lateinit var mockIReader: MockIReaderApp
    private lateinit var plugin: MyPlugin
    
    @BeforeTest
    fun setup() {
        mockIReader = MockIReaderApp()
        plugin = MyPlugin()
        mockIReader.installPlugin(plugin)
    }
    
    @Test
    fun `plugin integrates with theme system`() {
        val themePlugin = plugin as ThemePlugin
        mockIReader.applyTheme(themePlugin)
        
        val currentTheme = mockIReader.getCurrentTheme()
        assertEquals(themePlugin.getColorScheme(true), currentTheme)
    }
}
```

## Manual Testing

### Testing Checklist

#### Functionality
- [ ] All features work as described
- [ ] No crashes or freezes
- [ ] Error messages are clear
- [ ] Edge cases handled properly

#### Performance
- [ ] Plugin loads quickly
- [ ] Operations complete in reasonable time
- [ ] No memory leaks
- [ ] Acceptable battery usage

#### UI/UX
- [ ] UI is responsive
- [ ] Follows IReader design guidelines
- [ ] Works in dark and light themes
- [ ] Accessible to all users

#### Compatibility
- [ ] Works on Android
- [ ] Works on iOS
- [ ] Works on Desktop
- [ ] Works with different IReader versions

#### Security
- [ ] No security vulnerabilities
- [ ] Permissions used appropriately
- [ ] User data protected
- [ ] Network requests use HTTPS

### Platform-Specific Testing

#### Android
```bash
# Install on Android device
adb push MyPlugin.iplugin /sdcard/IReader/plugins/
adb shell am force-stop com.ireader.app
adb shell am start com.ireader.app/.MainActivity
```

#### iOS
```bash
# Install on iOS simulator
xcrun simctl install booted MyPlugin.iplugin
```

#### Desktop
```bash
# Copy to plugins directory
cp MyPlugin.iplugin ~/.ireader/plugins/
```

## Debugging

### Enable Debug Logging

```kotlin
class MyPlugin : Plugin {
    private val logger = PluginLogger(this)
    
    override fun initialize(context: PluginContext) {
        logger.debug("Initializing plugin")
        // ...
    }
    
    override suspend fun translate(text: String, from: String, to: String): Result<String> {
        logger.debug("Translating: $text from $from to $to")
        
        return try {
            val result = performTranslation(text, from, to)
            logger.debug("Translation successful: $result")
            Result.success(result)
        } catch (e: Exception) {
            logger.error("Translation failed", e)
            Result.failure(e)
        }
    }
}
```

### View Logs

```bash
# Android
adb logcat | grep "IReaderPlugin"

# iOS
xcrun simctl spawn booted log stream --predicate 'subsystem == "com.ireader.plugins"'

# Desktop
tail -f ~/.ireader/logs/plugins.log
```

## Continuous Integration

### GitHub Actions Example

```yaml
name: Test Plugin

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      
      - name: Run tests
        run: ./gradlew test
      
      - name: Validate plugin
        run: ./gradlew validatePlugin
      
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v2
        with:
          name: test-results
          path: build/test-results/
```

## Test Coverage

### Measure Coverage

```kotlin
// build.gradle.kts
plugins {
    id("org.jetbrains.kotlinx.kover") version "0.7.0"
}

kover {
    reports {
        total {
            html {
                onCheck = true
            }
        }
    }
}
```

```bash
./gradlew koverHtmlReport
```

### Coverage Goals

- **Minimum**: 70% code coverage
- **Recommended**: 80% code coverage
- **Excellent**: 90%+ code coverage

## Summary

Good testing ensures:
- Plugin works correctly
- No regressions in updates
- Better user experience
- Faster approval process
- Fewer support requests

Always test thoroughly before submitting!
