package org.ireader.sources.extension

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.core_catalogs.model.InstallStep
import javax.inject.Inject

interface CatalogsState {
    val pinnedCatalogs: List<CatalogLocal>
    val unpinnedCatalogs: List<CatalogLocal>
    val remoteCatalogs: List<CatalogRemote>
    val languageChoices: List<LanguageChoice>
    var selectedLanguage: LanguageChoice
    val installSteps: Map<String, InstallStep>
    val isRefreshing: Boolean
    var searchQuery: String?
    var mappedCatalogs: State<Map<String, List<CatalogLocal>>>
}

fun CatalogsState(): CatalogsState {
    return CatalogsStateImpl()
}

class CatalogsStateImpl @Inject constructor() : CatalogsState {
    override var pinnedCatalogs by mutableStateOf(emptyList<CatalogLocal>())
    override var unpinnedCatalogs by mutableStateOf(emptyList<CatalogLocal>())
    override var remoteCatalogs by mutableStateOf(emptyList<CatalogRemote>())
    override var languageChoices by mutableStateOf(emptyList<LanguageChoice>())
    override var selectedLanguage by mutableStateOf<LanguageChoice>(LanguageChoice.All)
    override var installSteps by mutableStateOf(emptyMap<String, InstallStep>())
    override var isRefreshing by mutableStateOf(false)
    override var searchQuery by mutableStateOf<String?>(null)
    override var mappedCatalogs: State<Map<String, List<CatalogLocal>>> = derivedStateOf { unpinnedCatalogs.groupBy { it.source.lang } }

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
