package ireader.data.glossary

import ireader.data.backend.BackendService
import ireader.data.core.DatabaseHandler
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.GlobalGlossaryRepository
import ireader.domain.models.entities.GlobalGlossary
import ireader.domain.models.entities.GlobalGlossaryEntry
import ireader.domain.models.entities.GlobalGlossaryExport
import ireader.domain.models.entities.GlossaryTermType
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Repository implementation for global glossaries with Supabase sync support.
 * Handles both local SQLite storage and remote Supabase synchronization.
 */
class GlobalGlossaryRepositoryImpl(
    private val handler: DatabaseHandler,
    private val backendService: BackendService? = null,
    private val getCurrentUserId: suspend () -> String? = { null },
    private val json: Json = Json { 
        ignoreUnknownKeys = true 
        prettyPrint = true
        isLenient = true
        coerceInputValues = true
    }
) : GlobalGlossaryRepository {

    // DTO for Supabase glossary_entries table
    @Serializable
    private data class GlossaryEntryDto(
        @SerialName("id") val id: String? = null,
        @SerialName("user_id") val userId: String? = null,
        @SerialName("book_key") val bookKey: String,
        @SerialName("book_title") val bookTitle: String,
        @SerialName("source_term") val sourceTerm: String,
        @SerialName("target_term") val targetTerm: String,
        @SerialName("term_type") val termType: String = "custom",
        @SerialName("notes") val notes: String? = null,
        @SerialName("source_language") val sourceLanguage: String = "auto",
        @SerialName("target_language") val targetLanguage: String = "en",
        @SerialName("is_public") val isPublic: Boolean = false,
        @SerialName("created_at") val createdAt: String? = null,
        @SerialName("updated_at") val updatedAt: String? = null
    )

    // Local database operations
    override suspend fun getById(id: Long): GlobalGlossary? {
        return handler.awaitOneOrNull { globalGlossaryQueries.getById(id) }?.toGlobalGlossary()
    }

    override suspend fun getByBookKey(bookKey: String): List<GlobalGlossary> {
        return handler.awaitList { globalGlossaryQueries.getByBookKey(bookKey) }
            .map { it.toGlobalGlossary() }
    }

    override suspend fun getByBookKeyAndType(bookKey: String, termType: GlossaryTermType): List<GlobalGlossary> {
        return handler.awaitList { globalGlossaryQueries.getByBookKeyAndType(bookKey, termType.toString()) }
            .map { it.toGlobalGlossary() }
    }

    override suspend fun search(bookKey: String, query: String): List<GlobalGlossary> {
        return handler.awaitList { globalGlossaryQueries.search(bookKey, query) }
            .map { it.toGlobalGlossary() }
    }

    override suspend fun searchAll(query: String): List<GlobalGlossary> {
        return handler.awaitList { globalGlossaryQueries.searchAll(query) }
            .map { it.toGlobalGlossary() }
    }

    override suspend fun getAllBookKeys(): List<Pair<String, String>> {
        return handler.awaitList { globalGlossaryQueries.getAllBookKeys() }
            .map { it.book_key to it.book_title }
    }

    override suspend fun insert(glossary: GlobalGlossary): Long {
        return handler.await(inTransaction = true) {
            globalGlossaryQueries.insert(
                bookKey = glossary.bookKey,
                bookTitle = glossary.bookTitle,
                sourceTerm = glossary.sourceTerm,
                targetTerm = glossary.targetTerm,
                termType = glossary.termType.toString(),
                notes = glossary.notes,
                sourceLanguage = glossary.sourceLanguage,
                targetLanguage = glossary.targetLanguage,
                createdAt = glossary.createdAt,
                updatedAt = glossary.updatedAt,
                syncedAt = glossary.syncedAt,
                remoteId = glossary.remoteId
            )
            globalGlossaryQueries.selectLastInsertedRowId().executeAsOne()
        }
    }

    override suspend fun update(glossary: GlobalGlossary) {
        handler.await {
            globalGlossaryQueries.update(
                id = glossary.id,
                targetTerm = glossary.targetTerm,
                termType = glossary.termType.toString(),
                notes = glossary.notes,
                updatedAt = glossary.updatedAt
            )
        }
    }

    override suspend fun upsert(glossary: GlobalGlossary) {
        handler.await {
            globalGlossaryQueries.upsert(
                bookKey = glossary.bookKey,
                bookTitle = glossary.bookTitle,
                sourceTerm = glossary.sourceTerm,
                targetTerm = glossary.targetTerm,
                termType = glossary.termType.toString(),
                notes = glossary.notes,
                sourceLanguage = glossary.sourceLanguage,
                targetLanguage = glossary.targetLanguage,
                createdAt = glossary.createdAt,
                updatedAt = glossary.updatedAt,
                syncedAt = glossary.syncedAt,
                remoteId = glossary.remoteId
            )
        }
    }

    override suspend fun delete(id: Long) {
        handler.await { globalGlossaryQueries.delete(id) }
    }

    override suspend fun deleteByBookKey(bookKey: String) {
        handler.await { globalGlossaryQueries.deleteByBookKey(bookKey) }
    }

    override fun subscribeToBookKey(bookKey: String): Flow<List<GlobalGlossary>> {
        return handler.subscribeToList { globalGlossaryQueries.getByBookKey(bookKey) }
            .map { list -> list.map { it.toGlobalGlossary() } }
    }

    override fun subscribeToAll(): Flow<List<GlobalGlossary>> {
        return handler.subscribeToList { globalGlossaryQueries.getAll() }
            .map { list -> list.map { it.toGlobalGlossary() } }
    }


    // Remote sync operations
    override suspend fun syncToRemote(bookKey: String): Result<Int> {
        val userId = getCurrentUserId() ?: return Result.failure(
            IllegalStateException("User not authenticated")
        )
        val backend = backendService ?: return Result.failure(
            IllegalStateException("Backend service not available")
        )
        
        return RemoteErrorMapper.withErrorMapping {
            val localEntries = getByBookKey(bookKey)
            var syncedCount = 0
            val now = currentTimeToLong()
            
            for (entry in localEntries) {
                val data = buildJsonObject {
                    put("user_id", userId)
                    put("book_key", entry.bookKey)
                    put("book_title", entry.bookTitle)
                    put("source_term", entry.sourceTerm)
                    put("target_term", entry.targetTerm)
                    put("term_type", entry.termType.toString())
                    entry.notes?.let { put("notes", it) }
                    put("source_language", entry.sourceLanguage)
                    put("target_language", entry.targetLanguage)
                    put("is_public", false)
                }
                
                val result = backend.upsert(
                    table = "glossary_entries",
                    data = data,
                    onConflict = "user_id,book_key,source_term",
                    returning = true
                ).getOrThrow()
                
                // Update local entry with remote ID and sync time
                result?.let { remoteEntry ->
                    val dto = json.decodeFromJsonElement(GlossaryEntryDto.serializer(), remoteEntry)
                    dto.id?.let { remoteId ->
                        handler.await {
                            globalGlossaryQueries.updateSyncStatus(
                                id = entry.id,
                                syncedAt = now,
                                remoteId = remoteId
                            )
                        }
                    }
                }
                syncedCount++
            }
            
            syncedCount
        }
    }

    override suspend fun syncFromRemote(bookKey: String): Result<Int> {
        val userId = getCurrentUserId() ?: return Result.failure(
            IllegalStateException("User not authenticated")
        )
        val backend = backendService ?: return Result.failure(
            IllegalStateException("Backend service not available")
        )
        
        return RemoteErrorMapper.withErrorMapping {
            // Fetch user's own entries for this book
            val userEntries = backend.query(
                table = "glossary_entries",
                filters = mapOf(
                    "user_id" to userId,
                    "book_key" to bookKey
                )
            ).getOrThrow()
            
            // Also fetch public/community entries for this book
            val publicEntries = backend.query(
                table = "glossary_entries",
                filters = mapOf(
                    "book_key" to bookKey,
                    "is_public" to true
                )
            ).getOrThrow()
            
            val allEntries = (userEntries + publicEntries).distinctBy { 
                json.decodeFromJsonElement(GlossaryEntryDto.serializer(), it).let { dto ->
                    "${dto.bookKey}_${dto.sourceTerm}"
                }
            }
            
            val now = currentTimeToLong()
            var importedCount = 0
            
            for (entry in allEntries) {
                val dto = json.decodeFromJsonElement(GlossaryEntryDto.serializer(), entry)
                val glossary = GlobalGlossary(
                    bookKey = dto.bookKey,
                    bookTitle = dto.bookTitle,
                    sourceTerm = dto.sourceTerm,
                    targetTerm = dto.targetTerm,
                    termType = GlossaryTermType.fromString(dto.termType),
                    notes = dto.notes,
                    sourceLanguage = dto.sourceLanguage,
                    targetLanguage = dto.targetLanguage,
                    createdAt = now,
                    updatedAt = now,
                    syncedAt = now,
                    remoteId = dto.id
                )
                upsert(glossary)
                importedCount++
            }
            
            importedCount
        }
    }

    override suspend fun syncAllFromRemote(): Result<Int> {
        val userId = getCurrentUserId() ?: return Result.failure(
            IllegalStateException("User not authenticated")
        )
        val backend = backendService ?: return Result.failure(
            IllegalStateException("Backend service not available")
        )
        
        return RemoteErrorMapper.withErrorMapping {
            // Fetch all user's entries
            val userEntries = backend.query(
                table = "glossary_entries",
                filters = mapOf("user_id" to userId)
            ).getOrThrow()
            
            val now = currentTimeToLong()
            var importedCount = 0
            
            for (entry in userEntries) {
                val dto = json.decodeFromJsonElement(GlossaryEntryDto.serializer(), entry)
                val glossary = GlobalGlossary(
                    bookKey = dto.bookKey,
                    bookTitle = dto.bookTitle,
                    sourceTerm = dto.sourceTerm,
                    targetTerm = dto.targetTerm,
                    termType = GlossaryTermType.fromString(dto.termType),
                    notes = dto.notes,
                    sourceLanguage = dto.sourceLanguage,
                    targetLanguage = dto.targetLanguage,
                    createdAt = now,
                    updatedAt = now,
                    syncedAt = now,
                    remoteId = dto.id
                )
                upsert(glossary)
                importedCount++
            }
            
            importedCount
        }
    }

    override suspend fun getRemoteGlossaries(bookKey: String): Result<List<GlobalGlossary>> {
        val backend = backendService ?: return Result.failure(
            IllegalStateException("Backend service not available")
        )
        
        return RemoteErrorMapper.withErrorMapping {
            // Fetch public/community entries for this book
            val entries = backend.query(
                table = "glossary_entries",
                filters = mapOf(
                    "book_key" to bookKey,
                    "is_public" to true
                )
            ).getOrThrow()
            
            val now = currentTimeToLong()
            entries.map { entry ->
                val dto = json.decodeFromJsonElement(GlossaryEntryDto.serializer(), entry)
                GlobalGlossary(
                    bookKey = dto.bookKey,
                    bookTitle = dto.bookTitle,
                    sourceTerm = dto.sourceTerm,
                    targetTerm = dto.targetTerm,
                    termType = GlossaryTermType.fromString(dto.termType),
                    notes = dto.notes,
                    sourceLanguage = dto.sourceLanguage,
                    targetLanguage = dto.targetLanguage,
                    createdAt = now,
                    updatedAt = now,
                    syncedAt = now,
                    remoteId = dto.id
                )
            }
        }
    }

    override suspend fun uploadGlossary(glossary: GlobalGlossary): Result<String> {
        val userId = getCurrentUserId() ?: return Result.failure(
            IllegalStateException("User not authenticated")
        )
        val backend = backendService ?: return Result.failure(
            IllegalStateException("Backend service not available")
        )
        
        return RemoteErrorMapper.withErrorMapping {
            val data = buildJsonObject {
                put("user_id", userId)
                put("book_key", glossary.bookKey)
                put("book_title", glossary.bookTitle)
                put("source_term", glossary.sourceTerm)
                put("target_term", glossary.targetTerm)
                put("term_type", glossary.termType.toString())
                glossary.notes?.let { put("notes", it) }
                put("source_language", glossary.sourceLanguage)
                put("target_language", glossary.targetLanguage)
                put("is_public", false)
            }
            
            val result = backend.insert(
                table = "glossary_entries",
                data = data,
                returning = true
            ).getOrThrow()
            
            val dto = result?.let { json.decodeFromJsonElement(GlossaryEntryDto.serializer(), it) }
            dto?.id ?: throw IllegalStateException("Failed to get remote ID")
        }
    }


    // Import/Export operations
    override suspend fun importFromJson(jsonString: String, bookKey: String, bookTitle: String): Result<Int> {
        return try {
            val export = json.decodeFromString<GlobalGlossaryExport>(jsonString)
            val now = currentTimeToLong()
            var count = 0
            
            export.entries.forEach { entry ->
                val glossary = GlobalGlossary(
                    bookKey = bookKey,
                    bookTitle = bookTitle,
                    sourceTerm = entry.sourceTerm,
                    targetTerm = entry.targetTerm,
                    termType = GlossaryTermType.fromString(entry.termType),
                    notes = entry.notes,
                    sourceLanguage = export.sourceLanguage,
                    targetLanguage = export.targetLanguage,
                    createdAt = now,
                    updatedAt = now
                )
                upsert(glossary)
                count++
            }
            
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun exportToJson(bookKey: String): Result<String> {
        return try {
            val entries = getByBookKey(bookKey)
            if (entries.isEmpty()) {
                return Result.failure(IllegalStateException("No entries found for book"))
            }
            
            val first = entries.first()
            val export = GlobalGlossaryExport(
                bookKey = bookKey,
                bookTitle = first.bookTitle,
                sourceLanguage = first.sourceLanguage,
                targetLanguage = first.targetLanguage,
                entries = entries.map { 
                    GlobalGlossaryEntry(
                        sourceTerm = it.sourceTerm,
                        targetTerm = it.targetTerm,
                        termType = it.termType.toString(),
                        notes = it.notes
                    )
                },
                exportedAt = currentTimeToLong()
            )
            
            Result.success(json.encodeToString(GlobalGlossaryExport.serializer(), export))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mapper function
    private fun data.Global_glossary.toGlobalGlossary(): GlobalGlossary {
        return GlobalGlossary(
            id = _id,
            bookKey = book_key,
            bookTitle = book_title,
            sourceTerm = source_term,
            targetTerm = target_term,
            termType = GlossaryTermType.fromString(term_type),
            notes = notes,
            sourceLanguage = source_language,
            targetLanguage = target_language,
            createdAt = created_at,
            updatedAt = updated_at,
            syncedAt = synced_at,
            remoteId = remote_id
        )
    }
}
