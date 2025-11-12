package ireader.domain.usecases.remote

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.ConnectionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Use case for observing the connection status to the remote backend
 * 
 * Requirements: 8.1, 8.2
 */
class ObserveConnectionStatusUseCase(
    private val remoteRepository: RemoteRepository
) {
    /**
     * Observe connection status updates
     * @return Flow emitting ConnectionStatus updates
     */
    operator fun invoke(): Flow<ConnectionStatus> {
        return remoteRepository.observeConnectionStatus()
    }
}
