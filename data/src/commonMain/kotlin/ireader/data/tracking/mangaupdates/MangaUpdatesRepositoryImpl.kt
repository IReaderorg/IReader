package ireader.data.tracking.mangaupdates

import io.ktor.client.HttpClient
import ireader.core.log.Log
import ireader.core.prefs.PreferenceStore
import ireader.domain.models.entities.Track
import ireader.domain.models.entities.TrackSearchResult
import ireader.domain.models.entities.TrackStatus
import ireader.domain.models.entities.TrackerService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for MangaUpdates tracking operations.
 * Handles session-based authentication, search, and sync with MangaUpdates API.
 */
class MangaUpdatesRepositoryImpl(
    httpClient: HttpClient,
    private val preferenceStore: PreferenceStore
) {
    private val api = MangaUpdatesApi(httpClient)
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()
    
    // Preference keys
    private val sessionTokenPref = preferenceStore.getString("mangaupdates_session_token", "")
    private val userIdPref = preferenceStore.getLong("mangaupdates_user_id", 0L)
    private val userNamePref = preferenceStore.getString("mangaupdates_user_name", "")
    
    init {
        // Load saved credentials on init
        val savedToken = sessionTokenPref.get()
        if (savedToken.isNotEmpty()) {
            api.setSessionToken(savedToken)
            _isLoggedIn.value = true
        }
    }
    
    /**
     * Login with username and password
     */
    suspend fun login(username: String, password: String): Boolean {
        return try {
            val loginResponse = api.login(username, password)
            if (loginResponse != null) {
                api.setSessionToken(loginResponse.sessionToken)
                
                // Save credentials
                sessionTokenPref.set(loginResponse.sessionToken)
                userIdPref.set(loginResponse.uid)
                userNamePref.set(loginResponse.username)
                
                _isLoggedIn.value = true
                
                Log.debug { "MangaUpdates login successful for user: ${loginResponse.username}" }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.error(e, "MangaUpdates login failed")
            api.setSessionToken(null)
            false
        }
    }
    
    /**
     * Login with existing session token (for restoring session)
     */
    fun loginWithToken(sessionToken: String): Boolean {
        api.setSessionToken(sessionToken)
        sessionTokenPref.set(sessionToken)
        _isLoggedIn.value = true
        return true
    }
    
    /**
     * Logout and clear credentials
     */
    suspend fun logout() {
        try {
            api.logout()
        } catch (e: Exception) {
            Log.error(e, "MangaUpdates logout error")
        }
        
        api.setSessionToken(null)
        sessionTokenPref.set("")
        userIdPref.set(0L)
        userNamePref.set("")
        
        _isLoggedIn.value = false
        
        Log.debug { "MangaUpdates logout successful" }
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean = _isLoggedIn.value
    
    /**
     * Get current user ID
     */
    fun getUserId(): Long = userIdPref.get()
    
    /**
     * Get current username
     */
    fun getUsername(): String = userNamePref.get()
    
    /**
     * Search for manga
     */
    suspend fun search(query: String): List<TrackSearchResult> {
        return try {
            api.search(query)
        } catch (e: Exception) {
            Log.error(e, "MangaUpdates search failed for query: $query")
            emptyList()
        }
    }
    
    /**
     * Add a track to user's MangaUpdates list
     */
    suspend fun addTrack(track: Track): Boolean {
        return try {
            api.addToList(track.mediaId, track.status.toMangaUpdatesListId())
        } catch (e: Exception) {
            Log.error(e, "Failed to add track to MangaUpdates")
            false
        }
    }
    
    /**
     * Update an existing track
     */
    suspend fun updateTrack(track: Track): Boolean {
        return try {
            api.updateListEntry(track.mediaId, track)
        } catch (e: Exception) {
            Log.error(e, "Failed to update track on MangaUpdates")
            false
        }
    }
    
    /**
     * Delete a track from user's MangaUpdates list
     */
    suspend fun deleteTrack(seriesId: Long): Boolean {
        return try {
            api.deleteFromList(seriesId)
        } catch (e: Exception) {
            Log.error(e, "Failed to delete track from MangaUpdates")
            false
        }
    }
    
    /**
     * Get series info from user's list
     */
    suspend fun getListEntry(seriesId: Long): MangaUpdatesListEntry? {
        return try {
            api.getListEntry(seriesId)
        } catch (e: Exception) {
            Log.error(e, "Failed to get MangaUpdates list entry")
            null
        }
    }
    
    /**
     * Bind a book to MangaUpdates entry
     */
    suspend fun bindBook(bookId: Long, searchResult: TrackSearchResult): Track? {
        return try {
            val existing = api.getListEntry(searchResult.mediaId)
            val seriesDetails = api.getSeriesDetails(searchResult.mediaId)
            
            val track = Track(
                mangaId = bookId,
                siteId = TrackerService.MANGAUPDATES,
                entryId = 0, // MangaUpdates doesn't have separate entry IDs
                mediaId = searchResult.mediaId,
                mediaUrl = searchResult.mediaUrl,
                title = searchResult.title,
                lastRead = existing?.chapter?.toFloat() ?: 0f,
                totalChapters = seriesDetails?.latestChapter ?: searchResult.totalChapters,
                score = (existing?.rating ?: 0) / 2f,
                status = existing?.listId?.toTrackStatusFromMangaUpdates() ?: TrackStatus.Planned,
                startReadTime = 0,
                endReadTime = 0
            )
            
            if (existing == null) {
                // Add to list
                val success = api.addToList(searchResult.mediaId, track.status.toMangaUpdatesListId())
                if (success) track else null
            } else {
                track
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to bind book to MangaUpdates")
            null
        }
    }
    
    /**
     * Sync a track with MangaUpdates (pull remote data)
     */
    suspend fun syncTrack(track: Track): Track? {
        return try {
            val remote = api.getListEntry(track.mediaId) ?: return null
            val seriesDetails = api.getSeriesDetails(track.mediaId)
            
            track.copy(
                lastRead = remote.chapter.toFloat(),
                score = (remote.rating ?: 0) / 2f,
                status = remote.listId.toTrackStatusFromMangaUpdates(),
                totalChapters = seriesDetails?.latestChapter ?: track.totalChapters
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to sync track with MangaUpdates")
            null
        }
    }
}
