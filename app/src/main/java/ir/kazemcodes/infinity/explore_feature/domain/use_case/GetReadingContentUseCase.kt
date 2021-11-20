package ir.kazemcodes.infinity.explore_feature.domain.use_case

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.core.Resource
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetReadingContentUseCase @Inject constructor(
    private val repository : Repository
) {


    operator fun invoke(url : String, headers : Map<String,String>) = flow<Resource<String>> {
        try {
            emit(Resource.Loading())
            val elements = repository.remote.getElements(url = url, headers = headers)
            val books = repository.remote.getReadingContent(elements)
            emit(Resource.Success<String>(books))
        } catch (e: HttpException) {
            emit(Resource.Error<String>(message = e.localizedMessage ?: "An Unexpected Error Occurred."))

        } catch (e: IOException) {
            emit(Resource.Error<String>(message = "Couldn't Read Server, Check Your Internet Connection."))
        }
    }

}