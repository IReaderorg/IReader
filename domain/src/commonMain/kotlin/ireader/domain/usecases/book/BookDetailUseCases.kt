package ireader.domain.usecases.book

import ireader.domain.usecases.chapter.GetChaptersByBookIdUseCase
import ireader.domain.usecases.chapter.GetLastReadChapterUseCase
import ireader.domain.usecases.chapter.UpdateChapterReadStatusUseCase
import ireader.domain.usecases.download.DownloadChaptersUseCase
import ireader.domain.usecases.epub.ExportNovelAsEpubUseCase
import ireader.domain.usecases.epub.ExportBookAsEpubUseCase
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.local.DeleteUseCase
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.review.GetBookReviewsUseCase
import ireader.domain.usecases.translation.GetTranslatedChaptersByBookIdUseCase

/**
 * Aggregate class for BookDetailViewModel use cases.
 * Groups related use cases to reduce constructor parameter count.
 * 
 * This aggregate reduces BookDetailViewModel constructor parameters from 28+ to ~12.
 * 
 * Requirements: 1.1, 1.4, 1.5
 * - 1.1: BookDetailViewModel accepts no more than 12 constructor parameters
 * - 1.4: Use case aggregate registered as factory in DI module
 * - 1.5: Maintains same functionality as individual use case injection
 */
data class BookDetailUseCases(
    /** Use cases for getting book data */
    val getBookUseCases: LocalGetBookUseCases,
    
    /** Use cases for getting chapter data */
    val getChapterUseCase: LocalGetChapterUseCase,
    
    /** Use cases for inserting/updating book and chapter data */
    val insertUseCases: LocalInsertUseCases,
    
    /** Use cases for deleting book and chapter data */
    val deleteUseCase: DeleteUseCase,
    
    /** Use cases for remote operations (fetching from source) */
    val remoteUseCases: RemoteUseCases,
    
    /** Use case for history operations */
    val historyUseCase: HistoryUseCase,
    
    /** Use case for getting last read chapter */
    val getLastReadChapter: GetLastReadChapterUseCase,
    
    /** Use case for marking chapters as read */
    val markChapterAsRead: UpdateChapterReadStatusUseCase,
    
    /** Use case for downloading chapters */
    val downloadChapters: DownloadChaptersUseCase,
    
    /** Use case for exporting book as EPUB (legacy) */
    val exportNovelAsEpub: ExportNovelAsEpubUseCase,
    
    /** Use case for exporting book as EPUB (new) */
    val exportBookAsEpub: ExportBookAsEpubUseCase,
    
    /** Use case for getting book reviews */
    val getBookReviews: GetBookReviewsUseCase,
    
    /** Use case for getting translated chapters by book ID */
    val getTranslatedChaptersByBookId: GetTranslatedChaptersByBookIdUseCase
)
