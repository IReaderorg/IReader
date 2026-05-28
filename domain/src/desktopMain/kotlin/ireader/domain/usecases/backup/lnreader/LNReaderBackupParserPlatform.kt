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
 * Desktop implementation of LNReader backup parsing using java.util.zip.
 *
 * Desktop/JVM has full access to java.util.zip, so we can provide the same
 * implementation as Android for parsing LNReader backup ZIP files.
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
 * Desktop implementation of chapter content extraction from download.zip.
 *
 * The download.zip contains HTML files at: Novels/{source}/{novelId}/{chapterId}/index.html
 */
fun extractChapterContentFromDownloadZip(backupBytes: ByteArray): Map<Int, String> {
    val chapterContentMap = mutableMapOf<Int, String>()

    try {
        ZipInputStream(ByteArrayInputStream(backupBytes)).use { zipIn ->
            var entry = zipIn.nextEntry
            while (entry != null) {
                if (entry.name == "download.zip") {
                    ireader.core.log.Log.info { "LNReader: Found download.zip, extracting chapter content..." }

                    val downloadZipBytes = zipIn.readBytes()

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
                                            val htmlContent = downloadZipIn.readBytes().decodeToString()
                                            chapterContentMap[chapterId] = htmlContent
                                        }
                                    }
                                } catch (e: Exception) {
                                    ireader.core.log.Log.warn(e, "Failed to extract chapter content from: $entryName")
                                }
                            } else {
                                val skipBuffer = ByteArray(8192)
                                while (downloadZipIn.read(skipBuffer) != -1) { /* skip */ }
                            }

                            downloadZipIn.closeEntry()
                            downloadEntry = downloadZipIn.nextEntry
                        }
                    }

                    ireader.core.log.Log.info { "LNReader: Extracted content for ${chapterContentMap.size} chapters from download.zip" }
                } else {
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
 * Desktop implementation of chapter content extraction from download.zip.
 */
actual fun extractChapterContentPlatform(backupBytes: ByteArray): Map<Int, String> {
    return extractChapterContentFromDownloadZip(backupBytes)
}

/**
 * Desktop implementation of LNReader backup detection.
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

            hasVersionJson || hasNovelAndChapters
        }
    } catch (e: Exception) {
        false
    }
}
