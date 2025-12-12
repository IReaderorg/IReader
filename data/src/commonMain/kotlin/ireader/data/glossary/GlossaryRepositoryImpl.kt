package ireader.data.glossary

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.GlossaryRepository
import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlossaryTermType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GlossaryRepositoryImpl(
    private val handler: DatabaseHandler
) : GlossaryRepository {

    override suspend fun getGlossaryById(id: Long): Glossary? {
        return handler.awaitOneOrNull { glossaryQueries.getGlossaryById(id) }?.toGlossary()
    }

    override suspend fun getGlossaryByBookId(bookId: Long): List<Glossary> {
        return handler.awaitList { glossaryQueries.getGlossaryByBookId(bookId) }
            .map { it.toGlossary() }
    }

    override suspend fun getGlossaryByBookIdAndType(bookId: Long, termType: GlossaryTermType): List<Glossary> {
        return handler.awaitList { glossaryQueries.getGlossaryByBookIdAndType(bookId, termType.toString()) }
            .map { it.toGlossary() }
    }

    override suspend fun searchGlossary(bookId: Long, query: String): List<Glossary> {
        return handler.awaitList { glossaryQueries.searchGlossary(bookId, query) }
            .map { it.toGlossary() }
    }

    override suspend fun insertGlossary(glossary: Glossary): Long {
        return handler.await(inTransaction = true) {
            glossaryQueries.insert(
                bookId = glossary.bookId,
                sourceTerm = glossary.sourceTerm,
                targetTerm = glossary.targetTerm,
                termType = glossary.termType.toString(),
                notes = glossary.notes,
                createdAt = glossary.createdAt,
                updatedAt = glossary.updatedAt
            )
            glossaryQueries.selectLastInsertedRowId().executeAsOne()
        }
    }

    override suspend fun updateGlossary(glossary: Glossary) {
        handler.await {
            glossaryQueries.update(
                id = glossary.id,
                targetTerm = glossary.targetTerm,
                termType = glossary.termType.toString(),
                notes = glossary.notes,
                updatedAt = glossary.updatedAt
            )
        }
    }

    override suspend fun upsertGlossary(glossary: Glossary) {
        handler.await {
            glossaryQueries.upsert(
                bookId = glossary.bookId,
                sourceTerm = glossary.sourceTerm,
                targetTerm = glossary.targetTerm,
                termType = glossary.termType.toString(),
                notes = glossary.notes,
                createdAt = glossary.createdAt,
                updatedAt = glossary.updatedAt
            )
        }
    }

    override suspend fun deleteGlossary(id: Long) {
        handler.await { glossaryQueries.deleteGlossary(id) }
    }

    override suspend fun deleteGlossaryByBookId(bookId: Long) {
        handler.await { glossaryQueries.deleteGlossaryByBookId(bookId) }
    }

    override fun subscribeToGlossaryByBookId(bookId: Long): Flow<List<Glossary>> {
        return handler.subscribeToList { glossaryQueries.getGlossaryByBookId(bookId) }
            .map { list -> list.map { it.toGlossary() } }
    }

    private fun data.Glossary.toGlossary(): Glossary {
        return Glossary(
            id = _id,
            bookId = book_id,
            sourceTerm = source_term,
            targetTerm = target_term,
            termType = GlossaryTermType.fromString(term_type),
            notes = notes,
            createdAt = created_at,
            updatedAt = updated_at
        )
    }
}
