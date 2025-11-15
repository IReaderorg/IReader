# Plugin Types

IReader supports four types of plugins, each serving different purposes.

## Overview

| Type | Purpose | Complexity | Examples |
|------|---------|------------|----------|
| **Theme** | Visual customization | Low | Color schemes, backgrounds |
| **Translation** | Text translation | Medium | Google Translate, DeepL |
| **TTS** | Text-to-speech | Medium | Custom voices, speech engines |
| **Feature** | Custom functionality | High | Statistics, notes, social features |

## Theme Plugins

Theme plugins customize the visual appearance of IReader.

### What You Can Customize

- **Colors**: Material 3 color scheme (primary, secondary, background, etc.)
- **Extra Colors**: IReader-specific colors (reader background, text color, etc.)
- **Typography**: Font families, sizes, and weights (optional)
- **Backgrounds**: Custom background images (optional)

### Use Cases

- Dark themes optimized for night reading
- High-contrast themes for accessibility
- Themed color schemes (e.g., "Forest", "Ocean", "Sunset")
- Brand-specific themes

### Example

```kotlin
class MyThemePlugin : ThemePlugin {
    override fun getColorScheme(isDark: Boolean): ColorScheme {
        return darkColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC6),
            // ... more colors
        )
    }
}
```

### Learn More

- [Theme Plugin Example](../examples/theme-plugin-example/)
- [Theme Plugin API Reference](api-reference.md#themeplugin)

## Translation Plugins

Translation plugins provide text translation services.

### What You Can Do

- Translate text between languages
- Batch translate multiple texts
- Configure API keys
- Cache translations
- Support multiple translation engines

### Use Cases

- Integration with translation APIs (Google, DeepL, etc.)
- Custom translation engines
- Offline translation
- Specialized translation (e.g., literary, technical)

### Example

```kotlin
class MyTranslationPlugin : TranslationPlugin {
    override suspend fun translate(
        text: String,
        from: String,
        to: String
    ): Result<String> {
        // Call translation API
        return Result.success(translatedText)
    }
}
```

### Best Practices

- Cache translations to reduce API calls
- Implement rate limiting for free tiers
- Handle network errors gracefully
- Support batch translation for efficiency
- Validate language codes

### Learn More

- [Translation Plugin Example](../examples/translation-plugin-example/)
- [Translation Plugin API Reference](api-reference.md#translationplugin)

## TTS Plugins

TTS (Text-to-Speech) plugins provide custom voice synthesis.

### What You Can Do

- Synthesize speech from text
- Provide multiple voices
- Configure voice parameters (speed, pitch, volume)
- Stream audio for long texts
- Support multiple languages

### Use Cases

- High-quality voice synthesis
- Celebrity or character voices
- Specialized voices (e.g., audiobook narrators)
- Offline TTS engines
- Custom audio effects

### Example

```kotlin
class MyTTSPlugin : TTSPlugin {
    override suspend fun speak(
        text: String,
        voice: VoiceConfig
    ): Result<AudioStream> {
        // Synthesize speech
        return Result.success(audioStream)
    }
    
    override fun getAvailableVoices(): List<VoiceModel> {
        return listOf(
            VoiceModel("en-US-1", "English (US)", "en-US", "female"),
            VoiceModel("en-US-2", "English (US)", "en-US", "male")
        )
    }
}
```

### Best Practices

- Support streaming for long texts
- Provide voice previews
- Cache synthesized audio
- Handle interruptions gracefully
- Optimize audio format for size/quality

### Learn More

- [TTS Plugin Example](../examples/tts-plugin-example/)
- [TTS Plugin API Reference](api-reference.md#ttsplugin)

## Feature Plugins

Feature plugins add custom functionality to IReader.

### What You Can Do

- Add menu items to reader interface
- Register custom screens
- React to reader events (text selection, chapter changes)
- Store plugin data
- Integrate with external services

### Use Cases

- **Reading Statistics**: Track reading time, speed, progress
- **Note-Taking**: Highlight text, add notes, bookmarks
- **Social Features**: Share quotes, discuss with friends
- **Export**: Export books, notes, highlights
- **Integrations**: Goodreads, Notion, Obsidian
- **Accessibility**: Screen readers, dyslexia-friendly fonts
- **Gamification**: Achievements, reading challenges

### Example

```kotlin
class MyFeaturePlugin : FeaturePlugin {
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
    
    override fun onReaderContext(context: ReaderContext): PluginAction? {
        // React to text selection
        if (context.selectedText != null) {
            return PluginAction.ShowDialog("Save as note?")
        }
        return null
    }
}
```

### Best Practices

- Keep UI consistent with IReader design
- Don't block the main thread
- Handle errors gracefully
- Respect user privacy
- Clean up resources properly

### Learn More

- [Feature Plugin Example](../examples/feature-plugin-example/)
- [Feature Plugin API Reference](api-reference.md#featureplugin)

## Choosing a Plugin Type

### Decision Tree

```
Do you want to customize appearance?
├─ Yes → Theme Plugin
└─ No
   └─ Do you want to translate text?
      ├─ Yes → Translation Plugin
      └─ No
         └─ Do you want to add voice synthesis?
            ├─ Yes → TTS Plugin
            └─ No → Feature Plugin
```

### Complexity Comparison

**Theme Plugin** (Easiest)
- No external dependencies
- Purely visual
- Quick to develop
- Easy to test

**Translation Plugin** (Medium)
- Network requests
- API integration
- Caching logic
- Error handling

**TTS Plugin** (Medium)
- Audio processing
- Voice management
- Streaming support
- Platform-specific code

**Feature Plugin** (Most Complex)
- UI development
- State management
- Event handling
- Data persistence

## Combining Plugin Types

You cannot combine multiple plugin types in a single plugin. Each plugin must be one specific type.

However, you can:
- Create multiple plugins that work together
- Use feature plugins to enhance other plugin types
- Coordinate between plugins using shared preferences

## Platform Support

### Cross-Platform Plugins

All plugin types can be cross-platform:

```kotlin
// Common code
expect class PlatformSpecific()

// Android implementation
actual class PlatformSpecific() { /* Android code */ }

// iOS implementation
actual class PlatformSpecific() { /* iOS code */ }

// Desktop implementation
actual class PlatformSpecific() { /* Desktop code */ }
```

### Platform-Specific Features

Some features may only work on certain platforms:

```kotlin
override fun initialize(context: PluginContext) {
    when (context.platform) {
        Platform.ANDROID -> initializeAndroid()
        Platform.IOS -> initializeIOS()
        Platform.DESKTOP -> initializeDesktop()
    }
}
```

## Next Steps

1. Choose your plugin type
2. Study the [example](../examples/) for that type
3. Use the [template generator](getting-started.md#generate-a-plugin-template)
4. Read the [API reference](api-reference.md)
5. Follow [best practices](best-practices.md)
6. Test thoroughly using the [testing guide](testing.md)
7. Submit following [submission guidelines](submission-guidelines.md)

## Resources

- [Getting Started Guide](getting-started.md)
- [API Reference](api-reference.md)
- [Example Plugins](../examples/)
- [Best Practices](best-practices.md)
- [Testing Guide](testing.md)
