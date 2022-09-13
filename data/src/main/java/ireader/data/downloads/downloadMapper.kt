package ireader.data.downloads

import ireader.common.models.entities.SavedDownload
import ireader.common.models.entities.SavedDownloadWithInfo

val downloadMapper = {  chapterId: Long, bookId: Long, priority: Int, _id: Long, title: String, url: String, name: String, scanlator: String?, source: Long,is_downloaded: Boolean ->
    SavedDownloadWithInfo(
        chapterId,
        bookId,
        priority.toInt(),
        _id,
        sourceId = source,
        bookName = title,
        url,
        name,
        scanlator?:"",
        isDownloaded = is_downloaded
    )
}