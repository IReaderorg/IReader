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
import ireader.domain.models.entities.isObsolete
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
    private val exportBookAsEpubUseCase: ireader.domain.usecases.epub.ExportBookAsEpubUseCase,
    val historyUseCase: HistoryUseCase,
    val readerPreferences: ReaderPreferences,
    val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    private val param: Param,
    val booksState: BooksState,
    private val archiveBookUseCase: ireader.domain.usecases.local.book_usecases.ArchiveBookUseCase,
    private val checkSourceAvailabilityUseCase: ireader.domain.usecases.source.CheckSourceAvailabilityUseCase,
    private val migrateToSourceUseCase: ireader.domain.usecases.source.MigrateToSourceUseCase,
    private val catalogStore: ireader.domain.catalogs.CatalogStore,
    private val syncUseCases: ireader.domain.usecases.sync.SyncUseCases? = null,
    private val downloadService: ireader.domain.services.common.DownloadService,
    // NEW: Clean architecture use cases
    private val bookUseCases: ireader.domain.usecases.book.BookUseCases,
    private val chapterUseCases: ireader.domain.usecases.chapter.ChapterUseCases,
    // Platform services - Clean architecture
    private val clipboardService: ireader.domain.services.platform.ClipboardService,
    private val shareService: ireader.domain.services.platform.ShareService,
    private val platformHelper: PlatformHelper,
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
    
    // Scroll state persistence
    var savedScrollIndex by mutableStateOf(0)
        private set
    var savedScrollOffset by mutableStateOf(0)
        private set
    
    fun saveScrollPosition(index: Int, offset: Int) {
        savedScrollIndex = index
        savedScrollOffset = offset
    }
    
    fun resetScrollPosition() {
        savedScrollIndex = 0
        savedScrollOffset = 0
    }
    
    init {
        booksState.book = null
        val bookId = param.bookId

        if (bookId != null) {
            toggleBookLoading(true)
            chapterIsLoading = true
            scope.launch {
                val sourceId = getBookUseCases.findBookById(bookId)?.sourceId
                
                if (sourceId != null) {
                    val catalogSource = getLocalCatalog.get(sourceId)
                    this@BookDetailViewModel.catalogSource = catalogSource

                    val source = catalogSource?.source
                    if (source is ireader.core.source.CatalogSource) {
                        this@BookDetailViewModel.modifiedCommands = source.getCommands()
                    }
                }
                
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
     */
    fun loadMigrationSources() {
        scope.launch {
            try {
                val currentSourceId = booksState.book?.sourceId
                val allSources = catalogStore.catalogs
                
                Log.info { "Loading migration sources. Current source: $currentSourceId" }
                Log.info { "Total catalogs available: ${allSources.size}" }
                
                // Filter out current source and ensure source is not null
                availableMigrationSources = allSources.filter { catalog ->
                    val isNotCurrentSource = catalog.sourceId != currentSourceId
                    val hasSource = catalog.source != null
                    val isEnabled = when (catalog) {
                        is ireader.domain.models.entities.CatalogInstalled -> !catalog.isObsolete
                        else -> true
                    }
                    
                    Log.debug { "Catalog: ${catalog.name} (${catalog.sourceId}) - NotCurrent: $isNotCurrentSource, HasSource: $hasSource, Enabled: $isEnabled" }
                    
                    isNotCurrentSource && hasSource && isEnabled
                }
                
                Log.info { "Available migration sources: ${availableMigrationSources.size}" }
                availableMigrationSources.forEach { 
                    Log.info { "  - ${it.name} (${it.sourceId})" }
                }
                
                showMigrationDialog = true
                
                if (availableMigrationSources.isEmpty()) {
                    Log.warn { "No alternative sources available for migration" }
                    showSnackBar(ireader.i18n.UiText.DynamicString("No alternative sources available (Total: ${allSources.size}, Current: $currentSourceId)"))
                }
            } catch (e: Exception) {
                Log.error("Error loading migration sources", e)
                showSnackBar(ireader.i18n.UiText.DynamicString("Failed to load sources: ${e.message ?: "Unknown error"}"))
            }
        }
    }
    
    /**
     * Start migration to a new source
     */
    fun startMigration(targetSourceId: Long) {
        val book = booksState.book ?: return
        
        showMigrationDialog = false
        sourceSwitchingState.showMigrationDialog = true
        
        scope.launch {
            try {
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
                            showSnackBar(ireader.i18n.UiText.DynamicString("Successfully migrated to ${targetSource?.name ?: "new source"}"))
                        } else {
                            delay(2000)
                            sourceSwitchingState.showMigrationDialog = false
                            showSnackBar(ireader.i18n.UiText.DynamicString("Migration failed: ${progress.error ?: "Unknown error"}"))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.error("Migration error", e)
                sourceSwitchingState.showMigrationDialog = false
                showSnackBar(ireader.i18n.UiText.DynamicString("Migration failed: ${e.message ?: "Unknown error"}"))
            }
        }
    }
    
    /**
     * Share book information using platform service
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
                
                // Use new platform service with clean error handling
                when (val result = shareService.shareText(shareText, book.title)) {
                    is ireader.domain.services.common.ServiceResult.Success -> {
                        // Success - no need to show message
                    }
                    is ireader.domain.services.common.ServiceResult.Error -> {
                        withUIContext {
                            showSnackBar(ireader.i18n.UiText.DynamicString("Failed to share: ${result.message}"))
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.error("Error sharing book", e)
                withUIContext {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Failed to share: ${e.message}"))
                }
            }
        }
    }
    
    /**
     * Copy book title to clipboard using platform service
     */
    fun copyBookTitle(title: String) {
        scope.launch {
            when (val result = clipboardService.copyText(title, "Book Title")) {
                is ireader.domain.services.common.ServiceResult.Success -> {
                    withUIContext {
                        showSnackBar(ireader.i18n.UiText.MStringResource(ireader.i18n.resources.Res.string.title_copied_to_clipboard))
                    }
                }
                is ireader.domain.services.common.ServiceResult.Error -> {
                    withUIContext {
                        showSnackBar(ireader.i18n.UiText.DynamicString("Failed to copy: ${result.message ?: "Unknown error"}"))
                    }
                }
                else -> {}
            }
        }
    }
    
    /**
     * Copy text to clipboard using platform service
     */
    fun copyToClipboard(label: String, text: String) {
        scope.launch {
            when (val result = clipboardService.copyText(text, label)) {
                is ireader.domain.services.common.ServiceResult.Success -> {
                    withUIContext {
                        showSnackBar(ireader.i18n.UiText.MStringResource(ireader.i18n.resources.Res.string.copied_to_clipboard))
                    }
                }
                is ireader.domain.services.common.ServiceResult.Error -> {
                    withUIContext {
                        showSnackBar(ireader.i18n.UiText.DynamicString("Failed to copy: ${result.message ?: "Unknown error"}"))
                    }
                }
                else -> {}
            }
        }
    }
    
    /**
     * Export book as EPUB with custom options
     * Implements full EPUB 3.0 export functionality with customizable formatting
     */
    fun exportAsEpub(options: ireader.presentation.ui.book.components.ExportOptions) {
        val book = booksState.book ?: return
        
        scope.launch {
            try {
                showSnackBar(UiText.MStringResource(Res.string.preparing_epub_export))
                
                Log.info { "Starting EPUB export for book: ${book.title}" }
                
                // Get output URI from platform helper with error handling
                val outputUri = try {
                    platformHelper.createEpubExportUri(book.title, book.author)
                } catch (e: Exception) {
                    Log.error("Failed to create export URI", e)
                    withUIContext {
                        showSnackBar(UiText.DynamicString("Failed to select save location: ${e.message ?: "Unknown error"}"))
                    }
                    return@launch
                }
                
                if (outputUri == null) {
                    Log.info { "EPUB export cancelled by user" }
                    showSnackBar(UiText.MStringResource(Res.string.export_cancelled))
                    return@launch
                }
                
                Log.info { "Export destination: $outputUri" }
                
                // Convert presentation options to domain options
                val domainOptions = ireader.domain.models.epub.ExportOptions(
                    selectedChapters = options.selectedChapters.toSet(),
                    includeCover = options.includeCover,
                    paragraphSpacing = options.formatOptions.paragraphSpacing,
                    chapterHeadingSize = options.formatOptions.chapterHeadingSize,
                    fontFamily = when (options.formatOptions.typography) {
                        ireader.presentation.ui.book.components.Typography.SERIF -> "serif"
                        ireader.presentation.ui.book.components.Typography.SANS_SERIF -> "sans-serif"
                        else -> "serif"
                    },
                    fontSize = 16
                )
                
                // Export the book
                val result = exportBookAsEpubUseCase(
                    bookId = book.id,
                    outputUri = ireader.domain.models.common.Uri.parse(outputUri),
                    options = domainOptions
                ) { progress ->
                    showSnackBar(UiText.DynamicString(progress))
                }
                
                result.onSuccess { filePath ->
                    withUIContext {
                        // Show success message with save location
                        showSnackBar(UiText.DynamicString("EPUB exported successfully to Downloads/IReader/"))
                    }
                    Log.info { "EPUB export successful: $filePath" }
                    Log.info { "File saved to: Downloads/IReader/${book.title}.epub" }
                }.onFailure { error ->
                    withUIContext {
                        showSnackBar(UiText.DynamicString("Export failed: ${error.message}"))
                    }
                    Log.error("EPUB export failed", error)
                }
            } catch (e: Exception) {
                Log.error("Error in EPUB export", e)
                withUIContext {
                    showSnackBar(UiText.DynamicString("Export failed: ${e.message}"))
                }
            }
        }
    }
    
    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("[|\\\\?*<\":>+\\[\\]/']+"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Archive a book using the new use case layer
     * Provides better error handling and consistency
     */
    fun archiveBook(book: Book) {
        applicationScope.launch {
            try {
                bookUseCases.updateArchiveStatus(book.id, isArchived = true)
                withUIContext {
                    showSnackBar(UiText.DynamicString("Book archived: ${book.title}"))
                }
            } catch (e: Exception) {
                withUIContext {
                    showSnackBar(UiText.ExceptionString(e))
                }
            }
        }
    }

    /**
     * Unarchive a book using the new use case layer
     */
    fun unarchiveBook(book: Book) {
        applicationScope.launch {
            try {
                bookUseCases.updateArchiveStatus(book.id, isArchived = false)
                withUIContext {
                    showSnackBar(UiText.DynamicString("Book unarchived: ${book.title}"))
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


    /**
     * Delete chapters using the new use case layer
     * Provides proper cleanup and error handling
     */
    fun deleteChapters(chapters: List<Chapter>) {
        scope.launch(Dispatchers.IO) {
            try {
                chapterUseCases.deleteChapters(chapters)
            } catch (e: Exception) {
                withUIContext {
                    showSnackBar(UiText.ExceptionString(e))
                }
            }
        }
    }

    fun downloadChapters() {
        booksState.book?.let { book ->
            scope.launch {
                when (val result = downloadService.queueChapters(this@BookDetailViewModel.selection.toList())) {
                    is ireader.domain.services.common.ServiceResult.Success -> {
                        showSnackBar(ireader.i18n.UiText.DynamicString("${this@BookDetailViewModel.selection.size} chapters queued for download"))
                    }
                    is ireader.domain.services.common.ServiceResult.Error -> {
                        showSnackBar(ireader.i18n.UiText.DynamicString("Download failed: ${result.message ?: "Unknown error"}"))
                    }
                    else -> {}
                }
            }
        }
    }

    fun startDownloadService(book: Book) {
        scope.launch {
            when (val result = downloadService.queueBooks(listOf(book.id))) {
                is ireader.domain.services.common.ServiceResult.Success -> {
                    showSnackBar(ireader.i18n.UiText.MStringResource(ireader.i18n.resources.Res.string.book_queued_for_download))
                }
                is ireader.domain.services.common.ServiceResult.Error -> {
                    showSnackBar(ireader.i18n.UiText.DynamicString("Download failed: ${result.message ?: "Unknown error"}"))
                }
                else -> {}
            }
        }
    }
    
    fun downloadUnreadChapters() {
        val unreadChapterIds = chapters.filter { !it.read }.map { it.id }
        if (unreadChapterIds.isNotEmpty()) {
            scope.launch {
                when (val result = downloadService.queueChapters(unreadChapterIds)) {
                    is ireader.domain.services.common.ServiceResult.Success -> {
                        showSnackBar(ireader.i18n.UiText.DynamicString("Downloading ${unreadChapterIds.size} unread chapters"))
                    }
                    is ireader.domain.services.common.ServiceResult.Error -> {
                        showSnackBar(ireader.i18n.UiText.DynamicString("Download failed: ${result.message ?: "Unknown error"}"))
                    }
                    else -> {}
                }
            }
        } else {
            scope.launch {
                showSnackBar(ireader.i18n.UiText.MStringResource(ireader.i18n.resources.Res.string.no_unread_chapters_to_download))
            }
        }
    }
    
    fun downloadUndownloadedChapters() {
        val undownloadedChapterIds = chapters.filter { it.content.joinToString("").isBlank() }.map { it.id }
        if (undownloadedChapterIds.isNotEmpty()) {
            scope.launch {
                when (val result = downloadService.queueChapters(undownloadedChapterIds)) {
                    is ireader.domain.services.common.ServiceResult.Success -> {
                        showSnackBar(ireader.i18n.UiText.DynamicString("Downloading ${undownloadedChapterIds.size} chapters"))
                    }
                    is ireader.domain.services.common.ServiceResult.Error -> {
                        showSnackBar(ireader.i18n.UiText.DynamicString("Download failed: ${result.message ?: "Unknown error"}"))
                    }
                    else -> {}
                }
            }
        } else {
            scope.launch {
                showSnackBar(ireader.i18n.UiText.MStringResource(ireader.i18n.resources.Res.string.all_chapters_already_downloaded))
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
        val targetSourceName = sourceSwitchingState.betterSourceName // Save before reset

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

                        showSnackBar(UiText.DynamicString("Successfully migrated to ${targetSourceName ?: "new source"}"))
                    } else {
                        // Migration failed
                        delay(2000) // Show error for a bit longer
                        sourceSwitchingState.showMigrationDialog = false
                        showSnackBar(UiText.DynamicString("Migration failed: ${progress.error ?: "Unknown error"}"))
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

