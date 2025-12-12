package ireader.domain.usecases.glossary

import ireader.domain.data.repository.GlobalGlossaryRepository
import ireader.domain.models.entities.GlobalGlossary
import ireader.domain.models.entities.GlossaryTermType
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime

/**
 * Use case for getting global glossary entries by book key
 */
class GetGlobalGlossaryUseCase(
    private val repository: GlobalGlossaryRepository
) {
    suspend fun execute(bookKey: String): List<GlobalGlossary> {
        return repository.getByBookKey(bookKey)
    }
    
    fun subscribe(bookKey: String): Flow<List<GlobalGlossary>> {
        return repository.subscribeToBookKey(bookKey)
    }
    
    fun subscribeAll(): Flow<List<GlobalGlossary>> {
        return repository.subscribeToAll()
    }
}

/**
 * Use case for saving global glossary entries
 */
class SaveGlobalGlossaryUseCase(
    private val repository: GlobalGlossaryRepository
) {
    @OptIn(ExperimentalTime::class)
    suspend fun execute(
        bookKey: String,
        bookTitle: String,
        sourceTerm: String,
        targetTerm: String,
        termType: GlossaryTermType,
        notes: String? = null,
        sourceLanguage: String = "auto",
        targetLanguage: String = "en",
        entryId: Long? = null
    ): Long {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        val glossary = GlobalGlossary(
            id = entryId ?: 0,
            bookKey = bookKey,
            bookTitle = bookTitle,
            sourceTerm = sourceTerm,
            targetTerm = targetTerm,
            termType = termType,
            notes = notes,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            createdAt = if (entryId != null) 0 else now,
            updatedAt = now
        )
        
        repository.upsert(glossary)
        return glossary.id
    }
}

/**
 * Use case for deleting global glossary entries
 */
class DeleteGlobalGlossaryUseCase(
    private val repository: GlobalGlossaryRepository
) {
    suspend fun execute(id: Long) {
        repository.delete(id)
    }
    
    suspend fun executeForBook(bookKey: String) {
        repository.deleteByBookKey(bookKey)
    }
}

/**
 * Use case for searching global glossary
 */
class SearchGlobalGlossaryUseCase(
    private val repository: GlobalGlossaryRepository
) {
    suspend fun execute(bookKey: String, query: String): List<GlobalGlossary> {
        return repository.search(bookKey, query)
    }
    
    suspend fun executeAll(query: String): List<GlobalGlossary> {
        return repository.searchAll(query)
    }
}

/**
 * Use case for getting all book keys with glossaries
 */
class GetGlossaryBooksUseCase(
    private val repository: GlobalGlossaryRepository
) {
    suspend fun execute(): List<Pair<String, String>> {
        return repository.getAllBookKeys()
    }
}

/**
 * Use case for exporting global glossary
 */
class ExportGlobalGlossaryUseCase(
    private val repository: GlobalGlossaryRepository
) {
    suspend fun execute(bookKey: String): Result<String> {
        return repository.exportToJson(bookKey)
    }
}

/**
 * Use case for importing global glossary
 */
class ImportGlobalGlossaryUseCase(
    private val repository: GlobalGlossaryRepository
) {
    suspend fun execute(json: String, bookKey: String, bookTitle: String): Result<Int> {
        return repository.importFromJson(json, bookKey, bookTitle)
    }
}

/**
 * Use case for syncing glossary with remote
 */
class SyncGlossaryUseCase(
    private val repository: GlobalGlossaryRepository
) {
    suspend fun syncToRemote(bookKey: String): Result<Int> {
        return repository.syncToRemote(bookKey)
    }
    
    suspend fun syncFromRemote(bookKey: String): Result<Int> {
        return repository.syncFromRemote(bookKey)
    }
    
    suspend fun syncAll(): Result<Int> {
        return repository.syncAllFromRemote()
    }
}

/**
 * Use case for getting glossary as a map for translation
 */
class GetGlossaryMapUseCase(
    private val repository: GlobalGlossaryRepository
) {
    suspend fun execute(bookKey: String): Map<String, String> {
        val entries = repository.getByBookKey(bookKey)
        return entries.associate { it.sourceTerm to it.targetTerm }
    }
}

/**
 * Aggregate container for all global glossary use cases
 */
data class GlobalGlossaryUseCases(
    val getGlossary: GetGlobalGlossaryUseCase,
    val saveGlossary: SaveGlobalGlossaryUseCase,
    val deleteGlossary: DeleteGlobalGlossaryUseCase,
    val searchGlossary: SearchGlobalGlossaryUseCase,
    val getBooks: GetGlossaryBooksUseCase,
    val exportGlossary: ExportGlobalGlossaryUseCase,
    val importGlossary: ImportGlobalGlossaryUseCase,
    val syncGlossary: SyncGlossaryUseCase,
    val getGlossaryMap: GetGlossaryMapUseCase
)
