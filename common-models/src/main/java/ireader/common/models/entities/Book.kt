package ireader.common.models.entities


import kotlinx.serialization.Serializable
import ireader.core.api.source.model.MangaInfo

@Serializable
data class Book(
    override val id: Long = 0,
    override val sourceId: Long,
    override val title: String,
    val key: String,
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: Long = 0,
    override val cover: String = "",
    override val customCover: String = "",
    override val favorite: Boolean = false,
    val lastUpdate: Long = 0,
    val initialized: Boolean = false,
    val dateAdded: Long = 0,
    val viewer: Long = 0,
    val flags: Long = 0,
) : BaseBook {

    companion object {
        fun Book.toBookInfo(): MangaInfo {
            return MangaInfo(
                cover = this.cover,
                key = this.key,
                title = this.title,
                status = this.status,
                genres = this.genres,
                description = this.description,
                author = this.author,

            )
        }

        const val UNKNOWN = 0
        const val ONGOING = 1
        const val COMPLETED = 2
        const val LICENSED = 3
    }

    fun getStatusByName(): String {
        return when (status) {
            0L -> "UNKNOWN"
            1L -> "ONGOING"
            2L -> "COMPLETED"
            3L -> "LICENSED"
            else -> "UNKNOWN"
        }
    }
}

fun String.takeIf(statement: () -> Boolean, defaultValue: String): String {
    return if (!statement()) {
        defaultValue
    } else {
        this
    }
}

fun MangaInfo.toBook(sourceId: Long, bookId: Long = 0, lastUpdated: Long = 0): Book {
    return Book(
        id = bookId,
        sourceId = sourceId,
        customCover = this.cover,
        cover = this.cover,
        flags = 0,
        key = this.key,
        dateAdded = 0L,
        lastUpdate = lastUpdated,
        favorite = false,
        title = this.title,
        status = this.status,
        genres = this.genres,
        description = this.description,
        author = this.author,
    )
}

fun MangaInfo.fromBookInfo(sourceId: Long): Book {
    return Book(
        id = 0,
        sourceId = sourceId,
        customCover = this.cover,
        cover = this.cover,
        flags = 0,
        key = this.key,
        dateAdded = 0L,
        lastUpdate = 0L,
        favorite = false,
        title = this.title,
        status = this.status,
        genres = this.genres,
        description = this.description,
        author = this.author,
    )
}

data class BookWithInfo(
    val id: Long = 0,
    val title: String,
    val lastRead: Long = 0,
    val lastUpdated: Long = 0,
    val unread: Boolean = false,
    val totalChapters: Int = 0,
    val dateUpload: Long = 0,
    val dateFetch: Long = 0,
    val dataAdded: Long = 0,
    val sourceId: Long,
    val totalDownload: Int,
    val isRead: Int,
    val link: String,
    val status: Long = 0,
    val cover: String = "",
    val customCover: String = "",
    val favorite: Boolean = false,
    val tableId: Long = 0,
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val viewer: Int = 0,
    val flags: Int = 0,
)

fun BookWithInfo.toBook(): Book {
    return Book(
        id = this.id,
        sourceId = sourceId,
        customCover = this.cover,
        cover = this.cover,
        flags = 0,
        key = this.link,
        dateAdded = 0L,
        lastUpdate = 0L,
        favorite = false,
        title = this.title,
        status = this.status,
        genres = this.genres,
        description = this.description,
        author = this.author,
    )
}

interface BookBase {
    val id: Long
    val sourceId: Long
    val key: String
    val title: String
}

data class LibraryBook(
    override val id: Long,
    override val sourceId: Long,
    override val key: String,
    override val title: String,
    val status: Long,
    val cover: String,
    val lastUpdate: Long = 0,
) : BookBase {
    var unreadCount: Int = 0
    var readCount: Int = 0
    val totalChapters
        get() = readCount + unreadCount

    val hasStarted
        get() = readCount > 0
    var category: Int = 0

    fun toBookItem(): BookItem {
        return BookItem(
            id = id,
            sourceId = sourceId,
            title = title,
            cover = cover,
            unread = unreadCount,
            downloaded = readCount
        )
    }

    fun toBook(): Book {
        return Book(
            id = id,
            sourceId = sourceId,
            title = title,
            key = key,
        )
    }
}

interface BaseBook {
    val id: Long
    val sourceId: Long
    val title: String
    val favorite: Boolean
    val cover: String
    val customCover: String
}

data class BookItem(
    override val id: Long = 0,
    override val sourceId: Long,
    override val title: String,
    override val favorite: Boolean = false,
    override val cover: String = "",
    override val customCover: String = "",
    val key: String = "",
    val unread: Int? = null,
    val downloaded: Int? = null,
) : BaseBook


fun BookItem.toBook() : Book {
    return Book(
        id =  this.id,
        key = this.key,
        title = this.title ,
        favorite = this.favorite,
        sourceId = this.sourceId,
        cover = this.cover,
        customCover = this.customCover,

    )
}

fun Book.toBookItem() : BookItem {
    return BookItem(
        id =  this.id,
        key = this.key,
        title = this.title ,
        favorite = this.favorite,
        sourceId = this.sourceId,
        customCover = this.customCover,
        cover = this.cover,
    )
}

