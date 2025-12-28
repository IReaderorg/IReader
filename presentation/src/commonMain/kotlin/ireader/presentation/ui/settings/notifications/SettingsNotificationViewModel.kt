package ireader.presentation.ui.settings.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.core.prefs.PreferenceStore
import ireader.domain.data.repository.NotificationRepository
import ireader.domain.models.notification.NotificationChannel
import ireader.domain.models.notification.NotificationImportance
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel for the enhanced notification settings screen.
 * Manages comprehensive notification preferences with granular control over types and channels.
 */
class SettingsNotificationViewModel(
    private val preferenceStore: PreferenceStore,
    private val notificationRepository: NotificationRepository? = null,
    private val notificationHelper: NotificationHelper = DefaultNotificationHelper()
) : BaseViewModel() {
    
    // Library notification preferences
    val libraryUpdateNotifications: StateFlow<Boolean> = preferenceStore.getBoolean("library_update_notifications", true).stateIn(scope)
    val newChapterNotifications: StateFlow<Boolean> = preferenceStore.getBoolean("new_chapter_notifications", true).stateIn(scope)
    
    // Download notification preferences
    val downloadProgressNotifications: StateFlow<Boolean> = preferenceStore.getBoolean("download_progress_notifications", true).stateIn(scope)
    val downloadCompleteNotifications: StateFlow<Boolean> = preferenceStore.getBoolean("download_complete_notifications", true).stateIn(scope)
    
    // System notification preferences
    val backupNotifications: StateFlow<Boolean> = preferenceStore.getBoolean("backup_notifications", true).stateIn(scope)
    val appUpdateNotifications: StateFlow<Boolean> = preferenceStore.getBoolean("app_update_notifications", true).stateIn(scope)
    val extensionUpdateNotifications: StateFlow<Boolean> = preferenceStore.getBoolean("extension_update_notifications", true).stateIn(scope)
    val errorNotifications: StateFlow<Boolean> = preferenceStore.getBoolean("error_notifications", true).stateIn(scope)
    
    // Notification behavior preferences
    val notificationSound: StateFlow<Boolean> = preferenceStore.getBoolean("notification_sound", true).stateIn(scope)
    val notificationVibration: StateFlow<Boolean> = preferenceStore.getBoolean("notification_vibration", true).stateIn(scope)
    val notificationLED: StateFlow<Boolean> = preferenceStore.getBoolean("notification_led", true).stateIn(scope)
    val groupNotifications: StateFlow<Boolean> = preferenceStore.getBoolean("group_notifications", true).stateIn(scope)
    
    // Quiet hours preferences
    val quietHoursEnabled: StateFlow<Boolean> = preferenceStore.getBoolean("quiet_hours_enabled", false).stateIn(scope)
    val quietHoursStart: StateFlow<Pair<Int, Int>> = preferenceStore.getString("quiet_hours_start", "22:00").stateIn(scope).map { parseTime(it) }.stateIn(scope, SharingStarted.Eagerly, parseTime("22:00"))
    val quietHoursEnd: StateFlow<Pair<Int, Int>> = preferenceStore.getString("quiet_hours_end", "08:00").stateIn(scope).map { parseTime(it) }.stateIn(scope, SharingStarted.Eagerly, parseTime("08:00"))
    
    // Permission state
    private val _hasNotificationPermission = MutableStateFlow(notificationHelper.hasNotificationPermission())
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()
    
    // Snackbar message
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    
    // Dialog states
    var showQuietHoursStartDialog by mutableStateOf(false)
        private set
    var showQuietHoursEndDialog by mutableStateOf(false)
        private set
    
    init {
        // Create notification channels on initialization
        createNotificationChannels()
    }
    
    // Library notification functions
    fun setLibraryUpdateNotifications(enabled: Boolean) {
        preferenceStore.getBoolean("library_update_notifications", true).set(enabled)
    }
    
    fun setNewChapterNotifications(enabled: Boolean) {
        preferenceStore.getBoolean("new_chapter_notifications", true).set(enabled)
    }
    
    // Download notification functions
    fun setDownloadProgressNotifications(enabled: Boolean) {
        preferenceStore.getBoolean("download_progress_notifications", true).set(enabled)
    }
    
    fun setDownloadCompleteNotifications(enabled: Boolean) {
        preferenceStore.getBoolean("download_complete_notifications", true).set(enabled)
    }
    
    // System notification functions
    fun setBackupNotifications(enabled: Boolean) {
        preferenceStore.getBoolean("backup_notifications", true).set(enabled)
    }
    
    fun setAppUpdateNotifications(enabled: Boolean) {
        preferenceStore.getBoolean("app_update_notifications", true).set(enabled)
    }
    
    fun setExtensionUpdateNotifications(enabled: Boolean) {
        preferenceStore.getBoolean("extension_update_notifications", true).set(enabled)
    }
    
    fun setErrorNotifications(enabled: Boolean) {
        preferenceStore.getBoolean("error_notifications", true).set(enabled)
    }
    
    // Notification behavior functions
    fun setNotificationSound(enabled: Boolean) {
        preferenceStore.getBoolean("notification_sound", true).set(enabled)
        updateNotificationChannels()
    }
    
    fun setNotificationVibration(enabled: Boolean) {
        preferenceStore.getBoolean("notification_vibration", true).set(enabled)
        updateNotificationChannels()
    }
    
    fun setNotificationLED(enabled: Boolean) {
        preferenceStore.getBoolean("notification_led", true).set(enabled)
        updateNotificationChannels()
    }
    
    fun setGroupNotifications(enabled: Boolean) {
        preferenceStore.getBoolean("group_notifications", true).set(enabled)
    }
    
    // Quiet hours functions
    fun setQuietHoursEnabled(enabled: Boolean) {
        preferenceStore.getBoolean("quiet_hours_enabled", false).set(enabled)
    }
    
    fun showQuietHoursStartDialog() {
        showQuietHoursStartDialog = true
    }
    
    fun dismissQuietHoursStartDialog() {
        showQuietHoursStartDialog = false
    }
    
    fun setQuietHoursStart(time: Pair<Int, Int>) {
        val timeString = "${time.first.toString().padStart(2, '0')}:${time.second.toString().padStart(2, '0')}"
        preferenceStore.getString("quiet_hours_start", "22:00").set(timeString)
    }
    
    fun showQuietHoursEndDialog() {
        showQuietHoursEndDialog = true
    }
    
    fun dismissQuietHoursEndDialog() {
        showQuietHoursEndDialog = false
    }
    
    fun setQuietHoursEnd(time: Pair<Int, Int>) {
        val timeString = "${time.first.toString().padStart(2, '0')}:${time.second.toString().padStart(2, '0')}"
        preferenceStore.getString("quiet_hours_end", "08:00").set(timeString)
    }
    
    // Advanced functions
    fun openNotificationChannelSettings() {
        notificationHelper.openNotificationSettings()
    }
    
    fun sendTestNotifications() {
        scope.launch {
            try {
                if (!hasNotificationPermission.value) {
                    showSnackbar("Notification permission not granted")
                    return@launch
                }
                
                sendTestLibraryNotification()
                sendTestDownloadNotification()
                sendTestSystemNotification()
                showSnackbar("Test notifications sent")
            } catch (e: Exception) {
                Log.error(e, "Failed to send test notifications")
                showSnackbar("Failed to send test notifications: ${e.message}")
            }
        }
    }
    
    private suspend fun sendTestLibraryNotification() {
        if (!libraryUpdateNotifications.value) return
        
        notificationRepository?.showNotification(
            ireader.domain.models.notification.IReaderNotification(
                id = 9001,
                channelId = CHANNEL_LIBRARY,
                title = "Test Library Update",
                content = "This is a test library update notification",
                ongoing = false
            )
        )
    }
    
    private suspend fun sendTestDownloadNotification() {
        if (!downloadCompleteNotifications.value) return
        
        notificationRepository?.showNotification(
            ireader.domain.models.notification.IReaderNotification(
                id = 9002,
                channelId = CHANNEL_DOWNLOADER,
                title = "Test Download Complete",
                content = "This is a test download notification",
                ongoing = false
            )
        )
    }
    
    private suspend fun sendTestSystemNotification() {
        notificationRepository?.showNotification(
            ireader.domain.models.notification.IReaderNotification(
                id = 9003,
                channelId = CHANNEL_COMMON,
                title = "Test System Notification",
                content = "This is a test system notification",
                ongoing = false
            )
        )
    }
    
    // Utility functions
    private fun parseTime(timeString: String): Pair<Int, Int> {
        return try {
            val parts = timeString.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                if (hour in 0..23 && minute in 0..59) {
                    hour to minute
                } else {
                    22 to 0 // Default fallback
                }
            } else {
                22 to 0 // Default fallback
            }
        } catch (e: Exception) {
            22 to 0 // Default fallback
        }
    }
    
    fun isInQuietHours(): Boolean {
        if (!quietHoursEnabled.value) return false
        
        val now = getCurrentTime()
        val start = quietHoursStart.value
        val end = quietHoursEnd.value
        
        return if (start.first < end.first || (start.first == end.first && start.second < end.second)) {
            // Same day quiet hours (e.g., 22:00 to 23:00)
            isTimeBetween(now, start, end)
        } else {
            // Overnight quiet hours (e.g., 22:00 to 08:00)
            isTimeBetween(now, start, 23 to 59) || isTimeBetween(now, 0 to 0, end)
        }
    }
    
    @OptIn(kotlin.time.ExperimentalTime::class)
    private fun getCurrentTime(): Pair<Int, Int> {
        val now = kotlin.time.Clock.System.now()
        val instant = kotlinx.datetime.Instant.fromEpochMilliseconds(now.toEpochMilliseconds())
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.hour to localDateTime.minute
    }
    
    private fun isTimeBetween(time: Pair<Int, Int>, start: Pair<Int, Int>, end: Pair<Int, Int>): Boolean {
        val timeMinutes = time.first * 60 + time.second
        val startMinutes = start.first * 60 + start.second
        val endMinutes = end.first * 60 + end.second
        
        return timeMinutes in startMinutes..endMinutes
    }
    
    // Notification channel management
    fun createNotificationChannels() {
        scope.launch {
            try {
                val channels = listOf(
                    NotificationChannel(
                        id = CHANNEL_COMMON,
                        name = "Common",
                        description = "General notifications",
                        importance = NotificationImportance.LOW
                    ),
                    NotificationChannel(
                        id = CHANNEL_LIBRARY,
                        name = "Library Updates",
                        description = "Notifications for library updates and new chapters",
                        importance = NotificationImportance.DEFAULT
                    ),
                    NotificationChannel(
                        id = CHANNEL_DOWNLOADER,
                        name = "Downloads",
                        description = "Download progress and completion notifications",
                        importance = NotificationImportance.LOW
                    ),
                    NotificationChannel(
                        id = CHANNEL_BACKUP_RESTORE,
                        name = "Backup & Restore",
                        description = "Backup and restore operation notifications",
                        importance = NotificationImportance.DEFAULT
                    ),
                    NotificationChannel(
                        id = CHANNEL_CRASH_LOGS,
                        name = "Crash Logs",
                        description = "Crash and error notifications",
                        importance = NotificationImportance.HIGH
                    ),
                    NotificationChannel(
                        id = CHANNEL_INCOGNITO,
                        name = "Incognito Mode",
                        description = "Incognito mode status notifications",
                        importance = NotificationImportance.LOW
                    )
                )
                
                notificationRepository?.createNotificationChannels(channels)
                Log.debug { "Notification channels created" }
            } catch (e: Exception) {
                Log.error(e, "Failed to create notification channels")
            }
        }
    }
    
    fun updateNotificationChannels() {
        // Channels are updated by recreating them with new settings
        createNotificationChannels()
    }
    
    // Notification permission handling
    fun checkNotificationPermission(): Boolean {
        val hasPermission = notificationHelper.hasNotificationPermission()
        _hasNotificationPermission.value = hasPermission
        return hasPermission
    }
    
    fun requestNotificationPermission() {
        notificationHelper.requestNotificationPermission()
    }
    
    private fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }
    
    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
    
    companion object {
        const val CHANNEL_COMMON = "common_channel"
        const val CHANNEL_LIBRARY = "library_channel"
        const val CHANNEL_DOWNLOADER = "downloader_channel"
        const val CHANNEL_BACKUP_RESTORE = "backup_restore_channel"
        const val CHANNEL_CRASH_LOGS = "crash_logs_channel"
        const val CHANNEL_INCOGNITO = "incognito_channel"
    }
}

/**
 * Interface for platform-specific notification operations
 */
interface NotificationHelper {
    fun hasNotificationPermission(): Boolean
    fun requestNotificationPermission()
    fun openNotificationSettings()
}

/**
 * Default implementation for commonMain
 */
class DefaultNotificationHelper : NotificationHelper {
    override fun hasNotificationPermission(): Boolean = true
    override fun requestNotificationPermission() {}
    override fun openNotificationSettings() {}
}
