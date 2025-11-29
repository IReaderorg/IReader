package ireader.domain.services.tts_service

import io.ktor.client.*
import ireader.core.log.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Manager for Gradio TTS configurations and engines.
 * Handles preset and custom TTS configurations, persistence, and engine creation.
 */
class GradioTTSManager(
    private val httpClient: HttpClient,
    private val audioPlayerFactory: () -> CoquiAudioPlayer,
    private val saveConfigs: (String) -> Unit,
    private val loadConfigs: () -> String?
) {
    private val json = Json { 
        ignoreUnknownKeys = true 
        prettyPrint = true
    }
    
    private val _configs = MutableStateFlow<List<GradioTTSConfig>>(emptyList())
    val configs: StateFlow<List<GradioTTSConfig>> = _configs.asStateFlow()
    
    private val _activeConfigId = MutableStateFlow<String?>(null)
    val activeConfigId: StateFlow<String?> = _activeConfigId.asStateFlow()
    
    private var currentEngine: GenericGradioTTSEngine? = null
    
    companion object {
        private const val TAG = "GradioTTSManager"
    }
    
    init {
        loadSavedConfigs()
    }
    
    /**
     * Load saved configurations from storage
     */
    private fun loadSavedConfigs() {
        try {
            val savedJson = loadConfigs()
            if (savedJson != null) {
                val saved = json.decodeFromString<GradioTTSManagerState>(savedJson)
                _configs.value = saved.configs
                _activeConfigId.value = saved.activeConfigId
                Log.info { "$TAG: Loaded ${saved.configs.size} configs" }
            } else {
                // Initialize with presets
                _configs.value = GradioTTSPresets.getAllPresets()
                Log.info { "$TAG: Initialized with ${_configs.value.size} presets" }
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to load configs: ${e.message}" }
            _configs.value = GradioTTSPresets.getAllPresets()
        }
    }
    
    /**
     * Save current configurations to storage
     */
    private fun saveCurrentConfigs() {
        try {
            val state = GradioTTSManagerState(
                configs = _configs.value,
                activeConfigId = _activeConfigId.value
            )
            saveConfigs(json.encodeToString(state))
            Log.info { "$TAG: Saved ${_configs.value.size} configs" }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to save configs: ${e.message}" }
        }
    }
    
    /**
     * Get all available configurations (presets + custom)
     */
    fun getAllConfigs(): List<GradioTTSConfig> = _configs.value
    
    /**
     * Get only preset configurations
     */
    fun getPresetConfigs(): List<GradioTTSConfig> = _configs.value.filter { !it.isCustom }
    
    /**
     * Get only custom configurations
     */
    fun getCustomConfigs(): List<GradioTTSConfig> = _configs.value.filter { it.isCustom }
    
    /**
     * Get configuration by ID
     */
    fun getConfigById(id: String): GradioTTSConfig? = _configs.value.find { it.id == id }
    
    /**
     * Get the currently active configuration
     */
    fun getActiveConfig(): GradioTTSConfig? {
        val activeId = _activeConfigId.value ?: return null
        return getConfigById(activeId)
    }
    
    /**
     * Set the active configuration
     */
    fun setActiveConfig(configId: String) {
        if (getConfigById(configId) != null) {
            _activeConfigId.value = configId
            currentEngine?.cleanup()
            currentEngine = null
            saveCurrentConfigs()
            Log.info { "$TAG: Set active config to $configId" }
        }
    }
    
    /**
     * Add a new custom configuration
     */
    fun addCustomConfig(config: GradioTTSConfig): Boolean {
        if (_configs.value.any { it.id == config.id }) {
            Log.warn { "$TAG: Config with id ${config.id} already exists" }
            return false
        }
        
        val customConfig = config.copy(isCustom = true)
        _configs.value = _configs.value + customConfig
        saveCurrentConfigs()
        Log.info { "$TAG: Added custom config: ${config.name}" }
        return true
    }
    
    /**
     * Update an existing configuration
     */
    fun updateConfig(config: GradioTTSConfig): Boolean {
        val index = _configs.value.indexOfFirst { it.id == config.id }
        if (index == -1) {
            Log.warn { "$TAG: Config with id ${config.id} not found" }
            return false
        }
        
        val updatedList = _configs.value.toMutableList()
        updatedList[index] = config
        _configs.value = updatedList
        
        // Recreate engine if this is the active config
        if (_activeConfigId.value == config.id) {
            currentEngine?.cleanup()
            currentEngine = null
        }
        
        saveCurrentConfigs()
        Log.info { "$TAG: Updated config: ${config.name}" }
        return true
    }
    
    /**
     * Delete a custom configuration
     */
    fun deleteConfig(configId: String): Boolean {
        val config = getConfigById(configId)
        if (config == null) {
            Log.warn { "$TAG: Config with id $configId not found" }
            return false
        }
        
        if (!config.isCustom) {
            Log.warn { "$TAG: Cannot delete preset config: ${config.name}" }
            return false
        }
        
        _configs.value = _configs.value.filter { it.id != configId }
        
        if (_activeConfigId.value == configId) {
            _activeConfigId.value = null
            currentEngine?.cleanup()
            currentEngine = null
        }
        
        saveCurrentConfigs()
        Log.info { "$TAG: Deleted config: ${config.name}" }
        return true
    }
    
    /**
     * Enable or disable a configuration
     */
    fun setConfigEnabled(configId: String, enabled: Boolean) {
        val config = getConfigById(configId) ?: return
        updateConfig(config.copy(enabled = enabled))
    }
    
    /**
     * Reset presets to default values
     */
    fun resetPresets() {
        val customConfigs = getCustomConfigs()
        _configs.value = GradioTTSPresets.getAllPresets() + customConfigs
        saveCurrentConfigs()
        Log.info { "$TAG: Reset presets to defaults" }
    }
    
    /**
     * Get or create the TTS engine for the active configuration
     */
    fun getEngine(): GenericGradioTTSEngine? {
        val activeConfig = getActiveConfig()
        if (activeConfig == null || !activeConfig.enabled) {
            return null
        }
        
        // Return existing engine if config hasn't changed
        if (currentEngine != null && currentEngine?.getConfig()?.id == activeConfig.id) {
            return currentEngine
        }
        
        // Create new engine
        currentEngine?.cleanup()
        currentEngine = createEngine(activeConfig)
        return currentEngine
    }
    
    /**
     * Create a TTS engine for a specific configuration
     */
    fun createEngine(config: GradioTTSConfig): GenericGradioTTSEngine {
        return GenericGradioTTSEngine(
            config = config,
            httpClient = httpClient,
            audioPlayer = audioPlayerFactory()
        )
    }
    
    /**
     * Test a configuration by synthesizing a sample text
     */
    suspend fun testConfig(configId: String, testText: String = "Hello, this is a test."): Result<ByteArray> {
        val config = getConfigById(configId)
            ?: return Result.failure(IllegalArgumentException("Config not found: $configId"))
        
        if (config.spaceUrl.isEmpty()) {
            return Result.failure(IllegalArgumentException("Space URL is empty"))
        }
        
        return try {
            val engine = createEngine(config)
            var result: ByteArray? = null
            var error: String? = null
            
            engine.setCallback(object : TTSEngineCallback {
                override fun onStart(utteranceId: String) {}
                override fun onDone(utteranceId: String) {}
                override fun onError(utteranceId: String, errorMsg: String) {
                    error = errorMsg
                }
            })
            
            // Use internal method to just generate audio without playing
            // For testing, we'll speak and capture the result
            engine.speak(testText, "test")
            
            // Wait a bit for the result
            kotlinx.coroutines.delay(5000)
            
            engine.cleanup()
            
            if (error != null) {
                Result.failure(Exception(error))
            } else {
                Result.success(ByteArray(0)) // Success indicator
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Test failed for $configId: ${e.message}" }
            Result.failure(e)
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        currentEngine?.cleanup()
        currentEngine = null
    }
}

/**
 * State class for serialization
 */
@kotlinx.serialization.Serializable
data class GradioTTSManagerState(
    val configs: List<GradioTTSConfig>,
    val activeConfigId: String?
)
