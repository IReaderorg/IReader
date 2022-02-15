package org.ireader.domain.use_cases.download

import com.google.common.truth.Truth
import kotlinx.coroutines.runBlocking
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.FakeDownloadRepository
import org.ireader.domain.use_cases.download.delete.DeleteAllSavedDownload
import org.junit.Before
import org.junit.Test

class DeleteAllSavedDownloadTest {

    private lateinit var deleteAllSavedDownload: DeleteAllSavedDownload
    private lateinit var fakeRepository: FakeDownloadRepository
    private var totalSize: Int = 0

    @Before
    fun setUp() {
        fakeRepository = FakeDownloadRepository()
        deleteAllSavedDownload = DeleteAllSavedDownload(fakeRepository)

        val downloadsToInsert = mutableListOf<SavedDownload>()
        ('a'..'z').forEachIndexed { index, c ->
            downloadsToInsert.add(SavedDownload(
                bookName = c.toString(),
                bookId = index.toLong(),
                sourceId = index.toLong(),
                chapterId = index.toLong(),
                translator = c.toString(),
                progress = index,
                chapterKey = c.toString(),
                chapterName = c.toString(),
                priority = index,
                totalChapter = index,
            ))

        }
        totalSize = downloadsToInsert.size
        fakeRepository.downloads.addAll(downloadsToInsert)
    }

    @Test
    fun `delete one download`() = runBlocking {
        deleteAllSavedDownload()
        Truth.assertThat(fakeRepository.downloads.size == 0).isTrue()
    }


}