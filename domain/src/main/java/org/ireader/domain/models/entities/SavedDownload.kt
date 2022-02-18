package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.ireader.core.utils.Constants

@Entity(tableName = Constants.DOWNLOAD_TABLE)
data class SavedDownload(
    @PrimaryKey(autoGenerate = false)
    val bookId: Long,
    val chapterId: Long,
    val sourceId: Long,
    val priority: Int = 0,
    val totalChapter: Int,
    val progress: Int,
    val bookName: String,
    val chapterKey: String,
    val chapterName: String,
    val translator: String,
)