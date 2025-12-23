package ireader.data.tracking.mangaupdates

import io.ktor.client.HttpClient
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
 * MangaUpdates API client for tracking manga.
 * Uses session-based authentication with username/password.
 * 
 * API Documentation: https://api.mangaupdates.com/
 */
class MangaUpdatesApi(
    private val httpClient: HttpClient,
    private val json: Json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
) {
    companion object {
        const val API_URL = "https://api.mangaupdates.com/v1"
        const val BASE_MANGA_URL = "https://www.mangaupdates.com/series/"
    }
    
    private var sessionToken: String? = null
    
    fun setSessionToken(token: String?) {
        sessionToken = token
    }
    
    /**
     * Login with username and password
     */
    suspend fun login(username: String, password: String): LoginResponse? {
        return try {
            val payload = buildJsonObject {
                put("username", username)
                put("password", password)
            }
            
            val response = httpClient.put("$API_URL/account/login") {
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            val context = jsonResponse["context"]?.jsonObject
            
            LoginResponse(
                sessionToken = context?.get("session_token")?.jsonPrimitive?.contentOrNull ?: return null,
                uid = context["uid"]?.jsonPrimitive?.longOrNull ?: 0,
                username = context["username"]?.jsonPrimitive?.contentOrNull ?: ""
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to login to MangaUpdates")
            null
        }
    }
    
    /**
     * Logout
     */
    suspend fun logout(): Boolean {
        val token = sessionToken ?: return true
        
        return try {
            val response = httpClient.post("$API_URL/account/logout") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.error(e, "Failed to logout from MangaUpdates")
            false
        }
    }
    
    /**
     * Search for manga
     */
    suspend fun search(query: String): List<TrackSearchResult> {
        return try {
            val payload = buildJsonObject {
                put("search", query)
            }
            
            val response = httpClient.post("$API_URL/series/search") {
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            parseSearchResults(jsonResponse)
        } catch (e: Exception) {
            Log.error(e, "MangaUpdates search failed for query: $query")
            emptyList()
        }
    }
    
    /**
     * Add series to user's list
     */
    suspend fun addToList(seriesId: Long, listId: Int): Boolean {
        val token = sessionToken ?: return false
        
        return try {
            val payload = buildJsonArray {
                add(buildJsonObject {
                    put("series", buildJsonObject {
                        put("id", seriesId)
                    })
                    put("list_id", listId)
                })
            }
            
            val response = httpClient.post("$API_URL/lists/series") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.error(e, "Failed to add series to MangaUpdates list")
            false
        }
    }
    
    /**
     * Update series in user's list
     */
    suspend fun updateListEntry(seriesId: Long, track: Track): Boolean {
        val token = sessionToken ?: return false
        
        return try {
            val payload = buildJsonArray {
                add(buildJsonObject {
                    put("series", buildJsonObject {
                        put("id", seriesId)
                    })
                    put("list_id", track.status.toMangaUpdatesListId())
                    put("status", buildJsonObject {
                        put("chapter", track.lastRead.toInt())
                    })
                })
            }
            
            val response = httpClient.post("$API_URL/lists/series/update") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.error(e, "Failed to update MangaUpdates list entry")
            false
        }
    }
    
    /**
     * Delete series from user's list
     */
    suspend fun deleteFromList(seriesId: Long): Boolean {
        val token = sessionToken ?: return false
        
        return try {
            val payload = buildJsonArray {
                add(seriesId)
            }
            
            val response = httpClient.post("$API_URL/lists/series/delete") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.error(e, "Failed to delete series from MangaUpdates list")
            false
        }
    }
    
    /**
     * Get series info from user's list
     */
    suspend fun getListEntry(seriesId: Long): MangaUpdatesListEntry? {
        val token = sessionToken ?: return null
        
        return try {
            val response = httpClient.get("$API_URL/lists/series/$seriesId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            parseListEntry(jsonResponse)
        } catch (e: Exception) {
            Log.error(e, "Failed to get MangaUpdates list entry")
            null
        }
    }
    
    /**
     * Get series details
     */
    suspend fun getSeriesDetails(seriesId: Long): MangaUpdatesSeriesDetails? {
        return try {
            val response = httpClient.get("$API_URL/series/$seriesId")
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            parseSeriesDetails(jsonResponse)
        } catch (e: Exception) {
            Log.error(e, "Failed to get MangaUpdates series details")
            null
        }
    }
    
    private fun parseSearchResults(response: JsonObject): List<TrackSearchResult> {
        return try {
            val results = response["results"]?.jsonArray ?: return emptyList()
            
            results.mapNotNull { item ->
                val record = item.jsonObject["record"]?.jsonObject ?: return@mapNotNull null
                val id = record["series_id"]?.jsonPrimitive?.longOrNull ?: return@mapNotNull null
                val title = record["title"]?.jsonPrimitive?.contentOrNull ?: "Unknown"
                
                TrackSearchResult(
                    mediaId = id,
                    mediaUrl = "$BASE_MANGA_URL$id",
                    title = title,
                    totalChapters = record["latest_chapter"]?.jsonPrimitive?.intOrNull ?: 0,
                    coverUrl = record["image"]?.jsonObject?.get("url")?.jsonObject?.get("original")?.jsonPrimitive?.contentOrNull ?: "",
                    summary = record["description"]?.jsonPrimitive?.contentOrNull ?: "",
                    publishingStatus = record["status"]?.jsonPrimitive?.contentOrNull ?: "Unknown",
                    publishingType = record["type"]?.jsonPrimitive?.contentOrNull ?: "Manga",
                    startDate = record["year"]?.jsonPrimitive?.contentOrNull ?: ""
                )
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to parse MangaUpdates search results")
            emptyList()
        }
    }
    
    private fun parseListEntry(response: JsonObject): MangaUpdatesListEntry? {
        return try {
            MangaUpdatesListEntry(
                seriesId = response["series"]?.jsonObject?.get("id")?.jsonPrimitive?.longOrNull ?: return null,
                listId = response["list_id"]?.jsonPrimitive?.intOrNull ?: 0,
                chapter = response["status"]?.jsonObject?.get("chapter")?.jsonPrimitive?.intOrNull ?: 0,
                rating = response["rating"]?.jsonPrimitive?.intOrNull
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to parse MangaUpdates list entry")
            null
        }
    }
    
    private fun parseSeriesDetails(response: JsonObject): MangaUpdatesSeriesDetails? {
        return try {
            MangaUpdatesSeriesDetails(
                seriesId = response["series_id"]?.jsonPrimitive?.longOrNull ?: return null,
                title = response["title"]?.jsonPrimitive?.contentOrNull ?: "",
                latestChapter = response["latest_chapter"]?.jsonPrimitive?.intOrNull ?: 0,
                status = response["status"]?.jsonPrimitive?.contentOrNull ?: ""
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to parse MangaUpdates series details")
            null
        }
    }
}

data class LoginResponse(
    val sessionToken: String,
    val uid: Long,
    val username: String
)

data class MangaUpdatesListEntry(
    val seriesId: Long,
    val listId: Int,
    val chapter: Int,
    val rating: Int?
)

data class MangaUpdatesSeriesDetails(
    val seriesId: Long,
    val title: String,
    val latestChapter: Int,
    val status: String
)

/**
 * MangaUpdates list IDs
 */
object MangaUpdatesListIds {
    const val READING = 0
    const val WISH = 1
    const val COMPLETE = 2
    const val UNFINISHED = 3
    const val ON_HOLD = 4
}

/**
 * Extension to convert TrackStatus to MangaUpdates list ID
 */
fun TrackStatus.toMangaUpdatesListId(): Int = when (this) {
    TrackStatus.Reading -> MangaUpdatesListIds.READING
    TrackStatus.Completed -> MangaUpdatesListIds.COMPLETE
    TrackStatus.OnHold -> MangaUpdatesListIds.ON_HOLD
    TrackStatus.Dropped -> MangaUpdatesListIds.UNFINISHED
    TrackStatus.Planned -> MangaUpdatesListIds.WISH
    TrackStatus.Repeating -> MangaUpdatesListIds.READING
}

/**
 * Extension to convert MangaUpdates list ID to TrackStatus
 */
fun Int.toTrackStatusFromMangaUpdates(): TrackStatus = when (this) {
    MangaUpdatesListIds.READING -> TrackStatus.Reading
    MangaUpdatesListIds.COMPLETE -> TrackStatus.Completed
    MangaUpdatesListIds.ON_HOLD -> TrackStatus.OnHold
    MangaUpdatesListIds.UNFINISHED -> TrackStatus.Dropped
    MangaUpdatesListIds.WISH -> TrackStatus.Planned
    else -> TrackStatus.Planned
}
