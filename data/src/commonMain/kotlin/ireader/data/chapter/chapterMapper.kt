package ireader.data.chapter

import ireader.domain.models.entities.Chapter
import ireader.core.source.model.Page

val chapterMapper = {_id: Long,
                     book_id: Long,
                     url: String,
                     name: String,
                     scanlator: String?,
                     read: Boolean,
                     bookmark: Boolean,
                     last_page_read: Long,
                     chapter_number: Float,
                     source_order: Long,
                     date_fetch: Long,
                     date_upload: Long,
                     content: List<Page>,
                     type: Long, ->
    Chapter(
        name = name,
        key = url,
        bookId = book_id,
        number = chapter_number,
        dateUpload = date_upload,
        translator = scanlator?:"",
        bookmark = bookmark,
        dateFetch = date_fetch,
        read = read,
        id = _id,
        lastPageRead = last_page_read,
        content = content,
        sourceOrder =  source_order,
        type = type,
    )
}

// Lightweight mapper without content field to prevent OOM errors
// Uses a special placeholder text to indicate downloaded status that can be detected by download service
val chapterMapperLight = {_id: Long,
                          book_id: Long,
                          url: String,
                          name: String,
                          scanlator: String?,
                          read: Boolean,
                          bookmark: Boolean,
                          last_page_read: Long,
                          chapter_number: Float,
                          source_order: Long,
                          date_fetch: Long,
                          date_upload: Long,
                          type: Long,
                          is_downloaded: Long, ->
    Chapter(
        name = name,
        key = url,
        bookId = book_id,
        number = chapter_number,
        dateUpload = date_upload,
        translator = scanlator?:"",
        bookmark = bookmark,
        dateFetch = date_fetch,
        read = read,
        id = _id,
        lastPageRead = last_page_read,
        // Use a placeholder with enough length (>= 50 chars) to indicate downloaded status
        // This allows download service to correctly skip already-downloaded chapters
        content = if (is_downloaded == 1L) listOf(ireader.core.source.model.Text(DOWNLOADED_CHAPTER_PLACEHOLDER)) else emptyList(),
        sourceOrder =  source_order,
        type = type,
    )
}

/**
 * Placeholder text used to indicate a chapter has been downloaded when using the light mapper.
 * This must be at least 50 characters to pass the download filter check.
 */
const val DOWNLOADED_CHAPTER_PLACEHOLDER = "[DOWNLOADED_CONTENT_PLACEHOLDER_DO_NOT_DISPLAY_THIS_TEXT_TO_USER]"