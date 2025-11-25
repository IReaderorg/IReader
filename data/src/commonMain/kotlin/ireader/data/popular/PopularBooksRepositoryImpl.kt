package ireader.data.popular

import io.github.jan.supabase.SupabaseClient
import ireader.data.backend.BackendService
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.PopularBooksRepository
import ireader.domain.models.remote.PopularBook
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class PopularBooksRepositoryImpl(
    private val supabaseClient: SupabaseClient,
    private val backendService: BackendService
) : PopularBooksRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    @Serializable
    private data class PopularBookDto(
        @SerialName("book_id") val bookId: String,
        @SerialName("title") val title: String,
        @SerialName("book_url") val bookUrl: String,
        @SerialName("source_id") val sourceId: Long,
        @SerialName("reader_count") val readerCount: Int,
        @SerialName("last_read") val lastRead: Long
    )
    
    override suspend fun getPopularBooks(limit: Int): Result<List<PopularBook>> =
        RemoteErrorMapper.withErrorMapping {
            // Try to use the SQL function first for better performance
            try {
                val rpcResult = backendService.rpc(
                    function = "get_popular_books",
                    parameters = mapOf("p_limit" to limit)
                ).getOrThrow()
                
                // RPC returns a JsonArray
                val results = if (rpcResult is kotlinx.serialization.json.JsonArray) {
                    rpcResult.map { json.decodeFromJsonElement(PopularBookDto.serializer(), it) }
                } else {
                    // Fallback: if it's a single object, wrap it
                    listOf(json.decodeFromJsonElement(PopularBookDto.serializer(), rpcResult))
                }
                
                results.map {
                    PopularBook(
                        bookId = it.bookId,
                        title = it.title,
                        bookUrl = it.bookUrl,
                        sourceId = it.sourceId,
                        readerCount = it.readerCount,
                        lastRead = it.lastRead
                    )
                }
            } catch (e: Exception) {
                // Fallback to client-side grouping if SQL function doesn't exist
                val queryResult = backendService.query(
                    table = "synced_books",
                    columns = "book_id, title, book_url, source_id, last_read",
                    orderBy = "last_read",
                    ascending = false,
                    limit = limit * 3  // Get more to account for grouping
                ).getOrThrow()
                
                // Group by book_id and count readers
                queryResult
                    .map { json.decodeFromJsonElement(PopularBookDto.serializer(), it) }
                    .groupBy { it.bookId }
                    .map { (bookId, books) ->
                        val first = books.first()
                        PopularBook(
                            bookId = bookId,
                            title = first.title,
                            bookUrl = first.bookUrl,
                            sourceId = first.sourceId,
                            readerCount = books.size,
                            lastRead = books.maxOf { it.lastRead }
                        )
                    }
                    .sortedByDescending { it.readerCount }
                    .take(limit)
            }
        }
}
