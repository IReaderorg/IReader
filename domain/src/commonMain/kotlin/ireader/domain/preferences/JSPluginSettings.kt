package ireader.domain.preferences

/**
 * Settings for JavaScript plugin system.
 * 
 * @property enabled Whether JS plugins are enabled
 * @property autoUpdate Whether to automatically check for and install updates
 * @property debugMode Whether to enable debug logging for plugins
 * @property maxConcurrentExecutions Maximum number of concurrent plugin executions
 * @property executionTimeout Timeout for plugin method execution in milliseconds
 * @property memoryLimit Memory limit per plugin in bytes
 */
data class JSPluginSettings(
    val enabled: Boolean = true,
    val autoUpdate: Boolean = true,
    val debugMode: Boolean = false,
    val maxConcurrentExecutions: Int = 5,
    val executionTimeout: Long = 30000L,
    val memoryLimit: Long = 64 * 1024 * 1024L // 64MB
)
