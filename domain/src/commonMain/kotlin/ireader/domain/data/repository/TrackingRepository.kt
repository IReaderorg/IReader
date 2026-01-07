package ireader.domain.data.repository

import ireader.domain.models.entities.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing external tracking service integration
 */
interface TrackingRepository {
    
    // Service management
    suspend fun getAvailableServices(): List<TrackerService>
    suspend fun getEnabledServices(): List<TrackerService>
    suspend fun enableService(serviceId: Int): Boolean
    suspend fun disableService(serviceId: Int): Boolean
    
    // Authentication
    suspend fun authenticate(serviceId: Int, credentials: TrackerCredentials): Boolean
    suspend fun refreshToken(serviceId: Int): Boolean
    suspend fun logout(serviceId: Int): Boolean
    suspend fun isAuthenticated(serviceId: Int): Boolean
    suspend fun getCredentials(serviceId: Int): TrackerCredentials?
    
    // Track management
    suspend fun getTracksByBook(bookId: Long): List<Track>
    suspend fun getTracksByService(serviceId: Int): List<Track>
    fun getTracksByBookAsFlow(bookId: Long): Flow<List<Track>>
    suspend fun addTrack(track: Track): Boolean
    suspend fun updateTrack(update: TrackUpdate): Boolean
    suspend fun removeTrack(bookId: Long, serviceId: Int): Boolean
    
    // Search and linking
    suspend fun searchTracker(serviceId: Int, query: String): List<TrackSearchResult>
    suspend fun linkBook(bookId: Long, serviceId: Int, searchResult: TrackSearchResult): Boolean
    suspend fun unlinkBook(bookId: Long, serviceId: Int): Boolean
    
    // Synchronization
    suspend fun syncTrack(bookId: Long, serviceId: Int): Boolean
    suspend fun syncAllTracks(bookId: Long): Boolean
    suspend fun batchSync(bookIds: List<Long>): Map<Long, Boolean>
    fun getSyncStatus(bookId: Long): Flow<List<TrackingSyncStatus>>
    
    // Batch operations
    suspend fun batchOperation(operation: BatchTrackingOperation): Map<Long, Boolean>
    suspend fun updateReadingProgress(bookId: Long, chaptersRead: Int): Boolean
    suspend fun updateStatus(bookId: Long, status: TrackStatus): Boolean
    suspend fun updateScore(bookId: Long, score: Float): Boolean
    
    // Statistics
    suspend fun getTrackingStatistics(): TrackingStatistics
    
    // MyNovelList specific
    fun getMyNovelListBaseUrl(): String
    fun setMyNovelListBaseUrl(url: String)
}

/**
 * Tracking statistics
 */
data class TrackingStatistics(
    val totalTrackedBooks: Int = 0,
    val trackedByService: Map<Int, Int> = emptyMap(),
    val syncSuccessRate: Float = 0f,
    val lastSyncTime: Long = 0,
    val pendingSyncs: Int = 0,
    val averageScore: Float = 0f,
    val statusDistribution: Map<TrackStatus, Int> = emptyMap()
)