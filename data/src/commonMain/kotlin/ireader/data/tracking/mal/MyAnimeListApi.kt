package ireader.data.tracking.mal

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import ireader.core.log.Log
import ireader.domain.models.entities.Track
import ireader.domain.models.entities.TrackSearchResult
import ireader.domain.models.entities.TrackStatus
import kotlinx.serialization.json.*

/**
 * MyAnimeList API client for tracking manga.
 * Uses OAuth2 with PKCE for authentication.
 * 
 * API Documentation: https://myanimelist.net/apiconfig/references/api/v2
 */
class MyAnimeListApi(
    private val httpClient: HttpClient,
    private val json: Json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
) {
    companion object {
        const val API_URL = "https://api.myanimelist.net/v2"
        const val AUTH_URL = "https://myanimelist.net/v1/oauth2/authorize"
        const val TOKEN_URL = "https://myanimelist.net/v1/oauth2/token"
        const val BASE_MANGA_URL = "https://myanimelist.net/manga/"
        
        // Register your app at https://myanimelist.net/apiconfig
        const val CLIENT_ID = "b57bb4b4baefbe85d9f4ca61a38c58b3" // IReader MAL client ID
    }
    
    private var accessToken: String? = null
    private var refreshToken: String? = null
    
    fun setAccessToken(token: String?) {
        accessToken = token
    }
    
    fun setRefreshToken(token: String?) {
        refreshToken = token
    }
    
    fun getAuthUrl(codeChallenge: String): String {
        return "$AUTH_URL?response_type=code&client_id=$CLIENT_ID&code_challenge=$codeChallenge&code_challenge_method=plain"
    }
    
    /**
     * Exchange authorization code for access token
     */
    suspend fun getAccessToken(authCode: String, codeVerifier: String): TokenResponse? {
        return try {
            val response = httpClient.post(TOKEN_URL) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("client_id=$CLIENT_ID&code=$authCode&code_verifier=$codeVerifier&grant_type=authorization_code")
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            TokenResponse(
                accessToken = jsonResponse["access_token"]?.jsonPrimitive?.content ?: return null,
                refreshToken = jsonResponse["refresh_token"]?.jsonPrimitive?.contentOrNull,
                expiresIn = jsonResponse["expires_in"]?.jsonPrimitive?.intOrNull ?: 0
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to get MAL access token")
            null
        }
    }
    
    /**
     * Refresh access token
     */
    suspend fun refreshAccessToken(refreshToken: String): TokenResponse? {
        return try {
            val response = httpClient.post(TOKEN_URL) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("client_id=$CLIENT_ID&refresh_token=$refreshToken&grant_type=refresh_token")
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            TokenResponse(
                accessToken = jsonResponse["access_token"]?.jsonPrimitive?.content ?: return null,
                refreshToken = jsonResponse["refresh_token"]?.jsonPrimitive?.contentOrNull,
                expiresIn = jsonResponse["expires_in"]?.jsonPrimitive?.intOrNull ?: 0
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to refresh MAL access token")
            null
        }
    }
    
    /**
     * Get current user info
     */
    suspend fun getCurrentUser(): MalUser? {
        val token = accessToken ?: return null
        
        return try {
            val response = httpClient.get("$API_URL/users/@me") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            MalUser(
                id = jsonResponse["id"]?.jsonPrimitive?.int ?: return null,
                name = jsonResponse["name"]?.jsonPrimitive?.content ?: "Unknown"
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to get MAL user")
            null
        }
    }
    
    /**
     * Search for manga
     */
    suspend fun search(query: String): List<TrackSearchResult> {
        val token = accessToken ?: return emptyList()
        
        return try {
            val response = httpClient.get("$API_URL/manga") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("q", query)
                parameter("limit", 30)
                parameter("fields", "id,title,main_picture,synopsis,status,num_chapters,start_date")
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            parseSearchResults(jsonResponse)
        } catch (e: Exception) {
            Log.error(e, "MAL search failed for query: $query")
            emptyList()
        }
    }
    
    /**
     * Add manga to user's list
     */
    suspend fun addToList(track: Track): Boolean {
        val token = accessToken ?: return false
        
        return try {
            val response = httpClient.patch("$API_URL/manga/${track.mediaId}/my_list_status") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(buildString {
                    append("status=${track.status.toMalStatus()}")
                    append("&num_chapters_read=${track.lastRead.toInt()}")
                    if (track.score > 0) append("&score=${(track.score * 10).toInt()}")
                })
            }
            
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.error(e, "Failed to add manga to MAL list")
            false
        }
    }
    
    /**
     * Update manga in user's list
     */
    suspend fun updateEntry(track: Track): Boolean {
        return addToList(track) // Same endpoint for add/update
    }
    
    /**
     * Delete manga from user's list
     */
    suspend fun deleteEntry(mediaId: Long): Boolean {
        val token = accessToken ?: return false
        
        return try {
            val response = httpClient.delete("$API_URL/manga/$mediaId/my_list_status") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.error(e, "Failed to delete manga from MAL list")
            false
        }
    }
    
    /**
     * Get manga details from user's list
     */
    suspend fun getMangaDetails(mediaId: Long): MalMangaListStatus? {
        val token = accessToken ?: return null
        
        return try {
            val response = httpClient.get("$API_URL/manga/$mediaId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("fields", "id,title,num_chapters,my_list_status{status,score,num_chapters_read,start_date,finish_date}")
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            parseMangaListStatus(jsonResponse)
        } catch (e: Exception) {
            Log.error(e, "Failed to get MAL manga details")
            null
        }
    }
    
    private fun parseSearchResults(response: JsonObject): List<TrackSearchResult> {
        return try {
            val data = response["data"]?.jsonArray ?: return emptyList()
            
            data.mapNotNull { item ->
                val node = item.jsonObject["node"]?.jsonObject ?: return@mapNotNull null
                val id = node["id"]?.jsonPrimitive?.longOrNull ?: return@mapNotNull null
                val title = node["title"]?.jsonPrimitive?.contentOrNull ?: "Unknown"
                
                TrackSearchResult(
                    mediaId = id,
                    mediaUrl = "$BASE_MANGA_URL$id",
                    title = title,
                    totalChapters = node["num_chapters"]?.jsonPrimitive?.intOrNull ?: 0,
                    coverUrl = node["main_picture"]?.jsonObject?.get("large")?.jsonPrimitive?.contentOrNull
                        ?: node["main_picture"]?.jsonObject?.get("medium")?.jsonPrimitive?.contentOrNull ?: "",
                    summary = node["synopsis"]?.jsonPrimitive?.contentOrNull ?: "",
                    publishingStatus = node["status"]?.jsonPrimitive?.contentOrNull ?: "Unknown",
                    publishingType = "Manga",
                    startDate = node["start_date"]?.jsonPrimitive?.contentOrNull ?: ""
                )
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to parse MAL search results")
            emptyList()
        }
    }
    
    private fun parseMangaListStatus(response: JsonObject): MalMangaListStatus? {
        return try {
            val listStatus = response["my_list_status"]?.jsonObject ?: return null
            
            MalMangaListStatus(
                mediaId = response["id"]?.jsonPrimitive?.long ?: return null,
                title = response["title"]?.jsonPrimitive?.content ?: "",
                totalChapters = response["num_chapters"]?.jsonPrimitive?.intOrNull ?: 0,
                status = listStatus["status"]?.jsonPrimitive?.contentOrNull ?: "plan_to_read",
                score = listStatus["score"]?.jsonPrimitive?.intOrNull ?: 0,
                chaptersRead = listStatus["num_chapters_read"]?.jsonPrimitive?.intOrNull ?: 0,
                startDate = listStatus["start_date"]?.jsonPrimitive?.contentOrNull,
                finishDate = listStatus["finish_date"]?.jsonPrimitive?.contentOrNull
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to parse MAL manga list status")
            null
        }
    }
}

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Int
)

data class MalUser(
    val id: Int,
    val name: String
)

data class MalMangaListStatus(
    val mediaId: Long,
    val title: String,
    val totalChapters: Int,
    val status: String,
    val score: Int,
    val chaptersRead: Int,
    val startDate: String?,
    val finishDate: String?
)

/**
 * Extension to convert TrackStatus to MAL status string
 */
fun TrackStatus.toMalStatus(): String = when (this) {
    TrackStatus.Reading -> "reading"
    TrackStatus.Completed -> "completed"
    TrackStatus.OnHold -> "on_hold"
    TrackStatus.Dropped -> "dropped"
    TrackStatus.Planned -> "plan_to_read"
    TrackStatus.Repeating -> "reading"
}

/**
 * Extension to convert MAL status string to TrackStatus
 */
fun String.toTrackStatusFromMal(): TrackStatus = when (this.lowercase()) {
    "reading" -> TrackStatus.Reading
    "completed" -> TrackStatus.Completed
    "on_hold" -> TrackStatus.OnHold
    "dropped" -> TrackStatus.Dropped
    "plan_to_read" -> TrackStatus.Planned
    else -> TrackStatus.Planned
}
