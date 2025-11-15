package ireader.presentation.ui.core.theme

import androidx.compose.runtime.*
import ireader.domain.models.theme.Theme
import ireader.domain.plugins.ThemePlugin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Error handler for theme plugin errors with fallback support
 * Requirements: 3.5
 */
class ThemeErrorHandler(
    private val pluginThemeManager: PluginThemeManager
) {
    private val _errors = MutableStateFlow<List<ThemeError>>(emptyList())
    val errors: StateFlow<List<ThemeError>> = _errors.asStateFlow()
    
    /**
     * Apply a theme with error handling and fallback
     * Requirements: 3.5
     */
    fun applyThemeWithFallback(
        themeOption: ThemeOption,
        onError: ((ThemeError) -> Unit)? = null
    ): Theme {
        return when (val result = pluginThemeManager.applyTheme(themeOption)) {
            is Result.Success -> result.value
            is Result.Failure -> {
                val error = ThemeError.ApplicationFailed(
                    themeId = themeOption.id,
                    themeName = themeOption.name,
                    exception = result.exception
                )
                
                // Record the error
                recordError(error)
                
                // Notify callback
                onError?.invoke(error)
                
                // Return fallback theme
                pluginThemeManager.getDefaultTheme()
            }
        }
    }
    
    /**
     * Load a plugin theme with error handling
     */
    fun loadPluginTheme(
        plugin: ThemePlugin,
        isDark: Boolean,
        onError: ((ThemeError) -> Unit)? = null
    ): Theme? {
        return try {
            val themeOption = ThemeOption.Plugin(plugin, isDark)
            when (val result = pluginThemeManager.applyTheme(themeOption)) {
                is Result.Success -> result.value
                is Result.Failure -> {
                    val error = ThemeError.PluginLoadFailed(
                        pluginId = plugin.manifest.id,
                        pluginName = plugin.manifest.name,
                        exception = result.exception
                    )
                    recordError(error)
                    onError?.invoke(error)
                    null
                }
            }
        } catch (e: Exception) {
            val error = ThemeError.PluginLoadFailed(
                pluginId = plugin.manifest.id,
                pluginName = plugin.manifest.name,
                exception = e
            )
            recordError(error)
            onError?.invoke(error)
            null
        }
    }
    
    /**
     * Record an error
     */
    private fun recordError(error: ThemeError) {
        _errors.value = _errors.value + error
    }
    
    /**
     * Clear all errors
     */
    fun clearErrors() {
        _errors.value = emptyList()
    }
    
    /**
     * Clear errors for a specific theme
     */
    fun clearErrorsForTheme(themeId: String) {
        _errors.value = _errors.value.filter { 
            when (it) {
                is ThemeError.ApplicationFailed -> it.themeId != themeId
                is ThemeError.PluginLoadFailed -> it.pluginId != themeId
                is ThemeError.AssetLoadFailed -> it.pluginId != themeId
                is ThemeError.InvalidConfiguration -> it.pluginId != themeId
            }
        }
    }
    
    /**
     * Get errors for a specific theme
     */
    fun getErrorsForTheme(themeId: String): List<ThemeError> {
        return _errors.value.filter {
            when (it) {
                is ThemeError.ApplicationFailed -> it.themeId == themeId
                is ThemeError.PluginLoadFailed -> it.pluginId == themeId
                is ThemeError.AssetLoadFailed -> it.pluginId == themeId
                is ThemeError.InvalidConfiguration -> it.pluginId == themeId
            }
        }
    }
}

/**
 * Sealed class representing theme errors
 * Requirements: 3.5
 */
sealed class ThemeError {
    abstract val timestamp: Long
    abstract fun toUserMessage(): String
    
    data class ApplicationFailed(
        val themeId: String,
        val themeName: String,
        val exception: Throwable,
        override val timestamp: Long = System.currentTimeMillis()
    ) : ThemeError() {
        override fun toUserMessage(): String {
            return "Failed to apply theme '$themeName': ${exception.message ?: "Unknown error"}"
        }
    }
    
    data class PluginLoadFailed(
        val pluginId: String,
        val pluginName: String,
        val exception: Throwable,
        override val timestamp: Long = System.currentTimeMillis()
    ) : ThemeError() {
        override fun toUserMessage(): String {
            return "Failed to load theme plugin '$pluginName': ${exception.message ?: "Unknown error"}"
        }
    }
    
    data class AssetLoadFailed(
        val pluginId: String,
        val assetPath: String,
        val exception: Throwable,
        override val timestamp: Long = System.currentTimeMillis()
    ) : ThemeError() {
        override fun toUserMessage(): String {
            return "Failed to load theme asset '$assetPath': ${exception.message ?: "Unknown error"}"
        }
    }
    
    data class InvalidConfiguration(
        val pluginId: String,
        val reason: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : ThemeError() {
        override fun toUserMessage(): String {
            return "Invalid theme configuration: $reason"
        }
    }
}

/**
 * Composable for observing theme errors
 */
@Composable
fun rememberThemeErrors(
    errorHandler: ThemeErrorHandler
): State<List<ThemeError>> {
    return errorHandler.errors.collectAsState()
}

/**
 * Result type for theme operations
 */
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val exception: Throwable) : Result<Nothing>()
    
    companion object {
        fun <T> success(value: T): Result<T> = Success(value)
        fun failure(exception: Throwable): Result<Nothing> = Failure(exception)
    }
    
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
    
    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Failure -> exception
    }
    
    inline fun <R> onSuccess(action: (T) -> R): Result<T> {
        if (this is Success) action(value)
        return this
    }
    
    inline fun onFailure(action: (Throwable) -> Unit): Result<T> {
        if (this is Failure) action(exception)
        return this
    }
}
