package ireader.data.history

import ireader.domain.models.entities.History
import ireader.domain.models.entities.HistoryWithRelations
import ireader.domain.models.BookCover

val historyMapper: (Long, Long, Long?, Long) -> History = { id, chapterId, readAt, readDuration ->
  History(
    id = id,
    chapterId = chapterId,
    readAt = readAt,
    readDuration = readDuration,
  )
}

val historyWithRelationsMapper: (Long, Long, Long, String, String?, Long, Boolean, Long, Float, Long?, Long, String) -> HistoryWithRelations = {
    historyId, mangaId, chapterId, title, thumbnailUrl, sourceId, isFavorite, coverLastModified, chapterNumber, readAt, readDuration, chapterName ->
  HistoryWithRelations(
    id = historyId,
    chapterId = chapterId,
    bookId = mangaId,
    title = title,
    chapterNumber = chapterNumber,
    readAt = readAt,
    readDuration = readDuration,
    coverData = BookCover(
      bookId = mangaId,
      sourceId = sourceId,
      favorite = isFavorite,
      cover = thumbnailUrl,
      lastModified = coverLastModified,
    ),
    chapterName =chapterName,
  )
}
