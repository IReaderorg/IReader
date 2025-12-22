package ireader.domain.usecases.library

import ireader.domain.usecases.category.CategoriesUseCases
import ireader.domain.usecases.epub.ImportEpub
import ireader.domain.usecases.pdf.ImportPdf
import ireader.domain.usecases.history.GetLastReadNovelUseCase
import ireader.domain.usecases.local.DeleteUseCase
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.local.book_usecases.ArchiveBookUseCase
import ireader.domain.usecases.local.book_usecases.DownloadUnreadChaptersUseCase
import ireader.domain.usecases.local.book_usecases.GetLibraryCategory
import ireader.domain.usecases.local.book_usecases.MarkBookAsReadOrNotUseCase

/**
 * Aggregate for all library-related use cases.
 * Reduces LibraryViewModel constructor from 26 params to ~8.
 * 
 * Usage:
 * ```kotlin
 * class LibraryViewModel(
 *     private val libraryUseCases: LibraryUseCases,
 *     // ... other deps
 * ) {
 *     fun markAsRead(bookIds: List<Long>) {
 *         libraryUseCases.markAsRead.markAsReadWithUndo(bookIds)
 *     }
 * }
 * ```
 * 
 * Requirements: 4.1 - LibraryViewModel accepts LibraryUseCases aggregate
 */
data class LibraryUseCases(
    val getBooks: LocalGetBookUseCases,
    val getChapters: LocalGetChapterUseCase,
    val insert: LocalInsertUseCases,
    val delete: DeleteUseCase,
    val categories: CategoriesUseCases,
    val markAsRead: MarkBookAsReadOrNotUseCase,
    val downloadUnread: DownloadUnreadChaptersUseCase,
    val archiveBook: ArchiveBookUseCase,
    val getLastRead: GetLastReadNovelUseCase,
    val getLibraryCategory: GetLibraryCategory,
    val importEpub: ImportEpub,
    val importPdf: ImportPdf
)
