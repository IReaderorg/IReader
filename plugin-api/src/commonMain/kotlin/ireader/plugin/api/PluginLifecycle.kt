package ireader.plugin.api

/**
 * Extended lifecycle interface for plugins that need more control.
 * Provides hooks for various app lifecycle events.
 */
interface PluginLifecycle {
    /**
     * Called when the plugin is first installed.
     * Use for one-time setup like database migrations.
     */
    suspend fun onInstall(context: PluginContext) {}
    
    /**
     * Called when the plugin is updated from a previous version.
     * @param previousVersion The version being updated from
     */
    suspend fun onUpdate(context: PluginContext, previousVersion: String) {}
    
    /**
     * Called when the plugin is about to be uninstalled.
     * Use for cleanup like removing stored data.
     */
    suspend fun onUninstall(context: PluginContext) {}
    
    /**
     * Called when the app starts and plugin is enabled.
     */
    suspend fun onAppStart(context: PluginContext) {}
    
    /**
     * Called when the app is going to background.
     */
    suspend fun onAppBackground(context: PluginContext) {}
    
    /**
     * Called when the app returns to foreground.
     */
    suspend fun onAppForeground(context: PluginContext) {}
    
    /**
     * Called when the app is about to terminate.
     */
    suspend fun onAppTerminate(context: PluginContext) {}
    
    /**
     * Called when user opens a book.
     */
    suspend fun onBookOpened(context: PluginContext, bookId: String) {}
    
    /**
     * Called when user closes a book.
     */
    suspend fun onBookClosed(context: PluginContext, bookId: String) {}
    
    /**
     * Called when user starts reading a chapter.
     */
    suspend fun onChapterStarted(context: PluginContext, bookId: String, chapterId: String) {}
    
    /**
     * Called when user finishes reading a chapter.
     */
    suspend fun onChapterFinished(context: PluginContext, bookId: String, chapterId: String) {}
    
    /**
     * Called when network connectivity changes.
     */
    suspend fun onNetworkChanged(context: PluginContext, isConnected: Boolean) {}
    
    /**
     * Called periodically for background work (if BACKGROUND_SERVICE permission granted).
     * @param intervalMs The interval in milliseconds since last call
     */
    suspend fun onBackgroundWork(context: PluginContext, intervalMs: Long) {}
}

/**
 * Interface for plugins that provide configuration UI.
 */
interface ConfigurablePlugin {
    /**
     * Get configuration options for this plugin.
     */
    fun getConfigurationOptions(): List<ConfigOption>
    
    /**
     * Validate configuration before saving.
     * @return null if valid, error message if invalid
     */
    fun validateConfiguration(config: Map<String, Any>): String?
    
    /**
     * Called when configuration is updated.
     */
    fun onConfigurationChanged(config: Map<String, Any>)
}

/**
 * Configuration option for plugin settings UI.
 */
sealed class ConfigOption {
    abstract val key: String
    abstract val title: String
    abstract val description: String?
    abstract val required: Boolean
    
    /**
     * Text input option.
     */
    data class Text(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val defaultValue: String = "",
        val placeholder: String? = null,
        val inputType: TextInputType = TextInputType.TEXT,
        val maxLength: Int? = null,
        val validation: TextValidation? = null
    ) : ConfigOption()
    
    /**
     * Number input option.
     */
    data class Number(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val defaultValue: Double = 0.0,
        val min: Double? = null,
        val max: Double? = null,
        val step: Double = 1.0,
        val isInteger: Boolean = false
    ) : ConfigOption()
    
    /**
     * Boolean toggle option.
     */
    data class Toggle(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val defaultValue: Boolean = false
    ) : ConfigOption()
    
    /**
     * Single selection option.
     */
    data class Select(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val options: List<SelectOption>,
        val defaultValue: String? = null
    ) : ConfigOption()
    
    /**
     * Multi-selection option.
     */
    data class MultiSelect(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val options: List<SelectOption>,
        val defaultValues: List<String> = emptyList(),
        val maxSelections: Int? = null
    ) : ConfigOption()
    
    /**
     * Slider option.
     */
    data class Slider(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val defaultValue: Float = 0f,
        val min: Float = 0f,
        val max: Float = 100f,
        val step: Float = 1f,
        val showValue: Boolean = true,
        val valueFormat: String? = null
    ) : ConfigOption()
    
    /**
     * Color picker option.
     */
    data class Color(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val defaultValue: Long = 0xFF000000,
        val showAlpha: Boolean = false
    ) : ConfigOption()
    
    /**
     * Server endpoint option.
     */
    data class ServerEndpoint(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val defaultValue: String = "",
        val placeholder: String = "http://localhost:7860",
        val testConnection: Boolean = true
    ) : ConfigOption()
    
    /**
     * API key option (masked input).
     */
    data class ApiKey(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val placeholder: String = "Enter API key",
        val validateUrl: String? = null
    ) : ConfigOption()
    
    /**
     * File picker option.
     */
    data class FilePicker(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val allowedExtensions: List<String> = emptyList(),
        val allowMultiple: Boolean = false
    ) : ConfigOption()
    
    /**
     * Group of options (for organization).
     */
    data class Group(
        override val key: String,
        override val title: String,
        override val description: String? = null,
        override val required: Boolean = false,
        val options: List<ConfigOption>,
        val collapsible: Boolean = true,
        val initiallyExpanded: Boolean = true
    ) : ConfigOption()
}

data class SelectOption(
    val value: String,
    val label: String,
    val description: String? = null,
    val icon: String? = null
)

enum class TextInputType {
    TEXT,
    PASSWORD,
    EMAIL,
    URL,
    NUMBER,
    MULTILINE
}

data class TextValidation(
    val pattern: String? = null,
    val errorMessage: String? = null
)

/**
 * Interface for plugins that can be tested.
 */
interface TestablePlugin {
    /**
     * Run self-test to verify plugin is working correctly.
     * @return Test result with details
     */
    suspend fun runSelfTest(context: PluginContext): PluginTestResult
}

/**
 * Plugin test result.
 */
data class PluginTestResult(
    val success: Boolean,
    val message: String,
    val details: List<TestDetail> = emptyList(),
    val durationMs: Long
)

data class TestDetail(
    val name: String,
    val passed: Boolean,
    val message: String? = null
)

/**
 * Interface for plugins that provide analytics/metrics.
 */
interface MetricsPlugin {
    /**
     * Get plugin metrics.
     */
    fun getMetrics(): PluginMetrics
    
    /**
     * Reset metrics.
     */
    fun resetMetrics()
}

/**
 * Plugin metrics.
 */
data class PluginMetrics(
    val totalCalls: Long,
    val successfulCalls: Long,
    val failedCalls: Long,
    val averageResponseTimeMs: Long,
    val lastUsed: Long?,
    val customMetrics: Map<String, Any> = emptyMap()
)