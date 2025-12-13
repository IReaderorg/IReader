package ireader.core.source

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.url
import ireader.core.source.CatalogSource.Companion.TYPE_NOVEL
import ireader.core.source.model.Command
import ireader.core.source.model.CommandList
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.Listing
import ireader.core.source.model.PageComplete
import ireader.core.source.model.PageUrl
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * A simple implementation for sources from a website.
 */
@Suppress("unused", "unused_parameter")
abstract class HttpSource(private val dependencies: ireader.core.source.Dependencies) :
    ireader.core.source.CatalogSource {

    /**
     * Base url of the website without the trailing slash, like: http://mysite.com
     */
    abstract val baseUrl: String

    /**
     * Version id used to generate the source id. If the site completely changes and urls are
     * incompatible, you may increase this value and it'll be considered as a new source.
     */
    open val versionId = 1

    val eventFlow = MutableSharedFlow<String>()

    /**
     * Id of the source. By default it uses a generated id using the first 16 characters (64 bits)
     * of the MD5 of the string: sourcename/language/versionId
     * Note the generated id sets the sign bit to 0.
     */
    override val id: Long by lazy {
        val key = "${name.lowercase()}/$lang/$versionId"
        generateSourceId(key)
    }

    /**
     * Default network client for doing requests.
     */
    open val client: HttpClient
        get() = dependencies.httpClients.default

    open val type: Int = TYPE_NOVEL

    /**
     * Visible name of the source.
     */
    override fun toString() = "$name (${lang.uppercase()})"

    open suspend fun getPage(page: PageUrl): PageComplete {
        throw Exception("Incomplete source implementation. Please override getPage when using PageUrl")
    }

    open fun getImageRequest(page: ImageUrl): Pair<HttpClient, HttpRequestBuilder> {
        return client to HttpRequestBuilder().apply {
            url(page.url)
        }
    }

    open fun getCoverRequest(url: String): Pair<HttpClient, HttpRequestBuilder> {
        return client to HttpRequestBuilder().apply {
            url(url)
        }
    }

    override fun getListings(): List<Listing> {
        return emptyList()
    }

    override fun getCommands(): CommandList {
        return emptyList()
    }
    
    protected fun getAbsoluteUrl(path: String): String {
        return when {
            path.startsWith("http://") || path.startsWith("https://") -> path
            path.startsWith("//") -> "https:$path"
            path.startsWith("/") -> "$baseUrl$path"
            else -> "$baseUrl/$path"
        }
    }
    
    protected suspend fun emitEvent(event: String) {
        eventFlow.emit(event)
    }
    
    open suspend fun isAvailable(): Boolean = true
    
    open fun getCapabilities(): SourceCapabilities {
        return SourceCapabilities()
    }
    
    companion object {
        /**
         * Generate a source ID from a key string using a simple hash
         */
        fun generateSourceId(key: String): Long {
            // Simple hash implementation for KMP compatibility
            var hash = 0L
            for (char in key) {
                hash = 31 * hash + char.code
            }
            return hash and Long.MAX_VALUE
        }
    }
}

/**
 * Describes the capabilities of a source.
 * Sources can declare which features they support for better UI/UX.
 */
data class SourceCapabilities(
    // Basic capabilities
    val supportsLatest: Boolean = true,
    val supportsSearch: Boolean = true,
    val supportsFilters: Boolean = true,
    val supportsDeepLinks: Boolean = false,
    val supportsCommands: Boolean = false,
    
    // Command category support
    val supportsChapterCommands: Boolean = false,
    val supportsContentCommands: Boolean = false,
    val supportsAICommands: Boolean = false,
    val supportsAuthCommands: Boolean = false,
    val supportsBatchCommands: Boolean = false,
    val supportsTransformCommands: Boolean = false,
    val supportsCacheCommands: Boolean = false,
    val supportsSocialCommands: Boolean = false,
    val supportsMigrationCommands: Boolean = false,
    val supportsExploreCommands: Boolean = false,
    
    // Content type support
    val supportsImages: Boolean = false,
    val supportsVideo: Boolean = false,
    val supportsAudio: Boolean = false,
    
    // Premium/Auth features
    val requiresLogin: Boolean = false,
    val hasPremiumContent: Boolean = false,
    
    // Quality options
    val hasMultipleServers: Boolean = false,
    val hasQualityOptions: Boolean = false,
    val hasLanguageVariants: Boolean = false
)
