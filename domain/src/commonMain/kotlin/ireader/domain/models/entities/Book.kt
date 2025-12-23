package ireader.domain.models.entities


import ireader.core.source.model.MangaInfo
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

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
    val isPinned: Boolean = false,
    val pinnedOrder: Int = 0,
    val isArchived: Boolean = false,
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
    fun allStatus() : List<String> {
        return listOf(
            "UNKNOWN",
            "ONGOING",
            "COMPLETED",
            "LICENSED",
        )
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

@OptIn(ExperimentalTime::class)
fun MangaInfo.toBook(sourceId: Long, bookId: Long = 0, lastUpdated: Long = 0): Book {
    return Book(
        id = bookId,
        sourceId = sourceId,
        customCover = this.cover,
        cover = this.cover,
        flags = 0,
        key = this.key,
        dateAdded = kotlin.time.Clock.System.now().toEpochMilliseconds(),
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
    val customCover: String = "",
    val lastUpdate: Long = 0,
) : BookBase {
    var dateFetched: Long = 0
    var dateUpload: Long = 0
    var unreadCount: Int = 0
    var readCount: Int = 0
    // Cached total chapters from database (maintained by triggers)
    // Falls back to computed value if not set
    private var _totalChapters: Int = -1
    var totalChapters: Int
        get() = if (_totalChapters >= 0) _totalChapters else readCount + unreadCount
        set(value) { _totalChapters = value }

    val hasStarted
        get() = readCount > 0
    var category: Int = 0
    var lastRead: Long = 0
    var isPinned: Boolean = false
    var pinnedOrder: Int = 0
    var isArchived: Boolean = false
    
    /** Returns the effective cover URL, prioritizing customCover if set */
    val effectiveCover: String
        get() = if (customCover.isNotBlank() && customCover != cover) customCover else cover

    fun toBookItem(): BookItem {
        return BookItem(
            id = id,
            sourceId = sourceId,
            title = title,
            cover = cover,
            unread = unreadCount,
            downloaded = readCount,
            key = key,
            customCover = customCover,
            lastRead = lastRead,
            totalChapters = totalChapters,
            favorite = hasStarted,
            isPinned = isPinned,
            pinnedOrder = pinnedOrder,
            isArchived = isArchived,
        )
    }

    fun toBook(): Book {
        return Book(
            id = id,
            sourceId = sourceId,
            title = title,
            key = key,
            customCover = customCover,
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
    val column: Long = 0,
    override val id: Long = 0,
    override val sourceId: Long,
    override val title: String,
    override val favorite: Boolean = false,
    override val cover: String = "",
    override val customCover: String = "",
    val key: String = "",
    val unread: Int? = null,
    val downloaded: Int? = null,
    val author: String = "",
    val totalChapters : Int = 0,
    val lastRead: Long = 0,
    val description: String = "",
    val progress: Float? = 0.0f,
    val isPinned: Boolean = false,
    val pinnedOrder: Int = 0,
    val isArchived: Boolean = false
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
        author = this.author


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
        author = this.author,
        description = this.description,

    )
}

