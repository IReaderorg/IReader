package ireader.domain.usecases.glossary

import ireader.domain.data.repository.GlossaryRepository
import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlossaryExport
import ireader.domain.models.entities.GlossaryTermType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class GetGlossaryByBookIdUseCase(
    private val repository: GlossaryRepository
) {
    suspend fun execute(bookId: Long): List<Glossary> {
        return repository.getGlossaryByBookId(bookId)
    }
    
    fun subscribe(bookId: Long): Flow<List<Glossary>> {
        return repository.subscribeToGlossaryByBookId(bookId)
    }
}

class GetGlossaryByTypeUseCase(
    private val repository: GlossaryRepository
) {
    suspend fun execute(bookId: Long, termType: GlossaryTermType): List<Glossary> {
        return repository.getGlossaryByBookIdAndType(bookId, termType)
    }
}

class SearchGlossaryUseCase(
    private val repository: GlossaryRepository
) {
    suspend fun execute(bookId: Long, query: String): List<Glossary> {
        return repository.searchGlossary(bookId, query)
    }
}

class SaveGlossaryEntryUseCase(
    private val repository: GlossaryRepository
) {
    suspend fun execute(
        bookId: Long,
        sourceTerm: String,
        targetTerm: String,
        termType: GlossaryTermType,
        notes: String? = null,
        entryId: Long? = null
    ): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        val glossary = Glossary(
            id = entryId ?: 0,
            bookId = bookId,
            sourceTerm = sourceTerm,
            targetTerm = targetTerm,
            termType = termType,
            notes = notes,
            createdAt = if (entryId != null) 0 else now, // Will be preserved by upsert if updating
            updatedAt = now
        )
        
        repository.upsertGlossary(glossary)
        return bookId
    }
}

class UpdateGlossaryEntryUseCase(
    private val repository: GlossaryRepository
) {
    suspend fun execute(glossary: Glossary) {
        val updated = glossary.copy(
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )
        repository.updateGlossary(updated)
    }
}

class DeleteGlossaryEntryUseCase(
    private val repository: GlossaryRepository
) {
    suspend fun execute(id: Long) {
        repository.deleteGlossary(id)
    }
    
    suspend fun executeForBook(bookId: Long) {
        repository.deleteGlossaryByBookId(bookId)
    }
}

class ExportGlossaryUseCase(
    private val repository: GlossaryRepository
) {
    suspend fun execute(bookId: Long, bookTitle: String): String {
        val entries = repository.getGlossaryByBookId(bookId)
        val export = GlossaryExport(
            bookId = bookId,
            bookTitle = bookTitle,
            entries = entries,
            exportedAt = Clock.System.now().toEpochMilliseconds()
        )
        
        return Json.encodeToString(export)
    }
}

class ImportGlossaryUseCase(
    private val repository: GlossaryRepository
) {
    suspend fun execute(jsonString: String, targetBookId: Long? = null): Int {
        val export = Json.decodeFromString<GlossaryExport>(jsonString)
        val bookId = targetBookId ?: export.bookId
        
        export.entries.forEach { entry ->
            val glossary = entry.copy(
                id = 0, // Reset ID for new entries
                bookId = bookId
            )
            repository.upsertGlossary(glossary)
        }
        
        return export.entries.size
    }
}

class ImportGlossaryFromUrlUseCase(
    private val repository: GlossaryRepository
) {
    suspend fun execute(url: String, bookId: Long): Result<Int> {
        return try {
            // This would need HTTP client implementation
            // For now, return a placeholder
            Result.failure(Exception("URL import not yet implemented"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class GetGlossaryAsMapUseCase(
    private val repository: GlossaryRepository
) {
    suspend fun execute(bookId: Long): Map<String, String> {
        val entries = repository.getGlossaryByBookId(bookId)
        return entries.associate { it.sourceTerm to it.targetTerm }
    }
}
