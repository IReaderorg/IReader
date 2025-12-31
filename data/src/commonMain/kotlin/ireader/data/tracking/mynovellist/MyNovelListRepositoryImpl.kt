package ireader.data.tracking.mynovellist

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
 * Repository for MyNovelList tracking operations.
 * Handles API key authentication, search, and sync with MyNovelList API.
 * 
 * Unlike OAuth-based trackers, MyNovelList uses API key authentication.
 * Users generate an API key from their MyNovelList settings page.
 */
class MyNovelListRepositoryImpl(
    httpClient: HttpClient,
    private val preferenceStore: PreferenceStore
) {
    private val api = MyNovelListApi(httpClient)
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()
    
    // Preference keys
    private val apiKeyPref = preferenceStore.getString("mynovellist_api_key", "")
    private val baseUrlPref = preferenceStore.getString("mynovellist_base_url", "https://mynoveltracker.netlify.app")
    
    init {
        // Load saved credentials on init
        val savedApiKey = apiKeyPref.get()
        val savedBaseUrl = baseUrlPref.get()
        
        if (savedBaseUrl.isNotEmpty()) {
            api.setBaseUrl(savedBaseUrl)
        }
        
        if (savedApiKey.isNotEmpty()) {
            api.setApiKey(savedApiKey)
            _isLoggedIn.value = true
        }
    }
    
    /**
     * Get the base URL for the MyNovelList instance
     */
    fun getBaseUrl(): String = api.getBaseUrl()
    
    /**
     * Set the base URL for a self-hosted MyNovelList instance
     */
    fun setBaseUrl(url: String) {
        api.setBaseUrl(url)
        baseUrlPref.set(url)
    }
    
    /**
     * Login with API key
     */
    suspend fun login(apiKey: String): Boolean {
        return try {
            Log.debug { "MyNovelList login attempt with key prefix: ${apiKey.take(12)}..." }
            Log.debug { "MyNovelList base URL: ${api.getBaseUrl()}" }
            
            api.setApiKey(apiKey)
            
            // Validate the API key
            val isValid = api.validateApiKey()
            if (isValid) {
                apiKeyPref.set(apiKey)
                _isLoggedIn.value = true
                Log.debug { "MyNovelList login successful" }
                true
            } else {
                api.setApiKey(null)
                Log.warn { "MyNovelList API key validation failed - key may be invalid or expired" }
                false
            }
        } catch (e: Exception) {
            Log.error(e, "MyNovelList login failed with exception")
            api.setApiKey(null)
            false
        }
    }
    
    /**
     * Login with existing token (for restoring session)
     */
    suspend fun loginWithToken(apiKey: String): Boolean = login(apiKey)
    
    /**
     * Logout and clear credentials
     */
    fun logout() {
        api.setApiKey(null)
        apiKeyPref.set("")
        _isLoggedIn.value = false
        Log.debug { "MyNovelList logout successful" }
    }
    
    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Boolean = _isLoggedIn.value && api.hasApiKey()
    
    /**
     * Search for novels
     */
    suspend fun search(query: String): List<TrackSearchResult> {
        return try {
            api.search(query)
        } catch (e: Exception) {
            Log.error(e, "MyNovelList search failed for query: $query")
            emptyList()
        }
    }
    
    /**
     * Get user's library
     */
    suspend fun getLibrary(): List<MyNovelListEntry> {
        return try {
            api.getLibrary()
        } catch (e: Exception) {
            Log.error(e, "MyNovelList getLibrary failed")
            emptyList()
        }
    }
    
    /**
     * Add a track to user's MyNovelList
     */
    suspend fun addTrack(track: Track): MyNovelListEntry? {
        return try {
            api.addToLibrary(track)
        } catch (e: Exception) {
            Log.error(e, "Failed to add track to MyNovelList")
            null
        }
    }
    
    /**
     * Update an existing track
     */
    suspend fun updateTrack(track: Track): Boolean {
        return try {
            // Extract novel ID from mediaUrl or use mediaId
            val novelId = extractNovelId(track.mediaUrl) ?: track.mediaId.toString()
            api.updateProgress(novelId, track)
        } catch (e: Exception) {
            Log.error(e, "Failed to update track on MyNovelList")
            false
        }
    }
    
    /**
     * Delete a track from user's MyNovelList
     */
    suspend fun deleteTrack(mediaId: Long): Boolean {
        return try {
            api.removeFromLibrary(mediaId.toString())
        } catch (e: Exception) {
            Log.error(e, "Failed to delete track from MyNovelList")
            false
        }
    }
    
    /**
     * Find a novel in user's library by title
     */
    suspend fun findInLibrary(title: String): MyNovelListEntry? {
        return try {
            val library = api.getLibrary()
            library.find { it.title.equals(title, ignoreCase = true) }
        } catch (e: Exception) {
            Log.error(e, "Failed to find track in MyNovelList library")
            null
        }
    }
    
    /**
     * Bind a book to MyNovelList entry, creating or updating as needed
     */
    suspend fun bindBook(bookId: Long, searchResult: TrackSearchResult): Track? {
        return try {
            // Check if already in user's library
            val existing = findInLibrary(searchResult.title)
            
            val track = Track(
                mangaId = bookId,
                siteId = TrackerService.MYNOVELLIST,
                entryId = existing?.id?.hashCode()?.toLong() ?: 0,
                mediaId = searchResult.mediaId,
                mediaUrl = searchResult.mediaUrl,
                title = searchResult.title,
                lastRead = existing?.currentChapter?.toFloat() ?: 0f,
                totalChapters = searchResult.totalChapters,
                score = (existing?.score ?: 0) / 10f, // Convert from 100 to 10 scale
                status = existing?.status?.toTrackStatusFromMyNovelList() ?: TrackStatus.Planned,
                startReadTime = 0,
                endReadTime = 0
            )
            
            if (existing == null) {
                // Add to library
                val entry = api.addToLibrary(track)
                if (entry != null) {
                    track.copy(entryId = entry.id.hashCode().toLong())
                } else null
            } else {
                track
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to bind book to MyNovelList")
            null
        }
    }
    
    /**
     * Sync a track with MyNovelList (pull remote data)
     */
    suspend fun syncTrack(track: Track): Track? {
        return try {
            val novelId = extractNovelId(track.mediaUrl) ?: track.mediaId.toString()
            val remote = api.getNovel(novelId) ?: return null
            
            track.copy(
                entryId = remote.id.hashCode().toLong(),
                lastRead = remote.currentChapter.toFloat(),
                score = remote.score / 10f,
                status = remote.status.toTrackStatusFromMyNovelList(),
                totalChapters = remote.totalChapters
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to sync track with MyNovelList")
            null
        }
    }
    
    /**
     * Extract novel ID from URL
     */
    private fun extractNovelId(url: String): String? {
        // URL format: https://mynovellist.netlify.app/novel/{id}
        val regex = Regex("/novel/([a-zA-Z0-9-]+)")
        return regex.find(url)?.groupValues?.getOrNull(1)
    }
}
