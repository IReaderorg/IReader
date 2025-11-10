package ireader.domain.data.repository

import ireader.domain.models.entities.SourceComparison
import kotlinx.coroutines.flow.Flow

interface SourceComparisonRepository {
    
    suspend fun getSourceComparisonByBookId(bookId: Long): SourceComparison?
    
    fun subscribeSourceComparisonByBookId(bookId: Long): Flow<SourceComparison?>
    
    suspend fun insertSourceComparison(sourceComparison: SourceComparison)
    
    suspend fun updateSourceComparison(sourceComparison: SourceComparison)
    
    suspend fun upsertSourceComparison(sourceComparison: SourceComparison)
    
    suspend fun deleteSourceComparison(bookId: Long)
    
    suspend fun deleteOldEntries(timestamp: Long)
    
    suspend fun updateDismissedUntil(bookId: Long, dismissedUntil: Long)
}
