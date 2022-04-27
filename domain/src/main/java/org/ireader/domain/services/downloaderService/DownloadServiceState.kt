package org.ireader.domain.services.downloaderService

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.common_models.entities.SavedDownload
import javax.inject.Inject
import javax.inject.Singleton

interface DownloadServiceState {
    var downloads: List<SavedDownload>
    var isEnable: Boolean
    
}

@Singleton
class DownloadServiceStateImpl @Inject constructor() : DownloadServiceState {
    override var downloads: List<SavedDownload> by mutableStateOf(emptyList())
    override var isEnable: Boolean by mutableStateOf(false)
   
}
