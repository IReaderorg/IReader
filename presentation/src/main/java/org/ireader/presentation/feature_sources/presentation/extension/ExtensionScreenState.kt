package org.ireader.presentation.feature_sources.presentation.extension

import androidx.annotation.Keep
import org.ireader.core_api.source.CatalogSource
import org.ireader.domain.models.entities.CatalogLocal

@Keep
data class ExtensionScreenState(
    val sources: List<CatalogSource> = emptyList(),
    val communitySources: List<CatalogSource> = emptyList(),
    val catalogLocal: List<CatalogLocal> = emptyList(),
)