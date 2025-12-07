package ireader.domain.services.tts_service.v2

import ireader.core.log.Log

/**
 * Desktop implementation of TTSV2ServiceStarter
 * 
 * Desktop doesn't need a background service - TTS runs in the main process.
 * This is a no-op implementation.
 */
actual class TTSV2ServiceStarter {
    companion object {
        private const val TAG = "TTSV2ServiceStarter"
    }
    
    actual fun startService(bookId: Long, chapterId: Long, startParagraph: Int) {
        Log.warn { "$TAG: startService() - No-op on desktop" }
        // Desktop doesn't need a background service
    }
    
    actual fun stopService() {
        Log.warn { "$TAG: stopService() - No-op on desktop" }
        // Desktop doesn't need a background service
    }
    
    actual fun isServiceRunning(): Boolean {
        // Desktop always returns false - no background service
        return false
    }
}
