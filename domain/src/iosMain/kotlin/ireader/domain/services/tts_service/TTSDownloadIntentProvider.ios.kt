package ireader.domain.services.tts_service

/**
 * iOS implementation of TTSDownloadIntentProvider.
 * iOS notifications can have actions but they work differently.
 * For now, returns null for intents.
 */
class IosTTSDownloadIntentProvider : TTSDownloadIntentProvider {
    
    override fun getPauseIntent(): Any? = null
    
    override fun getCancelIntent(): Any? = null
}

actual fun createTTSDownloadIntentProvider(): TTSDownloadIntentProvider {
    return IosTTSDownloadIntentProvider()
}
