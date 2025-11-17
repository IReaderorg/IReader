package ireader.domain.use_cases.library

import ireader.domain.data.repository.TrackingRepository
import ireader.domain.models.entities.*
import kotlinx.coroutines.flow.Flow

/**
 * Use case for managing external tracking service integration
 */
class TrackingUseCase(
    private val trackingRepository: TrackingRepository
) {
    
    /**
     * Get all available tracking services
     */
    suspend fun getAvailableServices(): List<TrackerService> {
        return trackingRepository.getAvailableServices()
    }
    
    /**
     * Get enabled tracking services
     */
    suspend fun getEnabledServices(): List<TrackerService> {
        return trackingRepository.getEnabledServices()
    }
    
    /**
     * Enable a tracking service
     */
    suspend fun enableService(serviceId: Int): Boolean {
        return trackingRepository.enableService(serviceId)
    }
    
    /**
     * Disable a tracking service
     */
    suspend fun disableService(serviceId: Int): Boolean {
        return trackingRepository.disableService(serviceId)
    }
    
    /**
     * Authenticate with a tracking service
     */
    suspend fun authenticate(serviceId: Int, credentials: TrackerCredentials): Boolean {
        return trackingRepository.authenticate(serviceId, credentials)
    }
    
    /**
     * Check if authenticated with a service
     */
    suspend fun isAuthenticated(serviceId: Int): Boolean {
        return trackingRepository.isAuthenticated(serviceId)
    }
    
    /**
     * Logout from a tracking service
     */
    suspend fun logout(serviceId: Int): Boolean {
        return trackingRepository.logout(serviceId)
    }
    
    /**
     * Get tracks for a book
     */
    suspend fun getTracksForBook(bookId: Long): List<Track> {
        return trackingRepository.getTracksByBook(bookId)
    }
    
    /**
     * Get tracks for a book as Flow
     */
    fun getTracksForBookAsFlow(bookId: Long): Flow<List<Track>> {
        return trackingRepository.getTracksByBookAsFlow(bookId)
    }
    
    /**
     * Search for a book on a tracking service
     */
    suspend fun searchTracker(serviceId: Int, query: String): List<TrackSearchResult> {
        return trackingRepository.searchTracker(serviceId, query)
    }
    
    /**
     * Link a book to a tracking service
     */
    suspend fun linkBook(bookId: Long, serviceId: Int, searchResult: TrackSearchResult): Boolean {
        return trackingRepository.linkBook(bookId, serviceId, searchResult)
    }
    
    /**
     * Unlink a book from a tracking service
     */
    suspend fun unlinkBook(bookId: Long, serviceId: Int): Boolean {
        return trackingRepository.unlinkBook(bookId, serviceId)
    }
    
    /**
     * Sync a book's tracking data
     */
    suspend fun syncBook(bookId: Long, serviceId: Int): Boolean {
        return trackingRepository.syncTrack(bookId, serviceId)
    }
    
    /**
     * Sync all tracking services for a book
     */
    suspend fun syncAllTracking(bookId: Long): Boolean {
        return trackingRepository.syncAllTracks(bookId)
    }
    
    /**
     * Batch sync multiple books
     */
    suspend fun batchSync(bookIds: List<Long>): Map<Long, Boolean> {
        return trackingRepository.batchSync(bookIds)
    }
    
    /**
     * Update reading progress for a book
     */
    suspend fun updateReadingProgress(bookId: Long, chaptersRead: Int): Boolean {
        return trackingRepository.updateReadingProgress(bookId, chaptersRead)
    }
    
    /**
     * Update reading status for a book
     */
    suspend fun updateStatus(bookId: Long, status: TrackStatus): Boolean {
        return trackingRepository.updateStatus(bookId, status)
    }
    
    /**
     * Update score for a book
     */
    suspend fun updateScore(bookId: Long, score: Float): Boolean {
        return trackingRepository.updateScore(bookId, score)
    }
    
    /**
     * Get sync status for a book
     */
    fun getSyncStatus(bookId: Long): Flow<List<TrackingSyncStatus>> {
        return trackingRepository.getSyncStatus(bookId)
    }
    
    /**
     * Perform batch tracking operation
     */
    suspend fun batchOperation(operation: BatchTrackingOperation): Map<Long, Boolean> {
        return trackingRepository.batchOperation(operation)
    }
    
    /**
     * Get tracking statistics
     */
    suspend fun getTrackingStatistics(): TrackingStatistics {
        return trackingRepository.getTrackingStatistics()
    }
    
    /**
     * Update track information
     */
    suspend fun updateTrack(update: TrackUpdate): Boolean {
        return trackingRepository.updateTrack(update)
    }
    
    /**
     * Add a new track
     */
    suspend fun addTrack(track: Track): Boolean {
        return trackingRepository.addTrack(track)
    }
    
    /**
     * Remove a track
     */
    suspend fun removeTrack(bookId: Long, serviceId: Int): Boolean {
        return trackingRepository.removeTrack(bookId, serviceId)
    }
}