package org.ireader.sources.extension

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.setValue
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.common_models.entities.SourceState
import org.ireader.core_api.os.InstallStep
import org.ireader.sources.extension.SourceKeys.AVAILABLE
import org.ireader.sources.extension.SourceKeys.INSTALLED_KEY
import org.ireader.sources.extension.SourceKeys.LAST_USED_KEY
import org.ireader.sources.extension.SourceKeys.PINNED_KEY
import org.ireader.sources.extension.composables.SourceUiModel
import javax.inject.Inject
import javax.inject.Singleton

interface CatalogsState {
    val pinnedCatalogs: List<CatalogLocal>
    val unpinnedCatalogs: List<CatalogLocal>
    var lastReadCatalog: Long?
    val remoteCatalogs: List<CatalogRemote>
    val languageChoices: List<LanguageChoice>
    var selectedLanguage: LanguageChoice
    val installSteps: Map<String, InstallStep>
    val isRefreshing: Boolean
    val userSources: List<SourceUiModel>
    val remoteSources: List<SourceUiModel>
    var searchQuery: String?
    var currentPagerPage: Int
}

fun CatalogsState(): CatalogsState {
    return CatalogsStateImpl()
}

@Singleton
class CatalogsStateImpl @Inject constructor() : CatalogsState {

    override var currentPagerPage by mutableStateOf(0)

    override var pinnedCatalogs by mutableStateOf(emptyList<CatalogLocal>())
    override var unpinnedCatalogs by mutableStateOf(emptyList<CatalogLocal>())
    override var lastReadCatalog: Long? by mutableStateOf(null)
    override var remoteCatalogs by mutableStateOf(emptyList<CatalogRemote>())
    override var languageChoices by mutableStateOf(emptyList<LanguageChoice>())
    override val userSources: List<SourceUiModel> by derivedStateOf {
        val list = mutableListOf<SourceUiModel>()
        if (lastReadCatalog != null) {

           (pinnedCatalogs + unpinnedCatalogs).firstOrNull {
                it.sourceId == lastReadCatalog
            }?.let { c ->
                list.addAll(
                    listOf<SourceUiModel>(
                        SourceUiModel.Header(LAST_USED_KEY),
                        SourceUiModel.Item(c,SourceState.LastUsed)

                    )
                )
            }

        }

        if (pinnedCatalogs.isNotEmpty()) {
            list.addAll(
                listOf<SourceUiModel>(
                    SourceUiModel.Header(PINNED_KEY),
                    *pinnedCatalogs.map { source ->
                        SourceUiModel.Item(source,SourceState.Pinned)
                    }.toTypedArray()
                )
            )
        }
        if (unpinnedCatalogs.isNotEmpty()) {
            list.addAll(unpinnedCatalogs.groupBy {
                it.source?.lang ?: "others"
            }.flatMap {
                listOf<SourceUiModel>(
                    SourceUiModel.Header(it.key),
                    *it.value.map { source ->
                        SourceUiModel.Item(source,SourceState.UnPinned)
                    }.toTypedArray()
                )
            })
        }
        list
    }
    override val remoteSources: List<SourceUiModel> by derivedStateOf {
        val allCatalogs = pinnedCatalogs + unpinnedCatalogs
        val list = mutableListOf<SourceUiModel>()
        if (allCatalogs.isNotEmpty()) {
            list.addAll(
                listOf<SourceUiModel>(
                    SourceUiModel.Header(INSTALLED_KEY),
                    *allCatalogs.map { source ->
                        SourceUiModel.Item(source,SourceState.Installed)
                    }.toTypedArray()
                )
            )
        }
        if (remoteCatalogs.isNotEmpty()) {
            list.addAll(
                listOf<SourceUiModel>(
                    SourceUiModel.Header(AVAILABLE),
                    *remoteCatalogs.map { source ->
                        SourceUiModel.Item(source,SourceState.Remote)
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
