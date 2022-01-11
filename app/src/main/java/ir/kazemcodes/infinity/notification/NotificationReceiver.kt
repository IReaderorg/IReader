package ir.kazemcodes.infinity.notification

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ir.kazemcodes.infinity.data.network.utils.notificationManager
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.closestDI
import org.kodein.di.instance

/**
 * Global [BroadcastReceiver] that runs on UI thread
 * Pending Broadcasts should be made from here.
 * NOTE: Use local broadcasts if possible.
 */
class NotificationReceiver : BroadcastReceiver(), DIAware {

    val context by instance<Context>()

    override val di: DI by closestDI(context)


    private val downloadManager: DownloadManager by di.instance<DownloadManager>()

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
