package ireader.domain.data.repository

import ireader.domain.models.entities.ChapterHealth
import kotlinx.coroutines.flow.Flow

interface ChapterHealthRepository {
    
    suspend fun getChapterHealthById(chapterId: Long): ChapterHealth?
    
    fun subscribeChapterHealthById(chapterId: Long): Flow<ChapterHealth?>
    
    suspend fun insertChapterHealth(chapterHealth: ChapterHealth)
    
    suspend fun updateChapterHealth(chapterHealth: ChapterHealth)
    
    suspend fun upsertChapterHealth(chapterHealth: ChapterHealth)
    
    suspend fun deleteChapterHealth(chapterId: Long)
    
    suspend fun deleteOldEntries(timestamp: Long)
}
