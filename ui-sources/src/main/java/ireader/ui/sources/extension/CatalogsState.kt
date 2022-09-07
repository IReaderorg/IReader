package ireader.ui.sources.extension

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import ireader.common.models.entities.CatalogLocal
import ireader.common.models.entities.CatalogRemote
import ireader.common.models.entities.SourceState
import ireader.core.api.os.InstallStep
import ireader.ui.sources.extension.SourceKeys.AVAILABLE
import ireader.ui.sources.extension.SourceKeys.INSTALLED_KEY
import ireader.ui.sources.extension.composables.SourceUiModel
import org.koin.core.annotation.Single

interface CatalogsState {
    val pinnedCatalogs: List<CatalogLocal>
    val unpinnedCatalogs: List<CatalogLocal>

    val remoteCatalogs: List<CatalogRemote>
    val languageChoices: List<LanguageChoice>
    var selectedLanguage: LanguageChoice
    val installSteps: Map<String, InstallStep>
    val isRefreshing: Boolean
    //  val userSources: List<SourceUiModel>
    val remoteSources: List<SourceUiModel>
    var searchQuery: String?
    var currentPagerPage: Int
}

fun CatalogsState(): CatalogsState {
    return CatalogsStateImpl()
}

    @Single
class CatalogsStateImpl : CatalogsState {

    override var currentPagerPage by mutableStateOf(0)

    override var pinnedCatalogs by mutableStateOf(emptyList<CatalogLocal>())
    override var unpinnedCatalogs by mutableStateOf(emptyList<CatalogLocal>())

    override var remoteCatalogs by mutableStateOf(emptyList<CatalogRemote>())
    override var languageChoices by mutableStateOf(emptyList<LanguageChoice>())
//    override val userSources: List<SourceUiModel> by derivedStateOf {
//        val list = mutableListOf<SourceUiModel>()
//        if (lastReadCatalog != null) {
//
//           (pinnedCatalogs + unpinnedCatalogs).firstOrNull {
//                it.sourceId == lastReadCatalog
//            }?.let { c ->
//                list.addAll(
//                    listOf<SourceUiModel>(
//                        SourceUiModel.Header(LAST_USED_KEY),
//                        SourceUiModel.Item(c,SourceState.LastUsed)
//
//                    )
//                )
//            }
//
//        }
//
//        if (pinnedCatalogs.isNotEmpty()) {
//            list.addAll(
//                listOf<SourceUiModel>(
//                    SourceUiModel.Header(PINNED_KEY),
//                    *pinnedCatalogs.map { source ->
//                        SourceUiModel.Item(source,SourceState.Pinned)
//                    }.toTypedArray()
//                )
//            )
//        }
//        if (unpinnedCatalogs.isNotEmpty()) {
//            list.addAll(unpinnedCatalogs.groupBy {
//                it.source?.lang ?: "others"
//            }.flatMap {
//                listOf<SourceUiModel>(
//                    SourceUiModel.Header(it.key),
//                    *it.value.map { source ->
//                        SourceUiModel.Item(source,SourceState.UnPinned)
//                    }.toTypedArray()
//                )
//            })
//        }
//        list
//    }
    override val remoteSources: List<SourceUiModel> by derivedStateOf {
        val allCatalogs = pinnedCatalogs + unpinnedCatalogs
        val list = mutableListOf<SourceUiModel>()
        if (allCatalogs.isNotEmpty()) {
            list.addAll(
                listOf<SourceUiModel>(
                    SourceUiModel.Header(INSTALLED_KEY),
                    *allCatalogs.map { source ->
                        SourceUiModel.Item(source, SourceState.Installed)
                    }.toTypedArray()
                )
            )
        }
        if (remoteCatalogs.isNotEmpty()) {
            list.addAll(
                listOf<SourceUiModel>(
                    SourceUiModel.Header(AVAILABLE),
                    *remoteCatalogs.map { source ->
                        SourceUiModel.Item(source, SourceState.Remote)
                    }.toTypedArray()
                )
            )
        }
        list
    }

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
