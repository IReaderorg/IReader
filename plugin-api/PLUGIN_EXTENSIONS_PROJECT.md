# IReader-plugins Project Structure

This document describes the structure for a separate `IReader-plugins` repository (similar to `IReader-extensions`) for creating and distributing IReader app plugins.

## Project Structure

```
IReader-plugins/
├── build.gradle.kts              # Root build configuration
├── settings.gradle.kts           # Module includes
├── gradle/
│   └── libs.versions.toml        # Version catalog
├── common/                       # Shared utilities for plugins
│   ├── build.gradle.kts
│   └── src/
│       └── commonMain/kotlin/
│           └── ireader/plugin/common/
│               ├── HttpHelper.kt
│               └── JsonHelper.kt
├── buildSrc/                     # Build logic
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       ├── PluginExtension.kt    # Plugin DSL
│       └── PluginTask.kt         # Build tasks
├── plugins/                      # Plugin implementations
│   ├── themes/
│   │   ├── ocean-theme/
│   │   │   ├── build.gradle.kts
│   │   │   └── src/main/kotlin/OceanTheme.kt
│   │   └── dark-reader/
│   │       ├── build.gradle.kts
│   │       └── src/main/kotlin/DarkReaderTheme.kt
│   ├── tts/
│   │   ├── cloud-tts/
│   │   │   ├── build.gradle.kts
│   │   │   └── src/main/kotlin/CloudTTS.kt
│   │   └── local-tts/
│   │       ├── build.gradle.kts
│   │       └── src/main/kotlin/LocalTTS.kt
│   ├── translation/
│   │   ├── deepl/
│   │   │   ├── build.gradle.kts
│   │   │   └── src/main/kotlin/DeepLTranslation.kt
│   │   └── google-translate/
│   │       ├── build.gradle.kts
│   │       └── src/main/kotlin/GoogleTranslate.kt
│   └── features/
│       ├── dictionary/
│       │   ├── build.gradle.kts
│       │   └── src/main/kotlin/DictionaryPlugin.kt
│       └── reading-stats/
│           ├── build.gradle.kts
│           └── src/main/kotlin/ReadingStatsPlugin.kt
└── repo/                         # Generated plugin repository
    └── index.json
```

## Root build.gradle.kts

```kotlin
buildscript {
    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath(libs.android.gradle)
        classpath(libs.kotlin.gradle)
        classpath(libs.serialization.gradle)
    }
}

tasks.register("delete", Delete::class) {
    delete(rootProject.buildDir)
}

// Generate plugin repository index
tasks.register<PluginRepoTask>("repo") {
    group = "build"
    description = "Generate plugin repository index"
}
```

## Root settings.gradle.kts

```kotlin
enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

include(":common")

// Auto-include all plugins
File(rootDir, "plugins").eachDir { category ->
    category.eachDir { plugin ->
        if (File(plugin, "build.gradle.kts").exists()) {
            val name = ":plugins:${category.name}:${plugin.name}"
            include(name)
            project(name).projectDir = plugin
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        google()
        maven { setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots/") }
    }
}

inline fun File.eachDir(block: (File) -> Unit) {
    listFiles()?.filter { it.isDirectory }?.forEach { block(it) }
}
```

## gradle/libs.versions.toml

```toml
[versions]
kotlin = "2.1.0"
agp = "8.7.3"
serialization = "1.7.3"
coroutines = "1.9.0"
ktor = "3.0.3"
plugin-api = "1.0.0"

[libraries]
kotlin-gradle = { module = "org.jetbrains.kotlin:kotlin-gradle-plugin", version.ref = "kotlin" }
android-gradle = { module = "com.android.tools.build:gradle", version.ref = "agp" }
serialization-gradle = { module = "org.jetbrains.kotlin:kotlin-serialization", version.ref = "kotlin" }
serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
ktor-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-cio = { module = "io.ktor:ktor-client-cio", version.ref = "ktor" }
plugin-api = { module = "io.github.ireaderorg:plugin-api", version.ref = "plugin-api" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
android-library = { id = "com.android.library", version.ref = "agp" }
```

## common/build.gradle.kts

```kotlin
plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            api(libs.plugin.api)
            api(libs.coroutines.core)
            api(libs.serialization.json)
            api(libs.ktor.core)
        }
        
        jvmMain.dependencies {
            implementation(libs.ktor.cio)
        }
    }
}
```

## Example Plugin: plugins/themes/ocean-theme/build.gradle.kts

```kotlin
plugins {
    id("ireader.plugin")
}

pluginConfig {
    id = "com.example.ocean-theme"
    name = "Ocean Theme"
    version = "1.0.0"
    versionCode = 1
    description = "A calming ocean-inspired theme"
    author = "Developer Name"
    type = PluginType.THEME
    minIReaderVersion = "1.0.0"
}
```

## Example Plugin: plugins/themes/ocean-theme/src/main/kotlin/OceanTheme.kt

```kotlin
package com.example.oceantheme

import ireader.plugin.api.*

class OceanTheme : ThemePlugin {
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
    
    override fun initialize(context: PluginContext) {
        // Initialize theme resources
    }
    
    override fun cleanup() {
        // Release resources
    }
    
    override fun getColorScheme(isDark: Boolean): ThemeColorScheme {
        return if (isDark) darkColors else lightColors
    }
    
    override fun getExtraColors(isDark: Boolean): ThemeExtraColors {
        return ThemeExtraColors(
            bars = if (isDark) 0xFF0D47A1 else 0xFF1976D2,
            onBars = 0xFFFFFFFF,
            isBarLight = false
        )
    }
    
    private val lightColors = ThemeColorScheme(
        primary = 0xFF1976D2,
        onPrimary = 0xFFFFFFFF,
        primaryContainer = 0xFFBBDEFB,
        onPrimaryContainer = 0xFF0D47A1,
        secondary = 0xFF00ACC1,
        onSecondary = 0xFFFFFFFF,
        secondaryContainer = 0xFFB2EBF2,
        onSecondaryContainer = 0xFF006064,
        tertiary = 0xFF26A69A,
        onTertiary = 0xFFFFFFFF,
        tertiaryContainer = 0xFFB2DFDB,
        onTertiaryContainer = 0xFF004D40,
        error = 0xFFB00020,
        onError = 0xFFFFFFFF,
        errorContainer = 0xFFFCD8DF,
        onErrorContainer = 0xFF8B0000,
        background = 0xFFE3F2FD,
        onBackground = 0xFF0D47A1,
        surface = 0xFFFFFFFF,
        onSurface = 0xFF1A237E,
        surfaceVariant = 0xFFE8EAF6,
        onSurfaceVariant = 0xFF3F51B5,
        outline = 0xFF90CAF9,
        outlineVariant = 0xFFBBDEFB,
        scrim = 0xFF000000,
        inverseSurface = 0xFF1A237E,
        inverseOnSurface = 0xFFE8EAF6,
        inversePrimary = 0xFF82B1FF
    )
    
    private val darkColors = ThemeColorScheme(
        primary = 0xFF82B1FF,
        onPrimary = 0xFF0D47A1,
        primaryContainer = 0xFF1565C0,
        onPrimaryContainer = 0xFFBBDEFB,
        secondary = 0xFF80DEEA,
        onSecondary = 0xFF006064,
        secondaryContainer = 0xFF00838F,
        onSecondaryContainer = 0xFFB2EBF2,
        tertiary = 0xFF80CBC4,
        onTertiary = 0xFF004D40,
        tertiaryContainer = 0xFF00695C,
        onTertiaryContainer = 0xFFB2DFDB,
        error = 0xFFCF6679,
        onError = 0xFF000000,
        errorContainer = 0xFF8B0000,
        onErrorContainer = 0xFFFCD8DF,
        background = 0xFF0D1B2A,
        onBackground = 0xFFE3F2FD,
        surface = 0xFF1B2838,
        onSurface = 0xFFE8EAF6,
        surfaceVariant = 0xFF263238,
        onSurfaceVariant = 0xFF90CAF9,
        outline = 0xFF546E7A,
        outlineVariant = 0xFF37474F,
        scrim = 0xFF000000,
        inverseSurface = 0xFFE8EAF6,
        inverseOnSurface = 0xFF1A237E,
        inversePrimary = 0xFF1976D2
    )
}
```

## buildSrc/src/main/kotlin/PluginExtension.kt

```kotlin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import ireader.plugin.api.PluginType

abstract class PluginExtension {
    abstract val id: Property<String>
    abstract val name: Property<String>
    abstract val version: Property<String>
    abstract val versionCode: Property<Int>
    abstract val description: Property<String>
    abstract val author: Property<String>
    abstract val type: Property<PluginType>
    abstract val minIReaderVersion: Property<String>
    abstract val monetization: Property<String?>
}

// Plugin DSL
fun Project.pluginConfig(block: PluginExtension.() -> Unit) {
    extensions.configure<PluginExtension>("pluginConfig", block)
}
```

## Plugin Package Format (.iplugin)

The `.iplugin` file is a ZIP archive containing:

```
my-plugin.iplugin
├── plugin.json          # Plugin manifest (generated from build config)
├── classes.dex          # Android: Compiled DEX
├── classes.jar          # Desktop: Compiled JAR
└── resources/           # Optional resources (icons, etc.)
```

## Repository Index Format (repo/index.json)

```json
{
  "version": 1,
  "plugins": [
    {
      "id": "com.example.ocean-theme",
      "name": "Ocean Theme",
      "version": "1.0.0",
      "versionCode": 1,
      "description": "A calming ocean-inspired theme",
      "author": {
        "name": "Developer Name"
      },
      "type": "THEME",
      "minIReaderVersion": "1.0.0",
      "platforms": ["ANDROID", "IOS", "DESKTOP"],
      "downloadUrl": "https://example.com/plugins/ocean-theme-1.0.0.iplugin",
      "iconUrl": "https://example.com/plugins/ocean-theme/icon.png",
      "fileSize": 12345,
      "checksum": "sha256:abc123..."
    }
  ]
}
```

## Building Plugins

```bash
# Build all plugins
./gradlew assembleRelease

# Build specific plugin
./gradlew :plugins:themes:ocean-theme:assembleRelease

# Generate repository index
./gradlew repo
```

## Publishing

Plugins can be published to:
1. GitHub Releases
2. Custom plugin repository
3. IReader Plugin Store (when available)

The `repo` task generates an `index.json` that can be hosted alongside the `.iplugin` files.
