package ireader.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.Download

@Dao
interface DownloadDao : BaseDao<ireader.common.models.entities.Download> {

    @Query(
        """
        SELECT download.*, library.id, library.title as bookName,
        chapter.'key' as chapterKey, chapter.name as chapterName,
        chapter.translator,library.sourceId,length(chapter.content) > 10 AS isDownloaded
        FROM download
        JOIN library ON download.bookId = library.id
        JOIN chapter ON download.chapterId = chapter.id;
    """
    )
    fun subscribeAllDownloads(): Flow<List<ireader.common.models.entities.SavedDownloadWithInfo>>

    @Query(
        """
                SELECT download.*, library.id,
            library.title as bookName, chapter.'key' as chapterKey,
            chapter.name as chapterName,
        chapter.translator,library.sourceId,length(chapter.content) > 10 AS isDownloaded
        FROM download
        JOIN library ON download.bookId = library.id
        JOIN chapter ON download.chapterId = chapter.id;
    """
    )
    fun findAllDownloads(): List<ireader.common.models.entities.SavedDownloadWithInfo>

    @Query("DELETE FROM download WHERE bookId = :bookId ")
    suspend fun deleteSavedDownloadByBookId(bookId: Long)

    @Query("DELETE FROM download")
    suspend fun deleteAllSavedDownload()

    @Transaction
    suspend fun insertOrUpdate(objList: List<Download>) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<Download>()
        val idList = mutableListOf<Long>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objList[i])
            }
        }

        if (!updateList.isEmpty()) update(updateList)
    }
    @Transaction
    suspend fun insertOrUpdate(objList: Download) {
        kotlin.runCatching {
            val objectToInsert = listOf(objList)
            val insertResult = insert(objectToInsert)
            val updateList = mutableListOf<Download>()
            for (i in insertResult.indices) {
                if (insertResult[i] == -1L) {
                    updateList.add(objectToInsert[i])
                }
            }

            if (updateList.isNotEmpty()) update(updateList)
        }
    }
}
