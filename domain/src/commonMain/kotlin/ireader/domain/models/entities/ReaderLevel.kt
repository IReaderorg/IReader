package ireader.domain.models.entities

/**
 * Represents a reader's level based on total reading time.
 * Level = floor(total_reading_time_minutes / 60) + 1
 * XP = total_reading_time_minutes % 60 (progress to next level)
 */
data class ReaderLevel(
    val level: Int,
    val currentXp: Long,
    val xpToNextLevel: Long = 60,
    val totalMinutes: Long,
    val title: String
) {
    companion object {
        fun fromMinutes(totalMinutes: Long): ReaderLevel {
            val level = (totalMinutes / 60).toInt() + 1
            val currentXp = totalMinutes % 60
            val title = getLevelTitle(level)
            return ReaderLevel(
                level = level,
                currentXp = currentXp,
                xpToNextLevel = 60,
                totalMinutes = totalMinutes,
                title = title
            )
        }

        private fun getLevelTitle(level: Int): String {
            return when {
                level <= 5 -> "Novice Reader"
                level <= 15 -> "Curious Reader"
                level <= 30 -> "Avid Reader"
                level <= 50 -> "Bookworm"
                level <= 100 -> "Master Reader"
                level <= 200 -> "Literary Legend"
                else -> "Reading Deity"
            }
        }

        fun getLevelColor(level: Int): Long {
            return when {
                level <= 5 -> 0xFF9E9E9E // Gray
                level <= 15 -> 0xFF4CAF50 // Green
                level <= 30 -> 0xFF2196F3 // Blue
                level <= 50 -> 0xFF9C27B0 // Purple
                level <= 100 -> 0xFFFF9800 // Orange
                level <= 200 -> 0xFFFFD700 // Gold
                else -> 0xFFE91E63 // Pink/Diamond
            }
        }
    }

    val progress: Float
        get() = if (xpToNextLevel > 0) currentXp.toFloat() / xpToNextLevel.toFloat() else 0f

    val hoursRead: Long
        get() = totalMinutes / 60

    val minutesRead: Long
        get() = totalMinutes % 60
}
