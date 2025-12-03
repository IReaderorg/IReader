package ireader.presentation.ui.core.utils

import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * KMP-compatible string formatting utilities.
 * These replace JVM-only String.format() calls for cross-platform compatibility.
 */

/**
 * Format a Double to a string with specified decimal places.
 * Example: 3.14159.formatDecimal(2) -> "3.14"
 */
fun Double.formatDecimal(decimals: Int): String {
    if (decimals < 0) return this.toString()
    if (decimals == 0) return this.roundToInt().toString()
    
    val factor = 10.0.pow(decimals)
    val rounded = (this * factor).roundToInt() / factor
    
    val parts = rounded.toString().split(".")
    val intPart = parts[0]
    val decPart = if (parts.size > 1) parts[1] else ""
    
    return "$intPart.${decPart.padEnd(decimals, '0').take(decimals)}"
}

/**
 * Format a Float to a string with specified decimal places.
 * Example: 3.14159f.formatDecimal(2) -> "3.14"
 */
fun Float.formatDecimal(decimals: Int): String = this.toDouble().formatDecimal(decimals)

/**
 * Top-level function to format a number with specified decimal places.
 * Use this when you can't use extension function syntax.
 * Example: toDecimalString(3.14159, 2) -> "3.14"
 */
fun toDecimalString(value: Double, decimals: Int): String = value.formatDecimal(decimals)

/**
 * Top-level function to format a number with specified decimal places.
 * Use this when you can't use extension function syntax.
 * Example: toDecimalString(3.14159f, 2) -> "3.14"
 */
fun toDecimalString(value: Float, decimals: Int): String = value.toDouble().formatDecimal(decimals)

/**
 * Format rating with review count.
 * Example: formatRatingWithReviews(4.5f, 123) -> "4.5 (123 reviews)"
 */
fun formatRatingWithReviews(rating: Float, reviewCount: Int): String {
    return "${rating.formatDecimal(1)} ($reviewCount reviews)"
}

/**
 * Format rating with count (short form).
 * Example: formatRatingShort(4.5f, 123) -> "4.5 (123)"
 */
fun formatRatingShort(rating: Float, count: Int): String {
    return "${rating.formatDecimal(1)} ($count)"
}

/**
 * Format currency amount.
 * Example: formatCurrency("USD", 19.99) -> "USD 19.99"
 */
fun formatCurrency(currency: String, amount: Double): String {
    return "$currency ${amount.formatDecimal(2)}"
}

/**
 * Format price with currency symbol.
 * Example: formatPrice(19.99, "$") -> "$19.99"
 */
fun formatPrice(price: Double, symbol: String = "$"): String {
    return "$symbol${price.formatDecimal(2)}"
}

/**
 * Format large numbers with K/M suffix.
 * Example: formatCompactNumber(1500) -> "1.5K"
 */
fun formatCompactNumber(count: Int): String {
    return when {
        count >= 1_000_000 -> "${(count / 1_000_000.0).formatDecimal(1)}M"
        count >= 1_000 -> "${(count / 1_000.0).formatDecimal(1)}K"
        else -> count.toString()
    }
}

/**
 * Format donation amount with $ prefix.
 * Example: formatDonationAmount(1500.0) -> "$1.5K"
 */
fun formatDonationAmount(amount: Double): String {
    return when {
        amount >= 1000 -> "$${(amount / 1000).formatDecimal(1)}K"
        amount >= 100 -> "$${amount.toInt()}"
        else -> "$${amount.formatDecimal(2)}"
    }
}

/**
 * Format bytes to human-readable format (KMP-compatible).
 * Example: formatBytesKmp(1536) -> "1.5 KB"
 */
fun formatBytesKmp(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${(bytes / 1024.0).formatDecimal(1)} KB"
        bytes < 1024 * 1024 * 1024 -> "${(bytes / (1024.0 * 1024.0)).formatDecimal(2)} MB"
        else -> "${(bytes / (1024.0 * 1024.0 * 1024.0)).formatDecimal(2)} GB"
    }
}

/**
 * Format speed in bytes per second (KMP-compatible).
 * Example: formatSpeedKmp(1536f) -> "1.5 KB/s"
 */
fun formatSpeedKmp(bytesPerSecond: Float): String {
    return when {
        bytesPerSecond < 1024 -> "${bytesPerSecond.toInt()} B/s"
        bytesPerSecond < 1024 * 1024 -> "${(bytesPerSecond / 1024).formatDecimal(1)} KB/s"
        else -> "${(bytesPerSecond / (1024 * 1024)).formatDecimal(2)} MB/s"
    }
}

/**
 * Format percentage.
 * Example: formatPercentage(75.5) -> "75.5%"
 */
fun formatPercentage(value: Double, decimals: Int = 1): String {
    return "${value.formatDecimal(decimals)}%"
}

/**
 * Format multiplier (e.g., speed).
 * Example: formatMultiplier(1.5f) -> "1.5x"
 */
fun formatMultiplier(value: Float, decimals: Int = 1): String {
    return "${value.formatDecimal(decimals)}x"
}

/**
 * Format time duration as seconds.
 * Example: formatSeconds(1.5) -> "1.5s"
 */
fun formatSeconds(seconds: Double, decimals: Int = 1): String {
    return if (seconds == seconds.toInt().toDouble()) {
        "${seconds.toInt()}s"
    } else {
        "${seconds.formatDecimal(decimals)}s"
    }
}

/**
 * Pad an integer with leading zeros.
 * Example: 5.padZero(2) -> "05"
 */
fun Int.padZero(length: Int): String = this.toString().padStart(length, '0')

/**
 * Pad a Long with leading zeros.
 * Example: 5L.padZero(2) -> "05"
 */
fun Long.padZero(length: Int): String = this.toString().padStart(length, '0')
