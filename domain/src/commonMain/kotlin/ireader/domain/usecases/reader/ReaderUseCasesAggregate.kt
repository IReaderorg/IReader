package ireader.domain.usecases.reader

import ireader.domain.usecases.chapter.ReportBrokenChapterUseCase
import ireader.domain.usecases.glossary.DeleteGlossaryEntryUseCase
import ireader.domain.usecases.glossary.ExportGlossaryUseCase
import ireader.domain.usecases.glossary.GetGlossaryByBookIdUseCase
import ireader.domain.usecases.glossary.ImportGlossaryUseCase
import ireader.domain.usecases.glossary.SaveGlossaryEntryUseCase
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.local.book_usecases.BookMarkChapterUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.statistics.TrackReadingProgressUseCase
import ireader.domain.usecases.translate.TranslateChapterWithStorageUseCase
import ireader.domain.usecases.translate.TranslateParagraphUseCase
import ireader.domain.usecases.translation.GetTranslatedChapterUseCase

/**
 * Aggregate class for ReaderScreenViewModel use cases.
 * Groups related use cases to reduce constructor parameter count.
 * 
 * This aggregate reduces ReaderScreenViewModel constructor parameters from 40+ to ~15.
 * 
 * Requirements: 1.2, 1.4, 1.5
 * - 1.2: ReaderScreenViewModel accepts no more than 15 constructor parameters
 * - 1.4: Use case aggregate registered as factory in DI module
 * - 1.5: Maintains same functionality as individual use case injection
 */
data class ReaderUseCasesAggregate(
    /** Use cases for getting book data */
    val getBookUseCases: LocalGetBookUseCases,
    
    /** Use cases for getting chapter data */
    val getChapterUseCase: LocalGetChapterUseCase,
    
    /** Use cases for inserting/updating book and chapter data */
    val insertUseCases: LocalInsertUseCases,
    
    /** Use cases for remote operations (fetching from source) */
    val remoteUseCases: RemoteUseCases,
    
    /** Use case for history operations */
    val historyUseCase: HistoryUseCase,
    
    /** Use case for preloading chapters */
    val preloadChapter: PreloadChapterUseCase,
    
    /** Use case for bookmarking chapters */
    val bookmarkChapter: BookMarkChapterUseCase,
    
    /** Use case for reporting broken chapters */
    val reportBrokenChapter: ReportBrokenChapterUseCase,
    
    /** Use case for tracking reading progress/statistics */
    val trackReadingProgress: TrackReadingProgressUseCase,
    
    // Translation-related use cases
    /** Use case for translating chapters with storage */
    val translateChapterWithStorage: TranslateChapterWithStorageUseCase,
    
    /** Use case for translating paragraphs */
    val translateParagraph: TranslateParagraphUseCase,
    
    /** Use case for getting translated chapters */
    val getTranslatedChapter: GetTranslatedChapterUseCase,
    
    // Glossary-related use cases
    /** Use case for getting glossary entries by book ID */
    val getGlossaryByBookId: GetGlossaryByBookIdUseCase,
    
    /** Use case for saving glossary entries */
    val saveGlossaryEntry: SaveGlossaryEntryUseCase,
    
    /** Use case for deleting glossary entries */
    val deleteGlossaryEntry: DeleteGlossaryEntryUseCase,
    
    /** Use case for exporting glossary */
    val exportGlossary: ExportGlossaryUseCase,
    
    /** Use case for importing glossary */
    val importGlossary: ImportGlossaryUseCase,
    
    /** Use case for filtering content with regex patterns */
    val contentFilter: ContentFilterUseCase
)
