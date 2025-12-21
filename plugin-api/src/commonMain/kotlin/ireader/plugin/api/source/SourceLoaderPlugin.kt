package ireader.plugin.api.source

import ireader.plugin.api.Plugin
import ireader.plugin.api.PluginContext
import kotlinx.serialization.Serializable

/**
 * Base interface for source loader plugins.
 * 
 * Source loaders provide content sources (manga, novels, etc.) to IReader.
 * Different loaders can support different source formats:
 * - Tachiyomi/Mihon extensions (APK-based)
 * - LNReader sources (JS-based)
 * - IReader built-in sources
 * - Custom sources
 * 
 * All sources are unified through the [UnifiedSource] interface,
 * allowing IReader to display them in the same UI regardless of origin.
 * 
 * Source loaders should also implement [ExtensionLoader] to handle
 * the actual loading of extension files.
 */
interface SourceLoaderPlugin : Plugin, ExtensionLoader {
    /**
     * Loader type identifier.
     */
    val loaderType: SourceLoaderType
    
    /**
     * Human-readable loader name.
     */
    val loaderName: String
    
    /**
     * Get all loaded sources.
     */
    fun getSources(): List<UnifiedSource>
    
    /**
     * Get a source by ID.
     */
    fun getSource(sourceId: Long): UnifiedSource?
    
    /**
     * Get sources filtered by language.
     */
    fun getSourcesByLanguage(lang: String): List<UnifiedSource>
    
    /**
     * Get all available languages from loaded sources.
     */
    fun getAvailableLanguages(): List<String>
    
    /**
     * Search sources by name.
     */
    fun searchSources(query: String): List<UnifiedSource>
    
    /**
     * Refresh/reload all sources.
     */
    suspend fun refreshSources()
    
    /**
     * Check if this loader supports extension repositories.
     */
    fun supportsRepositories(): Boolean = false
    
    /**
     * Get extension repositories (if supported).
     */
    fun getRepositories(): List<SourceRepository> = emptyList()
    
    /**
     * Add a repository (if supported).
     */
    fun addRepository(repo: SourceRepository) {}
    
    /**
     * Remove a repository (if supported).
     */
    fun removeRepository(repoUrl: String) {}
    
    /**
     * Fetch available extensions from repositories (if supported).
     */
    suspend fun fetchAvailableExtensions(): List<SourceExtensionMeta> = emptyList()
    
    /**
     * Install an extension (if supported).
     * Uses the ExtensionLoader and ExtensionInstaller interfaces.
     */
    suspend fun installExtension(
        extension: SourceExtensionMeta,
        onProgress: (Float) -> Unit = {}
    ): SourceExtensionInfo? = null
    
    /**
     * Uninstall an extension (if supported).
     */
    suspend fun uninstallExtension(extensionId: String): Boolean = false
    
    /**
     * Get installed extensions (if supported).
     */
    fun getInstalledExtensions(): List<SourceExtensionInfo> = emptyList()
    
    /**
     * Check for extension updates (if supported).
     */
    suspend fun checkForUpdates(): List<SourceExtensionUpdate> = emptyList()
    
    /**
     * Get the extension installer for this loader.
     * Returns null if installation is not supported.
     */
    fun getInstaller(): ExtensionInstaller? = null
    
    /**
     * Get the extension downloader for this loader.
     * Returns null if downloading is not supported.
     */
    fun getDownloader(): ExtensionDownloader? = null
}

/**
 * Types of source loaders.
 */
@Serializable
enum class SourceLoaderType {
    /** Tachiyomi/Mihon APK extensions */
    TACHIYOMI,
    /** LNReader JS-based sources */
    LNREADER,
    /** Built-in IReader sources */
    IREADER_BUILTIN,
    /** User-defined sources */
    USER_SOURCE,
    /** Custom loader */
    CUSTOM
}

/**
 * Extension repository for downloading source extensions.
 */
@Serializable
data class SourceRepository(
    /** Repository name */
    val name: String,
    /** Base URL */
    val baseUrl: String,
    /** Whether this is an official repository */
    val isOfficial: Boolean = false,
    /** Whether repository is enabled */
    val isEnabled: Boolean = true,
    /** Repository icon URL */
    val iconUrl: String? = null,
    /** Loader type this repository provides */
    val loaderType: SourceLoaderType = SourceLoaderType.TACHIYOMI
) {
    companion object {
        /** Official Keiyoushi (Tachiyomi) repository */
        val KEIYOUSHI = SourceRepository(
            name = "Keiyoushi",
            baseUrl = "https://raw.githubusercontent.com/keiyoushi/extensions/repo",
            isOfficial = true,
            loaderType = SourceLoaderType.TACHIYOMI
        )
    }
}

/**
 * Metadata for an available extension (from repository).
 */
@Serializable
data class SourceExtensionMeta(
    /** Unique extension identifier */
    val id: String,
    /** Package name */
    val pkgName: String,
    /** Display name */
    val name: String,
    /** Version string */
    val versionName: String,
    /** Version code */
    val versionCode: Int,
    /** Primary language */
    val lang: String,
    /** Whether extension contains NSFW content */
    val isNsfw: Boolean,
    /** Download file name */
    val fileName: String,
    /** Icon URL */
    val iconUrl: String,
    /** Repository URL */
    val repoUrl: String,
    /** Number of sources in extension */
    val sourceCount: Int = 1,
    /** File size in bytes */
    val fileSize: Long? = null,
    /** Loader type */
    val loaderType: SourceLoaderType = SourceLoaderType.TACHIYOMI
)

/**
 * Information about an installed extension.
 */
@Serializable
data class SourceExtensionInfo(
    /** Unique extension identifier */
    val id: String,
    /** Package name */
    val pkgName: String,
    /** Display name */
    val name: String,
    /** Version string */
    val versionName: String,
    /** Version code */
    val versionCode: Int,
    /** Primary language */
    val lang: String,
    /** Whether extension contains NSFW content */
    val isNsfw: Boolean,
    /** Source IDs provided by this extension */
    val sourceIds: List<Long>,
    /** Icon URL or local path */
    val iconUrl: String? = null,
    /** Whether extension has an update available */
    val hasUpdate: Boolean = false,
    /** Whether extension is enabled */
    val isEnabled: Boolean = true,
    /** Loader type */
    val loaderType: SourceLoaderType = SourceLoaderType.TACHIYOMI,
    /** Installation timestamp */
    val installedAt: Long? = null
)

/**
 * Extension update information.
 */
@Serializable
data class SourceExtensionUpdate(
    /** Extension ID */
    val extensionId: String,
    /** Current version */
    val currentVersion: String,
    /** New version */
    val newVersion: String,
    /** New version code */
    val newVersionCode: Int,
    /** Changelog (if available) */
    val changelog: String? = null
)
