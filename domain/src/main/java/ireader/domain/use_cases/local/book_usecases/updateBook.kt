package ireader.domain.use_cases.local.book_usecases

import ireader.common.models.entities.Book
import ireader.common.models.entities.takeIf
import java.net.URI
import java.net.URISyntaxException
import java.util.Calendar

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
        lastUpdate = Calendar.getInstance().timeInMillis,
        favorite = oldBook.favorite,
        title = newBook.title.takeIf(statement = {
            newBook.title.isNotBlank() && newBook.title != oldBook.title
        }, oldBook.title),
        status = if (newBook.status != 0) newBook.status else oldBook.status,
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
        viewer = if (newBook.viewer != 0) newBook.viewer else oldBook.viewer,
        tableId = oldBook.tableId,
        lastInit = oldBook.lastInit,
        type = oldBook.type
    )
}

fun getBaseUrl(url: String): String {
    return try {
        URI.create(url).toURL().let { key ->
            key.protocol + "://" + key.authority
        }
    } catch (e: Throwable) {
        ""
    }
}

fun getUrlWithoutDomain(orig: String): String {
    return try {
        val uri = URI(orig.replace(" ", "%20"))
        var out = uri.path
        if (uri.query != null) {
            out += "?" + uri.query
        }
        if (uri.fragment != null) {
            out += "#" + uri.fragment
        }
        out
    } catch (e: URISyntaxException) {
        orig
    }
}
