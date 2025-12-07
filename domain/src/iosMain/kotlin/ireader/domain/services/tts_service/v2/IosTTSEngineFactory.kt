package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import platform.AVFAudio.*
import platform.darwin.NSObject

/**
 * iOS implementation of TTS Engine Factory for v2 architecture
 */
actual object TTSEngineFactory {
    actual fun createNativeEngine(): TTSEngine {
        return IosTTSEngineV2()
    }
    
    actual fun createGradioEngine(config: GradioConfig): TTSEngine? {
        // Gradio TTS can work on iOS via HTTP - not implemented yet
        return null
    }
}

/**
 * iOS TTS Engine implementation using AVSpeechSynthesizer for v2 architecture
 */
@OptIn(ExperimentalForeignApi::class)
class IosTTSEngineV2 : TTSEngine {
    companion object {
        private const val TAG = "IosTTSEngineV2"
    }
    
    private val _events = MutableSharedFlow<EngineEvent>(extraBufferCapacity = 10)
    private var speed = 1.0f
    private var pitch = 1.0f
    private var volume = 1.0f
    
    private val synthesizer = AVSpeechSynthesizer()
    private var delegate: SpeechDelegateV2? = null
    private var currentUtteranceId: String = ""
    
    override val events: Flow<EngineEvent> = _events
    override val name: String = "iOS Native TTS"
    
    init {
        delegate = SpeechDelegateV2(
            onStart = { utteranceId ->
                Log.warn { "$TAG: onStart($utteranceId)" }
                _events.tryEmit(EngineEvent.Started(utteranceId))
            },
            onDone = { utteranceId ->
                Log.warn { "$TAG: onDone($utteranceId)" }
                _events.tryEmit(EngineEvent.Completed(utteranceId))
            },
            onError = { utteranceId, error ->
                Log.warn { "$TAG: onError($utteranceId, $error)" }
                _events.tryEmit(EngineEvent.Error(utteranceId, error))
            }
        )
        synthesizer.delegate = delegate
        _events.tryEmit(EngineEvent.Ready)
    }
    
    override suspend fun speak(text: String, utteranceId: String) {
        Log.warn { "$TAG: speak($utteranceId)" }
        currentUtteranceId = utteranceId
        delegate?.currentUtteranceId = utteranceId
        
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text).apply {
            rate = (speed * AVSpeechUtteranceDefaultSpeechRate).coerceIn(
                AVSpeechUtteranceMinimumSpeechRate.toFloat(),
                AVSpeechUtteranceMaximumSpeechRate.toFloat()
            )
            pitchMultiplier = pitch.coerceIn(0.5f, 2.0f)
            this.volume = this@IosTTSEngineV2.volume.coerceIn(0.0f, 1.0f)
        }
        
        synthesizer.speakUtterance(utterance)
    }
    
    override fun stop() {
        Log.warn { "$TAG: stop()" }
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }
    
    override fun pause() {
        Log.warn { "$TAG: pause()" }
        synthesizer.pauseSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }
    
    override fun resume() {
        Log.warn { "$TAG: resume()" }
        synthesizer.continueSpeaking()
    }
    
    override fun setSpeed(speed: Float) {
        this.speed = speed.coerceIn(0.1f, 4.0f)
    }
    
    override fun setPitch(pitch: Float) {
        this.pitch = pitch.coerceIn(0.5f, 2.0f)
    }
    
    override fun isReady(): Boolean = true
    
    override fun release() {
        Log.warn { "$TAG: release()" }
        stop()
        delegate = null
    }
}

/**
 * AVSpeechSynthesizerDelegate implementation for v2
 */
@OptIn(ExperimentalForeignApi::class)
private class SpeechDelegateV2(
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
