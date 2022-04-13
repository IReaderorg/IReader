package org.ireader.presentation.feature_sources.presentation.extension

import androidx.annotation.Keep
import org.ireader.domain.models.entities.CatalogLocal
import tachiyomi.source.CatalogSource

@Keep
data class ExtensionScreenState(
    val sources: List<CatalogSource> = emptyList(),
    val communitySources: List<CatalogSource> = emptyList(),
    val catalogLocal: List<CatalogLocal> = emptyList(),
)