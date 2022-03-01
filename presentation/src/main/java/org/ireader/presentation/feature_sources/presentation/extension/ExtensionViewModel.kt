package org.ireader.presentation.feature_sources.presentation.extension

import androidx.compose.runtime.snapshotFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiEvent
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.catalog.CatalogInterceptors
import org.ireader.domain.catalog.model.InstallStep
import org.ireader.domain.models.entities.Catalog
import org.ireader.domain.models.entities.CatalogInstalled
import org.ireader.domain.models.entities.CatalogLocal
import org.ireader.domain.models.entities.CatalogRemote
import javax.inject.Inject


@HiltViewModel
class ExtensionViewModel @Inject constructor(
    private val state: CatalogsStateImpl,
    private val catalogInterceptors: CatalogInterceptors,
) : BaseViewModel(), CatalogsState by state {

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        scope.launch {
            catalogInterceptors.getCatalogsByType.subscribe(excludeRemoteInstalled = true)
                .collect { (pinned, unpinned, remote) ->
                    state.allPinnedCatalogs = pinned
                    state.allUnpinnedCatalogs = unpinned
                    state.allRemoteCatalogs = remote

                    state.languageChoices = getLanguageChoices(remote, pinned + unpinned)
                }
        }

        // Update catalogs whenever the query changes or there's a new update from the backend
        snapshotFlow { state.allPinnedCatalogs.filteredByQuery(searchQuery) }
            .onEach { state.pinnedCatalogs = it }
            .launchIn(scope)
        snapshotFlow { state.allUnpinnedCatalogs.filteredByQuery(searchQuery) }
            .onEach { state.unpinnedCatalogs = it }
            .launchIn(scope)
        snapshotFlow {
            state.allRemoteCatalogs.filteredByQuery(searchQuery).filteredByChoice(selectedLanguage)
        }
            .onEach { state.remoteCatalogs = it }
            .launchIn(scope)
    }

    fun installCatalog(catalog: Catalog) {
        scope.launch {
            val isUpdate = catalog is CatalogInstalled
            val (pkgName, flow) = if (isUpdate) {
                catalog as CatalogInstalled
                catalog.pkgName to catalogInterceptors.updateCatalog.await(catalog)
            } else {
                catalog as CatalogRemote
                catalog.pkgName to catalogInterceptors.installCatalog.await(catalog)
            }
            flow.collect { step ->
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
            catalogInterceptors.togglePinnedCatalog.await(catalog)
        }
    }

    fun uninstallCatalog(catalog: Catalog) {
        scope.launch {
            catalogInterceptors.uninstallCatalog.await(catalog as CatalogInstalled)
        }
    }

    fun refreshCatalogs() {
        scope.launch(Dispatchers.IO) {
            state.isRefreshing = true
            catalogInterceptors.syncRemoteCatalogs.await(true)
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

}


