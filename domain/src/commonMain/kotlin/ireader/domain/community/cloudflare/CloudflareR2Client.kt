package ireader.domain.community.cloudflare

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import ireader.core.util.randomUUID

/**
 * Client for Cloudflare R2 object storage operations.
 * Handles compressed translation content storage.
 */
class CloudflareR2Client(
    private val httpClient: HttpClient,
    private val config: CloudflareConfig
) {
    
    /**
     * Upload compressed translation content to R2.
     * Returns the object key for retrieval.
     */
    suspend fun uploadTranslation(
        compressedContent: ByteArray,
        bookHash: String,
        chapterNumber: Float,
        targetLanguage: String,
        engineId: String
    ): Result<String> {
        // Generate unique object key with organized path
        val objectKey = generateObjectKey(bookHash, chapterNumber, targetLanguage, engineId)
        
        return try {
            val response = httpClient.put("${config.r2ApiUrl}/objects/$objectKey") {
                header("Authorization", "Bearer ${config.apiToken}")
                contentType(ContentType.Application.OctetStream)
                header("Content-Length", compressedContent.size.toString())
                // Add metadata headers
                header("x-amz-meta-book-hash", bookHash)
                header("x-amz-meta-chapter", chapterNumber.toString())
                header("x-amz-meta-language", targetLanguage)
                header("x-amz-meta-engine", engineId)
                setBody(compressedContent)
            }
            
            if (response.status.isSuccess()) {
                Result.success(objectKey)
            } else {
                Result.failure(Exception("R2 upload failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Download compressed translation content from R2.
     */
    suspend fun downloadTranslation(objectKey: String): Result<ByteArray> {
        return try {
            // Try public URL first if available (faster, no auth needed)
            val url = if (config.r2PublicUrl.isNotBlank()) {
                "${config.r2PublicUrl.trimEnd('/')}/$objectKey"
            } else {
                "${config.r2ApiUrl}/objects/$objectKey"
            }
            
            val response = if (config.r2PublicUrl.isNotBlank()) {
                httpClient.get(url)
            } else {
                httpClient.get(url) {
                    header("Authorization", "Bearer ${config.apiToken}")
                }
            }
            
            if (response.status.isSuccess()) {
                Result.success(response.bodyAsBytes())
            } else {
                Result.failure(Exception("R2 download failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete translation content from R2.
     */
    suspend fun deleteTranslation(objectKey: String): Result<Unit> {
        return try {
            val response = httpClient.delete("${config.r2ApiUrl}/objects/$objectKey") {
                header("Authorization", "Bearer ${config.apiToken}")
            }
            
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("R2 delete failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if an object exists in R2.
     */
    suspend fun exists(objectKey: String): Boolean {
        return try {
            val url = if (config.r2PublicUrl.isNotBlank()) {
                "${config.r2PublicUrl.trimEnd('/')}/$objectKey"
            } else {
                "${config.r2ApiUrl}/objects/$objectKey"
            }
            
            val response = if (config.r2PublicUrl.isNotBlank()) {
                httpClient.get(url)
            } else {
                httpClient.get(url) {
                    header("Authorization", "Bearer ${config.apiToken}")
                }
            }
            
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get public URL for a translation (if public URL is configured).
     */
    fun getPublicUrl(objectKey: String): String? {
        return if (config.r2PublicUrl.isNotBlank()) {
            "${config.r2PublicUrl.trimEnd('/')}/$objectKey"
        } else {
            null
        }
    }
    
    /**
     * Generate organized object key for translation content.
     * Format: translations/{book_hash}/{language}/{chapter}_{engine}_{uuid}.bin
     */
    private fun generateObjectKey(
        bookHash: String,
        chapterNumber: Float,
        targetLanguage: String,
        engineId: String
    ): String {
        val uuid = randomUUID().take(8)
        val chapterStr = if (chapterNumber < 0) "unknown" else chapterNumber.toString().replace(".", "_")
        return "translations/$bookHash/$targetLanguage/${chapterStr}_${engineId}_$uuid.bin"
    }
}
