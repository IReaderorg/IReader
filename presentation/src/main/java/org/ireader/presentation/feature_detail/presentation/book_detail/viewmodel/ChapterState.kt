package org.ireader.presentation.feature_detail.presentation.book_detail.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Chapter
import javax.inject.Inject

open class ChapterStateImpl @Inject constructor() : ChapterState {
    override var chapterIsLoaded by mutableStateOf<Boolean>(false)
    override var chapterLoadingProgress by mutableStateOf<Float>(0f)
    override var chapterIsLoading by mutableStateOf<Boolean>(false)
    override var chapterError by mutableStateOf<UiText?>(null)
    override var chapters by mutableStateOf<List<Chapter>>(emptyList())


}


interface ChapterState {
    var chapterIsLoading: Boolean
    var chapterError: UiText?
    var chapters: List<Chapter>
    var chapterIsLoaded: Boolean
    var chapterLoadingProgress: Float
}