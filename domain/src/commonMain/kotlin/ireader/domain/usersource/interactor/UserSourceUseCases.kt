package ireader.domain.usersource.interactor

import ireader.domain.usersource.model.UserSource
import ireader.domain.usersource.repository.UserSourceRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case for getting all user sources.
 */
class GetUserSources(
    private val repository: UserSourceRepository
) {
    fun subscribe(): Flow<List<UserSource>> = repository.getAllAsFlow()
    
    suspend fun await(): List<UserSource> = repository.getAll()
    
    suspend fun getEnabled(): List<UserSource> = repository.getEnabled()
}

/**
 * Use case for getting a single user source.
 */
class GetUserSource(
    private val repository: UserSourceRepository
) {
    suspend fun byUrl(sourceUrl: String): UserSource? = repository.getByUrl(sourceUrl)
    
    suspend fun byId(sourceId: Long): UserSource? = repository.getById(sourceId)
}

/**
 * Use case for saving a user source.
 */
class SaveUserSource(
    private val repository: UserSourceRepository
) {
    suspend fun await(source: UserSource) {
        repository.upsert(source)
    }
    
    suspend fun awaitAll(sources: List<UserSource>) {
        repository.upsertAll(sources)
    }
}

/**
 * Use case for deleting a user source.
 */
class DeleteUserSource(
    private val repository: UserSourceRepository
) {
    suspend fun byUrl(sourceUrl: String) {
        repository.delete(sourceUrl)
    }
    
    suspend fun byId(sourceId: Long) {
        repository.deleteById(sourceId)
    }
    
    suspend fun all() {
        repository.deleteAll()
    }
}

/**
 * Use case for toggling source enabled state.
 */
class ToggleUserSourceEnabled(
    private val repository: UserSourceRepository
) {
    suspend fun await(sourceUrl: String, enabled: Boolean) {
        repository.setEnabled(sourceUrl, enabled)
    }
}

/**
 * Use case for importing/exporting user sources.
 */
class ImportExportUserSources(
    private val repository: UserSourceRepository
) {
    suspend fun exportToJson(): String = repository.exportToJson()
    
    suspend fun importFromJson(json: String): Result<Int> = repository.importFromJson(json)
}

/**
 * Use case for validating a user source configuration.
 */
class ValidateUserSource {
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String>
    )
    
    fun validate(source: UserSource): ValidationResult {
        val errors = mutableListOf<String>()
        
        if (source.sourceUrl.isBlank()) {
            errors.add("Source URL is required")
        }
        
        if (source.sourceName.isBlank()) {
            errors.add("Source name is required")
        }
        
        if (source.searchUrl.isBlank() && source.exploreUrl.isBlank()) {
            errors.add("At least one of Search URL or Explore URL is required")
        }
        
        if (source.searchUrl.isNotBlank() && source.ruleSearch.bookList.isBlank()) {
            errors.add("Search rule: Book list selector is required")
        }
        
        if (source.searchUrl.isNotBlank() && source.ruleSearch.name.isBlank()) {
            errors.add("Search rule: Name selector is required")
        }
        
        if (source.searchUrl.isNotBlank() && source.ruleSearch.bookUrl.isBlank()) {
            errors.add("Search rule: Book URL selector is required")
        }
        
        if (source.ruleToc.chapterList.isBlank()) {
            errors.add("TOC rule: Chapter list selector is required")
        }
        
        if (source.ruleToc.chapterName.isBlank()) {
            errors.add("TOC rule: Chapter name selector is required")
        }
        
        if (source.ruleToc.chapterUrl.isBlank()) {
            errors.add("TOC rule: Chapter URL selector is required")
        }
        
        if (source.ruleContent.content.isBlank()) {
            errors.add("Content rule: Content selector is required")
        }
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}
