package ireader.domain.services.tts_service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Broadcast receiver for handling TTS download notification actions.
 * Handles pause/resume and cancel actions from the notification.
 */
class TTSDownloadActionReceiver : BroadcastReceiver(), KoinComponent {
    
    companion object {
        const val ACTION_PAUSE = "ireader.tts.download.PAUSE"
        const val ACTION_RESUME = "ireader.tts.download.RESUME"
        const val ACTION_CANCEL = "ireader.tts.download.CANCEL"
        const val ACTION_TOGGLE_PAUSE = "ireader.tts.download.TOGGLE_PAUSE"
    }
    
    private val downloadManager: TTSChapterDownloadManager by inject()
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PAUSE -> {
                downloadManager.pause()
            }
            ACTION_RESUME -> {
                downloadManager.resume()
            }
            ACTION_TOGGLE_PAUSE -> {
                // Toggle between pause and resume based on current state
                if (downloadManager.state.value == TTSChapterDownloadManager.DownloadState.PAUSED) {
                    downloadManager.resume()
                } else if (downloadManager.state.value == TTSChapterDownloadManager.DownloadState.DOWNLOADING) {
                    downloadManager.pause()
                }
            }
            ACTION_CANCEL -> {
                downloadManager.cancel()
            }
        }
    }
}
