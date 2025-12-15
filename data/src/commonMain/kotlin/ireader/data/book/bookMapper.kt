package ireader.data.book

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.LibraryBook
import ireader.core.source.model.MangaInfo

val bookMapper = {
        _id: Long,
        sourceId: Long,
        title: String,
        key: String,
        author: String,
        description: String,
        genres: List<String>,
        status: Long,
        cover: String,
        custom_cover: String,
        favorite: Boolean,
        last_update: Long,
        initialized: Boolean,
        date_added: Long,
        viewer: Int,
        flags: Int ->
    Book(
        id=_id,
            sourceId, title, key, author, description, genres,
            status = status,
            cover = cover,
            customCover = custom_cover,
            favorite = favorite,
            lastUpdate=last_update,
            initialized = initialized,
            dateAdded = date_added,
            viewer = viewer.toLong(),
            flags = flags.toLong(),
    )
}

val mangaChapterMapper: (Long, Long, String, String?, String?, String?, List<String>?, String, Long, String?, Boolean, Long?, Long?, Boolean, Long, Long, Long, Long, Long, Long, String, String, String?, Boolean, Boolean, Long, Float, Long, Long, Long) -> Pair<Book, Chapter> =
    { _id, source, url, artist, author, description, genre, title, status, thumbnailUrl, favorite, lastUpdate, next_update, initialized, viewerFlags, chapterFlags, coverLastModified, dateAdded, chapterId, mangaId, chapterUrl, name, scanlator, read, bookmark, lastPageRead, chapterNumber, sourceOrder, dateFetch, dateUpload ->
        Book(
            id = _id,
            sourceId = source,
            favorite = favorite,
            lastUpdate = lastUpdate ?: 0,
            dateAdded = dateAdded,
            viewer = viewerFlags,
            flags = chapterFlags,
            key = url,
            title = title,
            author = author?: "",
            description = description?: "",
            genres = genre ?: emptyList(),
            status = status ?: MangaInfo.UNKNOWN,
            cover = thumbnailUrl ?:"",
            initialized = initialized,
        ) to Chapter(
            id = chapterId,
            bookId = mangaId,
            read = read,
            bookmark = bookmark,
            lastPageRead = lastPageRead,
            dateFetch = dateFetch,
            sourceOrder = sourceOrder,
            key = chapterUrl,
            name = name,
            dateUpload = dateUpload,
            number = chapterNumber,
            translator = scanlator ?:"",
        )
    }

val libraryManga:
            (Long, Long, String, String?, String?, String?, List<String>?, String, Long, String?, String, Boolean, Long?, Long?, Boolean, Long, Long, Long, Long, Boolean, Long, Boolean, Long?) -> LibraryBook =
    { _id, source, url, artist, author, description, genre, title,
        status, thumbnail_url, custom_cover, favorite, last_update,
      next_update, initialized, viewer, chapter_flags,
        cover_last_modified, date_added, is_pinned, pinned_order, is_archived, max ->
        LibraryBook(
            id = _id,
            sourceId = source,
            key = url,
            title = title,
            status = status,
            cover = thumbnail_url?:"",
            customCover = custom_cover,
            lastUpdate = last_update?:0L,
        ).apply {
            this.unreadCount = unreadCount.toInt()
            this.readCount = readCount.toInt()
            this.category = category.toInt()
            this.lastRead = max?.toLong()?:0L
            this.isPinned = is_pinned
            this.pinnedOrder = pinned_order.toInt()
            this.isArchived = is_archived
        }
    }

val booksMapper= { _id: Long,
                   source: Long,
                   url: String,
                   artist: String?,
                   author: String?,
                   description: String?,
                   genre: List<String>?,
                   title: String,
                   status: Long,
                   thumbnail_url: String?,
                   custom_cover: String,
                   favorite: Boolean,
                   last_update: Long?,
                   next_update: Long?,
                   initialized: Boolean,
                   viewer: Long,
                   chapter_flags: Long,
                   cover_last_modified: Long,
                   date_added: Long,
                   is_pinned: Boolean,
                   pinned_order: Long,
                   is_archived: Boolean, ->
    Book(
        id = _id,
        sourceId = source,
        favorite = favorite,
        lastUpdate = last_update ?: 0,
        dateAdded = date_added,
        viewer = viewer,
        flags = chapter_flags,
        key = url,
        title = title,
        author = author?: "",
        description = description?: "",
        genres = genre ?: emptyList(),
        status = status ?: MangaInfo.UNKNOWN,
        cover = thumbnail_url ?:"",
        customCover = custom_cover,
        initialized = initialized,
        isPinned = is_pinned,
        pinnedOrder = pinned_order.toInt(),
        isArchived = is_archived,
    )



}

val getLibraryMapper = { _id: Long,
                         source: Long,
                         url: String,
                         artist: String?,
                         author: String?,
                         description: String?,
                         genre: List<String>?,
                         title: String,
                         status: Long,
                         thumbnail_url: String?,
                         custom_cover: String,
                         favorite: Boolean,
                         last_update: Long?,
                         next_update: Long?,
                         initialized: Boolean,
                         viewer: Long,
                         chapter_flags: Long,
                         cover_last_modified: Long,
                         date_added: Long,
                         is_pinned: Boolean,
                         pinned_order: Long,
                         is_archived: Boolean,
                         unread_count: Long,
                         read_count: Long,
                         category: Long, ->

    LibraryBook(
        id = _id,
        sourceId = source,
        key = url,
        title = title,
        status = status,
        cover = thumbnail_url?:"",
        customCover = custom_cover,
        lastUpdate = last_update?:0L,
    ).apply {
        this.unreadCount = unread_count.toInt()
        this.readCount = read_count.toInt()
        this.category = category.toInt()
        this.isPinned = is_pinned
        this.pinnedOrder = pinned_order.toInt()
        this.isArchived = is_archived
    }

}


/**
 * Fast mapper for getLibraryFast query - doesn't include chapter counts
 * Chapter counts are loaded lazily to improve initial load time on low-end devices
 */
val getLibraryFastMapper = { _id: Long,
                              source: Long,
                              url: String,
                              artist: String?,
                              author: String?,
                              description: String?,
                              genre: List<String>?,
                              title: String,
                              status: Long,
                              thumbnail_url: String?,
                              custom_cover: String,
                              favorite: Boolean,
                              last_update: Long?,
                              next_update: Long?,
                              initialized: Boolean,
                              viewer: Long,
                              chapter_flags: Long,
                              cover_last_modified: Long,
                              date_added: Long,
                              is_pinned: Boolean,
                              pinned_order: Long,
                              is_archived: Boolean,
                              category: Long,
                              unread_count: Long,
                              read_count: Long, ->

    LibraryBook(
        id = _id,
        sourceId = source,
        key = url,
        title = title,
        status = status,
        cover = thumbnail_url ?: "",
        customCover = custom_cover,
        lastUpdate = last_update ?: 0L,
    ).apply {
        // Chapter counts are 0 from the fast query (hardcoded in SQL)
        this.unreadCount = unread_count.toInt()
        this.readCount = read_count.toInt()
        this.category = category.toInt()
        this.isPinned = is_pinned
        this.pinnedOrder = pinned_order.toInt()
        this.isArchived = is_archived
    }
}
