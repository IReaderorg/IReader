package org.ireader.core.utils

import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.takeIf
import java.net.URI
import java.net.URISyntaxException
import java.util.Calendar

fun updateBook(newBook: Book, oldBook: Book): Book {
    return Book(
        id = oldBook.id,
        sourceId = oldBook.sourceId,
        customCover = oldBook.customCover,
        flags = oldBook.flags,
        key = newBook.key.ifBlank { oldBook.key },
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
    )
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