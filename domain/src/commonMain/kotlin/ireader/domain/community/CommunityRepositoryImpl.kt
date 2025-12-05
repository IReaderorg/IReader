package ireader.domain.community

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.MangaInfo
import ireader.core.source.model.MangasPageInfo
import ireader.domain.preferences.prefs.SupabasePreferences
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Implementation of CommunityRepository that connects to Supabase.
 */
class CommunityRepositoryImpl(
    private val httpClient: HttpClient,
    private val supabasePreferences: SupabasePreferences,
    private val communityPreferences: CommunityPreferences
) : CommunityRepository {
    
    companion object {
        private const val PAGE_SIZE = 20
    }
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private fun getBaseUrl(): String {
        val customUrl = communityPreferences.communitySourceUrl().get()
        return if (customUrl.isNotBlank()) {
            customUrl.trimEnd('/')
        } else {
            // Fall back to library URL if community URL not set
            supabasePreferences.supabaseLibraryUrl().get().trimEnd('/')
        }
    }
    
    private fun getApiKey(): String {
        val customKey = communityPreferences.communitySourceApiKey().get()
        return if (customKey.isNotBlank()) {
            customKey
        } else {
            supabasePreferences.supabaseLibraryKey().get()
        }
    }
    
    override suspend fun getLatestBooks(page: Int): MangasPageInfo {
        return fetchBooks(
            endpoint = "/rest/v1/community_books",
            orderBy = "created_at.desc",
            page = page
        )
    }
    
    override suspend fun getPopularBooks(page: Int): MangasPageInfo {
        return fetchBooks(
            endpoint = "/rest/v1/community_books",
            orderBy = "view_count.desc",
            page = page
        )
    }
    
    override suspend fun getRecentlyTranslatedBooks(page: Int): MangasPageInfo {
        return fetchBooks(
            endpoint = "/rest/v1/community_books",
            orderBy = "last_updated.desc",
            page = page
        )
    }
    
    override suspend fun searchBooks(
        query: String,
        language: String?,
        genre: String?,
        status: String?,
        page: Int
    ): MangasPageInfo {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()
        
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return MangasPageInfo.empty()
        }
        
        try {
            val offset = (page - 1) * PAGE_SIZE
            
            val response = httpClient.get("$baseUrl/rest/v1/community_books") {
                header("apikey", apiKey)
                header("Authorization", "Bearer $apiKey")
                
                // Build filter query
                val filters = mutableListOf<String>()
                if (query.isNotBlank()) {
                    filters.add("or=(title.ilike.*$query*,author.ilike.*$query*)")
                }
                language?.let { filters.add("available_languages.cs.{$it}") }
                genre?.let { filters.add("genres.cs.{$it}") }
                status?.let { filters.add("status.eq.$it") }
                
                if (filters.isNotEmpty()) {
                    parameter("and", "(${filters.joinToString(",")})")
                }
                
                parameter("order", "view_count.desc")
                parameter("offset", offset)
                parameter("limit", PAGE_SIZE)
            }
            
            if (!response.status.isSuccess()) {
                return MangasPageInfo.empty()
            }
            
            val books = json.decodeFromString<List<CommunityBookDto>>(response.bodyAsText())
            return MangasPageInfo(
                mangas = books.map { it.toMangaInfo() },
                hasNextPage = books.size >= PAGE_SIZE
            )
        } catch (e: Exception) {
            return MangasPageInfo.empty()
        }
    }
    
    override suspend fun getBookDetails(bookKey: String): MangaInfo {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()
        
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return MangaInfo(key = bookKey, title = "Unknown")
        }
        
        try {
            val response = httpClient.get("$baseUrl/rest/v1/community_books") {
                header("apikey", apiKey)
                header("Authorization", "Bearer $apiKey")
                parameter("id", "eq.$bookKey")
                parameter("select", "*")
            }
            
            if (!response.status.isSuccess()) {
                return MangaInfo(key = bookKey, title = "Unknown")
            }
            
            val books = json.decodeFromString<List<CommunityBookDto>>(response.bodyAsText())
            return books.firstOrNull()?.toMangaInfo() ?: MangaInfo(key = bookKey, title = "Unknown")
        } catch (e: Exception) {
            return MangaInfo(key = bookKey, title = "Unknown")
        }
    }
    
    override suspend fun getChapters(bookKey: String, language: String?): List<ChapterInfo> {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()
        
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return emptyList()
        }
        
        try {
            val response = httpClient.get("$baseUrl/rest/v1/community_chapters") {
                header("apikey", apiKey)
                header("Authorization", "Bearer $apiKey")
                parameter("book_id", "eq.$bookKey")
                language?.let { parameter("language", "eq.$it") }
                parameter("order", "number.asc")
                parameter("select", "id,book_id,name,number,language,translator_name,rating,created_at")
            }
            
            if (!response.status.isSuccess()) {
                return emptyList()
            }
            
            val chapters = json.decodeFromString<List<CommunityChapterDto>>(response.bodyAsText())
            return chapters.map { it.toChapterInfo() }
        } catch (e: Exception) {
            return emptyList()
        }
    }
    
    override suspend fun getChapterContent(chapterKey: String): String {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()
        
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return "Community Source not configured. Please configure the Community Source URL in settings."
        }
        
        try {
            val response = httpClient.get("$baseUrl/rest/v1/community_chapters") {
                header("apikey", apiKey)
                header("Authorization", "Bearer $apiKey")
                parameter("id", "eq.$chapterKey")
                parameter("select", "content")
            }
            
            if (!response.status.isSuccess()) {
                return "Failed to load chapter content."
            }
            
            val chapters = json.decodeFromString<List<ChapterContentDto>>(response.bodyAsText())
            return chapters.firstOrNull()?.content ?: "Chapter content not found."
        } catch (e: Exception) {
            return "Error loading chapter: ${e.message}"
        }
    }
    
    override suspend fun submitBook(book: CommunityBook): Result<String> {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()
        
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return Result.failure(Exception("Community Source not configured"))
        }
        
        return try {
            val dto = CommunityBookDto(
                id = book.id.ifBlank { null },
                title = book.title,
                author = book.author,
                description = book.description,
                cover = book.cover,
                genres = book.genres,
                status = book.status,
                originalLanguage = book.originalLanguage,
                availableLanguages = book.availableLanguages,
                contributorId = book.contributorId,
                contributorName = book.contributorName,
                viewCount = book.viewCount,
                chapterCount = book.chapterCount,
                lastUpdated = currentTimeToLong(),
                createdAt = book.createdAt.takeIf { it > 0 } ?: currentTimeToLong()
            )
            
            val response = httpClient.post("$baseUrl/rest/v1/community_books") {
                header("apikey", apiKey)
                header("Authorization", "Bearer $apiKey")
                header("Prefer", "return=representation")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(CommunityBookDto.serializer(), dto))
            }
            
            if (response.status.isSuccess()) {
                val result = json.decodeFromString<List<CommunityBookDto>>(response.bodyAsText())
                Result.success(result.firstOrNull()?.id ?: "")
            } else {
                Result.failure(Exception("Failed to submit book: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun submitChapter(chapter: CommunityChapter): Result<String> {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()
        
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return Result.failure(Exception("Community Source not configured"))
        }
        
        return try {
            val dto = CommunityChapterDto(
                id = chapter.id.ifBlank { null },
                bookId = chapter.bookId,
                name = chapter.name,
                number = chapter.number,
                content = chapter.content,
                language = chapter.language,
                translatorId = chapter.translatorId,
                translatorName = chapter.translatorName,
                originalChapterKey = chapter.originalChapterKey,
                rating = chapter.rating,
                ratingCount = chapter.ratingCount,
                viewCount = chapter.viewCount,
                createdAt = chapter.createdAt.takeIf { it > 0 } ?: currentTimeToLong(),
                updatedAt = currentTimeToLong()
            )
            
            val response = httpClient.post("$baseUrl/rest/v1/community_chapters") {
                header("apikey", apiKey)
                header("Authorization", "Bearer $apiKey")
                header("Prefer", "return=representation")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(CommunityChapterDto.serializer(), dto))
            }
            
            if (response.status.isSuccess()) {
                val result = json.decodeFromString<List<CommunityChapterDto>>(response.bodyAsText())
                Result.success(result.firstOrNull()?.id ?: "")
            } else {
                Result.failure(Exception("Failed to submit chapter: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAvailableLanguages(bookKey: String): List<String> {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()
        
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return emptyList()
        }
        
        try {
            val response = httpClient.get("$baseUrl/rest/v1/community_books") {
                header("apikey", apiKey)
                header("Authorization", "Bearer $apiKey")
                parameter("id", "eq.$bookKey")
                parameter("select", "available_languages")
            }
            
            if (!response.status.isSuccess()) {
                return emptyList()
            }
            
            val books = json.decodeFromString<List<LanguagesDto>>(response.bodyAsText())
            return books.firstOrNull()?.availableLanguages ?: emptyList()
        } catch (e: Exception) {
            return emptyList()
        }
    }
    
    override suspend fun reportChapter(chapterKey: String, reason: String): Result<Unit> {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()
        
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return Result.failure(Exception("Community Source not configured"))
        }
        
        return try {
            val response = httpClient.post("$baseUrl/rest/v1/chapter_reports") {
                header("apikey", apiKey)
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody("""{"chapter_id": "$chapterKey", "reason": "$reason", "created_at": ${currentTimeToLong()}}""")
            }
            
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to report chapter"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserContributions(userId: String): List<CommunityBook> {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()
        
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return emptyList()
        }
        
        try {
            val response = httpClient.get("$baseUrl/rest/v1/community_books") {
                header("apikey", apiKey)
                header("Authorization", "Bearer $apiKey")
                parameter("contributor_id", "eq.$userId")
                parameter("order", "created_at.desc")
            }
            
            if (!response.status.isSuccess()) {
                return emptyList()
            }
            
            val books = json.decodeFromString<List<CommunityBookDto>>(response.bodyAsText())
            return books.map { it.toCommunityBook() }
        } catch (e: Exception) {
            return emptyList()
        }
    }
    
    override suspend fun rateTranslation(chapterKey: String, rating: Int): Result<Unit> {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()
        
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return Result.failure(Exception("Community Source not configured"))
        }
        
        return try {
            // Call RPC function to update rating
            val response = httpClient.post("$baseUrl/rest/v1/rpc/rate_chapter") {
                header("apikey", apiKey)
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody("""{"p_chapter_id": "$chapterKey", "p_rating": $rating}""")
            }
            
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to rate translation"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun fetchBooks(
        endpoint: String,
        orderBy: String,
        page: Int
    ): MangasPageInfo {
        val baseUrl = getBaseUrl()
        val apiKey = getApiKey()
        
        if (baseUrl.isBlank() || apiKey.isBlank()) {
            return MangasPageInfo.empty()
        }
        
        try {
            val offset = (page - 1) * PAGE_SIZE
            
            val response = httpClient.get("$baseUrl$endpoint") {
                header("apikey", apiKey)
                header("Authorization", "Bearer $apiKey")
                parameter("order", orderBy)
                parameter("offset", offset)
                parameter("limit", PAGE_SIZE)
                parameter("select", "id,title,author,description,cover,genres,status,available_languages,view_count,chapter_count")
            }
            
            if (!response.status.isSuccess()) {
                return MangasPageInfo.empty()
            }
            
            val books = json.decodeFromString<List<CommunityBookDto>>(response.bodyAsText())
            return MangasPageInfo(
                mangas = books.map { it.toMangaInfo() },
                hasNextPage = books.size >= PAGE_SIZE
            )
        } catch (e: Exception) {
            return MangasPageInfo.empty()
        }
    }
}

// DTOs for Supabase communication
@Serializable
private data class CommunityBookDto(
    val id: String? = null,
    val title: String,
    val author: String = "",
    val description: String = "",
    val cover: String = "",
    val genres: List<String> = emptyList(),
    val status: String = "Ongoing",
    val originalLanguage: String = "en",
    val availableLanguages: List<String> = emptyList(),
    val contributorId: String = "",
    val contributorName: String = "",
    val viewCount: Long = 0,
    val chapterCount: Int = 0,
    val lastUpdated: Long = 0,
    val createdAt: Long = 0
) {
    fun toMangaInfo(): MangaInfo {
        return MangaInfo(
            key = id ?: "",
            title = title,
            author = author,
            description = buildDescription(),
            cover = cover,
            genres = genres,
            status = when (status.lowercase()) {
                "ongoing" -> MangaInfo.ONGOING
                "completed" -> MangaInfo.COMPLETED
                "hiatus" -> MangaInfo.ON_HIATUS
                "dropped" -> MangaInfo.CANCELLED
                else -> MangaInfo.UNKNOWN
            }
        )
    }
    
    private fun buildDescription(): String {
        val parts = mutableListOf<String>()
        if (description.isNotBlank()) parts.add(description)
        if (availableLanguages.isNotEmpty()) {
            parts.add("\n\n📚 Available in: ${availableLanguages.joinToString(", ")}")
        }
        if (contributorName.isNotBlank()) {
            parts.add("👤 Contributed by: $contributorName")
        }
        if (chapterCount > 0) {
            parts.add("📖 Chapters: $chapterCount")
        }
        return parts.joinToString("\n")
    }
    
    fun toCommunityBook(): CommunityBook {
        return CommunityBook(
            id = id ?: "",
            title = title,
            author = author,
            description = description,
            cover = cover,
            genres = genres,
            status = status,
            originalLanguage = originalLanguage,
            availableLanguages = availableLanguages,
            contributorId = contributorId,
            contributorName = contributorName,
            viewCount = viewCount,
            chapterCount = chapterCount,
            lastUpdated = lastUpdated,
            createdAt = createdAt
        )
    }
}

@Serializable
private data class CommunityChapterDto(
    val id: String? = null,
    val bookId: String,
    val name: String,
    val number: Float = -1f,
    val content: String = "",
    val language: String,
    val translatorId: String = "",
    val translatorName: String = "",
    val originalChapterKey: String = "",
    val rating: Float = 0f,
    val ratingCount: Int = 0,
    val viewCount: Long = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    fun toChapterInfo(): ChapterInfo {
        val langSuffix = if (language.isNotBlank()) " [$language]" else ""
        val translatorSuffix = if (translatorName.isNotBlank()) " by $translatorName" else ""
        
        return ChapterInfo(
            key = id ?: "",
            name = "$name$langSuffix",
            number = number,
            dateUpload = createdAt,
            scanlator = "$translatorName$translatorSuffix"
        )
    }
}

@Serializable
private data class ChapterContentDto(
    val content: String
)

@Serializable
private data class LanguagesDto(
    val availableLanguages: List<String>
)
