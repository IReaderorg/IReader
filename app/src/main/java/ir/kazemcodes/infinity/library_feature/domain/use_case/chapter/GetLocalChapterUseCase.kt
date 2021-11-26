package ir.kazemcodes.infinity.library_feature.domain.use_case.chapter

import android.util.Log
import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.base_feature.util.Constants
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.library_feature.domain.util.InvalidBookException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetLocalChapterUseCase @Inject constructor(
    private val repository: Repository
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(bookName : String) : Flow<Resource<Chapter>> = flow{
        try {
            emit(Resource.Loading())
            val chapter = repository.localChapterRepository.getChapter(bookName= bookName)
            emit(Resource.Success<Chapter>(data = chapter.toChapter() ))
        }catch (e : Exception) {
            Log.e(Constants.TAG, "invoke: ${e.localizedMessage}", )
            emit(Resource.Error<Chapter>(message = e.message.toString()))
        }
    }



}