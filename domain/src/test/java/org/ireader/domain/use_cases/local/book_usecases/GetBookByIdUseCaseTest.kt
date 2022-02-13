package org.ireader.domain.use_cases.local.book_usecases


import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.FakeLocalBookRepository
import org.junit.Before
import org.junit.Test

class GetBookByIdUseCaseTest {

    private lateinit var getBooks: GetBookById

    @Before
    fun setUp() {
        val fakeLocalBookRepository = FakeLocalBookRepository()
        getBooks = GetBookById(fakeLocalBookRepository)

        val booksToInsert = mutableListOf<Book>()
        ('a'..'z').forEachIndexed { index, c ->
            booksToInsert.add(Book(
                id = index,
                title = c.toString(),
                link = c.toString(),
                sourceId = 1
            ))
        }
        fakeLocalBookRepository.books.addAll(booksToInsert)
    }

    @Test
    fun `return books`() = runBlocking {
        val id = 1
        val result = getBooks(id).first()
        assertThat(result?.id == id).isTrue()
    }

    @Test
    fun `return nothing when there is no book with this id`() = runBlocking {
        val id = 500
        val result = getBooks(id).first()
        assertThat(result?.id == id).isFalse()
    }

}