package ireader.domain.usecases.backup.lnreader

import android.content.Context
import ireader.core.log.Log
import ireader.domain.models.common.Uri
import ireader.domain.models.entities.BookCategory
import ireader.domain.models.entities.Category
import ireader.domain.models.lnreader.LNReaderCategory
import ireader.domain.models.lnreader.LNReaderNovel
import ireader.domain.usecases.file.AndroidFileSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

///**
// * Android-specific streaming implementation for LNReader backup import.
// *
// * ## Flow:
// * ```
// * Open URI as InputStream (IO dispatcher)
// *       ↓
// * Wrap in BufferedInputStream + ZipInputStream
// *       ↓
// * For each ZIP entry:
// *   - Version.json → skip
// *   - Category.json → collect categories
// *   - Setting.json → skip
// *   - NovelAndChapters/*.json → parse + import novel immediately
// *   - Other entries → skip efficiently
// *       ↓
// * Import categories and associate with novels
// *       ↓
// * Emit Complete(result)
// * ```
// */


/**
 * Create the Android streaming importer.
 * Registered in the Android DI module as LNReaderStreamingImporter.
 */
fun ImportLNReaderBackup.createAndroidStreamingImporter(): LNReaderStreamingImporter {
    val instance = this
    return LNReaderStreamingImporter { uri, options ->
        importFromUriStreaming(instance, uri, options)
    }
}

/**
 * Streaming import implementation.
 */
private fun importFromUriStreaming(
    instance: ImportLNReaderBackup,
    uri: Uri,
    options: ImportLNReaderBackup.ImportOptions
): Flow<ImportLNReaderBackup.ImportProgress> = flow {

    Log.info { "LNReader: Starting streaming import" }
    emit(ImportLNReaderBackup.ImportProgress.Parsing("Opening backup file..."))

    // ── Step 1: Open input stream ──
    val context = (instance.fileSaver as? AndroidFileSaver)?.context
        ?: throw LNReaderImportException.UnknownException("Android context not available")

    val inputStream = try {
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(android.net.Uri.parse(uri.toString()))
                ?: throw LNReaderImportException.ReadFailedException("Cannot open file")
        }
    } catch (e: Exception) {
        throw LNReaderImportException.ReadFailedException(e.message ?: "Unable to open file", e)
    }

    // ── Step 2: Parse ZIP entries and import ──
    try {
        val result = parseZipAndImport(instance, inputStream, options) { progress ->
            emit(progress)
        }
        emit(ImportLNReaderBackup.ImportProgress.Complete(result))
    } finally {
        withContext(Dispatchers.IO) { inputStream.close() }
    }

    Log.info { "LNReader: Streaming import finished" }
}

/**
 * Parse ZIP entries and import data.
 */
private suspend fun parseZipAndImport(
    instance: ImportLNReaderBackup,
    inputStream: InputStream,
    options: ImportLNReaderBackup.ImportOptions,
    emitProgress: suspend (ImportLNReaderBackup.ImportProgress) -> Unit
): ImportLNReaderBackup.ImportResult {

    val bufferedStream = if (inputStream is java.io.BufferedInputStream) inputStream
        else java.io.BufferedInputStream(inputStream, 65536)

    val categories = mutableListOf<LNReaderCategory>()
    val novelIdMap = mutableMapOf<Int, Long>()
    val defaultCategoryNames = setOf("local", "default", "uncategorized", "all", "library")
    val novelsInDefaultCategories = mutableSetOf<Int>()
    val errors = mutableListOf<ImportLNReaderBackup.ImportError>()

    var novelsImported = 0
    var novelsSkipped = 0
    var novelsFailed = 0
    var chaptersImported = 0
    var entryCount = 0

    val json = ireader.domain.usecases.backup.lnreader.LNReaderBackupParser.json

    // Chapter content map extracted from download.zip
    val chapterContentMap = mutableMapOf<Int, String>()

    Log.info { "LNReader: Starting ZIP parsing" }

    java.util.zip.ZipInputStream(bufferedStream).use { zipIn ->
        var entry = zipIn.nextEntry
        while (entry != null) {
            entryCount++
            val entryName = entry.name
            Log.info { "LNReader: Entry #$entryCount: $entryName (${entry.size} bytes)" }

            when {
                entryName == "Version.json" -> { /* skip */ }

                entryName == "Category.json" -> {
                    val content = zipIn.readBytes().decodeToString()
                    try {
                        val cats = json.decodeFromString<List<LNReaderCategory>>(content)
                        categories.addAll(cats)
                        Log.info { "LNReader: Found ${cats.size} categories" }
                    } catch (e: Exception) {
                        Log.warn(e, "Failed to parse Category.json")
                    }
                }

                entryName == "Setting.json" -> { /* skip */ }

                // Extract download.zip for chapter content
                entryName == "download.zip" -> {
                    Log.info { "LNReader: Found download.zip, extracting chapter content..." }
                    try {
                        val downloadZipBytes = zipIn.readBytes()
                        extractChapterContentFromDownloadZipBytes(downloadZipBytes, chapterContentMap)
                        Log.info { "LNReader: Extracted content for ${chapterContentMap.size} chapters from download.zip" }
                    } catch (e: Exception) {
                        Log.warn(e, "Failed to extract download.zip content")
                    }
                }

                entryName.startsWith("NovelAndChapters/") && entryName.endsWith(".json") -> {
                    val content = zipIn.readBytes().decodeToString()
                    try {
                        val novel = json.decodeFromString<LNReaderNovel>(content)
                        Log.info { "LNReader: Importing novel '${novel.name}' (${novel.chapters.size} chapters)" }

                        when (val result = instance.importNovel(novel, options, chapterContentMap)) {
                            is ImportLNReaderBackup.NovelImportResult.Imported -> {
                                novelsImported++
                                chaptersImported += result.chaptersImported
                                novelIdMap[novel.id] = result.bookId
                            }
                            is ImportLNReaderBackup.NovelImportResult.Skipped -> {
                                novelsSkipped++
                                val sourceId = instance.sourceMapper.mapPluginId(novel.pluginId)
                                    ?: instance.sourceMapper.getUnmappedSourceId()
                                val existing = instance.bookRepository.find(novel.path, sourceId)
                                if (existing != null) novelIdMap[novel.id] = existing.id
                            }
                            is ImportLNReaderBackup.NovelImportResult.Failed -> {
                                novelsFailed++
                                errors.add(ImportLNReaderBackup.ImportError("Novel", novel.name, result.error))
                            }
                        }

                        val processed = novelsImported + novelsSkipped + novelsFailed
                        emitProgress(ImportLNReaderBackup.ImportProgress.ImportingNovels(
                            current = processed, total = processed, novelName = novel.name
                        ))
                    } catch (e: Exception) {
                        novelsFailed++
                        errors.add(ImportLNReaderBackup.ImportError("Novel", entryName, e.message ?: "Unknown"))
                        Log.warn(e, "Failed to import novel: $entryName")
                    }
                }

                else -> {
                    // Skip non-matching entries efficiently (important for large files like download.zip)
                    if (entry.size > 1_000_000) {
                        val skipBuffer = ByteArray(8192)
                        while (zipIn.read(skipBuffer) != -1) { /* skip */ }
                    }
                }
            }

            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
    }

    Log.info { "LNReader: ZIP parsing complete. Entries: $entryCount, Novels: $novelsImported" }

    // ── Import categories ──
    var categoriesImported = 0
    if (options.importCategories && categories.isNotEmpty()) {
        val allCategories = instance.categoryRepository.getAll()

        val categoriesToImport = categories.filter { category ->
            val isDefault = defaultCategoryNames.any { category.name.equals(it, ignoreCase = true) }
            if (isDefault) novelsInDefaultCategories.addAll(category.novelIds)
            !isDefault
        }

        categoriesToImport.forEachIndexed { index, category ->
            emitProgress(ImportLNReaderBackup.ImportProgress.ImportingCategories(index + 1, categoriesToImport.size))
            try {
                val categoryId = instance.importCategory(category)
                categoriesImported++
                for (lnNovelId in category.novelIds) {
                    val bookId = novelIdMap[lnNovelId] ?: continue
                    try { instance.bookCategoryRepository.insert(BookCategory(bookId, categoryId)) } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                errors.add(ImportLNReaderBackup.ImportError("Category", category.name, e.message ?: "Unknown"))
                Log.warn(e, "Failed to import category: ${category.name}")
            }
        }

        // Create "LNReader" category for novels in default categories
        if (novelsInDefaultCategories.isNotEmpty()) {
            var lnReaderCategoryId = allCategories.find { it.name.equals("LNReader", ignoreCase = true) }?.id
            if (lnReaderCategoryId == null) {
                val newCat = Category(id = 0, name = "LNReader",
                    order = (allCategories.maxOfOrNull { it.order } ?: 0) + 1, flags = 0)
                instance.categoryRepository.insert(newCat)
                categoriesImported++
                lnReaderCategoryId = instance.categoryRepository.getAll().find { it.name.equals("LNReader", ignoreCase = true) }?.id
            }
            if (lnReaderCategoryId != null) {
                for (lnNovelId in novelsInDefaultCategories) {
                    val bookId = novelIdMap[lnNovelId] ?: continue
                    try { instance.bookCategoryRepository.insert(BookCategory(bookId, lnReaderCategoryId)) } catch (_: Exception) {}
                }
            }
        }
    }

    return ImportLNReaderBackup.ImportResult(
        novelsImported = novelsImported,
        novelsSkipped = novelsSkipped,
        novelsFailed = novelsFailed,
        chaptersImported = chaptersImported,
        categoriesImported = categoriesImported,
        errors = errors
    )
}

/**
 * Extract chapter content from download.zip bytes.
 * Populates the chapterContentMap with chapter ID to HTML content mapping.
 */
private fun extractChapterContentFromDownloadZipBytes(
    downloadZipBytes: ByteArray,
    chapterContentMap: MutableMap<Int, String>
) {
    try {
        ZipInputStream(ByteArrayInputStream(downloadZipBytes)).use { downloadZipIn ->
            var downloadEntry = downloadZipIn.nextEntry
            while (downloadEntry != null) {
                val entryName = downloadEntry.name

                // Look for index.html files in Novels/{source}/{novelId}/{chapterId}/index.html
                if (entryName.startsWith("Novels/") && entryName.endsWith("/index.html")) {
                    try {
                        // Extract chapter ID from path
                        val parts = entryName.removePrefix("Novels/")
                            .removeSuffix("/index.html")
                            .split("/")

                        if (parts.size == 3) {
                            val chapterId = parts[2].toIntOrNull()
                            if (chapterId != null) {
                                val htmlContent = downloadZipIn.readBytes().decodeToString()
                                chapterContentMap[chapterId] = htmlContent
                            }
                        }
                    } catch (e: Exception) {
                        Log.warn(e, "Failed to extract chapter content from: $entryName")
                    }
                } else {
                    // Skip non-matching entries
                    val skipBuffer = ByteArray(8192)
                    while (downloadZipIn.read(skipBuffer) != -1) { /* skip */ }
                }

                downloadZipIn.closeEntry()
                downloadEntry = downloadZipIn.nextEntry
            }
        }
    } catch (e: Exception) {
        Log.warn(e, "Failed to parse download.zip", e)
    }
}
