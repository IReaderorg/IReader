package org.ireader.domain.use_cases.download.delete

import coil.network.HttpException
import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Book
import org.ireader.domain.repository.DownloadRepository
import org.ireader.domain.utils.Resource
import java.io.IOException
import javax.inject.Inject

class DeleteSavedDownloadByBookId @Inject constructor(private val downloadRepository: DownloadRepository) {
    suspend operator fun invoke(bookId: Long) {
        try {
            downloadRepository.deleteSavedDownloadByBookId(bookId)
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