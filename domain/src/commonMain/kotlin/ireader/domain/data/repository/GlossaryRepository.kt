package ireader.domain.data.repository

import ireader.domain.models.entities.Glossary
import ireader.domain.models.entities.GlossaryTermType
import kotlinx.coroutines.flow.Flow

interface GlossaryRepository {
    suspend fun getGlossaryById(id: Long): Glossary?
    
    suspend fun getGlossaryByBookId(bookId: Long): List<Glossary>
    
    suspend fun getGlossaryByBookIdAndType(bookId: Long, termType: GlossaryTermType): List<Glossary>
    
    suspend fun searchGlossary(bookId: Long, query: String): List<Glossary>
    
    suspend fun insertGlossary(glossary: Glossary): Long
    
    suspend fun updateGlossary(glossary: Glossary)
    
    suspend fun upsertGlossary(glossary: Glossary)
    
    suspend fun deleteGlossary(id: Long)
    
    suspend fun deleteGlossaryByBookId(bookId: Long)
    
    fun subscribeToGlossaryByBookId(bookId: Long): Flow<List<Glossary>>
}
