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
                     content: String,
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
        content = chapterContentConvertor.decode(content),
        sourceOrder =  source_order,
        type = type,
    )
}