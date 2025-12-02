package ireader.domain.usecases.backup.lnreader

import ireader.domain.models.lnreader.*
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Parser for LNReader backup ZIP files
 * 
 * LNReader backup structure:
 * - Version.json - Backup version info
 * - Category.json - Categories with novel associations
 * - Setting.json - App settings (MMKV key-value)
 * - NovelAndChapters/ - Directory with individual novel JSON files
 * - download.zip - Downloaded chapter content (optional)
 */
class LNReaderBackupParser {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    /**
     * Parse a complete LNReader backup from input stream
     */
    suspend fun parseBackup(inputStream: InputStream): LNReaderBackup {
        var version = LNReaderVersion("unknown")
        val novels = mutableListOf<LNReaderNovel>()
        var categories = emptyList<LNReaderCategory>()
        var settings = emptyMap<String, String>()
        
        ZipInputStream(inputStream).use { zipStream ->
            var entry = zipStream.nextEntry
            while (entry != null) {
                val entryName = entry.name
                
                when {
                    entryName == "Version.json" || entryName.endsWith("/Version.json") -> {
                        val content = zipStream.readBytes().decodeToString()
                        version = parseVersion(content)
                    }
                    entryName == "Category.json" || entryName.endsWith("/Category.json") -> {
                        val content = zipStream.readBytes().decodeToString()
                        categories = parseCategories(content)
                    }
                    entryName == "Setting.json" || entryName.endsWith("/Setting.json") -> {
                        val content = zipStream.readBytes().decodeToString()
                        settings = parseSettings(content)
                    }
                    entryName.contains("NovelAndChapters/") && entryName.endsWith(".json") -> {
                        val content = zipStream.readBytes().decodeToString()
                        try {
                            val novel = parseNovel(content)
                            novels.add(novel)
                        } catch (e: Exception) {
                            ireader.core.log.Log.warn(e, "Failed to parse novel: $entryName")
                        }
                    }
                }
                
                zipStream.closeEntry()
                entry = zipStream.nextEntry
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
     * Parse backup from byte array
     */
    suspend fun parseBackup(bytes: ByteArray): LNReaderBackup {
        return parseBackup(ByteArrayInputStream(bytes))
    }
    
    /**
     * Parse Version.json content
     */
    fun parseVersion(jsonContent: String): LNReaderVersion {
        return try {
            json.decodeFromString<LNReaderVersion>(jsonContent)
        } catch (e: Exception) {
            LNReaderVersion("unknown")
        }
    }
    
    /**
     * Parse Category.json content
     */
    fun parseCategories(jsonContent: String): List<LNReaderCategory> {
        return try {
            json.decodeFromString<List<LNReaderCategory>>(jsonContent)
        } catch (e: Exception) {
            ireader.core.log.Log.warn(e, "Failed to parse categories")
            emptyList()
        }
    }
    
    /**
     * Parse Setting.json content
     */
    fun parseSettings(jsonContent: String): Map<String, String> {
        return try {
            json.decodeFromString<Map<String, String>>(jsonContent)
        } catch (e: Exception) {
            ireader.core.log.Log.warn(e, "Failed to parse settings")
            emptyMap()
        }
    }
    
    /**
     * Parse individual novel JSON file
     */
    fun parseNovel(jsonContent: String): LNReaderNovel {
        return json.decodeFromString<LNReaderNovel>(jsonContent)
    }
    
    companion object {
        /**
         * Check if the given bytes represent an LNReader backup
         */
        fun isLNReaderBackup(bytes: ByteArray): Boolean {
            return try {
                ZipInputStream(ByteArrayInputStream(bytes)).use { zipStream ->
                    var entry = zipStream.nextEntry
                    var hasVersion = false
                    var hasNovelDir = false
                    
                    while (entry != null) {
                        val name = entry.name
                        if (name == "Version.json" || name.endsWith("/Version.json")) {
                            hasVersion = true
                        }
                        if (name.contains("NovelAndChapters/")) {
                            hasNovelDir = true
                        }
                        if (hasVersion && hasNovelDir) return@use true
                        
                        zipStream.closeEntry()
                        entry = zipStream.nextEntry
                    }
                    hasVersion || hasNovelDir
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}
