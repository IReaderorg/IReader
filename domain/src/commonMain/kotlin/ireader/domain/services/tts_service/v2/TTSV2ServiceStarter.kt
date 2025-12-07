package ireader.domain.services.tts_service.v2

/**
 * Platform-specific service starter for TTS v2
 * 
 * On Android: Starts TTSV2Service as a foreground service
 * On Desktop/iOS: No-op (notifications handled differently)
 */
expect class TTSV2ServiceStarter {
    /**
     * Start the TTS v2 background service
     * 
     * @param bookId The book ID to load
     * @param chapterId The chapter ID to load
     * @param startParagraph The paragraph to start from
     */
    fun startService(bookId: Long, chapterId: Long, startParagraph: Int = 0)
    
    /**
     * Stop the TTS v2 background service
     */
    fun stopService()
    
    /**
     * Check if the service is running
     */
    fun isServiceRunning(): Boolean
}
