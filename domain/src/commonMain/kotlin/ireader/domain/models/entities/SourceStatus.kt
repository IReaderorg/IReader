package ireader.domain.models.entities

import kotlinx.serialization.Serializable

/**
 * Represents the operational status of a source/plugin.
 * Used to track whether sources are working, broken, or unavailable.
 * 
 * This is a sealed class to allow for pattern matching and additional data
 * in error states.
 */
@Serializable
sealed class SourceStatus {
    /** Source is working normally and online */
    @Serializable
    object Working : SourceStatus()
    
    /** Source is online and reachable */
    @Serializable
    object Online : SourceStatus()
    
    /** Source is offline or unreachable */
    @Serializable
    object Offline : SourceStatus()
    
    /** Source requires user to login first */
    @Serializable
    object LoginRequired : SourceStatus()
    
    /** Source is outdated and may not work correctly */
    @Serializable
    data class Outdated(val currentVersion: String? = null, val requiredVersion: String? = null) : SourceStatus()
    
    /** Source failed to load or initialize */
    @Serializable
    data class LoadFailed(val error: String) : SourceStatus()
    
    /** Source requires a plugin that is not installed */
    @Serializable
    data class RequiresPlugin(val pluginId: String, val pluginName: String) : SourceStatus()
    
    /** Source is incompatible with current app version */
    @Serializable
    data class Incompatible(val requiredAppVersion: String? = null) : SourceStatus()
    
    /** Source has been deprecated by the author */
    @Serializable
    data class Deprecated(val reason: String? = null) : SourceStatus()
    
    /** Source encountered an error */
    @Serializable
    data class Error(val errorMessage: String) : SourceStatus()
    
    /** Source status is unknown (not yet checked) */
    @Serializable
    object Unknown : SourceStatus()
    
    /**
     * Check if this status indicates the source is usable.
     */
    fun isUsable(): Boolean = when (this) {
        is Working, is Online -> true
        else -> false
    }
    
    /**
     * Check if this status indicates an error condition.
     */
    fun isError(): Boolean = when (this) {
        is Error, is LoadFailed, is Incompatible -> true
        else -> false
    }
    
    /**
     * Check if this status can be retried.
     */
    fun canRetry(): Boolean = when (this) {
        is Offline, is Error, is LoginRequired -> true
        else -> false
    }
    
    /**
     * Get a user-friendly message for this status.
     */
    fun getMessage(): String = when (this) {
        is Working -> "Working"
        is Online -> "Online"
        is Offline -> "Offline"
        is LoginRequired -> "Login Required"
        is Outdated -> "Outdated"
        is LoadFailed -> "Load Failed: $error"
        is RequiresPlugin -> "Requires $pluginName"
        is Incompatible -> "Incompatible"
        is Deprecated -> "Deprecated"
        is Error -> errorMessage
        is Unknown -> "Unknown"
    }
}

/**
 * Detailed information about why a source is unavailable.
 */
@Serializable
data class SourceUnavailableInfo(
    val status: SourceStatus,
    val reason: String,
    val suggestion: String,
    val canRetry: Boolean = false,
    val canUninstall: Boolean = true,
    val requiredPluginId: String? = null,
    val requiredAppVersion: String? = null,
    val lastError: String? = null,
    val errorTimestamp: Long? = null
) {
    companion object {
        fun loadFailed(error: String): SourceUnavailableInfo {
            return SourceUnavailableInfo(
                status = SourceStatus.LoadFailed(error),
                reason = "Failed to load source: $error",
                suggestion = "Try reinstalling the source or updating the app",
                canRetry = true,
                lastError = error
            )
        }
        
        fun requiresPlugin(pluginId: String, pluginName: String): SourceUnavailableInfo {
            return SourceUnavailableInfo(
                status = SourceStatus.RequiresPlugin(pluginId, pluginName),
                reason = "This source requires the $pluginName plugin",
                suggestion = "Install the required plugin to use this source",
                canRetry = false,
                requiredPluginId = pluginId
            )
        }
        
        fun outdated(currentVersion: String, requiredVersion: String): SourceUnavailableInfo {
            return SourceUnavailableInfo(
                status = SourceStatus.Outdated(currentVersion, requiredVersion),
                reason = "Source version $currentVersion is outdated",
                suggestion = "Update the source to version $requiredVersion or newer",
                canRetry = false
            )
        }
        
        fun incompatible(appVersion: String, requiredVersion: String): SourceUnavailableInfo {
            return SourceUnavailableInfo(
                status = SourceStatus.Incompatible(requiredVersion),
                reason = "Source requires app version $requiredVersion",
                suggestion = "Update the app to use this source",
                canRetry = false,
                requiredAppVersion = requiredVersion
            )
        }
        
        fun deprecated(reason: String): SourceUnavailableInfo {
            return SourceUnavailableInfo(
                status = SourceStatus.Deprecated(reason),
                reason = "This source has been deprecated: $reason",
                suggestion = "Consider using an alternative source",
                canRetry = false
            )
        }
        
        fun offline(lastOnline: Long? = null): SourceUnavailableInfo {
            return SourceUnavailableInfo(
                status = SourceStatus.Offline,
                reason = "Source is currently offline or unreachable",
                suggestion = "Check your internet connection or try again later",
                canRetry = true,
                canUninstall = false,
                errorTimestamp = lastOnline
            )
        }
        
        fun loginRequired(loginUrl: String? = null): SourceUnavailableInfo {
            return SourceUnavailableInfo(
                status = SourceStatus.LoginRequired,
                reason = "This source requires you to login",
                suggestion = "Open the source in WebView to login",
                canRetry = true,
                canUninstall = false,
                lastError = loginUrl
            )
        }
        
        fun error(message: String): SourceUnavailableInfo {
            return SourceUnavailableInfo(
                status = SourceStatus.Error(message),
                reason = message,
                suggestion = "Try again or contact support if the issue persists",
                canRetry = true
            )
        }
    }
}
