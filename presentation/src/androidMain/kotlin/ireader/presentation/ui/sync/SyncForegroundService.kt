package ireader.presentation.ui.sync

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Foreground service for managing sync operations on Android.
 * 
 * This service ensures that sync operations continue running even when the app
 * is in the background, and displays a persistent notification to the user.
 * 
 * Following TDD methodology - tests were written first before implementation.
 * 
 * Requirements:
 * - Must run as a foreground service (Android 8.0+)
 * - Must create and use a notification channel
 * - Must show persistent notification during sync
 * - Must update notification with progress
 * - Must stop when sync completes or is cancelled
 * 
 * Usage:
 * ```kotlin
 * // Start sync
 * val intent = Intent(context, SyncForegroundService::class.java).apply {
 *     action = SyncForegroundService.ACTION_START_SYNC
 *     putExtra(SyncForegroundService.EXTRA_DEVICE_NAME, "Device Name")
 * }
 * context.startForegroundService(intent)
 * 
 * // Update progress
 * val updateIntent = Intent(context, SyncForegroundService::class.java).apply {
 *     action = SyncForegroundService.ACTION_UPDATE_PROGRESS
 *     putExtra(SyncForegroundService.EXTRA_PROGRESS, 50)
 *     putExtra(SyncForegroundService.EXTRA_CURRENT_ITEM, "Book.epub")
 * }
 * context.startService(updateIntent)
 * 
 * // Stop sync
 * val stopIntent = Intent(context, SyncForegroundService::class.java).apply {
 *     action = SyncForegroundService.ACTION_STOP_SYNC
 * }
 * context.startService(stopIntent)
 * ```
 */
class SyncForegroundService : Service() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var batteryOptimizationManager: ireader.domain.services.sync.BatteryOptimizationManager
    private var deviceName: String = ""
    private var progress: Int = 0
    private var currentItem: String = ""
    private var currentIndex: Int = 0
    private var totalItems: Int = 0
    private var startBatteryLevel: Int = 100
    private var syncStartTime: Long = 0L
    private var bytesTransferred: Long = 0L

    companion object {
        const val CHANNEL_ID = "sync_service_channel"
        const val NOTIFICATION_ID = 1001
        const val COMPLETION_NOTIFICATION_ID = 1002
        const val ERROR_NOTIFICATION_ID = 1003

        // Actions
        const val ACTION_START_SYNC = "ireader.action.START_SYNC"
        const val ACTION_STOP_SYNC = "ireader.action.STOP_SYNC"
        const val ACTION_UPDATE_PROGRESS = "ireader.action.UPDATE_PROGRESS"
        const val ACTION_CANCEL_SYNC = "ireader.action.CANCEL_SYNC"
        const val ACTION_SYNC_COMPLETED = "ireader.action.SYNC_COMPLETED"
        const val ACTION_SYNC_FAILED = "ireader.action.SYNC_FAILED"

        // Extras
        const val EXTRA_DEVICE_NAME = "device_name"
        const val EXTRA_PROGRESS = "progress"
        const val EXTRA_CURRENT_ITEM = "current_item"
        const val EXTRA_CURRENT_INDEX = "current_index"
        const val EXTRA_TOTAL_ITEMS = "total_items"
        const val EXTRA_SYNCED_ITEMS = "synced_items"
        const val EXTRA_DURATION = "duration"
        const val EXTRA_ERROR_MESSAGE = "error_message"
        const val EXTRA_ERROR_SUGGESTION = "error_suggestion"

        /**
         * Callback for handling sync cancellation.
         * This is set by the SyncViewModel to receive cancel events from the notification.
         */
        var onCancelCallback: (() -> Unit)? = null

        /**
         * Helper method to start the sync service.
         */
        fun startSync(context: Context, deviceName: String) {
            val intent = Intent(context, SyncForegroundService::class.java).apply {
                action = ACTION_START_SYNC
                putExtra(EXTRA_DEVICE_NAME, deviceName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Helper method to update sync progress.
         */
        fun updateProgress(context: Context, progress: Int, currentItem: String, currentIndex: Int = 0, totalItems: Int = 0) {
            val intent = Intent(context, SyncForegroundService::class.java).apply {
                action = ACTION_UPDATE_PROGRESS
                putExtra(EXTRA_PROGRESS, progress)
                putExtra(EXTRA_CURRENT_ITEM, currentItem)
                putExtra(EXTRA_CURRENT_INDEX, currentIndex)
                putExtra(EXTRA_TOTAL_ITEMS, totalItems)
            }
            context.startService(intent)
        }

        /**
         * Helper method to stop the sync service.
         */
        fun stopSync(context: Context) {
            val intent = Intent(context, SyncForegroundService::class.java).apply {
                action = ACTION_STOP_SYNC
            }
            context.startService(intent)
        }

        /**
         * Helper method to cancel the sync operation.
         */
        fun cancelSync(context: Context) {
            val intent = Intent(context, SyncForegroundService::class.java).apply {
                action = ACTION_CANCEL_SYNC
            }
            context.startService(intent)
        }

        /**
         * Helper method to show completion notification.
         */
        fun showCompletionNotification(context: Context, deviceName: String, syncedItems: Int, durationMs: Long) {
            val intent = Intent(context, SyncForegroundService::class.java).apply {
                action = ACTION_SYNC_COMPLETED
                putExtra(EXTRA_DEVICE_NAME, deviceName)
                putExtra(EXTRA_SYNCED_ITEMS, syncedItems)
                putExtra(EXTRA_DURATION, durationMs)
            }
            context.startService(intent)
        }

        /**
         * Helper method to show error notification.
         */
        fun showErrorNotification(
            context: Context,
            deviceName: String?,
            errorMessage: String,
            suggestion: String? = null
        ) {
            val intent = Intent(context, SyncForegroundService::class.java).apply {
                action = ACTION_SYNC_FAILED
                putExtra(EXTRA_DEVICE_NAME, deviceName)
                putExtra(EXTRA_ERROR_MESSAGE, errorMessage)
                suggestion?.let { putExtra(EXTRA_ERROR_SUGGESTION, it) }
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        batteryOptimizationManager = ireader.domain.services.sync.BatteryOptimizationManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SYNC -> {
                deviceName = intent.getStringExtra(EXTRA_DEVICE_NAME) ?: "Unknown Device"
                progress = 0
                currentItem = ""
                currentIndex = 0
                totalItems = 0
                
                // Battery optimization: acquire wake lock and record start state
                batteryOptimizationManager.acquireWakeLock()
                startBatteryLevel = batteryOptimizationManager.getBatteryLevel()
                syncStartTime = System.currentTimeMillis()
                bytesTransferred = 0L
                
                startForeground(NOTIFICATION_ID, createNotification())
            }
            ACTION_UPDATE_PROGRESS -> {
                progress = intent.getIntExtra(EXTRA_PROGRESS, 0)
                currentItem = intent.getStringExtra(EXTRA_CURRENT_ITEM) ?: ""
                currentIndex = intent.getIntExtra(EXTRA_CURRENT_INDEX, 0)
                totalItems = intent.getIntExtra(EXTRA_TOTAL_ITEMS, 0)
                
                // Update battery state periodically
                batteryOptimizationManager.updateBatteryLevel()
                batteryOptimizationManager.updateBatterySaverState()
                
                updateNotification()
            }
            ACTION_SYNC_COMPLETED -> {
                val completedDeviceName = intent.getStringExtra(EXTRA_DEVICE_NAME) ?: deviceName
                val syncedItems = intent.getIntExtra(EXTRA_SYNCED_ITEMS, 0)
                val durationMs = intent.getLongExtra(EXTRA_DURATION, 0L)
                
                // Battery optimization: log usage and release wake lock
                val endBatteryLevel = batteryOptimizationManager.getBatteryLevel()
                batteryOptimizationManager.logBatteryUsage(
                    startBatteryLevel,
                    endBatteryLevel,
                    durationMs,
                    bytesTransferred
                )
                batteryOptimizationManager.releaseWakeLock()
                
                showCompletionNotification(completedDeviceName, syncedItems, durationMs)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_SYNC_FAILED -> {
                val failedDeviceName = intent.getStringExtra(EXTRA_DEVICE_NAME)
                val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE) ?: "Unknown error"
                val suggestion = intent.getStringExtra(EXTRA_ERROR_SUGGESTION)
                
                // Battery optimization: release wake lock on error
                batteryOptimizationManager.releaseWakeLock()
                
                showErrorNotification(failedDeviceName, errorMessage, suggestion)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_STOP_SYNC -> {
                // Battery optimization: release wake lock
                batteryOptimizationManager.releaseWakeLock()
                
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_CANCEL_SYNC -> {
                // Invoke the cancel callback to notify the ViewModel
                onCancelCallback?.invoke()
                
                // Battery optimization: release wake lock
                batteryOptimizationManager.releaseWakeLock()
                
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Create the notification channel for Android 8.0+.
     * Required for foreground services on Android O and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sync Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for WiFi sync operations"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Create the initial notification for the foreground service.
     */
    private fun createNotification(): Notification {
        // Build content text with detailed progress information
        val contentText = buildContentText()

        // Create cancel action PendingIntent
        val cancelIntent = Intent(this, SyncForegroundService::class.java).apply {
            action = ACTION_CANCEL_SYNC
        }
        val cancelPendingIntent = PendingIntent.getService(
            this,
            0,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WiFi Sync in Progress")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_notify_sync) // Using system sync icon
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOnlyAlertOnce(true) // Prevent notification sound/vibration on updates
            .addAction(
                android.R.drawable.ic_delete, // Cancel icon
                "Cancel",
                cancelPendingIntent
            )
            .build()
    }

    /**
     * Build the content text for the notification based on current state.
     * Shows: current item, progress percentage, and item count (e.g., "Syncing 5 of 20 books")
     */
    private fun buildContentText(): String {
        return when {
            // Show detailed progress when we have all information
            currentItem.isNotEmpty() && totalItems > 0 -> {
                "Syncing $currentIndex of $totalItems: $currentItem ($progress%)"
            }
            // Show item and percentage when we have current item
            currentItem.isNotEmpty() -> {
                "Syncing: $currentItem ($progress%)"
            }
            // Show item count when available
            totalItems > 0 -> {
                "Syncing $currentIndex of $totalItems items ($progress%)"
            }
            // Show basic progress
            progress > 0 -> {
                "Syncing with $deviceName... ($progress%)"
            }
            // Initial state
            else -> {
                "Syncing with $deviceName..."
            }
        }
    }

    /**
     * Update the notification with current progress.
     */
    private fun updateNotification() {
        val notification = createNotification()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Show a completion notification when sync finishes successfully.
     * 
     * This notification:
     * - Shows device name, number of items synced, and duration
     * - Uses a success/checkmark icon
     * - Is dismissible (not ongoing)
     * - Has a tap action to open the sync screen
     * - Uses default priority for visibility
     * - Auto-dismisses when tapped
     */
    private fun showCompletionNotification(deviceName: String, syncedItems: Int, durationMs: Long) {
        // Format duration in human-readable format
        val durationText = formatDuration(durationMs)
        
        // Create content text with all information
        val contentText = "Synced $syncedItems ${if (syncedItems == 1) "item" else "items"} in $durationText"
        
        // Create tap action to open sync screen (placeholder intent for now)
        val tapIntent = Intent(this, SyncForegroundService::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sync Complete with $deviceName")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_notify_sync_noanim) // Success/checkmark icon
            .setAutoCancel(true) // Dismiss when tapped
            .setOngoing(false) // Not ongoing, can be dismissed
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Default priority for visibility
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(COMPLETION_NOTIFICATION_ID, notification)
    }

    /**
     * Show an error notification when sync fails.
     * 
     * This notification:
     * - Shows device name (if available) and error message
     * - Includes helpful suggestion if provided
     * - Uses an error/warning icon
     * - Is dismissible (not ongoing)
     * - Has a tap action to open the sync screen
     * - Uses higher priority than completion notification for visibility
     */
    private fun showErrorNotification(deviceName: String?, errorMessage: String, suggestion: String?) {
        // Build content text with error message and suggestion
        val contentText = if (suggestion != null) {
            "$errorMessage. $suggestion"
        } else {
            errorMessage
        }
        
        // Build title with device name if available
        val title = if (deviceName != null) {
            "Sync Failed with $deviceName"
        } else {
            "Sync Failed"
        }
        
        // Create tap action to open sync screen (placeholder intent for now)
        val tapIntent = Intent(this, SyncForegroundService::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_notify_error) // Error/warning icon
            .setAutoCancel(true) // Dismiss when tapped
            .setOngoing(false) // Not ongoing, can be dismissed
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Higher priority for visibility
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText)) // Allow long text
            .build()
        
        notificationManager.notify(ERROR_NOTIFICATION_ID, notification)
    }

    /**
     * Format duration in milliseconds to human-readable format.
     * Examples:
     * - 5000ms -> "5s"
     * - 65000ms -> "1m 5s"
     * - 125000ms -> "2m 5s"
     * - 3665000ms -> "1h 1m"
     */
    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000).toInt()
        val minutes = seconds / 60
        val hours = minutes / 60
        
        return when {
            hours > 0 -> {
                val remainingMinutes = minutes % 60
                if (remainingMinutes > 0) {
                    "${hours}h ${remainingMinutes}m"
                } else {
                    "${hours}h"
                }
            }
            minutes > 0 -> {
                val remainingSeconds = seconds % 60
                if (remainingSeconds > 0) {
                    "${minutes}m ${remainingSeconds}s"
                } else {
                    "${minutes}m"
                }
            }
            else -> "${seconds}s"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure wake lock is released when service is destroyed
        batteryOptimizationManager.cleanup()
    }
}
