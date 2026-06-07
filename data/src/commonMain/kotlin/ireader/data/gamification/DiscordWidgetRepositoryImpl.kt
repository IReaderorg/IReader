package ireader.data.gamification

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import ireader.domain.data.repository.DiscordWidgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DiscordWidgetRepositoryImpl(
    private val widgetUrl: String,
    private val httpClient: HttpClient,
) : DiscordWidgetRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Serializable
    private data class Widget(@SerialName("presence_count") val presenceCount: Int = 0)

    override suspend fun getOnlineCount(): Int? = withContext(Dispatchers.Default) {
        runCatching {
            val body = httpClient.get(widgetUrl).bodyAsText()
            json.decodeFromString(Widget.serializer(), body).presenceCount
        }.getOrNull()
    }
}
