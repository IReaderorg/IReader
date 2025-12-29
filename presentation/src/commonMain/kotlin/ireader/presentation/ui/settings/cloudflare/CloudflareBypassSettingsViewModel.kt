package ireader.presentation.ui.settings.cloudflare

import ireader.core.http.cloudflare.CloudflareBypassPluginManager
import ireader.core.prefs.PreferenceStore
import ireader.domain.plugins.Plugin
import ireader.domain.plugins.PluginManager
import ireader.presentation.ui.component.DownloadPhase
import ireader.presentation.ui.component.ExternalResourceDownloadProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Cloudflare bypass settings screen.
 */
class CloudflareBypassSettingsViewModel(
    val bypassManager: CloudflareBypassPluginManager,
    private val preferenceStore: PreferenceStore,
    private val pluginManager: PluginManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // FlareSolverr URL preference
    private val flareSolverrUrlPref = preferenceStore.getString(
        key = "flaresolverr_url",
        defaultValue = "http://localhost:8191/v1"
    )
    
    private val _flareSolverrUrl = MutableStateFlow(flareSolverrUrlPref.get())
    val flareSolverrUrl: StateFlow<String> = _flareSolverrUrl.asStateFlow()
    
    val providers = bypassManager.providers
    val status = bypassManager.status
    
    // FlareSolverr download state
    val flareSolverrDownloadState = FlareSolverrDownloadState()
    
    // Server running state
    private val _isFlareSolverrRunning = MutableStateFlow(false)
    val isFlareSolverrRunning: StateFlow<Boolean> = _isFlareSolverrRunning.asStateFlow()
    
    // Cache the FlareSolverr plugin reference
    private var flareSolverrPlugin: Plugin? = null
    
    init {
        // Check initial download state
        checkFlareSolverrStatus()
    }
    
    fun updateFlareSolverrUrl(url: String) {
        _flareSolverrUrl.value = url
        flareSolverrUrlPref.set(url)
    }
    
    fun clearCache() {
        bypassManager.clearCache()
    }
    
    suspend fun testConnection(): Boolean {
        return bypassManager.hasAvailableProvider()
    }

    
    /**
     * Find the FlareSolverr plugin from the plugin manager.
     */
    private fun findFlareSolverrPlugin(): Plugin? {
        if (flareSolverrPlugin != null) return flareSolverrPlugin
        
        // Look for the FlareSolverr plugin in the plugin manager
        val plugins = pluginManager.getEnabledPlugins()
        flareSolverrPlugin = plugins.find { plugin ->
            plugin.manifest.id.contains("flaresolverr", ignoreCase = true) ||
            plugin.manifest.name.contains("flaresolverr", ignoreCase = true)
        }
        return flareSolverrPlugin
    }
    
    /**
     * Check FlareSolverr download and server status.
     */
    fun checkFlareSolverrStatus() {
        scope.launch {
            val plugin = findFlareSolverrPlugin()
            
            if (plugin != null) {
                // Use reflection helper to check status
                val isDownloaded = PluginReflectionHelper.isDownloaded(plugin)
                flareSolverrDownloadState.setDownloaded(isDownloaded)
                
                val isRunning = PluginReflectionHelper.isServerRunning(plugin)
                _isFlareSolverrRunning.value = isRunning
            } else {
                // Check if any provider is available from the bypass manager
                val flareSolverrProvider = providers.value.find { 
                    it.id.contains("flaresolverr", ignoreCase = true) 
                }
                
                if (flareSolverrProvider != null) {
                    val isAvailable = try {
                        flareSolverrProvider.isAvailable()
                    } catch (e: Exception) {
                        false
                    }
                    _isFlareSolverrRunning.value = isAvailable
                    flareSolverrDownloadState.setDownloaded(isAvailable)
                } else {
                    _isFlareSolverrRunning.value = false
                    flareSolverrDownloadState.setDownloaded(false)
                }
            }
        }
    }
    
    /**
     * Start downloading FlareSolverr.
     */
    fun downloadFlareSolverr() {
        scope.launch {
            val plugin = findFlareSolverrPlugin()
            
            if (plugin == null) {
                flareSolverrDownloadState.updateProgress(
                    ExternalResourceDownloadProgress(
                        phase = DownloadPhase.ERROR,
                        status = "FlareSolverr plugin not installed. Please install it from the Feature Store first."
                    )
                )
                return@launch
            }
            
            // Check if already downloaded
            if (PluginReflectionHelper.isDownloaded(plugin)) {
                flareSolverrDownloadState.setDownloaded(true)
                flareSolverrDownloadState.updateProgress(
                    ExternalResourceDownloadProgress(
                        phase = DownloadPhase.COMPLETE,
                        downloaded = 500_000_000L,
                        total = 500_000_000L,
                        status = "Already downloaded"
                    )
                )
                return@launch
            }
            
            flareSolverrDownloadState.updateProgress(
                ExternalResourceDownloadProgress(
                    phase = DownloadPhase.CHECKING,
                    status = "Starting download..."
                )
            )
            
            try {
                // Start download with progress callback
                val started = PluginReflectionHelper.startDownload(plugin) { progress, status ->
                    val phase = when {
                        progress >= 1f -> DownloadPhase.COMPLETE
                        progress >= 0.8f -> DownloadPhase.EXTRACTING
                        progress > 0f -> DownloadPhase.DOWNLOADING
                        else -> DownloadPhase.CHECKING
                    }
                    
                    // Estimate bytes based on progress (assuming ~500MB)
                    val estimatedTotal = 500_000_000L
                    val downloaded = (progress * estimatedTotal).toLong()
                    
                    flareSolverrDownloadState.updateProgress(
                        ExternalResourceDownloadProgress(
                            phase = phase,
                            downloaded = downloaded,
                            total = estimatedTotal,
                            status = status
                        )
                    )
                }
                
                if (!started) {
                    // Check if it's because already downloaded
                    if (PluginReflectionHelper.isDownloaded(plugin)) {
                        flareSolverrDownloadState.setDownloaded(true)
                        flareSolverrDownloadState.updateProgress(
                            ExternalResourceDownloadProgress(
                                phase = DownloadPhase.COMPLETE,
                                downloaded = 500_000_000L,
                                total = 500_000_000L,
                                status = "Already downloaded"
                            )
                        )
                        return@launch
                    }
                    
                    flareSolverrDownloadState.updateProgress(
                        ExternalResourceDownloadProgress(
                            phase = DownloadPhase.ERROR,
                            status = "Failed to start download"
                        )
                    )
                    return@launch
                }
                
                // Poll for completion
                while (true) {
                    delay(500)
                    val isDownloading = PluginReflectionHelper.isCurrentlyDownloading(plugin)
                    if (!isDownloading) {
                        val isDownloaded = PluginReflectionHelper.isDownloaded(plugin)
                        flareSolverrDownloadState.setDownloaded(isDownloaded)
                        
                        if (isDownloaded) {
                            flareSolverrDownloadState.updateProgress(
                                ExternalResourceDownloadProgress(
                                    phase = DownloadPhase.COMPLETE,
                                    downloaded = 500_000_000L,
                                    total = 500_000_000L,
                                    status = "Download complete!"
                                )
                            )
                        } else {
                            // Download finished but not found - get error status
                            val status = PluginReflectionHelper.getDownloadStatus(plugin)
                            flareSolverrDownloadState.updateProgress(
                                ExternalResourceDownloadProgress(
                                    phase = DownloadPhase.ERROR,
                                    status = status.ifEmpty { "Download failed" }
                                )
                            )
                        }
                        break
                    }
                    
                    // Update progress from plugin
                    val progress = PluginReflectionHelper.getDownloadProgress(plugin)
                    val status = PluginReflectionHelper.getDownloadStatus(plugin)
                    
                    val phase = when {
                        progress >= 0.8f -> DownloadPhase.EXTRACTING
                        progress > 0f -> DownloadPhase.DOWNLOADING
                        else -> DownloadPhase.CHECKING
                    }
                    
                    val estimatedTotal = 500_000_000L
                    val downloaded = (progress * estimatedTotal).toLong()
                    
                    flareSolverrDownloadState.updateProgress(
                        ExternalResourceDownloadProgress(
                            phase = phase,
                            downloaded = downloaded,
                            total = estimatedTotal,
                            status = status
                        )
                    )
                }
            } catch (e: Exception) {
                println("[CloudflareBypassSettings] Download error: ${e.message}")
                flareSolverrDownloadState.updateProgress(
                    ExternalResourceDownloadProgress(
                        phase = DownloadPhase.ERROR,
                        status = "Download failed: ${e.message}"
                    )
                )
            }
        }
    }

    
    /**
     * Start the FlareSolverr server.
     */
    fun startFlareSolverr() {
        scope.launch {
            val plugin = findFlareSolverrPlugin()
            
            if (plugin != null) {
                try {
                    PluginReflectionHelper.startServer(plugin)
                    
                    // Wait for server to start
                    repeat(30) {
                        delay(1000)
                        val isRunning = PluginReflectionHelper.isServerRunning(plugin)
                        if (isRunning) {
                            _isFlareSolverrRunning.value = true
                            return@launch
                        }
                    }
                    _isFlareSolverrRunning.value = false
                } catch (e: Exception) {
                    println("[CloudflareBypassSettings] Failed to start FlareSolverr: ${e.message}")
                    _isFlareSolverrRunning.value = false
                }
            }
        }
    }
    
    /**
     * Stop the FlareSolverr server.
     */
    fun stopFlareSolverr() {
        scope.launch {
            val plugin = findFlareSolverrPlugin()
            
            if (plugin != null) {
                try {
                    PluginReflectionHelper.stopServer(plugin)
                    _isFlareSolverrRunning.value = false
                } catch (e: Exception) {
                    println("[CloudflareBypassSettings] Failed to stop FlareSolverr: ${e.message}")
                }
            } else {
                _isFlareSolverrRunning.value = false
            }
        }
    }
}
