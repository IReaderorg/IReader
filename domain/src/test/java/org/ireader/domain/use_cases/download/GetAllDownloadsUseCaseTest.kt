package org.ireader.domain.use_cases.download


import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.FakeDownloadRepository
import org.ireader.domain.use_cases.download.get.GetAllDownloadsUseCase
import org.junit.Before
import org.junit.Test


class GetAllDownloadsUseCaseTest {

    private lateinit var getDownloads: GetAllDownloadsUseCase
    private var totalSize: Int = 0

    @Before
    fun setUp() {
        val fakeRepository = FakeDownloadRepository()
        getDownloads = GetAllDownloadsUseCase(fakeRepository)

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
    fun `get all downloads`() = runBlocking {
        val res = getDownloads().first()
        assertThat(res.size == totalSize).isTrue()
    }


}