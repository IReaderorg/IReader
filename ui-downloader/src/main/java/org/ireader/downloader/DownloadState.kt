package org.ireader.downloader

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.ireader.common_models.entities.SavedDownloadWithInfo
import javax.inject.Inject


interface DownloadState {
    var downloads: List<SavedDownloadWithInfo>
    var isMenuExpanded: Boolean
    var selection: SnapshotStateList<Long>
    val hasSelection: Boolean
}

open class DownloadStateImpl @Inject constructor() : DownloadState {
    override var downloads: List<SavedDownloadWithInfo> by mutableStateOf(emptyList())
    override var isMenuExpanded: Boolean by mutableStateOf(false)
    override var selection: SnapshotStateList<Long> = mutableStateListOf()
    override val hasSelection: Boolean by derivedStateOf { selection.isNotEmpty() }
}




