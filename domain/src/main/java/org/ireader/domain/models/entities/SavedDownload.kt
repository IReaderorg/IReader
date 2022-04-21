package org.ireader.domain.models.entities

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ireader.core.utils.Constants.DOWNLOAD_TABLE

@Entity(tableName = DOWNLOAD_TABLE)

data class SavedDownload(
    @PrimaryKey(autoGenerate = false)
    val chapterId: Long,
    val bookId: Long,
    val sourceId: Long,
    val priority: Int = 0,
    val bookName: String,
    val chapterKey: String,
    val chapterName: String,
    val translator: String,
)


data class SavedDownloadWithInfo(
    @PrimaryKey(autoGenerate = false)
    val chapterId: Long,
    val bookId: Long,
    val sourceId: Long,
    val priority: Int = 0,
    val bookName: String,
    val chapterKey: String,
    val chapterName: String,
    val translator: String,
    val isDownloaded:Boolean
)

fun SavedDownloadWithInfo.toSavedDownload() :SavedDownload {
    return SavedDownload(
        bookId = bookId,
        priority = 1,
        chapterName = chapterName,
        chapterKey = chapterKey,
        translator = translator,
        chapterId = chapterId,
        bookName = bookName,
        sourceId = sourceId,
    )
}



fun buildSavedDownload(book:Book,chapter:Chapter) : SavedDownload {
    return SavedDownload(
        bookId = book.id,
        priority = 1,
        chapterName = chapter.title,
        chapterKey = chapter.link,
        translator = chapter.translator,
        chapterId = chapter.id,
        bookName = book.title,
        sourceId = book.sourceId,
    )
}

