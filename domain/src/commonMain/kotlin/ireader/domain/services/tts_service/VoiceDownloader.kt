package ireader.domain.services.tts_service

import ireader.domain.models.tts.VoiceModel
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for downloading voice models with progress tracking
 * Requirements: 4.2, 4.3, 4.4
 */
class VoiceDownloader(
    private val downloadDirectory: File
) {
    
    init {
        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdirs()
        }
    }
    
    /**
     * Download a voice model with progress tracking
     * @param voice Voice model to download
     * @param onProgress Progress callback (0.0 to 1.0)
     * @return Result containing the downloaded model file
     */
    suspend fun downloadVoice(
        voice: VoiceModel,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            // Create voice directory
            val voiceDir = File(downloadDirectory, voice.id)
            if (!voiceDir.exists()) {
                voiceDir.mkdirs()
            }
            
            // Download model file
            val modelFile = File(voiceDir, "${voice.id}.onnx")
            downloadFile(voice.downloadUrl, modelFile) { progress ->
                onProgress(progress * 0.8f) // Model is 80% of total
            }
            
            // Download config file
            val configFile = File(voiceDir, "${voice.id}.onnx.json")
            downloadFile(voice.configUrl, configFile) { progress ->
                onProgress(0.8f + progress * 0.2f) // Config is 20% of total
            }
            
            // Verify checksum if provided
            if (voice.checksum != "sha256:placeholder") {
                val isValid = verifyChecksum(modelFile, voice.checksum)
                if (!isValid) {
                    // Cleanup on failure
                    modelFile.delete()
                    configFile.delete()
                    voiceDir.delete()
                    return@withContext Result.failure(
                        Exception("Checksum verification failed for ${voice.id}")
                    )
                }
            }
            
            onProgress(1.0f)
            Result.success(modelFile)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download a file with progress tracking
     */
    private suspend fun downloadFile(
        urlString: String,
        destination: File,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.connect()
            
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw Exception("HTTP error: ${connection.responseCode}")
            }
            
            val fileLength = connection.contentLength
            val input: InputStream = connection.inputStream
            val output = destination.outputStream()
            
            try {
                val buffer = ByteArray(8192)
                var total: Long = 0
                var count: Int
                
                while (input.read(buffer).also { count = it } != -1) {
                    total += count
                    output.write(buffer, 0, count)
                    
                    if (fileLength > 0) {
                        onProgress(total.toFloat() / fileLength)
                    }
                }
                
                output.flush()
            } finally {
                output.close()
                input.close()
            }
            
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * Verify file checksum
     */
    private fun verifyChecksum(file: File, expectedChecksum: String): Boolean {
        val actualChecksum = calculateSHA256(file)
        val expected = expectedChecksum.removePrefix("sha256:")
        return actualChecksum.equals(expected, ignoreCase = true)
    }
    
    /**
     * Calculate SHA-256 checksum of a file
     */
    private fun calculateSHA256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Resume a partial download
     */
    suspend fun resumeDownload(
        voice: VoiceModel,
        partialFile: File,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val url = URL(voice.downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            try {
                val existingSize = if (partialFile.exists()) partialFile.length() else 0
                
                connection.requestMethod = "GET"
                connection.setRequestProperty("Range", "bytes=$existingSize-")
                connection.connectTimeout = 30000
                connection.readTimeout = 30000
                connection.connect()
                
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_PARTIAL && 
                    responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("HTTP error: $responseCode")
                }
                
                val fileLength = connection.contentLength + existingSize
                val input: InputStream = connection.inputStream
                val output = java.io.FileOutputStream(partialFile, true) // Append mode
                
                try {
                    val buffer = ByteArray(8192)
                    var total: Long = existingSize
                    var count: Int
                    
                    while (input.read(buffer).also { count = it } != -1) {
                        total += count
                        output.write(buffer, 0, count)
                        
                        if (fileLength > 0) {
                            onProgress(total.toFloat() / fileLength)
                        }
                    }
                    
                    output.flush()
                } finally {
                    output.close()
                    input.close()
                }
                
                Result.success(partialFile)
                
            } finally {
                connection.disconnect()
            }
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cancel an ongoing download
     */
    fun cancelDownload(voiceId: String) {
        // Implementation would track active downloads and cancel them
        // For now, this is a placeholder
    }
}
