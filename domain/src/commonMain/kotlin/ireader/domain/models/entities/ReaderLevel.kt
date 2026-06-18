package ireader.domain.models.entities

/**
 * Represents a reader's level based on total reading time.
 *
 * Level thresholds (total hours required to REACH that level):
 *   1: 0h        2: 2h       3: 10h      4: 30h      5: 100h
 *   6: 1000h     7: 2500h    8: 5000h    9: 10000h   10: 20000h
 *   11: 40000h   12: 70000h  13: 110000h 14: 160000h 15: 220000h
 *   16: 300000h  17: 400000h 18: 520000h 19: 660000h 20: 820000h
 *
 * Easy early progression (1-5), then a steep wall from level 6 onward.
 */
data class ReaderLevel(
    val level: Int,
    val currentXp: Long,
    val xpToNextLevel: Long,
    val totalMinutes: Long,
    val title: String
) {
    val progress: Float
        get() = if (xpToNextLevel > 0) currentXp.toFloat() / xpToNextLevel.toFloat() else 0f

    val hoursRead: Long
        get() = totalMinutes / 60

    val minutesRead: Long
        get() = totalMinutes % 60

    companion object {
        // Total hours required to REACH each level (index = level - 1)
        private val LEVEL_THRESHOLDS_HOURS = longArrayOf(
            0,         // Level 1
            2,         // Level 2
            10,        // Level 3
            30,        // Level 4
            100,       // Level 5
            1000,      // Level 6
            2500,      // Level 7
            5000,      // Level 8
            10000,     // Level 9
            20000,     // Level 10
            40000,     // Level 11
            70000,     // Level 12
            110000,    // Level 13
            160000,    // Level 14
            220000,    // Level 15
            300000,    // Level 16
            400000,    // Level 17
            520000,    // Level 18
            660000,    // Level 19
            820000,    // Level 20
        )

        private const val MAX_LEVEL = 20

        fun fromMinutes(totalMinutes: Long): ReaderLevel {
            val totalHours = totalMinutes / 60.0

            // Find current level (highest threshold <= totalHours)
            var level = 1
            for (i in LEVEL_THRESHOLDS_HOURS.indices.reversed()) {
                if (totalHours >= LEVEL_THRESHOLDS_HOURS[i]) {
                    level = i + 1
                    break
                }
            }

            // XP within current level: hours spent in this level
            val levelStartHours = LEVEL_THRESHOLDS_HOURS[(level - 1).coerceAtMost(LEVEL_THRESHOLDS_HOURS.size - 1)]
            val nextLevelHours = if (level < MAX_LEVEL) {
                LEVEL_THRESHOLDS_HOURS[level]
            } else {
                // Beyond max level: keep scaling up
                LEVEL_THRESHOLDS_HOURS[MAX_LEVEL - 1] + (level - MAX_LEVEL + 1) * 200000L
            }
            val xpInLevelHours = (totalHours - levelStartHours).toLong().coerceAtLeast(0)
            val xpToNextHours = (nextLevelHours - levelStartHours).coerceAtLeast(1)

            return ReaderLevel(
                level = level,
                currentXp = xpInLevelHours,
                xpToNextLevel = xpToNextHours,
                totalMinutes = totalMinutes,
                title = getLevelTitle(level)
            )
        }

        private fun getLevelTitle(level: Int): String {
            return when {
                level <= 2 -> "Novice Reader"
                level <= 5 -> "Curious Reader"
                level <= 8 -> "Avid Reader"
                level <= 11 -> "Bookworm"
                level <= 14 -> "Master Reader"
                level <= 17 -> "Literary Legend"
                else -> "Reading Deity"
            }
        }

        fun getLevelColor(level: Int): Long {
            return when {
                level <= 2 -> 0xFF9E9E9E // Gray
                level <= 5 -> 0xFF4CAF50 // Green
                level <= 8 -> 0xFF2196F3 // Blue
                level <= 11 -> 0xFF9C27B0 // Purple
                level <= 14 -> 0xFFFF9800 // Orange
                level <= 17 -> 0xFFFFD700 // Gold
                else -> 0xFFE91E63 // Pink/Diamond
            }
        }
    }
}
