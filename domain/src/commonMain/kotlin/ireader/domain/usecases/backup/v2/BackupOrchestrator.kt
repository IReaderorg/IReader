package ireader.domain.usecases.backup.v2

import ireader.core.db.Transactions
import ireader.core.log.Log
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.data.repository.LibraryRepository
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.BookCategory
import ireader.domain.usecases.file.FileSaver
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Single entry point for every backup / restore / validate operation.
 *
 * Design rules:
 *  - Every public method returns [Result] — never throws.
 *  - Progress is reported via a callback, not a Flow (simpler cancellation).
 *  - The serializer handles format, compression, and checksum — this class
 *    only talks to the database and the file system.
 *  - Legacy format support is delegated to [LegacyMigrator].
 */
class BackupOrchestrator(
    private val serializer: BackupSerializer,
    private val legacyMigrator: LegacyMigrator,
    private val fileSaver: FileSaver,
    private val libraryRepository: LibraryRepository,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val categoryRepository: CategoryRepository,
    private val bookCategoryRepository: BookCategoryRepository,
    private val transactions: Transactions,
) {

    // ── CREATE ────────────────────────────────────────────────────────────

    /**
     * Serialize the user's library into a compressed, checksummed backup file.
     *
     * Pipeline:  collect → serialize → compress → write → verify
     *
     * Every step can fail; every failure is caught and returned as [Result.failure].
     */
    suspend fun createBackup(
        uri: Uri,
        options: BackupOptions = BackupOptions(),
        onProgress: (BackupProgress) -> Unit = {},
    ): Result<BackupSummary> = runCatching {
        // 1. Collect
        onProgress(BackupProgress.Collecting)
        val books = collectBooks(options)
        val categories = if (options.includeCategories) collectCategories() else emptyList()

        // 2. Serialize + compress (handled by serializer)
        onProgress(BackupProgress.Compressing)
        val payload = BackupPayload(
            version = BackupPayload.CURRENT_VERSION,
            books = books,
            categories = categories,
            metadata = BackupMetadata(
                createdAt = currentTimeToLong(),
                bookCount = books.size,
                chapterCount = books.sumOf { it.chapters.size },
            ),
        )
        val bytes = serializer.serialize(payload)

        // 3. Write
        onProgress(BackupProgress.Writing)
        try {
            fileSaver.save(uri, bytes)
        } catch (e: Exception) {
            throw BackupException.WriteFailed(uri.toString(), e)
        }

        // 4. Verify: read back and compare
        onProgress(BackupProgress.Verifying)
        val written = try {
            fileSaver.read(uri)
        } catch (e: Exception) {
            throw BackupException.ReadFailed(uri.toString(), e)
        }
        if (!written.contentEquals(bytes)) {
            throw BackupException.VerificationFailed("Read-back bytes differ from written bytes")
        }

        // 5. Verify: deserialize to confirm integrity
        serializer.deserialize(written)

        onProgress(BackupProgress.Complete)
        BackupSummary(
            booksCount = books.size,
            chaptersCount = books.sumOf { it.chapters.size },
            fileSizeBytes = bytes.size.toLong(),
        )
    }

    // ── RESTORE ───────────────────────────────────────────────────────────

    /**
     * Read a backup file, parse it (current or legacy), and merge into the DB.
     *
     * Pipeline:  read → decompress → parse (try current, fallback to legacy) → restore → done
     *
     * Partial failures (individual book/chapter) are logged and collected in
     * [RestoreSummary.errors] but do NOT abort the entire restore.
     */
    suspend fun restoreBackup(
        uri: Uri,
        options: RestoreOptions = RestoreOptions(),
        onProgress: (RestoreProgress) -> Unit = {},
    ): Result<RestoreSummary> = runCatching {
        // 1. Read
        onProgress(RestoreProgress.Reading)
        val raw = try {
            fileSaver.read(uri)
        } catch (e: Exception) {
            throw BackupException.ReadFailed(uri.toString(), e)
        }

        // 2. Parse: try current format first, then legacy
        onProgress(RestoreProgress.Decompressing)
        val decompressed = try {
            serializer.decompress(raw)
        } catch (e: BackupException.Corrupted) {
            // Might be raw (uncompressed) legacy data — try as-is
            raw
        }

        onProgress(RestoreProgress.Validating)
        val payload = try {
            serializer.deserialize(raw)
        } catch (_: Exception) {
            // Current format failed — try legacy migrator on decompressed bytes
            legacyMigrator.migrate(decompressed)
        }

        // 3. Restore into DB
        val errors = mutableListOf<RestoreItemError>()
        var booksRestored = 0
        var chaptersRestored = 0

        transactions.run {
            if (options.restoreCategories) {
                restoreCategories(payload.categories)
            }
            val categoryMap = buildCategoryMap(payload.categories)

            for ((index, book) in payload.books.withIndex()) {
                onProgress(RestoreProgress.Restoring(index + 1, payload.books.size, book.title))
                try {
                    val bookId = restoreBook(book)

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

        onProgress(RestoreProgress.Complete)
        RestoreSummary(
            booksRestored = booksRestored,
            chaptersRestored = chaptersRestored,
            errors = errors,
        )
    }

    // ── VALIDATE ──────────────────────────────────────────────────────────

    /**
     * Read and parse a backup without writing anything to the DB.
     * Returns metadata about what the backup contains.
     */
    suspend fun validateBackup(uri: Uri): Result<ValidationResult> = runCatching {
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
            )
        } catch (e: Exception) {
            // Try legacy
            try {
                val decompressed = try { serializer.decompress(raw) } catch (_: Exception) { raw }
                val payload = legacyMigrator.migrate(decompressed)
                ValidationResult(
                    isValid = true,
                    version = payload.version,
                    bookCount = payload.books.size,
                    chapterCount = payload.books.sumOf { it.chapters.size },
                )
            } catch (_: Exception) {
                ValidationResult(
                    isValid = false,
                    errors = listOf(e.message ?: "Unknown validation error"),
                )
            }
        }
    }

    // ── Private: Collect ──────────────────────────────────────────────────

    private suspend fun collectBooks(options: BackupOptions): List<BookSnapshot> {
        val books = libraryRepository.findFavorites()
        return books.map { book ->
            val chapters = if (options.includeChapters) {
                chapterRepository.findChaptersByBookId(book.id).map {
                    ChapterSnapshot.fromChapter(it)
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
                // Unique constraint race — try to find the existing one
                bookRepository.find(book.key, book.sourceId)?.id ?: throw e
            }
        }

        // Update if backup is newer or book isn't favorited yet
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

        val dbChapters = chapterRepository.findChaptersByBookId(bookId)

        if (dbChapters.isEmpty()) {
            // Fresh insert — no conflicts possible
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
                    snap.toChapter(bookId).copy(
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
                    db.copy(
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
}
