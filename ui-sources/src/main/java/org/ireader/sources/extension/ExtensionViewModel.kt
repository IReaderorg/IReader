package org.ireader.sources.extension

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.ireader.common_extensions.UiText
import org.ireader.common_extensions.launchIO
import org.ireader.common_models.entities.Catalog
import org.ireader.common_models.entities.CatalogInstalled
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.common_models.entities.CatalogRemote
import org.ireader.core_catalogs.interactor.GetCatalogsByType
import org.ireader.core_catalogs.interactor.InstallCatalog
import org.ireader.core_catalogs.interactor.SyncRemoteCatalogs
import org.ireader.core_catalogs.interactor.TogglePinnedCatalog
import org.ireader.core_catalogs.interactor.UninstallCatalog
import org.ireader.core_catalogs.interactor.UpdateCatalog
import org.ireader.core_catalogs.model.InstallStep
import org.ireader.core_ui.exceptionHandler
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.use_cases.remote.key.RemoteKeyUseCase
import javax.inject.Inject

@HiltViewModel
class ExtensionViewModel @Inject constructor(
    private val state: CatalogsStateImpl,
    private val getCatalogsByType: GetCatalogsByType,
    private val updateCatalog: UpdateCatalog,
    private val installCatalog: InstallCatalog,
    private val uninstallCatalog: UninstallCatalog,
    private val togglePinnedCatalog: TogglePinnedCatalog,
    private val syncRemoteCatalogs: SyncRemoteCatalogs,
    private val remoteKeyUseCase: RemoteKeyUseCase,
) : BaseViewModel(), CatalogsState by state {

    var getCatalogJob: Job? = null

    init {
        scope.launch {
            getCatalogsByType.subscribe(excludeRemoteInstalled = true)
                .collect { (pinned, unpinned, remote) ->
                    state.allPinnedCatalogs = pinned
                    state.allUnpinnedCatalogs = unpinned
                    state.allRemoteCatalogs = remote

                    state.languageChoices = getLanguageChoices(remote, pinned + unpinned)
                }
        }

        // Update catalogs whenever the query changes or there's a new update from the backend
        viewModelScope.launch {
            snapshotFlow { state.allPinnedCatalogs.filteredByQuery(searchQuery) }
                .collect { state.pinnedCatalogs = it }
        }
        viewModelScope.launch {
            snapshotFlow { state.allUnpinnedCatalogs.filteredByQuery(searchQuery) }
                .collect { state.unpinnedCatalogs = it }
        }
        viewModelScope.launch {
            snapshotFlow {
                state.allRemoteCatalogs.filteredByQuery(searchQuery)
                    .filteredByChoice(selectedLanguage)
            }
                .collect { state.remoteCatalogs = it }
        }
    }

    fun installCatalog(catalog: Catalog) {
        scope.launch {
            val isUpdate = catalog is CatalogInstalled
            val (pkgName, flow) = if (isUpdate) {
                catalog as CatalogInstalled
                catalog.pkgName to updateCatalog.await(catalog) { error ->
                    showSnackBar(exceptionHandler(error))
                }
            } else {
                catalog as CatalogRemote
                catalog.pkgName to installCatalog.await(catalog) { error ->
                    showSnackBar(exceptionHandler(error))
                }
            }
            flow.collect { step ->
                if (step == InstallStep.Error) {
                    showSnackBar(UiText.DynamicString(step.name))
                }
                state.installSteps = if (step != InstallStep.Completed) {
                    installSteps + (pkgName to step)
                } else {
                    installSteps - pkgName
                }
            }
        }
    }

    fun togglePinnedCatalog(catalog: CatalogLocal) {
        scope.launch {
            togglePinnedCatalog.await(catalog)
        }
    }

    fun uninstallCatalog(catalog: Catalog) {
        scope.launch {
            if (catalog is CatalogInstalled) {
                uninstallCatalog.await(catalog) { error ->
                    showSnackBar(exceptionHandler(error))
                }
            }
        }
    }

    fun refreshCatalogs() {
        scope.launch(Dispatchers.IO) {
            state.isRefreshing = true
            syncRemoteCatalogs.await(true, onError = { error ->
                showSnackBar(exceptionHandler(error))
            })
            state.isRefreshing = false
        }
    }

    private fun getLanguageChoices(
        remote: List<CatalogRemote>,
        local: List<CatalogLocal>,
    ): List<LanguageChoice> {
        val knownLanguages = mutableListOf<LanguageChoice.One>()
        val unknownLanguages = mutableListOf<Language>()

        val languageComparators = UserLanguagesComparator()
            .then(InstalledLanguagesComparator(local))
            .thenBy { it.code }

        remote.asSequence()
            .map { Language(it.lang) }
            .distinct()
            .sortedWith(languageComparators)
            .forEach { code ->
                if (code.toEmoji() != null) {
                    knownLanguages.add(LanguageChoice.One(code))
                } else {
                    unknownLanguages.add(code)
                }
            }

        val languages = mutableListOf<LanguageChoice>()
        languages.add(LanguageChoice.All)
        languages.addAll(knownLanguages)
        if (unknownLanguages.isNotEmpty()) {
            languages.add(LanguageChoice.Others(unknownLanguages))
        }

        return languages
    }

    private fun <T : Catalog> List<T>.filteredByQuery(query: String?): List<T> {
        return if (query == null) {
            this
        } else {
            filter { it.name.contains(query, true) }
        }
    }

    private fun List<CatalogRemote>.filteredByChoice(choice: LanguageChoice): List<CatalogRemote> {
        return when (choice) {
            LanguageChoice.All -> this
            is LanguageChoice.One -> filter { choice.language.code == it.lang }
            is LanguageChoice.Others -> {
                val codes = choice.languages.map { it.code }
                filter { it.lang in codes }
            }
        }
    }

    fun clearExploreMode() {
        viewModelScope.launchIO {
            remoteKeyUseCase.clearExploreMode()
        }
    }
}