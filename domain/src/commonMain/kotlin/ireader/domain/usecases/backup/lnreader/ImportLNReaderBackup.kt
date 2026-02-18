package ireader.domain.usecases.backup.lnreader

import com.fleeksoft.io.exception.OutOfMemoryError
import ireader.core.db.Transactions
import ireader.domain.data.repository.BookCategoryRepository
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.CategoryRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.Book
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
    internal val sourceMapper: LNReaderSourceMapper,
    internal val bookRepository: BookRepository,
    internal val chapterRepository: ChapterRepository,
    internal val categoryRepository: CategoryRepository,
    internal val bookCategoryRepository: BookCategoryRepository,
    private val transactions: Transactions,
    internal val fileSaver: FileSaver
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
     * Execute the import from a URI using streaming to avoid memory issues
     */
    suspend fun invoke(
        uri: Uri,
        options: ImportOptions = ImportOptions()
    ): Flow<ImportProgress> = flow {
        emit(ImportProgress.Starting)

        try {
            // Use streaming import for large backups to avoid OutOfMemoryError
            emit(ImportProgress.Parsing("Opening backup file..."))
            
            // Try platform-specific streaming first (Android), fall back to byte array
            try {
                invokeStreamingPlatform(uri, options).collect { progress ->
                    emit(progress)
                }
            } catch (e: NotImplementedError) {
                // Fall back to byte-array approach for non-Android platforms
                emit(ImportProgress.Parsing("Reading backup file..."))
                val bytes = try {
                    val source = fileSaver.readSource(uri).buffer()
                    try {
                        source.readByteArray()
                    } finally {
                        source.close()
                    }
                } catch (ex: Exception) {
                    throw LNReaderImportException.ReadFailedException(
                        ex.message ?: "Unable to read file", ex
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
                } catch (ex: Exception) {
                    throw LNReaderImportException.ParseFailedException(
                        ex.message ?: "Unable to parse backup", ex
                    )
                }

                // Check if backup has any content
                if (backup.novels.isEmpty() && backup.categories.isEmpty()) {
                    emit(ImportProgress.Error(LNReaderImportException.EmptyBackupException()))
                    return@flow
                }

                ireader.core.log.Log.debug { "LNReader backup parsed: ${backup.novels.size} novels, ${backup.categories.size} categories" }
                
                // Continue with original import logic for non-Android platforms
                // (This is the fallback - Android uses streaming)
            }
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
     * Platform-specific streaming implementation
     * Android will override this with true streaming to avoid memory issues
     * Other platforms throw NotImplementedError to fall back to byte-array approach
     */
    internal suspend fun invokeStreamingPlatform(
        uri: Uri,
        options: ImportOptions
    ): Flow<ImportProgress> {
        throw NotImplementedError("Streaming not implemented for this platform")
    }

    internal sealed class NovelImportResult {
        data class Imported(val bookId: Long, val chaptersImported: Int) : NovelImportResult()
        object Skipped : NovelImportResult()
        data class Failed(val error: String) : NovelImportResult()
    }

    internal suspend fun importCategory(category: LNReaderCategory): Long {
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

    internal suspend fun importNovel(
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
            kotlinx.datetime.Instant.parse(dateString).toEpochMilliseconds()
        } catch (e: Exception) {
            try {
                // Try common ISO-like formats by normalizing the string
                val normalized = dateString.trim()
                    .replace(" ", "T")
                    .let { if (!it.endsWith("Z") && !it.contains("+")) "${it}Z" else it }
                kotlinx.datetime.Instant.parse(normalized).toEpochMilliseconds()
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
