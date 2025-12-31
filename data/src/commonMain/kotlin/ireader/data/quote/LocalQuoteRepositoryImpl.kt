package ireader.data.quote

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.LocalQuoteRepository
import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * SQLDelight implementation of LocalQuoteRepository
 */
class LocalQuoteRepositoryImpl(
    private val handler: DatabaseHandler
) : LocalQuoteRepository {

    override suspend fun insert(quote: LocalQuote): Long {
        return handler.await {
            localQuoteQueries.insert(
                text = quote.text,
                book_id = quote.bookId,
                book_title = quote.bookTitle,
                chapter_title = quote.chapterTitle,
                chapter_number = quote.chapterNumber?.toLong(),
                author = quote.author,
                created_at = quote.createdAt,
                has_context_backup = quote.hasContextBackup
            )
            localQuoteQueries.selectLastInsertedRowId().executeAsOne()
        }
    }

    override suspend fun update(quote: LocalQuote) {
        handler.await {
            localQuoteQueries.update(
                text = quote.text,
                book_title = quote.bookTitle,
                chapter_title = quote.chapterTitle,
                chapter_number = quote.chapterNumber?.toLong(),
                author = quote.author,
                has_context_backup = quote.hasContextBackup,
                id = quote.id
            )
        }
    }

    override suspend fun delete(id: Long) {
        handler.await {
            localQuoteQueries.deleteById(id)
        }
    }

    override suspend fun getById(id: Long): LocalQuote? {
        return handler.awaitOneOrNull {
            localQuoteQueries.getById(id, localQuoteMapper)
        }
    }

    override suspend fun getAll(): List<LocalQuote> {
        return handler.awaitList {
            localQuoteQueries.getAll(localQuoteMapper)
        }
    }

    override suspend fun getByBookId(bookId: Long): List<LocalQuote> {
        return handler.awaitList {
            localQuoteQueries.getByBookId(bookId, localQuoteMapper)
        }
    }

    override fun observeAll(): Flow<List<LocalQuote>> {
        return handler.subscribeToList {
            localQuoteQueries.observeAll(localQuoteMapper)
        }
    }

    override fun observeByBookId(bookId: Long): Flow<List<LocalQuote>> {
        return handler.subscribeToList {
            localQuoteQueries.observeByBookId(bookId, localQuoteMapper)
        }
    }

    override suspend fun search(query: String): List<LocalQuote> {
        return handler.awaitList {
            localQuoteQueries.search(query, query, query, localQuoteMapper)
        }
    }

    override suspend fun getCount(): Long {
        return handler.awaitOne {
            localQuoteQueries.getCount()
        }
    }

    // Context backup operations

    override suspend fun saveContext(quoteId: Long, contexts: List<QuoteContext>) {
        handler.await(inTransaction = true) {
            // Delete existing context first
            localQuoteQueries.deleteContextByQuoteId(quoteId)
            // Insert new contexts
            contexts.forEach { context ->
                localQuoteQueries.insertContext(
                    quote_id = quoteId,
                    chapter_id = context.chapterId,
                    chapter_title = context.chapterTitle,
                    content = context.content
                )
            }
        }
    }

    override suspend fun getContext(quoteId: Long): List<QuoteContext> {
        return handler.awaitList {
            localQuoteQueries.getContextByQuoteId(quoteId, quoteContextMapper)
        }
    }

    override suspend fun deleteContext(quoteId: Long) {
        handler.await {
            localQuoteQueries.deleteContextByQuoteId(quoteId)
        }
    }

    override suspend fun hasContext(quoteId: Long): Boolean {
        return handler.awaitOne {
            localQuoteQueries.hasContext(quoteId)
        }
    }

    companion object {
        private val localQuoteMapper: (
            id: Long,
            text: String,
            book_id: Long,
            book_title: String,
            chapter_title: String,
            chapter_number: Long?,
            author: String?,
            created_at: Long,
            has_context_backup: Boolean
        ) -> LocalQuote = { id, text, bookId, bookTitle, chapterTitle, chapterNumber, author, createdAt, hasContextBackup ->
            LocalQuote(
                id = id,
                text = text,
                bookId = bookId,
                bookTitle = bookTitle,
                chapterTitle = chapterTitle,
                chapterNumber = chapterNumber?.toInt(),
                author = author,
                createdAt = createdAt,
                hasContextBackup = hasContextBackup
            )
        }

        private val quoteContextMapper: (
            id: Long,
            quote_id: Long,
            chapter_id: Long,
            chapter_title: String,
            content: String
        ) -> QuoteContext = { id, quoteId, chapterId, chapterTitle, content ->
            QuoteContext(
                id = id,
                quoteId = quoteId,
                chapterId = chapterId,
                chapterTitle = chapterTitle,
                content = content
            )
        }
    }
}
