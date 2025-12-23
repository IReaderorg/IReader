package ireader.data.repository

import ireader.data.core.DatabaseHandler
import ireader.data.tracking.anilist.AniListRepositoryImpl
import ireader.domain.data.repository.TrackingRepository
import ireader.domain.data.repository.TrackingStatistics
import ireader.domain.models.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementation of TrackingRepository with AniList integration and database persistence.
 */
class TrackingRepositoryImpl(
    private val handler: DatabaseHandler,
    private val aniListRepository: AniListRepositoryImpl
) : TrackingRepository {
    
    private val _syncStatusFlows = mutableMapOf<Long, MutableStateFlow<List<TrackingSyncStatus>>>()
    
    override suspend fun getAvailableServices(): List<TrackerService> {
        return TrackerService.services
    }
    
    override suspend fun getEnabledServices(): List<TrackerService> {
        return TrackerService.services.filter { service ->
            when (service.id) {
                TrackerService.ANILIST -> aniListRepository.isAuthenticated()
                else -> false // Other services not implemented yet
            }
        }
    }
    
    override suspend fun enableService(serviceId: Int): Boolean {
        // Services are enabled by authenticating
        return true
    }
    
    override suspend fun disableService(serviceId: Int): Boolean {
        return when (serviceId) {
            TrackerService.ANILIST -> {
                aniListRepository.logout()
                true
            }
            else -> false
        }
    }
    
    override suspend fun authenticate(serviceId: Int, credentials: TrackerCredentials): Boolean {
        return when (serviceId) {
            TrackerService.ANILIST -> {
                aniListRepository.login(credentials.accessToken)
            }
            else -> false
        }
    }
    
    override suspend fun refreshToken(serviceId: Int): Boolean {
        // AniList tokens don't need refresh (1 year validity)
        return true
    }
    
    override suspend fun logout(serviceId: Int): Boolean {
        return when (serviceId) {
            TrackerService.ANILIST -> {
                aniListRepository.logout()
                true
            }
            else -> false
        }
    }
    
    override suspend fun isAuthenticated(serviceId: Int): Boolean {
        return when (serviceId) {
            TrackerService.ANILIST -> aniListRepository.isAuthenticated()
            else -> false
        }
    }
    
    override suspend fun getCredentials(serviceId: Int): TrackerCredentials? {
        return when (serviceId) {
            TrackerService.ANILIST -> {
                if (aniListRepository.isAuthenticated()) {
                    TrackerCredentials(
                        serviceId = serviceId,
                        accessToken = "", // Don't expose token
                        username = aniListRepository.getUserId().toString(),
                        isValid = true
                    )
                } else null
            }
            else -> null
        }
    }
    
    override suspend fun getTracksByBook(bookId: Long): List<Track> {
        return handler.awaitList {
            trackQueries.getTracksByBookId(bookId, ::mapTrack)
        }
    }
    
    override suspend fun getTracksByService(serviceId: Int): List<Track> {
        return handler.awaitList {
            trackQueries.getTracksBySiteId(serviceId, ::mapTrack)
        }
    }
    
    override fun getTracksByBookAsFlow(bookId: Long): Flow<List<Track>> {
        return handler.subscribeToList {
            trackQueries.getTracksByBookId(bookId, ::mapTrack)
        }
    }
    
    override suspend fun addTrack(track: Track): Boolean {
        return try {
            handler.await {
                trackQueries.upsertTrack(
                    mangaId = track.mangaId,
                    siteId = track.siteId,
                    entryId = track.entryId,
                    mediaId = track.mediaId,
                    mediaUrl = track.mediaUrl,
                    title = track.title,
                    lastRead = track.lastRead,
                    totalChapters = track.totalChapters,
                    score = track.score,
                    status = track.status.value,
                    startReadTime = track.startReadTime,
                    endReadTime = track.endReadTime
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun updateTrack(update: TrackUpdate): Boolean {
        val existingTrack = handler.awaitOneOrNull {
            trackQueries.getTrackById(update.id, ::mapTrack)
        } ?: return false
        
        val updatedTrack = existingTrack.copy(
            entryId = update.entryId ?: existingTrack.entryId,
            mediaId = update.mediaId ?: existingTrack.mediaId,
            mediaUrl = update.mediaUrl ?: existingTrack.mediaUrl,
            title = update.title ?: existingTrack.title,
            lastRead = update.lastRead ?: existingTrack.lastRead,
            totalChapters = update.totalChapters ?: existingTrack.totalChapters,
            score = update.score ?: existingTrack.score,
            status = update.status ?: existingTrack.status,
            startReadTime = update.startReadTime ?: existingTrack.startReadTime,
            endReadTime = update.endReadTime ?: existingTrack.endReadTime
        )
        
        // Update in database
        handler.await {
            trackQueries.updateTrack(
                id = update.id,
                entryId = updatedTrack.entryId,
                mediaId = updatedTrack.mediaId,
                mediaUrl = updatedTrack.mediaUrl,
                title = updatedTrack.title,
                lastRead = updatedTrack.lastRead,
                totalChapters = updatedTrack.totalChapters,
                score = updatedTrack.score,
                status = updatedTrack.status.value,
                startReadTime = updatedTrack.startReadTime,
                endReadTime = updatedTrack.endReadTime
            )
        }
        
        // Sync to remote service
        return when (existingTrack.siteId) {
            TrackerService.ANILIST -> aniListRepository.updateTrack(updatedTrack)
            else -> true
        }
    }
    
    override suspend fun removeTrack(bookId: Long, serviceId: Int): Boolean {
        val track = handler.awaitOneOrNull {
            trackQueries.getTrack(bookId, serviceId, ::mapTrack)
        }
        
        // Remove from remote
        if (track != null && serviceId == TrackerService.ANILIST && track.entryId > 0) {
            aniListRepository.deleteTrack(track.entryId)
        }
        
        // Remove from database
        handler.await {
            trackQueries.deleteTrack(bookId, serviceId)
        }
        return true
    }
    
    override suspend fun searchTracker(serviceId: Int, query: String): List<TrackSearchResult> {
        return when (serviceId) {
            TrackerService.ANILIST -> aniListRepository.search(query)
            else -> emptyList()
        }
    }
    
    override suspend fun linkBook(bookId: Long, serviceId: Int, searchResult: TrackSearchResult): Boolean {
        return when (serviceId) {
            TrackerService.ANILIST -> {
                val track = aniListRepository.bindBook(bookId, searchResult)
                if (track != null) {
                    addTrack(track)
                    true
                } else false
            }
            else -> false
        }
    }
    
    override suspend fun unlinkBook(bookId: Long, serviceId: Int): Boolean {
        return removeTrack(bookId, serviceId)
    }
    
    override suspend fun syncTrack(bookId: Long, serviceId: Int): Boolean {
        val track = handler.awaitOneOrNull {
            trackQueries.getTrack(bookId, serviceId, ::mapTrack)
        } ?: return false
        
        return when (serviceId) {
            TrackerService.ANILIST -> {
                val synced = aniListRepository.syncTrack(track)
                if (synced != null) {
                    // Update local database with remote data
                    addTrack(synced)
                    true
                } else false
            }
            else -> false
        }
    }
    
    override suspend fun syncAllTracks(bookId: Long): Boolean {
        val tracks = getTracksByBook(bookId)
        return tracks.all { track ->
            syncTrack(bookId, track.siteId)
        }
    }
    
    override suspend fun batchSync(bookIds: List<Long>): Map<Long, Boolean> {
        return bookIds.associateWith { bookId ->
            try {
                syncAllTracks(bookId)
            } catch (e: Exception) {
                false
            }
        }
    }
    
    override fun getSyncStatus(bookId: Long): Flow<List<TrackingSyncStatus>> {
        return _syncStatusFlows.getOrPut(bookId) {
            MutableStateFlow(emptyList())
        }.asStateFlow()
    }
    
    override suspend fun batchOperation(operation: BatchTrackingOperation): Map<Long, Boolean> {
        return operation.bookIds.associateWith { bookId ->
            try {
                when (operation.operation) {
                    TrackingOperation.SYNC_PROGRESS -> syncAllTracks(bookId)
                    TrackingOperation.UPDATE_STATUS -> true
                    TrackingOperation.UPDATE_SCORE -> true
                    TrackingOperation.REMOVE_TRACKING -> {
                        operation.serviceIds.ifEmpty { 
                            getEnabledServices().map { it.id } 
                        }.all { serviceId ->
                            removeTrack(bookId, serviceId)
                        }
                    }
                }
            } catch (e: Exception) {
                false
            }
        }
    }
    
    override suspend fun updateReadingProgress(bookId: Long, chaptersRead: Int): Boolean {
        val tracks = getTracksByBook(bookId)
        return tracks.all { track ->
            val updatedTrack = track.copy(lastRead = chaptersRead.toFloat())
            
            // Update local database
            addTrack(updatedTrack)
            
            // Sync to remote
            when (track.siteId) {
                TrackerService.ANILIST -> aniListRepository.updateTrack(updatedTrack)
                else -> true
            }
        }
    }
    
    override suspend fun updateStatus(bookId: Long, status: TrackStatus): Boolean {
        val tracks = getTracksByBook(bookId)
        return tracks.all { track ->
            val updatedTrack = track.copy(status = status)
            
            // Update local database
            addTrack(updatedTrack)
            
            // Sync to remote
            when (track.siteId) {
                TrackerService.ANILIST -> aniListRepository.updateTrack(updatedTrack)
                else -> true
            }
        }
    }
    
    override suspend fun updateScore(bookId: Long, score: Float): Boolean {
        val tracks = getTracksByBook(bookId)
        return tracks.all { track ->
            val updatedTrack = track.copy(score = score)
            
            // Update local database
            addTrack(updatedTrack)
            
            // Sync to remote
            when (track.siteId) {
                TrackerService.ANILIST -> aniListRepository.updateTrack(updatedTrack)
                else -> true
            }
        }
    }
    
    override suspend fun getTrackingStatistics(): TrackingStatistics {
        val allTracks = handler.awaitList {
            trackQueries.getAllTracks(::mapTrack)
        }
        val trackCount = handler.awaitOne {
            trackQueries.getTrackCount()
        }
        
        return TrackingStatistics(
            totalTrackedBooks = trackCount.toInt(),
            trackedByService = allTracks.groupBy { it.siteId }.mapValues { it.value.size },
            syncSuccessRate = 1f,
            lastSyncTime = 0,
            pendingSyncs = 0,
            averageScore = if (allTracks.isNotEmpty()) allTracks.map { it.score }.average().toFloat() else 0f,
            statusDistribution = allTracks.groupBy { it.status }.mapValues { it.value.size }
        )
    }
    
    // AniList-specific methods
    fun getAniListAuthUrl(): String = aniListRepository.getAuthUrl()
    
    suspend fun loginToAniList(accessToken: String): Boolean = aniListRepository.login(accessToken)
    
    fun logoutFromAniList() = aniListRepository.logout()
    
    fun isAniListAuthenticated(): Boolean = aniListRepository.isAuthenticated()
    
    // Helper function to map database row to Track entity
    private fun mapTrack(
        id: Long,
        mangaId: Long,
        siteId: Int,
        entryId: Long,
        mediaId: Long,
        mediaUrl: String,
        title: String,
        lastRead: Float,
        totalChapters: Int,
        score: Float,
        status: Int,
        startReadTime: Long,
        endReadTime: Long
    ): Track {
        return Track(
            id = id,
            mangaId = mangaId,
            siteId = siteId,
            entryId = entryId,
            mediaId = mediaId,
            mediaUrl = mediaUrl,
            title = title,
            lastRead = lastRead,
            totalChapters = totalChapters,
            score = score,
            status = TrackStatus.from(status),
            startReadTime = startReadTime,
            endReadTime = endReadTime
        )
    }
}
