package org.ireader.domain.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.ireader.core.utils.Constants.BOOK_TABLE

@Serializable
@Entity(tableName = BOOK_TABLE)
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceId: Long,
    val link: String,
    val title: String,
    val translator: String = "",
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: Int = 0,
    val cover: String = "",
    val customCover: String = "",
    val favorite: Boolean = false,
    val rating: Int = 0,
    val lastUpdated: Long = 0,
    val lastRead: Long = 0,
    val dataAdded: Long = 0,
    val viewer: Int = 0,
    val flags: Int = 0,
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

