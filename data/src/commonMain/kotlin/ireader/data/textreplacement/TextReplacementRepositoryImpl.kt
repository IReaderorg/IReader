package ireader.data.textreplacement

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.TextReplacementRepository
import ireader.domain.models.entities.TextReplacement
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TextReplacementRepositoryImpl(
    private val handler: DatabaseHandler
) : TextReplacementRepository {
    
    override fun getGlobalReplacements(): Flow<List<TextReplacement>> {
        return handler.subscribeToList { textReplacementQueries.getGlobalReplacements() }
            .map { list -> list.map { it.toTextReplacement() } }
    }
    
    override suspend fun getEnabledGlobalReplacements(): List<TextReplacement> {
        return handler.awaitList { textReplacementQueries.getEnabledGlobalReplacements() }
            .map { it.toTextReplacement() }
    }
    
    override fun getReplacementsForBook(bookId: Long): Flow<List<TextReplacement>> {
        return handler.subscribeToList { textReplacementQueries.getReplacementsForBook(bookId) }
            .map { list -> list.map { it.toTextReplacement() } }
    }
    
    override suspend fun getEnabledReplacementsForBook(bookId: Long): List<TextReplacement> {
        return handler.awaitList { textReplacementQueries.getEnabledReplacementsForBook(bookId) }
            .map { it.toTextReplacement() }
    }
    
    override fun getBookSpecificReplacements(bookId: Long): Flow<List<TextReplacement>> {
        return handler.subscribeToList { textReplacementQueries.getBookSpecificReplacements(bookId) }
            .map { list -> list.map { it.toTextReplacement() } }
    }
    
    override suspend fun getReplacementById(id: Long): TextReplacement? {
        return handler.awaitOneOrNull {
            textReplacementQueries.getReplacementById(id)
        }?.toTextReplacement()
    }
    
    override suspend fun insert(replacement: TextReplacement): Long {
        val now = currentTimeToLong()
        return handler.await(inTransaction = true) {
            textReplacementQueries.insert(
                bookId = replacement.bookId,
                name = replacement.name,
                findText = replacement.findText,
                replaceText = replacement.replaceText,
                description = replacement.description,
                enabled = replacement.enabled,
                caseSensitive = replacement.caseSensitive,
                createdAt = now,
                updatedAt = now
            )
            textReplacementQueries.selectLastInsertedRowId().executeAsOne()
        }
    }
    
    override suspend fun insertWithId(replacement: TextReplacement) {
        val now = currentTimeToLong()
        handler.await {
            textReplacementQueries.insertWithId(
                id = replacement.id,
                bookId = replacement.bookId,
                name = replacement.name,
                findText = replacement.findText,
                replaceText = replacement.replaceText,
                description = replacement.description,
                enabled = replacement.enabled,
                caseSensitive = replacement.caseSensitive,
                createdAt = now,
                updatedAt = now
            )
        }
    }
    
    override suspend fun update(replacement: TextReplacement) {
        handler.await {
            textReplacementQueries.update(
                id = replacement.id,
                name = replacement.name,
                findText = replacement.findText,
                replaceText = replacement.replaceText,
                description = replacement.description,
                enabled = replacement.enabled,
                caseSensitive = replacement.caseSensitive,
                updatedAt = currentTimeToLong()
            )
        }
    }
    
    override suspend fun toggleEnabled(id: Long) {
        handler.await {
            textReplacementQueries.toggleEnabled(
                id = id,
                updatedAt = currentTimeToLong()
            )
        }
    }
    
    override suspend fun delete(id: Long) {
        handler.await {
            textReplacementQueries.deleteReplacement(id)
        }
    }
    
    override suspend fun deleteBookReplacements(bookId: Long) {
        handler.await {
            textReplacementQueries.deleteBookReplacements(bookId)
        }
    }
    
    override suspend fun countReplacements(): Long {
        return handler.awaitOne { textReplacementQueries.countReplacements() }
    }
    
    override suspend fun countEnabledReplacements(): Long {
        return handler.awaitOne { textReplacementQueries.countEnabledReplacements() }
    }
    
    private fun data.Text_replacement.toTextReplacement(): TextReplacement {
        return TextReplacement(
            id = _id,
            bookId = book_id,
            name = name,
            findText = find_text,
            replaceText = replace_text,
            description = description,
            enabled = enabled,
            caseSensitive = case_sensitive,
            createdAt = created_at,
            updatedAt = updated_at
        )
    }
}
