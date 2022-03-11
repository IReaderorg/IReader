package org.ireader.presentation.feature_sources.presentation.extension

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import org.ireader.domain.catalog.model.InstallStep
import org.ireader.domain.models.entities.CatalogLocal
import org.ireader.domain.models.entities.CatalogRemote


interface CatalogsState {
    val pinnedCatalogs: List<CatalogLocal>
    val unpinnedCatalogs: List<CatalogLocal>
    val remoteCatalogs: List<CatalogRemote>
    val languageChoices: List<LanguageChoice>
    var selectedLanguage: LanguageChoice
    val installSteps: Map<String, InstallStep>
    val isRefreshing: Boolean
    var searchQuery: String?
}

fun CatalogsState(): CatalogsState {
    return CatalogsStateImpl()
}

class CatalogsStateImpl : CatalogsState {
    override var pinnedCatalogs by mutableStateOf(emptyList<CatalogLocal>())
    override var unpinnedCatalogs by mutableStateOf(emptyList<CatalogLocal>())
    override var remoteCatalogs by mutableStateOf(emptyList<CatalogRemote>())
    override var languageChoices by mutableStateOf(emptyList<LanguageChoice>())
    override var selectedLanguage by mutableStateOf<LanguageChoice>(LanguageChoice.All)
    override var installSteps by mutableStateOf(emptyMap<String, InstallStep>())
    override var isRefreshing by mutableStateOf(false)
    override var searchQuery by mutableStateOf<String?>(null)

    var allPinnedCatalogs by mutableStateOf(
        emptyList<CatalogLocal>(),
        referentialEqualityPolicy()
    )
    var allUnpinnedCatalogs by mutableStateOf(
        emptyList<CatalogLocal>(),
        referentialEqualityPolicy()
    )
    var allRemoteCatalogs by mutableStateOf(
        emptyList<CatalogRemote>(),
        referentialEqualityPolicy()
    )
}
