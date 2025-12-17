package ireader.domain.plugins

import ireader.plugin.api.AppVersionInfo
import ireader.plugin.api.CharacterServiceProvider
import ireader.plugin.api.GlossaryServiceProvider
import ireader.plugin.api.LibraryServiceProvider
import ireader.plugin.api.LogLevel
import ireader.plugin.api.Platform
import ireader.plugin.api.PluginHttpClientProvider
import ireader.plugin.api.ReaderContextProvider
import ireader.plugin.api.SyncServiceProvider
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
    private val preferencesStore: PluginPreferencesStore
) : PluginContext {
    
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
        // TODO: Implement HTTP client provider
        return null
    }
    
    override fun getGlossaryService(): GlossaryServiceProvider? {
        if (!hasPermission(PluginPermission.GLOSSARY_ACCESS)) return null
        // TODO: Implement glossary service provider
        return null
    }
    
    override fun getCharacterService(): CharacterServiceProvider? {
        if (!hasPermission(PluginPermission.CHARACTER_DATABASE)) return null
        // TODO: Implement character service provider
        return null
    }
    
    override fun getSyncService(): SyncServiceProvider? {
        if (!hasPermission(PluginPermission.SYNC_DATA)) return null
        // TODO: Implement sync service provider
        return null
    }
    
    override fun getLibraryService(): LibraryServiceProvider? {
        if (!hasPermission(PluginPermission.LIBRARY_ACCESS)) return null
        // TODO: Implement library service provider
        return null
    }
    
    override fun getReaderContext(): ReaderContextProvider? {
        if (!hasPermission(PluginPermission.READER_CONTEXT)) return null
        // TODO: Implement reader context provider
        return null
    }
    
    override fun showNotification(title: String, message: String, channelId: String) {
        if (!hasPermission(PluginPermission.NOTIFICATIONS)) return
        // TODO: Implement notification showing
    }
    
    override fun log(level: LogLevel, message: String, throwable: Throwable?) {
        // Always allow logging
        val prefix = "[$pluginId]"
        when (level) {
            LogLevel.DEBUG -> println("DEBUG $prefix $message")
            LogLevel.INFO -> println("INFO $prefix $message")
            LogLevel.WARN -> println("WARN $prefix $message")
            LogLevel.ERROR -> println("ERROR $prefix $message ${throwable?.message ?: ""}")
        }
    }
    
    override fun getAppVersion(): AppVersionInfo {
        // TODO: Get actual app version
        return AppVersionInfo(
            versionName = "2.0.0",
            versionCode = 1,
            buildType = "release"
        )
    }
    
    override fun getPlatform(): Platform {
        // TODO: Detect actual platform
        return Platform.ANDROID
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
    private val fileSystem: ireader.core.io.FileSystem
) {
    /**
     * Create a sandboxed context for a plugin
     * Requirements: 10.1, 10.2, 10.3, 10.4
     */
    fun createContext(
        pluginId: String,
        manifest: PluginManifest,
        preferencesStore: PluginPreferencesStore
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
            preferencesStore = preferencesStore
        )
    }
}
