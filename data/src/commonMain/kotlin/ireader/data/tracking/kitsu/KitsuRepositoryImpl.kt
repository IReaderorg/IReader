package ireader.data.tracking.kitsu

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
 * Repository for Kitsu tracking operations.
 * Handles OAuth2 authentication with username/password, search, and sync with Kitsu API.
 */
class KitsuRepositoryImpl(
    httpClient: HttpClient,
    private val preferenceStore: PreferenceStore
) {
    private val api = KitsuApi(httpClient)
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _currentUser = MutableStateFlow<KitsuUser?>(null)
    val currentUser: Flow<KitsuUser?> = _currentUser.asStateFlow()
    
    // Preference keys
    private val accessTokenPref = preferenceStore.getString("kitsu_access_token", "")
    private val refreshTokenPref = preferenceStore.getString("kitsu_refresh_token", "")
    private val userIdPref = preferenceStore.getString("kitsu_user_id", "")
    private val userNamePref = preferenceStore.getString("kitsu_user_name", "")
    
    init {
        // Load saved credentials on init
        val savedToken = accessTokenPref.get()
        val savedUserId = userIdPref.get()
        if (savedToken.isNotEmpty() && savedUserId.isNotEmpty()) {
            api.setAccessToken(savedToken)
            api.setUserId(savedUserId)
            _isLoggedIn.value = true
            _currentUser.value = KitsuUser(
                id = savedUserId,
                name = userNamePref.get()
            )
        }
    }
    
    /**
     * Login with username and password
     */
    suspend fun login(username: String, password: String): Boolean {
        return try {
            val tokenResponse = api.login(username, password)
            if (tokenResponse != null) {
                api.setAccessToken(tokenResponse.accessToken)
                
                // Fetch user info
                val user = api.getCurrentUser()
                if (user != null) {
                    api.setUserId(user.id)
                    
                    // Save credentials
                    accessTokenPref.set(tokenResponse.accessToken)
                    refreshTokenPref.set(tokenResponse.refreshToken ?: "")
                    userIdPref.set(user.id)
                    userNamePref.set(user.name)
                    
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    
                    Log.debug { "Kitsu login successful for user: ${user.name}" }
                    true
                } else {
                    api.setAccessToken(null)
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.error(e, "Kitsu login failed")
            api.setAccessToken(null)
            false
        }
    }
    
    /**
     * Login with existing access token (for restoring session)
     */
    suspend fun loginWithToken(accessToken: String): Boolean {
        return try {
            api.setAccessToken(accessToken)
            
            val user = api.getCurrentUser()
            if (user != null) {
                api.setUserId(user.id)
                
                accessTokenPref.set(accessToken)
                userIdPref.set(user.id)
                userNamePref.set(user.name)
                
                _currentUser.value = user
                _isLoggedIn.value = true
                
                Log.debug { "Kitsu token login successful for user: ${user.name}" }
                true
            } else {
                api.setAccessToken(null)
                false
            }
        } catch (e: Exception) {
            Log.error(e, "Kitsu token login failed")
            api.setAccessToken(null)
            false
        }
    }
    
    /**
     * Logout and clear credentials
     */
    fun logout() {
        api.setAccessToken(null)
        api.setUserId(null)
        accessTokenPref.set("")
        refreshTokenPref.set("")
        userIdPref.set("")
        userNamePref.set("")
        
        _currentUser.value = null
        _isLoggedIn.value = false
        
        Log.debug { "Kitsu logout successful" }
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean = _isLoggedIn.value
    
    /**
     * Get current user ID
     */
    fun getUserId(): String = userIdPref.get()
    
    /**
     * Search for manga
     */
    suspend fun search(query: String): List<TrackSearchResult> {
        return try {
            api.search(query)
        } catch (e: Exception) {
            Log.error(e, "Kitsu search failed for query: $query")
            emptyList()
        }
    }
    
    /**
     * Add a track to user's Kitsu library
     */
    suspend fun addTrack(track: Track): String? {
        return try {
            api.addToLibrary(track)
        } catch (e: Exception) {
            Log.error(e, "Failed to add track to Kitsu")
            null
        }
    }
    
    /**
     * Update an existing track
     */
    suspend fun updateTrack(entryId: String, track: Track): Boolean {
        return try {
            api.updateEntry(entryId, track)
        } catch (e: Exception) {
            Log.error(e, "Failed to update track on Kitsu")
            false
        }
    }
    
    /**
     * Delete a track from user's Kitsu library
     */
    suspend fun deleteTrack(entryId: String): Boolean {
        return try {
            api.deleteEntry(entryId)
        } catch (e: Exception) {
            Log.error(e, "Failed to delete track from Kitsu")
            false
        }
    }
    
    /**
     * Find manga in user's library
     */
    suspend fun findInLibrary(mediaId: Long): KitsuLibraryEntry? {
        return try {
            api.findInLibrary(mediaId)
        } catch (e: Exception) {
            Log.error(e, "Failed to find manga in Kitsu library")
            null
        }
    }
    
    /**
     * Bind a book to Kitsu entry
     */
    suspend fun bindBook(bookId: Long, searchResult: TrackSearchResult): Track? {
        return try {
            val existing = api.findInLibrary(searchResult.mediaId)
            
            val track = Track(
                mangaId = bookId,
                siteId = TrackerService.KITSU,
                entryId = existing?.id?.toLongOrNull() ?: 0,
                mediaId = searchResult.mediaId,
                mediaUrl = searchResult.mediaUrl,
                title = searchResult.title,
                lastRead = existing?.progress?.toFloat() ?: 0f,
                totalChapters = searchResult.totalChapters,
                score = (existing?.ratingTwenty ?: 0) / 2f,
                status = existing?.status?.toTrackStatusFromKitsu() ?: TrackStatus.Planned,
                startReadTime = 0,
                endReadTime = 0
            )
            
            if (existing == null) {
                // Add to library
                val entryId = api.addToLibrary(track)
                if (entryId != null) {
                    track.copy(entryId = entryId.toLongOrNull() ?: 0)
                } else null
            } else {
                track
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to bind book to Kitsu")
            null
        }
    }
    
    /**
     * Sync a track with Kitsu (pull remote data)
     */
    suspend fun syncTrack(track: Track): Track? {
        return try {
            val remote = api.findInLibrary(track.mediaId) ?: return null
            
            track.copy(
                entryId = remote.id.toLongOrNull() ?: 0,
                lastRead = remote.progress.toFloat(),
                score = (remote.ratingTwenty ?: 0) / 2f,
                status = remote.status.toTrackStatusFromKitsu(),
                totalChapters = remote.totalChapters
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to sync track with Kitsu")
            null
        }
    }
}
