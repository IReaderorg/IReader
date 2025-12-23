package ireader.data.tracking.anilist

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
import kotlinx.serialization.json.Json

/**
 * Repository for AniList tracking operations.
 * Handles authentication, search, and sync with AniList API.
 */
class AniListRepositoryImpl(
    httpClient: HttpClient,
    private val preferenceStore: PreferenceStore
) {
    private val api = AniListApi(httpClient)
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _currentUser = MutableStateFlow<AniListUser?>(null)
    val currentUser: Flow<AniListUser?> = _currentUser.asStateFlow()
    
    // Preference keys
    private val accessTokenPref = preferenceStore.getString("anilist_access_token", "")
    private val userIdPref = preferenceStore.getInt("anilist_user_id", 0)
    private val userNamePref = preferenceStore.getString("anilist_user_name", "")
    private val scoreFormatPref = preferenceStore.getString("anilist_score_format", "POINT_100")
    
    init {
        // Load saved credentials on init
        val savedToken = accessTokenPref.get()
        if (savedToken.isNotEmpty()) {
            api.setAccessToken(savedToken)
            _isLoggedIn.value = true
            _currentUser.value = AniListUser(
                id = userIdPref.get(),
                name = userNamePref.get(),
                scoreFormat = scoreFormatPref.get()
            )
        }
    }
    
    /**
     * Get the OAuth authorization URL
     */
    fun getAuthUrl(): String = api.getAuthUrl()
    
    /**
     * Login with OAuth access token
     */
    suspend fun login(accessToken: String): Boolean {
        return try {
            api.setAccessToken(accessToken)
            
            // Fetch user info to validate token
            val user = api.getCurrentUser()
            if (user != null) {
                // Save credentials
                accessTokenPref.set(accessToken)
                userIdPref.set(user.id)
                userNamePref.set(user.name)
                scoreFormatPref.set(user.scoreFormat)
                
                _currentUser.value = user
                _isLoggedIn.value = true
                
                Log.debug { "AniList login successful for user: ${user.name}" }
                true
            } else {
                api.setAccessToken(null)
                false
            }
        } catch (e: Exception) {
            Log.error(e, "AniList login failed")
            api.setAccessToken(null)
            false
        }
    }
    
    /**
     * Logout and clear credentials
     */
    fun logout() {
        api.setAccessToken(null)
        accessTokenPref.set("")
        userIdPref.set(0)
        userNamePref.set("")
        
        _currentUser.value = null
        _isLoggedIn.value = false
        
        Log.debug { "AniList logout successful" }
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean = _isLoggedIn.value
    
    /**
     * Get current user ID
     */
    fun getUserId(): Int = userIdPref.get()
    
    /**
     * Search for manga/light novels
     */
    suspend fun search(query: String): List<TrackSearchResult> {
        return try {
            api.search(query)
        } catch (e: Exception) {
            Log.error(e, "AniList search failed for query: $query")
            emptyList()
        }
    }
    
    /**
     * Add a track to user's AniList
     */
    suspend fun addTrack(track: Track): Long? {
        return try {
            api.addToList(track)
        } catch (e: Exception) {
            Log.error(e, "Failed to add track to AniList")
            null
        }
    }
    
    /**
     * Update an existing track
     */
    suspend fun updateTrack(track: Track): Boolean {
        return try {
            api.updateEntry(track)
        } catch (e: Exception) {
            Log.error(e, "Failed to update track on AniList")
            false
        }
    }
    
    /**
     * Delete a track from user's AniList
     */
    suspend fun deleteTrack(entryId: Long): Boolean {
        return try {
            api.deleteEntry(entryId)
        } catch (e: Exception) {
            Log.error(e, "Failed to delete track from AniList")
            false
        }
    }
    
    /**
     * Find a manga in user's list
     */
    suspend fun findInUserList(mediaId: Long): AniListMediaListEntry? {
        val userId = getUserId()
        if (userId == 0) return null
        
        return try {
            api.findInUserList(mediaId, userId)
        } catch (e: Exception) {
            Log.error(e, "Failed to find track in AniList user list")
            null
        }
    }
    
    /**
     * Bind a book to AniList entry, creating or updating as needed
     */
    suspend fun bindBook(bookId: Long, searchResult: TrackSearchResult): Track? {
        val userId = getUserId()
        if (userId == 0) return null
        
        return try {
            // Check if already in user's list
            val existing = api.findInUserList(searchResult.mediaId, userId)
            
            val track = Track(
                mangaId = bookId,
                siteId = TrackerService.ANILIST,
                entryId = existing?.id ?: 0,
                mediaId = searchResult.mediaId,
                mediaUrl = searchResult.mediaUrl,
                title = searchResult.title,
                lastRead = existing?.progress?.toFloat() ?: 0f,
                totalChapters = searchResult.totalChapters,
                score = (existing?.score ?: 0f) / 10f, // Convert from 100 to 10 scale
                status = existing?.status?.toTrackStatus() ?: TrackStatus.Planned,
                startReadTime = existing?.startedAt ?: 0,
                endReadTime = existing?.completedAt ?: 0
            )
            
            if (existing == null) {
                // Add to list
                val entryId = api.addToList(track)
                if (entryId != null) {
                    track.copy(entryId = entryId)
                } else null
            } else {
                track
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to bind book to AniList")
            null
        }
    }
    
    /**
     * Sync a track with AniList (pull remote data)
     */
    suspend fun syncTrack(track: Track): Track? {
        val userId = getUserId()
        if (userId == 0) return null
        
        return try {
            val remote = api.findInUserList(track.mediaId, userId) ?: return null
            
            track.copy(
                entryId = remote.id,
                lastRead = remote.progress.toFloat(),
                score = remote.score / 10f,
                status = remote.status.toTrackStatus(),
                startReadTime = remote.startedAt,
                endReadTime = remote.completedAt,
                totalChapters = remote.totalChapters
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to sync track with AniList")
            null
        }
    }
}
