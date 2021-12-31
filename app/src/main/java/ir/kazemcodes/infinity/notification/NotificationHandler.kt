package ir.kazemcodes.infinity.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import ir.kazemcodes.infinity.presentation.home.MainActivity

/**
 * Class that manages [PendingIntent] of activity's
 */
object NotificationHandler {
    /**
     * Returns [PendingIntent] that starts a download activity.
     *
     * @param context context of application
     */
    internal fun openDownloadManagerPendingActivity(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            action = MainActivity.SHORTCUT_DOWNLOADS
        }
        return PendingIntent.getActivity(context, 0, intent, 0)
    }
}
