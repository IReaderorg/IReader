package ireader.domain.services.tts_service

import platform.Foundation.NSData
import platform.Foundation.create

/**
 * iOS implementation of TTS Engine Factory
 * 
 * TODO: Implement using AVSpeechSynthesizer
 */
actual object TTSEngineFactory {
    actual fun createNativeEngine(): TTSEngine? {
        // TODO: Return AVSpeechSynthesizer-based engine
        return null
    }
    
    actual fun createAIEngine(): TTSEngine? {
        // TODO: Implement AI TTS if needed
        return null
    }
}

/**
 * Desktop TTS engines are not available on iOS
 */
actual object DesktopTTSEngines {
    actual fun createPiperEngine(): TTSEngine? = null
    actual fun createKokoroEngine(): TTSEngine? = null
}

actual fun base64DecodeToBytes(base64: String): ByteArray {
    val nsData = NSData.create(base64Encoding = base64)
    return nsData?.let { data ->
        ByteArray(data.length.toInt()).also { bytes ->
            data.getBytes(bytes.refTo(0), data.length)
        }
    } ?: ByteArray(0)
}

private fun ByteArray.refTo(index: Int): kotlinx.cinterop.CValuesRef<kotlinx.cinterop.ByteVar> {
    return this.usePinned { it.addressOf(index) }
}

private inline fun <T> ByteArray.usePinned(block: (kotlinx.cinterop.Pinned<ByteArray>) -> T): T {
    return kotlinx.cinterop.memScoped {
        val pinned = kotlinx.cinterop.pin(this@usePinned)
        try {
            block(pinned)
        } finally {
            pinned.unpin()
        }
    }
}
