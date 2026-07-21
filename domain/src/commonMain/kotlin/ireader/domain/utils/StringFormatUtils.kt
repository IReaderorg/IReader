package ireader.domain.utils

import kotlin.math.pow
import kotlin.math.roundToInt

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

fun Float.formatDecimal(decimals: Int): String = this.toDouble().formatDecimal(decimals)

fun toDecimalString(value: Double, decimals: Int): String = value.formatDecimal(decimals)
fun toDecimalString(value: Float, decimals: Int): String = value.toDouble().formatDecimal(decimals)

fun formatRatingWithReviews(rating: Float, reviewCount: Int): String {
    return "${rating.formatDecimal(1)} ($reviewCount reviews)"
}

fun formatRatingShort(rating: Float, count: Int): String {
    return "${rating.formatDecimal(1)} ($count)"
}

fun formatCurrency(currency: String, amount: Double): String {
    return "$currency ${amount.formatDecimal(2)}"
}

fun formatPrice(price: Double, symbol: String = "$"): String {
    return "$symbol${price.formatDecimal(2)}"
}

fun formatCompactNumber(count: Int): String {
    return when {
        count >= 1_000_000 -> "${(count / 1_000_000.0).formatDecimal(1)}M"
        count >= 1_000 -> "${(count / 1_000.0).formatDecimal(1)}K"
        else -> count.toString()
    }
}

fun formatDonationAmount(amount: Double): String {
    return when {
        amount >= 1000 -> "$${(amount / 1000).formatDecimal(1)}K"
        amount >= 100 -> "$${amount.toInt()}"
        else -> "$${amount.formatDecimal(2)}"
    }
}

fun formatBytesKmp(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${(bytes / 1024.0).formatDecimal(1)} KB"
        bytes < 1024 * 1024 * 1024 -> "${(bytes / (1024.0 * 1024.0)).formatDecimal(2)} MB"
        else -> "${(bytes / (1024.0 * 1024.0 * 1024.0)).formatDecimal(2)} GB"
    }
}

fun formatSpeedKmp(bytesPerSecond: Float): String {
    return when {
        bytesPerSecond < 1024 -> "${bytesPerSecond.toInt()} B/s"
        bytesPerSecond < 1024 * 1024 -> "${(bytesPerSecond / 1024).formatDecimal(1)} KB/s"
        else -> "${(bytesPerSecond / (1024 * 1024)).formatDecimal(2)} MB/s"
    }
}

fun formatPercentage(value: Double, decimals: Int = 1): String {
    return "${value.formatDecimal(decimals)}%"
}

fun formatMultiplier(value: Float, decimals: Int = 1): String {
    return "${value.formatDecimal(decimals)}x"
}

fun formatSeconds(seconds: Double, decimals: Int = 1): String {
    return if (seconds == seconds.toInt().toDouble()) {
        "${seconds.toInt()}s"
    } else {
        "${seconds.formatDecimal(decimals)}s"
    }
}

fun Int.padZero(length: Int): String = this.toString().padStart(length, '0')
fun Long.padZero(length: Int): String = this.toString().padStart(length, '0')
