package ireader.presentation.core.util

/**
 * Current battery charge as a percentage (0..100), or null when the platform
 * does not expose a battery level (desktop) or the reading is unavailable.
 *
 * @param platformContext Android Context on Android; unused on other platforms.
 */
expect fun currentBatteryLevel(platformContext: Any?): Int?

/** Footer text for the page slot: page / total (percent). */
fun formatPageProgress(currentPage: Int, totalPages: Int): String {
    val percent = if (totalPages > 0) {
        (currentPage.toFloat() / totalPages.toFloat() * 100).toInt()
    } else {
        0
    }
    return "$currentPage / $totalPages ($percent%)"
}

/** Footer text for the battery slot; empty when no battery reading exists. */
fun formatBatteryLevel(level: Int?): String = level?.let { "$it%" }.orEmpty()

/**
 * Battery icon fill level from 0 (empty) to 7 (full), or -1 when unavailable.
 * Used by the footer to pick one of Material's Battery0Bar..Battery6Bar /
 * BatteryFull icons.
 */
fun batteryBarLevel(level: Int?): Int = when {
    level == null -> -1
    level <= 0 -> 0
    level <= 14 -> 1
    level <= 28 -> 2
    level <= 42 -> 3
    level <= 56 -> 4
    level <= 70 -> 5
    level <= 84 -> 6
    else -> 7
}
