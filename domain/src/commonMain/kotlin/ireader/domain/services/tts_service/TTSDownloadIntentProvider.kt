package ireader.domain.services.tts_service

/**
 * Platform-specific provider for TTS download notification action intents.
 * On Android, this creates PendingIntents for pause/cancel actions.
 * On other platforms, this may return null or platform-specific equivalents.
 */
interface TTSDownloadIntentProvider {
    /**
     * Get the intent for toggling pause/resume
     */
    fun getPauseIntent(): Any?
    
    /**
     * Get the intent for cancelling the download
     */
    fun getCancelIntent(): Any?
}

/**
 * Factory function to create platform-specific intent provider
 */
expect fun createTTSDownloadIntentProvider(): TTSDownloadIntentProvider
