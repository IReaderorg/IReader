# IReader Plugin API

A Kotlin Multiplatform library for creating IReader plugins. This API allows developers to extend IReader with custom themes, TTS engines, translation services, and features.

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.ireaderorg:plugin-api:1.0.0")
}
```

## Plugin Types

### Theme Plugin

Create custom visual themes for IReader:

```kotlin
class OceanThemePlugin : ThemePlugin {
    override val manifest = PluginManifest(
        id = "com.example.ocean-theme",
        name = "Ocean Theme",
        version = "1.0.0",
        versionCode = 1,
        description = "A calming ocean-inspired theme",
        author = PluginAuthor("Developer Name"),
        type = PluginType.THEME,
        permissions = emptyList(),
        minIReaderVersion = "1.0.0",
        platforms = listOf(Platform.ANDROID, Platform.IOS, Platform.DESKTOP)
    )
    
    override fun initialize(context: PluginContext) {}
    override fun cleanup() {}
    
    override fun getColorScheme(isDark: Boolean): ThemeColorScheme {
        return if (isDark) darkColors else lightColors
    }
    
    override fun getExtraColors(isDark: Boolean): ThemeExtraColors {
        return ThemeExtraColors(
            bars = 0xFF1A237E,
            onBars = 0xFFFFFFFF,
            isBarLight = false
        )
    }
}
```

### TTS Plugin

Add custom text-to-speech engines:

```kotlin
class CloudTTSPlugin : TTSPlugin {
    override val manifest = PluginManifest(
        id = "com.example.cloud-tts",
        name = "Cloud TTS",
        version = "1.0.0",
        versionCode = 1,
        description = "High-quality cloud-based TTS",
        author = PluginAuthor("Developer Name"),
        type = PluginType.TTS,
        permissions = listOf(PluginPermission.NETWORK),
        minIReaderVersion = "1.0.0",
        platforms = listOf(Platform.ANDROID, Platform.IOS, Platform.DESKTOP),
        monetization = PluginMonetization.Premium(
            price = 4.99,
            currency = "USD",
            trialDays = 7
        )
    )
    
    override suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream> {
        // Implement TTS logic
    }
    
    override fun getAvailableVoices(): List<VoiceModel> {
        return listOf(
            VoiceModel("en-us-1", "English (US) - Sarah", "en-US", VoiceGender.FEMALE),
            VoiceModel("en-us-2", "English (US) - John", "en-US", VoiceGender.MALE)
        )
    }
    
    override fun supportsStreaming() = true
    
    override fun getAudioFormat() = AudioFormat(
        encoding = AudioEncoding.MP3,
        sampleRate = 44100,
        channels = 1,
        bitDepth = 16
    )
}
```

### Translation Plugin

Add translation services:

```kotlin
class DeepLPlugin : TranslationPlugin {
    override val manifest = PluginManifest(
        id = "com.example.deepl",
        name = "DeepL Translation",
        version = "1.0.0",
        versionCode = 1,
        description = "High-quality neural translation",
        author = PluginAuthor("Developer Name"),
        type = PluginType.TRANSLATION,
        permissions = listOf(PluginPermission.NETWORK),
        minIReaderVersion = "1.0.0",
        platforms = listOf(Platform.ANDROID, Platform.IOS, Platform.DESKTOP)
    )
    
    override suspend fun translate(text: String, from: String, to: String): Result<String> {
        // Implement translation logic
    }
    
    override suspend fun translateBatch(texts: List<String>, from: String, to: String): Result<List<String>> {
        // Implement batch translation
    }
    
    override fun getSupportedLanguages(): List<LanguagePair> {
        return listOf(
            LanguagePair("en", "ja"),
            LanguagePair("en", "de"),
            // ... more pairs
        )
    }
    
    override fun requiresApiKey() = true
    override fun configureApiKey(key: String) { /* Store API key */ }
}
```

### Feature Plugin

Add custom features:

```kotlin
class DictionaryPlugin : FeaturePlugin {
    override val manifest = PluginManifest(
        id = "com.example.dictionary",
        name = "Dictionary Lookup",
        version = "1.0.0",
        versionCode = 1,
        description = "Look up word definitions while reading",
        author = PluginAuthor("Developer Name"),
        type = PluginType.FEATURE,
        permissions = listOf(PluginPermission.READER_CONTEXT, PluginPermission.NETWORK),
        minIReaderVersion = "1.0.0",
        platforms = listOf(Platform.ANDROID, Platform.IOS, Platform.DESKTOP)
    )
    
    override fun getMenuItems(): List<PluginMenuItem> {
        return listOf(
            PluginMenuItem("lookup", "Look up word", "dictionary", 0)
        )
    }
    
    override fun getScreens(): List<PluginScreen> {
        return listOf(
            PluginScreen("dictionary/{word}", "Dictionary", DictionaryScreen)
        )
    }
    
    override fun onReaderContext(context: ReaderContext): PluginAction? {
        return context.selectedText?.let { word ->
            PluginAction.Navigate("dictionary/$word")
        }
    }
}
```

## Permissions

Plugins must declare required permissions in their manifest:

- `NETWORK` - Make HTTP requests
- `STORAGE` - Access local storage
- `READER_CONTEXT` - Access current reading state
- `LIBRARY_ACCESS` - Access user's library
- `PREFERENCES` - Read/write preferences
- `NOTIFICATIONS` - Show notifications

## Monetization

Plugins support three monetization models:

1. **Free** - No cost
2. **Premium** - One-time purchase with optional trial
3. **Freemium** - Free base with purchasable features

## License

Mozilla Public License 2.0
