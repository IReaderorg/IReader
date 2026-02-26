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
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of DiscordQuoteRepository that sends quotes to Discord webhook
 */
class DiscordQuoteRepositoryImpl(
    private val webhookUrl: String,
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
        if (webhookUrl.isBlank()) {
            throw Exception("Discord webhook URL not configured")
        }
        
        // Generate quote card image
        val imageBytes = withContext(Dispatchers.Default) {
            quoteCardGenerator.generateQuoteCard(quote, style)
        }
        
        // Build Discord embed
        val embed = DiscordEmbed(
            title = "📚 New Quote Shared",
            description = "\"${quote.text.take(2000)}\"", // Discord limit
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
            color = 5814783, // Purple color
            timestamp = kotlin.time.Clock.System.now().toString(),
            footer = DiscordEmbedFooter("IReader Community")
        )
        
        val payload = DiscordWebhookPayload(
            embeds = listOf(embed)
        )
        
        // Send to Discord with image attachment
        val response = httpClient.submitFormWithBinaryData(
            url = webhookUrl,
            formData = formData {
                // Add JSON payload
                append("payload_json", json.encodeToString(payload))
                
                // Add image attachment
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
