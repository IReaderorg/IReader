package ireader.domain.services.source

import ireader.core.log.Log
import ireader.core.source.CatalogSource
import ireader.core.source.Source
import ireader.core.util.createICoroutineScope
import ireader.domain.catalogs.CatalogStore
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.JSPluginCatalog
import ireader.domain.models.entities.SourceStatus
import ireader.domain.models.entities.SourceUnavailableInfo
import ireader.domain.preferences.prefs.SourcePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Service for checking and tracking the health status of sources.
 * Monitors source availability and provides status information for UI display.
 */
class SourceHealthChecker(
    private val catalogStore: CatalogStore,
    private val sourcePreferences: SourcePreferences
) {
    private val scope = createICoroutineScope()
    
    // Map of source ID to unavailable info
    private val _unavailableSources = MutableStateFlow<Map<Long, SourceUnavailableInfo>>(emptyMap())
    val unavailableSources: StateFlow<Map<Long, SourceUnavailableInfo>> = _unavailableSources.asStateFlow()
    
    // Map of source ID to last error
    private val _sourceErrors = MutableStateFlow<Map<Long, String>>(emptyMap())
    val sourceErrors: StateFlow<Map<Long, String>> = _sourceErrors.asStateFlow()
    
    init {
        // Load persisted errors
        scope.launch {
            _sourceErrors.value = sourcePreferences.getSourceErrorsMap()
        }
    }
    
    /**
     * Check the health status of a source.
     * Returns the status and any unavailability info.
     */
    fun checkSourceHealth(catalog: CatalogLocal): Pair<SourceStatus, SourceUnavailableInfo?> {
        return try {
            val source = catalog.source
            
            // Check if source is null (failed to load)
            if (source == null) {
                val info = SourceUnavailableInfo.loadFailed("Source failed to initialize")
                markSourceUnavailable(catalog.sourceId, info)
                return SourceStatus.LoadFailed("Source failed to initialize") to info
            }
            
            // Check if it's a stub source (requires JS engine)
            if (catalog is JSPluginCatalog && 
                catalog.source is ireader.domain.js.loader.JSPluginStubSource) {
                val info = SourceUnavailableInfo.requiresPlugin(
                    "io.github.ireaderorg.plugins.j2v8-engine",
                    "JavaScript Engine"
                )
                markSourceUnavailable(catalog.sourceId, info)
                return SourceStatus.RequiresPlugin("io.github.ireaderorg.plugins.j2v8-engine", "JavaScript Engine") to info
            }
            
            // Check if it's a pending source
            if (catalog is JSPluginCatalog && 
                catalog.source is ireader.domain.js.loader.JSPluginPendingSource) {
                val info = SourceUnavailableInfo.requiresPlugin(
                    "io.github.ireaderorg.plugins.j2v8-engine",
                    "JavaScript Engine"
                )
                markSourceUnavailable(catalog.sourceId, info)
                return SourceStatus.RequiresPlugin("io.github.ireaderorg.plugins.j2v8-engine", "JavaScript Engine") to info
            }
            
            // Source appears healthy
            markSourceAvailable(catalog.sourceId)
            SourceStatus.Working to null
            
        } catch (e: Exception) {
            Log.error("SourceHealthChecker: Error checking source health", e)
            val info = SourceUnavailableInfo.loadFailed(e.message ?: "Unknown error")
            markSourceUnavailable(catalog.sourceId, info)
            SourceStatus.LoadFailed(e.message ?: "Unknown error") to info
        }
    }
    
    /**
     * Record an error that occurred while using a source.
     */
    fun recordSourceError(sourceId: Long, error: Throwable) {
        val errorMessage = error.message ?: "Unknown error"
        
        scope.launch {
            // Update in-memory state
            val current = _sourceErrors.value.toMutableMap()
            current[sourceId] = errorMessage
            _sourceErrors.value = current
            
            // Persist error
            sourcePreferences.setSourceError(sourceId, errorMessage)
            
            // Determine if this is a critical error that makes the source unavailable
            val isCritical = isCriticalError(error)
            if (isCritical) {
                val info = SourceUnavailableInfo.loadFailed(errorMessage)
                markSourceUnavailable(sourceId, info)
            }
        }
    }
    
    /**
     * Clear error for a source (e.g., after successful operation).
     */
    fun clearSourceError(sourceId: Long) {
        scope.launch {
            val current = _sourceErrors.value.toMutableMap()
            current.remove(sourceId)
            _sourceErrors.value = current
            
            sourcePreferences.clearSourceError(sourceId)
        }
    }
    
    /**
     * Mark a source as unavailable.
     */
    fun markSourceUnavailable(sourceId: Long, info: SourceUnavailableInfo) {
        val current = _unavailableSources.value.toMutableMap()
        current[sourceId] = info
        _unavailableSources.value = current
    }
    
    /**
     * Mark a source as available (remove from unavailable list).
     */
    fun markSourceAvailable(sourceId: Long) {
        val current = _unavailableSources.value.toMutableMap()
        current.remove(sourceId)
        _unavailableSources.value = current
    }
    
    /**
     * Get unavailability info for a source.
     */
    fun getUnavailableInfo(sourceId: Long): SourceUnavailableInfo? {
        return _unavailableSources.value[sourceId]
    }
    
    /**
     * Check if a source is unavailable.
     */
    fun isSourceUnavailable(sourceId: Long): Boolean {
        return sourceId in _unavailableSources.value
    }
    
    /**
     * Get the last error for a source.
     */
    fun getLastError(sourceId: Long): String? {
        return _sourceErrors.value[sourceId]
    }
    
    /**
     * Check if user has skipped the warning for this source.
     */
    fun isWarningSkipped(sourceId: Long): Boolean {
        return sourcePreferences.isSourceSkipped(sourceId)
    }
    
    /**
     * Skip the warning for a source.
     */
    fun skipWarning(sourceId: Long) {
        sourcePreferences.skipUnavailableSource(sourceId)
    }
    
    /**
     * Unskip the warning for a source.
     */
    fun unskipWarning(sourceId: Long) {
        sourcePreferences.unskipUnavailableSource(sourceId)
    }
    
    /**
     * Determine if an error is critical (makes source unusable).
     * Note: JVM-specific exception types are checked by class name for multiplatform compatibility.
     */
    private fun isCriticalError(error: Throwable): Boolean {
        val message = error.message?.lowercase() ?: ""
        val errorClassName = error::class.simpleName ?: ""
        return when {
            // Check JVM-specific exceptions by class name (not available in Kotlin/Native)
            errorClassName == "ClassNotFoundException" -> true
            errorClassName == "NoClassDefFoundError" -> true
            errorClassName == "LinkageError" -> true
            errorClassName == "UnsatisfiedLinkError" -> true
            message.contains("class not found") -> true
            message.contains("no class def found") -> true
            message.contains("unsatisfied link") -> true
            message.contains("native library") -> true
            message.contains("plugin not loaded") -> true
            message.contains("engine not available") -> true
            else -> false
        }
    }
    
    /**
     * Scan all catalogs and check their health.
     */
    fun scanAllSources() {
        scope.launch {
            catalogStore.getCatalogsFlow().collect { catalogs ->
                catalogs.forEach { catalog ->
                    checkSourceHealth(catalog)
                }
            }
        }
    }
}
