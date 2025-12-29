package ireader.presentation.ui.settings.cloudflare

import ireader.domain.plugins.Plugin

/**
 * Android stub implementation of PluginReflectionHelper.
 * FlareSolverr is not supported on Android, so all methods return defaults.
 */
actual object PluginReflectionHelper {
    
    actual fun isDownloaded(plugin: Plugin): Boolean = false
    
    actual fun isCurrentlyDownloading(plugin: Plugin): Boolean = false
    
    actual fun getDownloadProgress(plugin: Plugin): Float = 0f
    
    actual fun getDownloadStatus(plugin: Plugin): String = "Not supported on Android"
    
    actual fun startDownload(plugin: Plugin, onProgress: (Float, String) -> Unit): Boolean = false
    
    actual fun isServerRunning(plugin: Plugin): Boolean = false
    
    actual fun startServer(plugin: Plugin) {}
    
    actual fun stopServer(plugin: Plugin) {}
}
