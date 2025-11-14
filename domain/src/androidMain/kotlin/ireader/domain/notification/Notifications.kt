package ireader.domain.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.NotificationManagerCompat.*
import androidx.core.graphics.drawable.toBitmap
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.allowHardware
import ireader.domain.notification.NotificationsIds.CHANNEL_APP_UPDATE
import ireader.domain.notification.NotificationsIds.CHANNEL_BACKUP_RESTORE_COMPLETE
import ireader.domain.notification.NotificationsIds.CHANNEL_BACKUP_RESTORE_PROGRESS
import ireader.domain.notification.NotificationsIds.CHANNEL_COMMON
import ireader.domain.notification.NotificationsIds.CHANNEL_CRASH_LOGS
import ireader.domain.notification.NotificationsIds.CHANNEL_DOWNLOADER_COMPLETE
import ireader.domain.notification.NotificationsIds.CHANNEL_DOWNLOADER_ERROR
import ireader.domain.notification.NotificationsIds.CHANNEL_DOWNLOADER_PROGRESS
import ireader.domain.notification.NotificationsIds.CHANNEL_EXTENSIONS_UPDATE
import ireader.domain.notification.NotificationsIds.CHANNEL_INSTALLER_COMPLETE
import ireader.domain.notification.NotificationsIds.CHANNEL_INSTALLER_ERROR
import ireader.domain.notification.NotificationsIds.CHANNEL_INSTALLER_PROGRESS
import ireader.domain.notification.NotificationsIds.CHANNEL_LIBRARY_ERROR
import ireader.domain.notification.NotificationsIds.CHANNEL_LIBRARY_PROGRESS
import ireader.domain.notification.NotificationsIds.CHANNEL_NEW_CHAPTERS
import ireader.domain.notification.NotificationsIds.CHANNEL_TTS
import ireader.domain.notification.NotificationsIds.CHANNEL_TTS_ERROR
import ireader.domain.notification.NotificationsIds.GROUP_APK_UPDATES
import ireader.domain.notification.NotificationsIds.GROUP_DOWNLOADER
import ireader.domain.notification.NotificationsIds.GROUP_INSTALLER
import ireader.domain.notification.NotificationsIds.GROUP_BACKUP_RESTORE
import ireader.domain.notification.NotificationsIds.GROUP_LIBRARY
import ireader.domain.notification.NotificationsIds.GROUP_TTS
import ireader.domain.utils.extensions.buildNotificationChannel
import ireader.domain.utils.extensions.buildNotificationChannelGroup
import ireader.i18n.LocalizeHelper
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

/**
 * Class to manage the basic information of all the notifications used in the app.
 */
object Notifications {

    

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
    fun createChannels(context: Context,localizeHelper: LocalizeHelper) {
        val notificationService = NotificationManagerCompat.from(context)

        // Delete old notification channels
        deprecatedChannels.forEach(notificationService::deleteNotificationChannel)

        notificationService.createNotificationChannelGroupsCompat(
            listOf(
                buildNotificationChannelGroup(GROUP_BACKUP_RESTORE) {
                    setName(localizeHelper.localize(Res.string.label_backup))
                },
                buildNotificationChannelGroup(GROUP_DOWNLOADER) {
                    setName(localizeHelper.localize(Res.string.download_notifier_downloader_title))
                },
                buildNotificationChannelGroup(GROUP_INSTALLER) {
                    setName(localizeHelper.localize(Res.string.install))
                },
                buildNotificationChannelGroup(GROUP_LIBRARY) {
                    setName(localizeHelper.localize(Res.string.label_library))
                },
                buildNotificationChannelGroup(GROUP_TTS) {
                    setName(localizeHelper.localize(Res.string.label_text_reader))
                },
                buildNotificationChannelGroup(GROUP_APK_UPDATES) {
                    setName(localizeHelper.localize(Res.string.label_recent_updates))
                },
            )
        )

        notificationService.createNotificationChannelsCompat(
            listOf(
                buildNotificationChannel(
                    CHANNEL_COMMON,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_common))
                },
                buildNotificationChannel(
                    CHANNEL_TTS,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_progress))
                    setGroup(GROUP_TTS)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_TTS_ERROR,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_errors))
                    setGroup(GROUP_TTS)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_LIBRARY_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_progress))
                    setGroup(GROUP_LIBRARY)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_LIBRARY_ERROR,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_errors))
                    setGroup(GROUP_LIBRARY)
                    setShowBadge(false)
                },

                buildNotificationChannel(
                    CHANNEL_NEW_CHAPTERS,
                    IMPORTANCE_DEFAULT
                ) {
                    setName(localizeHelper.localize(Res.string.channel_new_chapters))
                },
                buildNotificationChannel(
                    CHANNEL_DOWNLOADER_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_progress))
                    setGroup(GROUP_DOWNLOADER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_DOWNLOADER_COMPLETE,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_complete))
                    setGroup(GROUP_DOWNLOADER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_DOWNLOADER_ERROR,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_errors))
                    setGroup(GROUP_DOWNLOADER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_BACKUP_RESTORE_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_progress))
                    setGroup(GROUP_BACKUP_RESTORE)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_INSTALLER_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_progress))
                    setGroup(GROUP_INSTALLER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_INSTALLER_COMPLETE,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_complete))
                    setGroup(GROUP_INSTALLER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_INSTALLER_ERROR,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_errors))
                    setGroup(GROUP_INSTALLER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_BACKUP_RESTORE_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize(Res.string.channel_progress))
                    setGroup(GROUP_BACKUP_RESTORE)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_BACKUP_RESTORE_COMPLETE,
                    IMPORTANCE_HIGH
                ) {
                    setName(localizeHelper.localize(Res.string.channel_complete))
                    setGroup(GROUP_BACKUP_RESTORE)
                    setShowBadge(false)
                    setSound(null, null)
                },
                buildNotificationChannel(
                    CHANNEL_CRASH_LOGS,
                    IMPORTANCE_HIGH
                ) {
                    setName(localizeHelper.localize(Res.string.channel_crash_logs))
                },
                buildNotificationChannel(
                    CHANNEL_APP_UPDATE,
                    IMPORTANCE_DEFAULT
                ) {
                    setGroup(GROUP_APK_UPDATES)
                    setName(localizeHelper.localize(Res.string.channel_app_updates))
                },
                buildNotificationChannel(
                    CHANNEL_EXTENSIONS_UPDATE,
                    IMPORTANCE_DEFAULT
                ) {
                    setGroup(GROUP_APK_UPDATES)
                    setName(localizeHelper.localize(Res.string.channel_ext_updates))
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

@OptIn(ExperimentalCoilApi::class)
suspend fun NotificationCompat.Builder.setLargeIcon(
    context: Context,
    data: Any?,
): NotificationCompat.Builder {
    return try {
        if (data != null) {
            val request = ImageRequest.Builder(context)
                .data(data)
                .allowHardware(true)
                .target { setLargeIcon(it.asDrawable(context.resources).toBitmap()) }
                .size(512)
                .build()
            ImageLoader(context).execute(request)
        }
        this
    } catch (e: Throwable) {
        this
    }
}
val legacyFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
else
    PendingIntent.FLAG_UPDATE_CURRENT

val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
else
    PendingIntent.FLAG_UPDATE_CURRENT
