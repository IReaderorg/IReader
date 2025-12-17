package ireader.domain.js.engine

import ireader.domain.plugins.PluginManager
import ireader.plugin.api.JSEngineCapabilities
import ireader.plugin.api.JSEngineInstance
import ireader.plugin.api.JSEnginePlugin
import ireader.plugin.api.PluginType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Provider for JavaScript engines.
 * 
 * This service manages JS engine plugins and provides engine instances
 * for executing JavaScript code (e.g., LNReader source plugins).
 * 
 * The provider supports:
 * - Plugin-based engines (J2V8, GraalVM, QuickJS)
 * - Fallback to built-in engine (if available)
 * - Engine pooling for performance
 * 
 * Architecture:
 * - JSEnginePlugin: Low-level JS execution (from plugin-api)
 * - JSEngineProvider: Manages plugin-based engines
 * - GraalVMJSEngine/AndroidJSEngine: High-level LNReader plugin loaders
 *   (these use either bundled engines or plugin-based engines via this provider)
 */
class JSEngineProvider(
    private val pluginManager: PluginManager
) {
    private val mutex = Mutex()
    
    // Currently active JS engine plugin
    private var activePlugin: JSEnginePlugin? = null
    
    // Engine instance pool (keyed by source plugin ID for isolation)
    private val enginePool = mutableMapOf<String, MutableList<JSEngineInstance>>()
    
    // State flow for UI to observe
    private val _engineState = MutableStateFlow(JSEngineState())
    val engineState: StateFlow<JSEngineState> = _engineState.asStateFlow()
    
    // Whether to use plugin-based engines (false = use bundled engines)
    private var usePluginEngines = false
    
    /**
     * Check if a JS engine is available (either plugin-based or bundled).
     * This always returns true since bundled engines are available as fallback.
     */
    fun isEngineAvailable(): Boolean {
        return true // Bundled engines are always available as fallback
    }
    
    /**
     * Check if plugin-based JS engines are available.
     */
    fun isPluginEngineAvailable(): Boolean {
        return getInstalledEngines().isNotEmpty()
    }
    
    /**
     * Get all installed JS engine plugins.
     */
    fun getInstalledEngines(): List<JSEnginePlugin> {
        return pluginManager.getPluginsByType(PluginType.JS_ENGINE)
            .filterIsInstance<JSEnginePlugin>()
            .filter { it.isAvailable() }
    }
    
    /**
     * Get the currently active JS engine plugin.
     * Returns null if using bundled engines.
     */
    fun getActiveEngine(): JSEnginePlugin? {
        if (!usePluginEngines) return null
        return activePlugin ?: getInstalledEngines().firstOrNull()
    }
    
    /**
     * Set whether to use plugin-based engines.
     * When false, bundled engines (J2V8/GraalVM) are used directly.
     */
    suspend fun setUsePluginEngines(use: Boolean) = mutex.withLock {
        if (usePluginEngines != use) {
            clearEnginePoolInternal()
            usePluginEngines = use
            updateState()
        }
    }
    
    /**
     * Check if plugin-based engines are being used.
     */
    fun isUsingPluginEngines(): Boolean = usePluginEngines
    
    /**
     * Set the active JS engine plugin.
     */
    suspend fun setActiveEngine(pluginId: String): Result<Unit> = mutex.withLock {
        val plugin = getInstalledEngines().find { it.manifest.id == pluginId }
            ?: return Result.failure(IllegalArgumentException("JS engine plugin not found: $pluginId"))
        
        // Clear existing pool if switching engines
        if (activePlugin?.manifest?.id != pluginId) {
            clearEnginePoolInternal()
        }
        
        activePlugin = plugin
        usePluginEngines = true
        updateState()
        
        Result.success(Unit)
    }
    
    /**
     * Get or create an engine instance for a specific source plugin.
     * 
     * @param sourcePluginId The ID of the source plugin that needs the engine
     * @return An engine instance, or null if no plugin engine is available
     */
    suspend fun getEngine(sourcePluginId: String): JSEngineInstance? = mutex.withLock {
        val plugin = getActiveEngine() ?: return null
        
        // Try to get from pool
        val pool = enginePool.getOrPut(sourcePluginId) { mutableListOf() }
        val existingEngine = pool.find { it.isValid() }
        
        if (existingEngine != null) {
            return existingEngine
        }
        
        // Create new engine
        return try {
            val engine = plugin.createEngine()
            engine.initialize()
            pool.add(engine)
            engine
        } catch (e: Exception) {
            println("[JSEngineProvider] Failed to create engine: ${e.message}")
            null
        }
    }
    
    /**
     * Release an engine instance back to the pool.
     */
    suspend fun releaseEngine(sourcePluginId: String, engine: JSEngineInstance) = mutex.withLock {
        // Engine stays in pool for reuse
        // Could implement LRU eviction here if needed
    }
    
    /**
     * Dispose of an engine instance.
     */
    suspend fun disposeEngine(sourcePluginId: String, engine: JSEngineInstance) = mutex.withLock {
        val pool = enginePool[sourcePluginId] ?: return
        pool.remove(engine)
        engine.dispose()
    }
    
    /**
     * Clear all engine instances from the pool.
     */
    suspend fun clearEnginePool() = mutex.withLock {
        clearEnginePoolInternal()
    }
    
    private fun clearEnginePoolInternal() {
        enginePool.values.flatten().forEach { engine ->
            try {
                engine.dispose()
            } catch (e: Exception) {
                println("[JSEngineProvider] Error disposing engine: ${e.message}")
            }
        }
        enginePool.clear()
    }
    
    /**
     * Get capabilities of the active engine.
     */
    fun getCapabilities(): JSEngineCapabilities? {
        return getActiveEngine()?.getCapabilities()
    }
    
    /**
     * Update the state flow.
     */
    private fun updateState() {
        val engines = getInstalledEngines()
        val active = getActiveEngine()
        
        _engineState.value = JSEngineState(
            isAvailable = true, // Bundled engines always available
            isPluginEngineAvailable = engines.isNotEmpty(),
            usingPluginEngine = usePluginEngines && active != null,
            installedEngines = engines.map { 
                InstalledEngine(
                    pluginId = it.manifest.id,
                    name = it.manifest.name,
                    version = it.manifest.version,
                    capabilities = it.getCapabilities()
                )
            },
            activeEngineId = active?.manifest?.id,
            activeEngineName = active?.manifest?.name ?: getBundledEngineName()
        )
    }
    
    /**
     * Get the name of the bundled engine for the current platform.
     */
    private fun getBundledEngineName(): String {
        return "Bundled Engine" // Platform-specific name set by actual implementations
    }
    
    /**
     * Refresh the engine state (call after plugin installation/removal).
     */
    fun refresh() {
        updateState()
    }
}

/**
 * State of the JS engine provider.
 */
data class JSEngineState(
    /** Whether any JS engine is available (always true due to bundled engines) */
    val isAvailable: Boolean = true,
    /** Whether plugin-based engines are installed */
    val isPluginEngineAvailable: Boolean = false,
    /** Whether currently using a plugin-based engine */
    val usingPluginEngine: Boolean = false,
    /** List of installed plugin-based engines */
    val installedEngines: List<InstalledEngine> = emptyList(),
    /** ID of the active plugin engine (null if using bundled) */
    val activeEngineId: String? = null,
    /** Name of the active engine (bundled or plugin) */
    val activeEngineName: String? = null
)

/**
 * Information about an installed JS engine.
 */
data class InstalledEngine(
    val pluginId: String,
    val name: String,
    val version: String,
    val capabilities: JSEngineCapabilities
)

/**
 * Exception thrown when no JS engine is available.
 */
class NoJSEngineException(
    message: String = "No JavaScript engine is available."
) : Exception(message)
