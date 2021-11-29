package ir.kazemcodes.infinity.library_feature.domain.use_case.chapter

import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.library_feature.domain.util.InvalidBookException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

class GetLocalChapterUseCase @Inject constructor(
    private val repository: Repository
) {

    @Throws(InvalidBookException::class)
    operator fun invoke(bookName : String) : Flow<Resource<Chapter>> = flow{
        try {
            emit(Resource.Loading())
            repository.localChapterRepository.getChapter(bookName= bookName).collect { chapter->
                if (chapter != null) {
                    emit(Resource.Success<Chapter>(data = chapter.toChapter() ))
                }else{
                    emit(Resource.Success<Chapter>(data = Chapter.create()))
                }

            }
        }catch (e : Exception) {
            Timber.e("GetLocalChapterUseCase: " + e.localizedMessage)
            emit(Resource.Error<Chapter>(message = e.message.toString()))
        }
    }



}