package ireader.domain.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH
import androidx.core.app.NotificationManagerCompat.IMPORTANCE_LOW
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import ireader.domain.R

/**
 * Class to manage the basic information of all the notifications used in the app.
 */
object Notifications {

    /**
     * Common notification channel and ids used anywhere.
     */
    const val CHANNEL_COMMON = "common_channel"
    const val ID_DOWNLOAD_IMAGE = 2

    /**
     * Notification channel and ids used by the library updater.
     */
    private const val GROUP_LIBRARY = "group_library"
    const val CHANNEL_LIBRARY_PROGRESS = "library_progress_channel"
    const val ID_LIBRARY_PROGRESS = -101
    const val CHANNEL_LIBRARY_ERROR = "library_errors_channel"
    const val ID_LIBRARY_ERROR = -102

    private const val GROUP_TTS = "group_text_reader"
    const val CHANNEL_TTS = "library_text_reader_channel"
    const val ID_TTS = -601
    const val CHANNEL_TTS_ERROR = "library_text_reader_error_channel"
    const val ID_TTS_ERROR = -602

    /**
     * Notification channel and ids used by the downloader.
     */
    const val GROUP_DOWNLOADER = "group_downloader"
    const val CHANNEL_DOWNLOADER_PROGRESS = "downloader_progress_channel"
    const val ID_DOWNLOAD_CHAPTER_PROGRESS = -201
    const val CHANNEL_DOWNLOADER_COMPLETE = "downloader_complete_channel"
    const val ID_DOWNLOAD_CHAPTER_COMPLETE = -203
    const val CHANNEL_DOWNLOADER_ERROR = "downloader_error_channel"
    const val ID_DOWNLOAD_CHAPTER_ERROR = -202

    /**
     * Notification channel and ids used by the library updater.
     */
    const val CHANNEL_NEW_CHAPTERS = "new_chapters_channel"
    const val ID_NEW_CHAPTERS = -301
    const val GROUP_NEW_CHAPTERS = "eu.kanade.tachiyomi.NEW_CHAPTERS"

    /**
     * Notification channel and ids used by the backup/restore system.
     */
    private const val GROUP_BACKUP_RESTORE = "group_backup_restore"
    const val CHANNEL_BACKUP_RESTORE_PROGRESS = "backup_restore_progress_channel"
    const val ID_BACKUP_PROGRESS = -501
    const val ID_RESTORE_PROGRESS = -503
    const val CHANNEL_BACKUP_RESTORE_COMPLETE = "backup_restore_complete_channel_v2"
    const val ID_BACKUP_COMPLETE = -502
    const val ID_RESTORE_COMPLETE = -504

    /**
     * Notification channel used for crash log file sharing.
     */
    const val CHANNEL_CRASH_LOGS = "crash_logs_channel"
    const val ID_CRASH_LOGS = -601

    /**
     * Notification channel used for Incognito Mode
     */
    const val CHANNEL_INCOGNITO_MODE = "incognito_mode_channel"
    const val ID_INCOGNITO_MODE = -701

    /**
     * Notification channel and ids used for app and extension updates.
     */
    private const val GROUP_APK_UPDATES = "group_apk_updates"
    const val CHANNEL_APP_UPDATE = "app_apk_update_channel"
    const val ID_APP_UPDATER = 1
    const val CHANNEL_EXTENSIONS_UPDATE = "ext_apk_update_channel"
    const val ID_UPDATES_TO_EXTS = -401
    const val ID_EXTENSION_INSTALLER = -402

    private val deprecatedChannels = listOf(
        "downloader_channel",
        "backup_restore_complete_channel",
        "library_channel",
        "library_progress_channel",
        "updates_ext_channel",
    )

    /**
     * Creates the notification channels introduced in Android Oreo.
     * This won't do anything on Android versions that don't support notification channels.
     *
     * @param context The application context.
     */
    fun createChannels(context: Context) {
        val notificationService = NotificationManagerCompat.from(context)

        // Delete old notification channels
        deprecatedChannels.forEach(notificationService::deleteNotificationChannel)

        notificationService.createNotificationChannelGroupsCompat(
            listOf(
                ireader.common.extensions.buildNotificationChannelGroup(GROUP_BACKUP_RESTORE) {
                    setName(context.getString(R.string.label_backup))
                },
                ireader.common.extensions.buildNotificationChannelGroup(GROUP_DOWNLOADER) {
                    setName(context.getString(R.string.download_notifier_downloader_title))
                },
                ireader.common.extensions.buildNotificationChannelGroup(GROUP_LIBRARY) {
                    setName(context.getString(R.string.label_library))
                },
                ireader.common.extensions.buildNotificationChannelGroup(GROUP_TTS) {
                    setName(context.getString(R.string.label_text_reader))
                },
                ireader.common.extensions.buildNotificationChannelGroup(GROUP_APK_UPDATES) {
                    setName(context.getString(R.string.label_recent_updates))
                },
            )
        )

        notificationService.createNotificationChannelsCompat(
            listOf(
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_COMMON,
                    IMPORTANCE_LOW
                ) {
                    setName(context.getString(R.string.channel_common))
                },
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_TTS,
                    IMPORTANCE_LOW
                ) {
                    setName(context.getString(R.string.channel_progress))
                    setGroup(GROUP_TTS)
                    setShowBadge(false)
                },
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_TTS_ERROR,
                    IMPORTANCE_LOW
                ) {
                    setName(context.getString(R.string.channel_errors))
                    setGroup(GROUP_TTS)
                    setShowBadge(false)
                },
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_LIBRARY_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(context.getString(R.string.channel_progress))
                    setGroup(GROUP_LIBRARY)
                    setShowBadge(false)
                },
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_LIBRARY_ERROR,
                    IMPORTANCE_LOW
                ) {
                    setName(context.getString(R.string.channel_errors))
                    setGroup(GROUP_LIBRARY)
                    setShowBadge(false)
                },

                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_NEW_CHAPTERS,
                    IMPORTANCE_DEFAULT
                ) {
                    setName(context.getString(R.string.channel_new_chapters))
                },
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_DOWNLOADER_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(context.getString(R.string.channel_progress))
                    setGroup(GROUP_DOWNLOADER)
                    setShowBadge(false)
                },
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_DOWNLOADER_COMPLETE,
                    IMPORTANCE_LOW
                ) {
                    setName(context.getString(R.string.channel_complete))
                    setGroup(GROUP_DOWNLOADER)
                    setShowBadge(false)
                },
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_DOWNLOADER_ERROR,
                    IMPORTANCE_LOW
                ) {
                    setName(context.getString(R.string.channel_errors))
                    setGroup(GROUP_DOWNLOADER)
                    setShowBadge(false)
                },
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_BACKUP_RESTORE_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(context.getString(R.string.channel_progress))
                    setGroup(GROUP_BACKUP_RESTORE)
                    setShowBadge(false)
                },
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_BACKUP_RESTORE_COMPLETE,
                    IMPORTANCE_HIGH
                ) {
                    setName(context.getString(R.string.channel_complete))
                    setGroup(GROUP_BACKUP_RESTORE)
                    setShowBadge(false)
                    setSound(null, null)
                },
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_CRASH_LOGS,
                    IMPORTANCE_HIGH
                ) {
                    setName(context.getString(R.string.channel_crash_logs))
                },
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_APP_UPDATE,
                    IMPORTANCE_DEFAULT
                ) {
                    setGroup(GROUP_APK_UPDATES)
                    setName(context.getString(R.string.channel_app_updates))
                },
                ireader.common.extensions.buildNotificationChannel(
                    CHANNEL_EXTENSIONS_UPDATE,
                    IMPORTANCE_DEFAULT
                ) {
                    setGroup(GROUP_APK_UPDATES)
                    setName(context.getString(R.string.channel_ext_updates))
                },
            )
        )
    }
}

data class Channel(
    val name: String,
    val id: String,
    val importance: Int = NotificationManager.IMPORTANCE_DEFAULT,
)

fun createChannel(context: Context, channel: Channel) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = channel.name
        val importance = channel.importance
        // Register the channel with the system
        val notificationManager: NotificationManager = context.getSystemService(
            Context.NOTIFICATION_SERVICE
        ) as NotificationManager
        notificationManager.createNotificationChannel(
            NotificationChannel(channel.id, name, importance)
        )
    }
}

suspend fun NotificationCompat.Builder.setLargeIcon(
    context: Context,
    data: Any?,
): NotificationCompat.Builder {
    return try {
        if (data != null) {
            val request = ImageRequest.Builder(context)
                .data(data)
                .allowHardware(true)
                .target { setLargeIcon(it.toBitmap()) }
                .size(512)
                .build()
            ImageLoader(context).execute(request)
        }
        this
    } catch (e: Throwable) {
        this
    }
}

val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
else
    PendingIntent.FLAG_UPDATE_CURRENT
