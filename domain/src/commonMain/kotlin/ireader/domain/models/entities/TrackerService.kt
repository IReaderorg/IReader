package ireader.domain.models.entities

import kotlinx.serialization.Serializable

/**
 * Represents an external tracking service
 */
@Serializable
data class TrackerService(
    val id: Int,
    val name: String,
    val icon: String,
    val baseUrl: String,
    val isEnabled: Boolean = true,
    val requiresAuthentication: Boolean = true,
    val supportsScoring: Boolean = true,
    val supportsStatus: Boolean = true,
    val supportsProgress: Boolean = true,
    val supportsDates: Boolean = true
) {
    companion object {
        const val MYANIMELIST = 1
        const val ANILIST = 2
        const val KITSU = 3
        const val MANGAUPDATES = 4
        const val SHIKIMORI = 5
        const val BANGUMI = 6
        const val MYNOVELLIST = 7
        
        val services = listOf(
            TrackerService(
                id = MYANIMELIST,
                name = "MyAnimeList",
                icon = "mal",
                baseUrl = "https://myanimelist.net"
            ),
            TrackerService(
                id = ANILIST,
                name = "AniList",
                icon = "anilist",
                baseUrl = "https://anilist.co"
            ),
            TrackerService(
                id = KITSU,
                name = "Kitsu",
                icon = "kitsu",
                baseUrl = "https://kitsu.io"
            ),
            TrackerService(
                id = MANGAUPDATES,
                name = "MangaUpdates",
                icon = "mangaupdates",
                baseUrl = "https://mangaupdates.com"
            ),
            TrackerService(
                id = SHIKIMORI,
                name = "Shikimori",
                icon = "shikimori",
                baseUrl = "https://shikimori.one"
            ),
            TrackerService(
                id = BANGUMI,
                name = "Bangumi",
                icon = "bangumi",
                baseUrl = "https://bangumi.tv"
            ),
            TrackerService(
                id = MYNOVELLIST,
                name = "MyNovelList",
                icon = "mynovellist",
                baseUrl = "https://mynoveltracker.netlify.app"
            )
        )
    }
}

/**
 * Authentication credentials for tracking services
 */
@Serializable
data class TrackerCredentials(
    val serviceId: Int,
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAt: Long = 0,
    val username: String = "",
    val isValid: Boolean = true
)

/**
 * Tracking synchronization status
 */
@Serializable
data class TrackingSyncStatus(
    val bookId: Long,
    val serviceId: Int,
    val lastSyncTime: Long,
    val syncStatus: SyncStatus,
    val errorMessage: String? = null,
    val pendingUpdates: List<TrackUpdate> = emptyList()
)

enum class SyncStatus {
    SYNCED,
    PENDING,
    ERROR,
    DISABLED
}

/**
 * Batch tracking operation
 */
data class BatchTrackingOperation(
    val bookIds: List<Long>,
    val operation: TrackingOperation,
    val serviceIds: List<Int> = emptyList() // Empty means all enabled services
)

enum class TrackingOperation {
    SYNC_PROGRESS,
    UPDATE_STATUS,
    UPDATE_SCORE,
    REMOVE_TRACKING
}