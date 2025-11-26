package ireader.domain.services.tts_service

import androidx.compose.runtime.*
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.TrayState
import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.awt.SystemTray
import java.awt.TrayIcon

/**
 * Desktop implementation of TTS Notification Factory
 */
actual object TTSNotificationFactory {
    actual fun create(callback: TTSNotificationCallback): TTSNotification {
        return DesktopTTSNotificationImpl(callback)
    }
}

/**
 * Desktop implementation using system tray and desktop notifications
 * 
 * Features:
 * - System tray icon with menu
 * - Desktop notifications (Windows Toast, macOS Notification Center, Linux notify-send)
 * - Playback controls in tray menu
 */
class DesktopTTSNotificationImpl(
    private val callback: TTSNotificationCallback
) : TTSNotification {
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var trayState: TrayState? = null
    private var currentData: TTSNotificationData? = null
    private var isVisible = false
    
    // State for UI updates
    private val _notificationState = mutableStateOf<TTSNotificationData?>(null)
    val notificationState: State<TTSNotificationData?> = _notificationState
    
    /**
     * Set the TrayState for system tray integration
     * This should be called from the main Compose Desktop application
     */
    fun setTrayState(state: TrayState) {
        this.trayState = state
    }
    
    override fun show(data: TTSNotificationData) {
        currentData = data
        _notificationState.value = data
        isVisible = true
        
        // Show desktop notification
        showDesktopNotification(data)
        
        // Update tray tooltip
        updateTrayTooltip(data)
    }
    
    override fun hide() {
        isVisible = false
        currentData = null
        _notificationState.value = null
    }
    
    override fun updatePlaybackState(isPlaying: Boolean) {
        currentData?.let { data ->
            val updated = data.copy(isPlaying = isPlaying)
            currentData = updated
            _notificationState.value = updated
            updateTrayTooltip(updated)
        }
    }
    
    override fun updateProgress(current: Int, total: Int) {
        currentData?.let { data ->
            val updated = data.copy(
                currentParagraph = current,
                totalParagraphs = total
            )
            currentData = updated
            _notificationState.value = updated
            updateTrayTooltip(updated)
        }
    }
    
    /**
     * Show a desktop notification popup
     */
    private fun showDesktopNotification(data: TTSNotificationData) {
        scope.launch {
            try {
                val title = when {
                    data.isLoading -> "Loading..."
                    else -> "Paragraph ${data.currentParagraph + 1}/${data.totalParagraphs}"
                }
                
                val message = buildString {
                    append(data.title) // Chapter name
                    if (data.subtitle.isNotEmpty()) {
                        append("\n")
                        append(data.subtitle) // Book title
                    }
                }
                
                // Use Compose Desktop's notification system
                trayState?.sendNotification(
                    Notification(
                        title = title,
                        message = message,
                        type = Notification.Type.Info
                    )
                )
                
                Log.info { "Desktop notification shown: $title" }
            } catch (e: Exception) {
                Log.error { "Failed to show desktop notification: ${e.message}" }
                // Fallback to AWT system tray notification
                showAWTNotification(data)
            }
        }
    }
    
    /**
     * Fallback to AWT system tray notification
     */
    private fun showAWTNotification(data: TTSNotificationData) {
        try {
            if (!SystemTray.isSupported()) {
                Log.warn { "System tray not supported on this platform" }
                return
            }
            
            val tray = SystemTray.getSystemTray()
            val trayIcons = tray.trayIcons
            
            if (trayIcons.isEmpty()) {
                Log.warn { "No tray icon available for notification" }
                return
            }
            
            val trayIcon = trayIcons[0]
            val title = when {
                data.isLoading -> "Loading..."
                else -> "Paragraph ${data.currentParagraph + 1}/${data.totalParagraphs}"
            }
            
            val message = buildString {
                append(data.title) // Chapter name
                if (data.subtitle.isNotEmpty()) {
                    append("\n")
                    append(data.subtitle) // Book title
                }
            }
            
            trayIcon.displayMessage(
                title,
                message,
                TrayIcon.MessageType.INFO
            )
        } catch (e: Exception) {
            Log.error { "Failed to show AWT notification: ${e.message}" }
        }
    }
    
    /**
     * Update system tray tooltip
     */
    private fun updateTrayTooltip(data: TTSNotificationData) {
        try {
            if (!SystemTray.isSupported()) return
            
            val tray = SystemTray.getSystemTray()
            val trayIcons = tray.trayIcons
            
            if (trayIcons.isEmpty()) return
            
            val trayIcon = trayIcons[0]
            val tooltip = buildString {
                append("iReader TTS")
                if (data.isLoading) {
                    append(" - Loading...")
                } else {
                    append(" - ")
                    append(if (data.isPlaying) "Playing" else "Paused")
                    append(" (${data.currentParagraph + 1}/${data.totalParagraphs})")
                }
                append("\n")
                append(data.title) // Chapter name
                if (data.subtitle.isNotEmpty()) {
                    append("\n")
                    append(data.subtitle) // Book title
                }
            }
            
            trayIcon.toolTip = tooltip
        } catch (e: Exception) {
            Log.error { "Failed to update tray tooltip: ${e.message}" }
        }
    }
    
    /**
     * Get current notification data
     */
    fun getCurrentData(): TTSNotificationData? = currentData
    
    /**
     * Check if notification is visible
     */
    fun isNotificationVisible(): Boolean = isVisible
    
    /**
     * Create tray menu items for TTS controls
     * This should be called from the main application's tray menu
     */
    fun createTrayMenuItems(): List<TrayMenuItem> {
        val data = currentData ?: return emptyList()
        
        return buildList {
            // Title
            add(TrayMenuItem.Label("TTS: ${if (data.isPlaying) "Playing" else "Paused"}"))
            add(TrayMenuItem.Separator)
            
            // Chapter info
            if (data.title.isNotEmpty()) {
                add(TrayMenuItem.Label(data.title))
            }
            if (data.subtitle.isNotEmpty()) {
                add(TrayMenuItem.Label(data.subtitle))
            }
            
            // Progress
            if (!data.isLoading && data.totalParagraphs > 0) {
                add(TrayMenuItem.Label("Paragraph ${data.currentParagraph + 1}/${data.totalParagraphs}"))
            }
            
            add(TrayMenuItem.Separator)
            
            // Controls
            add(TrayMenuItem.Action(
                label = if (data.isPlaying) "Pause" else "Play",
                onClick = {
                    if (data.isPlaying) callback.onPause() else callback.onPlay()
                }
            ))
            
            add(TrayMenuItem.Action(
                label = "Previous Paragraph",
                onClick = { callback.onPreviousParagraph() }
            ))
            
            add(TrayMenuItem.Action(
                label = "Next Paragraph",
                onClick = { callback.onNextParagraph() }
            ))
            
            add(TrayMenuItem.Separator)
            
            add(TrayMenuItem.Action(
                label = "Previous Chapter",
                onClick = { callback.onPrevious() }
            ))
            
            add(TrayMenuItem.Action(
                label = "Next Chapter",
                onClick = { callback.onNext() }
            ))
            
            add(TrayMenuItem.Separator)
            
            add(TrayMenuItem.Action(
                label = "Stop TTS",
                onClick = { callback.onClose() }
            ))
        }
    }
}

/**
 * Tray menu item types
 */
sealed class TrayMenuItem {
    data class Label(val text: String) : TrayMenuItem()
    data class Action(val label: String, val onClick: () -> Unit) : TrayMenuItem()
    object Separator : TrayMenuItem()
}
