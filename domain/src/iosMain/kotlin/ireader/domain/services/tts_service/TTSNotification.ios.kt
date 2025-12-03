package ireader.domain.services.tts_service

import platform.UserNotifications.*
import platform.MediaPlayer.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of TTS Notification Factory
 * 
 * Uses:
 * - UserNotifications framework for local notifications
 * - MPNowPlayingInfoCenter for lock screen/control center media controls
 */
actual object TTSNotificationFactory {
    actual fun create(callback: TTSNotificationCallback): TTSNotification {
        return IosTTSNotification(callback)
    }
}

/**
 * iOS TTS Notification implementation
 * 
 * Provides media playback controls via MPNowPlayingInfoCenter
 * and optional local notifications for TTS status
 */
@OptIn(ExperimentalForeignApi::class)
private class IosTTSNotification(
    private val callback: TTSNotificationCallback
) : TTSNotification {
    
    private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()
    private val nowPlayingInfoCenter = MPNowPlayingInfoCenter.defaultCenter()
    private var currentData: TTSNotificationData? = null
    private var isPlaying = false
    
    init {
        // Set up remote command center for media controls
        setupRemoteCommandCenter()
    }
    
    override fun show(data: TTSNotificationData) {
        currentData = data
        updateNowPlayingInfo(data)
        
        // Optionally show a local notification
        showLocalNotification(data)
    }
    
    override fun hide() {
        currentData = null
        
        // Clear now playing info
        nowPlayingInfoCenter.nowPlayingInfo = null
        
        // Remove any pending notifications
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(listOf("tts_notification"))
        notificationCenter.removeDeliveredNotificationsWithIdentifiers(listOf("tts_notification"))
    }
    
    override fun updatePlaybackState(isPlaying: Boolean) {
        this.isPlaying = isPlaying
        
        currentData?.let { data ->
            updateNowPlayingInfo(data)
        }
    }
    
    override fun updateProgress(current: Int, total: Int) {
        currentData?.let { data ->
            val updatedData = data.copy(
                currentParagraph = current,
                totalParagraphs = total
            )
            currentData = updatedData
            updateNowPlayingInfo(updatedData)
        }
    }
    
    /**
     * Update the Now Playing info center for lock screen/control center display
     */
    private fun updateNowPlayingInfo(data: TTSNotificationData) {
        val info = mutableMapOf<Any?, Any?>()
        
        // Title (book name)
        info[MPMediaItemPropertyTitle] = data.title
        
        // Artist (chapter name)
        info[MPMediaItemPropertyArtist] = data.subtitle
        
        // Album (app name)
        info[MPMediaItemPropertyAlbumTitle] = "IReader TTS"
        
        // Playback state
        info[MPNowPlayingInfoPropertyPlaybackRate] = if (isPlaying) 1.0 else 0.0
        
        // Progress
        if (data.totalParagraphs > 0) {
            info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = data.currentParagraph.toDouble()
            info[MPMediaItemPropertyPlaybackDuration] = data.totalParagraphs.toDouble()
        }
        
        nowPlayingInfoCenter.nowPlayingInfo = info
    }
    
    /**
     * Set up remote command center for media control buttons
     */
    private fun setupRemoteCommandCenter() {
        val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
        
        // Play command
        commandCenter.playCommand.enabled = true
        commandCenter.playCommand.addTargetWithHandler { _ ->
            callback.onPlay()
            MPRemoteCommandHandlerStatusSuccess
        }
        
        // Pause command
        commandCenter.pauseCommand.enabled = true
        commandCenter.pauseCommand.addTargetWithHandler { _ ->
            callback.onPause()
            MPRemoteCommandHandlerStatusSuccess
        }
        
        // Toggle play/pause
        commandCenter.togglePlayPauseCommand.enabled = true
        commandCenter.togglePlayPauseCommand.addTargetWithHandler { _ ->
            if (isPlaying) {
                callback.onPause()
            } else {
                callback.onPlay()
            }
            MPRemoteCommandHandlerStatusSuccess
        }
        
        // Next track (next chapter)
        commandCenter.nextTrackCommand.enabled = true
        commandCenter.nextTrackCommand.addTargetWithHandler { _ ->
            callback.onNext()
            MPRemoteCommandHandlerStatusSuccess
        }
        
        // Previous track (previous chapter)
        commandCenter.previousTrackCommand.enabled = true
        commandCenter.previousTrackCommand.addTargetWithHandler { _ ->
            callback.onPrevious()
            MPRemoteCommandHandlerStatusSuccess
        }
        
        // Stop command
        commandCenter.stopCommand.enabled = true
        commandCenter.stopCommand.addTargetWithHandler { _ ->
            callback.onClose()
            MPRemoteCommandHandlerStatusSuccess
        }
    }
    
    /**
     * Show a local notification for TTS status
     */
    private fun showLocalNotification(data: TTSNotificationData) {
        val content = UNMutableNotificationContent().apply {
            setTitle(data.title)
            setBody(data.subtitle)
            setSound(null) // Silent notification
            
            // Set category for actions
            setCategoryIdentifier("TTS_PLAYBACK")
        }
        
        // Create request (no trigger = immediate)
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "tts_notification",
            content = content,
            trigger = null
        )
        
        notificationCenter.addNotificationRequest(request) { error ->
            if (error != null) {
                println("[TTSNotification] Error showing notification: ${error.localizedDescription}")
            }
        }
    }
}
