package ireader.presentation.ui.settings.downloader

/**
 * Format download speed in bytes per second to human-readable format
 */
fun formatSpeed(bytesPerSecond: Float): String {
    return when {
        bytesPerSecond < 1024 -> "${bytesPerSecond.toInt()} B/s"
        bytesPerSecond < 1024 * 1024 -> String.format("%.1f KB/s", bytesPerSecond / 1024)
        else -> String.format("%.2f MB/s", bytesPerSecond / (1024 * 1024))
    }
}

/**
 * Format duration in milliseconds to human-readable format
 */
fun formatDuration(milliseconds: Long): String {
    if (milliseconds <= 0) return "Calculating..."
    
    val seconds = milliseconds / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}

/**
 * Format bytes to human-readable format
 */
fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
