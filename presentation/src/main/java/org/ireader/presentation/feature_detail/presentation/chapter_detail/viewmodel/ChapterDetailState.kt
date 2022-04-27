package org.ireader.presentation.feature_detail.presentation.chapter_detail.viewmodel

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.core.utils.UiText
import javax.inject.Inject


open class ChapterDetailStateImpl @Inject constructor() : ChapterDetailState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var chapters by mutableStateOf<List<Chapter>>(emptyList())
    override var book by mutableStateOf<Book?>(null)
    override var isAsc by mutableStateOf<Boolean>(true)
    override var error by mutableStateOf<UiText?>(null)
    override var chapterOrderType by mutableStateOf<OrderType>(OrderType.Ascending)
    override var reverse by mutableStateOf<Boolean>(false)
    override var currentScrollPosition by mutableStateOf<Int>(0)
    override var query by mutableStateOf<String>("")
    override var lastRead by mutableStateOf<Long?>(null)
    override val isEmpty: Boolean by derivedStateOf { chapters.isEmpty() }
    override var selection: SnapshotStateList<Long> = mutableStateListOf()
    override val hasSelection: Boolean by derivedStateOf { selection.isNotEmpty() }
}


interface ChapterDetailState {
    val isLoading: Boolean
    var chapters: List<Chapter>
    var book: Book?
    var isAsc: Boolean
    val error: UiText?
    val chapterOrderType: OrderType
    val reverse: Boolean
    val currentScrollPosition: Int
    var query: String
    var lastRead: Long?
    val isEmpty: Boolean
    var selection: SnapshotStateList<Long>
    val hasSelection: Boolean
}