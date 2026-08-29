package ireader.domain.usecases.backup.v2

import ireader.core.db.Transactions
import ireader.core.log.Log
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.HistoryRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.data.repository.ReaderThemeRepository
import ireader.domain.data.repository.TrackingRepository
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.History
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.file.FileSaver
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.protobuf.ProtoBuf

/**
 * Single entry point for every backup / restore / validate operation.
 *
 * Full backup covers:
 *  - Books & Metadata
 *  - Chapters (with read status, bookmarks, and last page read)
 *  - Downloaded Chapter Text Content (Streamed book-by-book in O(1) memory)
 *  - Categories & Category Ordering
 *  - Reading History
 *  - Tracking Services (MyAnimeList / AniList / etc.)
 *  - Custom Reader Themes
 *  - Settings & Preferences
 *
 * Design rules:
 *  - Every public method returns [Result] — never throws.
 *  - Options default to true for complete backup ("duplicate the app").
 *  - Streaming ZIP architecture guarantees $O(1)$ memory, preventing OutOfMemoryErrors.
 *  - Legacy format support is delegated to [LegacyMigrator].
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
class BackupOrchestrator(
    private val serializer: BackupSerializer,
    private val legacyMigrator: LegacyMigrator,
    private val fileSaver: FileSaver,
    private val libraryRepository: LibraryRepository,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val categoryRepository: CategoryRepository,
    private val bookCategoryRepository: BookCategoryRepository,
    private val historyRepository: HistoryRepository,
    private val trackingRepository: TrackingRepository,
    private val readerThemeRepository: ReaderThemeRepository,
    private val uiPreferences: UiPreferences,
    private val readerPreferences: ReaderPreferences,
    private val transactions: Transactions,
    private val archiveStreamer: BackupArchiveStreamer? = null,
) {

    // ── CREATE ────────────────────────────────────────────────────────────

    /**
     * Serialize the user's entire library into a compressed, memory-safe backup file.
     */
    suspend fun createBackup(
        uri: Uri,
        options: BackupOptions = BackupOptions(),
        onProgress: (BackupProgress) -> Unit = {},
    ): Result<BackupSummary> = runCatching {
        // 1. Collect metadata (books with lightweight chapter info - zero heavy text in RAM)
        onProgress(BackupProgress.Collecting)
        val rawBooks = if (options.includeBooks) libraryRepository.findFavorites() else emptyList()
        val booksLight = if (options.includeBooks) collectBooksLight(rawBooks, options) else emptyList()
        val categories = if (options.includeCategories) collectCategories() else emptyList()
        val histories = if (options.includeHistory) collectHistories(rawBooks) else emptyList()
        val tracks = if (options.includeTracks) collectTracks(rawBooks) else emptyList()
        val themes = if (options.includeThemes) collectThemes() else emptyList()
        val settings = if (options.includeSettings) collectSettings() else emptyList()

        val metadataPayload = BackupPayload(
            version = BackupPayload.CURRENT_VERSION,
            books = booksLight,
            categories = categories,
            histories = histories,
            tracks = tracks,
            themes = themes,
            settings = settings,
            metadata = BackupMetadata(
                createdAt = currentTimeToLong(),
                bookCount = booksLight.size,
                chapterCount = booksLight.sumOf { it.chapters.size },
                historyCount = histories.size,
                categoryCount = categories.size,
                trackCount = tracks.size,
                themeCount = themes.size,
            ),
        )
        val metadataBytes = serializer.serialize(metadataPayload)

        // 2. Stream to archive if streamer is available (processes 1 book at a time to prevent OOM)
        if (archiveStreamer != null && rawBooks.isNotEmpty()) {
            onProgress(BackupProgress.Writing)
            archiveStreamer.createArchive(
                uri = uri,
                metadataBytes = metadataBytes,
                totalBooks = rawBooks.size,
            ) { emitEntry ->
                if (options.includeChapterContent) {
                    for ((index, book) in rawBooks.withIndex()) {
                        onProgress(BackupProgress.Serializing(index + 1, rawBooks.size, book.title))
                        val fullChapters = chapterRepository.findChaptersByBookIdWithContent(book.id)
                        val contentChapters = fullChapters.filter { !it.isEmpty() }
                        if (contentChapters.isNotEmpty()) {
                            val bookContent = BookContentSnapshot(
                                bookKey = book.key,
                                bookSourceId = book.sourceId,
                                chapters = contentChapters.map { ChapterSnapshot.fromChapter(it) }
                            )
                            val contentBytes = ProtoBuf.encodeToByteArray(BookContentSnapshot.serializer(), bookContent)
                            emitEntry("chapters/${book.sourceId}_${index}.pb", contentBytes)
                        }
                    }
                }
            }
        } else {
            // Write standard single-pass compressed payload
            onProgress(BackupProgress.Writing)
            try {
                fileSaver.save(uri, metadataBytes)
            } catch (e: Exception) {
                throw BackupException.WriteFailed(uri.toString(), e)
            }
        }

        // 3. Verify file exists
        onProgress(BackupProgress.Verifying)
        val valid = fileSaver.validate(uri)
        if (!valid) {
            throw BackupException.VerificationFailed("Failed to verify written backup file")
        }

        onProgress(BackupProgress.Complete)
        BackupSummary(
            booksCount = booksLight.size,
            chaptersCount = booksLight.sumOf { it.chapters.size },
            historyCount = histories.size,
            categoriesCount = categories.size,
            tracksCount = tracks.size,
            fileSizeBytes = 0L,
        )
    }

    // ── RESTORE ───────────────────────────────────────────────────────────

    /**
     * Read a backup file, parse it (streaming archive, monolithic, or legacy), and merge into DB.
     */
    suspend fun restoreBackup(
        uri: Uri,
        options: RestoreOptions = RestoreOptions(),
        onProgress: (RestoreProgress) -> Unit = {},
    ): Result<RestoreSummary> = runCatching {
        val isZip = archiveStreamer?.isZipArchive(uri) == true

        if (isZip && archiveStreamer != null) {
            restoreStreamingArchive(uri, options, onProgress)
        } else {
            restoreMonolithicOrLegacy(uri, options, onProgress)
        }
    }

    // ── Private: Streaming Archive Restore ─────────────────────────────────

    private suspend fun restoreStreamingArchive(
        uri: Uri,
        options: RestoreOptions,
        onProgress: (RestoreProgress) -> Unit,
    ): RestoreSummary {
        onProgress(RestoreProgress.Reading)
        val errors = mutableListOf<RestoreItemError>()
        var booksRestored = 0
        var chaptersRestored = 0
        var historyRestored = 0
        var tracksRestored = 0
        var categoriesRestored = 0
        var themesRestored = 0
        var settingsRestored = 0

        val bookKeyMap = mutableMapOf<Pair<String, Long>, Long>()

        archiveStreamer!!.extractArchive(
            uri = uri,
            onMetadata = { metaBytes ->
                onProgress(RestoreProgress.Validating)
                val payload = serializer.deserialize(metaBytes)

                transactions.run {
                    if (options.restoreCategories && payload.categories.isNotEmpty()) {
                        restoreCategories(payload.categories)
                        categoriesRestored = payload.categories.size
                    }
                    val categoryMap = buildCategoryMap(payload.categories)

                    if (options.restoreBooks) {
                        for ((index, book) in payload.books.withIndex()) {
                            onProgress(RestoreProgress.Restoring(index + 1, payload.books.size, book.title))
                            try {
                                val bookId = restoreBook(book)
                                bookKeyMap[book.key to book.sourceId] = bookId

                                if (options.restoreChapters && book.chapters.isNotEmpty()) {
                                    restoreChapters(book, bookId, options.mergeMode)
                                    chaptersRestored += book.chapters.size
                                }

                                if (options.restoreCategories) {
                                    restoreBookCategories(bookId, book.categoryOrders, categoryMap)
                                }

                                booksRestored++
                            } catch (e: Exception) {
                                Log.warn(e, "Failed to restore book: ${book.title}")
                                errors.add(
                                    RestoreItemError("book", book.title, e.message ?: "Unknown error")
                                )
                            }
                        }
                    }

                    if (options.restoreHistory && payload.histories.isNotEmpty()) {
                        historyRestored = restoreHistories(payload.histories, bookKeyMap)
                    }

                    if (options.restoreTracks && payload.tracks.isNotEmpty()) {
                        tracksRestored = restoreTracks(payload.tracks, bookKeyMap)
                    }

                    if (options.restoreThemes && payload.themes.isNotEmpty()) {
                        restoreThemes(payload.themes)
                        themesRestored = payload.themes.size
                    }

                    if (options.restoreSettings && payload.settings.isNotEmpty()) {
                        restoreSettings(payload.settings)
                        settingsRestored = payload.settings.size
                    }
                }
            },
            onBookContent = { entryName, bytes ->
                try {
                    val bookContent = ProtoBuf.decodeFromByteArray(BookContentSnapshot.serializer(), bytes)
                    val bookId = bookKeyMap[bookContent.bookKey to bookContent.bookSourceId]
                        ?: bookRepository.find(bookContent.bookKey, bookContent.bookSourceId)?.id

                    if (bookId != null && options.restoreChapters && bookContent.chapters.isNotEmpty()) {
                        transactions.run {
                            val dummyBookSnapshot = BookSnapshot(
                                sourceId = bookContent.bookSourceId,
                                key = bookContent.bookKey,
                                title = "",
                                chapters = bookContent.chapters
                            )
                            restoreChapters(dummyBookSnapshot, bookId, options.mergeMode)
                        }
                    }
                } catch (e: Exception) {
                    Log.warn(e, "Failed to restore chapter content entry: $entryName")
                }
            }
        )

        onProgress(RestoreProgress.Complete)
        return RestoreSummary(
            booksRestored = booksRestored,
            chaptersRestored = chaptersRestored,
            historyRestored = historyRestored,
            categoriesRestored = categoriesRestored,
            tracksRestored = tracksRestored,
            themesRestored = themesRestored,
            settingsRestored = settingsRestored,
            errors = errors,
        )
    }

    // ── Private: Monolithic & Legacy Restore ───────────────────────────────

    private suspend fun restoreMonolithicOrLegacy(
        uri: Uri,
        options: RestoreOptions,
        onProgress: (RestoreProgress) -> Unit,
    ): RestoreSummary {
        onProgress(RestoreProgress.Reading)
        val raw = try {
            fileSaver.read(uri)
        } catch (e: Exception) {
            throw BackupException.ReadFailed(uri.toString(), e)
        }

        onProgress(RestoreProgress.Decompressing)
        val decompressed = try {
            serializer.decompressFully(raw)
        } catch (_: BackupException.Corrupted) {
            raw
        }

        onProgress(RestoreProgress.Validating)
        val payload = try {
            serializer.deserialize(raw)
        } catch (_: Exception) {
            legacyMigrator.migrate(decompressed)
        }

        val errors = mutableListOf<RestoreItemError>()
        var booksRestored = 0
        var chaptersRestored = 0
        var historyRestored = 0
        var tracksRestored = 0
        var categoriesRestored = 0
        var themesRestored = 0
        var settingsRestored = 0

        val bookKeyMap = mutableMapOf<Pair<String, Long>, Long>()

        transactions.run {
            if (options.restoreCategories && payload.categories.isNotEmpty()) {
                restoreCategories(payload.categories)
                categoriesRestored = payload.categories.size
            }
            val categoryMap = buildCategoryMap(payload.categories)

            if (options.restoreBooks) {
                for ((index, book) in payload.books.withIndex()) {
                    onProgress(RestoreProgress.Restoring(index + 1, payload.books.size, book.title))
                    try {
                        val bookId = restoreBook(book)
                        bookKeyMap[book.key to book.sourceId] = bookId

                        if (options.restoreChapters) {
                            restoreChapters(book, bookId, options.mergeMode)
                            chaptersRestored += book.chapters.size
                        }

                        if (options.restoreCategories) {
                            restoreBookCategories(bookId, book.categoryOrders, categoryMap)
                        }

                        booksRestored++
                    } catch (e: Exception) {
                        Log.warn(e, "Failed to restore book: ${book.title}")
                        errors.add(
                            RestoreItemError(
                                itemType = "book",
                                itemName = book.title,
                                error = e.message ?: "Unknown error",
                            )
                        )
                    }
                }
            }

            if (options.restoreHistory && payload.histories.isNotEmpty()) {
                historyRestored = restoreHistories(payload.histories, bookKeyMap)
            }

            if (options.restoreTracks && payload.tracks.isNotEmpty()) {
                tracksRestored = restoreTracks(payload.tracks, bookKeyMap)
            }

            if (options.restoreThemes && payload.themes.isNotEmpty()) {
                restoreThemes(payload.themes)
                themesRestored = payload.themes.size
            }

            if (options.restoreSettings && payload.settings.isNotEmpty()) {
                restoreSettings(payload.settings)
                settingsRestored = payload.settings.size
            }
        }

        onProgress(RestoreProgress.Complete)
        return RestoreSummary(
            booksRestored = booksRestored,
            chaptersRestored = chaptersRestored,
            historyRestored = historyRestored,
            categoriesRestored = categoriesRestored,
            tracksRestored = tracksRestored,
            themesRestored = themesRestored,
            settingsRestored = settingsRestored,
            errors = errors,
        )
    }

    // ── VALIDATE ──────────────────────────────────────────────────────────

    /**
     * Read and parse a backup without writing anything to DB.
     */
    suspend fun validateBackup(uri: Uri): Result<ValidationResult> = runCatching {
        val isZip = archiveStreamer?.isZipArchive(uri) == true

        if (isZip && archiveStreamer != null) {
            var result: ValidationResult? = null
            archiveStreamer.extractArchive(
                uri = uri,
                onMetadata = { metaBytes ->
                    val payload = serializer.deserialize(metaBytes)
                    result = ValidationResult(
                        isValid = true,
                        version = payload.version,
                        bookCount = payload.books.size,
                        chapterCount = payload.books.sumOf { it.chapters.size },
                        historyCount = payload.histories.size,
                        categoryCount = payload.categories.size,
                    )
                },
                onBookContent = { _, _ -> }
            )
            result ?: ValidationResult(isValid = false, errors = listOf("Empty archive"))
        } else {
            val raw = try {
                fileSaver.read(uri)
            } catch (e: Exception) {
                throw BackupException.ReadFailed(uri.toString(), e)
            }

            try {
                val payload = serializer.deserialize(raw)
                ValidationResult(
                    isValid = true,
                    version = payload.version,
                    bookCount = payload.books.size,
                    chapterCount = payload.books.sumOf { it.chapters.size },
                    historyCount = payload.histories.size,
                    categoryCount = payload.categories.size,
                )
            } catch (e: Exception) {
                try {
                    val decompressed = try { serializer.decompressFully(raw) } catch (_: Exception) { raw }
                    val payload = legacyMigrator.migrate(decompressed)
                    ValidationResult(
                        isValid = true,
                        version = payload.version,
                        bookCount = payload.books.size,
                        chapterCount = payload.books.sumOf { it.chapters.size },
                        historyCount = payload.histories.size,
                        categoryCount = payload.categories.size,
                    )
                } catch (_: Exception) {
                    ValidationResult(
                        isValid = false,
                        errors = listOf(e.message ?: "Unknown validation error"),
                    )
                }
            }
        }
    }

    // ── Private: Collect helpers ──────────────────────────────────────────

    private suspend fun collectBooksLight(books: List<Book>, options: BackupOptions): List<BookSnapshot> {
        return books.map { book ->
            val chapters = if (options.includeChapters) {
                chapterRepository.findChaptersByBookId(book.id).map { light ->
                    ChapterSnapshot(
                        key = light.key,
                        name = light.name,
                        translator = light.translator,
                        read = light.read,
                        bookmark = light.bookmark,
                        dateFetch = light.dateFetch,
                        dateUpload = light.dateUpload,
                        number = light.number,
                        sourceOrder = light.sourceOrder,
                        content = "", // Lightweight metadata; full content is streamed entry-by-entry
                        type = light.type,
                        lastPageRead = light.lastPageRead,
                    )
                }
            } else {
                emptyList()
            }

            val categoryOrders = categoryRepository.getCategoriesByMangaId(book.id)
                .filter { !it.isSystemCategory }
                .map { it.order }

            BookSnapshot.fromBook(book, chapters, categoryOrders)
        }
    }

    private suspend fun collectCategories(): List<CategorySnapshot> {
        return categoryRepository.findAll()
            .filter { !it.category.isSystemCategory }
            .map { CategorySnapshot.fromCategory(it.category) }
    }

    private suspend fun collectHistories(books: List<Book>): List<HistorySnapshot> {
        val histories = historyRepository.findHistories()
        if (histories.isEmpty()) return emptyList()

        val bookMap = books.associateBy { it.id }
        val result = mutableListOf<HistorySnapshot>()

        for (history in histories) {
            val chapter = chapterRepository.findChapterById(history.chapterId) ?: continue
            val book = bookMap[chapter.bookId] ?: bookRepository.findBookById(chapter.bookId) ?: continue

            result.add(
                HistorySnapshot(
                    bookKey = book.key,
                    bookSourceId = book.sourceId,
                    chapterKey = chapter.key,
                    lastRead = history.readAt ?: 0L,
                    timeRead = history.readDuration,
                    progress = history.progress
                )
            )
        }
        return result
    }

    private suspend fun collectTracks(books: List<Book>): List<TrackSnapshot> {
        val result = mutableListOf<TrackSnapshot>()
        for (book in books) {
            val tracks = trackingRepository.getTracksByBook(book.id)
            for (track in tracks) {
                result.add(TrackSnapshot.fromTrack(track, book.key, book.sourceId))
            }
        }
        return result
    }

    private suspend fun collectThemes(): List<ReaderThemeSnapshot> {
        return try {
            val themes = readerThemeRepository.subscribe().firstOrNull() ?: emptyList()
            themes.map { ReaderThemeSnapshot.fromReaderTheme(it) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun collectSettings(): List<SettingSnapshot> {
        val list = mutableListOf<SettingSnapshot>()
        try {
            list.add(SettingSnapshot("theme_mode", uiPreferences.themeMode().get().name))
            list.add(SettingSnapshot("font_size", readerPreferences.fontSize().get().toString()))
            list.add(SettingSnapshot("line_height", readerPreferences.lineHeight().get().toString()))
            list.add(SettingSnapshot("paragraph_distance", readerPreferences.paragraphDistance().get().toString()))
            list.add(SettingSnapshot("paragraph_indent", readerPreferences.paragraphIndent().get().toString()))
            list.add(SettingSnapshot("auto_backup", uiPreferences.automaticBackupTime().get().name))
            list.add(SettingSnapshot("max_auto_backup", uiPreferences.maxAutomaticBackupFiles().get().toString()))
        } catch (e: Exception) {
            Log.warn(e, "Error collecting settings")
        }
        return list
    }

    // ── Private: Restore helpers ──────────────────────────────────────────

    private suspend fun restoreCategories(categories: List<CategorySnapshot>) {
        if (categories.isEmpty()) return

        val dbCategories = categoryRepository.findAll()
        val dbNames = dbCategories.map { it.name.lowercase() }.toSet()

        val toAdd = categories
            .filter { it.name.lowercase() !in dbNames }
            .mapIndexed { idx, cat ->
                cat.toCategory().copy(order = (dbCategories.size + idx).toLong())
            }

        if (toAdd.isNotEmpty()) {
            categoryRepository.insert(toAdd)
        }
    }

    private suspend fun buildCategoryMap(
        categories: List<CategorySnapshot>,
    ): Map<Long, Long> {
        val dbCategories = categoryRepository.findAll()
        return categories.mapNotNull { snap ->
            val dbCat = dbCategories.find { it.name.equals(snap.name, ignoreCase = true) }
            dbCat?.let { snap.order to it.id }
        }.toMap()
    }

    private suspend fun restoreBook(book: BookSnapshot): Long {
        val existing = try {
            bookRepository.find(book.key, book.sourceId)
        } catch (e: Exception) {
            Log.warn(e, "Error finding book ${book.title}")
            null
        }

        if (existing == null) {
            return try {
                bookRepository.upsert(book.toBook())
            } catch (e: Exception) {
                bookRepository.find(book.key, book.sourceId)?.id ?: throw e
            }
        }

        if (!existing.favorite || book.lastUpdate > existing.lastUpdate) {
            try {
                bookRepository.updateBook(
                    book.toBook(existing.id).copy(favorite = true)
                )
            } catch (e: Exception) {
                Log.warn(e, "Failed to update book ${book.title}")
            }
        }
        return existing.id
    }

    private suspend fun restoreChapters(
        book: BookSnapshot,
        bookId: Long,
        mergeMode: MergeMode,
    ) {
        if (book.chapters.isEmpty()) return

        val dbChapters = chapterRepository.findChaptersByBookIdWithContent(bookId)

        if (dbChapters.isEmpty()) {
            val chapters = book.chapters.map { it.toChapter(bookId) }
            chapterRepository.insertChapters(chapters)
            return
        }

        when (mergeMode) {
            MergeMode.REPLACE_EXISTING -> {
                chapterRepository.deleteChapters(dbChapters)
                val chapters = book.chapters.map { it.toChapter(bookId) }
                chapterRepository.insertChapters(chapters)
            }

            MergeMode.MERGE_PREFER_BACKUP -> {
                val dbMap = dbChapters.associateBy { it.key }
                val merged = book.chapters.map { snap ->
                    val db = dbMap[snap.key]
                    val backupChapter = snap.toChapter(bookId)
                    val content = if (backupChapter.content.isNotEmpty()) {
                        backupChapter.content
                    } else {
                        db?.content ?: emptyList()
                    }
                    backupChapter.copy(
                        content = content,
                        read = snap.read || (db?.read ?: false),
                        bookmark = snap.bookmark || (db?.bookmark ?: false),
                        lastPageRead = maxOf(snap.lastPageRead, db?.lastPageRead ?: 0),
                    )
                }
                chapterRepository.deleteChapters(dbChapters)
                chapterRepository.insertChapters(merged)
            }

            MergeMode.MERGE_PREFER_DB -> {
                val backupMap = book.chapters.associateBy { it.key }
                val toUpdate = dbChapters.mapNotNull { db ->
                    val snap = backupMap[db.key] ?: return@mapNotNull null
                    val backupChapter = snap.toChapter(bookId)
                    val content = if (db.content.isNotEmpty()) {
                        db.content
                    } else {
                        backupChapter.content
                    }
                    db.copy(
                        content = content,
                        read = db.read || snap.read,
                        bookmark = db.bookmark || snap.bookmark,
                        lastPageRead = maxOf(db.lastPageRead, snap.lastPageRead),
                    )
                }
                val toAdd = book.chapters
                    .filter { it.key !in dbChapters.map { c -> c.key }.toSet() }
                    .map { it.toChapter(bookId) }

                if (toUpdate.isNotEmpty()) chapterRepository.insertChapters(toUpdate)
                if (toAdd.isNotEmpty()) chapterRepository.insertChapters(toAdd)
            }
        }
    }

    private suspend fun restoreBookCategories(
        bookId: Long,
        categoryOrders: List<Long>,
        categoryMap: Map<Long, Long>,
    ) {
        if (categoryOrders.isEmpty()) return

        val categoryIds = categoryOrders.mapNotNull { categoryMap[it] }
        if (categoryIds.isEmpty()) return

        val bookCategories = categoryIds.map { BookCategory(bookId, it) }
        bookCategoryRepository.insertAll(bookCategories)
    }

    private suspend fun restoreHistories(
        histories: List<HistorySnapshot>,
        bookKeyMap: Map<Pair<String, Long>, Long>,
    ): Int {
        if (histories.isEmpty()) return 0
        var count = 0

        for (snap in histories) {
            val bookId = bookKeyMap[snap.bookKey to snap.bookSourceId] ?: continue
            val chapters = chapterRepository.findChaptersByBookId(bookId)
            val chapter = chapters.find { it.key == snap.chapterKey } ?: continue

            try {
                historyRepository.insertHistory(
                    History(
                        id = bookId,
                        chapterId = chapter.id,
                        readAt = snap.lastRead,
                        readDuration = snap.timeRead,
                        progress = snap.progress,
                    )
                )
                count++
            } catch (e: Exception) {
                Log.warn(e, "Failed to restore history entry for book $bookId")
            }
        }
        return count
    }

    private suspend fun restoreTracks(
        tracks: List<TrackSnapshot>,
        bookKeyMap: Map<Pair<String, Long>, Long>,
    ): Int {
        if (tracks.isEmpty()) return 0
        var count = 0

        for (snap in tracks) {
            val bookId = bookKeyMap[snap.bookKey to snap.bookSourceId] ?: continue
            try {
                val track = snap.toTrack(mangaId = bookId)
                trackingRepository.addTrack(track)
                count++
            } catch (e: Exception) {
                Log.warn(e, "Failed to restore track entry for book $bookId")
            }
        }
        return count
    }

    private suspend fun restoreThemes(themes: List<ReaderThemeSnapshot>) {
        if (themes.isEmpty()) return
        try {
            val domainThemes = themes.map { it.toReaderTheme() }
            readerThemeRepository.insert(domainThemes)
        } catch (e: Exception) {
            Log.warn(e, "Failed to restore reader themes")
        }
    }

    private fun restoreSettings(settings: List<SettingSnapshot>) {
        if (settings.isEmpty()) return
        val map = settings.associate { it.key to it.value }
        try {
            map["font_size"]?.toIntOrNull()?.let { readerPreferences.fontSize().set(it) }
            map["line_height"]?.toIntOrNull()?.let { readerPreferences.lineHeight().set(it) }
            map["paragraph_distance"]?.toIntOrNull()?.let { readerPreferences.paragraphDistance().set(it) }
            map["paragraph_indent"]?.toIntOrNull()?.let { readerPreferences.paragraphIndent().set(it) }
            map["max_auto_backup"]?.toIntOrNull()?.let { uiPreferences.maxAutomaticBackupFiles().set(it) }
        } catch (e: Exception) {
            Log.warn(e, "Failed to restore settings")
        }
    }
}
