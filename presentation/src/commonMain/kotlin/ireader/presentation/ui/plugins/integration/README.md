# Feature Plugin Integration

This package provides integration between Feature Plugins and the IReader app navigation and reader systems.

## Components

### FeaturePluginIntegration
Main integration class that:
- Collects menu items from all enabled feature plugins
- Collects screens from all enabled feature plugins
- Handles reader context events and notifies plugins
- Provides plugin data storage API

**Requirements**: 6.1, 6.2, 6.3, 6.4, 6.5

### PluginDataStorage
Provides persistent storage API for plugins to save preferences and data.

**Requirements**: 6.5

### ReaderPluginMenuIntegration
Helper for rendering plugin menu items in the reader screen's overflow menu or bottom sheet.

**Requirements**: 6.1

### ReaderContextHandler
Handles reader context events (text selection, chapter change, bookmark) and notifies plugins.

**Requirements**: 6.3

### PluginNavigationExtensions
Extensions for registering plugin screens with the app's navigation system.

**Requirements**: 6.2

### FeaturePluginViewModel
ViewModel for managing plugin integration state in UI components.

**Requirements**: 6.1, 6.2, 6.3

## Usage

### 1. Setup DI Module

Add the plugin integration module to your Koin configuration:

```kotlin
startKoin {
    modules(
        // ... other modules
        pluginIntegrationModule
    )
}
```

### 2. Register Plugin Screens

In your navigation setup (e.g., `CommonNavHost.kt`):

```kotlin
@Composable
fun AppNavHost(
    navController: NavHostController,
    featurePluginIntegration: FeaturePluginIntegration
) {
    NavHost(navController = navController, startDestination = "main") {
        // ... your regular routes
        
        // Register plugin screens
        PluginNavigationExtensions.registerPluginScreens(
            navGraphBuilder = this,
            featurePluginIntegration = featurePluginIntegration
        )
    }
}
```

### 3. Add Plugin Menu Items to Reader

In your reader screen:

```kotlin
@Composable
fun ReaderScreen(
    bookId: Long,
    chapterId: Long,
    featurePluginIntegration: FeaturePluginIntegration,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    val pluginState = rememberReaderPluginIntegration(
        featurePluginIntegration = featurePluginIntegration,
        bookId = bookId,
        chapterId = chapterId,
        currentPosition = currentPosition,
        selectedText = selectedText,
        navController = navController,
        scope = scope
    )
    
    // In your overflow menu or bottom sheet:
    if (pluginState.hasPlugins) {
        ReaderPluginMenuIntegration.PluginMenuItems(
            menuItems = pluginState.menuItems,
            navController = navController,
            scope = scope,
            onDismiss = { /* close menu */ }
        )
    }
}
```

### 4. Notify Plugins of Reader Events

```kotlin
// When text is selected
pluginState.contextHandler.onTextSelection(
    bookId = bookId,
    chapterId = chapterId,
    selectedText = selectedText,
    currentPosition = position,
    scope = scope,
    navController = navController
)

// When chapter changes
pluginState.contextHandler.onChapterChange(
    bookId = bookId,
    chapterId = newChapterId,
    currentPosition = position,
    scope = scope,
    navController = navController
)

// When bookmark is created
pluginState.contextHandler.onBookmark(
    bookId = bookId,
    chapterId = chapterId,
    currentPosition = position,
    scope = scope,
    navController = navController
)
```

### 5. Plugin Data Storage

Plugins can access their data storage through the PluginContext:

```kotlin
class MyFeaturePlugin : FeaturePlugin {
    private lateinit var dataStore: PluginDataStore
    
    override fun initialize(context: PluginContext) {
        // Get plugin-specific data storage
        dataStore = context.getDataStore()
        
        // Save data
        dataStore.putString("key", "value")
        
        // Read data
        val value = dataStore.getString("key", "default")
        
        // Observe data changes
        dataStore.observeString("key").collect { value ->
            // Handle value changes
        }
    }
}
```

## Error Handling

All integration points handle errors gracefully without disrupting the main app functionality:

- Plugin loading errors don't crash the app
- Plugin screen rendering errors show an error screen
- Plugin action execution errors are logged but don't affect navigation
- Plugin context event errors don't interrupt reading

**Requirements**: 6.4

## Architecture

```
┌─────────────────────────────────────────┐
│         Reader Screen / App UI          │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│    FeaturePluginIntegration             │
│  - getPluginMenuItems()                 │
│  - getPluginScreens()                   │
│  - handleReaderContext()                │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│         PluginManager                   │
│  - getEnabledPlugins()                  │
│  - getPluginsByType(FEATURE)            │
└────────────────┬────────────────────────┘
                 │
                 ▼
┌─────────────────────────────────────────┐
│       Feature Plugins                   │
│  - getMenuItems()                       │
│  - getScreens()                         │
│  - onReaderContext()                    │
└─────────────────────────────────────────┘
```

## Testing

To test plugin integration:

1. Create a test feature plugin
2. Register it with the PluginManager
3. Enable the plugin
4. Verify menu items appear in the reader
5. Verify plugin screens are navigable
6. Verify reader context events are received

Example test plugin:

```kotlin
class TestFeaturePlugin : FeaturePlugin {
    override val manifest = PluginManifest(
        id = "test.feature",
        name = "Test Feature",
        type = PluginType.FEATURE,
        // ... other manifest fields
    )
    
    override fun getMenuItems() = listOf(
        PluginMenuItem(
            id = "test.action",
            label = "Test Action",
            order = 0
        )
    )
    
    override fun getScreens() = listOf(
        PluginScreen(
            route = "plugin/test",
            title = "Test Screen",
            content = { TestScreenContent() }
        )
    )
    
    override fun onReaderContext(context: ReaderContext): PluginAction? {
        // Handle reader events
        return null
    }
    
    override fun getPreferencesScreen() = null
    
    override fun initialize(context: PluginContext) {}
    override fun cleanup() {}
}
```
