package ir.kazemcodes.infinity.domain.use_cases.local.chapter

import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.domain.utils.InvalidBookException
import ir.kazemcodes.infinity.domain.utils.Resource
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
            Timber.d("Timber: GetLocalChapterUseCase was Called")

            emit(Resource.Loading())
            repository.localChapterRepository.getChapter(bookName= bookName).collect { chapter->
                if (chapter != null) {
                    emit(Resource.Success<Chapter>(data = chapter.toChapter() ))
                }else{
                    emit(Resource.Success<Chapter>(data = Chapter.create()))
                }

            }
            Timber.d("Timber: GetLocalChapterUseCase was Finished Successfully")
        }catch (e : Exception) {
            Timber.e("GetLocalChapterUseCase: " + e.localizedMessage)
            emit(Resource.Error<Chapter>(message = e.message.toString()))
        }
    }



}