package ireader.domain.services.tts_service

import androidx.compose.runtime.*
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.MenuScope
import androidx.compose.ui.window.TrayState
import ireader.core.log.Log

/**
 * Helper for integrating TTS notification with Desktop system tray
 * 
 * Usage in Main.kt:
 * ```
 * application {
 *     val trayState = rememberTrayState()
 *     
 *     Tray(
 *         state = trayState,
 *         icon = painterResource("icon.png"),
 *         menu = {
 *             TTSTrayMenu(trayState)
 *             // ... other menu items
 *         }
 *     )
 * }
 * ```
 */

/**
 * Composable function to add TTS controls to system tray menu
 */
@Composable
fun MenuScope.TTSTrayMenu(
    trayState: TrayState,
    notification: DesktopTTSNotificationImpl? = null
) {
    // Get or create notification instance
    val ttsNotification = remember(notification) {
        notification ?: (TTSNotificationFactory.create(object : TTSNotificationCallback {
            override fun onPlay() { Log.info { "TTS Play clicked" } }
            override fun onPause() { Log.info { "TTS Pause clicked" } }
            override fun onNext() { Log.info { "TTS Next clicked" } }
            override fun onPrevious() { Log.info { "TTS Previous clicked" } }
            override fun onNextParagraph() { Log.info { "TTS Next Paragraph clicked" } }
            override fun onPreviousParagraph() { Log.info { "TTS Previous Paragraph clicked" } }
            override fun onClose() { Log.info { "TTS Close clicked" } }
            override fun onNotificationClick() { Log.info { "TTS Notification clicked" } }
        }) as DesktopTTSNotificationImpl)
    }
    
    // Set tray state for notifications
    LaunchedEffect(trayState) {
        ttsNotification.setTrayState(trayState)
    }
    
    // Observe notification state
    val notificationData by ttsNotification.notificationState
    
    // Only show menu items if TTS is active
    if (notificationData != null && ttsNotification.isNotificationVisible()) {
        val data = notificationData!!
        
        Separator()
        
        // TTS Status
        Item(
            "TTS: ${if (data.isPlaying) "▶ Playing" else "⏸ Paused"}",
            onClick = {},
            enabled = false
        )
        
        // Chapter info
        if (data.title.isNotEmpty()) {
            Item(data.title, onClick = {}, enabled = false)
        }
        
        // Progress
        if (!data.isLoading && data.totalParagraphs > 0) {
            Item(
                "Paragraph ${data.currentParagraph + 1}/${data.totalParagraphs}",
                onClick = {},
                enabled = false
            )
        }
        
        Separator()
        
        // Playback controls
        Item(
            if (data.isPlaying) "⏸ Pause" else "▶ Play",
            onClick = {
                if (data.isPlaying) {
                    (ttsNotification as? DesktopTTSNotificationImpl)?.let {
                        // Callback will be triggered
                    }
                }
            }
        )
        
        Item(
            "⏮ Previous Paragraph",
            onClick = { /* Callback will be triggered */ }
        )
        
        Item(
            "⏭ Next Paragraph",
            onClick = { /* Callback will be triggered */ }
        )
        
        Separator()
        
        Item(
            "⏪ Previous Chapter",
            onClick = { /* Callback will be triggered */ }
        )
        
        Item(
            "⏩ Next Chapter",
            onClick = { /* Callback will be triggered */ }
        )
        
        Separator()
        
        Item(
            "⏹ Stop TTS",
            onClick = { /* Callback will be triggered */ }
        )
    }
}

/**
 * Remember TTS notification instance across recompositions
 */
@Composable
fun rememberTTSNotification(
    callback: TTSNotificationCallback
): DesktopTTSNotificationImpl {
    return remember(callback) {
        TTSNotificationFactory.create(callback) as DesktopTTSNotificationImpl
    }
}

/**
 * Effect to setup TTS notification with tray state
 */
@Composable
fun TTSNotificationEffect(
    trayState: TrayState,
    notification: DesktopTTSNotificationImpl
) {
    LaunchedEffect(trayState, notification) {
        notification.setTrayState(trayState)
    }
}

/**
 * Example usage in Desktop TTS Service
 */
@Composable
fun ApplicationScope.TTSNotificationExample() {
    val trayState = remember { TrayState() }
    
    val ttsNotification = rememberTTSNotification(
        callback = object : TTSNotificationCallback {
            override fun onPlay() {
                // Handle play
            }
            override fun onPause() {
                // Handle pause
            }
            override fun onNext() {
                // Handle next chapter
            }
            override fun onPrevious() {
                // Handle previous chapter
            }
            override fun onNextParagraph() {
                // Handle next paragraph
            }
            override fun onPreviousParagraph() {
                // Handle previous paragraph
            }
            override fun onClose() {
                // Handle close
            }
            override fun onNotificationClick() {
                // Handle notification click
            }
        }
    )
    
    TTSNotificationEffect(trayState, ttsNotification)
    
    // Show notification when TTS starts
    LaunchedEffect(Unit) {
        ttsNotification.show(
            TTSNotificationData(
                title = "Chapter 1: Introduction",
                subtitle = "My Book Title",
                isPlaying = true,
                currentParagraph = 0,
                totalParagraphs = 50
            )
        )
    }
}
