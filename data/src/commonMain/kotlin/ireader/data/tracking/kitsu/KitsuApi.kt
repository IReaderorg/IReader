package ireader.data.tracking.kitsu

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
 * Kitsu API client for tracking manga.
 * Uses OAuth2 for authentication with username/password.
 * 
 * API Documentation: https://kitsu.docs.apiary.io/
 */
class KitsuApi(
    private val httpClient: HttpClient,
    private val json: Json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
) {
    companion object {
        const val API_URL = "https://kitsu.io/api/edge"
        const val AUTH_URL = "https://kitsu.io/api/oauth/token"
        const val BASE_MANGA_URL = "https://kitsu.io/manga/"
        
        // Kitsu uses public client credentials
        const val CLIENT_ID = "dd031b32d2f56c990b1425efe6c42ad847e7fe3ab46bf1299f05ecd856bdb7dd"
        const val CLIENT_SECRET = "54d7307928f63414defd96399fc31ba847961ceaecef3a5fd93144e960c0e151"
    }
    
    private var accessToken: String? = null
    private var userId: String? = null
    
    fun setAccessToken(token: String?) {
        accessToken = token
    }
    
    fun setUserId(id: String?) {
        userId = id
    }
    
    /**
     * Login with username and password
     */
    suspend fun login(username: String, password: String): TokenResponse? {
        return try {
            val response = httpClient.post(AUTH_URL) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("grant_type=password&username=$username&password=$password")
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            TokenResponse(
                accessToken = jsonResponse["access_token"]?.jsonPrimitive?.content ?: return null,
                refreshToken = jsonResponse["refresh_token"]?.jsonPrimitive?.contentOrNull,
                expiresIn = jsonResponse["expires_in"]?.jsonPrimitive?.intOrNull ?: 0
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to login to Kitsu")
            null
        }
    }
    
    /**
     * Get current user info
     */
    suspend fun getCurrentUser(): KitsuUser? {
        val token = accessToken ?: return null
        
        return try {
            val response = httpClient.get("$API_URL/users") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("filter[self]", "true")
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            val data = jsonResponse["data"]?.jsonArray?.firstOrNull()?.jsonObject ?: return null
            
            KitsuUser(
                id = data["id"]?.jsonPrimitive?.content ?: return null,
                name = data["attributes"]?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull ?: "Unknown"
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to get Kitsu user")
            null
        }
    }
    
    /**
     * Search for manga
     */
    suspend fun search(query: String): List<TrackSearchResult> {
        return try {
            val response = httpClient.get("$API_URL/manga") {
                parameter("filter[text]", query)
                parameter("page[limit]", 30)
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            parseSearchResults(jsonResponse)
        } catch (e: Exception) {
            Log.error(e, "Kitsu search failed for query: $query")
            emptyList()
        }
    }
    
    /**
     * Add manga to user's library
     */
    suspend fun addToLibrary(track: Track): String? {
        val token = accessToken ?: return null
        val uid = userId ?: return null
        
        return try {
            val payload = buildJsonObject {
                put("data", buildJsonObject {
                    put("type", "libraryEntries")
                    put("attributes", buildJsonObject {
                        put("status", track.status.toKitsuStatus())
                        put("progress", track.lastRead.toInt())
                        if (track.score > 0) put("ratingTwenty", (track.score * 2).toInt())
                    })
                    put("relationships", buildJsonObject {
                        put("user", buildJsonObject {
                            put("data", buildJsonObject {
                                put("type", "users")
                                put("id", uid)
                            })
                        })
                        put("manga", buildJsonObject {
                            put("data", buildJsonObject {
                                put("type", "manga")
                                put("id", track.mediaId.toString())
                            })
                        })
                    })
                })
            }
            
            val response = httpClient.post("$API_URL/library-entries") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                header("Accept", "application/vnd.api+json")
                setBody(payload.toString())
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            jsonResponse["data"]?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull
        } catch (e: Exception) {
            Log.error(e, "Failed to add manga to Kitsu library")
            null
        }
    }
    
    /**
     * Update library entry
     */
    suspend fun updateEntry(entryId: String, track: Track): Boolean {
        val token = accessToken ?: return false
        
        return try {
            val payload = buildJsonObject {
                put("data", buildJsonObject {
                    put("id", entryId)
                    put("type", "libraryEntries")
                    put("attributes", buildJsonObject {
                        put("status", track.status.toKitsuStatus())
                        put("progress", track.lastRead.toInt())
                        if (track.score > 0) put("ratingTwenty", (track.score * 2).toInt())
                    })
                })
            }
            
            val response = httpClient.patch("$API_URL/library-entries/$entryId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                header("Accept", "application/vnd.api+json")
                setBody(payload.toString())
            }
            
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.error(e, "Failed to update Kitsu library entry")
            false
        }
    }
    
    /**
     * Delete library entry
     */
    suspend fun deleteEntry(entryId: String): Boolean {
        val token = accessToken ?: return false
        
        return try {
            val response = httpClient.delete("$API_URL/library-entries/$entryId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            
            response.status.value in 200..299
        } catch (e: Exception) {
            Log.error(e, "Failed to delete Kitsu library entry")
            false
        }
    }
    
    /**
     * Find manga in user's library
     */
    suspend fun findInLibrary(mediaId: Long): KitsuLibraryEntry? {
        val token = accessToken ?: return null
        val uid = userId ?: return null
        
        return try {
            val response = httpClient.get("$API_URL/library-entries") {
                header(HttpHeaders.Authorization, "Bearer $token")
                parameter("filter[userId]", uid)
                parameter("filter[mangaId]", mediaId)
                parameter("include", "manga")
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            parseLibraryEntry(jsonResponse)
        } catch (e: Exception) {
            Log.error(e, "Failed to find manga in Kitsu library")
            null
        }
    }
    
    private fun parseSearchResults(response: JsonObject): List<TrackSearchResult> {
        return try {
            val data = response["data"]?.jsonArray ?: return emptyList()
            
            data.mapNotNull { item ->
                val obj = item.jsonObject
                val id = obj["id"]?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: return@mapNotNull null
                val attrs = obj["attributes"]?.jsonObject ?: return@mapNotNull null
                
                val title = attrs["canonicalTitle"]?.jsonPrimitive?.contentOrNull
                    ?: attrs["titles"]?.jsonObject?.values?.firstOrNull()?.jsonPrimitive?.contentOrNull
                    ?: "Unknown"
                
                TrackSearchResult(
                    mediaId = id,
                    mediaUrl = "$BASE_MANGA_URL$id",
                    title = title,
                    totalChapters = attrs["chapterCount"]?.jsonPrimitive?.intOrNull ?: 0,
                    coverUrl = attrs["posterImage"]?.jsonObject?.get("large")?.jsonPrimitive?.contentOrNull
                        ?: attrs["posterImage"]?.jsonObject?.get("medium")?.jsonPrimitive?.contentOrNull ?: "",
                    summary = attrs["synopsis"]?.jsonPrimitive?.contentOrNull ?: "",
                    publishingStatus = attrs["status"]?.jsonPrimitive?.contentOrNull ?: "Unknown",
                    publishingType = attrs["mangaType"]?.jsonPrimitive?.contentOrNull ?: "Manga",
                    startDate = attrs["startDate"]?.jsonPrimitive?.contentOrNull ?: ""
                )
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to parse Kitsu search results")
            emptyList()
        }
    }
    
    private fun parseLibraryEntry(response: JsonObject): KitsuLibraryEntry? {
        return try {
            val data = response["data"]?.jsonArray?.firstOrNull()?.jsonObject ?: return null
            val attrs = data["attributes"]?.jsonObject ?: return null
            
            // Get manga info from included
            val included = response["included"]?.jsonArray?.firstOrNull()?.jsonObject
            val mangaAttrs = included?.get("attributes")?.jsonObject
            
            KitsuLibraryEntry(
                id = data["id"]?.jsonPrimitive?.content ?: return null,
                status = attrs["status"]?.jsonPrimitive?.contentOrNull ?: "planned",
                progress = attrs["progress"]?.jsonPrimitive?.intOrNull ?: 0,
                ratingTwenty = attrs["ratingTwenty"]?.jsonPrimitive?.intOrNull,
                mediaId = included?.get("id")?.jsonPrimitive?.contentOrNull?.toLongOrNull() ?: 0,
                mediaTitle = mangaAttrs?.get("canonicalTitle")?.jsonPrimitive?.contentOrNull ?: "",
                totalChapters = mangaAttrs?.get("chapterCount")?.jsonPrimitive?.intOrNull ?: 0
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to parse Kitsu library entry")
            null
        }
    }
}

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Int
)

data class KitsuUser(
    val id: String,
    val name: String
)

data class KitsuLibraryEntry(
    val id: String,
    val status: String,
    val progress: Int,
    val ratingTwenty: Int?,
    val mediaId: Long,
    val mediaTitle: String,
    val totalChapters: Int
)

/**
 * Extension to convert TrackStatus to Kitsu status string
 */
fun TrackStatus.toKitsuStatus(): String = when (this) {
    TrackStatus.Reading -> "current"
    TrackStatus.Completed -> "completed"
    TrackStatus.OnHold -> "on_hold"
    TrackStatus.Dropped -> "dropped"
    TrackStatus.Planned -> "planned"
    TrackStatus.Repeating -> "current"
}

/**
 * Extension to convert Kitsu status string to TrackStatus
 */
fun String.toTrackStatusFromKitsu(): TrackStatus = when (this.lowercase()) {
    "current" -> TrackStatus.Reading
    "completed" -> TrackStatus.Completed
    "on_hold" -> TrackStatus.OnHold
    "dropped" -> TrackStatus.Dropped
    "planned" -> TrackStatus.Planned
    else -> TrackStatus.Planned
}
