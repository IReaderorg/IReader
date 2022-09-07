@file:OptIn(ExperimentalSerializationApi::class)

package ireader.domain.use_cases.backup.backup

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import ireader.common.models.entities.Chapter
import ireader.core.api.source.model.Page

@Serializable
internal data class ChapterProto(
    @ProtoNumber(1) val key: String,
    @ProtoNumber(2) val name: String,
    @ProtoNumber(3) val translator: String = "",
    @ProtoNumber(4) val read: Boolean = false,
    @ProtoNumber(5) val bookmark: Boolean = false,
    @ProtoNumber(6) val dateFetch: Long = 0,
    @ProtoNumber(7) val dateUpload: Long = 0,
    @ProtoNumber(8) val number: Float = 0f,
    @ProtoNumber(9) val sourceOrder: Int = 0,
    @ProtoNumber(10) val content: List<Page> = emptyList(),

) {

    fun toDomain(mangaId: Long): Chapter {
        return Chapter(
            bookId = mangaId,
            key = key,
            name = name,
            translator = translator,
            read = read,
            bookmark = bookmark,
            dateFetch = dateFetch,
            dateUpload = dateUpload,
            number = number,
            sourceOrder = sourceOrder,
            content = content,
        )
    }

    companion object {
        fun fromDomain(chapter: Chapter): ChapterProto {
            return ChapterProto(
                key = chapter.key,
                name = chapter.name,
                translator = chapter.translator,
                read = chapter.read,
                bookmark = chapter.bookmark,
                dateFetch = chapter.dateFetch,
                dateUpload = chapter.dateUpload,
                number = chapter.number,
                sourceOrder = chapter.sourceOrder,
                content = chapter.content,
            )
        }
    }
}
