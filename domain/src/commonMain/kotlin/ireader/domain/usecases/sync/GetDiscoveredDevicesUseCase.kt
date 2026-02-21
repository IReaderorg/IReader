package ireader.domain.usecases.sync

import ireader.domain.models.sync.DiscoveredDevice
import ireader.domain.repositories.SyncRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing discovered devices on the local network.
 *
 * @property syncRepository Repository for sync operations
 */
class GetDiscoveredDevicesUseCase(
    private val syncRepository: SyncRepository
) {
    /**
     * Observe the list of discovered devices.
     *
     * @return Flow of discovered device lists
     */
    operator fun invoke(): Flow<List<DiscoveredDevice>> {
        return syncRepository.observeDiscoveredDevices()
    }
}
