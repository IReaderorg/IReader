package ir.kazemcodes.infinity.library_feature.domain.use_case.chapter

import android.util.Log
import ir.kazemcodes.infinity.base_feature.repository.Repository
import ir.kazemcodes.infinity.base_feature.util.Constants
import ir.kazemcodes.infinity.core.Resource
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.library_feature.domain.model.ChapterEntity
import ir.kazemcodes.infinity.library_feature.domain.util.InvalidBookException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException
import javax.inject.Inject

class InsertLocalChapterUseCase @Inject constructor(
    private val repository: Repository
) {

    @Throws(InvalidBookException::class)
    suspend operator fun invoke(chapterEntity: List<ChapterEntity>,bookName: String): Flow<Resource<List<Chapter>>> =
        flow {
            try {
                emit(Resource.Loading())
                repository.localChapterRepository.insertChapters(chapterEntity = chapterEntity, bookName = bookName)
                Log.d(
                    Constants.TAG,
                    "GetBookDetailUseCase: local BookDetail was loaded Successfully"
                )
            } catch (e: IOException) {
                emit(Resource.Error<List<Chapter>>(message = "Couldn't load from local database."))
            } catch (e: Exception) {
                emit(Resource.Error<List<Chapter>>(message = e.message.toString()))
            }
        }
}