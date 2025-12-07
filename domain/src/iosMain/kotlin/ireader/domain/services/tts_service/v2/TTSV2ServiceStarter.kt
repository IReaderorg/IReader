package ireader.domain.services.tts_service.v2

import ireader.core.log.Log

/**
 * iOS implementation of TTSV2ServiceStarter
 * 
 * iOS doesn't need a background service in the same way as Android.
 * Background audio is handled through AVAudioSession.
 * This is a no-op implementation.
 */
actual class TTSV2ServiceStarter {
    companion object {
        private const val TAG = "TTSV2ServiceStarter"
    }
    
    actual fun startService(bookId: Long, chapterId: Long, startParagraph: Int) {
        Log.warn { "$TAG: startService() - No-op on iOS" }
        // iOS handles background audio differently
    }
    
    actual fun stopService() {
        Log.warn { "$TAG: stopService() - No-op on iOS" }
        // iOS handles background audio differently
    }
    
    actual fun isServiceRunning(): Boolean {
        // iOS always returns false - no background service in Android sense
        return false
    }
}
