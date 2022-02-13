package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ireader.core.utils.Constants.BOOK_TABLE

@Serializable
@Entity(tableName = BOOK_TABLE)
data class Book(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var sourceId: Long,
    var link: String,
    var title: String,
    var translator: String = "",
    var author: String = "",
    var description: String = "",
    var genres: List<String> = emptyList(),
    var status: Int = 0,
    var cover: String = "",
    var customCover: String = "",
    val exploreMode: Boolean = false,
    var inLibrary: Boolean = false,
    var rating: Int = 0,
    var lastUpdated: Long = 0,
    var lastRead: Long = 0,
    var dataAdded: Long = 0,
    var viewer: Int = 0,
    var flags: Int = 0,
) {

    companion object {
        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
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

