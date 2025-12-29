package ireader.presentation.ui.settings.cloudflare

import ireader.domain.plugins.Plugin

/**
 * Helper for calling plugin methods via reflection.
 * This is needed because plugins may not implement standard interfaces
 * but still expose methods we need to call.
 * 
 * Desktop-only implementation uses JVM reflection.
 */
expect object PluginReflectionHelper {
    /**
     * Check if the plugin has FlareSolverr downloaded.
     */
    fun isDownloaded(plugin: Plugin): Boolean
    
    /**
     * Check if the plugin is currently downloading.
     */
    fun isCurrentlyDownloading(plugin: Plugin): Boolean
    
    /**
     * Get the download progress (0.0 to 1.0).
     */
    fun getDownloadProgress(plugin: Plugin): Float
    
    /**
     * Get the download status message.
     */
    fun getDownloadStatus(plugin: Plugin): String
    
    /**
     * Start downloading FlareSolverr.
     * @return true if download started successfully
     */
    fun startDownload(plugin: Plugin, onProgress: (Float, String) -> Unit): Boolean
    
    /**
     * Check if the FlareSolverr server is running.
     */
    fun isServerRunning(plugin: Plugin): Boolean
    
    /**
     * Start the FlareSolverr server.
     */
    fun startServer(plugin: Plugin)
    
    /**
     * Stop the FlareSolverr server.
     */
    fun stopServer(plugin: Plugin)
}
