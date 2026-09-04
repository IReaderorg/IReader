package ireader.domain.services.discord

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.util.encodeBase64
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Service for posting to Discord via Supabase proxy or direct webhooks
 */
class DiscordWebhookService(
    private val httpClient: HttpClient,
    private val webhookUrl: String = "",
    private val supabaseUrl: String = "",
    private val supabaseKey: String = ""
) {
    val isConfigured: Boolean get() = supabaseUrl.isNotBlank() || webhookUrl.isNotBlank()
    
    /**
     * Post character art to Discord
     * 
     * @param imageBytes The image data
     * @param characterName Name of the character
     * @param bookTitle Title of the book
     * @param bookAuthor Author of the book (optional)
     * @param aiModel AI model used (optional)
     * @param prompt Generation prompt (optional)
     * @param username Username to display (optional)
     * @return Result with Discord message URL on success
     */
    suspend fun postCharacterArt(
        imageBytes: ByteArray,
        characterName: String,
        bookTitle: String,
        bookAuthor: String = "",
        aiModel: String = "",
        prompt: String = "",
        username: String = "IReader"
    ): Result<String> {
        return try {
            // Build embed message
            val content = buildString {
                append("**New Character Art Generated!**\n\n")
                append("📖 **Character:** $characterName\n")
                append("📚 **From:** $bookTitle")
                if (bookAuthor.isNotBlank()) {
                    append(" by $bookAuthor")
                }
                append("\n")
                if (aiModel.isNotBlank()) {
                    append("🤖 **AI Model:** $aiModel\n")
                }
                if (prompt.isNotBlank() && prompt.length <= 500) {
                    append("💭 **Prompt:** $prompt\n")
                }
            }
            
            if (supabaseUrl.isNotBlank()) {
                val base64Image = imageBytes.encodeBase64()
                val bodyJson = buildJsonObject {
                    put("type", "character_art")
                    put("content", content)
                    put("username", username)
                    put("fileBase64", base64Image)
                    put("fileName", "${characterName.replace(" ", "_")}.jpg")
                }.toString()
                
                val response = httpClient.post("${supabaseUrl.trimEnd('/')}/functions/v1/discord-webhook") {
                    header("apikey", supabaseKey)
                    header("Authorization", "Bearer $supabaseKey")
                    header(HttpHeaders.ContentType, "application/json")
                    setBody(bodyJson)
                }
                if (response.status.isSuccess()) {
                    Result.success("Posted to Discord successfully!")
                } else {
                    Result.failure(Exception("Supabase proxy failed: ${response.status}"))
                }
            } else if (webhookUrl.isNotBlank()) {
                val response = httpClient.post(webhookUrl) {
                    setBody(MultiPartFormDataContent(
                        formData {
                            append("content", content)
                            append("username", username)
                            append("file", imageBytes, Headers.build {
                                append(HttpHeaders.ContentType, "image/jpeg")
                                append(HttpHeaders.ContentDisposition, "filename=\"${characterName.replace(" ", "_")}.jpg\"")
                            })
                        }
                    ))
                }
                if (response.status.isSuccess()) {
                    Result.success("Posted to Discord successfully!")
                } else {
                    Result.failure(Exception("Discord webhook failed: ${response.status}"))
                }
            } else {
                Result.failure(Exception("Discord webhook not configured"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Test the webhook connection
     */
    suspend fun testWebhook(): Result<Unit> {
        return postMessage("✅ IReader Discord webhook integration is working!")
    }

    /**
     * Post a plain text message to the webhook channel.
     * Used by the generic share repository (achievements, level-ups, streaks, reviews).
     */
    suspend fun postMessage(content: String, username: String = "IReader", type: String = "share"): Result<Unit> {
        if (supabaseUrl.isBlank() && webhookUrl.isBlank()) {
            return Result.failure(Exception("Discord webhook not configured"))
        }
        return try {
            if (supabaseUrl.isNotBlank()) {
                val bodyJson = buildJsonObject {
                    put("type", type)
                    put("content", content.take(1900))
                    put("username", username)
                }.toString()

                val response = httpClient.post("${supabaseUrl.trimEnd('/')}/functions/v1/discord-webhook") {
                    header("apikey", supabaseKey)
                    header("Authorization", "Bearer $supabaseKey")
                    header(HttpHeaders.ContentType, "application/json")
                    setBody(bodyJson)
                }
                if (response.status.isSuccess()) Result.success(Unit)
                else Result.failure(Exception("Supabase proxy failed: ${response.status}"))
            } else {
                val response = httpClient.post(webhookUrl) {
                    setBody(MultiPartFormDataContent(
                        formData {
                            append("content", content.take(1900))
                            append("username", username)
                        }
                    ))
                }
                if (response.status.isSuccess()) Result.success(Unit)
                else Result.failure(Exception("Discord webhook failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Post an app crash report to Discord via Supabase proxy
     */
    suspend fun postCrashReport(
        exceptionType: String,
        exceptionMessage: String,
        stackTrace: String,
        deviceModel: String,
        appVersion: String,
        androidVersion: String
    ): Result<Unit> {
        if (supabaseUrl.isBlank() && webhookUrl.isBlank()) {
            return Result.failure(Exception("Crash webhook not configured"))
        }
        return try {
            if (supabaseUrl.isNotBlank()) {
                val bodyJson = buildJsonObject {
                    put("type", "crash_report")
                    put("title", "🚨 Crash: $exceptionType")
                    put("description", "**Message:** ${exceptionMessage.take(200)}\n\n```\n${stackTrace.take(3000)}\n```")
                    put("username", "IReader Crash Reporter")
                    putJsonArray("fields") {
                        addJsonObject {
                            put("name", "App Version")
                            put("value", appVersion)
                            put("inline", true)
                        }
                        addJsonObject {
                            put("name", "Device")
                            put("value", deviceModel)
                            put("inline", true)
                        }
                        addJsonObject {
                            put("name", "OS Version")
                            put("value", androidVersion)
                            put("inline", true)
                        }
                    }
                }.toString()

                val response = httpClient.post("${supabaseUrl.trimEnd('/')}/functions/v1/discord-webhook") {
                    header("apikey", supabaseKey)
                    header("Authorization", "Bearer $supabaseKey")
                    header(HttpHeaders.ContentType, "application/json")
                    setBody(bodyJson)
                }
                if (response.status.isSuccess()) Result.success(Unit)
                else Result.failure(Exception("Supabase crash webhook failed: ${response.status}"))
            } else {
                val response = httpClient.post(webhookUrl) {
                    setBody(MultiPartFormDataContent(
                        formData {
                            append("content", "🚨 **Crash: $exceptionType**\n$exceptionMessage\n```\n${stackTrace.take(1500)}\n```")
                            append("username", "IReader Crash Reporter")
                        }
                    ))
                }
                if (response.status.isSuccess()) Result.success(Unit)
                else Result.failure(Exception("Discord crash webhook failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
