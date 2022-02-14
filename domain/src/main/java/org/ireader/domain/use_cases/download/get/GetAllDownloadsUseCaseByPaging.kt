package org.ireader.domain.use_cases.download.get

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.ireader.core.utils.Constants
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.DownloadRepository

class GetAllDownloadsUseCaseByPaging(private val downloadRepository: DownloadRepository) {
    operator fun invoke(): Flow<PagingData<SavedDownload>> {
        val config = downloadRepository.getAllDownloadsByPaging()
        return Pager(
            config = PagingConfig(pageSize = Constants.DEFAULT_PAGE_SIZE,
                maxSize = Constants.MAX_PAGE_SIZE, enablePlaceholders = true),
            pagingSourceFactory = {
                downloadRepository.getAllDownloadsByPaging()
            }
        ).flow
    }
}