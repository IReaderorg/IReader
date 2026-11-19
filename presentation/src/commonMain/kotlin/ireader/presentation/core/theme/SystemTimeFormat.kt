package ireader.presentation.core.theme

/**
 * Formats a timestamp with the platform's system time format, so the reader footer
 * follows the user's 12/24-hour setting and locale (e.g. "2:30 PM" or "14:30").
 *
 * @param timestampMillis epoch milliseconds in UTC
 * @param platformContext Android [android.content.Context] on Android; unused elsewhere
 */
expect fun formatSystemTime(timestampMillis: Long, platformContext: Any?): String
