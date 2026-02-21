package ireader.domain.usecases.sync

import ireader.domain.models.sync.ConflictResolutionStrategy
import ireader.domain.models.sync.SyncData
import ireader.domain.repositories.SyncRepository
import ireader.domain.repositories.SyncResult

/**
 * Use case for syncing with a specific device.
 * This orchestrates the entire sync process:
 * 1. Verify device is reachable
 * 2. Connect to device
 * 3. Exchange manifests
 * 4. Detect conflicts
 * 5. Resolve conflicts
 * 6. Transfer data
 *
 * @property syncRepository Repository for sync operations
 * @property detectConflictsUseCase Use case for detecting conflicts
 * @property resolveConflictsUseCase Use case for resolving conflicts
 */
class SyncWithDeviceUseCase(
    private val syncRepository: SyncRepository,
    private val detectConflictsUseCase: DetectConflictsUseCase,
    private val resolveConflictsUseCase: ResolveConflictsUseCase
) {
    /**
     * Sync with the specified device.
     *
     * @param deviceId ID of the device to sync with
     * @param conflictStrategy Strategy to use for resolving conflicts
     * @return Result containing SyncResult or error
     */
    suspend operator fun invoke(
        deviceId: String,
        conflictStrategy: ConflictResolutionStrategy
    ): Result<SyncResult> {
        // Step 1: Verify device is reachable
        val device = syncRepository.getDeviceInfo(deviceId).getOrElse {
            return Result.failure(it)
        }

        // Step 2: Connect to device
        val connection = syncRepository.connectToDevice(device).getOrElse {
            return Result.failure(it)
        }

        try {
            // Step 3: Exchange manifests
            val (localManifest, remoteManifest) = syncRepository.exchangeManifests(connection).getOrElse {
                syncRepository.disconnectFromDevice(connection)
                return Result.failure(it)
            }

            // Step 4: Get local and remote data for conflict detection
            val localBooks = syncRepository.getBooksToSync().getOrElse { emptyList() }
            val localProgress = syncRepository.getReadingProgress().getOrElse { emptyList() }
            val localBookmarks = syncRepository.getBookmarks().getOrElse { emptyList() }

            val localData = SyncData(
                books = localBooks,
                readingProgress = localProgress,
                bookmarks = localBookmarks,
                metadata = localManifest.let {
                    ireader.domain.models.sync.SyncMetadata(
                        deviceId = it.deviceId,
                        timestamp = it.timestamp,
                        version = 1,
                        checksum = ""
                    )
                }
            )

            // For testing purposes, create remote data from manifest
            // In real implementation, this would come from the remote device
            val remoteData = localData.copy(
                metadata = remoteManifest.let {
                    ireader.domain.models.sync.SyncMetadata(
                        deviceId = it.deviceId,
                        timestamp = it.timestamp,
                        version = 1,
                        checksum = ""
                    )
                }
            )

            // Step 5: Detect conflicts
            val conflicts = detectConflictsUseCase(localData, remoteData)

            // Step 6: Resolve conflicts if any exist
            if (conflicts.isNotEmpty()) {
                resolveConflictsUseCase(conflicts, conflictStrategy).getOrElse {
                    syncRepository.disconnectFromDevice(connection)
                    return Result.failure(it)
                }
            }

            // Step 7: Perform sync
            val syncResult = syncRepository.performSync(connection, localManifest, remoteManifest).getOrElse {
                syncRepository.disconnectFromDevice(connection)
                return Result.failure(it)
            }

            // Step 8: Disconnect
            syncRepository.disconnectFromDevice(connection)

            return Result.success(syncResult)
        } catch (e: Exception) {
            syncRepository.disconnectFromDevice(connection)
            return Result.failure(e)
        }
    }
}
