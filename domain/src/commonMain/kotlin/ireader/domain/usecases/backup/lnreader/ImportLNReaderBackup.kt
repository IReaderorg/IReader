package ireader.domain.usecases.backup.lnreader

import ireader.core.db.Transactions
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.Category
import ireader.domain.models.entities.Chapter
import ireader.domain.models.lnreader.LNReaderCategory
import ireader.domain.models.lnreader.LNReaderChapter
import ireader.domain.models.lnreader.LNReaderNovel
import ireader.domain.usecases.file.FileSaver
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.buffer
import kotlin.time.ExperimentalTime

/**
 * Use case for importing LNReader backup files into IReader
 */
class ImportLNReaderBackup(
    private val parser: LNReaderBackupParser,
    private val sourceMapper: LNReaderSourceMapper,
    private val bookRepository: BookRepository,
    private val chapterRepository: ChapterRepository,
    private val categoryRepository: CategoryRepository,
    private val bookCategoryRepository: BookCategoryRepository,
    private val transactions: Transactions,
    private val fileSaver: FileSaver
) {

    /**
     * Import options for customizing the import behavior
     */
    data class ImportOptions(
        val importLibrary: Boolean = true,
        val importCategories: Boolean = true,
        val importSettings: Boolean = false,
        val importReadProgress: Boolean = true,
        val conflictStrategy: ConflictStrategy = ConflictStrategy.MERGE
    )

    /**
     * Strategy for handling conflicts with existing data
     */
    enum class ConflictStrategy {
        SKIP,      // Skip existing novels
        MERGE,     // Merge reading progress
        OVERWRITE  // Overwrite existing data
    }

    /**
     * Progress updates during import
     */
    sealed class ImportProgress {
        object Starting : ImportProgress()
        data class Parsing(val message: String) : ImportProgress()
        data class ImportingNovels(val current: Int, val total: Int, val novelName: String) :
            ImportProgress()

        data class ImportingCategories(val current: Int, val total: Int) : ImportProgress()
        data class Complete(val result: ImportResult) : ImportProgress()
        data class Error(val error: Throwable) : ImportProgress()
    }

    /**
     * Result of the import operation
     */
    data class ImportResult(
        val novelsImported: Int,
        val novelsSkipped: Int,
        val novelsFailed: Int,
        val chaptersImported: Int,
        val categoriesImported: Int,
        val errors: List<ImportError>
    )

    /**
     * Individual import error
     */
    data class ImportError(
        val itemType: String,
        val itemName: String,
        val error: String
    )

    /**
     * Execute the import from a URI
     */
    suspend fun invoke(
        uri: Uri,
        options: ImportOptions = ImportOptions()
    ): Flow<ImportProgress> = flow {
        emit(ImportProgress.Starting)

        try {
            // Parse backup - use readSource() to get raw bytes without GZIP decompression
            // LNReader backups are ZIP files, not GZIP compressed
            emit(ImportProgress.Parsing("Reading backup file..."))
            val bytes = try {
                fileSaver.readSource(uri).buffer().use { it.readByteArray() }
            } catch (e: Exception) {
                throw LNReaderImportException.ReadFailedException(
                    e.message ?: "Unable to read file", e
                )
            }

            // Verify it's an LNReader backup
            if (!LNReaderBackupParser.isLNReaderBackup(bytes)) {
                emit(ImportProgress.Error(LNReaderImportException.InvalidBackupException()))
                return@flow
            }

            emit(ImportProgress.Parsing("Parsing backup data..."))
            val backup = try {
                parser.parseBackup(bytes)
            } catch (e: Exception) {
                throw LNReaderImportException.ParseFailedException(
                    e.message ?: "Unable to parse backup", e
                )
            }

            // Check if backup has any content
            if (backup.novels.isEmpty() && backup.categories.isEmpty()) {
                emit(ImportProgress.Error(LNReaderImportException.EmptyBackupException()))
                return@flow
            }

            ireader.core.log.Log.debug { "LNReader backup parsed: ${backup.novels.size} novels, ${backup.categories.size} categories" }

            // Track LNReader novel ID to IReader book ID mapping
            val novelIdMap = mutableMapOf<Int, Long>()

            var novelsImported = 0
            var novelsSkipped = 0
            var novelsFailed = 0
            var chaptersImported = 0
            var categoriesImported = 0
            val errors = mutableListOf<ImportError>()

            // Import categories first (outside transaction for progress updates)
            // Skip default/system categories and redirect their novels to "LNReader" category
            val defaultCategoryNames = setOf(
                "local", "default", "uncategorized", "all", "library"
            )

            // Track which novel IDs were in default categories (to add to "LNReader" category later)
            val novelsInDefaultCategories = mutableSetOf<Int>()

            if (options.importCategories && backup.categories.isNotEmpty()) {
                val categoryIdMap = mutableMapOf<Int, Long>()

                // Filter out default categories and collect their novel IDs
                val categoriesToImport = backup.categories.filter { category ->
                    val isDefault = defaultCategoryNames.any {
                        category.name.equals(it, ignoreCase = true)
                    }
                    if (isDefault) {
                        // Collect novels from default categories
                        novelsInDefaultCategories.addAll(category.novelIds)
                        ireader.core.log.Log.debug { "Skipping default category: ${category.name}, novels: ${category.novelIds.size}" }
                    }
                    !isDefault
                }

                categoriesToImport.forEachIndexed { index, category ->
                    emit(ImportProgress.ImportingCategories(index + 1, categoriesToImport.size))
                    try {
                        val newId = importCategory(category)
                        categoryIdMap[category.id] = newId
                        categoriesImported++
                    } catch (e: Exception) {
                        errors.add(
                            ImportError(
                                "Category",
                                category.name,
                                e.message ?: "Unknown error"
                            )
                        )
                        ireader.core.log.Log.warn(e, "Failed to import category: ${category.name}")
                    }
                }

                // Import category-novel associations after novels are imported
                // Store for later use
            }

            // Import novels
            if (options.importLibrary && backup.novels.isNotEmpty()) {
                backup.novels.forEachIndexed { index, novel ->
                    emit(ImportProgress.ImportingNovels(index + 1, backup.novels.size, novel.name))
                    try {
                        val result = importNovel(novel, options)
                        when (result) {
                            is NovelImportResult.Imported -> {
                                novelsImported++
                                chaptersImported += result.chaptersImported
                                novelIdMap[novel.id] = result.bookId
                            }

                            is NovelImportResult.Skipped -> {
                                novelsSkipped++
                                // Still track the mapping for category associations
                                val sourceId = sourceMapper.mapPluginId(novel.pluginId)
                                    ?: sourceMapper.getUnmappedSourceId()
                                val existing = bookRepository.find(novel.path, sourceId)
                                if (existing != null) {
                                    novelIdMap[novel.id] = existing.id
                                }
                            }

                            is NovelImportResult.Failed -> {
                                novelsFailed++
                                errors.add(ImportError("Novel", novel.name, result.error))
                            }
                        }
                    } catch (e: Exception) {
                        novelsFailed++
                        errors.add(ImportError("Novel", novel.name, e.message ?: "Unknown error"))
                        ireader.core.log.Log.warn(e, "Failed to import novel: ${novel.name}")
                    }
                }
            }

            // Import category-novel associations
            if (options.importCategories && backup.categories.isNotEmpty()) {
                val allCategories = categoryRepository.getAll()

                // Create or get "LNReader" category for novels from default categories
                var lnReaderCategoryId: Long? = null
                if (novelsInDefaultCategories.isNotEmpty()) {
                    val existingLnReaderCategory = allCategories.find {
                        it.name.equals("LNReader", ignoreCase = true)
                    }
                    lnReaderCategoryId = if (existingLnReaderCategory != null) {
                        existingLnReaderCategory.id
                    } else {
                        // Create "LNReader" category
                        val newCategory = Category(
                            id = 0,
                            name = "LNReader",
                            order = (allCategories.maxOfOrNull { it.order } ?: 0) + 1,
                            flags = 0
                        )
                        categoryRepository.insert(newCategory)
                        categoriesImported++
                        // Get the newly created category ID
                        categoryRepository.getAll().find {
                            it.name.equals("LNReader", ignoreCase = true)
                        }?.id
                    }
                }

                // Associate novels from default categories with "LNReader" category
                if (lnReaderCategoryId != null) {
                    for (lnNovelId in novelsInDefaultCategories) {
                        val bookId = novelIdMap[lnNovelId] ?: continue
                        try {
                            bookCategoryRepository.insert(BookCategory(bookId, lnReaderCategoryId))
                        } catch (e: Exception) {
                            // Ignore duplicate associations
                        }
                    }
                }

                // Associate novels with their non-default categories
                for (category in backup.categories) {
                    // Skip default categories
                    if (defaultCategoryNames.any { category.name.equals(it, ignoreCase = true) }) {
                        continue
                    }

                    try {
                        val categoryId = allCategories.find {
                            it.name.equals(category.name, ignoreCase = true)
                        }?.id ?: continue
                        for (lnNovelId in category.novelIds) {
                            val bookId = novelIdMap[lnNovelId] ?: continue
                            try {
                                bookCategoryRepository.insert(BookCategory(bookId, categoryId))
                            } catch (e: Exception) {
                                // Ignore duplicate associations
                            }
                        }
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(
                            e,
                            "Failed to import category associations for: ${category.name}"
                        )
                    }
                }
            }

            val result = ImportResult(
                novelsImported = novelsImported,
                novelsSkipped = novelsSkipped,
                novelsFailed = novelsFailed,
                chaptersImported = chaptersImported,
                categoriesImported = categoriesImported,
                errors = errors
            )

            emit(ImportProgress.Complete(result))

        } catch (e: LNReaderImportException) {
            ireader.core.log.Log.error(e, "LNReader backup import failed: ${e.message}")
            emit(ImportProgress.Error(e))
        } catch (e: OutOfMemoryError) {
            ireader.core.log.Log.error(e, "LNReader backup import failed: Out of memory")
            emit(ImportProgress.Error(LNReaderImportException.OutOfMemoryException(cause = e)))
        } catch (e: Exception) {
            ireader.core.log.Log.error(e, "LNReader backup import failed")
            emit(ImportProgress.Error(LNReaderImportException.fromException(e)))
        }
    }

    /**
     * Import from byte array directly
     */
    suspend fun invoke(
        bytes: ByteArray,
        options: ImportOptions = ImportOptions()
    ): Flow<ImportProgress> = flow {
        emit(ImportProgress.Starting)

        try {
            if (!LNReaderBackupParser.isLNReaderBackup(bytes)) {
                emit(ImportProgress.Error(LNReaderImportException.InvalidBackupException()))
                return@flow
            }

            emit(ImportProgress.Parsing("Parsing backup data..."))
            val backup = try {
                parser.parseBackup(bytes)
            } catch (e: Exception) {
                throw LNReaderImportException.ParseFailedException(
                    e.message ?: "Unable to parse backup", e
                )
            }

            if (backup.novels.isEmpty() && backup.categories.isEmpty()) {
                emit(ImportProgress.Error(LNReaderImportException.EmptyBackupException()))
                return@flow
            }

            // Delegate to common implementation by creating a temporary URI
            // For now, just emit an error as this method is not fully implemented
            emit(ImportProgress.Error(LNReaderImportException.UnknownException("Direct byte array import not fully implemented")))
        } catch (e: LNReaderImportException) {
            emit(ImportProgress.Error(e))
        } catch (e: Exception) {
            emit(ImportProgress.Error(LNReaderImportException.fromException(e)))
        }
    }

    private sealed class NovelImportResult {
        data class Imported(val bookId: Long, val chaptersImported: Int) : NovelImportResult()
        object Skipped : NovelImportResult()
        data class Failed(val error: String) : NovelImportResult()
    }

    private suspend fun importCategory(category: LNReaderCategory): Long {
        // Find existing category by name
        val allCategories = categoryRepository.getAll()
        val existing = allCategories.find { it.name.equals(category.name, ignoreCase = true) }
        if (existing != null) {
            return existing.id
        }

        val newCategory = Category(
            id = 0,
            name = category.name,
            order = category.sort.toLong(),
            flags = 0
        )
        categoryRepository.insert(newCategory)

        // Get the newly inserted category's ID
        val inserted =
            categoryRepository.getAll().find { it.name.equals(category.name, ignoreCase = true) }
        return inserted?.id ?: 0L
    }

    private suspend fun importNovel(
        novel: LNReaderNovel,
        options: ImportOptions
    ): NovelImportResult {
        // Map source
        val sourceId = sourceMapper.mapPluginId(novel.pluginId)
            ?: sourceMapper.getUnmappedSourceId()

        // Check for existing
        val existing = bookRepository.find(novel.path, sourceId)

        if (existing != null) {
            when (options.conflictStrategy) {
                ConflictStrategy.SKIP -> return NovelImportResult.Skipped
                ConflictStrategy.MERGE -> {
                    // Merge chapters only
                    val chaptersImported = mergeChapters(existing.id, novel.chapters, options)
                    return NovelImportResult.Imported(existing.id, chaptersImported)
                }

                ConflictStrategy.OVERWRITE -> {
                    // Delete existing chapters and reimport
                    chapterRepository.deleteChaptersByBookId(existing.id)
                }
            }
        }

        // Create or update book
        // Handle cover URL - LNReader stores local covers as relative paths
        val coverUrl = processCoverUrl(novel.cover)

        val book = Book(
            id = existing?.id ?: 0,
            sourceId = sourceId,
            key = novel.path,
            title = novel.name,
            author = novel.author ?: "",
            description = novel.summary ?: "",
            genres = parseGenres(novel.genres),
            status = mapStatus(novel.status),
            cover = coverUrl,
            favorite = novel.inLibrary,
            initialized = true,
            dateAdded = currentTimeToLong()
        )

        val bookId = bookRepository.upsert(book)

        // Import chapters
        val chaptersImported = importChapters(bookId, novel.chapters, options)

        return NovelImportResult.Imported(bookId, chaptersImported)
    }

    private suspend fun importChapters(
        bookId: Long,
        chapters: List<LNReaderChapter>,
        options: ImportOptions
    ): Int {
        if (chapters.isEmpty()) return 0

        val newChapters = chapters.mapIndexed { index, chapter ->
            Chapter(
                id = 0,
                bookId = bookId,
                key = chapter.path,
                name = chapter.name,
                read = !chapter.unread && options.importReadProgress,
                bookmark = chapter.bookmark,
                dateUpload = parseDate(chapter.releaseTime),
                dateFetch = currentTimeToLong(),
                number = chapter.chapterNumber ?: (index + 1).toFloat(),
                sourceOrder = (chapter.position ?: index).toLong(),
                lastPageRead = if (options.importReadProgress) {
                    ((chapter.progress ?: 0f) * 100).toLong()
                } else 0,
                content = emptyList(),
                type = 0,
                translator = ""
            )
        }

        chapterRepository.insertChapters(newChapters)
        return newChapters.size
    }

    private suspend fun mergeChapters(
        bookId: Long,
        backupChapters: List<LNReaderChapter>,
        options: ImportOptions
    ): Int {
        val existingChapters = chapterRepository.findChaptersByBookId(bookId)
        val existingMap = existingChapters.associateBy { it.key }

        var merged = 0
        val chaptersToUpdate = mutableListOf<Chapter>()
        val chaptersToAdd = mutableListOf<Chapter>()

        for ((index, backupChapter) in backupChapters.withIndex()) {
            val existing = existingMap[backupChapter.path]

            if (existing != null && options.importReadProgress) {
                // Merge read status
                val shouldUpdate = (!backupChapter.unread && !existing.read) ||
                        (backupChapter.bookmark && !existing.bookmark) ||
                        ((backupChapter.progress ?: 0f) * 100).toLong() > existing.lastPageRead

                if (shouldUpdate) {
                    val updated = existing.copy(
                        read = existing.read || !backupChapter.unread,
                        bookmark = existing.bookmark || backupChapter.bookmark,
                        lastPageRead = maxOf(
                            existing.lastPageRead,
                            ((backupChapter.progress ?: 0f) * 100).toLong()
                        )
                    )
                    chaptersToUpdate.add(updated)
                    merged++
                }
            } else if (existing == null) {
                // Add new chapter
                val newChapter = Chapter(
                    id = 0,
                    bookId = bookId,
                    key = backupChapter.path,
                    name = backupChapter.name,
                    read = !backupChapter.unread && options.importReadProgress,
                    bookmark = backupChapter.bookmark,
                    dateUpload = parseDate(backupChapter.releaseTime),
                    dateFetch = currentTimeToLong(),
                    number = backupChapter.chapterNumber ?: (index + 1).toFloat(),
                    sourceOrder = (backupChapter.position ?: index).toLong(),
                    lastPageRead = if (options.importReadProgress) {
                        ((backupChapter.progress ?: 0f) * 100).toLong()
                    } else 0,
                    content = emptyList(),
                    type = 0,
                    translator = ""
                )
                chaptersToAdd.add(newChapter)
                merged++
            }
        }

        if (chaptersToUpdate.isNotEmpty()) {
            chapterRepository.insertChapters(chaptersToUpdate)
        }
        if (chaptersToAdd.isNotEmpty()) {
            chapterRepository.insertChapters(chaptersToAdd)
        }

        return merged
    }

    private fun parseGenres(genres: String?): List<String> {
        if (genres.isNullOrBlank()) return emptyList()
        return genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun mapStatus(status: String?): Long {
        return when (status?.lowercase()?.trim()) {
            "ongoing", "publishing" -> 1L
            "completed", "finished" -> 2L
            "hiatus", "on hiatus" -> 3L
            "cancelled", "canceled", "dropped" -> 4L
            else -> 0L // Unknown
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun parseDate(dateString: String?): Long {
        if (dateString.isNullOrBlank()) return 0L
        return try {
            // Try parsing ISO format using kotlinx-datetime
            kotlin.time.Instant.parse(dateString).toEpochMilliseconds()
        } catch (e: Exception) {
            try {
                // Try common ISO-like formats by normalizing the string
                val normalized = dateString.trim()
                    .replace(" ", "T")
                    .let { if (!it.endsWith("Z") && !it.contains("+")) "${it}Z" else it }
                kotlin.time.Instant.parse(normalized).toEpochMilliseconds()
            } catch (e: Exception) {
                // If all parsing fails, return 0
                0L
            }
        }
    }

    /**
     * Process cover URL from LNReader backup
     *
     * LNReader stores covers in different formats:
     * - HTTP/HTTPS URLs: Keep as-is (e.g., "https://example.com/cover.jpg")
     * - Local file paths: May be relative or absolute paths
     * - Empty/null: Return empty string
     *
     * For local covers, we can't directly use them since they're from LNReader's storage.
     * The novel will need to fetch the cover from the source when opened.
     */
    private fun processCoverUrl(cover: String?): String {
        if (cover.isNullOrBlank()) return ""

        val trimmedCover = cover.trim()

        // If it's already an HTTP/HTTPS URL, use it directly
        if (trimmedCover.startsWith("http://") || trimmedCover.startsWith("https://")) {
            return trimmedCover
        }

        // If it's a file:// URL, it's a local LNReader path - we can't use it
        // The cover will be fetched from the source when the novel is opened
        if (trimmedCover.startsWith("file://")) {
            ireader.core.log.Log.debug { "LNReader local cover path ignored: $trimmedCover" }
            return ""
        }

        // If it's an absolute path (starts with /), it's a local LNReader path
        if (trimmedCover.startsWith("/")) {
            ireader.core.log.Log.debug { "LNReader local cover path ignored: $trimmedCover" }
            return ""
        }

        // Otherwise, assume it might be a relative URL or some other format
        // Try to use it as-is, it might work
        return trimmedCover
    }
}
