package ireader.domain.js.models

/**
 * Permissions that JavaScript plugins can request.
 */
enum class JSPluginPermission {
    /**
     * Permission to make network requests.
     */
    NETWORK,
    
    /**
     * Permission to store persistent data.
     */
    STORAGE,
    
    /**
     * Permission to use WebView for authentication.
     */
    WEBVIEW
}

/**
 * Represents a permission request result.
 */
sealed class PermissionResult {
    object Granted : PermissionResult()
    object Denied : PermissionResult()
    data class Error(val message: String) : PermissionResult()
    
    fun isGranted(): Boolean = this is Granted
}
