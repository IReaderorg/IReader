package ireader.domain.usecases.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import ireader.core.log.Log
import ireader.domain.preferences.models.prefs.IReaderVoice
import ireader.domain.services.tts_service.toIReaderVoice
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Use case to fetch available voices and languages from Android's native TTS engine
 * 
 * This allows users to see and select from all installed TTS voices on their device.
 */
class GetNativeTTSVoicesUseCase(
    private val context: Context
) {
    
    /**
     * Get all available voices from the native TTS engine
     * Returns a list of IReaderVoice objects
     */
    suspend fun getAvailableVoices(): Result<List<IReaderVoice>> = suspendCancellableCoroutine { continuation ->
        var ttsInstance: TextToSpeech? = null
        try {
            ttsInstance = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        val voices = ttsInstance?.voices?.mapNotNull { voice: Voice ->
                            try {
                                voice.toIReaderVoice()
                            } catch (e: Exception) {
                                Log.warn { "Failed to convert voice: ${e.message}" }
                                null
                            }
                        } ?: emptyList<IReaderVoice>()
                        
                        Log.info { "Found ${voices.size} native TTS voices" }
                        ttsInstance?.shutdown()
                        continuation.resume(Result.success(voices))
                    } catch (e: Exception) {
                        Log.error { "Error getting voices: ${e.message}" }
                        ttsInstance?.shutdown()
                        continuation.resume(Result.failure(e))
                    }
                } else {
                    Log.error { "TTS initialization failed with status: $status" }
                    continuation.resume(Result.failure(Exception("TTS initialization failed")))
                }
            }
            
            continuation.invokeOnCancellation {
                ttsInstance?.shutdown()
            }
        } catch (e: Exception) {
            Log.error { "Failed to create TTS: ${e.message}" }
            continuation.resume(Result.failure(e))
        }
    }
    
    /**
     * Get all available languages from the native TTS engine
     * Returns a list of Locale objects
     */
    suspend fun getAvailableLanguages(): Result<List<Locale>> = suspendCancellableCoroutine { continuation ->
        var ttsInstance: TextToSpeech? = null
        try {
            ttsInstance = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    try {
                        val languages = ttsInstance?.availableLanguages?.toList() ?: emptyList<Locale>()
                        Log.info { "Found ${languages.size} native TTS languages" }
                        ttsInstance?.shutdown()
                        continuation.resume(Result.success(languages))
                    } catch (e: Exception) {
                        Log.error { "Error getting languages: ${e.message}" }
                        ttsInstance?.shutdown()
                        continuation.resume(Result.failure(e))
                    }
                } else {
                    Log.error { "TTS initialization failed with status: $status" }
                    continuation.resume(Result.failure(Exception("TTS initialization failed")))
                }
            }
            
            continuation.invokeOnCancellation {
                ttsInstance?.shutdown()
            }
        } catch (e: Exception) {
            Log.error { "Failed to create TTS: ${e.message}" }
            continuation.resume(Result.failure(e))
        }
    }
    
    /**
     * Get voices filtered by language
     */
    suspend fun getVoicesForLanguage(locale: Locale): Result<List<IReaderVoice>> {
        return getAvailableVoices().map { voices ->
            voices.filter { voice ->
                voice.language == locale.language && 
                (voice.country.isEmpty() || voice.country == locale.country)
            }
        }
    }
}
