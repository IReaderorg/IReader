package ir.kazemcodes.infinity.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import ir.kazemcodes.infinity.core.data.local.BookDatabase
import ir.kazemcodes.infinity.core.data.local.ExploreBook
import ir.kazemcodes.infinity.core.data.local.dao.InLibraryUpdate
import ir.kazemcodes.infinity.core.data.local.dao.LibraryBookDao
import ir.kazemcodes.infinity.core.data.local.dao.LibraryChapterDao
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.BookEntity
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants.NO_BOOKS_ERROR
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants.NO_BOOK_ERROR
import ir.kazemcodes.infinity.feature_library.presentation.components.SortType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.util.*

class LocalBookRepositoryImpl(
    private val bookDao: LibraryBookDao,
    private val libraryChapterDao: LibraryChapterDao,
    private val bookDatabase: BookDatabase,
) : LocalBookRepository {

    override fun getBooks(sortType: SortType,isAsc: Boolean): Flow<PagingData<BookEntity>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                getAllInLibraryForPagingBooks(sortType,isAsc)
            }
        ).flow
    }


    override fun getAllInLibraryForPagingBooks(sortType : SortType,isAsc:Boolean): PagingSource<Int, BookEntity> {
        return when(sortType) {
            is SortType.Alphabetically -> {
                bookDao.getAllLocalBooksForPagingSortedByAlphabetically(isAsc)
            }
            is SortType.DateAdded -> {
                bookDao.getAllLocalBooksForPagingSortedByDateAdded(isAsc)
            }
            is SortType.LastRead -> {
                bookDao.getAllLocalBooksForPagingSortedByLastRead(isAsc)
            }
            is SortType.TotalChapter -> {
                bookDao.getAllLocalBooksForPagingSortedByTotalChapter(isAsc)
            }
            is SortType.Download -> {
                bookDao.getAllLocalBooksForPagingSortedByDownloads(isAsc)
            }
        }
    }

    override fun getAllBooks(): Flow<Resource<List<Book>>> = flow {
        try {
            emit(Resource.Loading())
            bookDao.getAllBooks().collect { books ->
                if (books != null) {
                    emit(Resource.Success(books.map { it.toBook() }))
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

    override fun getBooksById(id: String): Flow<Resource<Book>> = flow {

        try {
            emit(Resource.Loading())
            bookDao.getBookById(id).collect { book ->
                if (book != null) {
                    emit(Resource.Success(data = book.toBook()))

                } else {
                    Resource.Error<Resource<Book>>(
                        message = NO_BOOK_ERROR
                    )
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

    override fun getLocalBookByName(bookName: String): Flow<Resource<Book?>> = flow {
        try {
            emit(Resource.Loading())
            bookDao.getBookByName(bookName).first { book ->
                if (book != null) {
                    emit(Resource.Success(data = book.toBook()))
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

    override fun searchInLibraryScreenBooks(query: String): Flow<PagingData<BookEntity>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE,enablePlaceholders = true),
            pagingSourceFactory = {
                searchBooksByPaging(query)
            }
        ).flow
    }

    override fun searchBooksByPaging(query: String): PagingSource<Int, BookEntity> {
        return bookDao.searchBook(query)
    }

    override fun deleteChapters() {
        libraryChapterDao.deleteLibraryChapters()
    }

    override suspend fun insertBook(book: Book) {
        return bookDao.insertBook(book.toBookEntity().copy(dataAdded = System.currentTimeMillis()))
    }

    override suspend fun deleteBook(id: String) {
        return bookDao.deleteBook(bookId = id)
    }

    override suspend fun deleteAllBook() {
        return bookDao.deleteAllBook()
    }

    override fun getExploreBookById(id: String): Flow<Resource<Book>> =
        flow {
            try {
                Timber.d("Timber: GetExploreBookByIdUseCase was Called")
                emit(Resource.Loading())
                bookDao.getExploreBookById(id = id)
                    .collect { bookEntity ->
                        if (bookEntity != null) {
                            emit(Resource.Success<Book>(data = bookEntity.toBook()))
                        } else {
                            emit(Resource.Error<Book>(message = "Empty Data."))
                        }


                    }
                Timber.d("Timber: GetExploreBookByIdUseCase was Finished Successfully")

            } catch (e: Exception) {
                emit(Resource.Error<Book>(message = e.localizedMessage?:Constants.NO_BOOK_ERROR))
            }
        }

    override fun getAllExploreBook(): PagingSource<Int, ExploreBook> {
        return bookDao.getAllExploreBookByPaging()
    }


    override suspend fun insertAllExploreBook(bookEntity: List<ExploreBook>) {
        return bookDao.insertAllExploredBook(bookEntity)
    }

    override suspend fun deleteAllExploreBook() {
        return bookDao.deleteAllExploredBook()
    }

    override fun getBookById(bookId: String): Flow<Resource<Book>> =
        flow {
            try {
                Timber.d("Timber: GetExploreBookByIdUseCase was Called")
                emit(Resource.Loading())
                bookDao.getBookById(bookId = bookId)
                    .collect { bookEntity ->

                        if (bookEntity != null) {
                            emit(Resource.Success<Book>(data = bookEntity.toBook()))

                        } else {
                            emit(Resource.Error<Book>(message = NO_BOOK_ERROR))
                        }


                    }
                Timber.d("Timber: GetExploreBookByIdUseCase was Finished Successfully")

            } catch (e: Exception) {
                emit(Resource.Error<Book>(message = e.localizedMessage ?:Constants.NO_BOOK_ERROR))
            }
        }

    override suspend fun updateLocalBook(book: Book) {
       bookDao.updateBook(InLibraryUpdate(book.id,book.inLibrary,book.totalChapters))
    }


}