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
            (Long, Long, String, String?, String?, String?, List<String>?, String, Long, String?, String, Boolean, Long?, Long?, Boolean, Long, Long, Long, Long, Boolean, Long, Boolean, Long, Long, Long, Long, Long, Long?) -> LibraryBook =
    { _id, source, url, artist, author, description, genre, title,
        status, thumbnail_url, custom_cover, favorite, last_update,
      next_update, initialized, viewer, chapter_flags,
        cover_last_modified, date_added, is_pinned, pinned_order, is_archived,
        cached_unread_count, cached_read_count, cached_total_chapters, last_read_at, chapter_page, max ->
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
            this.unreadCount = cached_unread_count.toInt()
            this.readCount = cached_read_count.toInt()
            this.totalChapters = cached_total_chapters.toInt()
            this.lastRead = max?.toLong() ?: last_read_at
            this.isPinned = is_pinned
            this.pinnedOrder = pinned_order.toInt()
            this.isArchived = is_archived
            this.dateUpload = date_added // Used by RecentlyAdded smart category
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
                   is_archived: Boolean,
                   cached_unread_count: Long,
                   cached_read_count: Long,
                   cached_total_chapters: Long,
                   last_read_at: Long,
                   chapter_page: Long, ->
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
        chapterPage = chapter_page.toInt(),
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
                         cached_unread_count: Long,
                         cached_read_count: Long,
                         cached_total_chapters: Long,
                         last_read_at: Long,
                         chapter_page: Long,
                         category: Long,
                         unread_count: Long,
                         read_count: Long, ->

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
        this.totalChapters = cached_total_chapters.toInt()
        this.lastRead = last_read_at
        this.category = category.toInt()
        this.isPinned = is_pinned
        this.pinnedOrder = pinned_order.toInt()
        this.isArchived = is_archived
        this.dateUpload = date_added // Used by RecentlyAdded smart category
    }

}


/**
 * Fast mapper for getLibraryFast query - uses cached chapter counts from book table
 * No chapter table joins needed - counts are maintained by triggers
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
                              cached_unread_count: Long,
                              cached_read_count: Long,
                              cached_total_chapters: Long,
                              last_read_at: Long,
                              chapter_page: Long,
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
        // Use cached counts from book table (maintained by triggers)
        this.unreadCount = unread_count.toInt()
        this.readCount = read_count.toInt()
        this.totalChapters = cached_total_chapters.toInt()
        this.lastRead = last_read_at
        this.category = category.toInt()
        this.isPinned = is_pinned
        this.pinnedOrder = pinned_order.toInt()
        this.isArchived = is_archived
        this.dateUpload = date_added // Used by RecentlyAdded smart category
    }
}
