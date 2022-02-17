package org.ireader.domain.models.source

import org.ireader.domain.models.entities.Book
import org.ireader.source.models.BookInfo

fun BookInfo.fromBookInfo(sourceId: Long): Book {
    return Book(
        id = 0,
        sourceId = sourceId,
        customCover = this.cover,
        cover = this.cover,
        flags = 0,
        link = this.link,
        lastRead = 0,
        dataAdded = 0L,
        lastUpdated = 0L,
        favorite = false,
        title = this.title,
        translator = this.translator,
        status = this.status,
        genres = this.genres,
        description = this.description,
        author = this.author,
        rating = this.rating,
        viewer = this.viewer
    )
}