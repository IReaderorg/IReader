package ireader.plugin.api

/**
 * Context provided to plugins for accessing app resources.
 * Provides sandboxed access to system resources based on granted permissions.
 */
interface PluginContext {
    /**
     * Plugin's unique identifier.
     */
    val pluginId: String
    
    /**
     * Granted permissions for this plugin.
     */
    val permissions: List<PluginPermission>
    
    /**
     * Get the plugin's data directory for storing files.
     * Only available if STORAGE permission is granted.
     * 
     * @return Path to plugin's data directory
     */
    fun getDataDir(): String
    
    /**
     * Check if plugin has a specific permission.
     * 
     * @param permission Permission to check
     * @return true if permission is granted
     */
    fun hasPermission(permission: PluginPermission): Boolean
    
    /**
     * Get plugin-specific preferences storage.
     * Only available if PREFERENCES permission is granted.
     * 
     * @return Preferences store for this plugin
     */
    fun getPreferences(): PluginPreferencesStore
    
    /**
     * Get the cache directory for temporary files.
     * Only available if STORAGE permission is granted.
     */
    fun getCacheDir(): String
    
    /**
     * Get the HTTP client for network requests.
     * Only available if NETWORK permission is granted.
     */
    fun getHttpClient(): PluginHttpClientProvider?
    
    /**
     * Get the glossary service.
     * Only available if GLOSSARY_ACCESS permission is granted.
     */
    fun getGlossaryService(): GlossaryServiceProvider?
    
    /**
     * Get the character database service.
     * Only available if CHARACTER_DATABASE permission is granted.
     */
    fun getCharacterService(): CharacterServiceProvider?
    
    /**
     * Get the sync service.
     * Only available if SYNC_DATA permission is granted.
     */
    fun getSyncService(): SyncServiceProvider?
    
    /**
     * Get the library service.
     * Only available if LIBRARY_ACCESS permission is granted.
     */
    fun getLibraryService(): LibraryServiceProvider?
    
    /**
     * Get the reader context.
     * Only available if READER_CONTEXT permission is granted.
     */
    fun getReaderContext(): ReaderContextProvider?
    
    /**
     * Show a notification.
     * Only available if NOTIFICATIONS permission is granted.
     */
    fun showNotification(title: String, message: String, channelId: String = "plugin")
    
    /**
     * Log a message for debugging.
     */
    fun log(level: LogLevel, message: String, throwable: Throwable? = null)
    
    /**
     * Get app version information.
     */
    fun getAppVersion(): AppVersionInfo
    
    /**
     * Get current platform.
     */
    fun getPlatform(): Platform
}

/**
 * Log levels for plugin logging.
 */
enum class LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR
}

/**
 * App version information.
 */
data class AppVersionInfo(
    val versionName: String,
    val versionCode: Int,
    val buildType: String
)

/**
 * Plugin-specific preferences storage interface.
 * Provides key-value storage for plugin configuration.
 */
interface PluginPreferencesStore {
    /**
     * Get a string preference.
     */
    fun getString(key: String, defaultValue: String = ""): String
    
    /**
     * Set a string preference.
     */
    fun putString(key: String, value: String)
    
    /**
     * Get an integer preference.
     */
    fun getInt(key: String, defaultValue: Int = 0): Int
    
    /**
     * Set an integer preference.
     */
    fun putInt(key: String, value: Int)
    
    /**
     * Get a boolean preference.
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    
    /**
     * Set a boolean preference.
     */
    fun putBoolean(key: String, value: Boolean)
    
    /**
     * Get a long preference.
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long
    
    /**
     * Set a long preference.
     */
    fun putLong(key: String, value: Long)
    
    /**
     * Get a float preference.
     */
    fun getFloat(key: String, defaultValue: Float = 0f): Float
    
    /**
     * Set a float preference.
     */
    fun putFloat(key: String, value: Float)
    
    /**
     * Get a string set preference.
     */
    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Set<String>
    
    /**
     * Set a string set preference.
     */
    fun putStringSet(key: String, value: Set<String>)
    
    /**
     * Remove a preference.
     */
    fun remove(key: String)
    
    /**
     * Clear all preferences for this plugin.
     */
    fun clear()
    
    /**
     * Check if a preference exists.
     */
    fun contains(key: String): Boolean
    
    /**
     * Get all preference keys.
     */
    fun getAllKeys(): Set<String>
}

/**
 * HTTP client provider for plugins.
 */
interface PluginHttpClientProvider {
    suspend fun get(url: String, headers: Map<String, String> = emptyMap()): HttpResponse
    suspend fun post(url: String, body: String, headers: Map<String, String> = emptyMap()): HttpResponse
    suspend fun postForm(url: String, formData: Map<String, String>, headers: Map<String, String> = emptyMap()): HttpResponse
    suspend fun postMultipart(url: String, parts: List<MultipartData>, headers: Map<String, String> = emptyMap()): HttpResponse
    suspend fun download(url: String, headers: Map<String, String> = emptyMap()): ByteArray
}

data class HttpResponse(
    val statusCode: Int,
    val body: String,
    val headers: Map<String, String>
)

data class MultipartData(
    val name: String,
    val filename: String? = null,
    val contentType: String = "application/octet-stream",
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as MultipartData
        return name == other.name && filename == other.filename && data.contentEquals(other.data)
    }
    
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (filename?.hashCode() ?: 0)
        result = 31 * result + data.contentHashCode()
        return result
    }
}

/**
 * Glossary service provider for plugins.
 */
interface GlossaryServiceProvider {
    suspend fun getGlossaries(): List<Glossary>
    suspend fun lookupTerm(term: String, glossaryIds: List<String> = emptyList()): List<GlossaryEntry>
    suspend fun applyGlossary(text: String, glossaryIds: List<String>): String
}

/**
 * Character service provider for plugins.
 * Uses CharacterInfo from CharacterDatabaseApi.
 */
interface CharacterServiceProvider {
    suspend fun getCharacters(bookId: String): List<CharacterInfo>
    suspend fun addCharacter(bookId: String, character: CharacterInfo): CharacterInfo
    suspend fun updateCharacter(character: CharacterInfo): CharacterInfo
    suspend fun deleteCharacter(characterId: String)
    suspend fun getCharacterRelationships(characterId: String): List<PluginCharacterRelationship>
}

/**
 * Character relationship for service provider.
 */
data class PluginCharacterRelationship(
    val sourceCharacterId: String,
    val targetCharacterId: String,
    val relationshipType: String,
    val description: String? = null
)

/**
 * Sync service provider for plugins.
 */
interface SyncServiceProvider {
    suspend fun getLastSyncTime(): Long?
    suspend fun triggerSync()
    suspend fun getSyncStatus(): SyncStatus
}

/**
 * Library service provider for plugins.
 */
interface LibraryServiceProvider {
    suspend fun getLibraryBooks(): List<LibraryBook>
    suspend fun getBook(bookId: String): LibraryBook?
    suspend fun getReadingProgress(bookId: String): ReadingProgress?
    suspend fun updateReadingProgress(bookId: String, progress: ReadingProgress)
}

data class LibraryBook(
    val id: String,
    val sourceId: String,
    val title: String,
    val author: String?,
    val coverUrl: String?,
    val status: String,
    val chapterCount: Int,
    val lastReadChapter: String?,
    val addedAt: Long,
    val lastUpdated: Long
)

data class ReadingProgress(
    val chapterId: String,
    val position: Int,
    val percentage: Float,
    val lastRead: Long
)

/**
 * Reader context provider for plugins.
 */
interface ReaderContextProvider {
    fun getCurrentBook(): LibraryBook?
    fun getCurrentChapter(): ChapterInfo?
    fun getCurrentPosition(): Int
    fun getSelectedText(): String?
    fun navigateToChapter(chapterId: String)
    fun navigateToPosition(position: Int)
}

data class ChapterInfo(
    val id: String,
    val bookId: String,
    val title: String,
    val number: Float,
    val content: String? = null
)
