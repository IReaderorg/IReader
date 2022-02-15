package org.ireader.domain.use_cases.download

import com.google.common.truth.Truth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.FakeDownloadRepository
import org.ireader.domain.use_cases.download.get.GetOneSavedDownload
import org.junit.Before
import org.junit.Test

class GetOneSavedDownloadTest {

    private lateinit var getOneSavedDownload: GetOneSavedDownload
    private var totalSize: Int = 0

    @Before
    fun setUp() {
        val fakeRepository = FakeDownloadRepository()
        getOneSavedDownload = GetOneSavedDownload(fakeRepository)

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
    fun `get one download`() = runBlocking {
        val id = 0L
        val res = getOneSavedDownload(id).first()
        Truth.assertThat(res?.bookId == id).isTrue()
    }


}