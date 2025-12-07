package ireader.domain.services.tts_service.v2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ireader.core.log.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Broadcast receiver for TTS V2 notification actions.
 * 
 * This receiver handles actions from the notification buttons
 * and forwards them to the TTSController.
 */
class TTSV2ActionReceiver : BroadcastReceiver(), KoinComponent {
    
    companion object {
        private const val TAG = "TTSV2ActionReceiver"
    }
    
    private val controller: TTSController by inject()
    
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action ?: return
        
        Log.warn { "$TAG: Received action: $action" }
        
        when (action) {
            TTSV2Service.ACTION_PLAY_PAUSE -> {
                val state = controller.state.value
                if (state.isPlaying) {
                    controller.dispatch(TTSCommand.Pause)
                } else {
                    controller.dispatch(TTSCommand.Play)
                }
            }
            TTSV2Service.ACTION_STOP -> {
                controller.dispatch(TTSCommand.Stop)
                // Stop the service
                context?.stopService(Intent(context, TTSV2Service::class.java))
            }
            TTSV2Service.ACTION_NEXT -> {
                val state = controller.state.value
                if (state.chunkModeEnabled) {
                    controller.dispatch(TTSCommand.NextChunk)
                } else {
                    controller.dispatch(TTSCommand.NextParagraph)
                }
            }
            TTSV2Service.ACTION_PREVIOUS -> {
                val state = controller.state.value
                if (state.chunkModeEnabled) {
                    controller.dispatch(TTSCommand.PreviousChunk)
                } else {
                    controller.dispatch(TTSCommand.PreviousParagraph)
                }
            }
        }
    }
}
