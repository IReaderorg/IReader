# Design Document

## Overview

This design implements a comprehensive plugin system for IReader that enables third-party developers to extend functionality through themes, translations, TTS engines, and custom features. The system leverages the existing catalog architecture (CatalogStore, CatalogLoader) as a foundation and extends it to support multiple plugin types with monetization capabilities.

The plugin system follows a modular architecture with clear separation between plugin types, secure sandboxing, and a marketplace for discovery and installation. It integrates seamlessly with the existing Koin DI system and Compose Multiplatform UI.

## Architecture

### Core Components

1. **Plugin Manager**: Central service managing plugin lifecycle (load, enable, disable, uninstall)
2. **Plugin Loader**: Loads and validates plugin packages from the plugins directory
3. **Plugin Registry**: Maintains registry of installed plugins and their metadata
4. **Plugin Marketplace**: UI and backend for discovering, installing, and purchasing plugins
5. **Plugin API**: Interfaces that plugins implement (ThemePlugin, TranslationPlugin, TTSPlugin, FeaturePlugin)
6. **Plugin Sandbox**: Security layer restricting plugin access to system resources
7. **Monetization Service**: Handles premium plugin purchases and in-plugin transactions

### Plugin Types


#### 1. Theme Plugin
- Provides custom color schemes, fonts, backgrounds, and UI styling
- Implements `ThemePlugin` interface with `getColorScheme()`, `getTypography()`, `getBackgrounds()`
- Integrates with existing ExtraColors and theme system

#### 2. Translation Plugin
- Provides text translation services using various engines (Google, DeepL, custom)
- Implements `TranslationPlugin` interface with `translate()`, `getSupportedLanguages()`
- Integrates with existing TranslationEnginesManager

#### 3. TTS Plugin
- Provides text-to-speech engines with custom voices
- Implements `TTSPlugin` interface with `speak()`, `getVoices()`, `configure()`
- Extends existing VoiceCatalog and TTS service architecture

#### 4. Feature Plugin
- Adds custom functionality (reading statistics, note-taking, social features, etc.)
- Implements `FeaturePlugin` interface with `getMenuItems()`, `getScreens()`, `onReaderContext()`
- Can register custom screens and navigation routes

### Data Flow

```
User → Plugin Marketplace UI → Plugin Manager → Plugin Loader → Plugin Instance
                                      ↓
                              Plugin Registry (Database)
                                      ↓
                              Active Plugins (Memory)
```

### State Management

All plugin state managed through:
- `PluginPreferences`: User preferences for enabled/disabled plugins
- `PluginDatabase`: Installed plugins metadata and purchase records
- `PluginStateFlow`: Real-time plugin status updates
- ViewModels for each plugin-related screen



## Components and Interfaces

### 1. Plugin API Interfaces

**Location**: `domain/src/commonMain/kotlin/ireader/domain/plugins/`

```kotlin
// Base plugin interface
interface Plugin {
    val manifest: PluginManifest
    fun initialize(context: PluginContext)
    fun cleanup()
}

// Plugin manifest with metadata
data class PluginManifest(
    val id: String,
    val name: String,
    val version: String,
    val versionCode: Int,
    val description: String,
    val author: PluginAuthor,
    val type: PluginType,
    val permissions: List<PluginPermission>,
    val minIReaderVersion: String,
    val platforms: List<Platform>,
    val monetization: PluginMonetization?,
    val iconUrl: String?,
    val screenshotUrls: List<String>
)

data class PluginAuthor(
    val name: String,
    val email: String?,
    val website: String?
)

enum class PluginType {
    THEME, TRANSLATION, TTS, FEATURE
}

sealed class PluginMonetization {
    data class Premium(val price: Double, val currency: String, val trialDays: Int?) : PluginMonetization()
    data class Freemium(val features: List<PremiumFeature>) : PluginMonetization()
    object Free : PluginMonetization()
}

data class PremiumFeature(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String
)
```



### 2. Plugin Type Interfaces

```kotlin
// Theme Plugin
interface ThemePlugin : Plugin {
    fun getColorScheme(isDark: Boolean): ColorScheme
    fun getExtraColors(isDark: Boolean): ExtraColors
    fun getTypography(): Typography?
    fun getBackgroundAssets(): ThemeBackgrounds?
}

data class ThemeBackgrounds(
    val readerBackground: String?, // Asset path or URL
    val appBackground: String?
)

// Translation Plugin
interface TranslationPlugin : Plugin {
    suspend fun translate(text: String, from: String, to: String): Result<String>
    suspend fun translateBatch(texts: List<String>, from: String, to: String): Result<List<String>>
    fun getSupportedLanguages(): List<LanguagePair>
    fun requiresApiKey(): Boolean
    fun configureApiKey(key: String)
}

data class LanguagePair(val from: String, val to: String)

// TTS Plugin
interface TTSPlugin : Plugin {
    suspend fun speak(text: String, voice: VoiceConfig): Result<AudioStream>
    fun getAvailableVoices(): List<VoiceModel>
    fun supportsStreaming(): Boolean
    fun getAudioFormat(): AudioFormat
}

data class VoiceConfig(
    val voiceId: String,
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val volume: Float = 1.0f
)

// Feature Plugin
interface FeaturePlugin : Plugin {
    fun getMenuItems(): List<PluginMenuItem>
    fun getScreens(): List<PluginScreen>
    fun onReaderContext(context: ReaderContext): PluginAction?
    fun getPreferencesScreen(): PluginScreen?
}

data class PluginMenuItem(
    val id: String,
    val label: String,
    val icon: String?,
    val action: () -> Unit
)

data class PluginScreen(
    val route: String,
    val title: String,
    val content: @Composable () -> Unit
)

data class ReaderContext(
    val bookId: Long,
    val chapterId: Long,
    val selectedText: String?,
    val currentPosition: Int
)
```



### 3. Plugin Manager

**Location**: `domain/src/commonMain/kotlin/ireader/domain/plugins/PluginManager.kt`

```kotlin
class PluginManager(
    private val loader: PluginLoader,
    private val registry: PluginRegistry,
    private val preferences: PluginPreferences,
    private val monetization: MonetizationService
) {
    private val scope = createICoroutineScope()
    private val _pluginsFlow = MutableStateFlow<List<PluginInfo>>(emptyList())
    val pluginsFlow: StateFlow<List<PluginInfo>> = _pluginsFlow.asStateFlow()
    
    suspend fun loadPlugins() {
        val plugins = loader.loadAll()
        registry.registerAll(plugins)
        _pluginsFlow.value = registry.getAll()
    }
    
    suspend fun installPlugin(packageFile: File): Result<PluginInfo> {
        // Validate, extract, and install plugin
    }
    
    suspend fun uninstallPlugin(pluginId: String): Result<Unit> {
        // Remove plugin files and database entries
    }
    
    suspend fun enablePlugin(pluginId: String): Result<Unit> {
        // Enable plugin and initialize
    }
    
    suspend fun disablePlugin(pluginId: String): Result<Unit> {
        // Disable plugin and cleanup
    }
    
    fun getPlugin(pluginId: String): Plugin? {
        return registry.get(pluginId)
    }
    
    fun getPluginsByType(type: PluginType): List<Plugin> {
        return registry.getByType(type)
    }
}

data class PluginInfo(
    val id: String,
    val manifest: PluginManifest,
    val status: PluginStatus,
    val installDate: Long,
    val lastUpdate: Long?,
    val isPurchased: Boolean,
    val rating: Float?,
    val downloadCount: Int
)

enum class PluginStatus {
    ENABLED, DISABLED, ERROR, UPDATING
}
```



### 4. Plugin Loader

**Location**: `domain/src/commonMain/kotlin/ireader/domain/plugins/PluginLoader.kt`

```kotlin
class PluginLoader(
    private val pluginsDir: File,
    private val validator: PluginValidator,
    private val classLoader: PluginClassLoader
) {
    suspend fun loadAll(): List<Plugin> {
        return withContext(Dispatchers.IO) {
            pluginsDir.listFiles()
                ?.filter { it.extension == "iplugin" } // IReader Plugin format
                ?.mapNotNull { loadPlugin(it) }
                ?: emptyList()
        }
    }
    
    suspend fun loadPlugin(file: File): Plugin? {
        return try {
            // Extract manifest
            val manifest = extractManifest(file)
            
            // Validate manifest and permissions
            validator.validate(manifest)
            
            // Load plugin class
            val pluginClass = classLoader.loadPluginClass(file, manifest)
            
            // Instantiate plugin
            pluginClass.getDeclaredConstructor().newInstance() as Plugin
        } catch (e: Exception) {
            null
        }
    }
    
    private fun extractManifest(file: File): PluginManifest {
        // Extract and parse plugin.json from .iplugin file
    }
}
```

### 5. Plugin Registry

**Location**: `domain/src/commonMain/kotlin/ireader/domain/plugins/PluginRegistry.kt`

```kotlin
class PluginRegistry(
    private val database: PluginDatabase
) {
    private val plugins = mutableMapOf<String, Plugin>()
    private val lock = Mutex()
    
    suspend fun registerAll(pluginList: List<Plugin>) {
        lock.withLock {
            pluginList.forEach { plugin ->
                plugins[plugin.manifest.id] = plugin
                database.insertOrUpdate(plugin.manifest)
            }
        }
    }
    
    fun get(pluginId: String): Plugin? = plugins[pluginId]
    
    fun getAll(): List<PluginInfo> {
        return plugins.values.map { plugin ->
            val dbInfo = database.getPluginInfo(plugin.manifest.id)
            PluginInfo(
                id = plugin.manifest.id,
                manifest = plugin.manifest,
                status = dbInfo?.status ?: PluginStatus.DISABLED,
                installDate = dbInfo?.installDate ?: System.currentTimeMillis(),
                lastUpdate = dbInfo?.lastUpdate,
                isPurchased = dbInfo?.isPurchased ?: false,
                rating = dbInfo?.rating,
                downloadCount = dbInfo?.downloadCount ?: 0
            )
        }
    }
    
    fun getByType(type: PluginType): List<Plugin> {
        return plugins.values.filter { it.manifest.type == type }
    }
}
```



### 6. Monetization Service

**Location**: `domain/src/commonMain/kotlin/ireader/domain/plugins/MonetizationService.kt`

```kotlin
class MonetizationService(
    private val paymentProcessor: PaymentProcessor,
    private val purchaseRepository: PurchaseRepository
) {
    suspend fun purchasePlugin(pluginId: String, price: Double): Result<Purchase> {
        return try {
            val purchase = paymentProcessor.processPayment(pluginId, price)
            purchaseRepository.savePurchase(purchase)
            Result.success(purchase)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun purchaseFeature(pluginId: String, featureId: String, price: Double): Result<Purchase> {
        return try {
            val purchase = paymentProcessor.processPayment("$pluginId:$featureId", price)
            purchaseRepository.savePurchase(purchase)
            Result.success(purchase)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun isPurchased(pluginId: String): Boolean {
        return purchaseRepository.isPurchased(pluginId)
    }
    
    suspend fun isFeaturePurchased(pluginId: String, featureId: String): Boolean {
        return purchaseRepository.isFeaturePurchased(pluginId, featureId)
    }
    
    suspend fun syncPurchases(userId: String): Result<Unit> {
        // Sync purchases with backend for cross-device support
    }
}

data class Purchase(
    val id: String,
    val pluginId: String,
    val featureId: String?,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val userId: String
)
```



## Data Models

### Database Schema

**Location**: `data/src/commonMain/kotlin/ireader/data/database/`

```kotlin
// Plugin table
@Entity(tableName = "plugins")
data class PluginEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String,
    val versionCode: Int,
    val type: String, // THEME, TRANSLATION, TTS, FEATURE
    val author: String,
    val description: String,
    val iconUrl: String?,
    val status: String, // ENABLED, DISABLED, ERROR
    val installDate: Long,
    val lastUpdate: Long?,
    val manifestJson: String // Full manifest as JSON
)

// Purchase table
@Entity(tableName = "plugin_purchases")
data class PluginPurchaseEntity(
    @PrimaryKey val id: String,
    val pluginId: String,
    val featureId: String?,
    val amount: Double,
    val currency: String,
    val timestamp: Long,
    val userId: String,
    val receiptData: String?
)

// Plugin ratings and reviews
@Entity(tableName = "plugin_reviews")
data class PluginReviewEntity(
    @PrimaryKey val id: String,
    val pluginId: String,
    val userId: String,
    val rating: Float,
    val reviewText: String?,
    val timestamp: Long,
    val helpful: Int
)
```

### Preferences

```kotlin
class PluginPreferences(private val preferenceStore: PreferenceStore) {
    fun enabledPlugins(): Preference<Set<String>> {
        return preferenceStore.getStringSet("enabled_plugins", emptySet())
    }
    
    fun autoUpdatePlugins(): Preference<Boolean> {
        return preferenceStore.getBoolean("auto_update_plugins", true)
    }
    
    fun pluginUpdateCheckInterval(): Preference<Long> {
        return preferenceStore.getLong("plugin_update_interval", 86400000L) // 24 hours
    }
}
```



## UI Components

### 1. Plugin Marketplace Screen

**Location**: `presentation/src/commonMain/kotlin/ireader/presentation/ui/plugins/marketplace/`

```kotlin
@Composable
fun PluginMarketplaceScreen(
    viewModel: PluginMarketplaceViewModel
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Plugin Marketplace") },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            // Category tabs
            item {
                PluginCategoryTabs(
                    categories = PluginType.values().toList(),
                    selected = state.selectedCategory,
                    onSelect = { viewModel.selectCategory(it) }
                )
            }
            
            // Featured plugins
            item {
                FeaturedPluginsSection(plugins = state.featuredPlugins)
            }
            
            // Plugin grid
            items(state.plugins) { plugin ->
                PluginCard(
                    plugin = plugin,
                    onClick = { viewModel.openPluginDetails(plugin.id) }
                )
            }
        }
    }
}

@Composable
fun PluginCard(plugin: PluginInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Plugin icon
            AsyncImage(
                model = plugin.manifest.iconUrl,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(plugin.manifest.name, style = MaterialTheme.typography.titleMedium)
                Text(plugin.manifest.author.name, style = MaterialTheme.typography.bodySmall)
                Text(plugin.manifest.description, maxLines = 2)
                
                Row {
                    RatingBar(rating = plugin.rating ?: 0f)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${plugin.downloadCount} downloads")
                }
            }
            
            // Price or Install button
            if (plugin.manifest.monetization is PluginMonetization.Premium) {
                Text("$${(plugin.manifest.monetization as PluginMonetization.Premium).price}")
            } else {
                Text("Free", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
```



### 2. Plugin Details Screen

```kotlin
@Composable
fun PluginDetailsScreen(
    pluginId: String,
    viewModel: PluginDetailsViewModel
) {
    val state by viewModel.state.collectAsState()
    val plugin = state.plugin ?: return
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plugin.manifest.name) },
                navigationIcon = {
                    IconButton(onClick = { /* Navigate back */ }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            // Plugin header with icon and basic info
            item {
                PluginHeader(plugin)
            }
            
            // Screenshots
            item {
                PluginScreenshots(plugin.manifest.screenshotUrls)
            }
            
            // Description
            item {
                Text(
                    plugin.manifest.description,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // Developer info
            item {
                DeveloperInfoSection(plugin.manifest.author)
            }
            
            // Permissions
            item {
                PermissionsSection(plugin.manifest.permissions)
            }
            
            // Reviews
            item {
                ReviewsSection(
                    reviews = state.reviews,
                    onWriteReview = { viewModel.writeReview() }
                )
            }
            
            // Install/Purchase button
            item {
                InstallButton(
                    plugin = plugin,
                    onInstall = { viewModel.installPlugin() },
                    onPurchase = { viewModel.purchasePlugin() }
                )
            }
        }
    }
}
```

### 3. Plugin Management Screen

```kotlin
@Composable
fun PluginManagementScreen(
    viewModel: PluginManagementViewModel
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Installed Plugins") })
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(state.installedPlugins) { plugin ->
                InstalledPluginItem(
                    plugin = plugin,
                    onEnable = { viewModel.enablePlugin(plugin.id) },
                    onDisable = { viewModel.disablePlugin(plugin.id) },
                    onUninstall = { viewModel.uninstallPlugin(plugin.id) },
                    onConfigure = { viewModel.configurePlugin(plugin.id) }
                )
            }
        }
    }
}

@Composable
fun InstalledPluginItem(
    plugin: PluginInfo,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
    onUninstall: () -> Unit,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = plugin.manifest.iconUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(plugin.manifest.name, style = MaterialTheme.typography.titleMedium)
                Text("v${plugin.manifest.version}", style = MaterialTheme.typography.bodySmall)
                Text(
                    when (plugin.status) {
                        PluginStatus.ENABLED -> "Enabled"
                        PluginStatus.DISABLED -> "Disabled"
                        PluginStatus.ERROR -> "Error"
                        PluginStatus.UPDATING -> "Updating..."
                    },
                    color = when (plugin.status) {
                        PluginStatus.ENABLED -> Color.Green
                        PluginStatus.ERROR -> Color.Red
                        else -> Color.Gray
                    }
                )
            }
            
            // Actions
            IconButton(onClick = onConfigure) {
                Icon(Icons.Default.Settings, "Configure")
            }
            
            Switch(
                checked = plugin.status == PluginStatus.ENABLED,
                onCheckedChange = { if (it) onEnable() else onDisable() }
            )
            
            IconButton(onClick = onUninstall) {
                Icon(Icons.Default.Delete, "Uninstall")
            }
        }
    }
}
```



## Error Handling

### Plugin Loading Errors

```kotlin
sealed class PluginError {
    data class InvalidManifest(val reason: String) : PluginError()
    data class IncompatibleVersion(val required: String, val current: String) : PluginError()
    data class MissingPermissions(val permissions: List<PluginPermission>) : PluginError()
    data class LoadFailed(val exception: Throwable) : PluginError()
    data class InitializationFailed(val exception: Throwable) : PluginError()
    object PluginNotFound : PluginError()
}

fun PluginError.toUserMessage(): String = when (this) {
    is InvalidManifest -> "Plugin manifest is invalid: $reason"
    is IncompatibleVersion -> "Plugin requires IReader $required or higher (current: $current)"
    is MissingPermissions -> "Plugin requires permissions: ${permissions.joinToString()}"
    is LoadFailed -> "Failed to load plugin: ${exception.message}"
    is InitializationFailed -> "Failed to initialize plugin: ${exception.message}"
    is PluginNotFound -> "Plugin not found"
}
```

### Payment Errors

```kotlin
sealed class PaymentError {
    object NetworkError : PaymentError()
    object PaymentCancelled : PaymentError()
    object PaymentFailed : PaymentError()
    object AlreadyPurchased : PaymentError()
    data class ServerError(val code: Int) : PaymentError()
}

fun PaymentError.toUserMessage(): String = when (this) {
    is NetworkError -> "Network error. Please check your connection."
    is PaymentCancelled -> "Payment cancelled"
    is PaymentFailed -> "Payment failed. Please try again."
    is AlreadyPurchased -> "You already own this plugin"
    is ServerError -> "Server error ($code). Please try again later."
}
```

## Security

### Plugin Sandboxing

```kotlin
class PluginSandbox(
    private val permissions: List<PluginPermission>
) {
    fun checkPermission(permission: PluginPermission): Boolean {
        return permissions.contains(permission)
    }
    
    fun restrictFileAccess(path: String): Boolean {
        // Only allow access to plugin's own directory
        return path.startsWith(getPluginDataDir())
    }
    
    fun restrictNetworkAccess(url: String): Boolean {
        // Check if plugin has network permission
        return checkPermission(PluginPermission.NETWORK)
    }
}

enum class PluginPermission {
    NETWORK,           // Access to network
    STORAGE,           // Access to local storage
    READER_CONTEXT,    // Access to current reading context
    LIBRARY_ACCESS,    // Access to user's library
    PREFERENCES,       // Access to app preferences
    NOTIFICATIONS      // Show notifications
}
```

### Plugin Validation

```kotlin
class PluginValidator {
    fun validate(manifest: PluginManifest): Result<Unit> {
        return try {
            validateVersion(manifest.version)
            validatePermissions(manifest.permissions)
            validateMonetization(manifest.monetization)
            Result.success(Unit)
        } catch (e: ValidationException) {
            Result.failure(e)
        }
    }
    
    private fun validateVersion(version: String) {
        // Ensure version follows semver
    }
    
    private fun validatePermissions(permissions: List<PluginPermission>) {
        // Ensure permissions are valid
    }
    
    private fun validateMonetization(monetization: PluginMonetization?) {
        // Validate pricing information
    }
}
```



## Integration Points

### 1. Theme Plugin Integration

```kotlin
// In ThemeViewModel or theme management
class ThemeManager(
    private val pluginManager: PluginManager
) {
    fun getAvailableThemes(): List<ThemeOption> {
        val builtInThemes = getBuiltInThemes()
        val pluginThemes = pluginManager.getPluginsByType(PluginType.THEME)
            .filterIsInstance<ThemePlugin>()
            .map { ThemeOption.Plugin(it) }
        
        return builtInThemes + pluginThemes
    }
    
    fun applyTheme(theme: ThemeOption) {
        when (theme) {
            is ThemeOption.BuiltIn -> applyBuiltInTheme(theme)
            is ThemeOption.Plugin -> applyPluginTheme(theme.plugin)
        }
    }
    
    private fun applyPluginTheme(plugin: ThemePlugin) {
        val colorScheme = plugin.getColorScheme(isDark = true)
        val extraColors = plugin.getExtraColors(isDark = true)
        // Apply to app
    }
}
```

### 2. Translation Plugin Integration

```kotlin
// Extend existing TranslationEnginesManager
class TranslationEnginesManager(
    private val pluginManager: PluginManager,
    // ... existing dependencies
) {
    fun getAvailableEngines(): List<TranslationEngine> {
        val builtInEngines = getBuiltInEngines()
        val pluginEngines = pluginManager.getPluginsByType(PluginType.TRANSLATION)
            .filterIsInstance<TranslationPlugin>()
            .map { TranslationEngine.Plugin(it) }
        
        return builtInEngines + pluginEngines
    }
    
    suspend fun translate(text: String, from: String, to: String, engine: TranslationEngine): Result<String> {
        return when (engine) {
            is TranslationEngine.BuiltIn -> translateWithBuiltIn(text, from, to, engine)
            is TranslationEngine.Plugin -> engine.plugin.translate(text, from, to)
        }
    }
}
```

### 3. TTS Plugin Integration

```kotlin
// Extend existing TTS service
class TTSService(
    private val pluginManager: PluginManager,
    // ... existing dependencies
) {
    fun getAvailableVoices(): List<VoiceModel> {
        val builtInVoices = VoiceCatalog.getAllVoices()
        val pluginVoices = pluginManager.getPluginsByType(PluginType.TTS)
            .filterIsInstance<TTSPlugin>()
            .flatMap { it.getAvailableVoices() }
        
        return builtInVoices + pluginVoices
    }
    
    suspend fun speak(text: String, voice: VoiceModel): Result<Unit> {
        val plugin = findPluginForVoice(voice)
        return if (plugin != null) {
            plugin.speak(text, VoiceConfig(voice.id))
                .map { /* Handle audio stream */ }
        } else {
            speakWithBuiltIn(text, voice)
        }
    }
}
```

### 4. Feature Plugin Integration

```kotlin
// In main navigation or reader screen
class FeaturePluginIntegration(
    private val pluginManager: PluginManager
) {
    fun getPluginMenuItems(): List<PluginMenuItem> {
        return pluginManager.getPluginsByType(PluginType.FEATURE)
            .filterIsInstance<FeaturePlugin>()
            .flatMap { it.getMenuItems() }
    }
    
    fun getPluginScreens(): List<PluginScreen> {
        return pluginManager.getPluginsByType(PluginType.FEATURE)
            .filterIsInstance<FeaturePlugin>()
            .flatMap { it.getScreens() }
    }
    
    fun handleReaderContext(context: ReaderContext) {
        pluginManager.getPluginsByType(PluginType.FEATURE)
            .filterIsInstance<FeaturePlugin>()
            .forEach { plugin ->
                plugin.onReaderContext(context)?.let { action ->
                    // Execute plugin action
                }
            }
    }
}
```



## Testing Strategy

### Unit Tests
- Plugin manifest parsing and validation
- Plugin loader functionality
- Plugin registry operations
- Monetization service payment processing
- Permission checking and sandboxing
- Error handling and error message generation

### Integration Tests
- Plugin installation and uninstallation flow
- Plugin enable/disable functionality
- Theme plugin application
- Translation plugin integration with existing engines
- TTS plugin integration with voice catalog
- Feature plugin menu and screen registration
- Purchase flow and purchase verification
- Cross-device purchase synchronization

### UI Tests
- Plugin marketplace browsing and search
- Plugin details screen display
- Plugin installation UI flow
- Plugin management screen operations
- Payment dialog and confirmation
- Plugin configuration screens

### Platform-Specific Tests
- Desktop: Plugin file watching and hot-reload
- Android: APK-based plugin loading
- iOS: Framework-based plugin loading

## Performance Considerations

### Plugin Loading
- Load plugins asynchronously on app startup
- Use lazy loading for plugin resources
- Cache plugin manifests to avoid repeated parsing
- Implement plugin preloading for frequently used plugins

### Memory Management
- Unload disabled plugins from memory
- Implement plugin resource cleanup on disable
- Use weak references for plugin callbacks
- Monitor plugin memory usage and enforce limits

### Network Optimization
- Cache plugin marketplace data
- Implement incremental plugin updates
- Use CDN for plugin distribution
- Compress plugin packages

### UI Performance
- Use LazyColumn for plugin lists
- Implement proper keys for list items
- Cache plugin icons and screenshots
- Use placeholder images during loading

