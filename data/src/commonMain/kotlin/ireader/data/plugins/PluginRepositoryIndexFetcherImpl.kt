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
        return try {
            val response = httpClient.get(url)

            if (!response.status.isSuccess()) {
                return Result.failure(
                    Exception("Failed to fetch repository: HTTP ${response.status.value}")
                )
            }

            val body = response.bodyAsText()

            if (body.isBlank()) {
                return Result.failure(Exception("Empty response from repository"))
            }

            val index = json.decodeFromString<PluginRepositoryIndex>(body)
            Result.success(index)
        } catch (e: kotlinx.serialization.SerializationException) {
            Result.failure(Exception("Invalid repository format: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }
}
