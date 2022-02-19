package org.ireader.domain.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.ireader.data.local.dao.LibraryBookDao
import org.ireader.domain.models.entities.Book
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LibraryBookDaoTest {
    private lateinit var db: org.ireader.data.local.BookDatabase
    private lateinit var dao: org.ireader.data.local.dao.LibraryBookDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room.inMemoryDatabaseBuilder(context, org.ireader.data.local.BookDatabase::class.java)
                .allowMainThreadQueries()
                .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun insertBook() = runTest {
        val book = Book(title = "Test", link = "https://example.com", sourceId = 1)
        dao.insertBook(book = book)
        val allBooks = dao.subscribeAllInLibraryBooks().first()
        if (allBooks != null) {
            assertThat(book in allBooks)
        } else {
            false
        }
    }
}