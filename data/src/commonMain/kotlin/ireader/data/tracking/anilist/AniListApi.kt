package ireader.data.tracking.anilist

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
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
 * AniList GraphQL API client for tracking manga/light novels.
 * 
 * API Documentation: https://anilist.gitbook.io/anilist-apiv2-docs/
 */
class AniListApi(
    private val httpClient: HttpClient,
    private val json: Json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
) {
    companion object {
        const val API_URL = "https://graphql.anilist.co"
        const val AUTH_URL = "https://anilist.co/api/v2/oauth/authorize"
        const val BASE_MANGA_URL = "https://anilist.co/manga/"
        
        // Register your app at https://anilist.co/settings/developer
        // For now using a placeholder - you'll need to register and get your own
        const val CLIENT_ID = "21652"  // IReader client ID (needs registration)
    }
    
    private var accessToken: String? = null
    
    fun setAccessToken(token: String?) {
        accessToken = token
    }
    
    fun getAuthUrl(): String {
        return "$AUTH_URL?client_id=$CLIENT_ID&response_type=token"
    }
    
    /**
     * Search for manga/light novels on AniList
     */
    suspend fun search(query: String): List<TrackSearchResult> {
        val graphqlQuery = """
            query Search(${'$'}query: String) {
                Page(perPage: 30) {
                    media(search: ${'$'}query, type: MANGA) {
                        id
                        title {
                            userPreferred
                            romaji
                            english
                            native
                        }
                        coverImage {
                            large
                        }
                        format
                        status
                        chapters
                        description(asHtml: false)
                        startDate {
                            year
                            month
                            day
                        }
                    }
                }
            }
        """.trimIndent()
        
        val variables = buildJsonObject {
            put("query", query)
        }
        
        val response = executeQuery(graphqlQuery, variables)
        return parseSearchResults(response)
    }

    /**
     * Get current authenticated user info
     */
    suspend fun getCurrentUser(): AniListUser? {
        val graphqlQuery = """
            query {
                Viewer {
                    id
                    name
                    mediaListOptions {
                        scoreFormat
                    }
                }
            }
        """.trimIndent()
        
        val response = executeQuery(graphqlQuery, buildJsonObject {})
        return parseUser(response)
    }
    
    /**
     * Add manga to user's list
     */
    suspend fun addToList(track: Track): Long? {
        val graphqlQuery = """
            mutation AddManga(${'$'}mediaId: Int, ${'$'}status: MediaListStatus, ${'$'}progress: Int) {
                SaveMediaListEntry(mediaId: ${'$'}mediaId, status: ${'$'}status, progress: ${'$'}progress) {
                    id
                    status
                    progress
                }
            }
        """.trimIndent()
        
        val variables = buildJsonObject {
            put("mediaId", track.mediaId.toInt())
            put("status", track.status.toAniListStatus())
            put("progress", track.lastRead.toInt())
        }
        
        val response = executeQuery(graphqlQuery, variables)
        return parseAddResult(response)
    }
    
    /**
     * Update an existing list entry
     */
    suspend fun updateEntry(track: Track): Boolean {
        val graphqlQuery = """
            mutation UpdateManga(
                ${'$'}id: Int,
                ${'$'}status: MediaListStatus,
                ${'$'}progress: Int,
                ${'$'}score: Float,
                ${'$'}startedAt: FuzzyDateInput,
                ${'$'}completedAt: FuzzyDateInput
            ) {
                SaveMediaListEntry(
                    id: ${'$'}id,
                    status: ${'$'}status,
                    progress: ${'$'}progress,
                    scoreRaw: ${'$'}score,
                    startedAt: ${'$'}startedAt,
                    completedAt: ${'$'}completedAt
                ) {
                    id
                    status
                    progress
                    score
                }
            }
        """.trimIndent()
        
        val variables = buildJsonObject {
            put("id", track.entryId.toInt())
            put("status", track.status.toAniListStatus())
            put("progress", track.lastRead.toInt())
            put("score", track.score * 10) // AniList uses 0-100 scale
            put("startedAt", createFuzzyDate(track.startReadTime))
            put("completedAt", createFuzzyDate(track.endReadTime))
        }
        
        val response = executeQuery(graphqlQuery, variables)
        return response.containsKey("data")
    }
    
    /**
     * Delete a list entry
     */
    suspend fun deleteEntry(entryId: Long): Boolean {
        val graphqlQuery = """
            mutation DeleteEntry(${'$'}id: Int) {
                DeleteMediaListEntry(id: ${'$'}id) {
                    deleted
                }
            }
        """.trimIndent()
        
        val variables = buildJsonObject {
            put("id", entryId.toInt())
        }
        
        val response = executeQuery(graphqlQuery, variables)
        return response["data"]?.jsonObject
            ?.get("DeleteMediaListEntry")?.jsonObject
            ?.get("deleted")?.jsonPrimitive?.booleanOrNull == true
    }
    
    /**
     * Find manga in user's list by media ID
     */
    suspend fun findInUserList(mediaId: Long, userId: Int): AniListMediaListEntry? {
        val graphqlQuery = """
            query FindInList(${'$'}userId: Int!, ${'$'}mediaId: Int!) {
                Page {
                    mediaList(userId: ${'$'}userId, mediaId: ${'$'}mediaId, type: MANGA) {
                        id
                        status
                        progress
                        score(format: POINT_100)
                        startedAt {
                            year
                            month
                            day
                        }
                        completedAt {
                            year
                            month
                            day
                        }
                        media {
                            id
                            title {
                                userPreferred
                            }
                            chapters
                        }
                    }
                }
            }
        """.trimIndent()
        
        val variables = buildJsonObject {
            put("userId", userId)
            put("mediaId", mediaId.toInt())
        }
        
        val response = executeQuery(graphqlQuery, variables)
        return parseMediaListEntry(response)
    }

    /**
     * Execute a GraphQL query against AniList API
     */
    private suspend fun executeQuery(query: String, variables: JsonObject): JsonObject {
        val token = accessToken
        
        val payload = buildJsonObject {
            put("query", query)
            put("variables", variables)
        }
        
        try {
            val response = httpClient.post(API_URL) {
                contentType(ContentType.Application.Json)
                if (token != null) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
                setBody(payload.toString())
            }
            
            val responseText = response.bodyAsText()
            return json.parseToJsonElement(responseText).jsonObject
        } catch (e: Exception) {
            Log.error(e, "AniList API error")
            return buildJsonObject { }
        }
    }
    
    private fun parseSearchResults(response: JsonObject): List<TrackSearchResult> {
        return try {
            val media = response["data"]?.jsonObject
                ?.get("Page")?.jsonObject
                ?.get("media")?.jsonArray
                ?: return emptyList()
            
            media.mapNotNull { item ->
                val obj = item.jsonObject
                val id = obj["id"]?.jsonPrimitive?.longOrNull ?: return@mapNotNull null
                val title = obj["title"]?.jsonObject?.let { titleObj ->
                    titleObj["userPreferred"]?.jsonPrimitive?.contentOrNull
                        ?: titleObj["romaji"]?.jsonPrimitive?.contentOrNull
                        ?: titleObj["english"]?.jsonPrimitive?.contentOrNull
                        ?: "Unknown"
                } ?: "Unknown"
                
                TrackSearchResult(
                    mediaId = id,
                    mediaUrl = "$BASE_MANGA_URL$id",
                    title = title,
                    totalChapters = obj["chapters"]?.jsonPrimitive?.intOrNull ?: 0,
                    coverUrl = obj["coverImage"]?.jsonObject
                        ?.get("large")?.jsonPrimitive?.contentOrNull ?: "",
                    summary = obj["description"]?.jsonPrimitive?.contentOrNull
                        ?.replace(Regex("<[^>]*>"), "") ?: "",
                    publishingStatus = obj["status"]?.jsonPrimitive?.contentOrNull ?: "Unknown",
                    publishingType = obj["format"]?.jsonPrimitive?.contentOrNull ?: "Unknown",
                    startDate = parseStartDate(obj["startDate"]?.jsonObject)
                )
            }
        } catch (e: Exception) {
            Log.error(e, "Failed to parse AniList search results")
            emptyList()
        }
    }
    
    private fun parseUser(response: JsonObject): AniListUser? {
        return try {
            val viewer = response["data"]?.jsonObject?.get("Viewer")?.jsonObject
                ?: return null
            
            AniListUser(
                id = viewer["id"]?.jsonPrimitive?.int ?: return null,
                name = viewer["name"]?.jsonPrimitive?.content ?: "Unknown",
                scoreFormat = viewer["mediaListOptions"]?.jsonObject
                    ?.get("scoreFormat")?.jsonPrimitive?.contentOrNull ?: "POINT_100"
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to parse AniList user")
            null
        }
    }
    
    private fun parseAddResult(response: JsonObject): Long? {
        return try {
            response["data"]?.jsonObject
                ?.get("SaveMediaListEntry")?.jsonObject
                ?.get("id")?.jsonPrimitive?.longOrNull
        } catch (e: Exception) {
            Log.error(e, "Failed to parse AniList add result")
            null
        }
    }
    
    private fun parseMediaListEntry(response: JsonObject): AniListMediaListEntry? {
        return try {
            val mediaList = response["data"]?.jsonObject
                ?.get("Page")?.jsonObject
                ?.get("mediaList")?.jsonArray
                ?.firstOrNull()?.jsonObject
                ?: return null
            
            AniListMediaListEntry(
                id = mediaList["id"]?.jsonPrimitive?.long ?: return null,
                status = mediaList["status"]?.jsonPrimitive?.contentOrNull ?: "PLANNING",
                progress = mediaList["progress"]?.jsonPrimitive?.int ?: 0,
                score = mediaList["score"]?.jsonPrimitive?.floatOrNull ?: 0f,
                startedAt = parseFuzzyDate(mediaList["startedAt"]?.jsonObject),
                completedAt = parseFuzzyDate(mediaList["completedAt"]?.jsonObject),
                mediaId = mediaList["media"]?.jsonObject?.get("id")?.jsonPrimitive?.long ?: 0,
                mediaTitle = mediaList["media"]?.jsonObject?.get("title")?.jsonObject
                    ?.get("userPreferred")?.jsonPrimitive?.contentOrNull ?: "",
                totalChapters = mediaList["media"]?.jsonObject?.get("chapters")?.jsonPrimitive?.intOrNull ?: 0
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to parse AniList media list entry")
            null
        }
    }
    
    private fun parseStartDate(dateObj: JsonObject?): String {
        if (dateObj == null) return ""
        val year = dateObj["year"]?.jsonPrimitive?.intOrNull
        val month = dateObj["month"]?.jsonPrimitive?.intOrNull
        val day = dateObj["day"]?.jsonPrimitive?.intOrNull
        return if (year != null) {
            buildString {
                append(year)
                if (month != null) {
                    append("-")
                    append(month.toString().padStart(2, '0'))
                    if (day != null) {
                        append("-")
                        append(day.toString().padStart(2, '0'))
                    }
                }
            }
        } else ""
    }
    
    private fun parseFuzzyDate(dateObj: JsonObject?): Long {
        if (dateObj == null) return 0
        val year = dateObj["year"]?.jsonPrimitive?.intOrNull ?: return 0
        val month = dateObj["month"]?.jsonPrimitive?.intOrNull ?: 1
        val day = dateObj["day"]?.jsonPrimitive?.intOrNull ?: 1
        // Simple epoch calculation (approximate)
        return ((year - 1970) * 365L + (month - 1) * 30L + day) * 24 * 60 * 60 * 1000
    }
    
    private fun createFuzzyDate(epochMillis: Long): JsonObject {
        if (epochMillis == 0L) {
            return buildJsonObject {
                put("year", JsonNull)
                put("month", JsonNull)
                put("day", JsonNull)
            }
        }
        // Simple date extraction (approximate)
        val days = epochMillis / (24 * 60 * 60 * 1000)
        val year = 1970 + (days / 365).toInt()
        val dayOfYear = (days % 365).toInt()
        val month = (dayOfYear / 30) + 1
        val day = (dayOfYear % 30) + 1
        
        return buildJsonObject {
            put("year", year)
            put("month", month.coerceIn(1, 12))
            put("day", day.coerceIn(1, 28))
        }
    }
}

/**
 * Extension to convert TrackStatus to AniList status string
 */
fun TrackStatus.toAniListStatus(): String = when (this) {
    TrackStatus.Reading -> "CURRENT"
    TrackStatus.Completed -> "COMPLETED"
    TrackStatus.OnHold -> "PAUSED"
    TrackStatus.Dropped -> "DROPPED"
    TrackStatus.Planned -> "PLANNING"
    TrackStatus.Repeating -> "REPEATING"
}

/**
 * Extension to convert AniList status string to TrackStatus
 */
fun String.toTrackStatus(): TrackStatus = when (this.uppercase()) {
    "CURRENT" -> TrackStatus.Reading
    "COMPLETED" -> TrackStatus.Completed
    "PAUSED" -> TrackStatus.OnHold
    "DROPPED" -> TrackStatus.Dropped
    "PLANNING" -> TrackStatus.Planned
    "REPEATING" -> TrackStatus.Repeating
    else -> TrackStatus.Planned
}

/**
 * AniList user data
 */
data class AniListUser(
    val id: Int,
    val name: String,
    val scoreFormat: String
)

/**
 * AniList media list entry
 */
data class AniListMediaListEntry(
    val id: Long,
    val status: String,
    val progress: Int,
    val score: Float,
    val startedAt: Long,
    val completedAt: Long,
    val mediaId: Long,
    val mediaTitle: String,
    val totalChapters: Int
)
