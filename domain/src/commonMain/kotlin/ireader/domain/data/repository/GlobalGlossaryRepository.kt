package ireader.domain.data.repository

import ireader.domain.models.entities.GlobalGlossary
import ireader.domain.models.entities.GlossaryTermType
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing global glossaries that can exist independently of books.
 * Supports both local storage and remote sync with Supabase.
 */
interface GlobalGlossaryRepository {
    // Local operations
    suspend fun getById(id: Long): GlobalGlossary?
    suspend fun getByBookKey(bookKey: String): List<GlobalGlossary>
    suspend fun getByBookKeyAndType(bookKey: String, termType: GlossaryTermType): List<GlobalGlossary>
    suspend fun search(bookKey: String, query: String): List<GlobalGlossary>
    suspend fun searchAll(query: String): List<GlobalGlossary>
    suspend fun getAllBookKeys(): List<Pair<String, String>> // bookKey to bookTitle
    suspend fun insert(glossary: GlobalGlossary): Long
    suspend fun update(glossary: GlobalGlossary)
    suspend fun upsert(glossary: GlobalGlossary)
    suspend fun delete(id: Long)
    suspend fun deleteByBookKey(bookKey: String)
    fun subscribeToBookKey(bookKey: String): Flow<List<GlobalGlossary>>
    fun subscribeToAll(): Flow<List<GlobalGlossary>>
    
    // Remote sync operations
    suspend fun syncToRemote(bookKey: String): Result<Int>
    suspend fun syncFromRemote(bookKey: String): Result<Int>
    suspend fun syncAllFromRemote(): Result<Int>
    suspend fun getRemoteGlossaries(bookKey: String): Result<List<GlobalGlossary>>
    suspend fun uploadGlossary(glossary: GlobalGlossary): Result<String>
    
    // Bulk operations
    suspend fun importFromJson(json: String, bookKey: String, bookTitle: String): Result<Int>
    suspend fun exportToJson(bookKey: String): Result<String>
}
