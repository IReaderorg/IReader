package ireader.domain.services.tts_service
import ireader.domain.utils.extensions.ioDispatcher

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import ireader.domain.models.tts.VoiceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.HashingSink
import okio.Path
import okio.blackholeSink
import okio.buffer
import okio.use

/**
 * Service for downloading voice models with progress tracking.
 * Uses Ktor client and Okio for KMP compatibility.
 * Requirements: 4.2, 4.3, 4.4
 */
class VoiceDownloader(
    private val downloadDirectory: Path,
    private val httpClient: HttpClient,
    private val fileSystem: FileSystem
) {
    
    init {
        if (!fileSystem.exists(downloadDirectory)) {
            fileSystem.createDirectories(downloadDirectory)
        }
    }
    
    /**
     * Download a voice model with progress tracking
     * @param voice Voice model to download
     * @param onProgress Progress callback (0.0 to 1.0)
     * @return Result containing the downloaded model path
     */
    suspend fun downloadVoice(
        voice: VoiceModel,
        onProgress: (Float) -> Unit
    ): Result<Path> = withContext(ioDispatcher) {
        try {
            // Create voice directory
            val voiceDir = downloadDirectory / voice.id
            if (!fileSystem.exists(voiceDir)) {
                fileSystem.createDirectories(voiceDir)
            }
            
            // Download model file
            val modelFile = voiceDir / "${voice.id}.onnx"
            downloadFile(voice.downloadUrl, modelFile) { progress ->
                onProgress(progress * 0.8f) // Model is 80% of total
            }
            
            // Download config file
            val configFile = voiceDir / "${voice.id}.onnx.json"
            downloadFile(voice.configUrl, configFile) { progress ->
                onProgress(0.8f + progress * 0.2f) // Config is 20% of total
            }
            
            // Verify checksum if provided
            if (voice.checksum != "sha256:placeholder") {
                val isValid = verifyChecksum(modelFile, voice.checksum)
                if (!isValid) {
                    // Cleanup on failure
                    fileSystem.delete(modelFile)
                    fileSystem.delete(configFile)
                    fileSystem.delete(voiceDir)
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
     * Download a file with progress tracking using Ktor
     */
    private suspend fun downloadFile(
        urlString: String,
        destination: Path,
        onProgress: (Float) -> Unit
    ) = withContext(ioDispatcher) {
        val response = httpClient.get(urlString)
        
        if (!response.status.isSuccess()) {
            throw Exception("HTTP error: ${response.status.value}")
        }
        
        val fileLength = response.contentLength() ?: -1L
        val channel = response.bodyAsChannel()
        
        fileSystem.sink(destination).buffer().use { sink ->
            val buffer = ByteArray(8192)
            var total: Long = 0
            
            while (!channel.isClosedForRead) {
                val count = channel.readAvailable(buffer)
                if (count > 0) {
                    sink.write(buffer, 0, count)
                    total += count
                    
                    if (fileLength > 0) {
                        onProgress(total.toFloat() / fileLength)
                    }
                }
            }
        }
    }
    
    /**
     * Verify file checksum using Okio HashingSink
     */
    private fun verifyChecksum(file: Path, expectedChecksum: String): Boolean {
        val actualChecksum = calculateSHA256(file)
        val expected = expectedChecksum.removePrefix("sha256:")
        return actualChecksum.equals(expected, ignoreCase = true)
    }
    
    /**
     * Calculate SHA-256 checksum of a file using Okio
     */
    private fun calculateSHA256(file: Path): String {
        val hashingSink = HashingSink.sha256(blackholeSink())
        fileSystem.source(file).buffer().use { source ->
            source.readAll(hashingSink)
        }
        return hashingSink.hash.hex()
    }
    
    /**
     * Resume a partial download using Ktor with Range header
     */
    suspend fun resumeDownload(
        voice: VoiceModel,
        partialFile: Path,
        onProgress: (Float) -> Unit
    ): Result<Path> = withContext(ioDispatcher) {
        try {
            val existingSize = if (fileSystem.exists(partialFile)) {
                fileSystem.metadata(partialFile).size ?: 0L
            } else {
                0L
            }
            
            val response = httpClient.get(voice.downloadUrl) {
                header(HttpHeaders.Range, "bytes=$existingSize-")
            }
            
            if (!response.status.isSuccess() && response.status.value != 206) {
                throw Exception("HTTP error: ${response.status.value}")
            }
            
            val contentLength = response.contentLength() ?: 0L
            val fileLength = contentLength + existingSize
            val channel = response.bodyAsChannel()
            
            // Append to existing file
            fileSystem.appendingSink(partialFile).buffer().use { sink ->
                val buffer = ByteArray(8192)
                var total: Long = existingSize
                
                while (!channel.isClosedForRead) {
                    val count = channel.readAvailable(buffer)
                    if (count > 0) {
                        sink.write(buffer, 0, count)
                        total += count
                        
                        if (fileLength > 0) {
                            onProgress(total.toFloat() / fileLength)
                        }
                    }
                }
            }
            
            Result.success(partialFile)
            
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
