package ireader.data.characterart

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Supabase Storage implementation for character art images.
 * Uses Supabase's built-in storage which is simpler to set up than R2.
 * 
 * Setup in Supabase:
 * 1. Go to Storage in your Supabase dashboard
 * 2. Create a bucket named "character-art" 
 * 3. Set it to public (for reading) or configure RLS policies
 */
class SupabaseImageStorage(
    private val httpClient: HttpClient,
    private val supabaseUrl: String,
    private val supabaseKey: String,
    private val bucketName: String = "character-art"
) : ImageStorageProvider {
    
    private val json = Json { ignoreUnknownKeys = true }
    private val storageUrl = "$supabaseUrl/storage/v1"
    
    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): Result<String> {
        return try {
            val timestamp = System.currentTimeMillis()
            val objectPath = "pending/${timestamp}_$fileName"
            val contentType = getContentType(fileName)
            
            val response = httpClient.post("$storageUrl/object/$bucketName/$objectPath") {
                header("Authorization", "Bearer $supabaseKey")
                header("apikey", supabaseKey)
                contentType(ContentType.parse(contentType))
                setBody(imageBytes)
            }
            
            if (response.status.isSuccess()) {
                // Return public URL
                val publicUrl = "$supabaseUrl/storage/v1/object/public/$bucketName/$objectPath"
                Result.success(publicUrl)
            } else {
                val error = response.bodyAsText()
                Result.failure(Exception("Upload failed: ${response.status} - $error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val objectPath = extractObjectPath(imageUrl)
            
            val response = httpClient.delete("$storageUrl/object/$bucketName/$objectPath") {
                header("Authorization", "Bearer $supabaseKey")
                header("apikey", supabaseKey)
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
    
    override suspend fun moveToApproved(pendingUrl: String): String {
        return try {
            val pendingPath = extractObjectPath(pendingUrl)
            val approvedPath = pendingPath.replace("pending/", "approved/")
            
            // Supabase Storage move endpoint
            val response = httpClient.post("$storageUrl/object/move") {
                header("Authorization", "Bearer $supabaseKey")
                header("apikey", supabaseKey)
                contentType(ContentType.Application.Json)
                setBody("""{"bucketId":"$bucketName","sourceKey":"$pendingPath","destinationKey":"$approvedPath"}""")
            }
            
            if (response.status.isSuccess()) {
                "$supabaseUrl/storage/v1/object/public/$bucketName/$approvedPath"
            } else {
                // If move fails, return original URL
                pendingUrl
            }
        } catch (e: Exception) {
            pendingUrl
        }
    }
    
    override suspend fun generateThumbnail(imageBytes: ByteArray, width: Int, height: Int): ByteArray {
        // Supabase has image transformation, but for simplicity return original
        // You can use: $supabaseUrl/storage/v1/render/image/public/$bucketName/$path?width=$width&height=$height
        return imageBytes
    }
    
    private fun extractObjectPath(url: String): String {
        // Extract path from URL like: https://xxx.supabase.co/storage/v1/object/public/character-art/pending/123.jpg
        return url.substringAfter("/object/public/$bucketName/")
            .substringAfter("/object/$bucketName/")
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
}
