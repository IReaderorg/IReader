package ireader.data.repository

import ireader.data.core.DatabaseHandler
import ireader.data.tracking.anilist.AniListRepositoryImpl
import ireader.data.tracking.kitsu.KitsuRepositoryImpl
import ireader.data.tracking.mal.MyAnimeListRepositoryImpl
import ireader.data.tracking.mangaupdates.MangaUpdatesRepositoryImpl
import ireader.data.tracking.mynovellist.MyNovelListRepositoryImpl
import ireader.domain.data.repository.TrackingRepository
import ireader.domain.data.repository.TrackingStatistics
import ireader.domain.models.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementation of TrackingRepository with full integration for:
 * - AniList (OAuth token)
 * - MyAnimeList (OAuth2 with PKCE)
 * - Kitsu (OAuth2 with username/password)
 * - MangaUpdates (Session-based with username/password)
 * - MyNovelList (API key authentication)
 */
class TrackingRepositoryImpl(
    private val handler: DatabaseHandler,
    private val aniListRepository: AniListRepositoryImpl,
    private val malRepository: MyAnimeListRepositoryImpl? = null,
    private val kitsuRepository: KitsuRepositoryImpl? = null,
    private val mangaUpdatesRepository: MangaUpdatesRepositoryImpl? = null,
    private val myNovelListRepository: MyNovelListRepositoryImpl? = null
) : TrackingRepository {
    
    private val _syncStatusFlows = mutableMapOf<Long, MutableStateFlow<List<TrackingSyncStatus>>>()
    
    override suspend fun getAvailableServices(): List<TrackerService> {
        return TrackerService.services
    }
    
    override suspend fun getEnabledServices(): List<TrackerService> {
        return TrackerService.services.filter { service ->
            when (service.id) {
                TrackerService.ANILIST -> aniListRepository.isAuthenticated()
                TrackerService.MYANIMELIST -> malRepository?.isAuthenticated() == true
                TrackerService.KITSU -> kitsuRepository?.isAuthenticated() == true
                TrackerService.MANGAUPDATES -> mangaUpdatesRepository?.isAuthenticated() == true
                TrackerService.MYNOVELLIST -> myNovelListRepository?.isAuthenticated() == true
                else -> false
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
            TrackerService.MYANIMELIST -> {
                malRepository?.logout()
                true
            }
            TrackerService.KITSU -> {
                kitsuRepository?.logout()
                true
            }
            TrackerService.MANGAUPDATES -> {
                mangaUpdatesRepository?.logout()
                true
            }
            TrackerService.MYNOVELLIST -> {
                myNovelListRepository?.logout()
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
            TrackerService.MYANIMELIST -> {
                // MAL uses OAuth code flow, token is passed after exchange
                malRepository?.loginWithToken(credentials.accessToken) == true
            }
            TrackerService.KITSU -> {
                // Kitsu uses username/password, but we can also restore with token
                kitsuRepository?.loginWithToken(credentials.accessToken) == true
            }
            TrackerService.MANGAUPDATES -> {
                // MangaUpdates uses session token
                mangaUpdatesRepository?.loginWithToken(credentials.accessToken) == true
            }
            TrackerService.MYNOVELLIST -> {
                // MyNovelList uses API key
                myNovelListRepository?.login(credentials.accessToken) == true
            }
            else -> false
        }
    }
    
    override suspend fun refreshToken(serviceId: Int): Boolean {
        return when (serviceId) {
            TrackerService.ANILIST -> true // AniList tokens don't need refresh (1 year validity)
            TrackerService.MYANIMELIST -> malRepository?.refreshTokenIfNeeded() == true
            TrackerService.KITSU -> true // Kitsu tokens are long-lived
            TrackerService.MANGAUPDATES -> true // Session-based
            else -> false
        }
    }
    
    override suspend fun logout(serviceId: Int): Boolean {
        return when (serviceId) {
            TrackerService.ANILIST -> {
                aniListRepository.logout()
                true
            }
            TrackerService.MYANIMELIST -> {
                malRepository?.logout()
                true
            }
            TrackerService.KITSU -> {
                kitsuRepository?.logout()
                true
            }
            TrackerService.MANGAUPDATES -> {
                mangaUpdatesRepository?.logout()
                true
            }
            TrackerService.MYNOVELLIST -> {
                myNovelListRepository?.logout()
                true
            }
            else -> false
        }
    }
    
    override suspend fun isAuthenticated(serviceId: Int): Boolean {
        return when (serviceId) {
            TrackerService.ANILIST -> aniListRepository.isAuthenticated()
            TrackerService.MYANIMELIST -> malRepository?.isAuthenticated() == true
            TrackerService.KITSU -> kitsuRepository?.isAuthenticated() == true
            TrackerService.MANGAUPDATES -> mangaUpdatesRepository?.isAuthenticated() == true
            TrackerService.MYNOVELLIST -> myNovelListRepository?.isAuthenticated() == true
            else -> false
        }
    }
    
    override suspend fun getCredentials(serviceId: Int): TrackerCredentials? {
        return when (serviceId) {
            TrackerService.ANILIST -> {
                if (aniListRepository.isAuthenticated()) {
                    TrackerCredentials(
                        serviceId = serviceId,
                        accessToken = "",
                        username = aniListRepository.getUserId().toString(),
                        isValid = true
                    )
                } else null
            }
            TrackerService.MYANIMELIST -> {
                if (malRepository?.isAuthenticated() == true) {
                    TrackerCredentials(
                        serviceId = serviceId,
                        accessToken = "",
                        username = malRepository.getUserId().toString(),
                        isValid = true
                    )
                } else null
            }
            TrackerService.KITSU -> {
                if (kitsuRepository?.isAuthenticated() == true) {
                    TrackerCredentials(
                        serviceId = serviceId,
                        accessToken = "",
                        username = kitsuRepository.getUserId(),
                        isValid = true
                    )
                } else null
            }
            TrackerService.MANGAUPDATES -> {
                if (mangaUpdatesRepository?.isAuthenticated() == true) {
                    TrackerCredentials(
                        serviceId = serviceId,
                        accessToken = "",
                        username = mangaUpdatesRepository.getUsername(),
                        isValid = true
                    )
                } else null
            }
            TrackerService.MYNOVELLIST -> {
                if (myNovelListRepository?.isAuthenticated() == true) {
                    TrackerCredentials(
                        serviceId = serviceId,
                        accessToken = "",
                        username = "MyNovelList User",
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
        return syncToRemote(updatedTrack)
    }
    
    override suspend fun removeTrack(bookId: Long, serviceId: Int): Boolean {
        val track = handler.awaitOneOrNull {
            trackQueries.getTrack(bookId, serviceId, ::mapTrack)
        }
        
        // Remove from remote
        if (track != null) {
            when (serviceId) {
                TrackerService.ANILIST -> {
                    if (track.entryId > 0) aniListRepository.deleteTrack(track.entryId)
                }
                TrackerService.MYANIMELIST -> {
                    malRepository?.deleteTrack(track.mediaId)
                }
                TrackerService.KITSU -> {
                    if (track.entryId > 0) kitsuRepository?.deleteTrack(track.entryId.toString())
                }
                TrackerService.MANGAUPDATES -> {
                    mangaUpdatesRepository?.deleteTrack(track.mediaId)
                }
                TrackerService.MYNOVELLIST -> {
                    myNovelListRepository?.deleteTrack(track.mediaId)
                }
            }
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
            TrackerService.MYANIMELIST -> malRepository?.search(query) ?: emptyList()
            TrackerService.KITSU -> kitsuRepository?.search(query) ?: emptyList()
            TrackerService.MANGAUPDATES -> mangaUpdatesRepository?.search(query) ?: emptyList()
            TrackerService.MYNOVELLIST -> myNovelListRepository?.search(query) ?: emptyList()
            else -> emptyList()
        }
    }
    
    override suspend fun linkBook(bookId: Long, serviceId: Int, searchResult: TrackSearchResult): Boolean {
        val track = when (serviceId) {
            TrackerService.ANILIST -> aniListRepository.bindBook(bookId, searchResult)
            TrackerService.MYANIMELIST -> malRepository?.bindBook(bookId, searchResult)
            TrackerService.KITSU -> kitsuRepository?.bindBook(bookId, searchResult)
            TrackerService.MANGAUPDATES -> mangaUpdatesRepository?.bindBook(bookId, searchResult)
            TrackerService.MYNOVELLIST -> myNovelListRepository?.bindBook(bookId, searchResult)
            else -> null
        }
        
        return if (track != null) {
            addTrack(track)
            true
        } else false
    }
    
    override suspend fun unlinkBook(bookId: Long, serviceId: Int): Boolean {
        return removeTrack(bookId, serviceId)
    }
    
    override suspend fun syncTrack(bookId: Long, serviceId: Int): Boolean {
        val track = handler.awaitOneOrNull {
            trackQueries.getTrack(bookId, serviceId, ::mapTrack)
        } ?: return false
        
        val synced = when (serviceId) {
            TrackerService.ANILIST -> aniListRepository.syncTrack(track)
            TrackerService.MYANIMELIST -> malRepository?.syncTrack(track)
            TrackerService.KITSU -> kitsuRepository?.syncTrack(track)
            TrackerService.MANGAUPDATES -> mangaUpdatesRepository?.syncTrack(track)
            TrackerService.MYNOVELLIST -> myNovelListRepository?.syncTrack(track)
            else -> null
        }
        
        return if (synced != null) {
            addTrack(synced)
            true
        } else false
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
            addTrack(updatedTrack)
            syncToRemote(updatedTrack)
        }
    }
    
    override suspend fun updateStatus(bookId: Long, status: TrackStatus): Boolean {
        val tracks = getTracksByBook(bookId)
        return tracks.all { track ->
            val updatedTrack = track.copy(status = status)
            addTrack(updatedTrack)
            syncToRemote(updatedTrack)
        }
    }
    
    override suspend fun updateScore(bookId: Long, score: Float): Boolean {
        val tracks = getTracksByBook(bookId)
        return tracks.all { track ->
            val updatedTrack = track.copy(score = score)
            addTrack(updatedTrack)
            syncToRemote(updatedTrack)
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
    
    // ==================== Service-specific methods ====================
    
    // AniList
    fun getAniListAuthUrl(): String = aniListRepository.getAuthUrl()
    suspend fun loginToAniList(accessToken: String): Boolean = aniListRepository.login(accessToken)
    fun logoutFromAniList() = aniListRepository.logout()
    fun isAniListAuthenticated(): Boolean = aniListRepository.isAuthenticated()
    
    // MyAnimeList
    fun getMalAuthUrl(): String? = malRepository?.getAuthUrl()
    suspend fun loginToMal(authCode: String): Boolean = malRepository?.login(authCode) == true
    fun logoutFromMal() = malRepository?.logout()
    fun isMalAuthenticated(): Boolean = malRepository?.isAuthenticated() == true
    
    // Kitsu
    suspend fun loginToKitsu(username: String, password: String): Boolean = 
        kitsuRepository?.login(username, password) == true
    fun logoutFromKitsu() = kitsuRepository?.logout()
    fun isKitsuAuthenticated(): Boolean = kitsuRepository?.isAuthenticated() == true
    
    // MangaUpdates
    suspend fun loginToMangaUpdates(username: String, password: String): Boolean = 
        mangaUpdatesRepository?.login(username, password) == true
    suspend fun logoutFromMangaUpdates() = mangaUpdatesRepository?.logout()
    fun isMangaUpdatesAuthenticated(): Boolean = mangaUpdatesRepository?.isAuthenticated() == true
    
    // MyNovelList
    fun getMyNovelListBaseUrl(): String = myNovelListRepository?.getBaseUrl() ?: ""
    fun setMyNovelListBaseUrl(url: String) = myNovelListRepository?.setBaseUrl(url)
    suspend fun loginToMyNovelList(apiKey: String): Boolean = myNovelListRepository?.login(apiKey) == true
    fun logoutFromMyNovelList() = myNovelListRepository?.logout()
    fun isMyNovelListAuthenticated(): Boolean = myNovelListRepository?.isAuthenticated() == true
    
    // ==================== Helper methods ====================
    
    private suspend fun syncToRemote(track: Track): Boolean {
        return when (track.siteId) {
            TrackerService.ANILIST -> aniListRepository.updateTrack(track)
            TrackerService.MYANIMELIST -> malRepository?.updateTrack(track) == true
            TrackerService.KITSU -> {
                if (track.entryId > 0) {
                    kitsuRepository?.updateTrack(track.entryId.toString(), track) == true
                } else false
            }
            TrackerService.MANGAUPDATES -> mangaUpdatesRepository?.updateTrack(track) == true
            TrackerService.MYNOVELLIST -> myNovelListRepository?.updateTrack(track) == true
            else -> true
        }
    }
    
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
