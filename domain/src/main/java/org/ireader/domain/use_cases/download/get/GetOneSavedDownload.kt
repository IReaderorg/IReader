package org.ireader.domain.use_cases.download.get

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.DownloadRepository
import org.ireader.domain.utils.Resource
import retrofit2.HttpException
import java.io.IOException

class GetOneSavedDownload(private val downloadRepository: DownloadRepository) {
    operator fun invoke(bookId: Long): Flow<SavedDownload?> = flow {
        try {
            downloadRepository.findOneSavedDownload(bookId).first { downloads ->
                emit(downloads)
                true
            }
        } catch (e: IOException) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.ExceptionString(e)
            )
        } catch (e: HttpException) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.ExceptionString(e)
            )
        } catch (e: Exception) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.ExceptionString(e)
            )
        }


    }
}