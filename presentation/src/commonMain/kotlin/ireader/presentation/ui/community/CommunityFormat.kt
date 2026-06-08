package ireader.presentation.ui.community

/**
 * Compact reading-time formatter that stays readable at any scale.
 *  45      -> "45m"
 *  90      -> "1h 30m"
 *  1500    -> "25h"
 *  61_000  -> "1,016h"  (≈ "1k h" at large scale)
 */
fun formatReadingTimeCompact(minutes: Long): String {
    if (minutes < 60) return "${minutes}m"
    val hours = minutes / 60
    return when {
        hours < 10 -> {
            val m = minutes % 60
            if (m == 0L) "${hours}h" else "${hours}h ${m}m"
        }
        hours < 1000 -> "${hours}h"
        else -> {
            // 1,234h with thousands separators
            val s = hours.toString()
            val sb = StringBuilder()
            for ((i, c) in s.withIndex()) {
                if (i > 0 && (s.length - i) % 3 == 0) sb.append(',')
                sb.append(c)
            }
            "${sb}h"
        }
    }
}

/** Larger reader/online counts: 2341 -> "2.3k", 1_200_000 -> "1.2M". */
fun formatCount(n: Int): String = when {
    n >= 1_000_000 -> "${(n / 100_000) / 10.0}M"
    n >= 1_000 -> "${(n / 100) / 10.0}k"
    else -> "$n"
}

/** Normalize a title for dedup: lowercase, strip punctuation/whitespace. */
fun normalizeTitle(title: String): String =
    title.lowercase().filter { it.isLetterOrDigit() }
