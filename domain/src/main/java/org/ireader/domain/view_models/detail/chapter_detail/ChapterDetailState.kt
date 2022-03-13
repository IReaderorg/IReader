package org.ireader.domain.view_models.detail.chapter_detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import javax.inject.Inject


open class ChapterDetailStateImpl @Inject constructor() : ChapterDetailState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var stateChapters by mutableStateOf<List<Chapter>>(emptyList())
    override var book by mutableStateOf<Book?>(null)
    override var isAsc by mutableStateOf<Boolean>(true)
    override var error by mutableStateOf<UiText?>(null)
    override var chapterOrderType by mutableStateOf<OrderType>(OrderType.Ascending)
    override var localChapters by mutableStateOf<List<Chapter>>(emptyList())
    override var reverse by mutableStateOf<Boolean>(false)
    override var currentScrollPosition by mutableStateOf<Int>(0)
    override var query by mutableStateOf<String>("")
}


interface ChapterDetailState {
    val isLoading: Boolean
    val stateChapters: List<Chapter>
    var book: Book?
    var isAsc: Boolean
    val error: UiText?
    val chapterOrderType: OrderType
    var localChapters: List<Chapter>
    val reverse: Boolean
    val currentScrollPosition: Int
    var query: String
}