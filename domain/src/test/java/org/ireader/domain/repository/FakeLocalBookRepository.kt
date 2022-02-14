package org.ireader.domain.repository

import androidx.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.ireader.domain.models.SortType
import org.ireader.domain.models.entities.Book

class FakeLocalBookRepository : LocalBookRepository {


    val books = mutableListOf<Book>()


    override fun getBookById(id: Long): Flow<Book?> = flow {
        val result = books.find { it.id == id }
        emit(result)
    }

    override fun getAllInLibraryBooks(): Flow<List<Book>> = flow {
        val result = books.filter { it.inLibrary }
        emit(result)
    }

    @OptIn(ExperimentalPagingApi::class)
    override fun getBooksByQueryByPagingSource(query: String): PagingSource<Int, Book> {
        return FakeLibraryMediator(books.filter { it.title == query })

//        Pager(
//            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE),
//            pagingSourceFactory = pagingSourceFactory
//        ).flow
    }

    override fun getBooksByQueryPagingSource(query: String): PagingSource<Int, Book> {
        return FakeLibraryMediator(books.filter { it.title == query })
    }

    override fun getAllInLibraryPagingSource(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): PagingSource<Int, Book> {
        return FakeLibraryMediator(books.filter { it.inLibrary })
    }

    fun getAllInDownloadPagingSource(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): PagingSource<Int, Book> {
        //TODO need to write a text for this
        return FakeLibraryMediator(books.filter { it.inLibrary })
    }

    override fun getAllExploreBookPagingSource(): PagingSource<Int, Book> {
        //TODO need to write a text for this
        return FakeLibraryMediator(books.distinctBy {
            it.title
        })
    }

    override suspend fun deleteNotInLibraryChapters() {
        val b = books.filter { !it.inLibrary }
        books.removeAll(b)
    }

    override suspend fun deleteAllExploreBook() {
        //TODO need to write a text for this
        val b = books.distinctBy {
            it.title
        }
        books.removeAll(b)
    }

    override suspend fun deleteBookById(id: Long) {
        books.removeIf { it.id == id }
    }


    override suspend fun deleteAllBooks() {
        books.clear()
    }

    override suspend fun insertBook(book: Book) {
        books.add(book)
    }

    override suspend fun insertBooks(book: List<Book>) {
        books.addAll(book)
    }
}

private suspend fun <T : Any> PagingData<T>.collectDataForTest(): List<T> {
    val dcb = object : DifferCallback {
        override fun onChanged(position: Int, count: Int) {}
        override fun onInserted(position: Int, count: Int) {}
        override fun onRemoved(position: Int, count: Int) {}
    }
    val items = mutableListOf<T>()
    val dif = object : PagingDataDiffer<T>(dcb, TestCoroutineDispatcher()) {
        override suspend fun presentNewList(
            previousList: NullPaddedList<T>,
            newList: NullPaddedList<T>,
            lastAccessedIndex: Int,
            onListPresentable: () -> Unit,
        ): Int? {
            for (idx in 0 until newList.size)
                items.add(newList.getFromStorage(idx))
            onListPresentable()
            return null
        }
    }
    dif.collectFrom(this)
    return items
}