package ireader.data.plugins

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import ireader.domain.plugins.PluginRepositoryIndex
import ireader.domain.plugins.PluginRepositoryIndexFetcher
import kotlinx.serialization.json.Json

/**
 * Implementation of PluginRepositoryIndexFetcher using Ktor HTTP client
 */
class PluginRepositoryIndexFetcherImpl(
    private val httpClient: HttpClient
) : PluginRepositoryIndexFetcher {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override suspend fun fetchIndex(url: String): Result<PluginRepositoryIndex> {
        println("[PluginRepositoryIndexFetcher] Fetching index from: $url")
        return try {
            val response = httpClient.get(url)

            if (!response.status.isSuccess()) {
                val errorMsg = "Failed to fetch plugin index: HTTP ${response.status.value} from $url"
                println("[PluginRepositoryIndexFetcher] $errorMsg")
                return Result.failure(Exception(errorMsg))
            }

            val body = response.bodyAsText()

            if (body.isBlank()) {
                val errorMsg = "Empty response from plugin repository: $url"
                println("[PluginRepositoryIndexFetcher] $errorMsg")
                return Result.failure(Exception(errorMsg))
            }

            println("[PluginRepositoryIndexFetcher] Received ${body.length} bytes, parsing...")
            val index = json.decodeFromString<PluginRepositoryIndex>(body)
            println("[PluginRepositoryIndexFetcher] Parsed ${index.plugins.size} plugins from $url")
            Result.success(index)
        } catch (e: kotlinx.serialization.SerializationException) {
            val errorMsg = "Failed to parse plugin index from $url: ${e.message}"
            println("[PluginRepositoryIndexFetcher] $errorMsg")
            Result.failure(Exception(errorMsg, e))
        } catch (e: Exception) {
            val errorMsg = "Network error fetching plugin index from $url: ${e.message}"
            println("[PluginRepositoryIndexFetcher] $errorMsg")
            Result.failure(e)
        }
    }

}
