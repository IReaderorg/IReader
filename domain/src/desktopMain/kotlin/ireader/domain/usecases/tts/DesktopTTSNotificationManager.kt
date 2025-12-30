package ireader.domain.usecases.tts

import ireader.core.log.Log
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import java.awt.Image
import java.awt.MenuItem
import java.awt.PopupMenu
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

/**
 * Desktop implementation of TTS notification manager
 * 
 * Uses system tray notifications on desktop platforms:
 * - Windows: System tray with balloon notifications
 * - macOS: System tray (menu bar) with notifications
 * - Linux: System tray with notifications
 */
class DesktopTTSNotificationManager : TTSNotificationManager {
    
    private var callback: TTSNotificationCallback? = null
    private var currentState: TTSNotificationState? = null
    private var isShowing = false
    private var trayIcon: TrayIcon? = null
    private var currentBook: Book? = null
    private var currentChapter: Chapter? = null
    
    override fun showNotification(
        book: Book,
        chapter: Chapter,
        state: TTSNotificationState
    ) {
        this.currentState = state
        this.currentBook = book
        this.currentChapter = chapter
        this.isShowing = true
        
        Log.info { "Desktop TTS notification: ${book.title} - ${chapter.name}" }
        
        // Show system tray notification if supported
        if (SystemTray.isSupported()) {
            showSystemTrayNotification(book, chapter, state)
        } else {
            Log.warn { "System tray not supported on this platform" }
        }
    }
    
    override fun updateNotification(state: TTSNotificationState) {
        if (!isShowing) return
        
        this.currentState = state
        
        Log.debug { "Desktop TTS notification updated: ${state.ttsProvider}" }
        
        // Update tray icon tooltip with current state
        trayIcon?.toolTip = buildTooltip()
    }
    
    override fun hideNotification() {
        isShowing = false
        currentState = null
        currentBook = null
        currentChapter = null
        
        // Remove tray icon
        trayIcon?.let { icon ->
            try {
                SystemTray.getSystemTray().remove(icon)
            } catch (e: Exception) {
                Log.warn { "Failed to remove tray icon: ${e.message}" }
            }
        }
        trayIcon = null
        
        Log.info { "Desktop TTS notification hidden" }
    }
    
    override fun isNotificationShowing(): Boolean = isShowing
    
    override fun setNotificationCallback(callback: TTSNotificationCallback) {
        this.callback = callback
    }
    
    override fun cleanup() {
        hideNotification()
        callback = null
    }
    
    private fun showSystemTrayNotification(book: Book, chapter: Chapter, state: TTSNotificationState) {
        try {
            val systemTray = SystemTray.getSystemTray()
            
            // Remove existing icon if any
            trayIcon?.let { systemTray.remove(it) }
            
            // Create tray icon image
            val image = createTrayImage()
            
            // Create popup menu with controls
            val popup = PopupMenu().apply {
                add(MenuItem("▶ Play/Pause").apply {
                    addActionListener { callback?.onPlayPause() }
                })
                add(MenuItem("⏹ Stop").apply {
                    addActionListener { callback?.onStop() }
                })
                addSeparator()
                add(MenuItem("⏮ Previous").apply {
                    addActionListener { callback?.onPrevious() }
                })
                add(MenuItem("⏭ Next").apply {
                    addActionListener { callback?.onNext() }
                })
                addSeparator()
                add(MenuItem("Close").apply {
                    addActionListener { hideNotification() }
                })
            }
            
            // Create and add tray icon
            trayIcon = TrayIcon(image, buildTooltip(), popup).apply {
                isImageAutoSize = true
                addActionListener { callback?.onPlayPause() }
            }
            
            systemTray.add(trayIcon)
            
            // Show balloon notification
            trayIcon?.displayMessage(
                "IReader TTS",
                "Now reading: ${book.title}\n${chapter.name}",
                TrayIcon.MessageType.INFO
            )
            
        } catch (e: Exception) {
            Log.error { "Failed to show system tray notification" }
        }
    }
    
    private fun createTrayImage(): Image {
        return try {
            // Try to load app icon from resources
            val iconUrl = javaClass.getResource("/icons/tray_icon.png")
                ?: javaClass.getResource("/icon.png")
            
            if (iconUrl != null) {
                Toolkit.getDefaultToolkit().getImage(iconUrl)
            } else {
                // Create a simple default icon
                createDefaultTrayImage()
            }
        } catch (e: Exception) {
            Log.warn { "Failed to load tray icon, using default: ${e.message}" }
            createDefaultTrayImage()
        }
    }
    
    private fun createDefaultTrayImage(): Image {
        // Create a simple 16x16 image with a speaker icon representation
        val size = 16
        val image = java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        
        // Draw a simple speaker shape
        g.color = java.awt.Color(100, 149, 237) // Cornflower blue
        g.fillRect(2, 5, 4, 6) // Speaker body
        g.fillPolygon(intArrayOf(6, 10, 10, 6), intArrayOf(5, 2, 14, 11), 4) // Speaker cone
        
        // Sound waves
        g.color = java.awt.Color(100, 149, 237, 180)
        g.drawArc(10, 4, 4, 8, -45, 90)
        
        g.dispose()
        return image
    }
    
    private fun buildTooltip(): String {
        val book = currentBook
        val chapter = currentChapter
        val state = currentState
        
        return buildString {
            append("IReader TTS")
            if (book != null) {
                append("\n${book.title}")
            }
            if (chapter != null) {
                append("\n${chapter.name}")
            }
            if (state != null) {
                append("\nProvider: ${state.ttsProvider}")
            }
        }
    }
}
