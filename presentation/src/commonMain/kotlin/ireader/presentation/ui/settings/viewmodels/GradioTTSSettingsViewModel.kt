package ireader.presentation.ui.settings.viewmodels

import androidx.compose.runtime.Stable
import ireader.core.log.Log
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.services.tts_service.GradioParam
import ireader.domain.services.tts_service.GradioParamType
import ireader.domain.services.tts_service.GradioTTSConfig
import ireader.domain.services.tts_service.GradioTTSManager
import ireader.domain.services.tts_service.GradioTTSPresets
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * State for Gradio TTS settings screen
 */
@Stable
data class GradioTTSSettingsState(
    val useGradioTTS: Boolean = false,
    val configs: List<GradioTTSConfig> = emptyList(),
    val activeConfigId: String? = null,
    val globalSpeed: Float = 1.0f,
    val isLoading: Boolean = false,
    val isTesting: Boolean = false,
    val testResult: TestResult? = null,
    val error: String? = null,
    
    // Edit dialog state
    val editingConfig: GradioTTSConfig? = null,
    val isEditDialogOpen: Boolean = false
)

sealed class TestResult {
    object Success : TestResult()
    data class Error(val message: String) : TestResult()
}

/**
 * ViewModel for managing Gradio TTS settings
 */
class GradioTTSSettingsViewModel(
    private val gradioTTSManager: GradioTTSManager,
    private val appPreferences: AppPreferences
) : StateViewModel<GradioTTSSettingsState>(GradioTTSSettingsState()) {
    
    companion object {
        private const val TAG = "GradioTTSSettingsVM"
    }
    
    init {
        loadSettings()
        observeConfigs()
    }
    
    private fun loadSettings() {
        scope.launch {
            val useGradioTTS = appPreferences.useGradioTTS().get()
            val globalSpeed = appPreferences.gradioTTSSpeed().get()
            val activeConfigId = appPreferences.activeGradioConfigId().get().ifEmpty { null }
            
            updateState { it.copy(
                useGradioTTS = useGradioTTS,
                globalSpeed = globalSpeed,
                activeConfigId = activeConfigId,
                configs = gradioTTSManager.getAllConfigs()
            ) }
        }
    }
    
    private fun observeConfigs() {
        scope.launch {
            gradioTTSManager.configs.collectLatest { configs ->
                updateState { it.copy(configs = configs) }
            }
        }
        
        scope.launch {
            gradioTTSManager.activeConfigId.collectLatest { activeId ->
                updateState { it.copy(activeConfigId = activeId) }
            }
        }
    }
    
    /**
     * Enable or disable Gradio TTS
     */
    fun setUseGradioTTS(enabled: Boolean) {
        scope.launch {
            appPreferences.useGradioTTS().set(enabled)
            updateState { it.copy(useGradioTTS = enabled) }
            Log.info { "$TAG: Gradio TTS ${if (enabled) "enabled" else "disabled"}" }
        }
    }
    
    /**
     * Set the active configuration
     */
    fun setActiveConfig(configId: String) {
        scope.launch {
            gradioTTSManager.setActiveConfig(configId)
            appPreferences.activeGradioConfigId().set(configId)
            updateState { it.copy(activeConfigId = configId) }
            Log.info { "$TAG: Active config set to $configId" }
        }
    }
    
    /**
     * Set global speech speed
     */
    fun setGlobalSpeed(speed: Float) {
        scope.launch {
            val clampedSpeed = speed.coerceIn(0.5f, 2.0f)
            appPreferences.gradioTTSSpeed().set(clampedSpeed)
            updateState { it.copy(globalSpeed = clampedSpeed) }
        }
    }
    
    /**
     * Enable or disable a specific configuration
     */
    fun setConfigEnabled(configId: String, enabled: Boolean) {
        gradioTTSManager.setConfigEnabled(configId, enabled)
    }
    
    /**
     * Open edit dialog for a configuration
     */
    fun openEditDialog(config: GradioTTSConfig? = null) {
        val editConfig = config ?: GradioTTSPresets.createCustomTemplate()
        updateState { it.copy(
            editingConfig = editConfig,
            isEditDialogOpen = true
        ) }
    }
    
    /**
     * Close edit dialog
     */
    fun closeEditDialog() {
        updateState { it.copy(
            editingConfig = null,
            isEditDialogOpen = false
        ) }
    }
    
    /**
     * Save the currently editing configuration
     */
    fun saveEditingConfig(config: GradioTTSConfig) {
        scope.launch {
            val existingConfig = gradioTTSManager.getConfigById(config.id)
            
            if (existingConfig != null) {
                gradioTTSManager.updateConfig(config)
            } else {
                gradioTTSManager.addCustomConfig(config)
            }
            
            closeEditDialog()
            Log.info { "$TAG: Saved config: ${config.name}" }
        }
    }
    
    /**
     * Delete a custom configuration
     */
    fun deleteConfig(configId: String) {
        scope.launch {
            if (gradioTTSManager.deleteConfig(configId)) {
                Log.info { "$TAG: Deleted config: $configId" }
            }
        }
    }
    
    /**
     * Test a configuration
     */
    fun testConfig(configId: String) {
        scope.launch {
            updateState { it.copy(isTesting = true, testResult = null, error = null) }
            
            try {
                val result = gradioTTSManager.testConfig(configId)
                
                result.onSuccess {
                    updateState { it.copy(
                        isTesting = false,
                        testResult = TestResult.Success
                    ) }
                    Log.info { "$TAG: Test successful for $configId" }
                }.onFailure { error ->
                    updateState { it.copy(
                        isTesting = false,
                        testResult = TestResult.Error(error.message ?: "Unknown error")
                    ) }
                    Log.error { "$TAG: Test failed for $configId: ${error.message}" }
                }
            } catch (e: Exception) {
                updateState { it.copy(
                    isTesting = false,
                    testResult = TestResult.Error(e.message ?: "Unknown error")
                ) }
                Log.error { "$TAG: Test exception for $configId: ${e.message}" }
            }
        }
    }
    
    /**
     * Clear test result
     */
    fun clearTestResult() {
        updateState { it.copy(testResult = null) }
    }
    
    /**
     * Reset all presets to default values
     */
    fun resetPresets() {
        gradioTTSManager.resetPresets()
    }
    
    /**
     * Get preset configurations
     */
    fun getPresetConfigs(): List<GradioTTSConfig> = gradioTTSManager.getPresetConfigs()
    
    /**
     * Get custom configurations
     */
    fun getCustomConfigs(): List<GradioTTSConfig> = gradioTTSManager.getCustomConfigs()
    
    /**
     * Duplicate a configuration as custom
     */
    fun duplicateConfig(config: GradioTTSConfig) {
        val newConfig = config.copy(
            id = "custom_${currentTimeToLong()}",
            name = "${config.name} (Copy)",
            isCustom = true
        )
        openEditDialog(newConfig)
    }
    
    /**
     * Create a new custom configuration from scratch
     */
    fun createNewCustomConfig() {
        openEditDialog(null)
    }
    
    /**
     * Update a parameter in the editing config
     */
    fun updateEditingConfigParam(paramIndex: Int, newParam: GradioParam) {
        val currentConfig = state.value.editingConfig ?: return
        val newParams = currentConfig.parameters.toMutableList()
        if (paramIndex < newParams.size) {
            newParams[paramIndex] = newParam
        }
        updateState { it.copy(
            editingConfig = currentConfig.copy(parameters = newParams)
        ) }
    }
    
    /**
     * Add a parameter to the editing config
     */
    fun addParameterToEditingConfig(param: GradioParam) {
        val currentConfig = state.value.editingConfig ?: return
        updateState { it.copy(
            editingConfig = currentConfig.copy(
                parameters = currentConfig.parameters + param
            )
        ) }
    }
    
    /**
     * Remove a parameter from the editing config
     */
    fun removeParameterFromEditingConfig(paramIndex: Int) {
        val currentConfig = state.value.editingConfig ?: return
        val newParams = currentConfig.parameters.toMutableList()
        if (paramIndex < newParams.size) {
            newParams.removeAt(paramIndex)
        }
        updateState { it.copy(
            editingConfig = currentConfig.copy(parameters = newParams)
        ) }
    }
}
