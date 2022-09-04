package org.ireader.sources.extension

import org.ireader.common_models.entities.CatalogLocal
import org.ireader.core_api.source.CatalogSource

data class ExtensionScreenState(
    val sources: List<org.ireader.core_api.source.CatalogSource> = emptyList(),
    val communitySources: List<org.ireader.core_api.source.CatalogSource> = emptyList(),
    val catalogLocal: List<CatalogLocal> = emptyList(),
)
