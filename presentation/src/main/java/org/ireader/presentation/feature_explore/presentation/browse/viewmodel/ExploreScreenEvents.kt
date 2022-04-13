package org.ireader.presentation.feature_explore.presentation.browse.viewmodel

import androidx.annotation.Keep
import org.ireader.domain.models.DisplayMode

@Keep
sealed class ExploreScreenEvents {
    @Keep
    data class OnLayoutTypeChnage(val layoutType: DisplayMode) : ExploreScreenEvents()
    @Keep
    data class ToggleSearchMode(val inSearchMode: Boolean) : ExploreScreenEvents()
    @Keep
    data class OnQueryChange(val query: String) : ExploreScreenEvents()
}
