package org.ireader.presentation.feature_sources.presentation.extension

import org.ireader.domain.models.entities.CatalogLocal
import tachiyomi.source.CatalogSource

data class ExtensionScreenState(
    val sources: List<CatalogSource> = emptyList(),
    val communitySources: List<CatalogSource> = emptyList(),
    val catalogLocal: List<CatalogLocal> = emptyList(),
)