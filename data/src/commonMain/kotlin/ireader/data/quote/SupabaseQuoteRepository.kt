package ireader.data.quote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import ireader.data.backend.BackendService
import ireader.domain.data.repository.QuoteRepository
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteStatus
import ireader.domain.models.quote.SubmitQuoteRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Supabase implementation of QuoteRepository
 */
class SupabaseQuoteRepository(
    private val supabaseClient: SupabaseClient,
    private val backendService: BackendService
) : QuoteRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    @Serializable
    private data class QuoteDto(
        @SerialName("id") val id: String,
        @SerialName("user_id") val userId: String,
        @SerialName("quote_text") val quoteText: String,
        @SerialName("book_title") val bookTitle: String,
        @SerialName("author") val author: String? = null,
        @SerialName("chapter_title") val chapterTitle: String? = null,
        @SerialName("status") val status: String = "PENDING",
        @SerialName("submitted_at") val submittedAt: String? = null,
        @SerialName("likes_count") val likesCount: Int = 0,
        @SerialName("featured") val featured: Boolean = false
    )
    
    @Serializable
    private data class DailyQuoteDto(
        @SerialName("quote_id") val quoteId: String,
        @SerialName("quote_text") val quoteText: String,
        @SerialName("book_title") val bookTitle: String,
        @SerialName("author") val author: String? = null,
        @SerialName("chapter_title") val chapterTitle: String? = null,
        @SerialName("likes_count") val likesCount: Int = 0,
        @SerialName("submitter_username") val submitterUsername: String? = null,
        @SerialName("submitter_id") val submitterId: String? = null
    )
    
    override suspend fun getDailyQuote(): Result<Quote?> = runCatching {
        val result = backendService.rpc(
            function = "get_daily_quote",
            parameters = emptyMap()
        ).getOrThrow()
        
        // RPC returns a JsonElement, parse it as array
        val jsonArray = result as? kotlinx.serialization.json.JsonArray ?: return@runCatching null
        if (jsonArray.isEmpty()) return@runCatching null
        
        val dto = json.decodeFromJsonElement(DailyQuoteDto.serializer(), jsonArray.first())
        Quote(
            id = dto.quoteId,
            text = dto.quoteText,
            bookTitle = dto.bookTitle,
            author = dto.author ?: "",
            chapterTitle = dto.chapterTitle ?: "",
            submitterId = dto.submitterId ?: "",
            submitterUsername = dto.submitterUsername ?: "Anonymous",
            likesCount = dto.likesCount,
            status = QuoteStatus.APPROVED,
            isFeatured = true
        )
    }
    
    override suspend fun getApprovedQuotes(limit: Int, offset: Int): Result<List<Quote>> = runCatching {
        val result = backendService.query(
            table = "community_quotes",
            filters = mapOf("status" to "APPROVED"),
            columns = "*",
            limit = limit
        ).getOrThrow()
        
        result.map { json.decodeFromJsonElement(QuoteDto.serializer(), it) }
            .map { it.toDomain() }
    }
    
    override suspend fun getQuotesByBook(bookTitle: String): Result<List<Quote>> = runCatching {
        val result = backendService.query(
            table = "community_quotes",
            filters = mapOf(
                "status" to "APPROVED",
                "book_title" to bookTitle
            )
        ).getOrThrow()
        
        result.map { json.decodeFromJsonElement(QuoteDto.serializer(), it) }
            .map { it.toDomain() }
    }
    
    override suspend fun submitQuote(request: SubmitQuoteRequest): Result<Quote> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw Exception("User not logged in")
        
        val quoteData = buildJsonObject {
            put("user_id", userId)
            put("quote_text", request.quoteText)
            put("book_title", request.bookTitle)
            put("author", request.author)
            put("chapter_title", request.chapterTitle)
            put("status", "PENDING")
        }
        
        val result = backendService.insert(
            table = "community_quotes",
            data = quoteData,
            returning = true
        ).getOrThrow()
        
        result?.let { json.decodeFromJsonElement(QuoteDto.serializer(), it) }?.toDomain()
            ?: throw Exception("Failed to submit quote")
    }
    
    override suspend fun getUserQuotes(userId: String): Result<List<Quote>> = runCatching {
        val result = backendService.query(
            table = "community_quotes",
            filters = mapOf("user_id" to userId)
        ).getOrThrow()
        
        result.map { json.decodeFromJsonElement(QuoteDto.serializer(), it) }
            .map { it.toDomain() }
    }
    
    override suspend fun toggleLike(quoteId: String): Result<Boolean> = runCatching {
        val result = backendService.rpc(
            function = "toggle_quote_like",
            parameters = mapOf("p_quote_id" to quoteId)
        ).getOrThrow()
        
        // Function returns boolean - true if liked, false if unliked
        when (result) {
            is kotlinx.serialization.json.JsonPrimitive -> result.content.toBoolean()
            else -> false
        }
    }
    
    override suspend fun isLikedByUser(quoteId: String): Result<Boolean> = runCatching {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@runCatching false
        
        val result = backendService.query(
            table = "quote_likes",
            filters = mapOf(
                "user_id" to userId,
                "quote_id" to quoteId
            ),
            limit = 1
        ).getOrThrow()
        
        result.isNotEmpty()
    }
    
    override suspend fun getPendingQuotes(): Result<List<Quote>> = runCatching {
        val result = backendService.query(
            table = "community_quotes",
            filters = mapOf("status" to "PENDING")
        ).getOrThrow()
        
        result.map { json.decodeFromJsonElement(QuoteDto.serializer(), it) }
            .map { it.toDomain() }
    }
    
    override suspend fun approveQuote(quoteId: String, featured: Boolean): Result<Unit> = runCatching {
        backendService.rpc(
            function = "approve_quote",
            parameters = mapOf(
                "p_quote_id" to quoteId,
                "p_featured" to featured
            )
        ).getOrThrow()
        Unit
    }
    
    override suspend fun rejectQuote(quoteId: String): Result<Unit> = runCatching {
        backendService.rpc(
            function = "reject_quote",
            parameters = mapOf("p_quote_id" to quoteId)
        ).getOrThrow()
        Unit
    }
    
    override fun observeApprovedQuotes(): Flow<List<Quote>> = flow {
        // Initial load
        val quotes = getApprovedQuotes().getOrElse { emptyList() }
        emit(quotes)
        
        // TODO: Add realtime subscription when needed
    }
    
    private fun QuoteDto.toDomain(): Quote = Quote(
        id = id,
        text = quoteText,
        bookTitle = bookTitle,
        author = author ?: "",
        chapterTitle = chapterTitle ?: "",
        submitterId = userId,
        likesCount = likesCount,
        status = when (status) {
            "APPROVED" -> QuoteStatus.APPROVED
            "REJECTED" -> QuoteStatus.REJECTED
            else -> QuoteStatus.PENDING
        },
        isFeatured = featured
    )
}
