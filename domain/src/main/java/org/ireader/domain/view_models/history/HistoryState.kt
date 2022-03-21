package org.ireader.domain.view_models.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import javax.inject.Inject

interface HistoryState {
    var searchMode: Boolean
    var searchQuery: String


}

open class HistoryStateImpl @Inject constructor() : HistoryState {
    override var searchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")


}
