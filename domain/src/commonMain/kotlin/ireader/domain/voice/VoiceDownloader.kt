package ireader.domain.voice

import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import ireader.core.log.Log
import ireader.domain.models.tts.VoiceModel
import ireader.domain.storage.VoiceStorage

/**
 * Service for downloading voice models from remote servers
 * Requirements: 3.1, 3.2, 3.3
 */
class VoiceDownloader(
    private val httpClient: HttpClient,
    private val storage: VoiceStorage
) {
    /**
     * Download a voice model and save it to storage
     * @param voice Voice model to download
     * @param onProgress Callback for download progress (0.0 to 1.0)
     * @return Result indicating success or failure
     */
    suspend fun downloadVoice(
        voice: VoiceModel,
        onProgress: (Float) -> Unit
    ): Result<Unit> {
        return try {
            Log.info { "Starting download for voice: ${voice.name} (${voice.id})" }
            
            // Download the voice model file
            val response = httpClient.get(voice.downloadUrl) {
                onDownload { bytesSentTotal, contentLength ->
                    if (contentLength != null) {
                        if (contentLength > 0) {
                            val progress = bytesSentTotal.toFloat() / (contentLength?.toFloat() ?: 0F)
                            onProgress(progress)
                        }
                    }
                }
            }
            
            val data = response.readBytes()
            Log.info { "Downloaded ${data.size} bytes for voice: ${voice.id}" }
            
            // Verify checksum if provided
            if (voice.checksum.isNotEmpty()) {
                val actualChecksum = calculateChecksum(data)
                if (actualChecksum != voice.checksum) {
                    Log.error("Checksum mismatch for voice: ${voice.id}")
                    return Result.failure(
                        Exception("Checksum verification failed for voice: ${voice.name}")
                    )
                }
            }
            
            // Save to storage
            storage.saveVoiceModel(voice.id, data)
            
            Log.info { "Successfully downloaded and saved voice: ${voice.id}" }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.error("Voice download failed for ${voice.id}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Cancel an ongoing download
     * Note: This is a placeholder for future implementation
     * @param voiceId Voice ID to cancel download for
     */
    suspend fun cancelDownload(voiceId: String) {
        // TODO: Implement download cancellation with job tracking
        Log.info { "Download cancellation requested for: $voiceId" }
    }
    
    /**
     * Calculate SHA-256 checksum of data
     * @param data Byte array to calculate checksum for
     * @return Hex string representation of checksum
     */
    private fun calculateChecksum(data: ByteArray): String {
        return try {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(data)
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            Log.warn { "Failed to calculate checksum: ${e.message}" }
            ""
        }
    }
}
