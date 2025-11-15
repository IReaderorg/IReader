# IReader Plugin File Format Specification

## Overview

IReader plugins use the `.iplugin` file extension. An `.iplugin` file is a ZIP archive containing the plugin manifest and compiled plugin classes.

## File Structure

```
plugin-name.iplugin (ZIP archive)
├── plugin.json          # Plugin manifest (required)
├── classes/             # Compiled plugin classes
│   └── [plugin classes]
└── resources/           # Optional plugin resources
    ├── icons/
    ├── images/
    └── assets/
```

## Plugin Manifest (plugin.json)

The `plugin.json` file must be located at the root of the ZIP archive and contains the plugin metadata:

```json
{
  "id": "com.example.myplugin",
  "name": "My Plugin",
  "version": "1.0.0",
  "versionCode": 1,
  "description": "A sample plugin for IReader",
  "author": {
    "name": "John Doe",
    "email": "john@example.com",
    "website": "https://example.com"
  },
  "type": "THEME",
  "permissions": ["NETWORK", "STORAGE"],
  "minIReaderVersion": "1.0.0",
  "platforms": ["ANDROID", "DESKTOP"],
  "monetization": {
    "type": "Premium",
    "price": 2.99,
    "currency": "USD",
    "trialDays": 7
  },
  "iconUrl": "https://example.com/icon.png",
  "screenshotUrls": [
    "https://example.com/screenshot1.png",
    "https://example.com/screenshot2.png"
  ]
}
```

### Manifest Fields

- **id** (required): Unique plugin identifier (reverse domain notation recommended)
- **name** (required): Human-readable plugin name
- **version** (required): Semantic version (e.g., "1.0.0")
- **versionCode** (required): Integer version code for comparison
- **description** (required): Plugin description
- **author** (required): Author information
  - **name** (required): Author name
  - **email** (optional): Contact email
  - **website** (optional): Author website
- **type** (required): Plugin type - one of: THEME, TRANSLATION, TTS, FEATURE
- **permissions** (required): Array of required permissions
  - NETWORK: Access to network
  - STORAGE: Access to local storage
  - READER_CONTEXT: Access to reading context
  - LIBRARY_ACCESS: Access to user's library
  - PREFERENCES: Access to app preferences
  - NOTIFICATIONS: Show notifications
- **minIReaderVersion** (required): Minimum IReader version required
- **platforms** (required): Supported platforms - array of: ANDROID, IOS, DESKTOP
- **monetization** (optional): Monetization configuration
  - **Premium**: Paid plugin
    - price: Price amount
    - currency: Currency code (e.g., "USD")
    - trialDays: Optional trial period in days
  - **Freemium**: Free with in-plugin purchases
    - features: Array of premium features
  - **Free**: Completely free
- **iconUrl** (optional): URL to plugin icon
- **screenshotUrls** (optional): Array of screenshot URLs

## Plugin Class Convention

The main plugin class must follow this naming convention:
- Class name: `{manifest.id}.Plugin`
- Must implement the `Plugin` interface
- Must have a no-argument constructor

Example:
```kotlin
package com.example.myplugin

class Plugin : ireader.domain.plugins.Plugin {
    override val manifest: PluginManifest
        get() = // ... load from resources or hardcode
    
    override fun initialize(context: PluginContext) {
        // Initialize plugin
    }
    
    override fun cleanup() {
        // Cleanup resources
    }
}
```

## Platform-Specific Formats

### Android
- `.iplugin` files are APK/DEX files
- Loaded using `DexClassLoader`
- Must contain compiled DEX bytecode

### Desktop
- `.iplugin` files are JAR files
- Loaded using `URLClassLoader`
- Must contain compiled JVM bytecode

## Creating a Plugin Package

### Android
```bash
# Compile your plugin to DEX
kotlinc-jvm -d plugin.jar Plugin.kt
dx --dex --output=plugin.dex plugin.jar

# Create the package
zip plugin-name.iplugin plugin.json classes.dex
```

### Desktop
```bash
# Compile your plugin to JAR
kotlinc-jvm -d plugin.jar Plugin.kt

# Create the package
zip plugin-name.iplugin plugin.json -r classes/
```

## Validation

Before loading, the plugin loader validates:
1. Manifest format and required fields
2. Version format (semantic versioning)
3. Minimum IReader version compatibility
4. Platform compatibility
5. Permission validity
6. Monetization configuration (if present)

## Security

Plugins are executed in a sandboxed environment with:
- Restricted file system access (plugin's own directory only)
- Permission-based network access
- No access to sensitive system resources without explicit permissions
- Resource usage monitoring and limits
