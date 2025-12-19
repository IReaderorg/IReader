package ireader.domain.plugins.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ireader.core.util.createICoroutineScope
import ireader.domain.plugins.PluginManager
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manager for plugin backup and sync.
 */
class PluginSyncManager(
    private val pluginManager: PluginManager,
    private val syncRepository: PluginSyncRepository,
    private val changeTracker: ChangeTracker
) {
    private val scope: CoroutineScope = createICoroutineScope()
    private var autoSyncJob: Job? = null
    
    private val _syncStatus = MutableStateFlow(DeviceSyncStatus(
        deviceId = "",
        deviceName = "",
        lastSyncTime = null,
        syncState = SyncState.IDLE,
        pendingChanges = 0,
        conflictCount = 0
    ))
    val syncStatus: StateFlow<DeviceSyncStatus> = _syncStatus.asStateFlow()
    
    private val _conflicts = MutableStateFlow<List<SyncConflict>>(emptyList())
    val conflicts: StateFlow<List<SyncConflict>> = _conflicts.asStateFlow()
    
    private val _events = MutableSharedFlow<SyncEvent>(replay = 0)
    val events: Flow<SyncEvent> = _events.asSharedFlow()
    
    private var config = SyncConfig()
    
    /**
     * Initialize sync with device info.
     */
    suspend fun initialize(deviceId: String, deviceName: String) {
        _syncStatus.value = _syncStatus.value.copy(
            deviceId = deviceId,
            deviceName = deviceName
        )
        
        // Load pending changes
        val pendingChanges = changeTracker.getPendingChanges()
        _syncStatus.value = _syncStatus.value.copy(pendingChanges = pendingChanges.size)
    }

    /**
     * Update sync configuration.
     */
    fun updateConfig(newConfig: SyncConfig) {
        config = newConfig
        if (config.autoSync) {
            startAutoSync()
        } else {
            stopAutoSync()
        }
    }
    
    /**
     * Start automatic sync.
     */
    fun startAutoSync() {
        if (!config.enabled || !config.autoSync) return
        
        autoSyncJob?.cancel()
        autoSyncJob = scope.launch {
            while (isActive) {
                delay(config.syncIntervalMinutes * 60 * 1000L)
                sync()
            }
        }
    }
    
    /**
     * Stop automatic sync.
     */
    fun stopAutoSync() {
        autoSyncJob?.cancel()
        autoSyncJob = null
    }
    
    /**
     * Perform a full sync.
     */
    suspend fun sync(): SyncResult {
        if (!config.enabled) {
            return SyncResult(
                success = false,
                syncedPlugins = 0,
                syncedSettings = 0,
                syncedPipelines = 0,
                syncedCollections = 0,
                conflicts = emptyList(),
                errors = listOf(SyncError(null, "DISABLED", "Sync is disabled")),
                duration = 0,
                timestamp = currentTimeToLong()
            )
        }
        
        val startTime = currentTimeToLong()
        _syncStatus.value = _syncStatus.value.copy(syncState = SyncState.SYNCING)
        
        val pendingChanges = changeTracker.getPendingChanges()
        _events.emit(SyncEvent.Started(pendingChanges.size))
        
        val syncedPlugins = mutableListOf<String>()
        val syncedSettings = mutableListOf<String>()
        val syncedPipelines = mutableListOf<String>()
        val syncedCollections = mutableListOf<String>()
        val detectedConflicts = mutableListOf<SyncConflict>()
        val errors = mutableListOf<SyncError>()
        
        try {
            // Upload local changes
            _syncStatus.value = _syncStatus.value.copy(syncState = SyncState.UPLOADING)
            for ((index, change) in pendingChanges.withIndex()) {
                try {
                    _events.emit(SyncEvent.Progress(index + 1, pendingChanges.size, change.entityId))
                    
                    val conflict = syncRepository.uploadChange(change)
                    if (conflict != null) {
                        detectedConflicts.add(conflict)
                        _events.emit(SyncEvent.ConflictDetected(conflict))
                    } else {
                        changeTracker.markSynced(change.id)
                        when (change.entityType) {
                            EntityType.PLUGIN -> syncedPlugins.add(change.entityId)
                            EntityType.SETTING -> syncedSettings.add(change.entityId)
                            EntityType.PIPELINE -> syncedPipelines.add(change.entityId)
                            EntityType.COLLECTION -> syncedCollections.add(change.entityId)
                        }
                        _events.emit(SyncEvent.ItemSynced(change.entityType, change.entityId))
                    }
                } catch (e: Exception) {
                    errors.add(SyncError(change.entityId, "UPLOAD_FAILED", e.message ?: "Unknown error"))
                    _events.emit(SyncEvent.Error(errors.last()))
                }
            }
            
            // Download remote changes
            _syncStatus.value = _syncStatus.value.copy(syncState = SyncState.DOWNLOADING)
            val remoteChanges = syncRepository.getRemoteChanges(_syncStatus.value.lastSyncTime)
            
            for (remoteChange in remoteChanges) {
                try {
                    val localChange = changeTracker.getChange(remoteChange.entityType, remoteChange.entityId)
                    
                    if (localChange != null && !localChange.synced) {
                        // Conflict detected
                        val conflict = SyncConflict(
                            id = "conflict_${currentTimeToLong()}",
                            pluginId = remoteChange.entityId,
                            conflictType = when (remoteChange.entityType) {
                                EntityType.PLUGIN -> ConflictType.PLUGIN_VERSION_MISMATCH
                                EntityType.SETTING -> ConflictType.SETTING_CHANGED
                                EntityType.PIPELINE -> ConflictType.PIPELINE_MODIFIED
                                EntityType.COLLECTION -> ConflictType.COLLECTION_MODIFIED
                            },
                            localValue = localChange.newValue ?: "",
                            remoteValue = remoteChange.newValue ?: "",
                            localTimestamp = localChange.timestamp,
                            remoteTimestamp = remoteChange.timestamp,
                            deviceId = _syncStatus.value.deviceId
                        )
                        detectedConflicts.add(conflict)
                        _events.emit(SyncEvent.ConflictDetected(conflict))
                    } else {
                        // Apply remote change
                        applyRemoteChange(remoteChange)
                        when (remoteChange.entityType) {
                            EntityType.PLUGIN -> syncedPlugins.add(remoteChange.entityId)
                            EntityType.SETTING -> syncedSettings.add(remoteChange.entityId)
                            EntityType.PIPELINE -> syncedPipelines.add(remoteChange.entityId)
                            EntityType.COLLECTION -> syncedCollections.add(remoteChange.entityId)
                        }
                        _events.emit(SyncEvent.ItemSynced(remoteChange.entityType, remoteChange.entityId))
                    }
                } catch (e: Exception) {
                    errors.add(SyncError(remoteChange.entityId, "DOWNLOAD_FAILED", e.message ?: "Unknown error"))
                    _events.emit(SyncEvent.Error(errors.last()))
                }
            }
            
            // Resolve conflicts if auto-resolution is enabled
            if (config.conflictResolution != ConflictResolution.MANUAL) {
                _syncStatus.value = _syncStatus.value.copy(syncState = SyncState.RESOLVING_CONFLICTS)
                for (conflict in detectedConflicts.toList()) {
                    val resolved = resolveConflict(conflict, config.conflictResolution)
                    if (resolved) {
                        detectedConflicts.remove(conflict)
                        _events.emit(SyncEvent.ConflictResolved(conflict.id, config.conflictResolution))
                    }
                }
            }
            
            _conflicts.value = detectedConflicts
            
        } catch (e: Exception) {
            errors.add(SyncError(null, "SYNC_FAILED", e.message ?: "Unknown error"))
            _syncStatus.value = _syncStatus.value.copy(syncState = SyncState.ERROR)
        }
        
        val duration = currentTimeToLong() - startTime
        val result = SyncResult(
            success = errors.isEmpty(),
            syncedPlugins = syncedPlugins.size,
            syncedSettings = syncedSettings.size,
            syncedPipelines = syncedPipelines.size,
            syncedCollections = syncedCollections.size,
            conflicts = detectedConflicts,
            errors = errors,
            duration = duration,
            timestamp = currentTimeToLong()
        )
        
        _syncStatus.value = _syncStatus.value.copy(
            syncState = if (errors.isEmpty()) SyncState.IDLE else SyncState.ERROR,
            lastSyncTime = currentTimeToLong(),
            pendingChanges = changeTracker.getPendingChanges().size,
            conflictCount = detectedConflicts.size
        )
        
        _events.emit(SyncEvent.Completed(result))
        return result
    }
    
    /**
     * Create a backup.
     */
    suspend fun createBackup(): PluginBackup {
        val plugins = pluginManager.pluginsFlow.value.map { info ->
            PluginBackupEntry(
                pluginId = info.id,
                pluginName = info.manifest.name,
                version = info.manifest.version,
                versionCode = info.manifest.versionCode,
                repositoryUrl = info.repositoryUrl,
                isEnabled = info.status == ireader.domain.plugins.PluginStatus.ENABLED,
                installedAt = info.installDate ?: 0L
            )
        }
        
        val settings = syncRepository.getAllPluginSettings()
        val pipelines = syncRepository.getAllPipelines()
        val collections = syncRepository.getAllCollections()
        
        return PluginBackup(
            id = "backup_${currentTimeToLong()}",
            createdAt = currentTimeToLong(),
            deviceId = _syncStatus.value.deviceId,
            deviceName = _syncStatus.value.deviceName,
            plugins = plugins,
            settings = settings,
            pipelines = pipelines,
            collections = collections
        )
    }
    
    /**
     * Restore from a backup.
     */
    suspend fun restoreBackup(backup: PluginBackup): Result<Unit> {
        return try {
            // Restore plugins
            for (pluginEntry in backup.plugins) {
                if (!config.excludedPlugins.contains(pluginEntry.pluginId)) {
                    syncRepository.restorePlugin(pluginEntry)
                }
            }
            
            // Restore settings
            for ((pluginId, settingsBackup) in backup.settings) {
                if (!config.excludedPlugins.contains(pluginId)) {
                    syncRepository.restoreSettings(settingsBackup)
                }
            }
            
            // Restore pipelines
            for (pipeline in backup.pipelines) {
                syncRepository.restorePipeline(pipeline)
            }
            
            // Restore collections
            for (collection in backup.collections) {
                syncRepository.restoreCollection(collection)
            }
            
            // Reload plugins
            pluginManager.loadPlugins(forceReload = true)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Resolve a conflict manually.
     */
    suspend fun resolveConflictManually(conflictId: String, resolution: ConflictResolution): Result<Unit> {
        val conflict = _conflicts.value.find { it.id == conflictId }
            ?: return Result.failure(IllegalArgumentException("Conflict not found"))
        
        return try {
            resolveConflict(conflict, resolution)
            _conflicts.value = _conflicts.value.filter { it.id != conflictId }
            _events.emit(SyncEvent.ConflictResolved(conflictId, resolution))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun resolveConflict(conflict: SyncConflict, resolution: ConflictResolution): Boolean {
        return when (resolution) {
            ConflictResolution.USE_LOCAL -> {
                syncRepository.forceUpload(conflict.pluginId, conflict.localValue)
                true
            }
            ConflictResolution.USE_REMOTE -> {
                syncRepository.applyRemoteValue(conflict.pluginId, conflict.remoteValue)
                true
            }
            ConflictResolution.USE_NEWEST -> {
                if (conflict.localTimestamp > conflict.remoteTimestamp) {
                    syncRepository.forceUpload(conflict.pluginId, conflict.localValue)
                } else {
                    syncRepository.applyRemoteValue(conflict.pluginId, conflict.remoteValue)
                }
                true
            }
            ConflictResolution.MERGE -> {
                // Merge logic depends on the type
                false // Not implemented for all types
            }
            ConflictResolution.MANUAL -> false
        }
    }
    
    private suspend fun applyRemoteChange(change: SyncChange) {
        when (change.changeType) {
            ChangeType.CREATED, ChangeType.UPDATED -> {
                change.newValue?.let { value ->
                    syncRepository.applyRemoteValue(change.entityId, value)
                }
            }
            ChangeType.DELETED -> {
                syncRepository.deleteEntity(change.entityType, change.entityId)
            }
        }
    }
}

/**
 * Repository interface for sync operations.
 */
interface PluginSyncRepository {
    suspend fun uploadChange(change: SyncChange): SyncConflict?
    suspend fun getRemoteChanges(since: Long?): List<SyncChange>
    suspend fun forceUpload(entityId: String, value: String)
    suspend fun applyRemoteValue(entityId: String, value: String)
    suspend fun deleteEntity(entityType: EntityType, entityId: String)
    suspend fun getAllPluginSettings(): Map<String, PluginSettingsBackup>
    suspend fun getAllPipelines(): List<PipelineBackupEntry>
    suspend fun getAllCollections(): List<CollectionBackupEntry>
    suspend fun restorePlugin(entry: PluginBackupEntry)
    suspend fun restoreSettings(backup: PluginSettingsBackup)
    suspend fun restorePipeline(entry: PipelineBackupEntry)
    suspend fun restoreCollection(entry: CollectionBackupEntry)
}

/**
 * Interface for tracking local changes.
 */
interface ChangeTracker {
    suspend fun trackChange(change: SyncChange)
    suspend fun getPendingChanges(): List<SyncChange>
    suspend fun getChange(entityType: EntityType, entityId: String): SyncChange?
    suspend fun markSynced(changeId: String)
    suspend fun clearSyncedChanges()
}
