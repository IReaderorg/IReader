package ireader.data.history

import ireader.domain.models.entities.History
import ireader.domain.models.entities.HistoryWithRelations
import ireader.domain.models.BookCover

val historyMapper: (Long, Long, Long?, Long, Double?) -> History = { id, chapterId, readAt, readDuration, progress ->
  History(
    id = id,
    chapterId = chapterId,
    readAt = readAt,
    readDuration = readDuration,
    progress = progress?.toFloat() ?: 0.0F
  )
}

val historyWithRelationsMapper: (Long, Long, Long, String, String?, Long, Boolean, Long, Float, Long?, Long, Double?, String) -> HistoryWithRelations = {
    historyId, bookId, chapterId, title, thumbnailUrl, source, favorite, cover_last_modified,
    chapterNumber, readAt, readDuration, progress, chapterName ->
  HistoryWithRelations(
    id = historyId,
    chapterId = chapterId,
    bookId = bookId,
    title = title,
    chapterNumber = chapterNumber,
    readAt = readAt ?:0,
    readDuration = readDuration,
    coverData = BookCover(
      bookId = bookId,
      sourceId = source,
      favorite = favorite,
      cover = thumbnailUrl,
      lastModified = cover_last_modified,
    ),
    chapterName =chapterName,
  )
}
