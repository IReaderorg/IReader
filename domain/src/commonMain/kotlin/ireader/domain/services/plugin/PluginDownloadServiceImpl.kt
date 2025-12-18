package ireader.domain.services.plugin

import ireader.core.util.createICoroutineScope
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginManager
import ireader.domain.services.common.NotificationService
import ireader.domain.services.common.PluginDownloadProgress
import ireader.domain.services.common.PluginDownloadService
import ireader.domain.services.common.PluginDownloadStatus
import ireader.domain.services.common.ServiceResult
import ireader.domain.services.common.ServiceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Implementation of PluginDownloadService
 * Handles plugin downloads with progress tracking and notifications
 */
class PluginDownloadServiceImpl(
    private val pluginManager: PluginManager,
    private val notificationService: NotificationService?
) : PluginDownloadService {
    
    companion object {
        private const val NOTIFICATION_ID_BASE = 10000
        private const val NOTIFICATION_GROUP_ID = 10999
    }
    
    private val scope = createICoroutineScope()
    
    private val _state = MutableStateFlow(ServiceState.IDLE)
    override val state: StateFlow<ServiceState> = _state.asStateFlow()
    
    private val _downloads = MutableStateFlow<Map<String, PluginDownloadProgress>>(emptyMap())
    override val downloads: StateFlow<Map<String, PluginDownloadProgress>> = _downloads.asStateFlow()
    
    // Active download jobs
    private val downloadJobs = mutableMapOf<String, Job>()
    
    override suspend fun initialize() {
        _state.value = ServiceState.IDLE
    }
    
    override suspend fun start() {
        _state.value = ServiceState.RUNNING
    }
    
    override suspend fun stop() {
        cancelAll()
        _state.value = ServiceState.STOPPED
    }
    
    override fun isRunning(): Boolean {
        return _state.value == ServiceState.RUNNING
    }
    
    override suspend fun cleanup() {
        cancelAll()
        _downloads.value = emptyMap()
        _state.value = ServiceState.IDLE
    }
    
    override suspend fun downloadPlugin(pluginInfo: PluginInfo): ServiceResult<Unit> {
        val pluginId = pluginInfo.id
        val pluginName = pluginInfo.manifest.name
        
        // Check if already downloading
        if (downloadJobs.containsKey(pluginId)) {
            return ServiceResult.Error("Plugin is already downloading")
        }
        
        // Add to downloads map
        updateProgress(pluginId, PluginDownloadProgress(
            pluginId = pluginId,
            pluginName = pluginName,
            version = pluginInfo.manifest.version,
            status = PluginDownloadStatus.QUEUED,
            totalBytes = pluginInfo.fileSize ?: 0
        ))
        
        // Show notification
        showDownloadNotification(pluginId, pluginName, 0f, PluginDownloadStatus.QUEUED)
        
        // Start download job
        val job = scope.launch {
            try {
                _state.value = ServiceState.RUNNING
                
                // Update status to downloading
                updateProgress(pluginId, getProgress(pluginId)?.copy(
                    status = PluginDownloadStatus.DOWNLOADING
                ) ?: return@launch)
                showDownloadNotification(pluginId, pluginName, 0f, PluginDownloadStatus.DOWNLOADING)
                
                // Perform download with progress callback
                val result = withContext(Dispatchers.Default) {
                    pluginManager.installPluginWithProgress(pluginInfo) { progress ->
                        updateProgress(pluginId, progress.toPluginDownloadProgress(pluginName))
                        showDownloadNotification(
                            pluginId, 
                            pluginName, 
                            progress.progress, 
                            when (progress.stage) {
                                ireader.domain.plugins.InstallStage.DOWNLOADING -> PluginDownloadStatus.DOWNLOADING
                                ireader.domain.plugins.InstallStage.VALIDATING -> PluginDownloadStatus.VALIDATING
                                ireader.domain.plugins.InstallStage.INSTALLING -> PluginDownloadStatus.INSTALLING
                                ireader.domain.plugins.InstallStage.COMPLETED -> PluginDownloadStatus.COMPLETED
                                ireader.domain.plugins.InstallStage.FAILED -> PluginDownloadStatus.FAILED
                                else -> PluginDownloadStatus.DOWNLOADING
                            }
                        )
                    }
                }
                
                result.onSuccess {
                    updateProgress(pluginId, getProgress(pluginId)?.copy(
                        status = PluginDownloadStatus.COMPLETED,
                        progress = 1f
                    ) ?: return@launch)
                    showCompletedNotification(pluginId, pluginName)
                }.onFailure { error ->
                    updateProgress(pluginId, getProgress(pluginId)?.copy(
                        status = PluginDownloadStatus.FAILED,
                        errorMessage = error.message
                    ) ?: return@launch)
                    showErrorNotification(pluginId, pluginName, error.message ?: "Unknown error")
                }
                
            } catch (e: Exception) {
                updateProgress(pluginId, getProgress(pluginId)?.copy(
                    status = PluginDownloadStatus.FAILED,
                    errorMessage = e.message
                ) ?: return@launch)
                showErrorNotification(pluginId, pluginName, e.message ?: "Unknown error")
            } finally {
                downloadJobs.remove(pluginId)
                updateServiceState()
            }
        }
        
        downloadJobs[pluginId] = job
        return ServiceResult.Success(Unit)
    }
    
    override suspend fun cancelDownload(pluginId: String): ServiceResult<Unit> {
        downloadJobs[pluginId]?.cancel()
        downloadJobs.remove(pluginId)
        
        updateProgress(pluginId, getProgress(pluginId)?.copy(
            status = PluginDownloadStatus.CANCELLED
        ))
        
        notificationService?.cancelNotification(getNotificationId(pluginId))
        updateServiceState()
        
        return ServiceResult.Success(Unit)
    }
    
    override suspend fun cancelAll(): ServiceResult<Unit> {
        downloadJobs.values.forEach { it.cancel() }
        downloadJobs.clear()
        
        _downloads.value.keys.forEach { pluginId ->
            updateProgress(pluginId, getProgress(pluginId)?.copy(
                status = PluginDownloadStatus.CANCELLED
            ))
            notificationService?.cancelNotification(getNotificationId(pluginId))
        }
        
        _state.value = ServiceState.IDLE
        return ServiceResult.Success(Unit)
    }
    
    override suspend fun retryDownload(pluginId: String): ServiceResult<Unit> {
        val progress = getProgress(pluginId) ?: return ServiceResult.Error("Download not found")
        
        // Find the original plugin info from the progress
        // For retry, we need to store the original PluginInfo somewhere
        // For now, return error - the caller should call downloadPlugin again
        return ServiceResult.Error("Please initiate download again")
    }
    
    override fun getDownloadStatus(pluginId: String): PluginDownloadProgress? {
        return _downloads.value[pluginId]
    }
    
    override fun isDownloading(pluginId: String): Boolean {
        return downloadJobs.containsKey(pluginId)
    }
    
    private fun updateProgress(pluginId: String, progress: PluginDownloadProgress?) {
        if (progress == null) return
        _downloads.update { current ->
            current + (pluginId to progress)
        }
    }
    
    private fun getProgress(pluginId: String): PluginDownloadProgress? {
        return _downloads.value[pluginId]
    }
    
    private fun updateServiceState() {
        _state.value = if (downloadJobs.isEmpty()) {
            ServiceState.IDLE
        } else {
            ServiceState.RUNNING
        }
    }
    
    private fun getNotificationId(pluginId: String): Int {
        return NOTIFICATION_ID_BASE + pluginId.hashCode().and(0xFFFF)
    }
    
    private fun showDownloadNotification(
        pluginId: String,
        pluginName: String,
        progress: Float,
        status: PluginDownloadStatus
    ) {
        val message = when (status) {
            PluginDownloadStatus.QUEUED -> "Waiting..."
            PluginDownloadStatus.DOWNLOADING -> "Downloading... ${(progress * 100).toInt()}%"
            PluginDownloadStatus.VALIDATING -> "Validating..."
            PluginDownloadStatus.INSTALLING -> "Installing..."
            else -> "Processing..."
        }
        
        notificationService?.showProgressNotification(
            id = getNotificationId(pluginId),
            title = "Installing $pluginName",
            message = message,
            progress = (progress * 100).toInt(),
            maxProgress = 100,
            indeterminate = status == PluginDownloadStatus.VALIDATING || 
                           status == PluginDownloadStatus.INSTALLING
        )
    }
    
    private fun showCompletedNotification(pluginId: String, pluginName: String) {
        notificationService?.showNotification(
            id = getNotificationId(pluginId),
            title = "$pluginName installed",
            message = "Plugin installed successfully"
        )
    }
    
    private fun showErrorNotification(pluginId: String, pluginName: String, error: String) {
        notificationService?.showNotification(
            id = getNotificationId(pluginId),
            title = "Failed to install $pluginName",
            message = error
        )
    }
    
    /**
     * Convert PluginInstallProgress to PluginDownloadProgress
     */
    private fun ireader.domain.plugins.PluginInstallProgress.toPluginDownloadProgress(
        pluginName: String
    ): PluginDownloadProgress {
        return PluginDownloadProgress(
            pluginId = this.pluginId,
            pluginName = pluginName,
            version = "",
            status = when (this.stage) {
                ireader.domain.plugins.InstallStage.PENDING -> PluginDownloadStatus.QUEUED
                ireader.domain.plugins.InstallStage.DOWNLOADING -> PluginDownloadStatus.DOWNLOADING
                ireader.domain.plugins.InstallStage.VALIDATING -> PluginDownloadStatus.VALIDATING
                ireader.domain.plugins.InstallStage.INSTALLING -> PluginDownloadStatus.INSTALLING
                ireader.domain.plugins.InstallStage.COMPLETED -> PluginDownloadStatus.COMPLETED
                ireader.domain.plugins.InstallStage.FAILED -> PluginDownloadStatus.FAILED
            },
            progress = this.progress,
            bytesDownloaded = this.bytesDownloaded,
            totalBytes = this.totalBytes,
            errorMessage = this.error
        )
    }
}
