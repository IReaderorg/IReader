package ireader.domain.services.tts_service

/**
 * Desktop implementation of TTSDownloadIntentProvider.
 * Desktop notifications don't support action buttons in the same way,
 * so this returns null for intents.
 */
class DesktopTTSDownloadIntentProvider : TTSDownloadIntentProvider {
    
    override fun getPauseIntent(): Any? = null
    
    override fun getCancelIntent(): Any? = null
}

actual fun createTTSDownloadIntentProvider(): TTSDownloadIntentProvider {
    return DesktopTTSDownloadIntentProvider()
}
