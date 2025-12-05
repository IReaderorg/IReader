package ireader.domain.services.downloaderService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ireader.core.log.Log
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * BroadcastReceiver to handle download notification actions (pause/resume).
 */
class DownloadActionReceiver : BroadcastReceiver(), KoinComponent {
    
    private val downloadStateHolder: DownloadStateHolder by inject()
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DownloadActions.ACTION_PAUSE -> {
                Log.info { "DownloadActionReceiver: Pause action received" }
                downloadStateHolder.setPaused(true)
            }
            DownloadActions.ACTION_RESUME -> {
                Log.info { "DownloadActionReceiver: Resume action received" }
                downloadStateHolder.setPaused(false)
            }
            DownloadActions.ACTION_CANCEL -> {
                Log.info { "DownloadActionReceiver: Cancel action received" }
                downloadStateHolder.setRunning(false)
            }
        }
    }
}
