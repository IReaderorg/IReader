package ireader.server.model

import kotlinx.serialization.Serializable
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo

/**
 * Reuse existing IReader models directly.
 * These are already @Serializable and work with Ktor.
 */

// Re-export existing models for API responses
typealias BookDto = MangaInfo
typealias ChapterDto = ChapterInfo

@Serializable
data class PageDto(
    val index: Int,
    val url: String
)

@Serializable
data class SourceDto(
    val id: Long,
    val name: String,
    val lang: String,
    val supportsLatest: Boolean,
    val iconUrl: String? = null
)

@Serializable
data class SearchQuery(
    val query: String,
    val page: Int = 1
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String? = null
)

@Serializable
data class ServerInfo(
    val name: String,
    val version: String,
    val port: String
)
