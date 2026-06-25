package ireader.presentation.ui.home.sources.extension

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import ireader.core.log.Log
import ireader.core.os.InstallStep
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.ExtensionManager
import ireader.domain.catalogs.interactor.ExtensionRepositoryManager
import ireader.domain.catalogs.interactor.ExtensionSecurityManager
import ireader.domain.catalogs.interactor.GetCatalogsByType
import ireader.domain.models.entities.*
import ireader.domain.preferences.prefs.BrowsePreferences
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.services.ExtensionChangeEvent
import ireader.domain.usecases.extension.ExtensionUseCases
import ireader.domain.usecases.services.StartExtensionManagerService
import ireader.domain.utils.exceptionHandler
import ireader.i18n.UiText
import ireader.presentation.ui.core.ui.asStateIn
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.IO
import kotlin.concurrent.Volatile

/**
 * ViewModel for the Extension screen.
 *
 * Simplified architecture — talks directly to CatalogStore and use cases.
 * No intermediate controller layer. Single state flow, single source of truth.
 */
class ExtensionViewModel(
    private val extensionUseCases: ExtensionUseCases,
    val uiPreferences: UiPreferences,
    val startExtensionManagerService: StartExtensionManagerService,
    private val catalogStore: CatalogStore,
    private val browsePreferences: BrowsePreferences,
    private val deleteUserSource: ireader.domain.usersource.interactor.DeleteUserSource? = null,
) : BaseViewModel() {

    companion object {
        private const val AUTO_FETCH_DEBOUNCE_MS = 500L
        private const val EMPTY_SOURCES_RETRY_DELAY_MS = 1500L
    }

    // Convenience accessors — prefixed with _ to avoid name collisions with public functions
    private val getCatalogsByType get() = extensionUseCases.getCatalogsByType
    private val _updateCatalog get() = extensionUseCases.updateCatalog
    private val _installCatalog get() = extensionUseCases.installCatalog
    private val _uninstallCatalog get() = extensionUseCases.uninstallCatalog
    private val _togglePinnedCatalog get() = extensionUseCases.togglePinnedCatalog
    private val _syncRemoteCatalogs get() = extensionUseCases.syncRemoteCatalogs
    private val sourceHealthChecker get() = extensionUseCases.sourceHealthChecker
    private val sourceCredentialsRepository get() = extensionUseCases.sourceCredentialsRepository
    private val extensionWatcherService get() = extensionUseCases.extensionWatcherService
    private val catalogSourceRepository get() = extensionUseCases.catalogSourceRepository
    private val extensionManager: ExtensionManager? get() = extensionUseCases.extensionManager
    private val extensionSecurityManager: ExtensionSecurityManager? get() = extensionUseCases.extensionSecurityManager

    // ── State ───────────────────────────────────────────────────────────

    private val _state = MutableStateFlow(ExtensionScreenState())
    val state: StateFlow<ExtensionScreenState> = _state.asStateFlow()

    var currentDialog by mutableStateOf<ExtensionDialog>(ExtensionDialog.None)
        private set

    private inline fun updateState(crossinline update: (ExtensionScreenState) -> ExtensionScreenState) {
        _state.update { update(it) }
    }

    @Volatile private var autoFetchJob: Job? = null
    private val installerJobs: MutableMap<Long, Job> = mutableMapOf()

    // ── Preferences ─────────────────────────────────────────────────────

    val incognito = uiPreferences.incognitoMode().asStateIn(scope)
    val lastUsedSource = uiPreferences.lastUsedSource().asStateIn(scope)
    val defaultRepo = uiPreferences.defaultRepository().asStateIn(scope)
    val autoInstaller = uiPreferences.autoCatalogUpdater().asStateIn(scope)
    val showLanguageFilter = uiPreferences.showLanguageFilter().asStateIn(scope)

    // ── Derived state ───────────────────────────────────────────────────

    val userSources: List<SourceUiModel>
        get() {
            val s = _state.value
            val fp = s.pinnedCatalogs.filteredByLanguageChoice(s.selectedUserSourceLanguage)
            val fu = s.unpinnedCatalogs.filteredByLanguageChoice(s.selectedUserSourceLanguage)
            val list = mutableListOf<SourceUiModel>()

            if (lastUsedSource.value != -1L) {
                (s.pinnedCatalogs + s.unpinnedCatalogs).firstOrNull { it.sourceId == lastUsedSource.value }?.let { c ->
                    if (c.matchesLanguageChoice(s.selectedUserSourceLanguage)) {
                        list.add(SourceUiModel.Header(SourceKeys.LAST_USED_KEY))
                        list.add(SourceUiModel.Item(c, SourceState.LastUsed))
                    }
                }
            }
            if (fp.isNotEmpty()) {
                list.add(SourceUiModel.Header(SourceKeys.PINNED_KEY))
                list.addAll(fp.map { SourceUiModel.Item(it, SourceState.Pinned) })
            }
            if (fu.isNotEmpty()) {
                list.addAll(
                    fu.groupBy { it.source?.lang ?: "others" }
                        .flatMap { (lang, sources) ->
                            listOf(SourceUiModel.Header(lang)) + sources.map { SourceUiModel.Item(it, SourceState.UnPinned) }
                        }
                )
            }
            return list
        }

    // ── Init ────────────────────────────────────────────────────────────

    init {
        initializeLanguagePreferences()
        subscribeToCatalogs()
        startExtensionWatcher()
        observeLoadingSources()
        scheduleEmptySourcesRetry()
    }

    /**
     * After a crash recovery, catalog flow subscription can lose the race.
     * Schedule retries to ensure sources appear.
     */
    private fun scheduleEmptySourcesRetry() {
        scope.launch(ioDispatcher) {
            delay(EMPTY_SOURCES_RETRY_DELAY_MS)
            val s = _state.value
            if (s.allPinnedCatalogs.isEmpty() && s.allUnpinnedCatalogs.isEmpty()) {
                Log.debug { "ExtensionViewModel: empty after retry, forcing reload" }
                catalogStore.reloadCatalogs()
            }
            // Second retry for edge cases
            if (s.allPinnedCatalogs.isEmpty() && s.allUnpinnedCatalogs.isEmpty()) {
                delay(EMPTY_SOURCES_RETRY_DELAY_MS * 3)
                val s2 = _state.value
                if (s2.allPinnedCatalogs.isEmpty() && s2.allUnpinnedCatalogs.isEmpty()) {
                    Log.debug { "ExtensionViewModel: still empty after second retry" }
                    catalogStore.reloadCatalogs()
                }
            }
        }
    }

    private fun initializeLanguagePreferences() {
        val initial = browsePreferences.selectedLanguages().get()
        val choice = when {
            initial.isEmpty() -> LanguageChoice.All
            initial.size == 1 -> LanguageChoice.One(Language(initial.first()))
            else -> LanguageChoice.Others(initial.map { Language(it) })
        }
        updateState { it.copy(selectedUserSourceLanguage = choice, selectedLanguage = choice) }

        scope.launch {
            browsePreferences.selectedLanguages().changes().collect { sel ->
                val c = when {
                    sel.isEmpty() -> LanguageChoice.All
                    sel.size == 1 -> LanguageChoice.One(Language(sel.first()))
                    else -> LanguageChoice.Others(sel.map { Language(it) })
                }
                updateState { s ->
                    s.copy(
                        selectedUserSourceLanguage = c,
                        selectedLanguage = c,
                        pinnedCatalogs = s.allPinnedCatalogs.filteredByQuery(s.searchQuery).filteredByLanguageChoice(c),
                        unpinnedCatalogs = s.allUnpinnedCatalogs.filteredByQuery(s.searchQuery).filteredByLanguageChoice(c),
                        remoteCatalogs = s.allRemoteCatalogs.filteredByQuery(s.searchQuery).filteredByChoice(c),
                    )
                }
            }
        }
    }

    private fun subscribeToCatalogs() {
        scope.launch {
            snapshotFlow { _state.value.selectedRepositoryType }
                .flatMapConcat { repoType ->
                    getCatalogsByType.subscribe(excludeRemoteInstalled = true, repositoryType = repoType)
                }
                .collect { catalogs ->
                    val choices = getLanguageChoices(catalogs.remote, catalogs.pinned + catalogs.unpinned)
                    updateState { s ->
                        s.copy(
                            allPinnedCatalogs = catalogs.pinned,
                            allUnpinnedCatalogs = catalogs.unpinned,
                            allRemoteCatalogs = catalogs.remote,
                            pinnedCatalogs = catalogs.pinned.filteredByQuery(s.searchQuery).filteredByLanguageChoice(s.selectedLanguage),
                            unpinnedCatalogs = catalogs.unpinned.filteredByQuery(s.searchQuery).filteredByLanguageChoice(s.selectedLanguage),
                            remoteCatalogs = catalogs.remote.filteredByQuery(s.searchQuery).filteredByChoice(s.selectedLanguage),
                            languageChoices = choices,
                        )
                    }
                    // Fix: if filter excluded everything, reset to All
                    val total = catalogs.pinned.size + catalogs.unpinned.size
                    val fp = catalogs.pinned.filteredByQuery(_state.value.searchQuery).filteredByLanguageChoice(_state.value.selectedLanguage)
                    val fu = catalogs.unpinned.filteredByQuery(_state.value.searchQuery).filteredByLanguageChoice(_state.value.selectedLanguage)
                    if (total > 0 && fp.isEmpty() && fu.isEmpty()) {
                        updateState { s ->
                            s.copy(
                                selectedUserSourceLanguage = LanguageChoice.All,
                                selectedLanguage = LanguageChoice.All,
                                pinnedCatalogs = catalogs.pinned.filteredByQuery(s.searchQuery).filteredByLanguageChoice(LanguageChoice.All),
                                unpinnedCatalogs = catalogs.unpinned.filteredByQuery(s.searchQuery).filteredByLanguageChoice(LanguageChoice.All),
                                remoteCatalogs = catalogs.remote.filteredByQuery(s.searchQuery).filteredByChoice(LanguageChoice.All),
                            )
                        }
                    }
                }
        }
    }

    private fun startExtensionWatcher() {
        extensionWatcherService.start()
        scope.launch {
            extensionWatcherService.events.collect { event ->
                when (event) {
                    is ExtensionChangeEvent.Added -> {
                        showSnackBar(UiText.DynamicString("Extension added: ${event.extensionName}"))
                        refreshCatalogsQuietly()
                    }
                    is ExtensionChangeEvent.Removed -> {
                        showSnackBar(UiText.DynamicString("Extension removed: ${event.extensionName}"))
                        refreshCatalogsQuietly()
                    }
                }
            }
        }
    }

    private fun observeLoadingSources() {
        scope.launch {
            catalogStore.getLoadingSourcesFlow().collect { ids ->
                updateState { it.copy(loadingSources = ids) }
            }
        }
    }

    // ── Public actions ───────────────────────────────────────────────────

    fun setSearchQuery(query: String?) {
        updateState { s ->
            s.copy(
                searchQuery = query,
                pinnedCatalogs = s.allPinnedCatalogs.filteredByQuery(query).filteredByLanguageChoice(s.selectedLanguage),
                unpinnedCatalogs = s.allUnpinnedCatalogs.filteredByQuery(query).filteredByLanguageChoice(s.selectedLanguage),
                remoteCatalogs = s.allRemoteCatalogs.filteredByQuery(query).filteredByChoice(s.selectedLanguage),
            )
        }
    }

    fun setSelectedLanguage(choice: LanguageChoice) {
        updateState { s ->
            s.copy(
                selectedLanguage = choice,
                pinnedCatalogs = s.allPinnedCatalogs.filteredByQuery(s.searchQuery).filteredByLanguageChoice(choice),
                unpinnedCatalogs = s.allUnpinnedCatalogs.filteredByQuery(s.searchQuery).filteredByLanguageChoice(choice),
                remoteCatalogs = s.allRemoteCatalogs.filteredByQuery(s.searchQuery).filteredByChoice(choice),
            )
        }
    }

    fun setUserSourceLanguage(choice: LanguageChoice) {
        updateState { it.copy(selectedUserSourceLanguage = choice) }
    }

    fun setCurrentPagerPage(page: Int) {
        updateState { it.copy(currentPagerPage = page) }
    }

    fun toggleSearchMode(enabled: Boolean) {
        updateState { it.copy(isInSearchMode = enabled) }
        if (!enabled) setSearchQuery(null)
    }

    fun setRepositoryTypeFilter(repositoryType: String?) {
        updateState { it.copy(selectedRepositoryType = repositoryType) }
    }

    /**
     * Install or update a catalog. Direct implementation — no controller indirection.
     */
    fun installCatalog(catalog: Catalog) {
        if (installerJobs.containsKey(catalog.sourceId)) return

        installerJobs[catalog.sourceId] = scope.launch {
            val isUpdate = catalog is CatalogInstalled
            val (pkgName, flow) = if (isUpdate) {
                catalog as CatalogInstalled
                catalog.pkgName to _updateCatalog.await(catalog)
            } else {
                catalog as CatalogRemote
                catalog.pkgName to _installCatalog.await(catalog)
            }

            flow.collect { step ->
                if (step is InstallStep.Error) showSnackBar(UiText.DynamicString(step.error))
                updateState { s ->
                    s.copy(
                        installSteps = if (step is InstallStep.Success) {
                            refreshCatalogsQuietly()
                            s.installSteps - pkgName
                        } else {
                            s.installSteps + (pkgName to step)
                        }
                    )
                }
            }
            installerJobs.remove(catalog.sourceId)
        }
    }

    fun togglePinnedCatalog(catalog: Catalog) {
        scope.launch { _togglePinnedCatalog.await(catalog) }
    }

    fun uninstallCatalog(catalog: Catalog) {
        scope.launch {
            if (catalog is CatalogInstalled) {
                _uninstallCatalog.await(catalog)
                delay(1000)
                refreshCatalogsQuietly()
            }
        }
    }

    fun cancelCatalogJob(catalog: Catalog) {
        installerJobs[catalog.sourceId]?.cancel()
        installerJobs.remove(catalog.sourceId)
        val pkgName = when (catalog) {
            is CatalogRemote -> catalog.pkgName
            is CatalogInstalled -> catalog.pkgName
            else -> return
        }
        updateState { s -> s.copy(installSteps = s.installSteps + (pkgName to InstallStep.Idle)) }
    }

    /**
     * Refresh catalogs from remote. Retries once on failure.
     */
    fun refreshCatalogs() {
        scope.launch(ioDispatcher) {
            updateState { it.copy(isRefreshing = true) }
            try {
                _syncRemoteCatalogs.await(true, onError = { showSnackBar(exceptionHandler(it)) })
            } catch (e: Exception) {
                Log.error("ExtensionViewModel: refresh failed, retrying once", e)
                delay(1000)
                try {
                    _syncRemoteCatalogs.await(true, onError = { })
                } catch (_: Exception) { }
            }
            updateState { it.copy(isRefreshing = false) }
            if (autoInstaller.value) startExtensionManagerService.start()
        }
    }

    fun refreshExtensions() = refreshCatalogs()

    private fun refreshCatalogsQuietly() {
        scope.launch(ioDispatcher) {
            try { catalogStore.reloadCatalogs() } catch (e: Exception) {
                Log.error("Failed to refresh catalogs quietly", e)
            }
        }
    }

    // ── Source health ────────────────────────────────────────────────────

    fun checkSourceHealth(sourceId: Long) {
        scope.launch(ioDispatcher) {
            try {
                val health = sourceHealthChecker.checkStatus(sourceId)
                updateState { s -> s.copy(sourceStatuses = s.sourceStatuses + (sourceId to health.status)) }
            } catch (e: Exception) {
                updateState { s -> s.copy(sourceStatuses = s.sourceStatuses + (sourceId to SourceStatus.Error(e.message ?: "Unknown"))) }
            }
        }
    }

    fun checkAllSourcesHealth() {
        scope.launch(Dispatchers.IO) {
            val ids = (_state.value.pinnedCatalogs + _state.value.unpinnedCatalogs).mapNotNull { it.sourceId }
            try {
                val map = sourceHealthChecker.checkMultipleSources(ids)
                updateState { s -> s.copy(sourceStatuses = s.sourceStatuses + map.mapValues { it.value.status }) }
            } catch (_: Exception) {}
        }
    }

    fun getSourceStatus(sourceId: Long): SourceStatus? = _state.value.sourceStatuses[sourceId]
    fun isSourceLoading(sourceId: Long): Boolean = sourceId in _state.value.loadingSources

    // ── Credentials ─────────────────────────────────────────────────────

    fun loginToSource(sourceId: Long, username: String, password: String) {
        scope.launch(ioDispatcher) {
            try {
                sourceCredentialsRepository.storeCredentials(sourceId, username, password)
                checkSourceHealth(sourceId)
                showSnackBar(UiText.DynamicString("Login successful"))
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Login failed: ${e.message ?: "Unknown"}"))
            }
        }
    }

    suspend fun hasCredentials(sourceId: Long): Boolean = sourceCredentialsRepository.hasCredentials(sourceId)

    fun logoutFromSource(sourceId: Long) {
        scope.launch(ioDispatcher) {
            try {
                sourceCredentialsRepository.removeCredentials(sourceId)
                checkSourceHealth(sourceId)
                showSnackBar(UiText.DynamicString("Logged out"))
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Logout failed: ${e.message ?: "Unknown"}"))
            }
        }
    }

    // ── Repository management ────────────────────────────────────────────

    fun addRepository(url: String) {
        scope.launch {
            try {
                val info = parseRepositoryUrl(url)
                val existing = catalogSourceRepository.subscribe().first()
                existing.filter { it.isEnable }.forEach { catalogSourceRepository.update(it.copy(isEnable = false)) }
                catalogSourceRepository.insert(
                    ExtensionSource(id = 0, name = info.name, key = info.url, owner = info.owner, source = info.source, isEnable = true, repositoryType = info.type)
                )
                val inserted = catalogSourceRepository.subscribe().first().firstOrNull { it.key == info.url }
                if (inserted != null) uiPreferences.defaultRepository().set(inserted.id)
                showSnackBar(UiText.DynamicString("Repository added"))
                triggerDebouncedAutoFetch()
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Failed to add repository: ${e.message ?: "Unknown"}"))
            }
        }
    }

    fun toggleSourceRepository(source: ExtensionSource, enabled: Boolean) {
        scope.launch {
            try {
                catalogSourceRepository.update(source.copy(isEnable = enabled))
                triggerDebouncedAutoFetch()
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Failed to update repository: ${e.message ?: "Unknown"}"))
            }
        }
    }

    private fun triggerDebouncedAutoFetch() {
        autoFetchJob?.cancel()
        autoFetchJob = scope.launch(ioDispatcher) {
            delay(AUTO_FETCH_DEBOUNCE_MS)
            if (!isActive) return@launch
            updateState { it.copy(isRefreshing = true) }
            try { _syncRemoteCatalogs.await(forceRefresh = true, onError = { Log.warn(it, "Auto-fetch failed") }) }
            finally { if (isActive) updateState { it.copy(isRefreshing = false) } }
        }
    }

    fun cancelAutoFetch() { autoFetchJob?.cancel(); autoFetchJob = null }

    private fun parseRepositoryUrl(url: String) = when {
        url.contains("lnreader-plugins") -> Triple("LNReader Plugins", "LNREADER", "https://github.com/LNReader/lnreader-plugins")
        url.contains("IReader-extensions") -> Triple("IReader Extensions", "IREADER", "https://github.com/IReaderorg/IReader-extensions")
        else -> Triple("Custom Repository", "IREADER", url)
    }.let { (name, type, source) -> RepositoryInfo(name, url, url.substringBeforeLast("/").substringAfterLast("/"), source, type) }

    private data class RepositoryInfo(val name: String, val url: String, val owner: String, val source: String, val type: String)

    fun getRepositoryTypeDisplayName(): String = when (_state.value.selectedRepositoryType) {
        "IREADER" -> "IReader"; "LNREADER" -> "LNReader"; else -> "All"
    }

    // ── Dialog ───────────────────────────────────────────────────────────

    fun showDialog(dialog: ExtensionDialog) { currentDialog = dialog }
    fun dismissDialog() { currentDialog = ExtensionDialog.None }

    // ── Enhanced extension management ────────────────────────────────────

    fun getExtensionSecurity(catalog: Catalog) {
        scope.launch(ioDispatcher) {
            try { extensionSecurityManager?.scanExtension(catalog); showSnackBar(UiText.DynamicString("Security scan complete")) }
            catch (e: Exception) { showSnackBar(UiText.DynamicString("Security scan failed: ${e.message ?: "Unknown"}")) }
        }
    }

    suspend fun getExtensionStatistics(extensionId: Long): ExtensionStatistics? = try { extensionManager?.getExtensionStatistics(extensionId) } catch (_: Exception) { null }

    fun setExtensionTrustLevel(extensionId: Long, trustLevel: ExtensionTrustLevel) {
        scope.launch(ioDispatcher) {
            try { extensionSecurityManager?.setTrustLevel(extensionId, trustLevel); showSnackBar(UiText.DynamicString("Trust level updated")) }
            catch (e: Exception) { showSnackBar(UiText.DynamicString("Failed: ${e.message ?: "Unknown"}")) }
        }
    }

    fun batchUpdateExtensions() {
        scope.launch(ioDispatcher) {
            val installed = (_state.value.pinnedCatalogs + _state.value.unpinnedCatalogs).filterIsInstance<CatalogInstalled>()
            extensionManager?.batchUpdateExtensions(installed)?.onSuccess { results ->
                showSnackBar(UiText.DynamicString("Updated ${results.values.count { it.isSuccess }} of ${installed.size}"))
            }
        }
    }

    fun checkForExtensionUpdates() {
        scope.launch(ioDispatcher) {
            try {
                val updates = extensionManager?.checkForUpdates()
                showSnackBar(UiText.DynamicString(if (updates.isNullOrEmpty()) "All up to date" else "${updates.size} updates available"))
            } catch (e: Exception) { showSnackBar(UiText.DynamicString("Update check failed: ${e.message ?: "Unknown"}")) }
        }
    }

    fun trackExtensionUsage(extensionId: Long) { scope.launch(ioDispatcher) { try { extensionManager?.trackExtensionUsage(extensionId) } catch (_: Exception) {} } }
    fun reportExtensionError(extensionId: Long, error: Throwable) { scope.launch(ioDispatcher) { try { extensionManager?.reportExtensionError(extensionId, error) } catch (_: Exception) {} } }

    // ── User source management ───────────────────────────────────────────

    fun deleteUserSourceById(sourceId: Long) {
        if (deleteUserSource == null) { showSnackBar(UiText.DynamicString("Delete not available")); return }
        scope.launch(ioDispatcher) {
            try { deleteUserSource.byId(sourceId); catalogStore.refreshUserSources(); showSnackBar(UiText.DynamicString("Source deleted")) }
            catch (e: Exception) { showSnackBar(UiText.DynamicString("Failed: ${e.message ?: "Unknown"}")) }
        }
    }

    fun deleteUserSourceByUrl(sourceUrl: String) {
        if (deleteUserSource == null) { showSnackBar(UiText.DynamicString("Delete not available")); return }
        scope.launch(ioDispatcher) {
            try { deleteUserSource.byUrl(sourceUrl); catalogStore.refreshUserSources(); showSnackBar(UiText.DynamicString("Source deleted")) }
            catch (e: Exception) { showSnackBar(UiText.DynamicString("Failed: ${e.message ?: "Unknown"}")) }
        }
    }

    // ── Utilities ────────────────────────────────────────────────────────

    private fun getLanguageChoices(remote: List<CatalogRemote>, local: List<CatalogLocal>): List<LanguageChoice> {
        val known = mutableListOf<LanguageChoice.One>()
        val unknown = mutableListOf<Language>()
        val cmp = UserLanguagesComparator().then(InstalledLanguagesComparator(local)).thenBy { it.code }
        (remote.asSequence().map { Language(it.lang) } + local.asSequence().mapNotNull { it.source?.lang }.map { Language(it) })
            .distinct().sortedWith(cmp).forEach { c -> if (c.toEmoji() != null) known.add(LanguageChoice.One(c)) else unknown.add(c) }
        return buildList {
            add(LanguageChoice.All)
            addAll(known)
            if (unknown.isNotEmpty()) add(LanguageChoice.Others(unknown))
        }
    }

    private fun <T : Catalog> List<T>.filteredByQuery(q: String?) = if (q == null) this else filter { it.name.contains(q, true) }
    private fun List<CatalogRemote>.filteredByChoice(c: LanguageChoice) = when (c) { LanguageChoice.All -> this; is LanguageChoice.One -> filter { c.language.code == it.lang }; is LanguageChoice.Others -> { val codes = c.languages.map { it.code }; filter { it.lang in codes } } }
    private fun List<CatalogLocal>.filteredByLanguageChoice(c: LanguageChoice) = when (c) { LanguageChoice.All -> this; is LanguageChoice.One -> filter { it.source?.lang == c.language.code }; is LanguageChoice.Others -> { val codes = c.languages.map { it.code }; filter { it.source?.lang in codes } } }
    private fun CatalogLocal.matchesLanguageChoice(c: LanguageChoice) = when (c) { LanguageChoice.All -> true; is LanguageChoice.One -> source?.lang == c.language.code; is LanguageChoice.Others -> source?.lang in c.languages.map { it.code } }
}
