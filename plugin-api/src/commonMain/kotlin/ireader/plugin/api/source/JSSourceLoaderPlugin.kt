package ireader.plugin.api.source

import ireader.plugin.api.Plugin
import ireader.plugin.api.PluginContext
import kotlinx.serialization.Serializable

/**
 * Plugin interface for loading JavaScript-based source extensions.
 * 
 * Supports LNReader-style JS sources and other JS-based extension formats.
 * The plugin handles:
 * - JS file parsing and validation
 * - JS engine execution (QuickJS, Duktape, etc.)
 * - Source instantiation and lifecycle
 * - Model conversion to unified source format
 */
interface JSSourceLoaderPlugin : SourceLoaderPlugin {
    
    override val loaderType: SourceLoaderType
        get() = SourceLoaderType.LNREADER
    
    override val loaderName: String
        get() = "JavaScript Sources"
    
    override val supportedFormats: List<ExtensionFormat>
        get() = listOf(ExtensionFormat.JS_BUNDLE, ExtensionFormat.JSON)
    
    // ==================== JS-Specific Methods ====================
    
    /**
     * Load a JS source from a file.
     * @param jsPath Path to the JS file or bundle
     * @return Loaded source info
     */
    suspend fun loadJSSource(jsPath: String): JSSourceInfo
    
    /**
     * Load a JS source from code string.
     * @param code JS source code
     * @param sourceId Unique identifier for the source
     * @return Loaded source info
     */
    suspend fun loadJSSourceFromCode(code: String, sourceId: String): JSSourceInfo
    
    /**
     * Unload a JS source and release resources.
     */
    fun unloadJSSource(sourceId: String)
    
    /**
     * Validate a JS source file before loading.
     */
    suspend fun validateJSSource(jsPath: String): JSValidationResult
    
    /**
     * Get loaded JS sources.
     */
    fun getJSSources(): List<JSSourceInfo>
    
    /**
     * Execute a JS function on a source.
     * Used for advanced operations not covered by UnifiedSource.
     */
    suspend fun executeFunction(
        sourceId: String,
        functionName: String,
        args: List<Any?> = emptyList()
    ): Any?
    
    /**
     * Get the JS engine being used.
     */
    fun getJSEngine(): JSEngineType
    
    /**
     * Set JS engine preferences.
     */
    fun setJSEnginePreferences(prefs: JSEnginePreferences)
}

/**
 * Loaded JS source info.
 */
@Serializable
data class JSSourceInfo(
    /** Unique source identifier */
    val id: String,
    /** Source display name */
    val name: String,
    /** Version string */
    val version: String,
    /** Primary language */
    val lang: String,
    /** Base URL */
    val baseUrl: String,
    /** Whether source contains NSFW content */
    val isNsfw: Boolean = false,
    /** Icon URL */
    val iconUrl: String? = null,
    /** Source file path */
    val filePath: String? = null,
    /** Whether source supports latest updates */
    val supportsLatest: Boolean = false,
    /** Content type */
    val contentType: SourceContentType = SourceContentType.NOVEL,
    /** Additional metadata */
    val metadata: Map<String, String> = emptyMap()
) {
    /** Convert to unified SourceExtensionInfo */
    fun toSourceExtensionInfo(): SourceExtensionInfo = SourceExtensionInfo(
        id = id,
        pkgName = id,
        name = name,
        versionName = version,
        versionCode = version.hashCode(),
        lang = lang,
        isNsfw = isNsfw,
        sourceIds = listOf(id.hashCode().toLong() and 0x7FFFFFFF),
        iconUrl = iconUrl,
        loaderType = SourceLoaderType.LNREADER
    )
}

/**
 * JS source validation result.
 */
@Serializable
sealed class JSValidationResult {
    @Serializable
    data class Valid(
        val id: String,
        val name: String,
        val version: String
    ) : JSValidationResult()
    
    @Serializable
    data class Invalid(val reason: String) : JSValidationResult()
    
    @Serializable
    data class SyntaxError(
        val line: Int,
        val column: Int,
        val message: String
    ) : JSValidationResult()
    
    @Serializable
    data class MissingExport(val exportName: String) : JSValidationResult()
}

/**
 * JS engine types supported.
 */
@Serializable
enum class JSEngineType {
    /** QuickJS - fast, lightweight */
    QUICKJS,
    /** Duktape - small footprint */
    DUKTAPE,
    /** V8 - full-featured (Android only) */
    V8,
    /** GraalJS - JVM-based */
    GRAALJS,
    /** Rhino - legacy JVM */
    RHINO,
    /** WebView - browser-based (Android only) */
    WEBVIEW
}

/**
 * JS engine preferences.
 */
@Serializable
data class JSEnginePreferences(
    /** Preferred engine type */
    val preferredEngine: JSEngineType = JSEngineType.QUICKJS,
    /** Memory limit in MB */
    val memoryLimitMb: Int = 64,
    /** Execution timeout in milliseconds */
    val timeoutMs: Long = 30000,
    /** Enable strict mode */
    val strictMode: Boolean = true,
    /** Allow network access from JS */
    val allowNetwork: Boolean = true,
    /** Allow file system access from JS */
    val allowFileSystem: Boolean = false,
    /** Enable console logging */
    val enableConsole: Boolean = true,
    /** Cache compiled scripts */
    val cacheCompiledScripts: Boolean = true
)

/**
 * JS source definition format (for JSON-based sources).
 * This allows defining simple sources without writing JS code.
 */
@Serializable
data class JSSourceDefinition(
    /** Source identifier */
    val id: String,
    /** Source name */
    val name: String,
    /** Version */
    val version: String,
    /** Language code */
    val lang: String,
    /** Base URL */
    val baseUrl: String,
    /** Icon URL */
    val iconUrl: String? = null,
    /** Whether NSFW */
    val isNsfw: Boolean = false,
    /** Content type */
    val contentType: String = "novel",
    /** Supports latest */
    val supportsLatest: Boolean = false,
    /** Popular page selector/config */
    val popular: PageConfig? = null,
    /** Latest page selector/config */
    val latest: PageConfig? = null,
    /** Search config */
    val search: SearchConfig? = null,
    /** Detail page config */
    val detail: DetailConfig? = null,
    /** Chapter list config */
    val chapters: ChapterConfig? = null,
    /** Content page config */
    val content: ContentConfig? = null,
    /** Custom headers */
    val headers: Map<String, String> = emptyMap(),
    /** Custom JS code to inject */
    val customJs: String? = null
)

/**
 * Page configuration for list pages.
 */
@Serializable
data class PageConfig(
    /** URL pattern (use {page} for page number) */
    val url: String,
    /** CSS selector for items */
    val itemSelector: String,
    /** Title selector (relative to item) */
    val titleSelector: String,
    /** URL selector (relative to item) */
    val urlSelector: String,
    /** Cover selector (relative to item) */
    val coverSelector: String? = null,
    /** Has next page selector */
    val hasNextSelector: String? = null,
    /** Attribute to get URL from */
    val urlAttribute: String = "href",
    /** Attribute to get cover from */
    val coverAttribute: String = "src"
)

/**
 * Search configuration.
 */
@Serializable
data class SearchConfig(
    /** Search URL pattern (use {query} and {page}) */
    val url: String,
    /** CSS selector for items */
    val itemSelector: String,
    /** Title selector */
    val titleSelector: String,
    /** URL selector */
    val urlSelector: String,
    /** Cover selector */
    val coverSelector: String? = null,
    /** Has next page selector */
    val hasNextSelector: String? = null,
    /** URL attribute */
    val urlAttribute: String = "href",
    /** Cover attribute */
    val coverAttribute: String = "src"
)

/**
 * Detail page configuration.
 */
@Serializable
data class DetailConfig(
    /** Title selector */
    val titleSelector: String? = null,
    /** Cover selector */
    val coverSelector: String? = null,
    /** Author selector */
    val authorSelector: String? = null,
    /** Description selector */
    val descriptionSelector: String? = null,
    /** Genre selector */
    val genreSelector: String? = null,
    /** Status selector */
    val statusSelector: String? = null,
    /** Cover attribute */
    val coverAttribute: String = "src",
    /** Status mapping */
    val statusMapping: Map<String, String> = emptyMap()
)

/**
 * Chapter list configuration.
 */
@Serializable
data class ChapterConfig(
    /** Chapter list selector */
    val listSelector: String,
    /** Chapter item selector */
    val itemSelector: String,
    /** Title selector */
    val titleSelector: String,
    /** URL selector */
    val urlSelector: String,
    /** Date selector */
    val dateSelector: String? = null,
    /** URL attribute */
    val urlAttribute: String = "href",
    /** Date format */
    val dateFormat: String? = null,
    /** Reverse order */
    val reverseOrder: Boolean = false
)

/**
 * Content page configuration.
 */
@Serializable
data class ContentConfig(
    /** Content selector */
    val contentSelector: String,
    /** Elements to remove */
    val removeSelectors: List<String> = emptyList(),
    /** Next page selector (for paginated content) */
    val nextPageSelector: String? = null,
    /** Image selector (for manga) */
    val imageSelector: String? = null,
    /** Image attribute */
    val imageAttribute: String = "src",
    /** Clean HTML */
    val cleanHtml: Boolean = true
)
