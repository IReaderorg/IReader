package ireader.data.updates


import ireader.domain.models.entities.UpdatesWithRelations
import ireader.domain.models.BookCover

val updatesMapper = {
        mangaId: Long,
        mangaTitle: String,
        chapterId: Long,
        chapterName: String,
        scanlator: String?,
        read: Boolean,
        bookmark: Boolean,
        source: Long,
        favorite: Boolean,
        thumbnailUrl: String?,
        customCover: String,
        coverLastModified: Long,
        dateUpload: Long,
        datefetch: Long,
        downlaoded: Boolean,
        readingProgress: Double?,
        lastReadAt: Long?, ->

    // Use customCover if set, otherwise fall back to thumbnailUrl
    val effectiveCover = if (customCover.isNotBlank() && customCover != thumbnailUrl) customCover else thumbnailUrl
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
            cover = effectiveCover,
            lastModified = coverLastModified,
            hasCustomCover = customCover.isNotBlank() && customCover != thumbnailUrl,
        ),
         downloaded = downlaoded
    )
}