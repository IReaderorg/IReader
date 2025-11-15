package ireader.plugin.tools

import java.io.File

/**
 * Generates plugin project templates for different plugin types.
 * 
 * Usage:
 *   ./gradlew generatePluginTemplate --type=theme --name=MyTheme
 */
class PluginTemplateGenerator {
    
    fun generate(type: PluginType, name: String, outputDir: File): File {
        val projectDir = File(outputDir, name)
        
        if (projectDir.exists()) {
            throw IllegalStateException("Directory already exists: ${projectDir.path}")
        }
        
        projectDir.mkdirs()
        
        println("Generating $type plugin template: $name")
        println("Output directory: ${projectDir.absolutePath}")
        
        // Create directory structure
        createDirectoryStructure(projectDir)
        
        // Generate files based on plugin type
        when (type) {
            PluginType.THEME -> generateThemePlugin(projectDir, name)
            PluginType.TRANSLATION -> generateTranslationPlugin(projectDir, name)
            PluginType.TTS -> generateTTSPlugin(projectDir, name)
            PluginType.FEATURE -> generateFeaturePlugin(projectDir, name)
        }
        
        // Generate common files
        generateBuildFile(projectDir, name, type)
        generateReadme(projectDir, name, type)
        generateGitignore(projectDir)
        
        println("✓ Template generated successfully!")
        println("\nNext steps:")
        println("1. cd $name")
        println("2. Edit src/commonMain/kotlin/${name}Plugin.kt")
        println("3. Edit resources/plugin.json")
        println("4. ./gradlew packagePlugin")
        
        return projectDir
    }
    
    private fun createDirectoryStructure(projectDir: File) {
        File(projectDir, "src/commonMain/kotlin").mkdirs()
        File(projectDir, "src/commonTest/kotlin").mkdirs()
        File(projectDir, "resources").mkdirs()
        File(projectDir, "resources/screenshots").mkdirs()
    }
    
    private fun generateThemePlugin(projectDir: File, name: String) {
        val pluginFile = File(projectDir, "src/commonMain/kotlin/${name}Plugin.kt")
        pluginFile.writeText("""
package com.example.${name.lowercase()}

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import ireader.domain.plugins.Plugin
import ireader.domain.plugins.PluginContext
import ireader.domain.plugins.PluginManifest
import ireader.domain.plugins.ThemePlugin
import ireader.domain.plugins.ThemeBackgrounds
import ireader.presentation.ui.theme.ExtraColors

class ${name}Plugin : ThemePlugin {
    
    override val manifest: PluginManifest by lazy {
        // Load from plugin.json
        loadManifestFromResources()
    }
    
    override fun initialize(context: PluginContext) {
        // Initialize your theme
    }
    
    override fun cleanup() {
        // Clean up resources
    }
    
    override fun getColorScheme(isDark: Boolean): ColorScheme {
        return if (isDark) {
            darkColorScheme(
                primary = Color(0xFF6200EE),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFF03DAC6),
                // TODO: Add more colors
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF6200EE),
                onPrimary = Color(0xFFFFFFFF),
                secondary = Color(0xFF03DAC6),
                // TODO: Add more colors
            )
        }
    }
    
    override fun getExtraColors(isDark: Boolean): ExtraColors {
        // TODO: Implement extra colors
        return ExtraColors.default(isDark)
    }
    
    override fun getTypography(): Typography? {
        // Return null to use default, or customize
        return null
    }
    
    override fun getBackgroundAssets(): ThemeBackgrounds? {
        // Return null for no custom backgrounds
        return null
    }
}
        """.trimIndent())
        
        generateManifest(projectDir, name, "THEME")
    }
    
    private fun generateTranslationPlugin(projectDir: File, name: String) {
        val pluginFile = File(projectDir, "src/commonMain/kotlin/${name}Plugin.kt")
        pluginFile.writeText("""
package com.example.${name.lowercase()}

import ireader.domain.plugins.Plugin
import ireader.domain.plugins.PluginContext
import ireader.domain.plugins.PluginManifest
import ireader.domain.plugins.TranslationPlugin
import ireader.domain.plugins.LanguagePair

class ${name}Plugin : TranslationPlugin {
    
    private lateinit var context: PluginContext
    
    override val manifest: PluginManifest by lazy {
        loadManifestFromResources()
    }
    
    override fun initialize(context: PluginContext) {
        this.context = context
    }
    
    override fun cleanup() {
        // Clean up resources
    }
    
    override suspend fun translate(text: String, from: String, to: String): Result<String> {
        return try {
            // TODO: Implement translation logic
            val translated = performTranslation(text, from, to)
            Result.success(translated)
        } catch (e: Exception) {
            context.logger.error("Translation failed", e)
            Result.failure(e)
        }
    }
    
    override suspend fun translateBatch(
        texts: List<String>,
        from: String,
        to: String
    ): Result<List<String>> {
        // TODO: Implement batch translation
        return Result.success(texts.map { translate(it, from, to).getOrThrow() })
    }
    
    override fun getSupportedLanguages(): List<LanguagePair> {
        // TODO: Return supported language pairs
        return listOf(
            LanguagePair("en", "es"),
            LanguagePair("en", "fr"),
            // Add more...
        )
    }
    
    override fun requiresApiKey(): Boolean = true
    
    override fun configureApiKey(key: String) {
        context.preferences.putString("api_key", key)
    }
    
    private suspend fun performTranslation(text: String, from: String, to: String): String {
        // TODO: Implement actual translation
        return text
    }
}
        """.trimIndent())
        
        generateManifest(projectDir, name, "TRANSLATION", listOf("NETWORK", "PREFERENCES"))
    }
    
    private fun generateTTSPlugin(projectDir: File, name: String) {
        val pluginFile = File(projectDir, "src/commonMain/kotlin/${name}Plugin.kt")
        pluginFile.writeText("""
package com.example.${name.lowercase()}

import ireader.domain.plugins.Plugin
import ireader.domain.plugins.PluginContext
import ireader.domain.plugins.PluginManifest
import ireader.domain.plugins.TTSPlugin
import ireader.domain.plugins.VoiceConfig
import ireader.domain.plugins.VoiceModel
import ireader.domain.plugins.AudioStream
import ireader.domain.plugins.AudioFormat

class ${name}Plugin : TTSPlugin {
    
    private lateinit var context: PluginContext
    
    override val manifest: PluginManifest by lazy {
        loadManifestFromResources()
    }
    
    override fun initialize(context: PluginContext) {
        this.context = context
    }
    
    override fun cleanup() {
        // Clean up resources
    }
    
    override suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream> {
        return try {
            // TODO: Implement speech synthesis
            val audio = synthesizeSpeech(text, voice)
            Result.success(audio)
        } catch (e: Exception) {
            context.logger.error("Speech synthesis failed", e)
            Result.failure(e)
        }
    }
    
    override fun getAvailableVoices(): List<VoiceModel> {
        // TODO: Return available voices
        return listOf(
            VoiceModel(
                id = "en-US-1",
                name = "English (US) - Voice 1",
                language = "en-US",
                gender = "female"
            )
        )
    }
    
    override fun supportsStreaming(): Boolean = false
    
    override fun getAudioFormat(): AudioFormat {
        return AudioFormat(
            codec = "mp3",
            sampleRate = 22050,
            channels = 1
        )
    }
    
    private suspend fun synthesizeSpeech(text: String, voice: VoiceConfig): AudioStream {
        // TODO: Implement actual synthesis
        return AudioStream(ByteArray(0), getAudioFormat())
    }
}
        """.trimIndent())
        
        generateManifest(projectDir, name, "TTS", listOf("NETWORK"))
    }
    
    private fun generateFeaturePlugin(projectDir: File, name: String) {
        val pluginFile = File(projectDir, "src/commonMain/kotlin/${name}Plugin.kt")
        pluginFile.writeText("""
package com.example.${name.lowercase()}

import ireader.domain.plugins.Plugin
import ireader.domain.plugins.PluginContext
import ireader.domain.plugins.PluginManifest
import ireader.domain.plugins.FeaturePlugin
import ireader.domain.plugins.PluginMenuItem
import ireader.domain.plugins.PluginScreen
import ireader.domain.plugins.ReaderContext
import ireader.domain.plugins.PluginAction

class ${name}Plugin : FeaturePlugin {
    
    private lateinit var context: PluginContext
    
    override val manifest: PluginManifest by lazy {
        loadManifestFromResources()
    }
    
    override fun initialize(context: PluginContext) {
        this.context = context
    }
    
    override fun cleanup() {
        // Clean up resources
    }
    
    override fun getMenuItems(): List<PluginMenuItem> {
        return listOf(
            PluginMenuItem(
                id = "my_action",
                label = "My Action",
                icon = "ic_action",
                action = { performAction() }
            )
        )
    }
    
    override fun getScreens(): List<PluginScreen> {
        return listOf(
            PluginScreen(
                route = "my_screen",
                title = "My Screen",
                content = { MyScreenContent() }
            )
        )
    }
    
    override fun onReaderContext(context: ReaderContext): PluginAction? {
        // TODO: Handle reader events
        return null
    }
    
    override fun getPreferencesScreen(): PluginScreen? {
        // TODO: Return settings screen or null
        return null
    }
    
    private fun performAction() {
        // TODO: Implement action
    }
}
        """.trimIndent())
        
        generateManifest(projectDir, name, "FEATURE", listOf("READER_CONTEXT", "STORAGE"))
    }
    
    private fun generateManifest(projectDir: File, name: String, type: String, permissions: List<String> = emptyList()) {
        val manifestFile = File(projectDir, "resources/plugin.json")
        val permissionsJson = if (permissions.isEmpty()) {
            "[]"
        } else {
            permissions.joinToString(", ", "[\"", "\"]") { it }
        }
        
        manifestFile.writeText("""
{
  "id": "com.example.${name.lowercase()}",
  "name": "$name",
  "version": "1.0.0",
  "versionCode": 1,
  "description": "A $type plugin for IReader",
  "author": {
    "name": "Your Name",
    "email": "you@example.com",
    "website": "https://example.com"
  },
  "type": "$type",
  "permissions": $permissionsJson,
  "minIReaderVersion": "1.0.0",
  "platforms": ["ANDROID", "IOS", "DESKTOP"],
  "iconUrl": "icon.png",
  "screenshotUrls": [],
  "monetization": {
    "type": "FREE"
  }
}
        """.trimIndent())
    }
    
    private fun generateBuildFile(projectDir: File, name: String, type: PluginType) {
        val buildFile = File(projectDir, "build.gradle.kts")
        buildFile.writeText("""
plugins {
    kotlin("multiplatform") version "1.9.0"
    id("io.github.ireader.plugin") version "1.0.0"
}

kotlin {
    jvm()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.ireader:plugin-api:1.0.0")
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.github.ireader:plugin-test-framework:1.0.0")
            }
        }
    }
}

ireaderPlugin {
    pluginName = "$name"
    pluginType = "${type.name}"
    outputDir = file("build/plugins")
}
        """.trimIndent())
    }
    
    private fun generateReadme(projectDir: File, name: String, type: PluginType) {
        val readmeFile = File(projectDir, "README.md")
        readmeFile.writeText("""
# $name Plugin

A ${type.name.lowercase()} plugin for IReader.

## Description

TODO: Add description

## Features

- TODO: List features

## Installation

1. Build the plugin:
   ```bash
   ./gradlew packagePlugin
   ```

2. Install in IReader:
   ```bash
   cp build/plugins/$name.iplugin ~/.ireader/plugins/
   ```

3. Restart IReader and enable the plugin in Settings → Plugins

## Development

### Building

```bash
./gradlew build
```

### Testing

```bash
./gradlew test
```

### Validation

```bash
./gradlew validatePlugin
```

## License

TODO: Add license
        """.trimIndent())
    }
    
    private fun generateGitignore(projectDir: File) {
        val gitignoreFile = File(projectDir, ".gitignore")
        gitignoreFile.writeText("""
.gradle/
build/
.idea/
*.iml
.DS_Store
local.properties
        """.trimIndent())
    }
}

enum class PluginType {
    THEME, TRANSLATION, TTS, FEATURE
}

// CLI entry point
fun main(args: Array<String>) {
    if (args.size < 2) {
        println("Usage: plugin-template-generator <type> <name> [output-dir]")
        println("Types: theme, translation, tts, feature")
        System.exit(1)
    }
    
    val type = PluginType.valueOf(args[0].uppercase())
    val name = args[1]
    val outputDir = if (args.size > 2) File(args[2]) else File(".")
    
    val generator = PluginTemplateGenerator()
    generator.generate(type, name, outputDir)
}
