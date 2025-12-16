package ireader.domain.services.update_service


import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.domain.models.update_service_models.Release
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.i18n.github_api_url
import ireader.i18n.repo_url
import kotlinx.serialization.json.Json



class UpdateApi(
    private val httpClients: HttpClients
) {
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    suspend fun checkRelease(): Release {
        val url = github_api_url + repo_url
        Log.info { "UpdateApi: Fetching release from $url" }
        
        val response = httpClients.default.get(url) {
            headers {
                append(HttpHeaders.Accept, "application/vnd.github.v3+json")
                append(HttpHeaders.UserAgent, "IReader-App")
            }
        }
        
        Log.info { "UpdateApi: Response status: ${response.status}" }
        
        val responseText = response.bodyAsText()
        
        if (response.status.value != 200) {
            Log.error { "UpdateApi: Failed to fetch release. Status: ${response.status}, Body: $responseText" }
            throw Exception("Failed to fetch release: ${response.status}")
        }
        
        Log.debug { "UpdateApi: Response body (first 500 chars): ${responseText.take(500)}" }
        
        return try {
            val release = json.decodeFromString<Release>(responseText)
            Log.info { "UpdateApi: Parsed release - tag_name: ${release.tag_name}, name: ${release.name}, html_url: ${release.html_url}" }
            release
        } catch (e: Exception) {
            Log.error("UpdateApi: Failed to parse release JSON", e)
            throw e
        }
    }
}
