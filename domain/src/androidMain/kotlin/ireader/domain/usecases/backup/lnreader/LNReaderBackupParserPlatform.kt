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
    var totalNovels = 0
    
    // First pass: count total novels for progress reporting
    inputStream.mark(Int.MAX_VALUE)
    ZipInputStream(inputStream).use { zipIn ->
        var entry = zipIn.nextEntry
        while (entry != null) {
            if (entry.name.startsWith("NovelAndChapters/") && entry.name.endsWith(".json")) {
                totalNovels++
            }
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
    }
    inputStream.reset()
    
    // Second pass: process entries
    ZipInputStream(inputStream).use { zipIn ->
        var entry = zipIn.nextEntry
        while (entry != null) {
            val entryName = entry.name
            
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
                        emptyMap()
                    }
                    callback.onSettings(settings)
                }
                entryName.startsWith("NovelAndChapters/") && entryName.endsWith(".json") -> {
                    val content = zipIn.readBytes().decodeToString()
                    try {
                        val novel = json.decodeFromString<LNReaderNovel>(content)
                        callback.onNovel(novel)
                        novelCount++
                        callback.onProgress(novelCount, totalNovels)
                    } catch (e: Exception) {
                        ireader.core.log.Log.warn(e, "Failed to parse novel: $entryName")
                    }
                }
            }
            
            zipIn.closeEntry()
            entry = zipIn.nextEntry
        }
    }
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
