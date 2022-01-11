package ir.kazemcodes.infinity.domain.use_cases.remote

import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.util.InvalidBookException
import ir.kazemcodes.infinity.util.Resource
import kotlinx.coroutines.flow.flow
import org.jsoup.select.Selector
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

class GetRemoteChaptersUseCase {

    @Throws(InvalidBookException::class)
    operator fun invoke(
        book: Book,
        source: Source,
    ) =
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

}