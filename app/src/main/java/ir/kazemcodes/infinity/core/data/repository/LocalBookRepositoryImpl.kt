package ir.kazemcodes.infinity.core.data.repository

import androidx.paging.*
import ir.kazemcodes.infinity.core.data.local.BookDatabase
import ir.kazemcodes.infinity.core.data.local.ExploreBook
import ir.kazemcodes.infinity.core.data.local.dao.LibraryBookDao
import ir.kazemcodes.infinity.core.data.local.dao.LibraryChapterDao
import ir.kazemcodes.infinity.core.data.network.models.ChapterPage
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.BookEntity
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.feature_detail.presentation.book_detail.Constants
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreRemoteMediator
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.jsoup.select.Selector
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class LocalBookRepositoryImpl(
    private val bookDao: LibraryBookDao,
    private val libraryChapterDao: LibraryChapterDao,
    private val bookDatabase: BookDatabase
) : LocalBookRepository {

    override fun getBooks(): Flow<PagingData<BookEntity>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE),
            pagingSourceFactory = {
                getAllInLibraryForPagingBooks()
            }
        ).flow
    }


    override fun getAllInLibraryForPagingBooks(): PagingSource<Int, BookEntity> {
        return bookDao.getAllBooksForPaging()
    }

    override fun getAllBooks(): Flow<Resource<List<Book>>> = flow {
        try {
            emit(Resource.Loading())
            bookDao.getAllBooks().map { list -> list.map { it.toBook() } }.collect { books ->
                emit(Resource.Success(books))
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
            bookDao.getBookById(id).map { it.toBook() }
                .collect { book ->
                    emit(Resource.Success(data = book))
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

    override fun getBookByName(bookName: String): Flow<Resource<Book>> = flow {
        try {
            emit(Resource.Loading())
            bookDao.getBookByName(bookName).map { it.toBook() }.collect { book ->
                emit(Resource.Success(data = book))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "There is No Book"))
        }

    }

    override fun searchBook(query: String): Flow<PagingData<BookEntity>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE),
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
        return bookDao.insertBook(book.toBookEntity())
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
                emit(Resource.Error<Book>(message = e.message.toString()))
            }
        }

    override fun getAllExploreBook(): PagingSource<Int, ExploreBook> {
        return bookDao.getAllExploreBook()
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

                        emit(Resource.Success<Book>(data = bookEntity.toBook()))


                    }
                Timber.d("Timber: GetExploreBookByIdUseCase was Finished Successfully")

            } catch (e: Exception) {
                emit(Resource.Error<Book>(message = e.message.toString()))
            }
        }


    override fun getRemoteBookDetail(book: Book, source: Source): Flow<Resource<Book>> = flow {
        emit(Resource.Loading())
        try {
            Timber.d("Timber: Remote Book Detail for ${book.bookName} Was called")
            val bookDetail = source.fetchBook(book)

            Timber.d("Timber: Remote Book Detail Was Fetched")
            emit(Resource.Success<Book>(bookDetail.book.copy(bookName = book.bookName,
                link = book.link,
                coverLink = book.coverLink,
                source = source.name)))
        } catch (e: HttpException) {
            emit(
                Resource.Error<Book>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )

        } catch (e: IOException) {
            emit(Resource.Error<Book>(message = "Couldn't Read Server, Check Your Internet Connection."))
        } catch (e: Exception) {
            emit(
                Resource.Error<Book>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )
        }
    }

    override fun getRemoteMostPopularBooksUseCase(
        page: Int,
        source: Source,
    ): Flow<Resource<List<Book>>> = flow {
        try {
            emit(Resource.Loading())
            Timber.d("Timber: GetRemoteMostPopularBooksUseCase page: $page was Called")
            val books = source.fetchPopular(page)

            if (books.isCloudflareEnabled) {
                emit(Resource.Error<List<Book>>("CloudFlare is Enable"))
            } else {
                emit(Resource.Success<List<Book>>(books.books.map { it.copy(source = source.name) }))
            }


            Timber.d("Timber: GetRemoteMostPopularBooksUseCase page: $page was Finished Successfully")
            if (!books.hasNextPage) {
                Resource.Error<List<Book>>(
                    message = "There is No More Books"
                )
            }

        } catch (e: HttpException) {
            emit(
                Resource.Error<List<Book>>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )
        } catch (e: IOException) {
            emit(Resource.Error<List<Book>>(message = "Couldn't Read Server, Check Your Internet Connection."))
        } catch (e: Exception) {
            emit(
                Resource.Error<List<Book>>(
                    message = e.localizedMessage ?: "An Unexpected Error Occurred."
                )
            )

        }
    }


    @OptIn(ExperimentalPagingApi::class)
    override fun getRemoteBooksUseCase(
        source: Source,
        exploreType: ExploreType,
    ): Flow<PagingData<ExploreBook>> {
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE),
            pagingSourceFactory = {
                getAllExploreBook()
            }, remoteMediator = ExploreRemoteMediator(
                source = source,
                database = bookDatabase,
                exploreType = exploreType
            )
        ).flow
    }

    override fun getRemoteChaptersUseCase(
        book: Book,
        source: Source,
    ): Flow<Resource<List<Chapter>>> =
        flow {
            emit(Resource.Loading())
            try {
                Timber.d("Timber: GetRemoteChaptersUseCase was Called")
                val chapters = mutableListOf<Chapter>()
                var currentPage = 1

                var hasNextPage = true

                while (hasNextPage) {
                    Timber.d("Timber: GetRemoteChaptersUseCase was with pages $currentPage Called")
                    val chaptersPage = source.fetchChapters(book = book, page = currentPage)
                    chapters.addAll(chaptersPage.chapters)
                    hasNextPage = chaptersPage.hasNextPage
                    currentPage += 1
                }
                emit(Resource.Success<List<Chapter>>(chapters))
                Timber.d("Timber: GetRemoteChaptersUseCase was Finished Successfully")

            } catch (e: HttpException) {
                emit(
                    Resource.Error<List<Chapter>>(
                        message = e.localizedMessage ?: "An Unexpected Error Occurred."
                    )
                )
            } catch (e: IOException) {
                emit(Resource.Error<List<Chapter>>(message = "Couldn't Read Remote Server, Check Your Internet Connection."))
            } catch (e: Selector.SelectorParseException) {
                emit(Resource.Error<List<Chapter>>(message = "Source is not working."))
            } catch (e: Exception) {
                emit(Resource.Error<List<Chapter>>(message = e.localizedMessage
                    ?: "An Unexpected Error Occurred"))
            }
        }

    override fun getRemoteReadingContentUseCase(
        chapter: Chapter,
        source: Source,
    ): Flow<Resource<ChapterPage>> = flow<Resource<ChapterPage>> {
        try {
            emit(Resource.Loading())
            Timber.d("Timber: GetRemoteReadingContentUseCase was Called")
            val content = source.fetchContent(chapter)
            if (content.content.isEmpty() || content.content.contains(Constants.CLOUDFLARE_LOG)) {
                emit(Resource.Error<ChapterPage>(message = "Can't Get The Chapter Content."))
            } else {
                Timber.d("Timber: GetRemoteReadingContentUseCase was Finished Successfully")
                emit(Resource.Success<ChapterPage>(content))

            }

        } catch (e: HttpException) {
            emit(Resource.Error<ChapterPage>(message = e.localizedMessage
                ?: "An Unexpected Error Occurred."))
        } catch (e: IOException) {
            emit(Resource.Error<ChapterPage>(message = e.localizedMessage
                ?: "Couldn't Read Server, Check Your Internet Connection."))
        } catch (e: Exception) {
            emit(Resource.Error<ChapterPage>(message = e.localizedMessage
                ?: "An Unexpected Error Occurred"))
        }
    }
}