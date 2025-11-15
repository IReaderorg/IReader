# IReader Plugin Examples

This directory contains example implementations for each type of IReader plugin.

## Available Examples

### 1. Theme Plugin Example
**Location:** `theme-plugin-example/`

A complete theme plugin demonstrating:
- Custom color schemes for dark and light modes
- Extra colors for reader-specific UI
- Custom typography (optional)
- Background assets (optional)

**Key Features:**
- "Midnight Blue" theme with carefully selected colors
- Reduced eye strain for night reading
- Full Material 3 color scheme implementation

**Files:**
- `MyThemePlugin.kt` - Main plugin implementation
- `plugin.json` - Plugin manifest
- `README.md` - Setup and usage instructions

### 2. Translation Plugin Example
**Location:** `translation-plugin-example/`

A translation plugin demonstrating:
- Integration with translation APIs
- Caching for performance
- Rate limiting for free users
- Freemium monetization model
- Batch translation support

**Key Features:**
- Support for 50+ languages
- Smart caching system
- API key configuration
- Premium features (unlimited translations)

**Files:**
- `MyTranslationPlugin.kt` - Main plugin implementation
- `plugin.json` - Plugin manifest
- `README.md` - Setup and usage instructions

### 3. TTS Plugin Example
**Location:** `tts-plugin-example/`

A text-to-speech plugin demonstrating:
- Custom voice synthesis
- Voice configuration (speed, pitch, volume)
- Audio streaming support
- Multiple voice options

**Key Features:**
- High-quality voice synthesis
- Configurable voice parameters
- Efficient audio streaming
- Multiple language support

**Files:**
- `MyTTSPlugin.kt` - Main plugin implementation
- `plugin.json` - Plugin manifest
- `README.md` - Setup and usage instructions

### 4. Feature Plugin Example
**Location:** `feature-plugin-example/`

A feature plugin demonstrating:
- Custom menu items in reader
- Custom screens and navigation
- Reader context integration
- Plugin preferences screen

**Key Features:**
- Reading statistics tracking
- Note-taking functionality
- Custom UI screens
- Integration with reader events

**Files:**
- `MyFeaturePlugin.kt` - Main plugin implementation
- `plugin.json` - Plugin manifest
- `README.md` - Setup and usage instructions

## Using These Examples

### 1. Study the Code

Each example is fully documented with comments explaining:
- How to implement the plugin interface
- Best practices for that plugin type
- Common patterns and techniques
- Error handling strategies

### 2. Copy and Modify

You can use these examples as templates:

```bash
# Copy an example
cp -r theme-plugin-example/ my-awesome-theme/

# Modify the files
cd my-awesome-theme/
# Edit MyThemePlugin.kt and plugin.json
```

### 3. Build and Test

```bash
# Build the plugin
./gradlew packagePlugin --plugin=my-awesome-theme

# Validate the plugin
./gradlew validatePlugin --plugin=my-awesome-theme

# Test in IReader
cp build/plugins/my-awesome-theme.iplugin ~/.ireader/plugins/
```

## Example Structure

Each example follows this structure:

```
example-plugin/
├── README.md              # Setup and usage instructions
├── plugin.json            # Plugin manifest
├── MyPlugin.kt            # Main plugin implementation
├── build.gradle.kts       # Build configuration (if applicable)
└── resources/             # Plugin resources
    ├── icon.png           # Plugin icon
    └── screenshots/       # Screenshots for marketplace
```

## Learning Path

We recommend studying the examples in this order:

1. **Theme Plugin** - Simplest to understand, no external dependencies
2. **Translation Plugin** - Introduces network requests and caching
3. **TTS Plugin** - Shows audio handling and streaming
4. **Feature Plugin** - Most complex, demonstrates UI integration

## Common Patterns

### Initialization

All plugins follow this pattern:

```kotlin
class MyPlugin : PluginType {
    private lateinit var context: PluginContext
    
    override fun initialize(context: PluginContext) {
        this.context = context
        // Initialize resources
    }
    
    override fun cleanup() {
        // Clean up resources
    }
}
```

### Error Handling

Use `Result` types for operations that can fail:

```kotlin
override suspend fun doSomething(): Result<String> {
    return try {
        val result = performOperation()
        Result.success(result)
    } catch (e: Exception) {
        context.logger.error("Operation failed", e)
        Result.failure(e)
    }
}
```

### Caching

Implement caching for performance:

```kotlin
private val cache = mutableMapOf<String, String>()

override suspend fun getData(key: String): Result<String> {
    // Check cache first
    cache[key]?.let { return Result.success(it) }
    
    // Fetch and cache
    return fetchData(key).also { result ->
        result.getOrNull()?.let { cache[key] = it }
    }
}
```

### Preferences

Store plugin settings:

```kotlin
override fun initialize(context: PluginContext) {
    this.context = context
    
    // Load saved settings
    val apiKey = context.preferences.getString("api_key", "")
    val enabled = context.preferences.getBoolean("enabled", true)
}

fun saveSettings(apiKey: String) {
    context.preferences.putString("api_key", apiKey)
}
```

## Testing Examples

Each example includes test cases. See the testing guide for details:

```kotlin
class MyPluginTest {
    private lateinit var plugin: MyPlugin
    private lateinit var context: MockPluginContext
    
    @BeforeTest
    fun setup() {
        context = MockPluginContext()
        plugin = MyPlugin()
        plugin.initialize(context)
    }
    
    @Test
    fun testFeature() {
        val result = plugin.doSomething()
        assertTrue(result.isSuccess)
    }
}
```

## Additional Resources

- [Plugin Development Guide](../plugin-development/README.md)
- [API Reference](../plugin-development/api-reference.md)
- [Best Practices](../plugin-development/best-practices.md)
- [Testing Guide](../plugin-development/testing.md)
- [Submission Guidelines](../plugin-development/submission-guidelines.md)

## Contributing Examples

Have a great plugin example? Consider contributing:

1. Fork the repository
2. Add your example to this directory
3. Include comprehensive documentation
4. Submit a pull request

## Support

- [Developer Forum](https://forum.ireader.app/developers)
- [Discord](https://discord.gg/ireader)
- [Email](mailto:plugins@ireader.app)

## License

All examples are provided under the MIT License and can be freely used as templates for your own plugins.
