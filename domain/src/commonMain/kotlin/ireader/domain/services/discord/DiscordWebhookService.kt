package ireader.domain.services.discord

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

/**
 * Service for posting character art to Discord via webhooks
 * 
 * Setup:
 * 1. Create a Discord channel for character art (e.g., #character-art)
 * 2. Go to Channel Settings → Integrations → Webhooks
 * 3. Create webhook and copy the URL
 * 4. Add to local.properties: DISCORD_CHARACTER_ART_WEBHOOK_URL=https://discord.com/api/webhooks/...
 */
class DiscordWebhookService(
    private val httpClient: HttpClient,
    private val webhookUrl: String
) {
    
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
                if (prompt.isNotBlank() && prompt.length <= 200) {
                    append("💭 **Prompt:** $prompt\n")
                }
            }
            
            val response = httpClient.post(webhookUrl) {
                setBody(MultiPartFormDataContent(
                    formData {
                        // Add the message content
                        append("content", content)
                        append("username", username)
                        
                        // Add the image file
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
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Test the webhook connection
     */
    suspend fun testWebhook(): Result<Unit> {
        return try {
            val response = httpClient.post(webhookUrl) {
                setBody(MultiPartFormDataContent(
                    formData {
                        append("content", "✅ IReader character art webhook is working!")
                        append("username", "IReader")
                    }
                ))
            }
            
            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Webhook test failed: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
