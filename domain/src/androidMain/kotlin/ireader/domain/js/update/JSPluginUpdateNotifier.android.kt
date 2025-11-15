package ireader.domain.js.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import ireader.domain.js.models.PluginUpdate

/**
 * Android implementation of plugin update notifier.
 */
actual class JSPluginUpdateNotifier(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "js_plugin_updates"
        private const val NOTIFICATION_ID = 1001
    }
    
    init {
        createNotificationChannel()
    }
    
    actual fun showUpdateNotification(updates: List<PluginUpdate>) {
        if (updates.isEmpty()) return
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val title = if (updates.size == 1) {
            "Plugin Update Available"
        } else {
            "${updates.size} Plugin Updates Available"
        }
        
        val text = if (updates.size == 1) {
            "${updates[0].pluginId}: ${updates[0].currentVersion} → ${updates[0].newVersion}"
        } else {
            "Tap to view and install updates"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    actual fun cancelUpdateNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Plugin Updates"
            val descriptionText = "Notifications about JavaScript plugin updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
