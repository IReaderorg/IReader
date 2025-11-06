package ireader.presentation.ui.home.sources.extension

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import ireader.core.os.InstallStep
import ireader.domain.catalogs.interactor.*
import ireader.domain.models.entities.*
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.services.StartExtensionManagerService
import ireader.domain.utils.exceptionHandler
import ireader.i18n.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


class ExtensionViewModel(
        private val state: CatalogsStateImpl,
        private val getCatalogsByType: GetCatalogsByType,
        private val updateCatalog: UpdateCatalog,
        private val installCatalog: InstallCatalog,
        private val uninstallCatalog: UninstallCatalogs,
        private val togglePinnedCatalog: TogglePinnedCatalog,
        private val syncRemoteCatalogs: SyncRemoteCatalogs,
        val uiPreferences: UiPreferences,
        val startExtensionManagerService: StartExtensionManagerService,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), CatalogsState by state {

    val incognito = uiPreferences.incognitoMode().asState()
    val lastUsedSource = uiPreferences.lastUsedSource().asState()
    val defaultRepo = uiPreferences.defaultRepository().asState()
    val autoInstaller = uiPreferences.autoCatalogUpdater().asState()
    val showLanguageFilter = uiPreferences.showLanguageFilter().asState()
    val userSources: List<SourceUiModel> by derivedStateOf {
        val filteredPinned = pinnedCatalogs.filteredByLanguageChoice(selectedUserSourceLanguage)
        val filteredUnpinned = unpinnedCatalogs.filteredByLanguageChoice(selectedUserSourceLanguage)

        val list = mutableListOf<SourceUiModel>()
        if (lastUsedSource.value != -1L) {

            (pinnedCatalogs + unpinnedCatalogs).firstOrNull {
                it.sourceId == lastUsedSource.value
            }?.let { c ->
                if (c.matchesLanguageChoice(selectedUserSourceLanguage)) {
                    list.addAll(
                            listOf<SourceUiModel>(
                                    SourceUiModel.Header(SourceKeys.LAST_USED_KEY),
                                    SourceUiModel.Item(c, SourceState.LastUsed)

                            )
                    )
                }
            }
        }

        if (filteredPinned.isNotEmpty()) {
            list.addAll(
                    listOf<SourceUiModel>(
                            SourceUiModel.Header(SourceKeys.PINNED_KEY),
                            *filteredPinned.map { source ->
                                SourceUiModel.Item(source, SourceState.Pinned)
                            }.toTypedArray()
                    )
            )
        }
        if (filteredUnpinned.isNotEmpty()) {
            list.addAll(
                    filteredUnpinned.groupBy {
                        it.source?.lang ?: "others"
                    }.flatMap {
                        listOf<SourceUiModel>(
                                SourceUiModel.Header(it.key),
                                *it.value.map { source ->
                                    SourceUiModel.Item(source, SourceState.UnPinned)
                                }.toTypedArray()
                        )
                    }
            )
        }
        list
    }

    var getCatalogJob: Job? = null

    var installerJobs: MutableMap<Long, Job> = mutableMapOf()


    init {
        scope.launch {
            getCatalogsByType.subscribe(excludeRemoteInstalled = true)
                    .onEach { (pinned, unpinned, remote) ->
                        state.allPinnedCatalogs = pinned
                        state.allUnpinnedCatalogs = unpinned
                        state.allRemoteCatalogs = remote

                        state.languageChoices = getLanguageChoices(remote, pinned + unpinned)
                    }.launchIn(scope)
        }

        // Update catalogs whenever the query changes or there's a new update from the backend

        snapshotFlow { state.allPinnedCatalogs.filteredByQuery(searchQuery) }
                .onEach { state.pinnedCatalogs = it }.launchIn(scope)

        snapshotFlow { state.allUnpinnedCatalogs.filteredByQuery(searchQuery) }
                .onEach { state.unpinnedCatalogs = it }.launchIn(scope)

        snapshotFlow {
            state.allRemoteCatalogs.filteredByQuery(searchQuery)
                    .filteredByChoice(selectedLanguage)
        }
                .onEach { state.remoteCatalogs = it }.launchIn(scope)
    }

    fun installCatalog(catalog: Catalog) {
        installerJobs.putIfAbsent(catalog.sourceId, Job())
        installerJobs[catalog.sourceId] =
                scope.launch {
                    val isUpdate = catalog is CatalogInstalled
                    val (pkgName, flow) = if (isUpdate) {
                        catalog as CatalogInstalled
                        catalog.pkgName to updateCatalog.await(catalog)
                    } else {
                        catalog as CatalogRemote
                        catalog.pkgName to installCatalog.await(catalog)
                    }
                    flow.collect { step ->
                        if (step is InstallStep.Error) {
                            showSnackBar(UiText.DynamicString(step.error))
                        }
                        state.installSteps = if (step != InstallStep.Success) {
                            if (step is InstallStep.Error) {
                                showSnackBar(UiText.DynamicString(step.error))
                            }
                            installSteps + (pkgName to step)
                        } else {
                            installSteps - pkgName
                        }
                    }
                }
    }

    fun togglePinnedCatalog(catalog: Catalog) {
        scope.launch {
            togglePinnedCatalog.await(catalog)
        }
    }

    fun uninstallCatalog(catalog: Catalog) {
        scope.launch {
            if (catalog is CatalogInstalled) {
                uninstallCatalog.await(catalog)
            }
        }
    }

    fun cancelCatalogJob(catalog: Catalog) {

        installerJobs[catalog.sourceId]?.cancel()
        installerJobs.remove(catalog.sourceId)
        if (catalog is CatalogRemote) {
            state.installSteps = installSteps + (catalog.pkgName to InstallStep.Idle)
        }
        if (catalog is CatalogInstalled) {
            state.installSteps = installSteps + (catalog.pkgName to InstallStep.Idle)
        }
    }

    fun refreshCatalogs() {
        scope.launch(Dispatchers.IO) {
            state.isRefreshing = true
            syncRemoteCatalogs.await(true, onError = { error ->
                showSnackBar(exceptionHandler(error))
            })
            state.isRefreshing = false
            if (autoInstaller.value) {
                startExtensionManagerService.start()
            }
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

    private fun List<CatalogLocal>.filteredByLanguageChoice(choice: LanguageChoice): List<CatalogLocal> {
        return when (choice) {
            LanguageChoice.All -> this
            is LanguageChoice.One -> filter { it.source?.lang == choice.language.code }
            is LanguageChoice.Others -> {
                val codes = choice.languages.map { it.code }
                filter { it.source?.lang in codes }
            }
        }
    }

    private fun CatalogLocal.matchesLanguageChoice(choice: LanguageChoice): Boolean {
        return when (choice) {
            LanguageChoice.All -> true
            is LanguageChoice.One -> source?.lang == choice.language.code
            is LanguageChoice.Others -> {
                val codes = choice.languages.map { it.code }
                source?.lang in codes
            }
        }
    }

}
