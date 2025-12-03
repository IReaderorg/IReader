package ireader.presentation.ui.settings.notifications

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.prefs.PreferenceStore
import ireader.presentation.ui.core.utils.asStateFlow
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

/**
 * ViewModel for the enhanced notification settings screen.
 * Manages comprehensive notification preferences with granular control over types and channels.
 */
class SettingsNotificationViewModel(
    private val preferenceStore: PreferenceStore
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
    
    // Dialog states
    var showQuietHoursStartDialog by mutableStateOf(false)
        private set
    var showQuietHoursEndDialog by mutableStateOf(false)
        private set
    
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
    }
    
    fun setNotificationVibration(enabled: Boolean) {
        preferenceStore.getBoolean("notification_vibration", true).set(enabled)
    }
    
    fun setNotificationLED(enabled: Boolean) {
        preferenceStore.getBoolean("notification_led", true).set(enabled)
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
        // TODO: Implement platform-specific notification channel settings
        // On Android, this should open the system notification settings for the app
    }
    
    fun sendTestNotifications() {
        // TODO: Implement test notification sending
        sendTestLibraryNotification()
        sendTestDownloadNotification()
        sendTestSystemNotification()
    }
    
    private fun sendTestLibraryNotification() {
        // TODO: Send a test library update notification
    }
    
    private fun sendTestDownloadNotification() {
        // TODO: Send a test download notification
    }
    
    private fun sendTestSystemNotification() {
        // TODO: Send a test system notification
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
    
    private fun getCurrentTime(): Pair<Int, Int> {
        // TODO: Implement platform-specific current time retrieval
        return 12 to 0 // Placeholder
    }
    
    private fun isTimeBetween(time: Pair<Int, Int>, start: Pair<Int, Int>, end: Pair<Int, Int>): Boolean {
        val timeMinutes = time.first * 60 + time.second
        val startMinutes = start.first * 60 + start.second
        val endMinutes = end.first * 60 + end.second
        
        return timeMinutes in startMinutes..endMinutes
    }
    
    // Notification channel management
    fun createNotificationChannels() {
        // TODO: Implement platform-specific notification channel creation
        // This should create separate channels for:
        // - Library updates
        // - New chapters
        // - Downloads
        // - System notifications
        // - Errors
    }
    
    fun updateNotificationChannels() {
        // TODO: Update existing notification channels with current preferences
    }
    
    // Notification permission handling
    fun checkNotificationPermission(): Boolean {
        // TODO: Implement platform-specific notification permission check
        return true // Placeholder
    }
    
    fun requestNotificationPermission() {
        // TODO: Implement platform-specific notification permission request
    }
}
