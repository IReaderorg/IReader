package ireader.plugin.api

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime

/**
 * Plugin interface for webhook and external notification integrations.
 * Enables sending notifications to external services (Discord, Telegram, IFTTT, etc.)
 * 
 * Example:
 * ```kotlin
 * class DiscordWebhookPlugin : WebhookPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.discord-webhook",
 *         name = "Discord Notifications",
 *         type = PluginType.FEATURE,
 *         permissions = listOf(PluginPermission.NETWORK, PluginPermission.READER_CONTEXT),
 *         // ... other manifest fields
 *     )
 *     
 *     override val webhookType = WebhookType.DISCORD
 *     
 *     override suspend fun sendNotification(notification: WebhookNotification): WebhookResult<Unit> {
 *         // Send to Discord webhook
 *     }
 * }
 * ```
 */
interface WebhookPlugin : Plugin {
    /**
     * Type of webhook service.
     */
    val webhookType: WebhookType
    
    /**
     * Webhook configuration.
     */
    val webhookConfig: WebhookConfig
    
    /**
     * Supported notification triggers.
     */
    val supportedTriggers: List<NotificationTrigger>
    
    /**
     * Test webhook connection.
     */
    suspend fun testConnection(): WebhookResult<Unit>
    
    /**
     * Send a notification.
     */
    suspend fun sendNotification(notification: WebhookNotification): WebhookResult<Unit>
    
    /**
     * Send a batch of notifications.
     */
    suspend fun sendBatch(notifications: List<WebhookNotification>): WebhookResult<WebhookBatchResult>
    
    /**
     * Configure webhook URL.
     */
    fun setWebhookUrl(url: String)
    
    /**
     * Get current webhook URL (masked).
     */
    fun getWebhookUrl(): String?
    
    /**
     * Enable/disable specific triggers.
     */
    fun setTriggerEnabled(trigger: NotificationTrigger, enabled: Boolean)
    
    /**
     * Check if trigger is enabled.
     */
    fun isTriggerEnabled(trigger: NotificationTrigger): Boolean
    
    /**
     * Get notification history.
     */
    suspend fun getHistory(limit: Int = 50): List<NotificationHistoryEntry>
    
    /**
     * Clear notification history.
     */
    suspend fun clearHistory()
    
    /**
     * Format notification for this webhook type.
     */
    fun formatNotification(notification: WebhookNotification): FormattedNotification
}

/**
 * Webhook service types.
 */
@Serializable
enum class WebhookType {
    /** Discord webhook */
    DISCORD,
    /** Telegram bot */
    TELEGRAM,
    /** Slack webhook */
    SLACK,
    /** IFTTT webhook */
    IFTTT,
    /** Pushover */
    PUSHOVER,
    /** Ntfy.sh */
    NTFY,
    /** Generic webhook */
    GENERIC,
    /** Email (SMTP) */
    EMAIL,
    /** Custom */
    CUSTOM
}

/**
 * Webhook configuration.
 */
@Serializable
data class WebhookConfig(
    /** Whether URL is required */
    val requiresUrl: Boolean = true,
    /** Whether API key/token is required */
    val requiresApiKey: Boolean = false,
    /** Maximum message length */
    val maxMessageLength: Int = 2000,
    /** Whether images are supported */
    val supportsImages: Boolean = false,
    /** Whether embeds/rich content is supported */
    val supportsEmbeds: Boolean = false,
    /** Rate limit (requests per minute) */
    val rateLimitPerMinute: Int = 30,
    /** Whether batching is supported */
    val supportsBatching: Boolean = false,
    /** Maximum batch size */
    val maxBatchSize: Int = 10
)

/**
 * Notification triggers.
 */
@Serializable
enum class NotificationTrigger {
    /** Book added to library */
    BOOK_ADDED,
    /** New chapter available */
    NEW_CHAPTER,
    /** Book completed */
    BOOK_COMPLETED,
    /** Reading goal achieved */
    GOAL_ACHIEVED,
    /** Achievement unlocked */
    ACHIEVEMENT_UNLOCKED,
    /** Reading streak milestone */
    STREAK_MILESTONE,
    /** Sync completed */
    SYNC_COMPLETED,
    /** Sync failed */
    SYNC_FAILED,
    /** Download completed */
    DOWNLOAD_COMPLETED,
    /** Reading session ended */
    SESSION_ENDED,
    /** Daily summary */
    DAILY_SUMMARY,
    /** Weekly summary */
    WEEKLY_SUMMARY,
    /** Custom trigger */
    CUSTOM
}

/**
 * Webhook notification.
 */
@Serializable
data class WebhookNotification(
    /** Notification trigger */
    val trigger: NotificationTrigger,
    /** Notification title */
    val title: String,
    /** Notification message */
    val message: String,
    /** Image URL (optional) */
    val imageUrl: String? = null,
    /** Additional data */
    val data: Map<String, String> = emptyMap(),
    /** Timestamp */
    val timestamp: Long,
    /** Priority */
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    /** Custom fields for specific webhook types */
    val customFields: Map<String, String> = emptyMap()
)

@Serializable
enum class NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

/**
 * Formatted notification ready to send.
 */
@Serializable
data class FormattedNotification(
    /** Formatted payload (JSON string) */
    val payload: String,
    /** Content type */
    val contentType: String = "application/json",
    /** Additional headers */
    val headers: Map<String, String> = emptyMap()
)

/**
 * Batch send result for webhooks.
 */
@Serializable
data class WebhookBatchResult(
    val totalSent: Int,
    val successful: Int,
    val failed: Int,
    val errors: List<String> = emptyList()
)

/**
 * Notification history entry.
 */
@Serializable
data class NotificationHistoryEntry(
    val id: String,
    val trigger: NotificationTrigger,
    val title: String,
    val message: String,
    val timestamp: Long,
    val success: Boolean,
    val errorMessage: String? = null
)

/**
 * Result wrapper for webhook operations.
 */
sealed class WebhookResult<out T> {
    data class Success<T>(val data: T) : WebhookResult<T>()
    data class Error(val error: WebhookError) : WebhookResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
}

/**
 * Webhook errors.
 */
@Serializable
sealed class WebhookError {
    data class InvalidUrl(val url: String) : WebhookError()
    data class ConnectionFailed(val reason: String) : WebhookError()
    data class AuthenticationFailed(val reason: String) : WebhookError()
    data class RateLimited(val retryAfterMs: Long?) : WebhookError()
    data class MessageTooLong(val maxLength: Int, val actualLength: Int) : WebhookError()
    data class ServerError(val statusCode: Int, val message: String) : WebhookError()
    data class Unknown(val message: String) : WebhookError()
}

/**
 * Helper for building common notification messages.
 */
object NotificationTemplates {
    fun bookAdded(title: String, author: String?): WebhookNotification {
        val authorText = author?.let { " by $it" } ?: ""
        return WebhookNotification(
            trigger = NotificationTrigger.BOOK_ADDED,
            title = "📚 New Book Added",
            message = "Added \"$title\"$authorText to library",
            timestamp = currentTimeMillis()
        )
    }
    
    fun newChapter(bookTitle: String, chapterTitle: String, chapterNumber: Int): WebhookNotification {
        return WebhookNotification(
            trigger = NotificationTrigger.NEW_CHAPTER,
            title = "📖 New Chapter Available",
            message = "$bookTitle - Chapter $chapterNumber: $chapterTitle",
            timestamp = currentTimeMillis()
        )
    }
    
    fun bookCompleted(title: String, totalChapters: Int, readingTimeHours: Float): WebhookNotification {
        val hoursFormatted = ((readingTimeHours * 10).toInt() / 10.0)
        return WebhookNotification(
            trigger = NotificationTrigger.BOOK_COMPLETED,
            title = "🎉 Book Completed!",
            message = "Finished \"$title\" ($totalChapters chapters in $hoursFormatted hours)",
            timestamp = currentTimeMillis(),
            priority = NotificationPriority.HIGH
        )
    }
    
    fun goalAchieved(goalType: String, target: Int): WebhookNotification {
        return WebhookNotification(
            trigger = NotificationTrigger.GOAL_ACHIEVED,
            title = "🏆 Goal Achieved!",
            message = "Completed $goalType goal: $target",
            timestamp = currentTimeMillis(),
            priority = NotificationPriority.HIGH
        )
    }
    
    fun streakMilestone(days: Int): WebhookNotification {
        return WebhookNotification(
            trigger = NotificationTrigger.STREAK_MILESTONE,
            title = "🔥 Reading Streak!",
            message = "$days day reading streak!",
            timestamp = currentTimeMillis()
        )
    }
    
    fun dailySummary(
        readingTimeMinutes: Int,
        chaptersRead: Int,
        wordsRead: Int
    ): WebhookNotification {
        return WebhookNotification(
            trigger = NotificationTrigger.DAILY_SUMMARY,
            title = "📊 Daily Reading Summary",
            message = "Today: ${readingTimeMinutes}min reading, $chaptersRead chapters, $wordsRead words",
            timestamp = currentTimeMillis()
        )
    }
    
    @OptIn(ExperimentalTime::class)
    private fun currentTimeMillis(): Long {
        return kotlin.time.Clock.System.now().toEpochMilliseconds()
    }
}