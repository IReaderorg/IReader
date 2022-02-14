package org.ireader.domain.use_cases.download.delete

import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.DownloadRepository
import org.ireader.domain.utils.Resource
import retrofit2.HttpException
import java.io.IOException

class DeleteSavedDownload(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(savedDownload: SavedDownload) {
        try {
            downloadRepository.deleteSavedDownload(savedDownload)
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