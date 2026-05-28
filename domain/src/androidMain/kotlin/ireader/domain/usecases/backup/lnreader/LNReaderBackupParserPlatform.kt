package ireader.domain.usecases.backup.lnreader

import ireader.domain.models.lnreader.LNReaderBackup
import ireader.domain.models.lnreader.LNReaderCategory
import ireader.domain.models.lnreader.LNReaderNovel
import ireader.domain.models.lnreader.LNReaderVersion
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

/**
 * Android implementation of LNReader backup parsing using java.util.zip
 * 
 * MEMORY OPTIMIZATION: This loads entire backup into memory.
 * For large backups (>500MB), use parseBackupStreamingPlatform instead.
 */
actual suspend fun parseBackupPlatform(bytes: ByteArray): LNReaderBackup {
    var version = LNReaderVersion("unknown")
    val novels = mutableListOf<LNReaderNovel>()
    var categories = emptyList<LNReaderCategory>()
    var settings = emptyMap<String, String>()
    
    ZipInputStream(ByteArrayInputStream(bytes)).use { zipIn ->
        var entry = zipIn.nextEntry
        while (entry != null) {
            val entryName = entry.name
            
            when {
                entryName == "Version.json" -> {
                    val content = zipIn.readBytes().decodeToString()
                    version = try {
                        json.decodeFromString<LNReaderVersion>(content)
                    } catch (e: Exception) {
                        LNReaderVersion("unknown")
                    }
                }
                entryName == "Category.json" -> {
                    val content = zipIn.readBytes().decodeToString()
                    categories = try {
                        json.decodeFromString<List<LNReaderCategory>>(content)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
                entryName == "Setting.json" -> {
                    val content = zipIn.readBytes().decodeToString()
                    settings = try {
                        json.decodeFromString<Map<String, String>>(content)
                    } catch (e: Exception) {
                        emptyMap()
                    }
                }
                entryName.startsWith("NovelAndChapters/") && entryName.endsWith(".json") -> {
                    val content = zipIn.readBytes().decodeToString()
                    try {
                        val novel = json.decodeFromString<LNReaderNovel>(content)
                        novels.add(novel)
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to parse novel: $entryName")
                    }
                }
            }
            
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
    }
    
    return LNReaderBackup(
        version = version,
        novels = novels,
        categories = categories,
        settings = settings
    )
}

/**
 * Streaming callback for processing backup entries without loading everything into memory
 */
interface LNReaderBackupStreamCallback {
    suspend fun onVersion(version: LNReaderVersion)
    suspend fun onCategories(categories: List<LNReaderCategory>)
    suspend fun onSettings(settings: Map<String, String>)
    suspend fun onNovel(novel: LNReaderNovel)
    suspend fun onProgress(current: Int, total: Int)
}

/**
 * Android implementation of streaming LNReader backup parsing
 * 
 * This processes the ZIP file entry-by-entry without loading the entire file into memory.
 * Suitable for large backups (>500MB) that would cause OutOfMemoryError.
 * 
 * @param inputStream The input stream to read from (will be wrapped in ZipInputStream)
 * @param callback Callback for processing each entry as it's parsed
 */
suspend fun parseBackupStreamingPlatform(
    inputStream: java.io.InputStream,
    callback: LNReaderBackupStreamCallback
) {
    var novelCount = 0
    var entryCount = 0

    // Use BufferedInputStream for better read performance
    val bufferedStream = if (inputStream is java.io.BufferedInputStream) inputStream
        else java.io.BufferedInputStream(inputStream, 65536)

    // Single pass: process entries as we read them.
    ZipInputStream(bufferedStream).use { zipIn ->
        var entry = zipIn.nextEntry
        while (entry != null) {
            entryCount++
            val entryName = entry.name
            ireader.core.log.Log.info { "LNReader parse: entry #$entryCount: $entryName (${entry.size} bytes)" }

            when {
                entryName == "Version.json" -> {
                    val content = zipIn.readBytes().decodeToString()
                    val version = try {
                        json.decodeFromString<LNReaderVersion>(content)
                    } catch (e: Exception) {
                        LNReaderVersion("unknown")
                    }
                    callback.onVersion(version)
                }
                entryName == "Category.json" -> {
                    val content = zipIn.readBytes().decodeToString()
                    val categories = try {
                        json.decodeFromString<List<LNReaderCategory>>(content)
                    } catch (e: Exception) {
                        emptyList()
                    }
                    callback.onCategories(categories)
                }
                entryName == "Setting.json" -> {
                    val content = zipIn.readBytes().decodeToString()
                    val settings = try {
                        json.decodeFromString<Map<String, String>>(content)
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to parse Setting.json")
                        emptyMap()
                    }
                    callback.onSettings(settings)
                }
                entryName.startsWith("NovelAndChapters/") && entryName.endsWith(".json") -> {
                    val content = zipIn.readBytes().decodeToString()
                    try {
                        val novel = json.decodeFromString<LNReaderNovel>(content)
                        ireader.core.log.Log.info { "LNReader parse: novel #$novelCount: ${novel.name} (${novel.chapters.size} chapters)" }
                        callback.onNovel(novel)
                        novelCount++
                        callback.onProgress(novelCount, novelCount)
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to parse novel: $entryName")
                    }
                }
            }

            // Skip any remaining bytes in this entry (important for large entries like download.zip)
            if (entryName != "Version.json" && entryName != "Category.json" && 
                entryName != "Setting.json" && 
                !(entryName.startsWith("NovelAndChapters/") && entryName.endsWith(".json"))) {
                // For non-matching entries, skip the data efficiently
                val skipBuffer = ByteArray(8192)
                while (zipIn.read(skipBuffer) != -1) { /* skip */ }
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
    }
    ireader.core.log.Log.info { "LNReader parse: finished. Total entries: $entryCount, novels: $novelCount" }
}

/**
 * Extract chapter content from download.zip contained in the backup.
 *
 * The download.zip contains HTML files at: Novels/{source}/{novelId}/{chapterId}/index.html
 *
 * @param backupBytes The main backup ZIP file bytes
 * @return Map of chapterId (Int) to HTML content string
 */
fun extractChapterContentFromDownloadZip(backupBytes: ByteArray): Map<Int, String> {
    val chapterContentMap = mutableMapOf<Int, String>()

    try {
        ZipInputStream(ByteArrayInputStream(backupBytes)).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                // Look for download.zip entry
                if (entry.name == "download.zip") {
                    ireader.core.log.Log.info { "LNReader: Found download.zip, extracting chapter content..." }

                    // Read the download.zip bytes
                    val downloadZipBytes = zipIn.readBytes()

                    // Parse download.zip
                    ZipInputStream(ByteArrayInputStream(downloadZipBytes)).use { downloadZipIn ->
                        var downloadEntry = downloadZipIn.nextEntry
                        while (downloadEntry != null) {
                            val entryName = downloadEntry.name

                            // Look for index.html files in Novels/{source}/{novelId}/{chapterId}/index.html
                            if (entryName.startsWith("Novels/") && entryName.endsWith("/index.html")) {
                                try {
                                    // Extract chapter ID from path: Novels/{source}/{novelId}/{chapterId}/index.html
                                    val parts = entryName.removePrefix("Novels/")
                                        .removeSuffix("/index.html")
                                        .split("/")

                                    if (parts.size == 3) {
                                        // parts[0] = source (e.g., "royalroad")
                                        // parts[1] = novelId in download.zip (e.g., "1")
                                        // parts[2] = chapterId (e.g., "414853")
                                        val chapterId = parts[2].toIntOrNull()
                                        if (chapterId != null) {
                                            val htmlContent = downloadZipIn.readBytes().decodeToString()
                                            chapterContentMap[chapterId] = htmlContent
                                        }
                                    }
                                } catch (e: Exception) {
                                    ireader.core.log.Log.warn(e, "Failed to extract chapter content from: $entryName")
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

                    ireader.core.log.Log.info { "LNReader: Extracted content for ${chapterContentMap.size} chapters from download.zip" }
                } else {
                    // Skip non-download.zip entries
                    val skipBuffer = ByteArray(8192)
                    while (zipIn.read(skipBuffer) != -1) { /* skip */ }
                }

                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
    } catch (e: Exception) {
        ireader.core.log.Log.warn(e, "Failed to extract download.zip content", e)
    }

    return chapterContentMap
}

/**
 * Convert HTML content to a list of Text pages.
 * Extracts text from paragraph tags and creates Text pages.
 */
fun htmlToTextPages(html: String): List<ireader.core.source.model.Page> {
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

/**
 * Android implementation of chapter content extraction from download.zip.
 */
actual fun extractChapterContentPlatform(backupBytes: ByteArray): Map<Int, String> {
    return extractChapterContentFromDownloadZip(backupBytes)
}

/**
 * Android implementation of LNReader backup detection
 */
actual fun isLNReaderBackupPlatform(bytes: ByteArray): Boolean {
    if (bytes.size < 4) return false
    
    // Check for ZIP magic number (PK..)
    if (bytes[0] != 0x50.toByte() || bytes[1] != 0x4B.toByte()) {
        return false
    }
    
    // Try to open as ZIP and look for LNReader-specific files
    return try {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zipIn ->
            var hasVersionJson = false
            var hasNovelAndChapters = false
            
            var entry = zipIn.nextEntry
            while (entry != null) {
                when {
                    entry.name == "Version.json" -> hasVersionJson = true
                    entry.name.startsWith("NovelAndChapters/") -> hasNovelAndChapters = true
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            
            // LNReader backups should have Version.json and NovelAndChapters directory
            hasVersionJson || hasNovelAndChapters
        }
    } catch (e: Exception) {
        false
    }
}
