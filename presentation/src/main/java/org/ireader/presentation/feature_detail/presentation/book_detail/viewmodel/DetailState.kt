package org.ireader.presentation.feature_detail.presentation.book_detail.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.core.utils.UiText
import org.ireader.core_api.source.Source
import org.ireader.domain.models.entities.Book
import javax.inject.Inject


open class DetailStateImpl @Inject constructor() : DetailState {
    override var source by mutableStateOf<Source?>(null)
    override var book by mutableStateOf<Book?>(null)
    override var inLibrary by mutableStateOf<Boolean>(false)
    override var detailIsLocalLoading by mutableStateOf<Boolean>(false)
    override var detailIsRemoteLoading by mutableStateOf<Boolean>(false)
    override var detailIsLocalLoaded by mutableStateOf<Boolean>(false)
    override var detailError: UiText by mutableStateOf<UiText>(UiText.DynamicString(""))
    override var detailIsRemoteLoaded by mutableStateOf<Boolean>(false)

}


interface DetailState {
    var source: Source?
    var book: Book?
    var inLibrary: Boolean
    var detailIsLocalLoading: Boolean
    var detailIsRemoteLoading: Boolean
    var detailIsLocalLoaded: Boolean
    var detailError: UiText
    var detailIsRemoteLoaded: Boolean
}

