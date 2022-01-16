package ir.kazemcodes.infinity.feature_activity.domain.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
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

    override fun onReceive(context: Context?, intent: Intent?) {
        TODO("Not yet implemented")
    }


}
