package ireader.data.updates

import ireader.common.models.BookCover
import ireader.common.models.entities.UpdatesWithRelations

val updatesMapper = {
        mangaId: Long, mangaTitle: String, chapterId: Long, chapterName: String, scanlator: String?, read: Boolean, bookmark: Boolean, source: Long, favorite: Boolean, thumbnailUrl: String?, coverLastModified: Long, dateUpload: Long, datefetch: Long,downloaded:Boolean ->

    UpdatesWithRelations(
        bookId = mangaId,
        bookTitle = mangaTitle,
        chapterId = chapterId,
        chapterName = chapterName,
        scanlator = scanlator,
        read = read,
        bookmark = bookmark,
        sourceId = source,
        dateFetch = datefetch,
        coverData = BookCover(
            bookId = mangaId,
            sourceId = source,
            favorite = favorite,
            cover = thumbnailUrl,
            lastModified = coverLastModified,
        ),
         downloaded = downloaded
    )
}