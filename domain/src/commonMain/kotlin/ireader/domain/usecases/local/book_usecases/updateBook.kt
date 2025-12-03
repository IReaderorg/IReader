package ireader.domain.usecases.local.book_usecases

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.takeIf
import ireader.domain.utils.extensions.currentTimeToLong
import io.ktor.http.Url

fun updateBook(newBook: Book, oldBook: Book): Book {
    val newKey = if (newBook.key.isNotBlank() && newBook.key != getBaseUrl(newBook.key)) {
        newBook.key
    } else {
        oldBook.key
    }
    return Book(
        id = oldBook.id,
        sourceId = oldBook.sourceId,
        customCover = oldBook.customCover,
        flags = oldBook.flags,
        key = newKey,
        dateAdded = oldBook.dateAdded,
        lastUpdate = currentTimeToLong(),
        favorite = oldBook.favorite,
        title = newBook.title.takeIf(statement = {
            newBook.title.isNotBlank() && newBook.title != oldBook.title
        }, oldBook.title),
        status = if (newBook.status != 0L) newBook.status else oldBook.status,
        genres = newBook.genres.ifEmpty { oldBook.genres },
        description = newBook.description.takeIf(statement = {
            newBook.description.isNotBlank() && newBook.description != oldBook.description
        }, oldBook.description),
        author = newBook.author.takeIf(statement = {
            newBook.author.isNotBlank() && newBook.author != oldBook.author
        }, oldBook.author),
        cover = newBook.cover.takeIf(statement = {
            newBook.cover.isNotBlank() && newBook.cover != oldBook.cover
        }, oldBook.cover),
        viewer = if (newBook.viewer != 0L) newBook.viewer else oldBook.viewer,
        initialized = oldBook.initialized,
    )
}

/**
 * Extract base URL using Ktor's Url class for KMP compatibility.
 */
fun getBaseUrl(url: String): String {
    return try {
        val parsed = Url(url)
        "${parsed.protocol.name}://${parsed.host}${if (parsed.port != parsed.protocol.defaultPort) ":${parsed.port}" else ""}"
    } catch (e: Throwable) {
        ""
    }
}

/**
 * Extract path and query from URL using Ktor's Url class for KMP compatibility.
 */
fun getUrlWithoutDomain(orig: String): String {
    return try {
        val url = Url(orig.replace(" ", "%20"))
        buildString {
            append(url.encodedPath)
            if (url.encodedQuery.isNotEmpty()) {
                append("?")
                append(url.encodedQuery)
            }
            if (url.fragment.isNotEmpty()) {
                append("#")
                append(url.fragment)
            }
        }
    } catch (e: Throwable) {
        orig
    }
}
