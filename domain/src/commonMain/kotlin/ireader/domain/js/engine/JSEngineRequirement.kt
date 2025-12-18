package ireader.domain.js.engine

import ireader.domain.plugins.RequiredPluginChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Service for managing JS engine requirements and prompts.
 * 
 * When a user tries to use a JS-based source without a JS engine installed,
 * this service triggers the RequiredPluginChecker to show the installation dialog.
 */
class JSEngineRequirement(
    private val jsEngineProvider: JSEngineProvider,
    private val requiredPluginChecker: RequiredPluginChecker? = null
) {
    private val _requirementState = MutableStateFlow<JSEngineRequirementState>(
        JSEngineRequirementState.NotRequired
    )
    val requirementState: StateFlow<JSEngineRequirementState> = _requirementState.asStateFlow()
    
    /**
     * Check if JS engine is required for a source and show prompt if needed.
     * 
     * @param sourceId The source that requires JS engine
     * @param sourceName Display name of the source
     * @return true if JS engine is available, false if prompt was shown
     */
    fun checkAndPrompt(sourceId: String, sourceName: String): Boolean {
        if (jsEngineProvider.isEngineAvailable()) {
            _requirementState.value = JSEngineRequirementState.NotRequired
            return true
        }
        
        // Use RequiredPluginChecker to show the modern installation dialog
        requiredPluginChecker?.requestJSEngine()
        
        // Also update local state for backward compatibility
        _requirementState.value = JSEngineRequirementState.Required(
            sourceId = sourceId,
            sourceName = sourceName,
            message = "This source requires a JavaScript engine to work. Please install a JS engine plugin from the Feature Store."
        )
        return false
    }
    
    /**
     * Check if JS engine is available without showing a prompt.
     */
    fun isEngineAvailable(): Boolean {
        return jsEngineProvider.isEngineAvailable()
    }
    
    /**
     * Request JS engine installation (shows the RequiredPluginDialog).
     */
    fun requestEngine() {
        requiredPluginChecker?.requestJSEngine()
    }
    
    /**
     * Dismiss the current requirement prompt.
     */
    fun dismissPrompt() {
        _requirementState.value = JSEngineRequirementState.NotRequired
        requiredPluginChecker?.clearJSEngineRequest()
    }
    
    /**
     * Get recommended JS engine plugin for the current platform.
     */
    fun getRecommendedEngine(): RecommendedJSEngine {
        // On Android, recommend J2V8; on Desktop, recommend GraalVM
        return RecommendedJSEngine(
            pluginId = RequiredPluginChecker.JS_ENGINE_PLUGIN_ID,
            name = "J2V8 JavaScript Engine",
            description = "V8 JavaScript engine for Android",
            size = "~33MB"
        )
    }
}

/**
 * State of JS engine requirement.
 */
sealed class JSEngineRequirementState {
    /** No JS engine is required */
    object NotRequired : JSEngineRequirementState()
    
    /** JS engine is required but not installed */
    data class Required(
        val sourceId: String,
        val sourceName: String,
        val message: String
    ) : JSEngineRequirementState()
    
    /** User chose to install JS engine */
    data class Installing(
        val pluginId: String
    ) : JSEngineRequirementState()
}

/**
 * Recommended JS engine plugin info.
 */
data class RecommendedJSEngine(
    val pluginId: String,
    val name: String,
    val description: String,
    val size: String
)
