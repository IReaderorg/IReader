package org.ireader.domain.view_models.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.domain.feature_services.io.HistoryWithRelations
import javax.inject.Inject

interface HistoryState {
    var searchMode: Boolean
    var searchQuery: String
    var history: List<HistoryWithRelations>


}

open class HistoryStateImpl @Inject constructor() : HistoryState {
    override var searchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
    override var history: List<HistoryWithRelations> by mutableStateOf(emptyList())


}
