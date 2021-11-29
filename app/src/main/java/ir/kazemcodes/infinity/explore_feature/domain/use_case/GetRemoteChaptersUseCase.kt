package ir.kazemcodes.infinity.explore_feature.domain.use_case

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.library_feature.domain.util.InvalidBookException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetRemoteChaptersUseCase @Inject constructor(
    private val repository: Repository
) {

    @Throws(InvalidBookException::class)
    operator fun invoke(book: Book): Flow<Resource<List<Chapter>>> =
        flow {
                emit(Resource.Loading())
            try {
                val elements = repository.remote.getElements(book.link, headers = mutableMapOf(
                    Pair<String, String>("Referer", book.link)
                ))
                val chapters = repository.remote.getChapters(book, elements = elements)
                emit(Resource.Success<List<Chapter>>(chapters))
            } catch (e: HttpException) {
                emit(
                    Resource.Error<List<Chapter>>(
                        message = e.localizedMessage ?: "An Unexpected Error Occurred."
                    )
                )
            } catch (e: IOException) {
                emit(Resource.Error<List<Chapter>>(message = "Couldn't Read Remote Server, Check Your Internet Connection."))
            } catch (e: Exception) {
                emit(Resource.Error<List<Chapter>>(message = e.localizedMessage?:"An Unexpected Error Occurred"))
            }
        }

}