package ireader.data.quote

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import ireader.domain.data.repository.DiscordQuoteRepository
import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import io.ktor.util.encodeBase64
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Implementation of DiscordQuoteRepository that sends quotes to Discord webhook
 * securely via Supabase Edge Function proxy, with direct webhook fallback.
 */
class DiscordQuoteRepositoryImpl(
    private val webhookUrl: String = "",
    private val supabaseUrl: String = "",
    private val supabaseKey: String = "",
    private val httpClient: HttpClient,
    private val quoteCardGenerator: QuoteCardGenerator
) : DiscordQuoteRepository {
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    override suspend fun submitQuote(
        quote: LocalQuote,
        style: QuoteCardStyle,
        username: String
    ): Result<Unit> = runCatching {
        if (supabaseUrl.isBlank() && webhookUrl.isBlank()) {
            throw Exception("Neither Supabase nor Discord webhook URL configured")
        }
        
        // Generate quote card image
        val imageBytes = withContext(Dispatchers.Default) {
            quoteCardGenerator.generateQuoteCard(quote, style)
        }
        
        if (supabaseUrl.isNotBlank()) {
            val base64Image = imageBytes.encodeBase64()
            val bodyJson = buildJsonObject {
                put("type", "quote")
                put("title", "📚 New Quote Shared")
                put("description", "\"${quote.text.take(2000)}\"")
                put("username", username)
                put("fileBase64", base64Image)
                put("fileName", "quote_${quote.createdAt}.png")
                put("color", 5814783)
                putJsonArray("fields") {
                    addJsonObject {
                        put("name", "Book")
                        put("value", quote.bookTitle)
                        put("inline", true)
                    }
                    val authorValue = quote.author
                    if (!authorValue.isNullOrBlank()) {
                        addJsonObject {
                            put("name", "Author")
                            put("value", authorValue)
                            put("inline", true)
                        }
                    }
                    if (quote.chapterTitle.isNotBlank()) {
                        addJsonObject {
                            put("name", "Chapter")
                            put("value", quote.chapterTitle)
                            put("inline", false)
                        }
                    }
                    addJsonObject {
                        put("name", "Shared by")
                        put("value", "@$username")
                        put("inline", false)
                    }
                }
            }.toString()

            val response = httpClient.post("${supabaseUrl.trimEnd('/')}/functions/v1/discord-webhook") {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                header(HttpHeaders.ContentType, "application/json")
                setBody(bodyJson)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                throw Exception("Discord webhook via Supabase proxy failed: ${response.status} - $errorBody")
            }
        } else {
            // Fallback to direct Discord webhook
            val embed = DiscordEmbed(
                title = "📚 New Quote Shared",
                description = "\"${quote.text.take(2000)}\"",
                fields = buildList {
                    add(DiscordEmbedField("Book", quote.bookTitle, inline = true))
                    val authorValue = quote.author
                    if (!authorValue.isNullOrBlank()) {
                        add(DiscordEmbedField("Author", authorValue, inline = true))
                    }
                    if (quote.chapterTitle.isNotBlank()) {
                        add(DiscordEmbedField("Chapter", quote.chapterTitle, inline = false))
                    }
                    add(DiscordEmbedField("Shared by", "@$username", inline = false))
                },
                color = 5814783,
                timestamp = kotlin.time.Clock.System.now().toString(),
                footer = DiscordEmbedFooter("IReader Community")
            )
            
            val payload = DiscordWebhookPayload(
                embeds = listOf(embed)
            )
            
            val response = httpClient.submitFormWithBinaryData(
                url = webhookUrl,
                formData = formData {
                    append("payload_json", json.encodeToString(payload))
                    append(
                        "file",
                        imageBytes,
                        Headers.build {
                            append(HttpHeaders.ContentType, "image/png")
                            append(HttpHeaders.ContentDisposition, "filename=\"quote_${quote.createdAt}.png\"")
                        }
                    )
                }
            )
            
            if (response.status != HttpStatusCode.OK && response.status != HttpStatusCode.NoContent) {
                val errorBody = response.bodyAsText()
                throw Exception("Discord webhook failed: ${response.status} - $errorBody")
            }
        }
    }
}

/**
 * Discord webhook payload structure
 */
@Serializable
private data class DiscordWebhookPayload(
    val embeds: List<DiscordEmbed>
)

@Serializable
private data class DiscordEmbed(
    val title: String,
    val description: String,
    val fields: List<DiscordEmbedField>,
    val color: Int,
    val timestamp: String,
    val footer: DiscordEmbedFooter
)

@Serializable
private data class DiscordEmbedField(
    val name: String,
    val value: String,
    val inline: Boolean = false
)

@Serializable
private data class DiscordEmbedFooter(
    val text: String
)
