package ireader.data.tracking.mangabaka

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import ireader.core.log.Log
import ireader.domain.models.entities.Track
import ireader.domain.models.entities.TrackSearchResult
import ireader.domain.models.entities.TrackStatus
import ireader.domain.models.entities.TrackerService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * Repository for MangaBaka tracking (https://mangabaka.org).
 *
 * MangaBaka exposes a public, read-only metadata API — there are no user lists,
 * so no authentication is required. Reading progress is kept locally and sync
 * only refreshes remote metadata (total chapters, publishing status).
 *
 * API docs: https://mangabaka.org/data/api
 */
class MangaBakaRepositoryImpl(
    private val httpClient: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    companion object {
        const val API_URL = "https://api.mangabaka.org/v1"
        const val BASE_SERIES_URL = "https://mangabaka.org/"
    }

    /** No auth needed — always available. */
    fun isAuthenticated(): Boolean = true

    /**
     * Search series: GET /v1/series/search?q=...
     */
    suspend fun search(query: String): List<TrackSearchResult> {
        return try {
            val response = httpClient.get("$API_URL/series/search") {
                url.parameters.append("q", query)
            }
            val root = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = root["data"]?.jsonArray ?: return emptyList()

            data.mapNotNull { item ->
                val record = item.jsonObject
                val id = record["id"]?.jsonPrimitive?.longOrNull ?: return@mapNotNull null
                TrackSearchResult(
                    mediaId = id,
                    mediaUrl = "$BASE_SERIES_URL$id",
                    title = record["title"]?.jsonPrimitive?.contentOrNull ?: "Unknown",
                    totalChapters = record["total_chapters"]?.jsonPrimitive?.intOrNull ?: 0,
                    coverUrl = record["cover"]?.jsonObject
                        ?.get("raw")?.jsonObject
                        ?.get("url")?.jsonPrimitive?.contentOrNull ?: "",
                    summary = record["description"]?.jsonPrimitive?.contentOrNull ?: "",
                    publishingStatus = record["status"]?.jsonPrimitive?.contentOrNull ?: "Unknown",
                    publishingType = record["type"]?.jsonPrimitive?.contentOrNull ?: "Manga",
                    startDate = record["year"]?.jsonPrimitive?.contentOrNull ?: ""
                )
            }
        } catch (e: Exception) {
            Log.error(e, "MangaBaka search failed for query: $query")
            emptyList()
        }
    }

    /**
     * Series details: GET /v1/series/{id}
     * Returns total chapters and publishing status, or null on failure.
     */
    private suspend fun getSeries(seriesId: Long): Pair<Int, String>? {
        return try {
            val response = httpClient.get("$API_URL/series/$seriesId")
            val data = json.parseToJsonElement(response.bodyAsText())
                .jsonObject["data"]?.jsonObject ?: return null
            val totalChapters = data["total_chapters"]?.jsonPrimitive?.intOrNull ?: 0
            val status = data["status"]?.jsonPrimitive?.contentOrNull ?: ""
            totalChapters to status
        } catch (e: Exception) {
            Log.error(e, "Failed to get MangaBaka series $seriesId")
            null
        }
    }

    /**
     * Bind a book — no remote list, so just build a local track from metadata.
     */
    suspend fun bindBook(bookId: Long, searchResult: TrackSearchResult): Track? {
        return try {
            val remote = getSeries(searchResult.mediaId)
            Track(
                mangaId = bookId,
                siteId = TrackerService.MANGABAKA,
                entryId = 0,
                mediaId = searchResult.mediaId,
                mediaUrl = searchResult.mediaUrl,
                title = searchResult.title,
                lastRead = 0f,
                totalChapters = remote?.first ?: searchResult.totalChapters,
                score = 0f,
                status = TrackStatus.Planned,
                startReadTime = 0,
                endReadTime = 0
            )
        } catch (e: Exception) {
            Log.error(e, "Failed to bind book to MangaBaka")
            null
        }
    }

    /**
     * Sync — refresh remote metadata only; progress/status stay local.
     */
    suspend fun syncTrack(track: Track): Track? {
        val remote = getSeries(track.mediaId) ?: return null
        return track.copy(totalChapters = remote.first)
    }

    /** No remote list — updates are local-only, always succeed. */
    suspend fun updateTrack(track: Track): Boolean = true

    /** No remote list — nothing to delete remotely. */
    suspend fun deleteTrack(seriesId: Long): Boolean = true
}
