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
