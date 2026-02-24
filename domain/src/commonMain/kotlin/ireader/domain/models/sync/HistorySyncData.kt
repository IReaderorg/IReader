package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Represents history/reading progress data for synchronization.
 *
 * @property chapterGlobalId Global ID of the chapter (sourceId + "|" + chapterKey)
 * @property lastRead Timestamp when this chapter was last read
 * @property timeRead Total time spent reading (milliseconds)
 * @property readingProgress Reading progress as a percentage (0.0 to 1.0)
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class HistorySyncData(
    val chapterGlobalId: String,
    val lastRead: Long,
    val timeRead: Long,
    val readingProgress: Double
) {
    init {
        require(chapterGlobalId.isNotBlank()) { "Chapter global ID cannot be empty or blank" }
        require(lastRead >= 0) { "Last read timestamp cannot be negative, got: $lastRead" }
        require(timeRead >= 0) { "Time read cannot be negative, got: $timeRead" }
        require(readingProgress in 0.0..1.0) { "Reading progress must be between 0.0 and 1.0, got: $readingProgress" }
    }
}
