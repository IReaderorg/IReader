package ireader.domain.services.tts_service

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.*
import platform.AVFAudio.*
import platform.Foundation.*
import platform.darwin.NSObject
import platform.posix.memcpy

/**
 * iOS implementation of TTS Engine Factory
 * 
 * Uses AVSpeechSynthesizer for native text-to-speech on iOS
 */
actual object TTSEngineFactory {
    actual fun createNativeEngine(): TTSEngine {
        return IosTTSEngine()
    }
    
    actual fun createGradioEngine(config: GradioTTSConfig): TTSEngine? {
        // Gradio TTS can work on iOS via HTTP - return GradioTTSEngine if needed
        return null
    }
    
    actual fun getAvailableEngines(): List<String> {
        return listOf("iOS Native TTS (AVSpeechSynthesizer)")
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
 * iOS TTS Engine implementation using AVSpeechSynthesizer
 */
@OptIn(ExperimentalForeignApi::class)
private class IosTTSEngine : TTSEngine {
    private var callback: TTSEngineCallback? = null
    private var speed = 1.0f
    private var pitch = 1.0f
    private var volume = 1.0f
    private var voiceIdentifier: String? = null
    
    private val synthesizer = AVSpeechSynthesizer()
    private var delegate: SpeechDelegate? = null
    private var currentUtteranceId: String? = null
    
    init {
        delegate = SpeechDelegate(
            onStart = { utteranceId ->
                callback?.onStart(utteranceId)
            },
            onDone = { utteranceId ->
                callback?.onDone(utteranceId)
            },
            onError = { utteranceId, error ->
                callback?.onError(utteranceId, error)
            }
        )
        synthesizer.delegate = delegate
    }
    
    override suspend fun speak(text: String, utteranceId: String) {
        currentUtteranceId = utteranceId
        delegate?.currentUtteranceId = utteranceId
        
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text).apply {
            rate = (speed * AVSpeechUtteranceDefaultSpeechRate).coerceIn(
                AVSpeechUtteranceMinimumSpeechRate.toFloat(),
                AVSpeechUtteranceMaximumSpeechRate.toFloat()
            )
            
            pitchMultiplier = pitch.coerceIn(0.5f, 2.0f)
            this.volume = this@IosTTSEngine.volume.coerceIn(0.0f, 1.0f)
            
            voiceIdentifier?.let { identifier ->
                AVSpeechSynthesisVoice.voiceWithIdentifier(identifier)?.let { voice ->
                    this.voice = voice
                }
            }
        }
        
        synthesizer.speakUtterance(utterance)
    }
    
    override fun stop() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }
    
    override fun pause() {
        synthesizer.pauseSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }
    
    override fun resume() {
        synthesizer.continueSpeaking()
    }
    
    override fun setSpeed(speed: Float) {
        this.speed = speed.coerceIn(0.1f, 4.0f)
    }
    
    override fun setPitch(pitch: Float) {
        this.pitch = pitch.coerceIn(0.5f, 2.0f)
    }
    
    override fun setCallback(callback: TTSEngineCallback) {
        this.callback = callback
    }
    
    override fun isReady(): Boolean = true
    
    override fun cleanup() {
        stop()
        callback = null
        delegate = null
    }
    
    override fun getEngineName(): String = "iOS Native TTS"
}

/**
 * AVSpeechSynthesizerDelegate implementation
 */
@OptIn(ExperimentalForeignApi::class)
private class SpeechDelegate(
    private val onStart: (String) -> Unit,
    private val onDone: (String) -> Unit,
    private val onError: (String, String) -> Unit
) : NSObject(), AVSpeechSynthesizerDelegateProtocol {
    
    var currentUtteranceId: String = ""
    
    @ObjCSignatureOverride
    override fun speechSynthesizer(
        synthesizer: AVSpeechSynthesizer,
        didStartSpeechUtterance: AVSpeechUtterance
    ) {
        onStart(currentUtteranceId)
    }
    
    @ObjCSignatureOverride
    override fun speechSynthesizer(
        synthesizer: AVSpeechSynthesizer,
        didFinishSpeechUtterance: AVSpeechUtterance
    ) {
        onDone(currentUtteranceId)
    }
}
