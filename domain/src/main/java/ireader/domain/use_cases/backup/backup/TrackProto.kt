@file:OptIn(ExperimentalSerializationApi::class)

package ireader.domain.use_cases.backup.backup

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import ireader.common.models.entities.Track
import ireader.common.models.entities.TrackStatus

@Serializable
internal data class TrackProto(
    @ProtoNumber(1) val siteId: Int,
    @ProtoNumber(2) val entryId: Long,
    @ProtoNumber(3) val mediaId: Long = 0,
    @ProtoNumber(4) val mediaUrl: String = "",
    @ProtoNumber(5) val title: String = "",
    @ProtoNumber(6) val lastRead: Float = 0f,
    @ProtoNumber(7) val totalChapters: Int = 0,
    @ProtoNumber(8) val score: Float = 0f,
    @ProtoNumber(9) val status: Int = 0,
    @ProtoNumber(10) val startReadTime: Long = 0,
    @ProtoNumber(11) val endReadTime: Long = 0
) {

    fun toDomain(mangaId: Long): Track {
        return Track(
            mangaId = mangaId,
            siteId = siteId,
            entryId = entryId,
            mediaId = mediaId,
            mediaUrl = mediaUrl,
            title = title,
            lastRead = lastRead,
            totalChapters = totalChapters,
            score = score,
            status = TrackStatus.from(status),
            startReadTime = startReadTime,
            endReadTime = endReadTime
        )
    }

    companion object {
        fun fromDomain(track: Track): TrackProto {
            return TrackProto(
                siteId = track.siteId,
                entryId = track.entryId,
                mediaId = track.mediaId,
                mediaUrl = track.mediaUrl,
                title = track.title,
                lastRead = track.lastRead,
                totalChapters = track.totalChapters,
                score = track.score,
                status = track.status.value,
                startReadTime = track.startReadTime,
                endReadTime = track.endReadTime
            )
        }
    }
}
