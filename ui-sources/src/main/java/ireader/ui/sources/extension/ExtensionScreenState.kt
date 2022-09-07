package ireader.ui.sources.extension

import ireader.common.models.entities.CatalogLocal

data class ExtensionScreenState(
    val sources: List<ireader.core.api.source.CatalogSource> = emptyList(),
    val communitySources: List<ireader.core.api.source.CatalogSource> = emptyList(),
    val catalogLocal: List<CatalogLocal> = emptyList(),
)
