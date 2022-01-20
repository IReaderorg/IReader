package ir.kazemcodes.infinity.feature_updater

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.core.data.network.utils.notificationBuilder
import ir.kazemcodes.infinity.core.data.network.utils.notificationManager
import ir.kazemcodes.infinity.feature_activity.domain.notification.NotificationHandler
import ir.kazemcodes.infinity.feature_activity.domain.notification.NotificationReceiver
import ir.kazemcodes.infinity.feature_activity.domain.notification.Notifications
import android.graphics.Bitmap

import androidx.core.content.ContextCompat

import android.graphics.drawable.Drawable




internal class AppUpdateNotifier(private val context: Context) {

    private val notificationBuilder = context.notificationBuilder(Notifications.CHANNEL_APP_UPDATE)

    /**
     * Call to show notification.
     *
     * @param id id of the notification channel.
     */
    private fun NotificationCompat.Builder.show(id: Int = Notifications.ID_APP_UPDATER) {
        context.notificationManager.notify(id, build())
    }

    fun promptUpdate(release: GithubRelease) {
        val intent = Intent(context, AppUpdateService::class.java).apply {
            putExtra(AppUpdateService.EXTRA_DOWNLOAD_URL, release.getDownloadLink())
        }
        val updateIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val releaseIntent = Intent(Intent.ACTION_VIEW, release.releaseLink.toUri()).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val releaseInfoIntent = PendingIntent.getActivity(context, release.hashCode(), releaseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        with(notificationBuilder) {
            setContentTitle(context.getString(R.string.update_check_notification_update_available))
            setContentText(release.version)
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            setContentIntent(updateIntent)

            clearActions()
            addAction(
                android.R.drawable.stat_sys_download_done,
                context.getString(R.string.action_download),
                updateIntent,
            )
            addAction(
                R.drawable.ic_info_24,
                context.getString(R.string.whats_new),
                releaseInfoIntent,
            )
        }
        notificationBuilder.show()
    }

    /**
     * Call when apk download starts.
     *
     * @param title tile of notification.
     */
    fun onDownloadStarted(title: String? = null): NotificationCompat.Builder {
        with(notificationBuilder) {
            title?.let { setContentTitle(title) }
            setContentText(context.getString(R.string.update_check_notification_download_in_progress))
            setSmallIcon(android.R.drawable.stat_sys_download)
            setOngoing(true)
        }
        notificationBuilder.show()
        return notificationBuilder
    }

    /**
     * Call when apk download progress changes.
     *
     * @param progress progress of download (xx%/100).
     */
    fun onProgressChange(progress: Int) {
        with(notificationBuilder) {
            setProgress(100, progress, false)
            setOnlyAlertOnce(true)
        }
        notificationBuilder.show()
    }

    /**
     * Call when apk download is finished.
     *
     * @param uri path location of apk.
     */
    fun onDownloadFinished(uri: Uri) {
        val installIntent = NotificationHandler.installApkPendingActivity(context, uri)
        with(notificationBuilder) {
            setContentText(context.getString(R.string.update_check_notification_download_complete))
            setSmallIcon(android.R.drawable.stat_sys_download_done)
            setOnlyAlertOnce(false)
            setProgress(0, 0, false)
            setContentIntent(installIntent)

            clearActions()
            addAction(
                R.drawable.baseline_upgrade_24,
                context.getString(R.string.action_install),
                installIntent
            )
            addAction(
                R.drawable.baseline_close_24,
                context.getString(R.string.action_cancel),
                NotificationReceiver.dismissNotificationPendingBroadcast(context, Notifications.ID_APP_UPDATER)
            )
        }
        notificationBuilder.show()
    }

    /**
     * Call when apk download throws a error
     *
     * @param url web location of apk to download.
     */
    fun onDownloadError(url: String) {
        with(notificationBuilder) {
            setContentText(context.getString(R.string.update_check_notification_download_error))
            setSmallIcon(R.drawable.baseline_warning_24)
            setOnlyAlertOnce(false)
            setProgress(0, 0, false)

            clearActions()
            addAction(
                R.drawable.baseline_refresh_24,
                context.getString(R.string.action_retry),
                AppUpdateService.downloadApkPendingService(context, url)
            )
            addAction(
                R.drawable.baseline_close_24,
                context.getString(R.string.action_cancel),
                NotificationReceiver.dismissNotificationPendingBroadcast(context, Notifications.ID_APP_UPDATER)
            )
        }
        notificationBuilder.show(Notifications.ID_APP_UPDATER)
    }
}
