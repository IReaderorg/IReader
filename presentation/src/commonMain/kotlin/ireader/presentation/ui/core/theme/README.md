# Theme Plugin Integration

This directory contains the implementation for integrating theme plugins with the IReader app theme system.

## Quick Start

### For App Developers

To integrate plugin themes into your appearance settings:

```kotlin
@Composable
fun AppearanceSettingScreen(
    viewModel: AppearanceViewModel,
    pluginManager: PluginManager
) {
    LazyColumnWithInsets {
        // ... existing settings ...
        
        // Add plugin theme section
        item {
            PluginThemeSection(
                pluginManager = pluginManager,
                viewModel = viewModel,
                onThemeSelected = { themeOption ->
                    val theme = viewModel.applyTheme(themeOption)
                    viewModel.colorTheme.value = theme.id
                },
                currentThemeId = viewModel.colorTheme.value
            )
        }
    }
}
```

### For Plugin Developers

Create a theme plugin by implementing `ThemePlugin`:

```kotlin
class MyThemePlugin : ThemePlugin {
    override val manifest = PluginManifest(
        id = "com.example.my-theme",
        name = "My Theme",
        version = "1.0.0",
        // ... other manifest fields
    )
    
    override fun getColorScheme(isDark: Boolean): ThemeColorScheme {
        // Return your color scheme
    }
    
    override fun getExtraColors(isDark: Boolean): ThemeExtraColors {
        // Return extra colors
    }
    
    // Optional: custom typography and backgrounds
    override fun getTypography(): ThemeTypography? = null
    override fun getBackgroundAssets(): ThemeBackgrounds? = null
}
```

## Files

### Core Components
- **ThemeOption.kt** - Theme choice representation
- **PluginThemeManager.kt** - Theme plugin management
- **ThemeErrorHandler.kt** - Error handling with fallback
- **ThemeHotReload.kt** - Development hot reload support
- **ThemeAssetLoader.kt** - Asset loading utility
- **ThemePreview.kt** - Theme preview composables

### UI Components
- **PluginThemeSection.kt** - Settings UI section
- **AppearanceViewModelPluginExtension.kt** - ViewModel extension

### Documentation
- **PLUGIN_THEME_INTEGRATION_GUIDE.md** - Detailed integration guide
- **IMPLEMENTATION_SUMMARY.md** - Implementation overview
- **README.md** - This file

### Examples
- **docs/plugin-development/ExampleThemePlugin.kt** - Example implementations

## Features

✅ Automatic theme plugin discovery  
✅ Seamless theme switching  
✅ Error handling with fallback  
✅ Hot reload for development  
✅ Asset loading (backgrounds, images)  
✅ Theme preview functionality  
✅ Full Material 3 color scheme support  
✅ Custom typography support  
✅ Reactive UI updates  

## Requirements Satisfied

- **3.1** - Theme plugins added to available themes list
- **3.2** - Themes applied immediately when selected
- **3.3** - Supports colors, fonts, backgrounds, UI elements
- **3.4** - Validates and loads theme assets
- **3.5** - Preview, error handling, hot reload support

## Documentation

- See **PLUGIN_THEME_INTEGRATION_GUIDE.md** for detailed integration instructions
- See **IMPLEMENTATION_SUMMARY.md** for architecture and design decisions
- See **ExampleThemePlugin.kt** for example implementations

## Support

For issues or questions:
1. Check the integration guide
2. Review example implementations
3. Check error logs in ThemeErrorHandler
4. Enable hot reload for development debugging

## License

Part of the IReader project.
