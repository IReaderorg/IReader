@file:OptIn(ExperimentalSerializationApi::class)

package org.ireader.domain.use_cases.backup.backup

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import org.ireader.common_models.entities.History

@Serializable
internal data class HistoryProto(
    @ProtoNumber(1) val bookId: Long,
    @ProtoNumber(2) val chapterId: Long,
    @ProtoNumber(3) val readAt: Long,
    @ProtoNumber(4) val progress: Int = 0,

) {

    fun toDomain(bookId: Long): History {
        return History(
            bookId = bookId,
            chapterId = chapterId,
            readAt = readAt,
            progress = progress
        )
    }

    companion object {
        fun fromDomain(history: History): HistoryProto {
            return HistoryProto(
                bookId = history.bookId,
                chapterId = history.chapterId,
                readAt = history.readAt,
                progress = history.progress,
            )
        }
    }
}
