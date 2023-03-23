//package ireader.domain.notification
//
//import android.content.Context
//import androidx.core.app.NotificationCompat
//
//class INotification(
//    private val context: Context
//) {
//    private lateinit var  notification: NotificationCompat.Builder
//    fun builder(channelId: Long) {
//        notification = NotificationCompat.Builder(
//            context,
//            Notifications.CHANNEL_LIBRARY_PROGRESS
//        )
//    }
//    fun setContentTitle(title:String) {
//        notification.setContentTitle(title)
//    }
//    fun setSmallIcon(resId: Int) {
//        notification.setSmallIcon(resId)
//    }
//    fun setOnlyAlertOnce(enable:Boolean) {
//        notification.setOnlyAlertOnce(enable)
//    }
//    fun setAutoCancel(enable:Boolean) {
//        notification.setAutoCancel(enable)
//    }
//    fun setOngoing(enable:Boolean) {
//        notification.setOngoing(enable)
//    }
//    fun addAction(resId: Int, title:String, onAction: () -> Unit) {
//
//    }
//
//}