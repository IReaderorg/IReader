package ireader.domain.usecases.backup.lnreader

import com.fleeksoft.io.exception.OutOfMemoryError
import ireader.core.log.Log
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
import kotlinx.datetime.Instant
import okio.buffer
import kotlin.time.ExperimentalTime

///**
// * Use case for importing LNReader backup files into IReader.
// *
// * ## Import Flow:
// * ```
// * User selects .zip file
// *       ↓
// * File picker returns URI
// *       ↓
// * invoke(uri, options) called
// *       ↓
// * ┌─────────────────────────────────────┐
// * │ Step 1: Open backup file as stream  │
// * │ Step 2: Parse ZIP entries:          │
// * │   - Version.json (skip)             │
// * │   - Category.json (collect cats)    │
// * │   - Setting.json (skip)             │
// * │   - NovelAndChapters/*.json (import)│
// * │ Step 3: For each novel:             │
// * │   - Map plugin ID → source ID       │
// * │   - Check if book exists            │
// * │   - Insert/update book              │
// * │   - Insert chapters                 │
// * │ Step 4: Import categories           │
// * │ Step 5: Associate novels with cats  │
// * └─────────────────────────────────────┘
// *       ↓
// * Emit Complete(result)
// * ```
// *
// * ## Platform-specific streaming:
// * - Android: Uses streaming ZIP parsing (entry-by-entry, low memory)
// * - Other: Falls back to loading entire file into memory
// */
class ImportLNReaderBackup(
    private val parser: LNReaderBackupParser,
    internal val sourceMapper: LNReaderSourceMapper,
    internal val bookRepository: BookRepository,
    internal val chapterRepository: ChapterRepository,
    internal val categoryRepository: CategoryRepository,
    internal val bookCategoryRepository: BookCategoryRepository,
    private val transactions: ireader.core.db.Transactions,
    internal val fileSaver: FileSaver
) {

    data class ImportOptions(
        val importLibrary: Boolean = true,
        val importCategories: Boolean = true,
        val importSettings: Boolean = false,
        val importReadProgress: Boolean = true,
        val conflictStrategy: ConflictStrategy = ConflictStrategy.MERGE
    )

    enum class ConflictStrategy { SKIP, MERGE, OVERWRITE }

    sealed class ImportProgress {
        object Starting : ImportProgress()
        data class Parsing(val message: String) : ImportProgress()
        data class ImportingNovels(val current: Int, val total: Int, val novelName: String) : ImportProgress()
        data class ImportingCategories(val current: Int, val total: Int) : ImportProgress()
        data class Complete(val result: ImportResult) : ImportProgress()
        data class Error(val error: Throwable) : ImportProgress()
    }

    data class ImportResult(
        val novelsImported: Int,
        val novelsSkipped: Int,
        val novelsFailed: Int,
        val chaptersImported: Int,
        val categoriesImported: Int,
        val errors: List<ImportError>
    )

    data class ImportError(val itemType: String, val itemName: String, val error: String)

    // ─────────────────────────────────────────────────────────
    // PUBLIC ENTRY POINT
    // ─────────────────────────────────────────────────────────

    /**
     * Execute the import from a URI.
     * Emits progress updates as the import proceeds.
     */
    suspend fun invoke(
        uri: Uri,
        options: ImportOptions = ImportOptions()
    ): Flow<ImportProgress> = flow {
        emit(ImportProgress.Starting)
        try {
            emit(ImportProgress.Parsing("Opening backup file..."))
            // Platform-specific streaming import (Android overrides this)
            importFromUri(uri, options).collect { emit(it) }
        } catch (e: LNReaderImportException) {
            Log.error(e, "LNReader import failed: ${e.message}")
            emit(ImportProgress.Error(e))
        } catch (e: OutOfMemoryError) {
            Log.error(e, "LNReader import failed: Out of memory")
            emit(ImportProgress.Error(LNReaderImportException.OutOfMemoryException(cause = e)))
        } catch (e: Exception) {
            Log.error(e, "LNReader import failed")
            emit(ImportProgress.Error(LNReaderImportException.fromException(e)))
        }
    }

    // ─────────────────────────────────────────────────────────
    // PLATFORM-SPECIFIC: Override this in platform source sets
    // ─────────────────────────────────────────────────────────

    /**
     * Platform-specific implementation for importing from a URI.
     * Android provides an actual implementation; other platforms use byte-array fallback.
     */
    internal suspend fun importFromUri(
        uri: Uri,
        options: ImportOptions
    ): Flow<ImportProgress> {
        // Check if a platform-specific streaming importer was injected
        val importer = streamingImporter
        if (importer != null) {
            return importer.import(uri, options)
        }
        // Fallback: load entire file into memory
        return importFromUriFallback(uri, options)
    }

    /**
     * Injected by platform-specific DI (Android) to enable streaming.
     * Null on platforms without streaming support.
     */
    internal var streamingImporter: LNReaderStreamingImporter? = null


    /**
     * Fallback implementation: loads entire file into memory.
     * Used for non-Android platforms.
     */
    private fun importFromUriFallback(
        uri: Uri,
        options: ImportOptions
    ): Flow<ImportProgress> = flow {
        emit(ImportProgress.Parsing("Reading backup file..."))
        val bytes = try {
            val source = fileSaver.readSource(uri).buffer()
            try { source.readByteArray() } finally { source.close() }
        } catch (ex: Exception) {
            throw LNReaderImportException.ReadFailedException(ex.message ?: "Unable to read file", ex)
        }

        if (!LNReaderBackupParser.isLNReaderBackup(bytes)) {
            throw LNReaderImportException.InvalidBackupException()
        }

        emit(ImportProgress.Parsing("Parsing backup data..."))
        val backup = try {
            parser.parseBackup(bytes)
        } catch (ex: Exception) {
            throw LNReaderImportException.ParseFailedException(ex.message ?: "Unable to parse backup", ex)
        }

        if (backup.novels.isEmpty() && backup.categories.isEmpty()) {
            throw LNReaderImportException.EmptyBackupException()
        }

        // Extract chapter content from download.zip if present
        val chapterContentMap = extractChapterContent(bytes)
        if (chapterContentMap.isNotEmpty()) {
            emit(ImportProgress.Parsing("Extracted content for ${chapterContentMap.size} chapters from download.zip"))
        }

        val result = importBackupData(backup, options, chapterContentMap)
        emit(ImportProgress.Complete(result))
    }

    // ─────────────────────────────────────────────────────────
    // CORE IMPORT LOGIC (shared across all platforms)
    // ─────────────────────────────────────────────────────────

    /**
     * Import parsed backup data into the database.
     * This is the shared logic used by both streaming and byte-array approaches.
     *
     * @param backup The parsed backup data
     * @param options Import options
     * @param chapterContentMap Map of chapter ID to HTML content from download.zip
     */
    internal suspend fun importBackupData(
        backup: ireader.domain.models.lnreader.LNReaderBackup,
        options: ImportOptions,
        chapterContentMap: Map<Int, String> = emptyMap()
    ): ImportResult {
        val errors = mutableListOf<ImportError>()
        var novelsImported = 0
        var novelsSkipped = 0
        var novelsFailed = 0
        var chaptersImported = 0
        val novelIdMap = mutableMapOf<Int, Long>()

        // Step 1: Import all novels
        for (novel in backup.novels) {
            try {
                when (val result = importNovel(novel, options, chapterContentMap)) {
                    is NovelImportResult.Imported -> {
                        novelsImported++
                        chaptersImported += result.chaptersImported
                        novelIdMap[novel.id] = result.bookId
                    }
                    is NovelImportResult.Skipped -> {
                        novelsSkipped++
                        val sourceId = sourceMapper.mapPluginId(novel.pluginId)
                            ?: sourceMapper.getUnmappedSourceId()
                        val existing = bookRepository.find(novel.path, sourceId)
                        if (existing != null) novelIdMap[novel.id] = existing.id
                    }
                    is NovelImportResult.Failed -> {
                        novelsFailed++
                        errors.add(ImportError("Novel", novel.name, result.error))
                    }
                }
            } catch (e: Exception) {
                novelsFailed++
                errors.add(ImportError("Novel", novel.name, e.message ?: "Unknown error"))
                Log.warn(e, "Failed to import novel: ${novel.name}")
            }
        }

        // Step 2: Import categories
        var categoriesImported = 0
        if (options.importCategories && backup.categories.isNotEmpty()) {
            categoriesImported = importCategories(backup.categories, novelIdMap)
        }

        return ImportResult(
            novelsImported = novelsImported,
            novelsSkipped = novelsSkipped,
            novelsFailed = novelsFailed,
            chaptersImported = chaptersImported,
            categoriesImported = categoriesImported,
            errors = errors
        )
    }

    // ─────────────────────────────────────────────────────────
    // NOVEL IMPORT
    // ─────────────────────────────────────────────────────────

    internal sealed class NovelImportResult {
        data class Imported(val bookId: Long, val chaptersImported: Int) : NovelImportResult()
        object Skipped : NovelImportResult()
        data class Failed(val error: String) : NovelImportResult()
    }

    internal suspend fun importNovel(
        novel: LNReaderNovel,
        options: ImportOptions,
        chapterContentMap: Map<Int, String> = emptyMap()
    ): NovelImportResult {
        val sourceId = sourceMapper.mapPluginId(novel.pluginId)
            ?: sourceMapper.getUnmappedSourceId()

        val existing = bookRepository.find(novel.path, sourceId)

        if (existing != null) {
            return when (options.conflictStrategy) {
                ConflictStrategy.SKIP -> NovelImportResult.Skipped
                ConflictStrategy.MERGE -> {
                    val chaptersImported = mergeChapters(existing.id, novel.chapters, options, chapterContentMap)
                    NovelImportResult.Imported(existing.id, chaptersImported)
                }
                ConflictStrategy.OVERWRITE -> {
                    chapterRepository.deleteChaptersByBookId(existing.id)
                    val bookId = upsertBook(existing.id, novel, sourceId)
                    val chaptersImported = importChapters(bookId, novel.chapters, options, chapterContentMap)
                    NovelImportResult.Imported(bookId, chaptersImported)
                }
            }
        }

        val bookId = upsertBook(0, novel, sourceId)
        val chaptersImported = importChapters(bookId, novel.chapters, options, chapterContentMap)
        return NovelImportResult.Imported(bookId, chaptersImported)
    }

    private suspend fun upsertBook(bookId: Long, novel: LNReaderNovel, sourceId: Long): Long {
        val book = Book(
            id = bookId,
            sourceId = sourceId,
            key = novel.path,
            title = novel.name,
            author = novel.author ?: "",
            description = novel.summary ?: "",
            genres = parseGenres(novel.genres),
            status = mapStatus(novel.status),
            cover = processCoverUrl(novel.cover),
            favorite = novel.inLibrary,
            initialized = true,
            dateAdded = currentTimeToLong()
        )
        return bookRepository.upsert(book)
    }

    // ─────────────────────────────────────────────────────────
    // CHAPTER IMPORT
    // ─────────────────────────────────────────────────────────

    private suspend fun importChapters(
        bookId: Long,
        chapters: List<LNReaderChapter>,
        options: ImportOptions,
        chapterContentMap: Map<Int, String> = emptyMap()
    ): Int {
        if (chapters.isEmpty()) return 0
        val newChapters = chapters.mapIndexed { index, chapter ->
            // Look up chapter content from download.zip by chapter ID
            val content = chapterContentMap[chapter.id]?.let { htmlContent ->
                htmlToPages(htmlContent)
            } ?: emptyList()

            Chapter(
                id = 0, bookId = bookId, key = chapter.path, name = chapter.name,
                read = !chapter.unread && options.importReadProgress,
                bookmark = chapter.bookmark,
                dateUpload = parseDate(chapter.releaseTime),
                dateFetch = currentTimeToLong(),
                number = chapter.chapterNumber ?: (index + 1).toFloat(),
                sourceOrder = (chapter.position ?: index).toLong(),
                lastPageRead = if (options.importReadProgress) ((chapter.progress ?: 0f) * 100).toLong() else 0,
                content = content, type = 0, translator = ""
            )
        }
        chapterRepository.insertChapters(newChapters)
        return newChapters.size
    }

    private suspend fun mergeChapters(
        bookId: Long,
        backupChapters: List<LNReaderChapter>,
        options: ImportOptions,
        chapterContentMap: Map<Int, String> = emptyMap()
    ): Int {
        val existingChapters = chapterRepository.findChaptersByBookId(bookId)
        val existingMap = existingChapters.associateBy { it.key }
        var merged = 0
        val chaptersToUpdate = mutableListOf<Chapter>()
        val chaptersToAdd = mutableListOf<Chapter>()

        for ((index, backupChapter) in backupChapters.withIndex()) {
            val existing = existingMap[backupChapter.path]
            if (existing != null && options.importReadProgress) {
                val shouldUpdate = (!backupChapter.unread && !existing.read) ||
                    (backupChapter.bookmark && !existing.bookmark) ||
                    ((backupChapter.progress ?: 0f) * 100).toLong() > existing.lastPageRead
                if (shouldUpdate) {
                    // Also update content if available in download.zip
                    val newContent = chapterContentMap[backupChapter.id]?.let { htmlContent ->
                        htmlToPages(htmlContent)
                    }

                    chaptersToUpdate.add(existing.copy(
                        read = existing.read || !backupChapter.unread,
                        bookmark = existing.bookmark || backupChapter.bookmark,
                        lastPageRead = maxOf(existing.lastPageRead, ((backupChapter.progress ?: 0f) * 100).toLong()),
                        content = newContent ?: existing.content
                    ))
                    merged++
                }
            } else if (existing == null) {
                // Look up chapter content from download.zip by chapter ID
                val content = chapterContentMap[backupChapter.id]?.let { htmlContent ->
                    htmlToPages(htmlContent)
                } ?: emptyList()

                chaptersToAdd.add(Chapter(
                    id = 0, bookId = bookId, key = backupChapter.path, name = backupChapter.name,
                    read = !backupChapter.unread && options.importReadProgress,
                    bookmark = backupChapter.bookmark,
                    dateUpload = parseDate(backupChapter.releaseTime),
                    dateFetch = currentTimeToLong(),
                    number = backupChapter.chapterNumber ?: (index + 1).toFloat(),
                    sourceOrder = (backupChapter.position ?: index).toLong(),
                    lastPageRead = if (options.importReadProgress) ((backupChapter.progress ?: 0f) * 100).toLong() else 0,
                    content = content, type = 0, translator = ""
                ))
                merged++
            }
        }
        if (chaptersToUpdate.isNotEmpty()) chapterRepository.insertChapters(chaptersToUpdate)
        if (chaptersToAdd.isNotEmpty()) chapterRepository.insertChapters(chaptersToAdd)
        return merged
    }

    // ─────────────────────────────────────────────────────────
    // CATEGORY IMPORT
    // ─────────────────────────────────────────────────────────

    internal suspend fun importCategory(category: LNReaderCategory): Long {
        val existing = categoryRepository.getAll().find { it.name.equals(category.name, ignoreCase = true) }
        if (existing != null) return existing.id
        val newCategory = Category(id = 0, name = category.name, order = category.sort.toLong(), flags = 0)
        categoryRepository.insert(newCategory)
        return categoryRepository.getAll().find { it.name.equals(category.name, ignoreCase = true) }?.id ?: 0L
    }

    private suspend fun importCategories(categories: List<LNReaderCategory>, novelIdMap: Map<Int, Long>): Int {
        var count = 0
        for (category in categories) {
            try {
                val categoryId = importCategory(category)
                count++
                for (lnNovelId in category.novelIds) {
                    val bookId = novelIdMap[lnNovelId] ?: continue
                    try { bookCategoryRepository.insert(BookCategory(bookId, categoryId)) } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                Log.warn(e, "Failed to import category: ${category.name}")
            }
        }
        return count
    }

    // ─────────────────────────────────────────────────────────
    // UTILITY FUNCTIONS
    // ─────────────────────────────────────────────────────────

    private fun parseGenres(genres: String?): List<String> {
        if (genres.isNullOrBlank()) return emptyList()
        return genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun mapStatus(status: String?): Long = when (status?.lowercase()?.trim()) {
        "ongoing", "publishing" -> 1L
        "completed", "finished" -> 2L
        "hiatus", "on hiatus" -> 3L
        "cancelled", "canceled", "dropped" -> 4L
        else -> 0L
    }

    @OptIn(ExperimentalTime::class)
    private fun parseDate(dateString: String?): Long {
        if (dateString.isNullOrBlank()) return 0L
        return try {
            Instant.parse(dateString).toEpochMilliseconds()
        } catch (_: Exception) {
            try {
                val normalized = dateString.trim().replace(" ", "T")
                    .let { if (!it.endsWith("Z") && !it.contains("+")) "${it}Z" else it }
                Instant.parse(normalized).toEpochMilliseconds()
            } catch (_: Exception) { 0L }
        }
    }

    private fun processCoverUrl(cover: String?): String {
        if (cover.isNullOrBlank()) return ""
        val trimmed = cover.trim()
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
        if (trimmed.startsWith("file://") || trimmed.startsWith("/")) return ""
        return trimmed
    }

    // ─────────────────────────────────────────────────────────
    // CHAPTER CONTENT EXTRACTION FROM download.zip
    // ─────────────────────────────────────────────────────────

    /**
     * Extract chapter content from download.zip contained in the backup.
     * This is a platform-specific function - Android provides the actual implementation.
     *
     * @param backupBytes The main backup ZIP file bytes
     * @return Map of chapter ID to HTML content string
     */
    internal fun extractChapterContent(backupBytes: ByteArray): Map<Int, String> {
        return extractChapterContentPlatform(backupBytes)
    }

    /**
     * Convert HTML content to a list of Page objects.
     * Extracts text from paragraph tags and creates Text pages.
     */
    private fun htmlToPages(html: String): List<ireader.core.source.model.Page> {
        val pages = mutableListOf<ireader.core.source.model.Page>()

        // Simple HTML to text extraction
        // Look for content between <p> tags or other text-containing elements
        val paragraphRegex = Regex("<p[^>]*>(.*?)</p>", RegexOption.DOT_MATCHES_ALL)
        val matches = paragraphRegex.findAll(html)

        for (match in matches) {
            var text = match.groupValues[1]
                .replace(Regex("<[^>]+>"), "") // Remove inner HTML tags
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .trim()

            // Decode HTML entities (basic)
            text = text.replace(Regex("&#(\\d+);")) { result ->
                val code = result.groupValues[1].toIntOrNull()
                if (code != null) code.toChar().toString() else result.value
            }

            if (text.isNotBlank()) {
                pages.add(ireader.core.source.model.Text(text))
            }
        }

        // If no paragraphs found, try to extract text from the body
        if (pages.isEmpty()) {
            val bodyRegex = Regex("<body[^>]*>(.*?)</body>", RegexOption.DOT_MATCHES_ALL)
            val bodyMatch = bodyRegex.find(html)
            if (bodyMatch != null) {
                var text = bodyMatch.groupValues[1]
                    .replace(Regex("<[^>]+>"), " ")
                    .replace(Regex("\\s+"), " ")
                    .trim()

                if (text.isNotBlank()) {
                    pages.add(ireader.core.source.model.Text(text))
                }
            }
        }

        return pages
    }
}

/**
 * Interface for platform-specific streaming importers.
 * Android provides an implementation via ImportLNReaderBackupStreaming.kt.
 * Registered in DI and injected into ImportLNReaderBackup instances.
 */
fun interface LNReaderStreamingImporter {
    fun import(uri: ireader.domain.models.common.Uri, options: ImportLNReaderBackup.ImportOptions): Flow<ImportLNReaderBackup.ImportProgress>
}
