package ireader.domain.services.tts_service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of TTSDownloadIntentProvider.
 * Creates PendingIntents for notification action buttons.
 */
class AndroidTTSDownloadIntentProvider : TTSDownloadIntentProvider, KoinComponent {
    
    private val context: Context by inject()
    
    private val pendingIntentFlags: Int
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    
    override fun getPauseIntent(): Any {
        val intent = Intent(context, TTSDownloadActionReceiver::class.java).apply {
            action = TTSDownloadActionReceiver.ACTION_TOGGLE_PAUSE
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_PAUSE,
            intent,
            pendingIntentFlags
        )
    }
    
    override fun getCancelIntent(): Any {
        val intent = Intent(context, TTSDownloadActionReceiver::class.java).apply {
            action = TTSDownloadActionReceiver.ACTION_CANCEL
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_CANCEL,
            intent,
            pendingIntentFlags
        )
    }
    
    companion object {
        private const val REQUEST_CODE_PAUSE = 9101
        private const val REQUEST_CODE_CANCEL = 9102
    }
}

actual fun createTTSDownloadIntentProvider(): TTSDownloadIntentProvider {
    return AndroidTTSDownloadIntentProvider()
}
