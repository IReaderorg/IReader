package org.ireader.sources.extension

import org.ireader.common_models.entities.CatalogLocal
import org.ireader.core_api.source.CatalogSource

data class ExtensionScreenState(
    val sources: List<CatalogSource> = emptyList(),
    val communitySources: List<CatalogSource> = emptyList(),
    val catalogLocal: List<CatalogLocal> = emptyList(),
)
