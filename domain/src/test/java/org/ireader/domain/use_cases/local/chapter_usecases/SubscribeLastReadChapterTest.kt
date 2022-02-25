package org.ireader.domain.use_cases.local.chapter_usecases

import com.google.common.truth.Truth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.FakeChapterRepository
import org.junit.Before
import org.junit.Test

class SubscribeLastReadChapterTest {

    private lateinit var subscribeLastReadChapter: SubscribeLastReadChapter


    private val lastReadChapterIndex = 5L
    private val bookId = 1L

    @Before
    fun setUp() {
        val fakeRepository = FakeChapterRepository()
        subscribeLastReadChapter = SubscribeLastReadChapter(fakeRepository)

        val items = mutableListOf<Chapter>()
        ('a'..'z').forEachIndexed { index, c ->
            items.add(Chapter(
                id = index.toLong(),
                title = c.toString(),
                link = c.toString(),
                bookId = bookId,
                lastRead = index.toLong()
            ))
        }
        fakeRepository.chapters.addAll(items)
    }


    @Test
    fun `return correct chapter if available`() = runBlocking {
        val result = subscribeLastReadChapter(bookId).first()
        Truth.assertThat(result?.id == lastReadChapterIndex).isTrue()
    }

}