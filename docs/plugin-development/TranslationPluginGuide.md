# Translation Plugin Development Guide

This guide explains how to create and integrate translation plugins with IReader.

## Overview

Translation plugins extend IReader's translation capabilities by providing additional translation services. Plugins can integrate with any translation API or service.

## Requirements

Translation plugins must implement the `TranslationPlugin` interface which extends the base `Plugin` interface.

### Key Requirements

1. **Language Pair Support** (Requirement 4.1, 4.5): Plugins must declare which language pairs they support
2. **Translation Methods** (Requirement 4.2, 4.3): Implement both single and batch translation
3. **API Key Management** (Requirement 4.2): Handle API key configuration if required
4. **Error Handling** (Requirement 4.4): Gracefully handle errors with fallback support
5. **Caching** (Requirement 4.4): Translation results are automatically cached by the system

## Creating a Translation Plugin

### 1. Implement the TranslationPlugin Interface

```kotlin
package com.example.myplugin

import ireader.domain.plugins.TranslationPlugin
import ireader.domain.plugins.PluginManifest
import ireader.domain.plugins.PluginContext
import ireader.domain.plugins.LanguagePair

class MyTranslationPlugin : TranslationPlugin {
    
    override val manifest = PluginManifest(
        id = "com.example.mytranslator",
        name = "My Translator",
        version = "1.0.0",
        versionCode = 1,
        description = "Custom translation service",
        author = PluginAuthor(
            name = "Your Name",
            email = "your.email@example.com",
            website = "https://example.com"
        ),
        type = PluginType.TRANSLATION,
        permissions = listOf(PluginPermission.NETWORK),
        minIReaderVersion = "1.0.0",
        platforms = listOf(Platform.ANDROID, Platform.DESKTOP),
        monetization = PluginMonetization.Free,
        iconUrl = null,
        screenshotUrls = emptyList()
    )
    
    private var apiKey: String = ""
    private lateinit var context: PluginContext
    
    override fun initialize(context: PluginContext) {
        this.context = context
        // Load saved API key from plugin preferences
        apiKey = context.preferences.getString("api_key", "")
    }
    
    override fun cleanup() {
        // Clean up resources
    }
    
    override suspend fun translate(
        text: String,
        from: String,
        to: String
    ): Result<String> {
        return try {
            // Implement your translation logic here
            val translated = callTranslationApi(text, from, to)
            Result.success(translated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun translateBatch(
        texts: List<String>,
        from: String,
        to: String
    ): Result<List<String>> {
        return try {
            // Implement batch translation
            // Can call translate() for each text or use a batch API
            val translations = texts.map { text ->
                translate(text, from, to).getOrThrow()
            }
            Result.success(translations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getSupportedLanguages(): List<LanguagePair> {
        return listOf(
            LanguagePair("en", "es"),
            LanguagePair("en", "fr"),
            LanguagePair("es", "en"),
            LanguagePair("fr", "en"),
            // Add all supported language pairs
            // Use "*" for wildcard support: LanguagePair("*", "*")
        )
    }
    
    override fun requiresApiKey(): Boolean {
        return true // Set to false if no API key needed
    }
    
    override fun configureApiKey(key: String) {
        this.apiKey = key
        // Save to plugin preferences
        context.preferences.putString("api_key", key)
    }
    
    private suspend fun callTranslationApi(
        text: String,
        from: String,
        to: String
    ): String {
        // Implement your API call here
        // Use context.httpClient for network requests
        // Example:
        // val response = context.httpClient.post("https://api.example.com/translate") {
        //     setBody(TranslationRequest(text, from, to, apiKey))
        // }
        // return response.body<TranslationResponse>().translatedText
        
        return "Translated: $text"
    }
}
```

### 2. Create Plugin Manifest (plugin.json)

```json
{
  "id": "com.example.mytranslator",
  "name": "My Translator",
  "version": "1.0.0",
  "versionCode": 1,
  "description": "Custom translation service",
  "author": {
    "name": "Your Name",
    "email": "your.email@example.com",
    "website": "https://example.com"
  },
  "type": "TRANSLATION",
  "permissions": ["NETWORK"],
  "minIReaderVersion": "1.0.0",
  "platforms": ["ANDROID", "DESKTOP"],
  "monetization": {
    "type": "FREE"
  }
}
```

### 3. Package the Plugin

Package your plugin as a `.iplugin` file (ZIP format):

```
mytranslator.iplugin/
├── plugin.json
├── classes/
│   └── com/example/myplugin/MyTranslationPlugin.class
└── resources/ (optional)
```

## Using Translation Plugins

### In Code

```kotlin
// Get translation engine manager
val translationManager: TranslationEnginesManager = get()

// Get all available engines (built-in + plugins)
val engines = translationManager.getAvailableEngines()

// Get selected engine
val selectedEngine = translationManager.getSelectedEngine()

// Translate using selected engine
val result = translationManager.translate(
    text = "Hello, world!",
    from = "en",
    to = "es",
    engine = selectedEngine
)

result.fold(
    onSuccess = { translated ->
        println("Translation: $translated")
    },
    onFailure = { error ->
        println("Error: ${error.message}")
        // System automatically falls back to built-in engine
    }
)

// Batch translation
val batchResult = translationManager.translateBatch(
    texts = listOf("Hello", "World"),
    from = "en",
    to = "es",
    engine = selectedEngine
)

// Validate language pair support
val isSupported = translationManager.validateLanguagePair(
    from = "en",
    to = "es",
    engine = selectedEngine
)
```

### In UI

Translation plugins automatically appear in the Translation Settings screen:

1. Navigate to Settings → Translation
2. Select "Plugin Translation Engines" section
3. Choose your plugin from the list
4. Configure API key if required
5. Test the connection

## Features

### Automatic Caching

The system automatically caches translation results to improve performance. Cache keys include:
- Source text
- Source language
- Target language
- Engine identifier

### Error Handling with Fallback

If a plugin translation fails, the system automatically falls back to the default built-in engine. This ensures users always get translations even if a plugin has issues.

### Language Pair Validation

Before attempting translation, the system validates that the selected engine supports the requested language pair. This prevents unnecessary API calls and provides better error messages.

### API Key Management

Plugins that require API keys can:
1. Declare `requiresApiKey() = true`
2. Implement `configureApiKey(key: String)`
3. Store keys securely in plugin preferences
4. Access keys during translation

The UI automatically shows API key input fields for plugins that require them.

## Best Practices

1. **Handle Errors Gracefully**: Always return `Result.failure()` with descriptive error messages
2. **Support Batch Translation**: Implement efficient batch translation when possible
3. **Validate Input**: Check for empty strings, unsupported languages, etc.
4. **Use Caching**: Leverage the built-in caching system
5. **Respect Rate Limits**: Implement rate limiting for API calls
6. **Provide Clear Language Support**: Accurately declare supported language pairs
7. **Test Thoroughly**: Test with various text lengths, special characters, and edge cases

## Testing

```kotlin
// Test your plugin
class MyTranslationPluginTest {
    
    @Test
    fun testTranslation() = runBlocking {
        val plugin = MyTranslationPlugin()
        val context = createTestPluginContext()
        plugin.initialize(context)
        
        val result = plugin.translate("Hello", "en", "es")
        assertTrue(result.isSuccess)
        assertEquals("Hola", result.getOrNull())
    }
    
    @Test
    fun testBatchTranslation() = runBlocking {
        val plugin = MyTranslationPlugin()
        val context = createTestPluginContext()
        plugin.initialize(context)
        
        val result = plugin.translateBatch(
            listOf("Hello", "World"),
            "en",
            "es"
        )
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
    }
    
    @Test
    fun testLanguagePairValidation() {
        val plugin = MyTranslationPlugin()
        val pairs = plugin.getSupportedLanguages()
        
        assertTrue(pairs.any { it.from == "en" && it.to == "es" })
    }
}
```

## Requirements Mapping

- **Requirement 4.1**: Plugin discovery and installation through marketplace
- **Requirement 4.2**: Plugin selection in reader settings
- **Requirement 4.3**: Translation using selected plugin with batch support
- **Requirement 4.4**: Caching and error handling with fallback
- **Requirement 4.5**: Language pair validation

## Support

For questions or issues:
1. Check the plugin development documentation
2. Review example plugins in the repository
3. Contact the IReader development team
