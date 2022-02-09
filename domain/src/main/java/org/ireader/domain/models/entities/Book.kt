package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ireader.core.utils.Constants.BOOK_TABLE

@Serializable
@Entity(tableName = BOOK_TABLE)
data class Book(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var link: String,
    var bookName: String,
    var coverLink: String? = null,
    var description: List<String> = emptyList(),
    var author: String? = null,
    var translator: String? = null,
    var category: List<String> = emptyList(),
    var status: Int = -1,
    var rating: Int = 0,
    var sourceId: Long = 0,
    var isExploreMode: Boolean = false,
    var inLibrary: Boolean = false,
    var dataAdded: Long = 0,
    var isDownloaded: Boolean = false,
    var beingDownloaded: Boolean = false,
    var lastRead: Long = 0,
    var totalChapters: Int = 0,
    var unread: Boolean = true,
    var lastUpdated: Long = -1,
    var completed: Boolean = false,
    var areChaptersReversed: Boolean = true,
    var type: Int = -1,
    var lastChecked: Long = 0,
    var latestChapter: Long = 0,
    var dataFetched: Long = 0,
) {
    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
        fun create(): Book {
            return Book(bookName = "", link = "")
        }
    }

    fun getStatusByName(): String {
        return when (status) {
            0 -> "UNKNOWN"
            1 -> "ONGOING"
            2 -> "COMPLETED"
            3 -> "LICENSED"
            else -> "UNKNOWN"
        }
    }

}
