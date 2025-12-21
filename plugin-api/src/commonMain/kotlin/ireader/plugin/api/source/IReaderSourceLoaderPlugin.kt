package ireader.plugin.api.source

import ireader.plugin.api.Plugin
import kotlinx.serialization.Serializable

/**
 * Plugin interface for loading IReader's built-in sources.
 * 
 * IReader sources are Kotlin-based and compiled into the app or plugins.
 * They provide the most native integration with IReader's features.
 */
interface IReaderSourceLoaderPlugin : SourceLoaderPlugin {
    
    override val loaderType: SourceLoaderType
        get() = SourceLoaderType.IREADER_BUILTIN
    
    override val loaderName: String
        get() = "IReader Sources"
    
    override val supportedFormats: List<ExtensionFormat>
        get() = listOf(ExtensionFormat.IPLUGIN, ExtensionFormat.JAR)
    
    // ==================== IReader-Specific Methods ====================
    
    /**
     * Register a built-in source.
     * Used for sources compiled into the app.
     */
    fun registerSource(source: UnifiedSource)
    
    /**
     * Unregister a source.
     */
    fun unregisterSource(sourceId: Long)
    
    /**
     * Get all registered sources.
     */
    fun getRegisteredSources(): List<IReaderSourceInfo>
    
    /**
     * Load sources from an IReader plugin package.
     */
    suspend fun loadFromPlugin(pluginPath: String): List<IReaderSourceInfo>
    
    /**
     * Check if a source is built-in (compiled into app).
     */
    fun isBuiltIn(sourceId: Long): Boolean
}

/**
 * IReader source info.
 */
@Serializable
data class IReaderSourceInfo(
    /** Source ID */
    val id: Long,
    /** Source name */
    val name: String,
    /** Language */
    val lang: String,
    /** Base URL */
    val baseUrl: String,
    /** Whether built-in */
    val isBuiltIn: Boolean,
    /** Plugin ID (if from plugin) */
    val pluginId: String? = null,
    /** Version */
    val version: String = "1.0",
    /** Icon URL */
    val iconUrl: String? = null,
    /** Content type */
    val contentType: SourceContentType = SourceContentType.NOVEL,
    /** Whether NSFW */
    val isNsfw: Boolean = false
) {
    /** Convert to unified SourceExtensionInfo */
    fun toSourceExtensionInfo(): SourceExtensionInfo = SourceExtensionInfo(
        id = id.toString(),
        pkgName = pluginId ?: "ireader.builtin.$id",
        name = name,
        versionName = version,
        versionCode = version.hashCode(),
        lang = lang,
        isNsfw = isNsfw,
        sourceIds = listOf(id),
        iconUrl = iconUrl,
        loaderType = SourceLoaderType.IREADER_BUILTIN
    )
}
