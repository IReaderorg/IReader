package org.ireader.domain.use_cases.download

import com.google.common.truth.Truth
import kotlinx.coroutines.runBlocking
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.FakeDownloadRepository
import org.ireader.domain.use_cases.download.insert.InsertDownload
import org.junit.Before
import org.junit.Test

class InsertSavedDownloadTest {

    private lateinit var insertDownload: InsertDownload
    private lateinit var fakeRepository: FakeDownloadRepository
    private var totalSize: Int = 0

    @Before
    fun setUp() {
        fakeRepository = FakeDownloadRepository()
        insertDownload = InsertDownload(fakeRepository)

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
    fun `add one download`() = runBlocking {
        val savedDownload = SavedDownload(
            bookName = 'a'.toString(),
            bookId = 1.toLong(),
            sourceId = 1.toLong(),
            chapterId = 1.toLong(),
            translator = 'a'.toString(),
            progress = 1,
            chapterKey = 'q'.toString(),
            chapterName = 'q'.toString(),
            priority = 1,
            totalChapter = 1,
        )
        val prevSize = fakeRepository.downloads.size
        insertDownload(savedDownload)
        Truth.assertThat(prevSize + 1 == fakeRepository.downloads.size).isTrue()
    }


}