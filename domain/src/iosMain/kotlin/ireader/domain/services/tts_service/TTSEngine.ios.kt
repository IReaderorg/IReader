package ireader.domain.services.tts_service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

/**
 * iOS implementation of TTS Engine Factory
 * 
 * TODO: Full implementation using AVSpeechSynthesizer
 */
actual object TTSEngineFactory {
    actual fun createNativeEngine(): TTSEngine {
        return IosTTSEngine()
    }
    
    actual fun createGradioEngine(config: GradioTTSConfig): TTSEngine? {
        // Gradio TTS can work on iOS via HTTP
        return null // TODO: Implement
    }
    
    actual fun getAvailableEngines(): List<String> {
        return listOf("iOS Native TTS")
    }
}

/**
 * Desktop TTS engines are not available on iOS
 */
actual object DesktopTTSEngines {
    actual fun createPiperEngine(): TTSEngine? = null
    actual fun createKokoroEngine(): TTSEngine? = null
    actual fun createMayaEngine(): TTSEngine? = null
}

@OptIn(ExperimentalForeignApi::class)
actual fun base64DecodeToBytes(base64: String): ByteArray {
    val nsData = NSData.create(base64Encoding = base64)
    return nsData?.let { data ->
        val length = data.length.toInt()
        if (length == 0) return@let ByteArray(0)
        
        ByteArray(length).also { bytes ->
            bytes.usePinned { pinned ->
                memcpy(pinned.addressOf(0), data.bytes, data.length)
            }
        }
    } ?: ByteArray(0)
}

/**
 * iOS TTS Engine implementation stub
 */
private class IosTTSEngine : TTSEngine {
    private var callback: TTSEngineCallback? = null
    private var speed = 1.0f
    private var pitch = 1.0f
    
    override suspend fun speak(text: String, utteranceId: String) {
        callback?.onStart(utteranceId)
        // TODO: Implement using AVSpeechSynthesizer
        callback?.onDone(utteranceId)
    }
    
    override fun stop() {
        // TODO: Implement
    }
    
    override fun pause() {
        // TODO: Implement
    }
    
    override fun resume() {
        // TODO: Implement
    }
    
    override fun setSpeed(speed: Float) {
        this.speed = speed
    }
    
    override fun setPitch(pitch: Float) {
        this.pitch = pitch
    }
    
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
    
    override fun isReady(): Boolean = true
    
    override fun cleanup() {
        callback = null
    }
    
    override fun getEngineName(): String = "iOS Native TTS"
}
