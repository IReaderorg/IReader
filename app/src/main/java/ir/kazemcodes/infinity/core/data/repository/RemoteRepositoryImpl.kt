package ir.kazemcodes.infinity.core.data.repository

import android.content.Context
import android.webkit.WebView
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ir.kazemcodes.infinity.core.data.local.BookDatabase
import ir.kazemcodes.infinity.core.data.local.ExploreBook
import ir.kazemcodes.infinity.core.data.local.dao.LibraryBookDao
import ir.kazemcodes.infinity.core.data.network.models.ChapterPage
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.repository.RemoteRepository
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreRemoteMediator
import ir.kazemcodes.infinity.feature_explore.presentation.browse.ExploreType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.jsoup.select.Selector
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class RemoteRepositoryImpl(
    private val bookDao: LibraryBookDao,
    private val localBookRepository: LocalBookRepository,
    private val database: BookDatabase,
) : RemoteRepository {

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

            if (books.errorMessage.isNotBlank()) {
                emit(Resource.Error<List<Book>>(books.errorMessage))
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
        query: String?,
    ): Flow<PagingData<ExploreBook>>  {
            return Pager(
                config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                    maxSize = Constants.MAX_PAGE_SIZE),
                pagingSourceFactory = {
                    localBookRepository.getAllExploreBook()
                }, remoteMediator = ExploreRemoteMediator(
                    source = source,
                    database = database,
                    exploreType = exploreType,
                    query = query
                ),
            ).flow
    }


    override fun getRemoteChaptersUseCase(
        book: Book,
        source: Source,
    ): Flow<Resource<List<Chapter>>> =
        flow {
            try {
                emit(Resource.Loading())
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

            if (content.content.joinToString()
                    .isBlank() || content.content.contains(Constants.CLOUDFLARE_LOG)
            ) {
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

    override suspend fun downloadChapter(
        book: Book,
        source: Source,
        chapters: List<Chapter>,
        factory: (Context) -> WebView,
        totalRetries: Int,
    ): Flow<Chapter> {
        TODO("Not yet implemented")
    }

}


