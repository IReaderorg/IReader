package ir.kazemcodes.infinity.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import ir.kazemcodes.infinity.core.data.local.BookDatabase
import ir.kazemcodes.infinity.core.data.local.dao.InLibraryUpdate
import ir.kazemcodes.infinity.core.data.local.dao.LibraryBookDao
import ir.kazemcodes.infinity.core.data.local.dao.LibraryChapterDao
import ir.kazemcodes.infinity.core.data.local.dao.RemoteKeysDao
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Constants.NO_BOOKS_ERROR
import ir.kazemcodes.infinity.core.utils.Constants.NO_BOOK_ERROR
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.feature_library.presentation.components.FilterType
import ir.kazemcodes.infinity.feature_library.presentation.components.SortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class LocalBookRepositoryImpl(
    private val bookDao: LibraryBookDao,
    private val libraryChapterDao: LibraryChapterDao,
    private val bookDatabase: BookDatabase,
    private val remoteKeysDao: RemoteKeysDao
) : LocalBookRepository {


    override fun getLocalBooks(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: FilterType,
    ): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                getAllInLibraryForPagingBooks(sortType, isAsc, unreadFilter != FilterType.Disable)
            }
        ).flow
    }


    override fun getAllInLibraryForPagingBooks(
        sortType: SortType,
        isAsc: Boolean,
        unreadFilter: Boolean,
    ): PagingSource<Int, Book> {
        //bookDao.getAllLocalBooksForPagingSortedBySort()
        return when (sortType) {
            is SortType.Alphabetically -> {
                if (unreadFilter) {
                    bookDao.getAllLocalBooksForPagingSortedBySortAndFilter(sortByAbs = true,
                        isAsc = isAsc)
                } else {
                    bookDao.getAllLocalBooksForPagingSortedBySort(sortByAbs = true, isAsc = isAsc)

                }
            }
            is SortType.DateAdded -> {
                if (unreadFilter) {
                    bookDao.getAllLocalBooksForPagingSortedBySortAndFilter(sortByDateAdded = true,
                        isAsc = isAsc)
                } else {
                    bookDao.getAllLocalBooksForPagingSortedBySort(sortByDateAdded = true,
                        isAsc = isAsc)

                }
            }
            is SortType.LastRead -> {
                if (unreadFilter) {
                    bookDao.getAllLocalBooksForPagingSortedBySortAndFilter(sortByLastRead = true,
                        isAsc = isAsc)
                } else {
                    bookDao.getAllLocalBooksForPagingSortedBySort(sortByLastRead = true,
                        isAsc = isAsc)

                }
            }
            is SortType.TotalChapter -> {
                if (unreadFilter) {
                    bookDao.getAllLocalBooksForPagingSortedBySortAndFilter(sortByTotalChapter = true,
                        isAsc = isAsc)
                } else {
                    bookDao.getAllLocalBooksForPagingSortedBySort(sortByTotalChapter = true,
                        isAsc = isAsc)

                }
            }
        }
    }

    override fun getAllLocalBooks(): Flow<Resource<List<Book>>> = flow {
        try {
            emit(Resource.Loading())
            bookDao.getAllBooks().collect { books ->
                if (books != null) {
                    emit(Resource.Success(books))
                } else {
                    Resource.Error<Resource<List<Book>>>(
                        message = NO_BOOKS_ERROR
                    )
                }
            }
        } catch (e: IOException) {
            Resource.Error<Resource<List<Book>>>(
                message = e.localizedMessage ?: ""
            )
        } catch (e: HttpException) {
            Resource.Error<Resource<List<Book>>>(
                message = e.localizedMessage ?: ""
            )
        }


    }

    override fun getLocalBooksById(id: Int): Flow<Resource<Book>> = flow {

        try {
            emit(Resource.Loading())
            bookDao.getBookById(id).first { book ->
                if (book != null) {
                    emit(Resource.Success(data = book))
                    return@first true

                } else {
                    Resource.Error<Resource<Book>>(
                        message = NO_BOOK_ERROR
                    )
                    true
                }
            }
        } catch (e: IOException) {
            Resource.Error<Resource<Book>>(
                message = e.localizedMessage ?: ""
            )
        } catch (e: HttpException) {
            Resource.Error<Resource<Book>>(
                message = e.localizedMessage ?: ""
            )
        }
    }

    override fun getLocalBookByName(bookName: String,sourceName:String): Flow<Resource<Book?>> = flow {
        try {
            emit(Resource.Loading())
            bookDao.getBookByName(bookName, sourceName = sourceName).first { book ->
                if (book != null) {
                    emit(Resource.Success(data = book))
                    return@first true
                } else {
                    emit(Resource.Error("There is No book"))
                    return@first false
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "There is No Book"))
        }

    }

    override fun searchInLibraryScreenBooks(query: String): Flow<PagingData<Book>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                searchBooksByPaging(query)
            }
        ).flow
    }

    override fun searchBooksByPaging(query: String): PagingSource<Int, Book> {
        return bookDao.searchBook(query)
    }

    override fun deleteChapters() {
        libraryChapterDao.deleteLibraryChapters()
    }

    override suspend fun insertBook(book: Book) {
        return bookDao.insertBook(book.toBookEntity()
            .copy(dataAdded = System.currentTimeMillis()))
    }

    override suspend fun deleteBook(id: Int) {
        return bookDao.deleteBook(bookId = id)
    }

    override suspend fun deleteAllBook() {
        return bookDao.deleteAllBook()
    }

    override fun getExploreBookById(id: Int): Flow<Resource<Book>> =
        flow {
            try {
                Timber.d("Timber: GetExploreBookByIdUseCase was Called")
                emit(Resource.Loading())
                remoteKeysDao.getExploreBookById(id = id)
                    .first { bookEntity ->
                        if (bookEntity != null) {
                            emit(Resource.Success<Book>(data = bookEntity))
                            return@first true
                        } else {
                            emit(Resource.Error<Book>(message = "Empty Data."))
                            return@first false
                        }
                    }
                Timber.d("Timber: GetExploreBookByIdUseCase was Finished Successfully")
            } catch (e: Exception) {
                emit(Resource.Error<Book>(message = e.localizedMessage
                    ?: Constants.NO_BOOK_ERROR))
            }
        }



    override fun getAllExploreBook(): PagingSource<Int, Book> {
        return remoteKeysDao.getAllExploreBookByPaging()
    }


    override suspend fun insertAllExploreBook(bookEntity: List<Book>) {
        return remoteKeysDao.insertAllExploredBook(bookEntity)
    }

    override suspend fun deleteAllExploreBook() {
        return remoteKeysDao.deleteAllExploredBook()
    }

    override fun getBookById(id: Int): Flow<Resource<Book>> = flow {
                Timber.d("Timber: GetExploreBookByIdUseCase was Called")
                emit(Resource.Loading())
                bookDao.getBookById(bookId = id)
                    .first { book ->
                        if (book != null) {
                            emit(Resource.Success<Book>(data = book))
                            return@first true
                        } else {
                            emit(Resource.Error<Book>(message = "Empty Data."))
                            return@first false
                        }
                    }
                Timber.d("Timber: GetExploreBookByIdUseCase was Finished Successfully")

        }


    override suspend fun updateLocalBook(book: Book) {
        bookDao.updateBook(InLibraryUpdate(book.id,
            book.inLibrary,
            book.totalChapters,
            book.lastRead,
            unread = book.unread
        ))
    }


}