package ireader.domain.services.tts_service

/**
 * Desktop TTS Engines (Android stub)
 * These engines are only available on Desktop platform
 */
actual object DesktopTTSEngines {
    actual fun createPiperEngine(): TTSEngine? = null
    actual fun createKokoroEngine(): TTSEngine? = null
    actual fun createMayaEngine(): TTSEngine? = null
}
