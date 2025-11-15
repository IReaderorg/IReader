package ireader.presentation.ui.book.viewmodel


import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.core.source.model.CommandList
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.epub.EpubCreator
import ireader.domain.usecases.history.HistoryUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.services.ServiceUseCases
import ireader.domain.utils.extensions.withIOContext
import ireader.domain.utils.extensions.withUIContext
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.book.helpers.PlatformHelper
import ireader.presentation.ui.home.explore.viewmodel.BooksState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import kotlin.time.ExperimentalTime


class BookDetailViewModel(
    private val localInsertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    private val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
    private val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    val state: DetailStateImpl,
    val chapterState: ChapterStateImpl,
    private val serviceUseCases: ServiceUseCases,
    val deleteUseCase: ireader.domain.usecases.local.DeleteUseCase,
    private val applicationScope: CoroutineScope,
    val createEpub: EpubCreator,
    val exportNovelAsEpub: ireader.domain.usecases.epub.ExportNovelAsEpubUseCase,
    val historyUseCase: HistoryUseCase,
    val readerPreferences: ReaderPreferences,
    val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    private val param: Param,
    val booksState: BooksState,
    val platformHelper: PlatformHelper,
    private val archiveBookUseCase: ireader.domain.usecases.local.book_usecases.ArchiveBookUseCase,
    private val checkSourceAvailabilityUseCase: ireader.domain.usecases.source.CheckSourceAvailabilityUseCase,
    private val migrateToSourceUseCase: ireader.domain.usecases.source.MigrateToSourceUseCase,
    private val catalogStore: ireader.domain.catalogs.CatalogStore,
    private val syncUseCases: ireader.domain.usecases.sync.SyncUseCases? = null
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), DetailState by state, ChapterState by chapterState {
    data class Param(val bookId: Long?)


    var getBookDetailJob: Job? = null
    var getChapterDetailJob: Job? = null
    var initBooks = false
    var filters = mutableStateOf<List<ChaptersFilters>>(ChaptersFilters.getDefault(true))
    var sorting = mutableStateOf<ChapterSort>(loadSortingPreference())
    var layout by readerPreferences.showChapterNumberPreferences().asState()

    private fun loadSortingPreference(): ChapterSort {
        val sortTypeName = readerPreferences.chapterSortType().get()
        val isAscending = readerPreferences.chapterSortAscending().get()
        return try {
            val type = ChapterSort.Type.valueOf(sortTypeName)
            ChapterSort(type, isAscending)
        } catch (e: Exception) {
            ChapterSort.default
        }
    }

    private fun saveSortingPreference(sort: ChapterSort) {
        readerPreferences.chapterSortType().set(sort.type.name)
        readerPreferences.chapterSortAscending().set(sort.isAscending)
    }

    var launcher : () -> Any = {}
    
    // Source switching state
    val sourceSwitchingState = SourceSwitchingState()
    
    init {
        booksState.book = null
        val bookId = param.bookId
        val sourceId = runBlocking {
            bookId?.let { getBookUseCases.findBookById(it)?.sourceId }
        }

        if (bookId != null && sourceId != null) {
            val catalogSource = getLocalCatalog.get(sourceId)
            this.catalogSource = catalogSource

            val source = catalogSource?.source
            if (source is ireader.core.source.CatalogSource) {
                this.modifiedCommands = source.getCommands()
            }
            toggleBookLoading(true)
            chapterIsLoading = true
            scope.launch {
                subscribeBook(bookId = bookId)
                subscribeChapters(bookId = bookId)
                getLastReadChapter(bookId)
                initBook(bookId)
                checkSourceAvailability(bookId)
            }

        } else {
            scope.launch {
                showSnackBar(UiText.MStringResource(Res.string.something_is_wrong_with_this_book))
            }
        }
    }


    @OptIn(ExperimentalTime::class)
    private suspend fun initBook(bookId: Long) {
        var book = getBookUseCases.findBookById(bookId)

        if (book != null && book.lastUpdate < 1L && source != null) {
            book = book?.copy(
                id = bookId,
                initialized = true,
                lastUpdate = kotlin.time.Clock.System.now().toEpochMilliseconds(),

                )
            booksState.replaceBook(book)
            localInsertUseCases.updateBook.update(
                book
            )
            getRemoteBookDetail(book, catalogSource)
            getRemoteChapterDetail(book, catalogSource)
        } else {
            toggleBookLoading(false)
            chapterIsLoading = false
        }

    }


    private fun subscribeBook(bookId: Long) {
        getBookUseCases.subscribeBookById(bookId).onEach { snapshot ->
            booksState.replaceBook(snapshot)
        }.launchIn(scope)
    }

    private fun subscribeChapters(bookId: Long) {
        getChapterUseCase.subscribeChaptersByBookId(bookId).onEach { snapshot ->
            chapters = snapshot
        }.launchIn(scope)
    }


    @Composable
    fun getChapters(bookId: Long?): State<List<Chapter>> {
        if (bookId == null) return remember {
            mutableStateOf(emptyList())
        }
        val scope = rememberCoroutineScope()
        val unfiltered = remember(bookId, sorting.value, filters.value) {
            getChapterUseCase.subscribeChaptersByBookId(
                bookId = bookId,
                sort = sorting.value.parameter,
            ).shareIn(scope, SharingStarted.WhileSubscribed(1000), 1)
        }

        return remember(chapterState.query, bookId, sorting.value, filters.value) {
            val query = chapterState.query
            if (query.isNullOrBlank()) {
                unfiltered
            } else {
                unfiltered.map { chapters ->
                    chapters.filter { it.name.contains(query, true) }
                }
            }.map { it.filteredWith(filters.value) }.onEach {
                chapterState.chapters = it
            }
        }.collectAsState(emptyList())
    }

    private fun List<Chapter>.filteredWith(filters: List<ChaptersFilters>): List<Chapter> {
        if (filters.isEmpty()) return this
        val validFilters =
            filters.filter { it.value == ChaptersFilters.Value.Included || it.value == ChaptersFilters.Value.Excluded }
        var filteredList = this
        for (filter in validFilters) {
            val filterFn: (Chapter) -> Boolean = when (filter.type) {
                ChaptersFilters.Type.Unread -> {
                    {
                        !it.read
                    }
                }
                ChaptersFilters.Type.Bookmarked -> {
                    { book -> book.bookmark }
                }
                ChaptersFilters.Type.Downloaded -> {
                    {
                        it.content.joinToString("").isNotBlank()
                    }
                }
                ChaptersFilters.Type.Read -> {
                    {
                        it.read
                    }
                }
                ChaptersFilters.Type.Duplicate -> {
                    // Detect duplicates by comparing chapter names and numbers
                    { chapter ->
                        val isDuplicate = this.any { other ->
                            other.id != chapter.id && (
                                // Same name
                                other.name.trim().equals(chapter.name.trim(), ignoreCase = true) ||
                                // Same chapter number (if recognized)
                                (chapter.isRecognizedNumber && other.isRecognizedNumber && 
                                 kotlin.math.abs(other.number - chapter.number) < 0.01f)
                            )
                        }
                        isDuplicate
                    }
                }
            }
            filteredList = when (filter.value) {
                ChaptersFilters.Value.Included -> filteredList.filter(filterFn)
                ChaptersFilters.Value.Excluded -> filteredList.filterNot(filterFn)
                ChaptersFilters.Value.Missing -> filteredList
            }
        }

        return filteredList
    }

    fun getLastReadChapter(bookId: Long) {
        historyUseCase.subscribeHistoryByBookId(bookId).onEach {
            lastRead = it?.chapterId
        }.launchIn(scope)
    }

    suspend fun getRemoteBookDetail(book: Book?, source: CatalogLocal?) {
        if (book == null) return
        toggleBookLoading(true)
        getBookDetailJob?.cancel()
        getBookDetailJob = scope.launch {
            remoteUseCases.getBookDetail(
                book = book,
                catalog = source,
                onError = { message ->
                    withUIContext {
                        toggleBookLoading(false)
                        showSnackBar(message)
                        if (message != null) {
                            Log.error { message.toString() }
                        }
                    }
                },
                onSuccess = { resultBook ->
                    withUIContext {
                        toggleBookLoading(false)
                    }
                    localInsertUseCases.updateBook.update(resultBook)
                }

            )
        }
    }

    fun getLastChapterIndex(): Int {
        return when (val index = chapters.reversed().indexOfFirst { it.id == lastRead }) {
            -1 -> {
                throw Exception("chapter not found")
            }
            else -> {
                index
            }
        }
    }

    suspend fun getRemoteChapterDetail(
        book: Book?,
        source: CatalogLocal?,
        commands: CommandList = emptyList()
    ) {
        if (book == null) return
        chapterIsLoading = true
        getChapterDetailJob?.cancel()
        getChapterDetailJob = scope.launch {
            remoteUseCases.getRemoteChapters(
                book = book,
                catalog = source,
                onError = { message ->
                    Log.error { message.toString() }
                    showSnackBar(message)
                    withUIContext {
                        chapterIsLoading = false
                    }
                },
                onSuccess = { result ->
                    localInsertUseCases.insertChapters(result)
                    withUIContext {
                        chapterIsLoading = false
                    }
                },
                commands = commands,
                oldChapters = chapterState.chapters
            )
        }
    }

    fun toggleInLibrary(book: Book) {
        this.inLibraryLoading = true
        applicationScope.launch {
            withIOContext {
                // Use the clean architecture use case
                syncUseCases?.toggleBookInLibrary?.invoke(book)
                    ?: run {
                        // Fallback if sync is not available
                        if (!book.favorite) {
                            localInsertUseCases.updateBook.update(
                                book.copy(
                                    favorite = true,
                                    dateAdded = Calendar.getInstance().timeInMillis,
                                )
                            )
                        } else {
                            deleteUseCase.unFavoriteBook(listOf(book.id))
                        }
                    }
            }
            this@BookDetailViewModel.inLibraryLoading = false
        }
    }

    var showEndOfLifeDialog by mutableStateOf(false)
        private set

    fun showEndOfLifeOptionsDialog() {
        showEndOfLifeDialog = true
    }

    fun hideEndOfLifeOptionsDialog() {
        showEndOfLifeDialog = false
    }
    
    // Migration dialog state
    var showMigrationDialog by mutableStateOf(false)
    var availableMigrationSources by mutableStateOf<List<ireader.domain.models.entities.CatalogLocal>>(emptyList())
    
    // EPUB export dialog state
    var showEpubExportDialog by mutableStateOf(false)
    
    /**
     * Load available sources for migration (excluding current source)
     * TODO: Implement when CatalogStore API is stable
     */
    fun loadMigrationSources() {
        scope.launch {
            try {
                // TODO: Fix catalogStore.catalogs() call
                // val currentSourceId = booksState.book?.sourceId
                // val allSources = catalogStore.catalogs()
                // availableMigrationSources = allSources.filter { it.sourceId != currentSourceId }
                availableMigrationSources = emptyList()
                showMigrationDialog = true
                showSnackBar(ireader.i18n.UiText.DynamicString("Migration feature coming soon"))
            } catch (e: Exception) {
                Log.error("Error loading migration sources", e)
                showSnackBar(ireader.i18n.UiText.DynamicString("Failed to load sources: ${e.message}"))
            }
        }
    }
    
    /**
     * Start migration to a new source
     * TODO: Implement full migration functionality
     */
    fun startMigration(targetSourceId: Long) {
        val book = booksState.book ?: return
        
        sourceSwitchingState.showMigrationDialog = true
        
        scope.launch {
            try {
                // TODO: Use the migration use case when it's properly implemented
                // For now, just show a placeholder message
                delay(1000)
                sourceSwitchingState.showMigrationDialog = false
                showSnackBar(ireader.i18n.UiText.DynamicString("Migration feature coming soon"))
                
                /* Commented out until migrateToSourceUseCase is properly implemented
                migrateToSourceUseCase(book.id, targetSourceId).collect { progress ->
                    sourceSwitchingState.migrationProgress = progress
                    
                    if (progress.isComplete) {
                        if (progress.error == null) {
                            delay(1000)
                            sourceSwitchingState.showMigrationDialog = false
                            sourceSwitchingState.reset()
                            
                            // Refresh book data
                            initBook(book.id)
                            
                            val targetSource = catalogStore.get(targetSourceId)
                            showSnackBar(ireader.i18n.UiText.DynamicString("Successfully migrated to ${targetSource?.name}"))
                        } else {
                            delay(2000)
                            sourceSwitchingState.showMigrationDialog = false
                            showSnackBar(ireader.i18n.UiText.DynamicString("Migration failed: ${progress.error}"))
                        }
                    }
                }
                */
            } catch (e: Exception) {
                Log.error("Migration error", e)
                sourceSwitchingState.showMigrationDialog = false
                showSnackBar(ireader.i18n.UiText.DynamicString("Migration failed: ${e.message}"))
            }
        }
    }
    
    /**
     * Share book information
     */
    fun shareBook() {
        val book = booksState.book ?: return
        
        scope.launch {
            try {
                val shareText = buildString {
                    append(book.title)
                    if (book.author.isNotBlank()) {
                        append(" by ${book.author}")
                    }
                    append("\n\n")
                    if (book.description.isNotBlank()) {
                        append(book.description)
                        append("\n\n")
                    }
                    append("Read on iReader")
                }
                
                // Use platform helper to share
                platformHelper.shareText(shareText)
            } catch (e: Exception) {
                Log.error("Error sharing book", e)
                showSnackBar(ireader.i18n.UiText.DynamicString("Failed to share: ${e.message}"))
            }
        }
    }
    
    /**
     * Export book as EPUB with custom options
     * TODO: Implement full EPUB export functionality
     * See spec: .kiro/specs/epub-export-feature/ (to be created)
     */
    fun exportAsEpub(options: ireader.presentation.ui.book.components.ExportOptions) {
        val book = booksState.book ?: return
        
        scope.launch {
            try {
                // TODO: Implement EPUB export
                // Steps needed:
                // 1. Fetch all selected chapters content
                // 2. Create EPUB structure (META-INF, OEBPS, mimetype)
                // 3. Generate content.opf with metadata
                // 4. Generate toc.ncx for navigation
                // 5. Create XHTML files for each chapter
                // 6. Apply formatting options
                // 7. Package as ZIP with .epub extension
                // 8. Save to user-selected location
                
                showSnackBar(
                    ireader.i18n.UiText.DynamicString(
                        "EPUB export feature is coming soon. " +
                        "Selected ${options.selectedChapters.size} chapters from '${book.title}'"
                    )
                )
                
                Log.info { "EPUB export requested for book: ${book.title}, chapters: ${options.selectedChapters.size}" }
            } catch (e: Exception) {
                Log.error("Error in EPUB export stub", e)
                showSnackBar(ireader.i18n.UiText.DynamicString("EPUB export not yet available"))
            }
        }
    }

    fun archiveBook(book: Book) {
        applicationScope.launch {
            try {
                archiveBookUseCase.toggleArchive(book.id, true).onSuccess {
                    withUIContext {
                        showSnackBar(UiText.DynamicString("${book.title} has been archived"))
                    }
                }.onFailure { error ->
                    withUIContext {
                        showSnackBar(UiText.ExceptionString(error))
                    }
                }
            } catch (e: Exception) {
                withUIContext {
                    showSnackBar(UiText.ExceptionString(e))
                }
            }
        }
    }

    fun unarchiveBook(book: Book) {
        applicationScope.launch {
            try {
                archiveBookUseCase.toggleArchive(book.id, false).onSuccess {
                    withUIContext {
                        showSnackBar(UiText.DynamicString("${book.title} has been unarchived"))
                    }
                }.onFailure { error ->
                    withUIContext {
                        showSnackBar(UiText.ExceptionString(error))
                    }
                }
            } catch (e: Exception) {
                withUIContext {
                    showSnackBar(UiText.ExceptionString(e))
                }
            }
        }
    }

    /**
     * Exports a novel as an ePub file with progress reporting.
     * This is a convenience method that wraps the ExportNovelAsEpubUseCase.
     */
    fun exportNovelAsEpub(book: Book, uri: ireader.domain.models.common.Uri) {
        applicationScope.launch {
            try {
                exportNovelAsEpub(book, uri) { progress ->
                    showSnackBar(UiText.DynamicString(progress))
                }
                showSnackBar(UiText.MStringResource(Res.string.success))
            } catch (e: Exception) {
                showSnackBar(UiText.ExceptionString(e))
            }
        }
    }


    fun deleteChapters(chapters: List<Chapter>) {
        scope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChapters(chapters)
        }
    }

    fun downloadChapters() {
        booksState.book?.let { book ->
            serviceUseCases.startDownloadServicesUseCase.start(chapterIds = this.selection.toLongArray())
        }
    }

    fun startDownloadService(book: Book) {
        serviceUseCases.startDownloadServicesUseCase.start(bookIds = longArrayOf(book.id))
    }
    
    fun downloadUnreadChapters() {
        val unreadChapterIds = chapters.filter { !it.read }.map { it.id }.toLongArray()
        if (unreadChapterIds.isNotEmpty()) {
            serviceUseCases.startDownloadServicesUseCase.start(chapterIds = unreadChapterIds)
            scope.launch {
                showSnackBar(ireader.i18n.UiText.DynamicString("Downloading ${unreadChapterIds.size} unread chapters"))
            }
        } else {
            scope.launch {
                showSnackBar(ireader.i18n.UiText.DynamicString("No unread chapters to download"))
            }
        }
    }
    
    fun downloadUndownloadedChapters() {
        val undownloadedChapterIds = chapters.filter { it.content.joinToString("").isBlank() }.map { it.id }.toLongArray()
        if (undownloadedChapterIds.isNotEmpty()) {
            serviceUseCases.startDownloadServicesUseCase.start(chapterIds = undownloadedChapterIds)
            scope.launch {
                showSnackBar(ireader.i18n.UiText.DynamicString("Downloading ${undownloadedChapterIds.size} chapters"))
            }
        } else {
            scope.launch {
                showSnackBar(ireader.i18n.UiText.DynamicString("All chapters are already downloaded"))
            }
        }
    }

    fun toggleFilter(type: ChaptersFilters.Type) {
        val newFilters = filters.value
            .map { filterState ->
                if (type == filterState.type) {
                    ChaptersFilters(
                        type,
                        when (filterState.value) {
                            ChaptersFilters.Value.Included -> ChaptersFilters.Value.Excluded
                            ChaptersFilters.Value.Excluded -> ChaptersFilters.Value.Missing
                            ChaptersFilters.Value.Missing -> ChaptersFilters.Value.Included
                        }
                    )
                } else {
                    filterState
                }
            }
        this.filters.value = newFilters
    }

    private fun toggleBookLoading(isLoading: Boolean) {
        this.detailIsLoading = isLoading
    }

    fun toggleSort(type: ChapterSort.Type) {
        val currentSort = sorting
        val newSort = if (type == currentSort.value.type) {
            currentSort.value.copy(isAscending = !currentSort.value.isAscending)
        } else {
            currentSort.value.copy(type = type)
        }
        sorting.value = newSort
        saveSortingPreference(newSort)
    }
    // Source Switching Methods

    /**
     * Checks if better sources are available for the current book
     */
    fun checkSourceAvailability(bookId: Long) {
        scope.launch {
            try {
                val result = checkSourceAvailabilityUseCase(bookId)
                result.onSuccess { comparison ->
                    if (comparison != null && comparison.betterSourceId != null) {
                        sourceSwitchingState.sourceComparison = comparison
                        sourceSwitchingState.betterSourceName = catalogStore.get(comparison.betterSourceId)?.name
                        sourceSwitchingState.showBanner = true
                    }
                }
            } catch (e: Exception) {
                Log.error { "Error checking source availability" }
            }
        }
    }

    /**
     * Initiates migration to a better source
     */
    fun migrateToSource() {
        val comparison = sourceSwitchingState.sourceComparison ?: return
        val betterSourceId = comparison.betterSourceId ?: return

        sourceSwitchingState.showBanner = false
        sourceSwitchingState.showMigrationDialog = true

        scope.launch {
            migrateToSourceUseCase(comparison.bookId, betterSourceId).collect { progress ->
                sourceSwitchingState.migrationProgress = progress

                if (progress.isComplete) {
                    if (progress.error == null) {
                        // Migration successful - refresh the book and chapters
                        delay(1000) // Brief delay to show completion
                        sourceSwitchingState.showMigrationDialog = false
                        sourceSwitchingState.reset()

                        // Refresh the book data
                        initBook(comparison.bookId)

                        showSnackBar(UiText.DynamicString("Successfully migrated to ${sourceSwitchingState.betterSourceName}"))
                    } else {
                        // Migration failed
                        delay(2000) // Show error for a bit longer
                        sourceSwitchingState.showMigrationDialog = false
                        showSnackBar(UiText.DynamicString("Migration failed: ${progress.error}"))
                    }
                }
            }
        }
    }

    /**
     * Dismisses the source switching banner for 7 days
     */
    fun dismissSourceSwitchingBanner() {
        val comparison = sourceSwitchingState.sourceComparison ?: return

        scope.launch {
            try {
                val dismissedUntil = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L) // 7 days
                comparison.copy(dismissedUntil = dismissedUntil).let {
                    // Update the dismissal time in the repository
                    withIOContext {
                        insertUseCases.updateBook.update(
                            getBookUseCases.findBookById(comparison.bookId) ?: return@withIOContext
                        )
                    }
                }
                sourceSwitchingState.showBanner = false
            } catch (e: Exception) {
                Log.error { "Error dismissing source switching banner" }
            }
        }
    }
}

