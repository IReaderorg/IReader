package ireader.domain.usersource.importer

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import ireader.domain.usersource.model.UserSource
import ireader.domain.usersource.repository.UserSourceRepository

/**
 * Use case for importing sources from JSON or URL.
 */
class ImportSourceUseCase(
    private val httpClient: HttpClient,
    private val repository: UserSourceRepository,
    private val importer: SourceImporter = SourceImporter()
) {
    
    sealed class Result {
        data class Success(val importedCount: Int, val sources: List<UserSource>) : Result()
        data class Error(val message: String) : Result()
    }
    
    /**
     * Import sources from JSON string.
     */
    suspend fun importFromJson(json: String): Result {
        return when (val result = importer.importFromJson(json)) {
            is SourceImporter.ImportResult.Success -> {
                saveSources(result.sources)
                Result.Success(result.sources.size, result.sources)
            }
            is SourceImporter.ImportResult.Error -> {
                Result.Error(result.message + (result.details?.let { ": $it" } ?: ""))
            }
        }
    }
    
    /**
     * Import sources from URL.
     */
    suspend fun importFromUrl(url: String): Result {
        return try {
            val resolvedUrl = importer.parseImportUrl(url) ?: url
            val response = httpClient.get(resolvedUrl)
            val json = response.bodyAsText()
            importFromJson(json)
        } catch (e: Exception) {
            Result.Error("Failed to fetch from URL: ${e.message}")
        }
    }
    
    /**
     * Save selected sources to repository.
     */
    suspend fun saveSelectedSources(sources: List<UserSource>): Result {
        return try {
            saveSources(sources)
            Result.Success(sources.size, sources)
        } catch (e: Exception) {
            Result.Error("Failed to save sources: ${e.message}")
        }
    }
    
    private suspend fun saveSources(sources: List<UserSource>) {
        sources.forEach { source ->
            repository.upsert(source)
        }
    }
}
