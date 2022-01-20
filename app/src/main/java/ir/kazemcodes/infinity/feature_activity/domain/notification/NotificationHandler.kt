package ir.kazemcodes.infinity.feature_activity.domain.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import ir.kazemcodes.infinity.feature_activity.presentation.MainActivity

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

    /**
     * Returns [PendingIntent] that prompts user with apk install intent
     *
     * @param context context
     * @param uri uri of apk that is installed
     */
    fun installApkPendingActivity(context: Context, uri: Uri): PendingIntent {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        return PendingIntent.getActivity(context, 0, intent, 0)
    }
}
