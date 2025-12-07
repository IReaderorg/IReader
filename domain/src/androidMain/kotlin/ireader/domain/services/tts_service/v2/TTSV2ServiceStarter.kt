package ireader.domain.services.tts_service.v2

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import ireader.core.log.Log

/**
 * Android implementation of TTSV2ServiceStarter
 * 
 * Starts TTSV2Service as a foreground service for background playback
 * with media notification controls.
 */
actual class TTSV2ServiceStarter(
    private val context: Context
) {
    companion object {
        private const val TAG = "TTSV2ServiceStarter"
    }
    
    actual fun startService(bookId: Long, chapterId: Long, startParagraph: Int) {
        Log.warn { "$TAG: startService(bookId=$bookId, chapterId=$chapterId, startParagraph=$startParagraph)" }
        
        val intent = TTSV2Service.createIntent(context, bookId, chapterId, startParagraph)
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.warn { "$TAG: Service started successfully" }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to start service: ${e.message}" }
        }
    }
    
    actual fun stopService() {
        Log.warn { "$TAG: stopService()" }
        
        try {
            val intent = Intent(context, TTSV2Service::class.java)
            context.stopService(intent)
            Log.warn { "$TAG: Service stopped" }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to stop service: ${e.message}" }
        }
    }
    
    @Suppress("DEPRECATION")
    actual fun isServiceRunning(): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.getRunningServices(Int.MAX_VALUE)
        
        for (service in services) {
            if (TTSV2Service::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
