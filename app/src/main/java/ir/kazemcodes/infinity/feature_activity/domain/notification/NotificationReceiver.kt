package ir.kazemcodes.infinity.feature_activity.domain.notification

import android.app.Notification.EXTRA_NOTIFICATION_ID
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import ir.kazemcodes.infinity.core.data.network.utils.notificationManager

/**
 * Global [BroadcastReceiver] that runs on UI thread
 * Pending Broadcasts should be made from here.
 * NOTE: Use local broadcasts if possible.
 */
class NotificationReceiver : BroadcastReceiver() {

    /**
     * Dismiss the notification
     *
     * @param notificationId the id of the notification
     */
    private fun dismissNotification(context: Context, notificationId: Int) {
        context.notificationManager.cancel(notificationId)
    }
    companion object {
        private const val NAME = "NotificationReceiver"
        private const val ACTION_DISMISS_NOTIFICATION = "$NAME.ACTION_DISMISS_NOTIFICATION"

        /**
         * Returns [PendingIntent] that starts a service which dismissed the notification
         *
         * @param context context of application
         * @param notificationId id of notification
         * @return [PendingIntent]
         */
        internal fun dismissNotificationPendingBroadcast(context: Context, notificationId: Int): PendingIntent {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = ACTION_DISMISS_NOTIFICATION
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action ) {
            // Dismiss notification
            ACTION_DISMISS_NOTIFICATION -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dismissNotification(context,
                    intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1))
            }
        }
    }




}
