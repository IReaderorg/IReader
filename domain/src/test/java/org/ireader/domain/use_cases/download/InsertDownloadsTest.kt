package org.ireader.domain.use_cases.download

import com.google.common.truth.Truth
import kotlinx.coroutines.runBlocking
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.FakeDownloadRepository
import org.ireader.domain.use_cases.download.insert.InsertDownloads
import org.junit.Before
import org.junit.Test

class InsertDownloadsTest {

    private lateinit var insertDownloads: InsertDownloads
    private lateinit var fakeRepository: FakeDownloadRepository

    @Before
    fun setUp() {
        fakeRepository = FakeDownloadRepository()
        insertDownloads = InsertDownloads(fakeRepository)
    }

    @Test
    fun `add one download`() = runBlocking {
        val downloadsToInsert = mutableListOf<SavedDownload>()
        ('a'..'z').forEachIndexed { index, c ->
            downloadsToInsert.add(SavedDownload(
                id = index.toLong(),
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
        insertDownloads(downloadsToInsert)
        Truth.assertThat(downloadsToInsert.size == fakeRepository.downloads.size).isTrue()
    }


}