package org.ireader.core.utils

import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.takeIf
import org.ireader.image_loader.LibraryCovers
import java.util.*

fun updateBook(newBook: Book, oldBook: Book, libraryCovers: LibraryCovers): Book {
    if (newBook.cover.isNotBlank() && newBook.cover != oldBook.cover) {
        libraryCovers.invalidate(oldBook.id)
    }

    return Book(
        id = oldBook.id,
        sourceId = oldBook.sourceId,
        customCover = oldBook.customCover,
        flags = oldBook.flags,
        link = oldBook.link,
        dataAdded = oldBook.dataAdded,
        lastUpdated = Calendar.getInstance().timeInMillis,
        favorite = oldBook.favorite,
        title = newBook.title.takeIf(statement = {
            newBook.title.isNotBlank() && newBook.title != oldBook.title
        }, oldBook.title),
        status = if (newBook.status != 0) newBook.status else oldBook.status,
        genres = newBook.genres.ifEmpty { oldBook.genres },
        description = newBook.description.takeIf(statement = {
            newBook.description.isNotBlank() && newBook.description != oldBook.description
        },oldBook.description),
        author = newBook.author.takeIf(statement = {
            newBook.author.isNotBlank() && newBook.author != oldBook.author
        },oldBook.author),
        cover = newBook.cover.takeIf(statement = {
            newBook.cover.isNotBlank() && newBook.cover != oldBook.cover
        },oldBook.cover),
        viewer = if (newBook.viewer != 0) newBook.viewer else oldBook.viewer,
        tableId = oldBook.tableId,
    )
}