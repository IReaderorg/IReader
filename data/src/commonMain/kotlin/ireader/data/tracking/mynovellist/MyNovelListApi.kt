package ireader.data.tracking.mynovellist

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import ireader.core.log.Log
import ireader.domain.models.entities.Track
import ireader.domain.models.entities.TrackSearchResult
import ireader.domain.models.entities.TrackStatus
import kotlinx.serialization.json.*

/**
 * MyNovelList REST API client for tracking novels.
 * 
 * API Documentation: /docs/api on the MyNovelList website
 * 
 * This tracker uses API key authentication instead of OAuth.
 * Users generate an API key from their MyNovelList settings.
 */
class MyNovelListApi(
    private val httpClient: HttpClient,
    private val json: Json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
) {
    companion object {
        // Default MyNovelTracker URL
        private const val DEFAULT_BASE_URL = "https://mynoveltracker.netlify.app"
        const val API_VERSION = "v1"
    }
    
    private var baseUrl: String = DEFAULT_BASE_URL
    private var apiKey: String? = null
    
    fun setBaseUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }
    
    fun getBaseUrl(): String = baseUrl
    
    fun setApiKey(key: String?) {
        apiKey = key
    }
    
    fun hasApiKey(): Boolean = !apiKey.isNullOrEmpty()
    
    /**
     * Search for novels in the MyNovelList database
     */
    suspend fun search(query: String): List<TrackSearchResult> {
        val key = apiKey ?: return emptyList()
        
        return try {
            val response = httpClient.get("$baseUrl/api/$API_VERSION/novels/search") {
                header(HttpHeaders.Authorization, "Bearer $key")
                parameter("q", query)
                parameter("limit", 20)
            }
            
            if (!response.status.isSuccess()) {
                Log.warn { "MyNovelList search failed: ${response.status}" }
                return emptyList()
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            parseSearchResults(jsonResponse)
        } catch (e: Exception) {
            Log.error(e, "MyNovelList search failed for query: $query")
            emptyList()
        }
    }
    
    /**
     * Add novel to user's library (creates progress entry)
     */
    suspend fun addToLibraryById(novelId: String, status: String = "planning", chapter: Int = 0): Boolean {
        val key = apiKey ?: return false
        
        return try {
            val payload = buildJsonObject {
                put("status", status)
                put("current_chapter", chapter)
            }
            
            val response = httpClient.post("$baseUrl/api/$API_VERSION/novels/$novelId/progress") {
                header(HttpHeaders.Authorization, "Bearer $key")
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            
            response.status.isSuccess()
        } catch (e: Exception) {
            Log.error(e, "MyNovelList addToLibraryById failed for id: $novelId")
            false
        }
    }
    
    /**
     * Get all novels from user's library
     */
    suspend fun getLibrary(): List<MyNovelListEntry> {
        val key = apiKey ?: return emptyList()
        
        return try {
            val response = httpClient.get("$baseUrl/api/$API_VERSION/novels") {
                header(HttpHeaders.Authorization, "Bearer $key")
            }
            
            if (!response.status.isSuccess()) {
                Log.warn { "MyNovelList getLibrary failed: ${response.status}" }
                return emptyList()
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            parseLibraryEntries(jsonResponse)
        } catch (e: Exception) {
            Log.error(e, "MyNovelList getLibrary failed")
            emptyList()
        }
    }
    
    /**
     * Get a specific novel by ID
     */
    suspend fun getNovel(novelId: String): MyNovelListEntry? {
        val key = apiKey ?: return null
        
        return try {
            val response = httpClient.get("$baseUrl/api/$API_VERSION/novels/$novelId") {
                header(HttpHeaders.Authorization, "Bearer $key")
            }
            
            if (!response.status.isSuccess()) {
                return null
            }
            
            val responseText = response.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            parseNovelEntry(jsonResponse["data"]?.jsonObject)
        } catch (e: Exception) {
            Log.error(e, "MyNovelList getNovel failed for id: $novelId")
            null
        }
    }
    
    /**
     * Add a novel to user's library (creates novel if not exists, then adds progress)
     */
    suspend fun addToLibrary(track: Track): MyNovelListEntry? {
        val key = apiKey ?: return null
        
        return try {
            // First, create or find the novel
            val novelPayload = buildJsonObject {
                put("title", track.title)
                put("source_url", track.mediaUrl)
                put("total_chapters", track.totalChapters)
            }
            
            val createResponse = httpClient.post("$baseUrl/api/$API_VERSION/novels") {
                header(HttpHeaders.Authorization, "Bearer $key")
                contentType(ContentType.Application.Json)
                setBody(novelPayload.toString())
            }
            
            if (!createResponse.status.isSuccess()) {
                Log.warn { "MyNovelList addToLibrary failed: ${createResponse.status}" }
                return null
            }
            
            val responseText = createResponse.bodyAsText()
            val jsonResponse = json.parseToJsonElement(responseText).jsonObject
            
            parseNovelEntry(jsonResponse["data"]?.jsonObject)
        } catch (e: Exception) {
            Log.error(e, "MyNovelList addToLibrary failed")
            null
        }
    }
    
    /**
     * Update progress on a novel
     */
    suspend fun updateProgress(novelId: String, track: Track): Boolean {
        val key = apiKey ?: return false
        
        return try {
            val payload = buildJsonObject {
                put("status", track.status.toMyNovelListStatus())
                put("current_chapter", track.lastRead.toInt())
                put("score", (track.score * 10).toInt()) // Convert 0-10 to 0-100
                if (track.startReadTime > 0) {
                    put("started_at", epochToDateString(track.startReadTime))
                }
                if (track.endReadTime > 0) {
                    put("completed_at", epochToDateString(track.endReadTime))
                }
            }
            
            val response = httpClient.put("$baseUrl/api/$API_VERSION/novels/$novelId/progress") {
                header(HttpHeaders.Authorization, "Bearer $key")
                contentType(ContentType.Application.Json)
                setBody(payload.toString())
            }
            
            response.status.isSuccess()
        } catch (e: Exception) {
            Log.error(e, "MyNovelList updateProgress failed for id: $novelId")
            false
        }
    }
    
    /**
     * Remove novel from user's library (deletes progress)
     */
    suspend fun removeFromLibrary(novelId: String): Boolean {
        val key = apiKey ?: return false
        
        return try {
            val response = httpClient.delete("$baseUrl/api/$API_VERSION/novels/$novelId") {
                header(HttpHeaders.Authorization, "Bearer $key")
            }
            
            response.status.isSuccess()
        } catch (e: Exception) {
            Log.error(e, "MyNovelList removeFromLibrary failed for id: $novelId")
            false
        }
    }
    
    /**
     * Validate API key by fetching user info
     */
    suspend fun validateApiKey(): Boolean {
        val key = apiKey ?: return false
        
        return try {
            val url = "$baseUrl/api/$API_VERSION/novels"
            Log.debug { "MyNovelList validating API key at: $url" }
            
            val response = httpClient.get(url) {
                header(HttpHeaders.Authorization, "Bearer $key")
                parameter("per_page", 1)
            }
            
            Log.debug { "MyNovelList validation response status: ${response.status}" }
            
            if (!response.status.isSuccess()) {
                val body = try { response.bodyAsText() } catch (e: Exception) { "unable to read body" }
                Log.warn { "MyNovelList API key validation failed with status: ${response.status}, body: $body" }
                return false
            }
            
            true
        } catch (e: Exception) {
            Log.error(e, "MyNovelList API key validation failed with exception")
            false
        }
    }
    
    // ==================== Parsing helpers ====================
    
    private fun parseSearchResults(response: JsonObject): List<TrackSearchResult> {
        return try {
            val data = response["data"]?.jsonObject
            val novels = data?.get("novels")?.jsonArray
                ?: response["novels"]?.jsonArray
                ?: return emptyList()
            
            novels.mapNotNull { item ->
                val obj = item.jsonObject
                val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                
                TrackSearchResult(
                    mediaId = id.hashCode().toLong(),
                    mediaUrl = "$baseUrl/novel/$id",
                    title = obj["title"]?.jsonPrimitive?.contentOrNull ?: "Unknown",
                    totalChapters = obj["total_chapters"]?.jsonPrimitive?.intOrNull ?: 0,
                    coverUrl = obj["cover_url"]?.jsonPrimitive?.contentOrNull ?: "",
                    summary = "",
                    publishingStatus = if (obj["in_library"]?.jsonPrimitive?.booleanOrNull == true) "In Library" else "Not in Library",
                    publishingType = "Novel",
                    startDate = ""
                )
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to parse MyNovelList search results")
            emptyList()
        }
    }
    
    private fun parseLibraryEntries(response: JsonObject): List<MyNovelListEntry> {
        return try {
            val novels = response["data"]?.jsonObject?.get("novels")?.jsonArray
                ?: response["novels"]?.jsonArray
                ?: return emptyList()
            
            novels.mapNotNull { parseNovelEntry(it.jsonObject) }
        } catch (e: Exception) {
            Log.error(e, "Failed to parse MyNovelList library")
            emptyList()
        }
    }
    
    private fun parseNovelEntry(obj: JsonObject?): MyNovelListEntry? {
        if (obj == null) return null
        
        return try {
            val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return null
            val progress = obj["progress"]?.jsonObject
            
            MyNovelListEntry(
                id = id,
                title = obj["title"]?.jsonPrimitive?.contentOrNull ?: "Unknown",
                author = obj["author"]?.jsonPrimitive?.contentOrNull,
                coverUrl = obj["cover_url"]?.jsonPrimitive?.contentOrNull,
                sourceUrl = obj["source_url"]?.jsonPrimitive?.contentOrNull,
                totalChapters = obj["total_chapters"]?.jsonPrimitive?.intOrNull ?: 0,
                // Progress fields
                status = progress?.get("status")?.jsonPrimitive?.contentOrNull ?: "planning",
                currentChapter = progress?.get("current_chapter")?.jsonPrimitive?.intOrNull ?: 0,
                score = progress?.get("score")?.jsonPrimitive?.intOrNull ?: 0,
                startedAt = progress?.get("started_at")?.jsonPrimitive?.contentOrNull,
                completedAt = progress?.get("completed_at")?.jsonPrimitive?.contentOrNull
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to parse MyNovelList entry")
            null
        }
    }
    
    private fun epochToDateString(epochMillis: Long): String {
        if (epochMillis == 0L) return ""
        // Simple date conversion (YYYY-MM-DD format)
        val days = epochMillis / (24 * 60 * 60 * 1000)
        val year = 1970 + (days / 365).toInt()
        val dayOfYear = (days % 365).toInt()
        val month = (dayOfYear / 30) + 1
        val day = (dayOfYear % 30) + 1
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }
}

/**
 * Extension to convert TrackStatus to MyNovelList status string
 */
fun TrackStatus.toMyNovelListStatus(): String = when (this) {
    TrackStatus.Reading -> "reading"
    TrackStatus.Completed -> "completed"
    TrackStatus.OnHold -> "on_hold"
    TrackStatus.Dropped -> "dropped"
    TrackStatus.Planned -> "planning"
    TrackStatus.Repeating -> "reading"
}

/**
 * Extension to convert MyNovelList status string to TrackStatus
 */
fun String.toTrackStatusFromMyNovelList(): TrackStatus = when (this.lowercase()) {
    "reading" -> TrackStatus.Reading
    "completed" -> TrackStatus.Completed
    "on_hold" -> TrackStatus.OnHold
    "dropped" -> TrackStatus.Dropped
    "planning" -> TrackStatus.Planned
    else -> TrackStatus.Planned
}

/**
 * MyNovelList entry data
 */
data class MyNovelListEntry(
    val id: String,
    val title: String,
    val author: String?,
    val coverUrl: String?,
    val sourceUrl: String?,
    val totalChapters: Int,
    val status: String,
    val currentChapter: Int,
    val score: Int,
    val startedAt: String?,
    val completedAt: String?
)
