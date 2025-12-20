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
import ireader.core.log.Log
import ireader.core.util.randomUUID

/**
 * Client for Cloudflare R2 object storage operations.
 * Uses Cloudflare API for uploads and public URL for downloads.
 * 
 * R2 requires either:
 * 1. Public bucket URL for reads (recommended for downloads)
 * 2. S3-compatible API with access keys for writes
 */
class CloudflareR2Client(
    private val httpClient: HttpClient,
    private val config: CloudflareConfig
) {
    
    /**
     * Upload compressed translation content to R2.
     * Returns the object key for retrieval.
     * 
     * Uses Cloudflare API with Bearer token authentication.
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
        
        // If R2 is not configured, we can't upload
        if (!config.isR2Configured()) {
            Log.warn { "R2 not configured - cannot upload translations" }
            return Result.failure(Exception("R2 not configured. Please set API token and bucket name."))
        }
        
        return try {
            // Use Cloudflare R2 API endpoint
            // Format: https://api.cloudflare.com/client/v4/accounts/{account_id}/r2/buckets/{bucket_name}/objects/{object_key}
            val url = "https://api.cloudflare.com/client/v4/accounts/${config.accountId}/r2/buckets/${config.r2BucketName}/objects/$objectKey"
            
            val response = httpClient.put(url) {
                contentType(ContentType.Application.OctetStream)
                header("Authorization", "Bearer ${config.apiToken}")
                header("Content-Length", compressedContent.size.toString())
                setBody(compressedContent)
            }
            
            if (response.status.isSuccess()) {
                Result.success(objectKey)
            } else {
                val body = try { response.bodyAsBytes().decodeToString() } catch (e: Exception) { "N/A" }
                Log.error { "R2 upload failed: ${response.status} - $body" }
                Result.failure(Exception("R2 upload failed: ${response.status} - $body"))
            }
        } catch (e: Exception) {
            Log.error("R2 upload error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Download compressed translation content from R2.
     * Uses S3-compatible endpoint for downloads since Cloudflare REST API doesn't support object downloads.
     */
    suspend fun downloadTranslation(objectKey: String): Result<ByteArray> {
        return try {
            // Try public URL first if available (fastest, no auth needed)
            if (config.r2PublicUrl.isNotBlank()) {
                val publicUrl = "${config.r2PublicUrl.trimEnd('/')}/$objectKey"
                val response = httpClient.get(publicUrl)
                if (response.status.isSuccess()) {
                    return Result.success(response.bodyAsBytes())
                }
            }
            
            // Use S3-compatible endpoint for download
            val s3Url = "https://${config.accountId}.r2.cloudflarestorage.com/${config.r2BucketName}/$objectKey"
            
            // Try with Bearer token first
            val response = httpClient.get(s3Url) {
                header("Authorization", "Bearer ${config.apiToken}")
            }
            
            if (response.status.isSuccess()) {
                Result.success(response.bodyAsBytes())
            } else {
                // Try without auth (public bucket)
                val publicResponse = httpClient.get(s3Url)
                if (publicResponse.status.isSuccess()) {
                    Result.success(publicResponse.bodyAsBytes())
                } else {
                    Result.failure(Exception("R2 download failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            Log.error("R2 download error", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete translation content from R2.
     */
    suspend fun deleteTranslation(objectKey: String): Result<Unit> {
        if (!config.isR2Configured()) {
            return Result.failure(Exception("R2 not configured"))
        }
        
        return try {
            // Use Cloudflare R2 API for delete
            val url = "https://api.cloudflare.com/client/v4/accounts/${config.accountId}/r2/buckets/${config.r2BucketName}/objects/$objectKey"
            
            val response = httpClient.delete(url) {
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
            } else if (config.isR2Configured()) {
                "https://api.cloudflare.com/client/v4/accounts/${config.accountId}/r2/buckets/${config.r2BucketName}/objects/$objectKey"
            } else {
                return false
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
