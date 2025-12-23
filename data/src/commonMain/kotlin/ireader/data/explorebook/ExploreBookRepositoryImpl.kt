package ireader.data.explorebook

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.ExploreBookRepository
import ireader.domain.models.entities.ExploreBook
import ireader.core.log.Log

/**
 * Implementation of ExploreBookRepository using SQLDelight.
 */
class ExploreBookRepositoryImpl(
    private val handler: DatabaseHandler
) : ExploreBookRepository {
    
    override suspend fun upsert(book: ExploreBook): Long {
        handler.await(inTransaction = true) {
            explore_bookQueries.upsertExploreBook(
                sourceId = book.sourceId,
                url = book.url,
                title = book.title,
                author = book.author,
                description = book.description,
                genre = book.genres,
                status = book.status,
                cover = book.cover,
                dateAdded = book.dateAdded
            )
        }
        // Get the ID of the inserted/updated row
        return handler.awaitOneOrNull {
            explore_bookQueries.findByUrlAndSource(book.url, book.sourceId, exploreBookMapper)
        }?.id ?: -1L
    }
    
    override suspend fun upsertAll(books: List<ExploreBook>) {
        if (books.isEmpty()) return
        
        handler.await(inTransaction = true) {
            books.forEach { book ->
                explore_bookQueries.upsertExploreBook(
                    sourceId = book.sourceId,
                    url = book.url,
                    title = book.title,
                    author = book.author,
                    description = book.description,
                    genre = book.genres,
                    status = book.status,
                    cover = book.cover,
                    dateAdded = book.dateAdded
                )
            }
        }
        
        // Cleanup after batch insert
        cleanup()
    }
    
    override suspend fun findByUrlAndSource(url: String, sourceId: Long): ExploreBook? {
        return handler.awaitOneOrNull {
            explore_bookQueries.findByUrlAndSource(url, sourceId, exploreBookMapper)
        }
    }
    
    override suspend fun findBySource(sourceId: Long): List<ExploreBook> {
        return handler.awaitList {
            explore_bookQueries.findBySource(sourceId, exploreBookMapper)
        }
    }
    
    override suspend fun countAll(): Long {
        return handler.awaitOne {
            explore_bookQueries.countAll()
        }
    }
    
    override suspend fun countBySource(sourceId: Long): Long {
        return handler.awaitOne {
            explore_bookQueries.countBySource(sourceId)
        }
    }
    
    override suspend fun deleteByUrlAndSource(url: String, sourceId: Long) {
        handler.await {
            explore_bookQueries.deleteByUrlAndSource(url, sourceId)
        }
    }
    
    override suspend fun deleteBySource(sourceId: Long) {
        handler.await {
            explore_bookQueries.deleteBySource(sourceId)
        }
    }
    
    override suspend fun deleteAll() {
        handler.await {
            explore_bookQueries.deleteAll()
        }
    }
    
    override suspend fun cleanup() {
        val count = countAll()
        if (count > ExploreBookRepository.MAX_EXPLORE_BOOKS) {
            val toDelete = count - ExploreBookRepository.MAX_EXPLORE_BOOKS
            Log.debug { "[ExploreBookRepository] Cleaning up $toDelete old explore books (total: $count, max: ${ExploreBookRepository.MAX_EXPLORE_BOOKS})" }
            handler.await {
                explore_bookQueries.deleteOldest(toDelete)
            }
        }
    }
    
    override suspend fun findById(id: Long): ExploreBook? {
        return handler.awaitOneOrNull {
            explore_bookQueries.findById(id, exploreBookMapper)
        }
    }
}

/**
 * Mapper for ExploreBook from database row.
 */
private val exploreBookMapper = { 
    _id: Long,
    source_id: Long,
    url: String,
    title: String,
    author: String?,
    description: String?,
    genre: List<String>?,
    status: Long,
    cover: String?,
    date_added: Long ->
    ExploreBook(
        id = _id,
        sourceId = source_id,
        url = url,
        title = title,
        author = author ?: "",
        description = description ?: "",
        genres = genre ?: emptyList(),
        status = status,
        cover = cover ?: "",
        dateAdded = date_added
    )
}
