package org.ireader.presentation.feature_detail.presentation.book_detail.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.domain.models.entities.Chapter
import javax.inject.Inject

open class ChapterStateImpl @Inject constructor() : ChapterState {
    override var chapterIsLoading by mutableStateOf<Boolean>(false)
    override var chapters by mutableStateOf<List<Chapter>>(emptyList())
}


interface ChapterState {
    var chapterIsLoading: Boolean
    var chapters: List<Chapter>
}