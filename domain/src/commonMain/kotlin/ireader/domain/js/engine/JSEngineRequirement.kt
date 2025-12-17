package ireader.domain.js.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Service for managing JS engine requirements and prompts.
 * 
 * When a user tries to use a JS-based source without a JS engine installed,
 * this service can be used to show a prompt to install one.
 */
class JSEngineRequirement(
    private val jsEngineProvider: JSEngineProvider
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
        // Check if bundled engine is available (always true for now)
        if (jsEngineProvider.isEngineAvailable()) {
            _requirementState.value = JSEngineRequirementState.NotRequired
            return true
        }
        
        // Show prompt to install JS engine
        _requirementState.value = JSEngineRequirementState.Required(
            sourceId = sourceId,
            sourceName = sourceName,
            message = "This source requires a JavaScript engine to work. Please install a JS engine plugin from the Feature Store."
        )
        return false
    }
    
    /**
     * Dismiss the current requirement prompt.
     */
    fun dismissPrompt() {
        _requirementState.value = JSEngineRequirementState.NotRequired
    }
    
    /**
     * Get recommended JS engine plugin for the current platform.
     */
    fun getRecommendedEngine(): RecommendedJSEngine {
        return RecommendedJSEngine(
            pluginId = "io.github.ireaderorg.plugins.graalvm-engine",
            name = "GraalVM JavaScript Engine",
            description = "High-performance JavaScript engine for Desktop",
            size = "~50MB"
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
