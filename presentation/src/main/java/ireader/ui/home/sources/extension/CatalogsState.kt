package ireader.ui.home.sources.extension

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote
import ireader.core.os.InstallStep
import org.koin.core.annotation.Single

interface CatalogsState {
    val allCatalogs: List<CatalogLocal>
    val pinnedCatalogs: List<CatalogLocal>
    val unpinnedCatalogs: List<CatalogLocal>

    val remoteCatalogs: List<CatalogRemote>
    val languageChoices: List<LanguageChoice>
    var selectedLanguage: LanguageChoice
    val installSteps: Map<String, InstallStep>
    val isRefreshing: Boolean
    //  val userSources: List<SourceUiModel>
    var searchQuery: String?
    var currentPagerPage: Int
}

fun CatalogsState(): CatalogsState {
    return CatalogsStateImpl()
}

    @Single
class CatalogsStateImpl : CatalogsState {

    override var currentPagerPage by mutableStateOf(0)

    override var allCatalogs by mutableStateOf(emptyList<CatalogLocal>())
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
