package ireader.domain.services.tts

import android.content.Context
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.core.log.Log
import ireader.domain.catalogs.PiperVoiceCatalog
import ireader.domain.models.tts.AudioData
import ireader.domain.models.tts.VoiceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Piper TTS - Free, high-quality neural text-to-speech
 * 
 * Note: This is a placeholder implementation.
 * Piper JNI library uses protected constructors and methods that require
 * a Java wrapper class to access. Integration is pending.
 * 
 * For now, users should use the native Android TTS which is already
 * available in the reader.
 */
class PiperTTSService(
    private val context: Context,
    private val httpClient: HttpClient
) : AITTSService {
    
    private val voicesDir = File(context.filesDir, "piper_voices")
    
    init {
        voicesDir.mkdirs()
        Log.info { "Piper TTS service created (integration pending)" }
    }
    
    override suspend fun getAvailableVoices(): Result<List<VoiceModel>> = withContext(Dispatchers.IO) {
        runCatching {
            // Use the shared Piper voice catalog (same as desktop)
            PiperVoiceCatalog.getAllVoices()
        }
    }
    
    override suspend fun synthesize(
        text: String,
        voiceId: String,
        speed: Float,
        pitch: Float
    ): Result<AudioData> = withContext(Dispatchers.IO) {
        Result.failure(
            Exception(
                "Piper TTS not yet available. " +
                "The library requires a Java wrapper to access protected APIs. " +
                "Please use native Android TTS for now."
            )
        )
    }
    
    override suspend fun isAvailable(): Boolean {
        return false
    }
    
    override fun getProviderName(): String = "Piper TTS (In Development)"
    
    /**
     * Download a voice model with progress tracking
     */
    suspend fun downloadVoice(
        voiceModel: VoiceModel,
        onProgress: (Int) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val modelFile = File(voicesDir, "${voiceModel.id}.onnx")
            val configFile = File(voicesDir, "${voiceModel.id}.onnx.json")
            
            if (modelFile.exists() && configFile.exists()) {
                Log.info { "Voice model already downloaded: ${voiceModel.name}" }
                onProgress(100)
                return@runCatching
            }
            
            Log.info { "Downloading voice model: ${voiceModel.name}" }
            
            onProgress(0)
            downloadFile(voiceModel.downloadUrl, modelFile) { progress ->
                onProgress((progress * 0.8).toInt())
            }
            
            onProgress(80)
            downloadFile(voiceModel.configUrl, configFile) { progress ->
                onProgress(80 + (progress * 0.2).toInt())
            }
            
            onProgress(100)
            Log.info { "Successfully downloaded voice model: ${voiceModel.name}" }
        }
    }
    
    private suspend fun downloadFile(
        url: String,
        destination: File,
        onProgress: (Int) -> Unit
    ) {
        val response: HttpResponse = httpClient.get(url)
        
        if (response.status.isSuccess()) {
            val bytes = response.readBytes()
            destination.writeBytes(bytes)
            onProgress(100)
        } else {
            throw Exception("Download failed: ${response.status}")
        }
    }
    
    fun isVoiceDownloaded(voiceId: String): Boolean {
        val modelFile = File(voicesDir, "$voiceId.onnx")
        val configFile = File(voicesDir, "$voiceId.onnx.json")
        return modelFile.exists() && configFile.exists()
    }
    
    fun deleteVoice(voiceId: String): Boolean {
        val modelFile = File(voicesDir, "$voiceId.onnx")
        val configFile = File(voicesDir, "$voiceId.onnx.json")
        
        var success = true
        if (modelFile.exists()) {
            success = success && modelFile.delete()
        }
        if (configFile.exists()) {
            success = success && configFile.delete()
        }
        
        return success
    }
    
    fun getDownloadedVoices(): List<String> {
        return voicesDir.listFiles()
            ?.filter { it.extension == "onnx" }
            ?.map { it.nameWithoutExtension }
            ?: emptyList()
    }
    
    fun getDownloadedVoicesSize(): Long {
        return voicesDir.listFiles()
            ?.sumOf { it.length() }
            ?: 0L
    }
    
    fun close() {
        Log.info { "Piper TTS service closed" }
    }
}
