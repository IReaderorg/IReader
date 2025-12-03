package ireader.domain.usecases.local

import ireader.core.source.LocalSource
import ireader.core.source.LocalCatalogSource
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Use case to refresh the local library by scanning the local folder
 * and syncing with the database
 */
class RefreshLocalLibrary(
    private val localSource: LocalCatalogSource,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository
) {
    
    suspend operator fun invoke(): Result {
        return try {
            val scannedNovels = localSource.scanLocalNovels()
            var addedBooks = 0
            var addedChapters = 0
            var updatedBooks = 0
            
            scannedNovels.forEach { novelInfo ->
                // Check if book already exists
                val existingBook = bookRepository.findBookByKey(novelInfo.key)
                
                val bookId = if (existingBook != null) {
                    // Update existing book
                    val updatedBook = existingBook.copy(
                        title = novelInfo.title,
                        author = novelInfo.author,
                        description = novelInfo.description,
                        cover = novelInfo.cover,
                        status = novelInfo.status,
                        lastUpdate = currentTimeToLong()
                    )
                    bookRepository.upsert(updatedBook)
                    updatedBooks++
                    existingBook.id
                } else {
                    // Insert new book
                    val newBook = Book(
                        title = novelInfo.title,
                        key = novelInfo.key,
                        sourceId = LocalSource.SOURCE_ID,
                        author = novelInfo.author,
                        description = novelInfo.description,
                        cover = novelInfo.cover,
                        status = novelInfo.status,
                        favorite = false,
                        lastUpdate = currentTimeToLong()
                    )
                    addedBooks++
                    bookRepository.upsert(newBook)
                }
                
                // Scan and sync chapters
                val scannedChapters = localSource.scanNovelChapters(novelInfo.key)
                val existingChapters = chapterRepository.findChaptersByBookId(bookId)
                val existingChapterKeys = existingChapters.map { it.key }.toSet()
                
                scannedChapters.forEach { chapterInfo ->
                    if (chapterInfo.key !in existingChapterKeys) {
                        val newChapter = Chapter(
                            bookId = bookId,
                            key = chapterInfo.key,
                            name = chapterInfo.name,
                            number = chapterInfo.number,
                            dateUpload = chapterInfo.dateUpload,
                            type = chapterInfo.type
                        )
                        chapterRepository.insertChapter(newChapter)
                        addedChapters++
                    }
                }
            }
            
            Result.Success(
                addedBooks = addedBooks,
                updatedBooks = updatedBooks,
                addedChapters = addedChapters
            )
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to refresh local library")
        }
    }
    
    sealed class Result {
        data class Success(
            val addedBooks: Int,
            val updatedBooks: Int,
            val addedChapters: Int
        ) : Result()
        
        data class Error(val message: String) : Result()
    }
}
