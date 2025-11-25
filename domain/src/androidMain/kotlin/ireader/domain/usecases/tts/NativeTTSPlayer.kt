package ireader.domain.usecases.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import ireader.core.log.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import kotlin.coroutines.resume

/**
 * Native Android TTS Player implementation
 * 
 * Wraps Android's TextToSpeech API with the unified TTSPlayer interface.
 */
class NativeTTSPlayer(
    private val context: Context
) : TTSPlayer {
    
    private var tts: TextToSpeech? = null
    private var callback: TTSPlayerCallback? = null
    private var isInitialized = false
    private var currentSpeechRate = 1.0f
    private var currentPitch = 1.0f
    
    override suspend fun initialize(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    
                    // Set default language
                    tts?.language = Locale.getDefault()
                    
                    // Set up utterance listener
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            utteranceId?.let { callback?.onStart(it) }
                        }
                        
                        override fun onDone(utteranceId: String?) {
                            utteranceId?.let { callback?.onDone(it) }
                        }
                        
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            utteranceId?.let { callback?.onError(it, "TTS Error") }
                        }
                        
                        override fun onError(utteranceId: String?, errorCode: Int) {
                            utteranceId?.let { callback?.onError(it, "TTS Error: $errorCode") }
                        }
                        
                        override fun onStop(utteranceId: String?, interrupted: Boolean) {
                            callback?.onStopped()
                        }
                    })
                    
                    Log.info { "Native TTS initialized successfully" }
                    continuation.resume(Result.success(Unit))
                } else {
                    Log.error { "Native TTS initialization failed: $status" }
                    continuation.resume(Result.failure(Exception("TTS initialization failed")))
                }
            }
        } catch (e: Exception) {
            Log.error { "Failed to create TTS: ${e.message}" }
            continuation.resume(Result.failure(e))
        }
    }
    
    override fun isReady(): Boolean = isInitialized && tts != null
    
    override suspend fun speak(text: String, utteranceId: String): Result<Unit> {
        return try {
            if (!isReady()) {
                return Result.failure(Exception("TTS not initialized"))
            }
            
            val result = tts?.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
            
            if (result == TextToSpeech.SUCCESS) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("TTS speak failed: $result"))
            }
        } catch (e: Exception) {
            Log.error { "Failed to speak: ${e.message}" }
            Result.failure(e)
        }
    }
    
    override fun stop() {
        tts?.stop()
        callback?.onStopped()
    }
    
    override fun pause() {
        // Native TTS doesn't support pause, so we stop
        stop()
    }
    
    override fun resume() {
        // Native TTS doesn't support resume
        // Caller needs to re-speak the text
    }
    
    override fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }
    
    override fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(currentSpeechRate)
    }
    
    override fun setPitch(pitch: Float) {
        currentPitch = pitch.coerceIn(0.5f, 2.0f)
        tts?.setPitch(currentPitch)
    }
    
    override fun getSpeechRate(): Float = currentSpeechRate
    
    override fun getPitch(): Float = currentPitch
    
    override fun setCallback(callback: TTSPlayerCallback) {
        this.callback = callback
    }
    
    override fun getProviderName(): String = "Native TTS"
    
    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        Log.info { "Native TTS shut down" }
    }
    
    /**
     * Set language
     */
    fun setLanguage(locale: Locale): Int {
        return tts?.setLanguage(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
    }
    
    /**
     * Get available languages
     */
    fun getAvailableLanguages(): Set<Locale>? {
        return tts?.availableLanguages
    }
}
