package ireader.data.sync.datasource

import ireader.domain.models.sync.BookSyncData
import ireader.domain.models.sync.BookmarkData
import ireader.domain.models.sync.ReadingProgressData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake implementation of SyncLocalDataSource for testing.
 */
class FakeSyncLocalDataSource : SyncLocalDataSource {
    
    private val syncMetadata = mutableMapOf<String, SyncMetadataEntity>()
    private val trustedDevices = mutableMapOf<String, TrustedDeviceEntity>()
    private val syncLogs = mutableListOf<SyncLogEntity>()
    private val books = mutableListOf<BookSyncData>()
    private val progress = mutableListOf<ReadingProgressData>()
    private val bookmarks = mutableListOf<BookmarkData>()
    
    private val _activeTrustedDevices = MutableStateFlow<List<TrustedDeviceEntity>>(emptyList())
    private val _syncLogsByDevice = mutableMapOf<String, MutableStateFlow<List<SyncLogEntity>>>()
    
    override suspend fun getSyncMetadata(deviceId: String): SyncMetadataEntity? {
        return syncMetadata[deviceId]
    }
    
    override suspend fun upsertSyncMetadata(metadata: SyncMetadataEntity) {
        syncMetadata[metadata.deviceId] = metadata
    }
    
    override suspend fun deleteSyncMetadata(deviceId: String) {
        syncMetadata.remove(deviceId)
    }
    
    override suspend fun getTrustedDevice(deviceId: String): TrustedDeviceEntity? {
        return trustedDevices[deviceId]
    }
    
    override suspend fun upsertTrustedDevice(device: TrustedDeviceEntity) {
        trustedDevices[device.deviceId] = device
        updateActiveTrustedDevices()
    }
    
    override fun getActiveTrustedDevices(): Flow<List<TrustedDeviceEntity>> {
        return _activeTrustedDevices.asStateFlow()
    }
    
    override suspend fun deactivateTrustedDevice(deviceId: String) {
        trustedDevices[deviceId]?.let {
            trustedDevices[deviceId] = it.copy(isActive = false)
            updateActiveTrustedDevices()
        }
    }
    
    override suspend fun updateDeviceExpiration(deviceId: String, expiresAt: Long) {
        trustedDevices[deviceId]?.let {
            trustedDevices[deviceId] = it.copy(expiresAt = expiresAt)
        }
    }
    
    override suspend fun deleteTrustedDevice(deviceId: String) {
        trustedDevices.remove(deviceId)
        updateActiveTrustedDevices()
    }
    
    override suspend fun updateCertificateFingerprint(deviceId: String, fingerprint: String?) {
        trustedDevices[deviceId]?.let {
            trustedDevices[deviceId] = it.copy(certificateFingerprint = fingerprint)
        }
    }
    
    override suspend fun getCertificateFingerprint(deviceId: String): String? {
        return trustedDevices[deviceId]?.certificateFingerprint
    }
    
    override suspend fun insertSyncLog(log: SyncLogEntity) {
        syncLogs.add(log)
        val deviceFlow = _syncLogsByDevice.getOrPut(log.deviceId) {
            MutableStateFlow(emptyList())
        }
        deviceFlow.value = syncLogs.filter { it.deviceId == log.deviceId }
    }
    
    override suspend fun getSyncLogById(id: Long): SyncLogEntity? {
        return syncLogs.find { it.id == id }
    }
    
    override fun getSyncLogsByDevice(deviceId: String): Flow<List<SyncLogEntity>> {
        return _syncLogsByDevice.getOrPut(deviceId) {
            MutableStateFlow(syncLogs.filter { it.deviceId == deviceId })
        }
    }
    
    // Test helper methods
    fun addSyncMetadata(metadata: SyncMetadataEntity) {
        syncMetadata[metadata.deviceId] = metadata
    }
    
    fun addBook(book: BookSyncData) {
        books.add(book)
    }
    
    override suspend fun getBooks(): List<BookSyncData> = books.toList()
    
    fun addProgress(progressData: ReadingProgressData) {
        progress.add(progressData)
    }
    
    override suspend fun getProgress(): List<ReadingProgressData> = progress.toList()
    
    fun addBookmark(bookmark: BookmarkData) {
        bookmarks.add(bookmark)
    }
    
    override suspend fun getBookmarks(): List<BookmarkData> = bookmarks.toList()
    
    fun addTrustedDevice(device: TrustedDeviceEntity) {
        trustedDevices[device.deviceId] = device
        updateActiveTrustedDevices()
    }
    
    private fun updateActiveTrustedDevices() {
        _activeTrustedDevices.value = trustedDevices.values.filter { it.isActive }
    }
    
    fun clear() {
        syncMetadata.clear()
        trustedDevices.clear()
        syncLogs.clear()
        books.clear()
        progress.clear()
        bookmarks.clear()
        _activeTrustedDevices.value = emptyList()
        _syncLogsByDevice.clear()
    }
}
