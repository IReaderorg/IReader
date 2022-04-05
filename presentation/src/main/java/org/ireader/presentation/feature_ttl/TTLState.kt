//package org.ireader.presentation.feature_ttl
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import org.ireader.core.utils.UiText
//import org.ireader.domain.R
//import org.ireader.domain.models.entities.Book
//import org.ireader.domain.models.entities.Chapter
//import javax.inject.Inject
//
//interface TTLState {
//    var book: Book?
//    var isPlaying: Boolean
//    val chapters : List<Chapter>
//    val stateChapter : Chapter?
//    val isLoading : Boolean
//    val error : UiText
//}
//
//open class TTLStateImpl @Inject constructor() : TTLState {
//    override var book: Book? by mutableStateOf<Book?>(null)
//    override var stateChapter: Chapter? by mutableStateOf<Chapter?>(null)
//    override var chapters by mutableStateOf<List<Chapter>>(emptyList())
//    override var isPlaying by mutableStateOf<Boolean>(false)
//    override var isLoading by mutableStateOf<Boolean>(false)
//    override var error by mutableStateOf<UiText>(UiText.StringResource(R.string.no_error))
//}
