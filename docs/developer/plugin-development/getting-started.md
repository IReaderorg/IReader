# Getting Started with Plugin Development

This guide will walk you through creating your first IReader plugin.

## Prerequisites

- Kotlin 1.9.0+
- Gradle 8.0+
- IReader SDK 1.0.0+
- Basic understanding of Kotlin and Compose Multiplatform

## Setting Up Your Development Environment

### 1. Install the IReader SDK

Add the IReader SDK to your project dependencies:

```kotlin
dependencies {
    implementation("io.github.ireader:plugin-api:1.0.0")
}
```

### 2. Generate a Plugin Template

Use the template generator to create a skeleton project:

```bash
./gradlew generatePluginTemplate --type=theme --name=MyAwesomeTheme
```

Available plugin types:
- `theme` - Visual themes
- `translation` - Translation services
- `tts` - Text-to-speech engines
- `feature` - Custom features

### 3. Project Structure

Your generated plugin will have this structure:

```
MyAwesomeTheme/
├── build.gradle.kts
├── src/
│   └── commonMain/
│       └── kotlin/
│           └── MyAwesomeThemePlugin.kt
├── resources/
│   ├── plugin.json
│   └── icon.png
└── README.md
```

## Creating Your First Plugin

### 1. Define the Manifest

Edit `resources/plugin.json`:

```json
{
  "id": "com.example.myawesometheme",
  "name": "My Awesome Theme",
  "version": "1.0.0",
  "versionCode": 1,
  "description": "A beautiful theme for IReader",
  "author": {
    "name": "Your Name",
    "email": "you@example.com",
    "website": "https://example.com"
  },
  "type": "THEME",
  "permissions": [],
  "minIReaderVersion": "1.0.0",
  "platforms": ["ANDROID", "IOS", "DESKTOP"],
  "monetization": {
    "type": "FREE"
  }
}
```

### 2. Implement the Plugin

Edit `MyAwesomeThemePlugin.kt`:

```kotlin
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import ireader.domain.plugins.Plugin
import ireader.domain.plugins.PluginContext
import ireader.domain.plugins.PluginManifest
import ireader.domain.plugins.ThemePlugin
import ireader.presentation.ui.theme.ExtraColors

class MyAwesomeThemePlugin : ThemePlugin {
    override val manifest: PluginManifest
        get() = loadManifest()
    
    override fun initialize(context: PluginContext) {
        // Initialize your plugin
    }
    
    override fun cleanup() {
        // Clean up resources
    }
    
    override fun getColorScheme(isDark: Boolean): ColorScheme {
        return if (isDark) {
            darkColorScheme(
                primary = Color(0xFF6200EE),
                secondary = Color(0xFF03DAC6)
                // ... more colors
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF6200EE),
                secondary = Color(0xFF03DAC6)
                // ... more colors
            )
        }
    }
    
    override fun getExtraColors(isDark: Boolean): ExtraColors {
        // Return custom colors
    }
    
    override fun getTypography(): Typography? = null
    
    override fun getBackgroundAssets(): ThemeBackgrounds? = null
}
```

### 3. Test Your Plugin

Run the validator:

```bash
./gradlew validatePlugin --plugin=MyAwesomeTheme
```

### 4. Package Your Plugin

Create a distributable package:

```bash
./gradlew packagePlugin --plugin=MyAwesomeTheme
```

This creates `MyAwesomeTheme.iplugin` in the `build/plugins/` directory.

### 5. Test in IReader

1. Copy the `.iplugin` file to IReader's plugins directory
2. Restart IReader
3. Go to Settings → Plugins → Installed
4. Enable your plugin

## Next Steps

- Learn about [Plugin Types](plugin-types.md)
- Read the [API Reference](api-reference.md)
- Check out [Example Plugins](../examples/)
- Review [Best Practices](best-practices.md)
