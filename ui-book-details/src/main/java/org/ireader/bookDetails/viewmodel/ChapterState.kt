package org.ireader.bookDetails.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.ireader.common_models.entities.Chapter
import javax.inject.Inject

open class ChapterStateImpl @Inject constructor() : ChapterState {
    override var chapterIsLoading by mutableStateOf<Boolean>(false)
    override val haveBeenRead by derivedStateOf { chapters.any { it.read } }
    override var chapters by mutableStateOf<List<Chapter>>(emptyList())
    override val isEmpty: Boolean by derivedStateOf { chapters.isEmpty() }
    override var selection: SnapshotStateList<Long> = mutableStateListOf()

    override var searchMode by mutableStateOf<Boolean>(false)
    override val hasSelection: Boolean by derivedStateOf { selection.isNotEmpty() }
    override var query by mutableStateOf<String?>(null)
    override var lastRead by mutableStateOf<Long?>(null)
}

interface ChapterState {
    var chapterIsLoading: Boolean
    var chapters: List<Chapter>
    val haveBeenRead: Boolean
    val isEmpty: Boolean
    var selection: SnapshotStateList<Long>
    var lastRead: Long?
    var searchMode: Boolean
    var query: String?
    val hasSelection: Boolean
}
