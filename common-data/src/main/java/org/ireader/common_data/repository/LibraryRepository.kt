package org.ireader.common_data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.DownloadedBook
import org.ireader.common_models.entities.LibraryBook
import org.ireader.common_models.library.LibrarySort

interface LibraryRepository {

    fun subscribeAll(sort: LibrarySort): Flow<List<LibraryBook>>

    fun subscribeUncategorized(sort: LibrarySort): Flow<List<LibraryBook>>

    fun subscribeToCategory(categoryId: Long, sort: LibrarySort): Flow<List<LibraryBook>>

    suspend fun findAll(sort: LibrarySort): List<LibraryBook>

    suspend fun findUncategorized(sort: LibrarySort): List<LibraryBook>

    suspend fun findForCategory(categoryId: Long, sort: LibrarySort): List<LibraryBook>

    suspend fun findDownloadedBooks(): List<DownloadedBook>

    suspend fun findFavorites(): List<Book>
}
