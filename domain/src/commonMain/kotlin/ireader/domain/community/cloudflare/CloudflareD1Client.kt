package ireader.domain.community.cloudflare

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Client for Cloudflare D1 database operations.
 * Handles translation metadata storage and queries.
 */
class CloudflareD1Client(
    private val httpClient: HttpClient,
    private val config: CloudflareConfig
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    /**
     * Initialize the D1 database schema.
     * Should be called once during setup.
     */
    suspend fun initializeSchema(): Result<Unit> {
        val sql = """
            CREATE TABLE IF NOT EXISTS translations (
                id TEXT PRIMARY KEY,
                content_hash TEXT NOT NULL,
                book_hash TEXT NOT NULL,
                book_title TEXT NOT NULL,
                book_author TEXT DEFAULT '',
                chapter_name TEXT NOT NULL,
                chapter_number REAL DEFAULT -1,
                source_language TEXT NOT NULL,
                target_language TEXT NOT NULL,
                engine_id TEXT NOT NULL,
                r2_object_key TEXT NOT NULL,
                original_size INTEGER NOT NULL,
                compressed_size INTEGER NOT NULL,
                compression_ratio REAL NOT NULL,
                contributor_id TEXT DEFAULT '',
                contributor_name TEXT DEFAULT '',
                rating REAL DEFAULT 0,
                rating_count INTEGER DEFAULT 0,
                download_count INTEGER DEFAULT 0,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            );
            
            CREATE INDEX IF NOT EXISTS idx_translations_content_hash ON translations(content_hash);
            CREATE INDEX IF NOT EXISTS idx_translations_book_hash ON translations(book_hash);
            CREATE INDEX IF NOT EXISTS idx_translations_languages ON translations(source_language, target_language);
            CREATE INDEX IF NOT EXISTS idx_translations_book_chapter ON translations(book_hash, chapter_number, target_language);
            CREATE INDEX IF NOT EXISTS idx_translations_rating ON translations(rating DESC);
            CREATE INDEX IF NOT EXISTS idx_translations_downloads ON translations(download_count DESC);
        """.trimIndent()
        
        return executeQuery(sql)
    }
    
    /**
     * Check if a translation already exists by content hash.
     */
    suspend fun findByContentHash(
        contentHash: String,
        targetLanguage: String,
        engineId: String
    ): TranslationMetadata? {
        val sql = """
            SELECT * FROM translations 
            WHERE content_hash = ? AND target_language = ? AND engine_id = ?
            ORDER BY rating DESC, download_count DESC
            LIMIT 1
        """.trimIndent()
        
        val result = executeQueryWithParams(sql, listOf(contentHash, targetLanguage, engineId))
        return result.getOrNull()?.firstOrNull()
    }
    
    /**
     * Find translations for a book chapter.
     */
    suspend fun findByBookChapter(
        bookHash: String,
        chapterNumber: Float,
        targetLanguage: String
    ): List<TranslationMetadata> {
        val sql = """
            SELECT * FROM translations 
            WHERE book_hash = ? AND chapter_number = ? AND target_language = ?
            ORDER BY rating DESC, download_count DESC
        """.trimIndent()
        
        return executeQueryWithParams(sql, listOf(bookHash, chapterNumber.toString(), targetLanguage))
            .getOrNull() ?: emptyList()
    }
    
    /**
     * Find all translations for a book.
     */
    suspend fun findByBook(
        bookHash: String,
        targetLanguage: String? = null
    ): List<TranslationMetadata> {
        val sql = if (targetLanguage != null) {
            "SELECT * FROM translations WHERE book_hash = ? AND target_language = ? ORDER BY chapter_number ASC"
        } else {
            "SELECT * FROM translations WHERE book_hash = ? ORDER BY chapter_number ASC"
        }
        
        val params = if (targetLanguage != null) listOf(bookHash, targetLanguage) else listOf(bookHash)
        return executeQueryWithParams(sql, params).getOrNull() ?: emptyList()
    }
    
    /**
     * Insert a new translation metadata record.
     */
    suspend fun insertTranslation(metadata: TranslationMetadata): Result<Unit> {
        val sql = """
            INSERT INTO translations (
                id, content_hash, book_hash, book_title, book_author,
                chapter_name, chapter_number, source_language, target_language,
                engine_id, r2_object_key, original_size, compressed_size,
                compression_ratio, contributor_id, contributor_name,
                rating, rating_count, download_count, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        
        val params = listOf(
            metadata.id,
            metadata.contentHash,
            metadata.bookHash,
            metadata.bookTitle,
            metadata.bookAuthor,
            metadata.chapterName,
            metadata.chapterNumber.toString(),
            metadata.sourceLanguage,
            metadata.targetLanguage,
            metadata.engineId,
            metadata.r2ObjectKey,
            metadata.originalSize.toString(),
            metadata.compressedSize.toString(),
            metadata.compressionRatio.toString(),
            metadata.contributorId,
            metadata.contributorName,
            metadata.rating.toString(),
            metadata.ratingCount.toString(),
            metadata.downloadCount.toString(),
            metadata.createdAt.toString(),
            metadata.updatedAt.toString()
        )
        
        return executeQueryWithParams(sql, params).map { }
    }
    
    /**
     * Increment download count for a translation.
     */
    suspend fun incrementDownloadCount(translationId: String): Result<Unit> {
        val sql = "UPDATE translations SET download_count = download_count + 1 WHERE id = ?"
        return executeQueryWithParams(sql, listOf(translationId)).map { }
    }
    
    /**
     * Update rating for a translation.
     */
    suspend fun updateRating(translationId: String, newRating: Float, newCount: Int): Result<Unit> {
        val sql = "UPDATE translations SET rating = ?, rating_count = ?, updated_at = ? WHERE id = ?"
        return executeQueryWithParams(sql, listOf(
            newRating.toString(),
            newCount.toString(),
            currentTimeToLong().toString(),
            translationId
        )).map { }
    }
    
    /**
     * Get popular translations (most downloaded).
     */
    suspend fun getPopularTranslations(
        targetLanguage: String,
        limit: Int = 50
    ): List<TranslationMetadata> {
        val sql = """
            SELECT * FROM translations 
            WHERE target_language = ?
            ORDER BY download_count DESC, rating DESC
            LIMIT ?
        """.trimIndent()
        
        return executeQueryWithParams(sql, listOf(targetLanguage, limit.toString()))
            .getOrNull() ?: emptyList()
    }
    
    /**
     * Search translations by book title.
     */
    suspend fun searchByTitle(
        query: String,
        targetLanguage: String? = null,
        limit: Int = 50
    ): List<TranslationMetadata> {
        val sql = if (targetLanguage != null) {
            """
                SELECT * FROM translations 
                WHERE book_title LIKE ? AND target_language = ?
                GROUP BY book_hash
                ORDER BY download_count DESC
                LIMIT ?
            """.trimIndent()
        } else {
            """
                SELECT * FROM translations 
                WHERE book_title LIKE ?
                GROUP BY book_hash
                ORDER BY download_count DESC
                LIMIT ?
            """.trimIndent()
        }
        
        val searchPattern = "%$query%"
        val params = if (targetLanguage != null) {
            listOf(searchPattern, targetLanguage, limit.toString())
        } else {
            listOf(searchPattern, limit.toString())
        }
        
        return executeQueryWithParams(sql, params).getOrNull() ?: emptyList()
    }
    
    // Execute raw SQL query
    private suspend fun executeQuery(sql: String): Result<Unit> {
        return try {
            val response = httpClient.post("${config.d1ApiUrl}/query") {
                header("Authorization", "Bearer ${config.apiToken}")
                contentType(ContentType.Application.Json)
                setBody(D1QueryRequest(sql = sql))
            }
            
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("D1 query failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Execute parameterized query
    private suspend fun executeQueryWithParams(
        sql: String,
        params: List<String>
    ): Result<List<TranslationMetadata>> {
        return try {
            val response = httpClient.post("${config.d1ApiUrl}/query") {
                header("Authorization", "Bearer ${config.apiToken}")
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(
                    D1QueryRequest.serializer(),
                    D1QueryRequest(sql = sql, params = params)
                ))
            }
            
            if (response.status.isSuccess()) {
                val responseBody = response.bodyAsText()
                val d1Response = json.decodeFromString<D1QueryResponse>(responseBody)
                
                val results = d1Response.result?.firstOrNull()?.results?.map { row ->
                    row.toTranslationMetadata()
                } ?: emptyList()
                
                Result.success(results)
            } else {
                Result.failure(Exception("D1 query failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
private data class D1QueryRequest(
    val sql: String,
    val params: List<String>? = null
)

@Serializable
private data class D1QueryResponse(
    val success: Boolean = false,
    val result: List<D1ResultSet>? = null,
    val errors: List<D1Error>? = null
)

@Serializable
private data class D1ResultSet(
    val results: List<kotlinx.serialization.json.JsonObject>? = null,
    val success: Boolean = false
)

@Serializable
private data class D1Error(
    val code: Int = 0,
    val message: String = ""
)

// Extension function to parse JsonObject to TranslationMetadata
private fun JsonObject.toTranslationMetadata(): TranslationMetadata {
    fun getString(key: String): String = (this[key] as? JsonPrimitive)?.contentOrNull ?: ""
    fun getFloat(key: String): Float = (this[key] as? JsonPrimitive)?.floatOrNull ?: 0f
    fun getInt(key: String): Int = (this[key] as? JsonPrimitive)?.intOrNull ?: 0
    fun getLong(key: String): Long = (this[key] as? JsonPrimitive)?.longOrNull ?: 0L
    
    return TranslationMetadata(
        id = getString("id"),
        contentHash = getString("content_hash"),
        bookHash = getString("book_hash"),
        bookTitle = getString("book_title"),
        bookAuthor = getString("book_author"),
        chapterName = getString("chapter_name"),
        chapterNumber = getFloat("chapter_number").let { if (it == 0f) -1f else it },
        sourceLanguage = getString("source_language"),
        targetLanguage = getString("target_language"),
        engineId = getString("engine_id"),
        r2ObjectKey = getString("r2_object_key"),
        originalSize = getInt("original_size"),
        compressedSize = getInt("compressed_size"),
        compressionRatio = getFloat("compression_ratio").let { if (it == 0f) 1f else it },
        contributorId = getString("contributor_id"),
        contributorName = getString("contributor_name"),
        rating = getFloat("rating"),
        ratingCount = getInt("rating_count"),
        downloadCount = getLong("download_count"),
        createdAt = getLong("created_at"),
        updatedAt = getLong("updated_at")
    )
}
