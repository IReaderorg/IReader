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

val historyWithRelationsMapper: (Long, Long, Long, String, String?, String, Long, Boolean, Long, Float, Long?, Long, Double?, String) -> HistoryWithRelations = {
    historyId, bookId, chapterId, title, thumbnailUrl, customCover, source, favorite, cover_last_modified,
    chapterNumber, readAt, readDuration, progress, chapterName ->
  // Use customCover if set, otherwise fall back to thumbnailUrl
  val effectiveCover = if (customCover.isNotBlank() && customCover != thumbnailUrl) customCover else thumbnailUrl
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
      cover = effectiveCover,
      lastModified = cover_last_modified,
      hasCustomCover = customCover.isNotBlank() && customCover != thumbnailUrl,
    ),
    chapterName =chapterName,
  )
}
