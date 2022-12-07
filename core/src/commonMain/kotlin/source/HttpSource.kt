package ireader.core.source

import androidx.annotation.Keep
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

/**
 * A simple implementation for sources from a website.
 */
@Suppress("unused", "unused_parameter")
@Keep
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

    /**
     * the commands that are used
     * to decrease the request to servers
     * @return [CommandList] check out [Command]
     */
    override fun getCommands(): CommandList {
        return emptyList()
    }
}
