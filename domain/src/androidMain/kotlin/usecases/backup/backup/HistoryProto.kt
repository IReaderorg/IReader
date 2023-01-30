


package ireader.domain.usecases.backup.backup

import ireader.domain.models.entities.History
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
internal data class HistoryProto(
    @ProtoNumber(1) val bookId: Long,
    @ProtoNumber(2) val chapterId: Long,
    @ProtoNumber(3) val readAt: Long,
    @ProtoNumber(4) val progress: Long = 0,

) {

    fun toDomain(bookId: Long): History {
        return History(
            id = bookId,
            chapterId = chapterId,
            readAt = readAt,
            readDuration = progress,
        )
    }

    companion object {
        fun fromDomain(history: History): HistoryProto {
            return HistoryProto(
                bookId = history.id,
                chapterId = history.chapterId,
                readAt = history.readAt?:0,
                progress = history.readDuration,
            )
        }
    }
}


