package org.ireader.presentation.feature_history.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.domain.feature_services.io.HistoryWithRelations
import javax.inject.Inject

interface HistoryState {
    val isLoading: Boolean
    val isEmpty: Boolean

    var searchMode: Boolean
    var searchQuery: String
    var history: Map<String, List<HistoryWithRelations>>


}

open class HistoryStateImpl @Inject constructor() : HistoryState {
    override var isLoading: Boolean by mutableStateOf(false)
    override val isEmpty: Boolean by derivedStateOf { history.isEmpty() }
    override var searchMode by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
    override var history: Map<String, List<HistoryWithRelations>> by mutableStateOf(emptyMap())


}
