package ireader.domain.usecases.backup.lnreader

import ireader.domain.models.lnreader.*
import kotlinx.serialization.json.Json
import okio.Buffer
import okio.BufferedSource
import okio.Source
import okio.buffer

/**
 * Parser for LNReader backup ZIP files.
 * Uses Okio for KMP-compatible stream operations.
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
     * Parse a complete LNReader backup from Okio Source
     */
    suspend fun parseBackup(source: Source): LNReaderBackup {
        var version = LNReaderVersion("unknown")
        val novels = mutableListOf<LNReaderNovel>()
        var categories = emptyList<LNReaderCategory>()
        var settings = emptyMap<String, String>()
        
        // Read all bytes first, then parse as ZIP
        val bytes = source.buffer().readByteArray()
        return parseBackup(bytes)
    }
    
    /**
     * Parse backup from byte array using platform-specific ZIP handling
     */
    suspend fun parseBackup(bytes: ByteArray): LNReaderBackup {
        return parseBackupFromZipBytes(bytes)
    }
    
    /**
     * Platform-specific ZIP parsing - implemented via expect/actual
     */
    private suspend fun parseBackupFromZipBytes(bytes: ByteArray): LNReaderBackup {
        // For now, use a simple approach that works cross-platform
        // This would need platform-specific ZIP implementations for full KMP support
        return parseBackupPlatform(bytes)
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
         * Uses platform-specific ZIP detection
         */
        fun isLNReaderBackup(bytes: ByteArray): Boolean {
            return isLNReaderBackupPlatform(bytes)
        }
    }
}

/**
 * Platform-specific backup parsing - needs expect/actual implementation
 */
expect suspend fun parseBackupPlatform(bytes: ByteArray): LNReaderBackup

/**
 * Platform-specific LNReader backup detection
 */
expect fun isLNReaderBackupPlatform(bytes: ByteArray): Boolean
