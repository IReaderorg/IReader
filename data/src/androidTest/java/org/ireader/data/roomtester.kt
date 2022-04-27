package org.ireader.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.ireader.data.local.AppDatabase
import org.ireader.data.local.dao.LibraryBookDao
import org.ireader.common_models.entities.Book
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class SimpleEntityReadWriteTest {
    private lateinit var bookDao: LibraryBookDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        bookDao = db.libraryBookDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() = runBlocking {
        val user: org.ireader.common_models.entities.Book =
            org.ireader.common_models.entities.Book(link = "", sourceId = 1L, title = "")
        bookDao.insertBook(user)
        val byName = bookDao.findBookById(1L)
        assertThat(byName != null).isTrue()
    }
}