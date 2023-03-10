package ireader.domain.services.downloaderService

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.SavedDownload

interface DownloadServiceState {
    var downloads: List<SavedDownload>
    var isEnable: Boolean
}


class DownloadServiceStateImpl() : DownloadServiceState {
    companion object {
        const val DOWNLOADER_SERVICE_NAME = "DOWNLOAD_SERVICE"
        const val DOWNLOADER_Chapters_IDS = "chapterIds"
        const val DOWNLOADER_MODE = "downloader_mode"
        const val DOWNLOADER_BOOKS_IDS = "booksIds"
    }

    override var downloads: List<SavedDownload> by mutableStateOf(emptyList())
    override var isEnable: Boolean by mutableStateOf(false)
}
