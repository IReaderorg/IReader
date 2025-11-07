package ireader.data.translation

import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlossaryTermType

val glossaryMapper = { _id: Long,
                       book_id: Long,
                       source_term: String,
                       target_term: String,
                       term_type: String,
                       notes: String?,
                       created_at: Long,
                       updated_at: Long ->
    Glossary(
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
