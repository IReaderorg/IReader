package ireader.domain.utils


expect class NotificationManager {
    val scope : Any?


    fun show(id: Int, notification: Any)
    fun cancel(id: Int)
}