package org.ireader.domain.view_models.explore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.ireader.core.utils.UiText
import org.ireader.domain.models.LayoutType
import tachiyomi.source.CatalogSource
import tachiyomi.source.model.Filter
import tachiyomi.source.model.Listing
import javax.inject.Inject

interface ExploreState {
    val isLoading: Boolean
    val error: UiText?
    val layout: LayoutType
    val isSearchModeEnable: Boolean
    val searchQuery: String
    val exploreType: Listing?
    val source: CatalogSource?
    val isFilterEnable: Boolean
    var topMenuEnable: Boolean
    var sort: Listing?
    var modifiedFilter: List<Filter<*>>
}

open class ExploreStateImpl @Inject constructor() : ExploreState {
    override var isLoading by mutableStateOf<Boolean>(false)
    override var error by mutableStateOf<UiText?>(null)
    override var layout by mutableStateOf<LayoutType>(LayoutType.GridLayout)
    override var isSearchModeEnable by mutableStateOf<Boolean>(false)
    override var searchQuery by mutableStateOf<String>("")
    override var source by mutableStateOf<CatalogSource?>(null)
    override var isFilterEnable by mutableStateOf<Boolean>(false)
    override var topMenuEnable: Boolean by mutableStateOf<Boolean>(false)
    override var sort: Listing? by mutableStateOf(null)
    override var exploreType by mutableStateOf<Listing?>(null)
    override var modifiedFilter by mutableStateOf(emptyList<Filter<*>>())
}
