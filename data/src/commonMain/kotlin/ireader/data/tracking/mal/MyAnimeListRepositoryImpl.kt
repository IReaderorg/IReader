package ireader.data.tracking.mal

import io.ktor.client.HttpClient
import ireader.core.log.Log
import ireader.core.prefs.PreferenceStore
import ireader.domain.models.entities.Track
import ireader.domain.models.entities.TrackSearchResult
import ireader.domain.models.entities.TrackStatus
import ireader.domain.models.entities.TrackerService
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * Repository for MyAnimeList tracking operations.
 * Handles OAuth2 with PKCE authentication, search, and sync with MAL API.
 */
class MyAnimeListRepositoryImpl(
    httpClient: HttpClient,
    private val preferenceStore: PreferenceStore
) {
    private val api = MyAnimeListApi(httpClient)
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()
    
    private val _currentUser = MutableStateFlow<MalUser?>(null)
    val currentUser: Flow<MalUser?> = _currentUser.asStateFlow()
    
    // Preference keys
    private val accessTokenPref = preferenceStore.getString("mal_access_token", "")
    private val refreshTokenPref = preferenceStore.getString("mal_refresh_token", "")
    private val userIdPref = preferenceStore.getInt("mal_user_id", 0)
    private val userNamePref = preferenceStore.getString("mal_user_name", "")
    private val tokenExpiryPref = preferenceStore.getLong("mal_token_expiry", 0L)
    
    // PKCE code verifier (stored temporarily during auth flow)
    private var codeVerifier: String? = null
    
    init {
        // Load saved credentials on init
        val savedToken = accessTokenPref.get()
        if (savedToken.isNotEmpty()) {
            api.setAccessToken(savedToken)
            api.setRefreshToken(refreshTokenPref.get())
            _isLoggedIn.value = true
            _currentUser.value = MalUser(
                id = userIdPref.get(),
                name = userNamePref.get()
            )
        }
    }
    
    /**
     * Generate PKCE code verifier and return auth URL
     */
    fun getAuthUrl(): String {
        // Generate random code verifier (43-128 characters)
        codeVerifier = generateCodeVerifier()
        return api.getAuthUrl(codeVerifier!!)
    }
    
    /**
     * Complete OAuth login with authorization code
     */
    suspend fun login(authCode: String): Boolean {
        val verifier = codeVerifier ?: return false
        
        return try {
            val tokenResponse = api.getAccessToken(authCode, verifier)
            if (tokenResponse != null) {
                api.setAccessToken(tokenResponse.accessToken)
                api.setRefreshToken(tokenResponse.refreshToken)
                
                // Fetch user info
                val user = api.getCurrentUser()
                if (user != null) {
                    // Save credentials
                    accessTokenPref.set(tokenResponse.accessToken)
                    refreshTokenPref.set(tokenResponse.refreshToken ?: "")
                    userIdPref.set(user.id)
                    userNamePref.set(user.name)
                    tokenExpiryPref.set(currentTimeToLong() + (tokenResponse.expiresIn * 1000L))
                    
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    
                    Log.debug { "MAL login successful for user: ${user.name}" }
                    true
                } else {
                    api.setAccessToken(null)
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            Log.error(e, "MAL login failed")
            api.setAccessToken(null)
            false
        } finally {
            codeVerifier = null
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
                accessTokenPref.set(accessToken)
                userIdPref.set(user.id)
                userNamePref.set(user.name)
                
                _currentUser.value = user
                _isLoggedIn.value = true
                
                Log.debug { "MAL token login successful for user: ${user.name}" }
                true
            } else {
                api.setAccessToken(null)
                false
            }
        } catch (e: Exception) {
            Log.error(e, "MAL token login failed")
            api.setAccessToken(null)
            false
        }
    }
    
    /**
     * Refresh access token if expired
     */
    suspend fun refreshTokenIfNeeded(): Boolean {
        val expiry = tokenExpiryPref.get()
        val currentTime = currentTimeToLong()
        
        // Refresh if token expires in less than 5 minutes
        if (expiry > 0 && currentTime > expiry - 300000) {
            val refreshToken = refreshTokenPref.get()
            if (refreshToken.isNotEmpty()) {
                val tokenResponse = api.refreshAccessToken(refreshToken)
                if (tokenResponse != null) {
                    api.setAccessToken(tokenResponse.accessToken)
                    api.setRefreshToken(tokenResponse.refreshToken)
                    
                    accessTokenPref.set(tokenResponse.accessToken)
                    refreshTokenPref.set(tokenResponse.refreshToken ?: "")
                    tokenExpiryPref.set(currentTime + (tokenResponse.expiresIn * 1000L))
                    
                    return true
                }
            }
            return false
        }
        return true
    }
    
    /**
     * Logout and clear credentials
     */
    fun logout() {
        api.setAccessToken(null)
        api.setRefreshToken(null)
        accessTokenPref.set("")
        refreshTokenPref.set("")
        userIdPref.set(0)
        userNamePref.set("")
        tokenExpiryPref.set(0L)
        
        _currentUser.value = null
        _isLoggedIn.value = false
        
        Log.debug { "MAL logout successful" }
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
     * Search for manga
     */
    suspend fun search(query: String): List<TrackSearchResult> {
        refreshTokenIfNeeded()
        return try {
            api.search(query)
        } catch (e: Exception) {
            Log.error(e, "MAL search failed for query: $query")
            emptyList()
        }
    }
    
    /**
     * Add a track to user's MAL list
     */
    suspend fun addTrack(track: Track): Boolean {
        refreshTokenIfNeeded()
        return try {
            api.addToList(track)
        } catch (e: Exception) {
            Log.error(e, "Failed to add track to MAL")
            false
        }
    }
    
    /**
     * Update an existing track
     */
    suspend fun updateTrack(track: Track): Boolean {
        refreshTokenIfNeeded()
        return try {
            api.updateEntry(track)
        } catch (e: Exception) {
            Log.error(e, "Failed to update track on MAL")
            false
        }
    }
    
    /**
     * Delete a track from user's MAL list
     */
    suspend fun deleteTrack(mediaId: Long): Boolean {
        refreshTokenIfNeeded()
        return try {
            api.deleteEntry(mediaId)
        } catch (e: Exception) {
            Log.error(e, "Failed to delete track from MAL")
            false
        }
    }
    
    /**
     * Get manga details from user's list
     */
    suspend fun getMangaDetails(mediaId: Long): MalMangaListStatus? {
        refreshTokenIfNeeded()
        return try {
            api.getMangaDetails(mediaId)
        } catch (e: Exception) {
            Log.error(e, "Failed to get MAL manga details")
            null
        }
    }
    
    /**
     * Bind a book to MAL entry
     */
    suspend fun bindBook(bookId: Long, searchResult: TrackSearchResult): Track? {
        refreshTokenIfNeeded()
        
        return try {
            val existing = api.getMangaDetails(searchResult.mediaId)
            
            val track = Track(
                mangaId = bookId,
                siteId = TrackerService.MYANIMELIST,
                entryId = 0, // MAL doesn't have separate entry IDs
                mediaId = searchResult.mediaId,
                mediaUrl = searchResult.mediaUrl,
                title = searchResult.title,
                lastRead = existing?.chaptersRead?.toFloat() ?: 0f,
                totalChapters = searchResult.totalChapters,
                score = (existing?.score ?: 0) / 10f,
                status = existing?.status?.toTrackStatusFromMal() ?: TrackStatus.Planned,
                startReadTime = 0,
                endReadTime = 0
            )
            
            if (existing == null) {
                // Add to list
                val success = api.addToList(track)
                if (success) track else null
            } else {
                track
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to bind book to MAL")
            null
        }
    }
    
    /**
     * Sync a track with MAL (pull remote data)
     */
    suspend fun syncTrack(track: Track): Track? {
        refreshTokenIfNeeded()
        
        return try {
            val remote = api.getMangaDetails(track.mediaId) ?: return null
            
            track.copy(
                lastRead = remote.chaptersRead.toFloat(),
                score = remote.score / 10f,
                status = remote.status.toTrackStatusFromMal(),
                totalChapters = remote.totalChapters
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to sync track with MAL")
            null
        }
    }
    
    private fun generateCodeVerifier(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        return (1..128).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}
