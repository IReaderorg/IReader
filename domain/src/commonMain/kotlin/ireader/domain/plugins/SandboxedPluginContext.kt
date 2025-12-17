package ireader.domain.plugins

import ireader.domain.plugins.providers.CharacterServiceProviderImpl
import ireader.domain.plugins.providers.GlossaryServiceProviderImpl
import ireader.domain.plugins.providers.PluginHttpClientProviderImpl
import ireader.domain.plugins.providers.SyncServiceProviderImpl
import ireader.plugin.api.AppVersionInfo
import ireader.plugin.api.CharacterServiceProvider
import ireader.plugin.api.GlossaryServiceProvider
import ireader.plugin.api.LibraryServiceProvider
import ireader.plugin.api.LogLevel
import ireader.plugin.api.Platform
import ireader.plugin.api.PluginHttpClientProvider
import ireader.plugin.api.ReaderContextProvider
import ireader.plugin.api.SyncServiceProvider
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking

/**
 * Sandboxed implementation of PluginContext
 * Provides secure, restricted access to app resources based on permissions
 * Requirements: 10.1, 10.2, 10.3, 10.4
 */
class SandboxedPluginContext(
    override val pluginId: String,
    override val permissions: List<PluginPermission>,
    private val sandbox: PluginSandbox,
    private val preferencesStore: PluginPreferencesStore,
    private val httpClient: HttpClient? = null,
    private val libraryServiceProvider: LibraryServiceProvider? = null,
    private val readerContextProvider: ReaderContextProvider? = null,
    private val notificationHandler: ((String, String, String) -> Unit)? = null,
    private val appVersionProvider: (() -> AppVersionInfo)? = null,
    private val platformProvider: (() -> Platform)? = null
) : PluginContext {
    
    // Lazy-initialized service providers
    private val httpClientProvider: PluginHttpClientProvider? by lazy {
        httpClient?.let { PluginHttpClientProviderImpl(it) }
    }
    
    private val glossaryServiceProvider: GlossaryServiceProvider by lazy {
        GlossaryServiceProviderImpl()
    }
    
    private val characterServiceProvider: CharacterServiceProvider by lazy {
        CharacterServiceProviderImpl()
    }
    
    private val syncServiceProvider: SyncServiceProvider by lazy {
        SyncServiceProviderImpl()
    }
    
    /**
     * Get the plugin's data directory for storing files
     * Requirements: 10.4
     * 
     * Note: This is cached on first access to avoid blocking calls.
     * The sandbox should initialize the data directory eagerly.
     */
    private val cachedDataDir: String by lazy {
        runBlocking {
            sandbox.getPluginDataDir(pluginId).path
        }
    }
    
    private val cachedCacheDir: String by lazy {
        runBlocking {
            sandbox.getPluginCacheDir(pluginId).path
        }
    }
    
    override fun getDataDir(): String {
        return cachedDataDir
    }
    
    override fun getCacheDir(): String {
        return cachedCacheDir
    }
    
    /**
     * Check if plugin has a specific permission
     * Requirements: 10.1, 10.2
     */
    override fun hasPermission(permission: PluginPermission): Boolean {
        return sandbox.checkPermission(permission)
    }
    
    /**
     * Get plugin-specific preferences storage
     * Requirements: 10.2
     */
    override fun getPreferences(): PluginPreferencesStore {
        // Verify preferences permission
        if (!hasPermission(PluginPermission.PREFERENCES)) {
            return RestrictedPluginPreferencesStore()
        }
        
        return preferencesStore
    }
    
    override fun getHttpClient(): PluginHttpClientProvider? {
        if (!hasPermission(PluginPermission.NETWORK)) return null
        return httpClientProvider
    }
    
    override fun getGlossaryService(): GlossaryServiceProvider? {
        if (!hasPermission(PluginPermission.GLOSSARY_ACCESS)) return null
        return glossaryServiceProvider
    }
    
    override fun getCharacterService(): CharacterServiceProvider? {
        if (!hasPermission(PluginPermission.CHARACTER_DATABASE)) return null
        return characterServiceProvider
    }
    
    override fun getSyncService(): SyncServiceProvider? {
        if (!hasPermission(PluginPermission.SYNC_DATA)) return null
        return syncServiceProvider
    }
    
    override fun getLibraryService(): LibraryServiceProvider? {
        if (!hasPermission(PluginPermission.LIBRARY_ACCESS)) return null
        return libraryServiceProvider
    }
    
    override fun getReaderContext(): ReaderContextProvider? {
        if (!hasPermission(PluginPermission.READER_CONTEXT)) return null
        return readerContextProvider
    }
    
    override fun showNotification(title: String, message: String, channelId: String) {
        if (!hasPermission(PluginPermission.NOTIFICATIONS)) return
        notificationHandler?.invoke(title, message, channelId)
    }
    
    override fun log(level: LogLevel, message: String, throwable: Throwable?) {
        // Always allow logging
        val prefix = "[$pluginId]"
        val logMessage = if (throwable != null) {
            "$prefix $message - ${throwable.message}"
        } else {
            "$prefix $message"
        }
        when (level) {
            LogLevel.DEBUG -> println("DEBUG $logMessage")
            LogLevel.INFO -> println("INFO $logMessage")
            LogLevel.WARN -> println("WARN $logMessage")
            LogLevel.ERROR -> println("ERROR $logMessage")
        }
    }
    
    override fun getAppVersion(): AppVersionInfo {
        return appVersionProvider?.invoke() ?: AppVersionInfo(
            versionName = "2.0.0",
            versionCode = 1,
            buildType = "release"
        )
    }
    
    override fun getPlatform(): Platform {
        return platformProvider?.invoke() ?: Platform.DESKTOP
    }
    
    /**
     * Validate file access before operation
     * Requirements: 10.3, 10.4
     */
    suspend fun validateFileAccess(path: String): Result<Unit> {
        return sandbox.validateFileOperation(path, FileOperation.READ)
    }
    
    /**
     * Validate network access before operation
     * Requirements: 10.3
     */
    fun validateNetworkAccess(url: String): Result<Unit> {
        return sandbox.validateNetworkOperation(url)
    }
    
    /**
     * Get current resource usage
     * Requirements: 11.1, 11.2
     */
    fun getResourceUsage(): PluginResourceUsage {
        return sandbox.getResourceUsage()
    }
    
    /**
     * Check if plugin has exceeded resource limits
     * Requirements: 11.3, 11.4
     */
    fun hasExceededResourceLimits(): Boolean {
        return sandbox.hasExceededResourceLimits()
    }
}

/**
 * Restricted preferences store that denies all operations
 * Used when plugin doesn't have preferences permission
 */
class RestrictedPluginPreferencesStore : PluginPreferencesStore {
    private fun throwPermissionError(): Nothing {
        throw IllegalStateException("Plugin does not have PREFERENCES permission")
    }
    
    override fun getString(key: String, defaultValue: String): String = defaultValue
    override fun putString(key: String, value: String) = throwPermissionError()
    override fun getInt(key: String, defaultValue: Int): Int = defaultValue
    override fun putInt(key: String, value: Int) = throwPermissionError()
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean = defaultValue
    override fun putBoolean(key: String, value: Boolean) = throwPermissionError()
    override fun getLong(key: String, defaultValue: Long): Long = defaultValue
    override fun putLong(key: String, value: Long) = throwPermissionError()
    override fun getFloat(key: String, defaultValue: Float): Float = defaultValue
    override fun putFloat(key: String, value: Float) = throwPermissionError()
    override fun getStringSet(key: String, defaultValue: Set<String>): Set<String> = defaultValue
    override fun putStringSet(key: String, value: Set<String>) = throwPermissionError()
    override fun remove(key: String) = throwPermissionError()
    override fun clear() = throwPermissionError()
    override fun contains(key: String): Boolean = false
    override fun getAllKeys(): Set<String> = emptySet()
}

/**
 * Factory for creating sandboxed plugin contexts
 */
class PluginContextFactory(
    private val permissionManager: PluginPermissionManager,
    private val fileSystem: ireader.core.io.FileSystem,
    private val httpClient: HttpClient? = null,
    private val libraryServiceProvider: LibraryServiceProvider? = null,
    private val notificationHandler: ((String, String, String) -> Unit)? = null,
    private val appVersionProvider: (() -> AppVersionInfo)? = null,
    private val platformProvider: (() -> Platform)? = null
) {
    /**
     * Create a sandboxed context for a plugin
     * Requirements: 10.1, 10.2, 10.3, 10.4
     */
    fun createContext(
        pluginId: String,
        manifest: PluginManifest,
        preferencesStore: PluginPreferencesStore,
        readerContextProvider: ReaderContextProvider? = null
    ): SandboxedPluginContext {
        val sandbox = PluginSandbox(
            pluginId = pluginId,
            manifest = manifest,
            permissionManager = permissionManager,
            fileSystem = fileSystem
        )
        
        return SandboxedPluginContext(
            pluginId = pluginId,
            permissions = manifest.permissions,
            sandbox = sandbox,
            preferencesStore = preferencesStore,
            httpClient = httpClient,
            libraryServiceProvider = libraryServiceProvider,
            readerContextProvider = readerContextProvider,
            notificationHandler = notificationHandler,
            appVersionProvider = appVersionProvider,
            platformProvider = platformProvider
        )
    }
}
