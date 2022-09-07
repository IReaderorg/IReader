package ireader.domain.services.downloaderService

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.common.models.entities.SavedDownload
import org.koin.core.annotation.Single

import javax.inject.Singleton

interface DownloadServiceState {
    var downloads: List<SavedDownload>
    var isEnable: Boolean
}

@Single
class DownloadServiceStateImpl() : DownloadServiceState {
    override var downloads: List<SavedDownload> by mutableStateOf(emptyList())
    override var isEnable: Boolean by mutableStateOf(false)
}
