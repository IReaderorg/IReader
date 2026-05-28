package ireader.domain.usecases.backup.lnreader

import ireader.core.log.Log
import ireader.domain.models.common.Uri
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

///**
// * Android import implementation with memory-efficient streaming.
// *
// * The backup ZIP file has this structure:
// * - NovelAndChapters/*.json (novel metadata)
// * - Version.json, Category.json, Setting.json (metadata)
// * - download.zip (chapter content, ~113MB) - at the END of the file
// *
// * Since download.zip is at the end, we:
// * 1. First pass: Stream through entries, buffering novel metadata (small files)
// * 2. When we hit download.zip, extract chapter content
// * 3. Import all buffered novels with the chapter content
// *
// * This avoids loading the entire backup into memory at once.
// */
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

    // ── Step 2: Stream through entries, buffering novels and extracting chapter content ──
    try {
        val result = parseZipAndImport(instance, inputStream, options) { progress ->
            emit(progress)
        }
        emit(ImportLNReaderBackup.ImportProgress.Complete(result))
    } catch (e: OutOfMemoryError) {
        Log.error(e, "LNReader import failed: Out of memory")
        throw LNReaderImportException.OutOfMemoryException(cause = e)
    } finally {
        withContext(Dispatchers.IO) { inputStream.close() }
    }

    Log.info { "LNReader: Streaming import finished" }
}

/**
 * Parse ZIP entries and import data with memory-efficient streaming.
 *
 * Strategy:
 * - Buffer novel metadata (small JSON files) in memory
 * - Skip large non-essential entries
 * - When download.zip is found, extract chapter content
 * - After all entries are processed, import buffered novels with chapter content
 */
private suspend fun parseZipAndImport(
    instance: ImportLNReaderBackup,
    inputStream: InputStream,
    options: ImportLNReaderBackup.ImportOptions,
    emitProgress: suspend (ImportLNReaderBackup.ImportProgress) -> Unit
): ImportLNReaderBackup.ImportResult {

    val bufferedStream = if (inputStream is java.io.BufferedInputStream) inputStream
        else java.io.BufferedInputStream(inputStream, 65536)

    // Buffers for first pass
    val bufferedNovels = mutableListOf<LNReaderNovel>()
    val categories = mutableListOf<LNReaderCategory>()
    val defaultCategoryNames = setOf("local", "default", "uncategorized", "all", "library")
    val novelsInDefaultCategories = mutableSetOf<Int>()
    val errors = mutableListOf<ImportLNReaderBackup.ImportError>()

    var entryCount = 0

    val json = ireader.domain.usecases.backup.lnreader.LNReaderBackupParser.json

    // Chapter content map - populated when we encounter download.zip
    val chapterContentMap = mutableMapOf<Int, String>()

    Log.info { "LNReader: Starting ZIP parsing (streaming)" }

    ZipInputStream(bufferedStream).use { zipIn ->
        var entry = zipIn.nextEntry
        while (entry != null) {
            entryCount++
            val entryName = entry.name

            when {
                entryName == "Version.json" -> {
                    // Skip - not needed for import
                }

                entryName == "Category.json" -> {
                    val content = readEntryBytes(zipIn, entry.size).decodeToString()
                    try {
                        val cats = json.decodeFromString<List<LNReaderCategory>>(content)
                        categories.addAll(cats)
                        Log.info { "LNReader: Found ${cats.size} categories" }
                    } catch (e: Exception) {
                        Log.warn(e, "Failed to parse Category.json")
                    }
                }

                entryName == "Setting.json" -> {
                    // Skip - not needed for import
                }

                // Extract download.zip for chapter content
                entryName == "download.zip" -> {
                    Log.info { "LNReader: Found download.zip (${entry.size} bytes), extracting chapter content..." }
                    try {
                        val downloadZipBytes = readEntryBytes(zipIn, entry.size)
                        Log.info { "LNReader: Read ${downloadZipBytes.size} bytes from download.zip" }
                        extractChapterContentFromDownloadZipBytes(downloadZipBytes, chapterContentMap)
                        Log.info { "LNReader: Extracted content for ${chapterContentMap.size} chapters" }
                    } catch (e: Exception) {
                        Log.warn(e, "Failed to extract download.zip content")
                    }
                }

                entryName.startsWith("NovelAndChapters/") && entryName.endsWith(".json") -> {
                    val content = readEntryBytes(zipIn, entry.size).decodeToString()
                    try {
                        val novel = json.decodeFromString<LNReaderNovel>(content)
                        bufferedNovels.add(novel)
                        Log.info { "LNReader: Buffered novel '${novel.name}' (${novel.chapters.size} chapters)" }
                    } catch (e: Exception) {
                        errors.add(ImportLNReaderBackup.ImportError("Novel", entryName, e.message ?: "Unknown"))
                        Log.warn(e, "Failed to parse novel: $entryName")
                    }
                }

                else -> {
                    // Skip non-matching entries
                    skipEntryBytes(zipIn)
                }
            }

            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
    }

    Log.info { "LNReader: ZIP parsing complete. Entries: $entryCount, Buffered novels: ${bufferedNovels.size}" }

    // ── Step 3: Import buffered novels with chapter content ──
    var novelsImported = 0
    var novelsSkipped = 0
    var novelsFailed = 0
    var chaptersImported = 0
    val novelIdMap = mutableMapOf<Int, Long>()

    for (novel in bufferedNovels) {
        try {
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
                current = processed, total = bufferedNovels.size, novelName = novel.name
            ))
        } catch (e: Exception) {
            novelsFailed++
            errors.add(ImportLNReaderBackup.ImportError("Novel", novel.name, e.message ?: "Unknown error"))
            Log.warn(e, "Failed to import novel: ${novel.name}")
        }
    }

    // ── Step 4: Import categories ──
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
                    try { instance.bookCategoryRepository.insert(ireader.domain.models.entities.BookCategory(bookId, categoryId)) } catch (_: Exception) {}
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
                val newCat = ireader.domain.models.entities.Category(id = 0, name = "LNReader",
                    order = (allCategories.maxOfOrNull { it.order } ?: 0) + 1, flags = 0)
                instance.categoryRepository.insert(newCat)
                categoriesImported++
                lnReaderCategoryId = instance.categoryRepository.getAll().find { it.name.equals("LNReader", ignoreCase = true) }?.id
            }
            if (lnReaderCategoryId != null) {
                for (lnNovelId in novelsInDefaultCategories) {
                    val bookId = novelIdMap[lnNovelId] ?: continue
                    try { instance.bookCategoryRepository.insert(ireader.domain.models.entities.BookCategory(bookId, lnReaderCategoryId)) } catch (_: Exception) {}
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
 * Read bytes from the current ZIP entry.
 * Uses entry.size if available, otherwise reads until end of entry.
 */
private fun readEntryBytes(zipIn: ZipInputStream, entrySize: Long): ByteArray {
    return if (entrySize > 0 && entrySize < Int.MAX_VALUE) {
        val size = entrySize.toInt()
        val bytes = ByteArray(size)
        var offset = 0
        while (offset < size) {
            val read = zipIn.read(bytes, offset, size - offset)
            if (read == -1) break
            offset += read
        }
        if (offset < size) bytes.copyOf(offset) else bytes
    } else {
        val buffer = java.io.ByteArrayOutputStream(8192)
        val tempBuffer = ByteArray(8192)
        var bytesRead: Int
        while (zipIn.read(tempBuffer).also { bytesRead = it } != -1) {
            buffer.write(tempBuffer, 0, bytesRead)
        }
        buffer.toByteArray()
    }
}

/**
 * Skip bytes in the current ZIP entry.
 */
private fun skipEntryBytes(zipIn: ZipInputStream) {
    val skipBuffer = ByteArray(8192)
    while (zipIn.read(skipBuffer) != -1) { /* skip */ }
}

/**
 * Extract chapter content from download.zip bytes.
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

                if (entryName.startsWith("Novels/") && entryName.endsWith("/index.html")) {
                    try {
                        val parts = entryName.removePrefix("Novels/")
                            .removeSuffix("/index.html")
                            .split("/")

                        if (parts.size == 3) {
                            val chapterId = parts[2].toIntOrNull()
                            if (chapterId != null) {
                                val htmlContent = readEntryBytes(downloadZipIn, downloadEntry.size).decodeToString()
                                chapterContentMap[chapterId] = htmlContent
                            }
                        }
                    } catch (e: Exception) {
                        Log.warn(e, "Failed to extract chapter content from: $entryName")
                    }
                } else {
                    skipEntryBytes(downloadZipIn)
                }

                downloadZipIn.closeEntry()
                downloadEntry = downloadZipIn.nextEntry
            }
        }
    } catch (e: Exception) {
        Log.warn(e, "Failed to parse download.zip", e)
    }
}
