package org.ireader.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import ireader.data.local.AppDatabase
import ireader.data.local.dao.LibraryBookDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTester {
    private lateinit var bookDao: LibraryBookDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
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
        val user: ireader.common.models.entities.Book =
            ireader.common.models.entities.Book(key = "", sourceId = 1L, title = "")
        bookDao.insert(user)
        val byName = bookDao.findBookById(1L)
        assertThat(byName != null).isTrue()
    }
}
