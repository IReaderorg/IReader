package ireader.domain.services.tts_service

import ireader.core.log.Log
import ireader.core.storage.AppDir
import ireader.domain.models.tts.PiperVoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Desktop implementation of PiperVoiceDownloader.
 * Downloads Piper voice models to local storage.
 */
class DesktopPiperVoiceDownloader(
    private val appDataDir: File = AppDir
) : PiperVoiceDownloader {
    
    private val modelsDir = File(appDataDir, "piper_models").apply { mkdirs() }
    
    override suspend fun downloadVoice(voice: PiperVoice, onProgress: (Float) -> Unit) {
        withContext(Dispatchers.IO) {
            val modelDir = File(modelsDir, voice.id).apply { mkdirs() }
            val modelFile = File(modelDir, "model.onnx")
            val configFile = File(modelDir, "config.json")
            
            try {
                Log.info { "[DesktopPiperVoiceDownloader] Downloading ${voice.name} from ${voice.downloadUrl}" }
                
                // Download model file
                downloadFile(voice.downloadUrl, modelFile, voice.modelSize, onProgress)
                
                // Download config file
                downloadFile(voice.configUrl, configFile, 0) { }
                
                // Verify files exist
                require(modelFile.exists() && modelFile.length() > 0) {
                    "Model file download failed"
                }
                require(configFile.exists() && configFile.length() > 0) {
                    "Config file download failed"
                }
                
                Log.info { "[DesktopPiperVoiceDownloader] Successfully downloaded ${voice.name}" }
            } catch (e: Exception) {
                // Clean up on failure
                modelDir.deleteRecursively()
                throw e
            }
        }
    }
    
    override suspend fun deleteVoice(voiceId: String) {
        withContext(Dispatchers.IO) {
            val modelDir = File(modelsDir, voiceId)
            if (modelDir.exists()) {
                modelDir.deleteRecursively()
                Log.info { "[DesktopPiperVoiceDownloader] Deleted voice $voiceId" }
            }
        }
    }
    
    override suspend fun isVoiceDownloaded(voiceId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val paths = getVoicePaths(voiceId)
            paths != null && File(paths.modelPath).exists() && File(paths.configPath).exists()
        }
    }
    
    override fun getVoicePaths(voiceId: String): VoicePaths? {
        val modelDir = File(modelsDir, voiceId)
        val modelFile = File(modelDir, "model.onnx")
        val configFile = File(modelDir, "config.json")
        
        return if (modelFile.exists() && configFile.exists()) {
            VoicePaths(modelFile.absolutePath, configFile.absolutePath)
        } else {
            null
        }
    }
    
    private suspend fun downloadFile(
        url: String,
        destination: File,
        totalSize: Long,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection()
        connection.connect()
        
        val contentLength = if (totalSize > 0) totalSize else connection.contentLengthLong
        
        connection.getInputStream().use { input ->
            FileOutputStream(destination).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L
                
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    
                    if (contentLength > 0) {
                        onProgress(totalBytesRead.toFloat() / contentLength)
                    }
                }
            }
        }
    }
}
