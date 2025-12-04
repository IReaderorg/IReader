package ireader.js.runtime

import ireader.core.source.Dependencies
import ireader.core.source.Source
import ireader.core.source.SourceFactory
import kotlin.js.JsExport

/**
 * Factory function type for creating sources.
 */
typealias SourceCreator = (JsDependencies) -> Source

/**
 * Registry for source factories.
 * Sources register themselves here when their JS module is loaded.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
object SourceRegistry {
    
    private val factories = mutableMapOf<String, SourceCreator>()
    private val deps = JsDependencies()
    
    /**
     * Register a source factory.
     * Called by individual source modules when they're loaded.
     */
    fun register(sourceId: String, factory: SourceCreator) {
        factories[sourceId] = factory
        console.log("SourceRegistry: Registered factory for '$sourceId'")
    }
    
    /**
     * Initialize a source and register it with the bridge.
     */
    fun initSource(sourceId: String): Boolean {
        val factory = factories[sourceId] ?: run {
            console.error("SourceRegistry: No factory found for '$sourceId'")
            return false
        }
        
        return try {
            val source = factory(deps)
            SourceBridge.registerSource(sourceId, source)
            true
        } catch (e: Exception) {
            console.error("SourceRegistry: Failed to init source '$sourceId' - ${e.message}")
            false
        }
    }
    
    /**
     * Initialize all registered sources.
     */
    fun initAllSources(): Int {
        var count = 0
        factories.keys.forEach { sourceId ->
            if (initSource(sourceId)) count++
        }
        return count
    }
    
    /**
     * Get list of available source IDs (registered but not necessarily initialized).
     */
    fun getAvailableSourceIds(): Array<String> {
        return factories.keys.toTypedArray()
    }
    
    /**
     * Check if a source factory is registered.
     */
    fun hasFactory(sourceId: String): Boolean {
        return factories.containsKey(sourceId)
    }
}

/**
 * Helper function for sources to register themselves.
 * 
 * Usage in source module:
 * ```kotlin
 * @JsExport
 * fun initNovelUpdates() {
 *     registerSource("novelupdates") { deps ->
 *         NovelUpdatesSource(deps)
 *     }
 * }
 * ```
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
fun registerSource(sourceId: String, factory: (JsDependencies) -> Source) {
    SourceRegistry.register(sourceId, factory)
}

/**
 * Initialize the runtime.
 * Should be called once when the JS context is created.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
fun initRuntime() {
    console.log("IReader Source Runtime initialized")
    console.log("Available APIs: SourceBridge, SourceRegistry, registerSource")
}

/**
 * Get the SourceBridge instance.
 * Convenience function for iOS interop.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
fun getSourceBridge(): SourceBridge = SourceBridge

/**
 * Get the SourceRegistry instance.
 * Convenience function for iOS interop.
 */
@JsExport
@OptIn(ExperimentalJsExport::class)
fun getSourceRegistry(): SourceRegistry = SourceRegistry
