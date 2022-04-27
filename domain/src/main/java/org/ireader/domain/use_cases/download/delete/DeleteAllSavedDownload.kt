package org.ireader.domain.use_cases.download.delete

import coil.network.HttpException
import org.ireader.common_models.entities.Book
import org.ireader.core.utils.UiText
import org.ireader.common_data.repository.DownloadRepository
import org.ireader.domain.utils.Resource
import java.io.IOException
import javax.inject.Inject

class DeleteAllSavedDownload @Inject constructor(private val downloadRepository: org.ireader.common_data.repository.DownloadRepository) {
    suspend operator fun invoke() {
        try {
            downloadRepository.deleteAllSavedDownload()
        } catch (e: IOException) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.ExceptionString(e)
            )
        } catch (e: HttpException) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.ExceptionString(e)
            )
        } catch (e: Throwable) {
            Resource.Error<Resource<List<Book>>>(
                uiText = UiText.ExceptionString(e)
            )
        }


    }
}