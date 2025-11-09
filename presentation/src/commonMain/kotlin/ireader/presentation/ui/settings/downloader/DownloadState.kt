package ireader.presentation.ui.settings.downloader

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.domain.models.entities.SavedDownloadWithInfo


enum class DownloadTab {
    ACTIVE, COMPLETED
}

interface DownloadState {
    var downloads: List<SavedDownloadWithInfo>
    var isMenuExpanded: Boolean
    var selection: SnapshotStateList<Long>
    val hasSelection: Boolean
    var selectedTab: DownloadTab
}

open class DownloadStateImpl : DownloadState {
    override var downloads: List<SavedDownloadWithInfo> by mutableStateOf(emptyList())
    override var isMenuExpanded: Boolean by mutableStateOf(false)
    override var selection: SnapshotStateList<Long> = mutableStateListOf()
    override val hasSelection: Boolean by derivedStateOf { selection.isNotEmpty() }
    override var selectedTab: DownloadTab by mutableStateOf(DownloadTab.ACTIVE)
}
