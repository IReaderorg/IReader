package ireader.domain.usersource.autodetect

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

/**
 * Use case for auto-detecting CSS selectors from a webpage.
 */
class AutoDetectUseCase(
    private val httpClient: HttpClient,
    private val detector: SelectorAutoDetector = SelectorAutoDetector()
) {
    
    sealed class Result {
        data class Success(val detection: SelectorAutoDetector.PageDetectionResult) : Result()
        data class Error(val message: String) : Result()
    }
    
    /**
     * Analyze a search results page.
     */
    suspend fun analyzeSearchPage(url: String): Result {
        return try {
            val html = fetchHtml(url)
            val detection = detector.detectSearchPage(html, extractBaseUrl(url))
            Result.Success(detection)
        } catch (e: Exception) {
            Result.Error("Failed to analyze page: ${e.message}")
        }
    }
    
    /**
     * Analyze a book info page.
     */
    suspend fun analyzeBookInfoPage(url: String): Result {
        return try {
            val html = fetchHtml(url)
            val detection = detector.detectBookInfoPage(html, extractBaseUrl(url))
            Result.Success(detection)
        } catch (e: Exception) {
            Result.Error("Failed to analyze page: ${e.message}")
        }
    }
    
    /**
     * Analyze a chapter list page.
     */
    suspend fun analyzeChapterListPage(url: String): Result {
        return try {
            val html = fetchHtml(url)
            val detection = detector.detectChapterListPage(html, extractBaseUrl(url))
            Result.Success(detection)
        } catch (e: Exception) {
            Result.Error("Failed to analyze page: ${e.message}")
        }
    }
    
    /**
     * Analyze a content page.
     */
    suspend fun analyzeContentPage(url: String): Result {
        return try {
            val html = fetchHtml(url)
            val detection = detector.detectContentPage(html, extractBaseUrl(url))
            Result.Success(detection)
        } catch (e: Exception) {
            Result.Error("Failed to analyze page: ${e.message}")
        }
    }
    
    private suspend fun fetchHtml(url: String): String {
        val response = httpClient.get(url)
        return response.bodyAsText()
    }
    
    private fun extractBaseUrl(url: String): String {
        val regex = Regex("""^(https?://[^/]+)""")
        return regex.find(url)?.value ?: url
    }
}
