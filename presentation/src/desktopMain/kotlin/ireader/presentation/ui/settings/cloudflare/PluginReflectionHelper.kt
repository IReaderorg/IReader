package ireader.presentation.ui.settings.cloudflare

import ireader.domain.plugins.Plugin

/**
 * Desktop implementation of PluginReflectionHelper using JVM reflection.
 */
actual object PluginReflectionHelper {
    
    actual fun isDownloaded(plugin: Plugin): Boolean {
        return callMethod<Boolean>(plugin, "isDownloaded") ?: false
    }
    
    actual fun isCurrentlyDownloading(plugin: Plugin): Boolean {
        return callMethod<Boolean>(plugin, "isCurrentlyDownloading") ?: false
    }
    
    actual fun getDownloadProgress(plugin: Plugin): Float {
        return callMethod<Float>(plugin, "getDownloadProgressFloat") ?: 0f
    }
    
    actual fun getDownloadStatus(plugin: Plugin): String {
        return callMethod<String>(plugin, "getDownloadStatusMessage") ?: ""
    }
    
    actual fun startDownload(plugin: Plugin, onProgress: (Float, String) -> Unit): Boolean {
        // Set the progress callback first
        try {
            val field = plugin::class.java.getDeclaredField("onDownloadProgress")
            field.isAccessible = true
            field.set(plugin, onProgress)
        } catch (e: Exception) {
            // Failed to set progress callback - continue anyway
        }
        
        // Start the download
        return callMethod<Boolean>(plugin, "downloadFlareSolverr") ?: false
    }
    
    actual fun isServerRunning(plugin: Plugin): Boolean {
        return callMethod<Boolean>(plugin, "isServerRunning") ?: false
    }
    
    actual fun startServer(plugin: Plugin) {
        callMethod<Unit>(plugin, "startServer")
    }
    
    actual fun stopServer(plugin: Plugin) {
        callMethod<Unit>(plugin, "stopServer")
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun <T> callMethod(plugin: Any, methodName: String): T? {
        return try {
            val method = plugin::class.java.methods.find { 
                it.name == methodName && it.parameterCount == 0 
            }
            method?.invoke(plugin) as? T
        } catch (e: Exception) {
            null
        }
    }
}
