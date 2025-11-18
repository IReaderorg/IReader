package ireader.domain.models.entities

import kotlinx.serialization.Serializable

data class Track(
    val id: Long = 0,
    val mangaId: Long,
    val siteId: Int,
    val entryId: Long,
    val mediaId: Long = 0,
    val mediaUrl: String = "",
    val title: String = "",
    val lastRead: Float = 0f,
    val totalChapters: Int = 0,
    val score: Float = 0f,
    val status: TrackStatus = TrackStatus.Reading,
    val startReadTime: Long = 0,
    val endReadTime: Long = 0
)
data class TrackState(
    val lastChapterRead: Float,
    val totalChapters: Int,
    val score: Float,
    val status: TrackStatus
)

@Serializable
data class TrackUpdate(
    val id: Long,
    val entryId: Long? = null,
    val mediaId: Long? = null,
    val mediaUrl: String? = null,
    val title: String? = null,
    val lastRead: Float? = null,
    val totalChapters: Int? = null,
    val score: Float? = null,
    val status: TrackStatus? = null,
    val startReadTime: Long? = null,
    val endReadTime: Long? = null
)
data class TrackSearchResult(
    val mediaId: Long,
    val mediaUrl: String,
    val title: String,
    val totalChapters: Int,
    val coverUrl: String,
    val summary: String,
    val publishingStatus: String,
    val publishingType: String,
    val startDate: String
)

enum class TrackStatus(val value: Int) {
    Reading(1),
    Completed(2),
    OnHold(3),
    Dropped(4),
    Planned(5),
    Repeating(6);

    companion object {
        fun from(value: Int): TrackStatus {
            return checkNotNull(values.find { it.value == value }) {
                "The provided value for TrackStatus doesn't exist"
            }
        }

        private val values = values()
    }
}
