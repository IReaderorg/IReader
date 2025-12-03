package ireader.data.characterart

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.headers
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import ireader.data.repository.CharacterArtRemoteDataSource
import ireader.domain.models.characterart.ArtStyleFilter
import ireader.domain.models.characterart.CharacterArt
import ireader.domain.models.characterart.CharacterArtSort
import ireader.domain.models.characterart.SubmitCharacterArtRequest
import kotlinx.serialization.json.Json
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.ExperimentalTime

/**
 * Cloudflare R2 implementation for character art image storage.
 * 
 * R2 is S3-compatible, so we use AWS Signature Version 4 for authentication.
 * 
 * Setup:
 * 1. Create R2 bucket in Cloudflare dashboard
 * 2. Create API token with Object Read & Write permissions
 * 3. Configure credentials in local.properties or environment
 * 
 * See docs/CLOUDFLARE_R2_SETUP.md for detailed instructions.
 */
class CloudflareR2DataSource(
    private val httpClient: HttpClient,
    private val config: R2Config,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : CharacterArtRemoteDataSource {
    
    private val endpoint = "https://${config.accountId}.r2.cloudflarestorage.com"
    
    // Note: R2 stores files only. Metadata should be stored in your database (Supabase, etc.)
    // This implementation focuses on image upload/download
    
    override suspend fun getApprovedArt(
        filter: ArtStyleFilter,
        sort: CharacterArtSort,
        searchQuery: String,
        limit: Int,
        offset: Int
    ): Result<List<CharacterArt>> {
        // R2 is object storage - metadata queries should go to your database
        // This would typically call your Supabase/database API
        return Result.failure(NotImplementedError("Use database for metadata queries"))
    }
    
    override suspend fun getArtByBook(bookTitle: String): Result<List<CharacterArt>> {
        return Result.failure(NotImplementedError("Use database for metadata queries"))
    }
    
    override suspend fun getArtByCharacter(characterName: String): Result<List<CharacterArt>> {
        return Result.failure(NotImplementedError("Use database for metadata queries"))
    }
    
    override suspend fun getFeaturedArt(limit: Int): Result<List<CharacterArt>> {
        return Result.failure(NotImplementedError("Use database for metadata queries"))
    }
    
    override suspend fun getUserSubmissions(): Result<List<CharacterArt>> {
        return Result.failure(NotImplementedError("Use database for metadata queries"))
    }
    
    override suspend fun getPendingArt(): Result<List<CharacterArt>> {
        return Result.failure(NotImplementedError("Use database for metadata queries"))
    }
    
    override suspend fun getArtById(id: String): Result<CharacterArt> {
        return Result.failure(NotImplementedError("Use database for metadata queries"))
    }
    
    override suspend fun submitArt(request: SubmitCharacterArtRequest): Result<CharacterArt> {
        return Result.failure(NotImplementedError("Use database for metadata, uploadImage for files"))
    }
    
    /**
     * Upload image to Cloudflare R2
     * Returns the public URL of the uploaded image
     */
    @OptIn(ExperimentalTime::class)
    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val objectKey = "character-art/pending/${kotlin.time.Clock.System.now().toEpochMilliseconds()}_$fileName"
            val contentType = getContentType(fileName)
            
            val response = httpClient.put("$endpoint/${config.bucketName}/$objectKey") {
                headers {
                    appendAwsHeaders(
                        method = "PUT",
                        path = "/${config.bucketName}/$objectKey",
                        contentType = contentType,
                        payloadHash = sha256Hex(imageBytes)
                    )
                    append(HttpHeaders.ContentType, contentType)
                }
                setBody(imageBytes)
            }
            
            if (response.status.isSuccess()) {
                val publicUrl = "${config.publicUrl}/$objectKey"
                Result.success(publicUrl)
            } else {
                Result.failure(Exception("Upload failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete image from R2
     */
    suspend fun deleteImage(objectKey: String): Result<Unit> {
        return try {
            val response = httpClient.delete("$endpoint/${config.bucketName}/$objectKey") {
                headers {
                    appendAwsHeaders(
                        method = "DELETE",
                        path = "/${config.bucketName}/$objectKey"
                    )
                }
            }
            
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Delete failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Move image from pending to approved folder
     */
    suspend fun approveImage(pendingKey: String, approvedKey: String): Result<String> {
        return try {
            // R2 doesn't have native move - copy then delete
            val copyResponse = httpClient.put("$endpoint/${config.bucketName}/$approvedKey") {
                headers {
                    appendAwsHeaders(
                        method = "PUT",
                        path = "/${config.bucketName}/$approvedKey"
                    )
                    append("x-amz-copy-source", "/${config.bucketName}/$pendingKey")
                }
            }
            
            if (copyResponse.status.isSuccess()) {
                deleteImage(pendingKey)
                val publicUrl = "${config.publicUrl}/$approvedKey"
                Result.success(publicUrl)
            } else {
                Result.failure(Exception("Copy failed: ${copyResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun toggleLike(artId: String): Result<Boolean> {
        return Result.failure(NotImplementedError("Use database for likes"))
    }
    
    override suspend fun approveArt(artId: String, featured: Boolean): Result<Unit> {
        return Result.failure(NotImplementedError("Use database for approval status"))
    }
    
    override suspend fun rejectArt(artId: String, reason: String): Result<Unit> {
        return Result.failure(NotImplementedError("Use database for rejection"))
    }
    
    override suspend fun deleteArt(artId: String): Result<Unit> {
        return Result.failure(NotImplementedError("Use deleteImage for file deletion"))
    }
    
    override suspend fun reportArt(artId: String, reason: String): Result<Unit> {
        return Result.failure(NotImplementedError("Use database for reports"))
    }
    
    // AWS Signature V4 helpers
    
    @OptIn(ExperimentalEncodingApi::class, ExperimentalTime::class)
    private fun HeadersBuilder.appendAwsHeaders(
        method: String,
        path: String,
        contentType: String = "",
        payloadHash: String = EMPTY_PAYLOAD_HASH
    ) {
        val now = kotlin.time.Clock.System.now()
        val dateStamp = now.toString().substring(0, 10).replace("-", "")
        val amzDate = now.toString().replace("-", "").replace(":", "").substring(0, 15) + "Z"
        
        append("x-amz-date", amzDate)
        append("x-amz-content-sha256", payloadHash)
        append("Host", "${config.accountId}.r2.cloudflarestorage.com")
        
        val canonicalHeaders = buildString {
            append("host:${config.accountId}.r2.cloudflarestorage.com\n")
            append("x-amz-content-sha256:$payloadHash\n")
            append("x-amz-date:$amzDate\n")
        }
        
        val signedHeaders = "host;x-amz-content-sha256;x-amz-date"
        
        val canonicalRequest = buildString {
            append("$method\n")
            append("$path\n")
            append("\n") // query string
            append(canonicalHeaders)
            append("\n")
            append(signedHeaders)
            append("\n")
            append(payloadHash)
        }
        
        val credentialScope = "$dateStamp/auto/s3/aws4_request"
        val stringToSign = buildString {
            append("AWS4-HMAC-SHA256\n")
            append("$amzDate\n")
            append("$credentialScope\n")
            append(sha256Hex(canonicalRequest.encodeToByteArray()))
        }
        
        val signingKey = getSignatureKey(config.secretAccessKey, dateStamp, "auto", "s3")
        val signature = hmacSha256Hex(signingKey, stringToSign)
        
        val authHeader = "AWS4-HMAC-SHA256 " +
                "Credential=${config.accessKeyId}/$credentialScope, " +
                "SignedHeaders=$signedHeaders, " +
                "Signature=$signature"
        
        append(HttpHeaders.Authorization, authHeader)
    }
    
    private fun getSignatureKey(key: String, dateStamp: String, region: String, service: String): ByteArray {
        val kDate = hmacSha256("AWS4$key".encodeToByteArray(), dateStamp)
        val kRegion = hmacSha256(kDate, region)
        val kService = hmacSha256(kRegion, service)
        return hmacSha256(kService, "aws4_request")
    }
    
    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.encodeToByteArray())
    }
    
    @OptIn(ExperimentalEncodingApi::class)
    private fun hmacSha256Hex(key: ByteArray, data: String): String {
        return hmacSha256(key, data).toHexString()
    }
    
    private fun sha256Hex(data: ByteArray): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest(data).toHexString()
    }
    
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }
    
    private fun getContentType(fileName: String): String {
        return when {
            fileName.endsWith(".jpg", true) || fileName.endsWith(".jpeg", true) -> "image/jpeg"
            fileName.endsWith(".png", true) -> "image/png"
            fileName.endsWith(".webp", true) -> "image/webp"
            fileName.endsWith(".gif", true) -> "image/gif"
            else -> "application/octet-stream"
        }
    }
    
    companion object {
        private const val EMPTY_PAYLOAD_HASH = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
    }
}

/**
 * Configuration for Cloudflare R2
 */
data class R2Config(
    val accountId: String,
    val accessKeyId: String,
    val secretAccessKey: String,
    val bucketName: String,
    val publicUrl: String
) {
    companion object {
        /**
         * Create config from environment variables or properties
         */
        fun fromEnvironment(): R2Config? {
            val accountId = System.getenv("R2_ACCOUNT_ID") ?: return null
            val accessKeyId = System.getenv("R2_ACCESS_KEY_ID") ?: return null
            val secretAccessKey = System.getenv("R2_SECRET_ACCESS_KEY") ?: return null
            val bucketName = System.getenv("R2_BUCKET_NAME") ?: "ireader-character-art"
            val publicUrl = System.getenv("R2_PUBLIC_URL") ?: "https://pub-$accountId.r2.dev"
            
            return R2Config(
                accountId = accountId,
                accessKeyId = accessKeyId,
                secretAccessKey = secretAccessKey,
                bucketName = bucketName,
                publicUrl = publicUrl
            )
        }
    }
}
