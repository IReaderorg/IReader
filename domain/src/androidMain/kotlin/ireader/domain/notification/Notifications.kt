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
import ireader.domain.notification.NotificationsIds.GROUP_BACKUP_RESTORE
import ireader.domain.notification.NotificationsIds.GROUP_DOWNLOADER
import ireader.domain.notification.NotificationsIds.GROUP_INSTALLER
import ireader.domain.notification.NotificationsIds.GROUP_LIBRARY
import ireader.domain.notification.NotificationsIds.GROUP_TTS
import ireader.domain.utils.extensions.buildNotificationChannel
import ireader.domain.utils.extensions.buildNotificationChannelGroup
import ireader.i18n.LocalizeHelper

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
    fun createChannels(context: Context, localizeHelper: LocalizeHelper) {
        val notificationService = NotificationManagerCompat.from(context)

        // Delete old notification channels
        deprecatedChannels.forEach(notificationService::deleteNotificationChannel)

        notificationService.createNotificationChannelGroupsCompat(
            listOf(
                buildNotificationChannelGroup(GROUP_BACKUP_RESTORE) {
                    setName(localizeHelper.localize() { xml ->
                        xml.labelBackup
                    })
                },
                buildNotificationChannelGroup(GROUP_DOWNLOADER) {
                    setName(localizeHelper.localize() { xml ->
                        xml.downloadNotifierDownloaderTitle
                    })
                },
                buildNotificationChannelGroup(GROUP_INSTALLER) {
                    setName(localizeHelper.localize() { xml ->
                        xml.install
                    })
                },
                buildNotificationChannelGroup(GROUP_LIBRARY) {
                    setName(localizeHelper.localize() { xml ->
                        xml.labelLibrary
                    })
                },
                buildNotificationChannelGroup(GROUP_TTS) {
                    setName(localizeHelper.localize() { xml ->
                        xml.labelTextReader
                    })
                },
                buildNotificationChannelGroup(GROUP_APK_UPDATES) {
                    setName(localizeHelper.localize() { xml ->
                        xml.labelRecentUpdates
                    })
                },
            )
        )

        notificationService.createNotificationChannelsCompat(
            listOf(
                buildNotificationChannel(
                    CHANNEL_COMMON,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelCommon
                    })
                },
                buildNotificationChannel(
                    CHANNEL_TTS,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelProgress
                    })
                    setGroup(GROUP_TTS)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_TTS_ERROR,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelErrors
                    })
                    setGroup(GROUP_TTS)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_LIBRARY_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelProgress
                    })
                    setGroup(GROUP_LIBRARY)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_LIBRARY_ERROR,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelErrors
                    })
                    setGroup(GROUP_LIBRARY)
                    setShowBadge(false)
                },

                buildNotificationChannel(
                    CHANNEL_NEW_CHAPTERS,
                    IMPORTANCE_DEFAULT
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelNewChapters
                    })
                },
                buildNotificationChannel(
                    CHANNEL_DOWNLOADER_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelProgress
                    })
                    setGroup(GROUP_DOWNLOADER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_DOWNLOADER_COMPLETE,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelComplete
                    })
                    setGroup(GROUP_DOWNLOADER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_DOWNLOADER_ERROR,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelErrors
                    })
                    setGroup(GROUP_DOWNLOADER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_BACKUP_RESTORE_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelProgress
                    })
                    setGroup(GROUP_BACKUP_RESTORE)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_INSTALLER_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelProgress
                    })
                    setGroup(GROUP_INSTALLER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_INSTALLER_COMPLETE,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelComplete
                    })
                    setGroup(GROUP_INSTALLER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_INSTALLER_ERROR,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelErrors
                    })
                    setGroup(GROUP_INSTALLER)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_BACKUP_RESTORE_PROGRESS,
                    IMPORTANCE_LOW
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelProgress
                    })
                    setGroup(GROUP_BACKUP_RESTORE)
                    setShowBadge(false)
                },
                buildNotificationChannel(
                    CHANNEL_BACKUP_RESTORE_COMPLETE,
                    IMPORTANCE_HIGH
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelComplete
                    })
                    setGroup(GROUP_BACKUP_RESTORE)
                    setShowBadge(false)
                    setSound(null, null)
                },
                buildNotificationChannel(
                    CHANNEL_CRASH_LOGS,
                    IMPORTANCE_HIGH
                ) {
                    setName(localizeHelper.localize() { xml ->
                        xml.channelCrashLogs
                    })
                },
                buildNotificationChannel(
                    CHANNEL_APP_UPDATE,
                    IMPORTANCE_DEFAULT
                ) {
                    setGroup(GROUP_APK_UPDATES)
                    setName(localizeHelper.localize() { xml ->
                        xml.channelAppUpdates
                    })
                },
                buildNotificationChannel(
                    CHANNEL_EXTENSIONS_UPDATE,
                    IMPORTANCE_DEFAULT
                ) {
                    setGroup(GROUP_APK_UPDATES)
                    setName(localizeHelper.localize() { xml ->
                        xml.channelExtUpdates
                    })
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

val legacyFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
else
    PendingIntent.FLAG_UPDATE_CURRENT

val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
else
    PendingIntent.FLAG_UPDATE_CURRENT
