package ireader.data.sync.repository

import ireader.data.sync.datasource.SyncLocalDataSource
import ireader.data.sync.datasource.TrustedDeviceEntity
import ireader.domain.models.sync.TrustedDevice
import ireader.domain.repositories.TrustedDeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of TrustedDeviceRepository using SyncLocalDataSource.
 * 
 * This repository provides access to trusted device data stored in the local database.
 * It maps between domain models (TrustedDevice) and data entities (TrustedDeviceEntity).
 * 
 * @param syncLocalDataSource Data source for accessing trusted device data
 */
class TrustedDeviceRepositoryImpl(
    private val syncLocalDataSource: SyncLocalDataSource
) : TrustedDeviceRepository {
    
    override suspend fun getTrustedDevice(deviceId: String): TrustedDevice? {
        return syncLocalDataSource.getTrustedDevice(deviceId)?.toDomain()
    }
    
    override suspend fun upsertTrustedDevice(device: TrustedDevice) {
        syncLocalDataSource.upsertTrustedDevice(device.toEntity())
    }
    
    override fun getActiveTrustedDevices(): Flow<List<TrustedDevice>> {
        return syncLocalDataSource.getActiveTrustedDevices()
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    override suspend fun deactivateTrustedDevice(deviceId: String) {
        syncLocalDataSource.deactivateTrustedDevice(deviceId)
    }
    
    override suspend fun updateDeviceExpiration(deviceId: String, expiresAt: Long) {
        syncLocalDataSource.updateDeviceExpiration(deviceId, expiresAt)
    }
    
    override suspend fun deleteTrustedDevice(deviceId: String) {
        syncLocalDataSource.deleteTrustedDevice(deviceId)
    }
    
    // ========== Mapping Functions ==========
    
    private fun TrustedDeviceEntity.toDomain(): TrustedDevice {
        return TrustedDevice(
            deviceId = deviceId,
            deviceName = deviceName,
            pairedAt = pairedAt,
            expiresAt = expiresAt,
            isActive = isActive
        )
    }
    
    private fun TrustedDevice.toEntity(): TrustedDeviceEntity {
        return TrustedDeviceEntity(
            deviceId = deviceId,
            deviceName = deviceName,
            pairedAt = pairedAt,
            expiresAt = expiresAt,
            isActive = isActive
        )
    }
}
