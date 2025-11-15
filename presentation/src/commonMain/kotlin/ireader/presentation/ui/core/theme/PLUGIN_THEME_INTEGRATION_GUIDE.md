# Plugin Theme Integration Guide

This guide explains how to integrate theme plugins with the IReader app theme system.

## Overview

The plugin theme integration allows third-party developers to create custom themes that seamlessly integrate with the app's appearance settings. The system provides:

- **Theme Discovery**: Automatic detection and loading of theme plugins
- **Theme Application**: Seamless switching between built-in and plugin themes
- **Error Handling**: Graceful fallback to default themes on errors
- **Hot Reload**: Development support for live theme updates
- **Asset Loading**: Support for custom backgrounds and images

## Architecture

### Core Components

1. **ThemeOption** - Sealed class representing theme choices (built-in or plugin)
2. **PluginThemeManager** - Manages theme plugin lifecycle and application
3. **ThemeErrorHandler** - Handles errors with fallback support
4. **ThemeHotReloadManager** - Development tool for live theme updates
5. **ThemeAssetLoader** - Loads custom backgrounds and images
6. **ThemePreview** - Composables for previewing themes

### Integration Flow

```
User selects theme → ThemeOption created → PluginThemeManager applies theme
                                                    ↓
                                          Error? → Fallback to default
                                                    ↓
                                          Success → Theme applied
```

## Usage

### 1. Basic Integration in AppearanceViewModel

```kotlin
class AppearanceViewModel(
    val uiPreferences: UiPreferences,
    val themeRepository: ThemeRepository,
    val pluginManager: PluginManager // Add this dependency
) : BaseViewModel() {
    
    // Create plugin extension
    private val pluginExtension = AppearanceViewModelPluginExtension(
        pluginManager = pluginManager,
        baseViewModel = this
    )
    
    // Get all themes including plugins
    fun getAllAvailableThemes(): List<ThemeOption> {
        return pluginExtension.getAllThemes()
    }
    
    // Apply a theme (built-in or plugin)
    fun applyTheme(themeOption: ThemeOption) {
        val theme = pluginExtension.applyTheme(themeOption)
        colorTheme.value = theme.id
    }
}
```

### 2. Display Plugin Themes in UI

```kotlin
@Composable
fun AppearanceSettingScreen(
    viewModel: AppearanceViewModel,
    pluginManager: PluginManager
) {
    LazyColumnWithInsets {
        // ... existing built-in themes ...
        
        // Add plugin theme section
        item {
            PluginThemeSection(
                pluginManager = pluginManager,
                viewModel = viewModel,
                onThemeSelected = { themeOption ->
                    viewModel.applyTheme(themeOption)
                },
                currentThemeId = viewModel.colorTheme.value
            )
        }
    }
}
```

### 3. Enable Hot Reload (Development Only)

```kotlin
@Composable
fun AppearanceSettingScreen(
    viewModel: AppearanceViewModel,
    pluginManager: PluginManager
) {
    val pluginExtension = rememberPluginThemeIntegration(pluginManager, viewModel)
    val hotReloadManager = pluginExtension.getHotReloadManager()
    
    // Enable hot reload in debug builds
    val isDebug = BuildConfig.DEBUG
    val reloadTrigger by rememberThemeHotReload(hotReloadManager, enabled = isDebug)
    
    // UI will automatically update when themes reload
}
```

### 4. Handle Theme Errors

```kotlin
@Composable
fun ThemeErrorDisplay(
    pluginManager: PluginManager,
    viewModel: AppearanceViewModel
) {
    val pluginExtension = rememberPluginThemeIntegration(pluginManager, viewModel)
    val errors by rememberThemeErrors(pluginExtension.getErrorHandler())
    
    if (errors.isNotEmpty()) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Theme Errors:", style = MaterialTheme.typography.titleSmall)
                errors.forEach { error ->
                    Text(error.toUserMessage())
                }
                TextButton(
                    onClick = { pluginExtension.getErrorHandler().clearErrors() }
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}
```

### 5. Load Theme Assets

```kotlin
@Composable
fun ReaderScreenWithPluginBackground(
    theme: Theme,
    pluginManager: PluginManager
) {
    val pluginExtension = rememberPluginThemeIntegration(pluginManager, viewModel)
    val assetLoader = remember { ThemeAssetLoader() }
    
    // Check if current theme is a plugin theme
    val themeOption = pluginExtension.getThemeById(theme.id.toString())
    
    if (themeOption is ThemeOption.Plugin) {
        val backgrounds = pluginExtension.getPluginBackgrounds(themeOption.plugin)
        val readerBackground by rememberThemeBackground(backgrounds?.first)
        
        // Use the background if loaded
        if (readerBackground != null) {
            Image(
                bitmap = readerBackground!!,
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    // ... rest of reader UI ...
}
```

## Theme Preview

The system provides composables for previewing themes:

```kotlin
@Composable
fun ThemePreviewDialog(theme: Theme) {
    Dialog(onDismissRequest = {}) {
        Card(modifier = Modifier.size(400.dp, 600.dp)) {
            ThemePreview(theme = theme)
        }
    }
}

// Or use mini preview for grid display
@Composable
fun ThemeGridItem(theme: Theme) {
    ThemePreviewMini(
        theme = theme,
        modifier = Modifier.size(120.dp, 80.dp)
    )
}
```

## Error Handling

The system provides comprehensive error handling:

### Error Types

1. **ApplicationFailed** - Theme failed to apply
2. **PluginLoadFailed** - Plugin failed to load
3. **AssetLoadFailed** - Asset (background/image) failed to load
4. **InvalidConfiguration** - Plugin configuration is invalid

### Fallback Behavior

When a theme plugin fails:
1. Error is logged and displayed to user
2. System falls back to default theme
3. User can continue using the app
4. Error can be dismissed or plugin can be reloaded

## Development Tools

### Hot Reload

Enable hot reload during development:

```kotlin
// In your development configuration
ThemeDevelopmentMode.enable()

// Hot reload will automatically detect plugin changes
// and reload themes without restarting the app
```

### Manual Reload

Trigger manual reload:

```kotlin
scope.launch {
    pluginExtension.reloadPluginThemes()
}

// Or reload specific plugin
scope.launch {
    pluginExtension.reloadPluginTheme("plugin-id")
}
```

## Best Practices

1. **Always handle errors**: Use ThemeErrorHandler to catch and handle theme errors
2. **Provide fallbacks**: Ensure default theme is always available
3. **Test with plugins disabled**: App should work without any plugins
4. **Cache assets**: Use ThemeAssetLoader's caching for better performance
5. **Clear cache on uninstall**: Clean up when plugins are removed
6. **Validate themes**: Check theme compatibility before applying
7. **Use hot reload in dev**: Speed up theme development with hot reload

## Requirements Mapping

This implementation satisfies the following requirements:

- **3.1**: Theme plugins are added to available themes list
- **3.2**: Themes are applied immediately when selected
- **3.3**: Supports colors, fonts, backgrounds, and UI elements
- **3.4**: Validates and loads theme assets
- **3.5**: Provides preview, error handling, and hot reload

## Testing

Test the integration:

```kotlin
@Test
fun testPluginThemeIntegration() {
    val pluginManager = mockPluginManager()
    val viewModel = AppearanceViewModel(...)
    val extension = AppearanceViewModelPluginExtension(pluginManager, viewModel)
    
    // Test theme discovery
    val themes = extension.getAllThemes()
    assertTrue(themes.isNotEmpty())
    
    // Test theme application
    val theme = extension.applyTheme(themes.first())
    assertNotNull(theme)
    
    // Test error handling
    val errorHandler = extension.getErrorHandler()
    assertNotNull(errorHandler)
}
```

## Troubleshooting

### Themes not appearing
- Check if plugins are enabled in PluginManager
- Verify plugin manifest is valid
- Check plugin permissions

### Theme fails to apply
- Check error logs in ThemeErrorHandler
- Verify theme colors are valid
- Ensure plugin is compatible with app version

### Assets not loading
- Check asset paths in plugin manifest
- Verify file permissions
- Check ThemeAssetLoader cache

### Hot reload not working
- Ensure ThemeDevelopmentMode is enabled
- Check if plugin files are being watched
- Verify plugin directory is accessible

## Future Enhancements

Potential improvements:

1. Theme marketplace integration
2. Theme rating and reviews
3. Theme categories and tags
4. Theme sharing between users
5. Theme customization UI
6. Advanced asset management
7. Theme animation support
8. Theme accessibility features
