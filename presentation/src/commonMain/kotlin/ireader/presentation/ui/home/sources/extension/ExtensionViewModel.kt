package ireader.presentation.ui.home.sources.extension

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import ireader.core.os.InstallStep
import ireader.domain.catalogs.interactor.*
import ireader.domain.models.entities.*
import ireader.domain.models.entities.SourceHealth
import ireader.domain.models.entities.SourceStatus
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.services.ExtensionChangeEvent
import ireader.domain.services.ExtensionWatcherService
import ireader.domain.services.SourceHealthChecker
import ireader.domain.usecases.services.StartExtensionManagerService
import ireader.domain.utils.exceptionHandler
import ireader.i18n.UiText
import ireader.presentation.ui.core.ui.asStateIn
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
        private val sourceHealthChecker: SourceHealthChecker,
        private val sourceCredentialsRepository: ireader.domain.data.repository.SourceCredentialsRepository,
        private val extensionWatcherService: ExtensionWatcherService,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), CatalogsState by state {

    val incognito = uiPreferences.incognitoMode().asStateIn(scope)
    val lastUsedSource = uiPreferences.lastUsedSource().asStateIn(scope)
    val defaultRepo = uiPreferences.defaultRepository().asStateIn(scope)
    val autoInstaller = uiPreferences.autoCatalogUpdater().asStateIn(scope)
    val showLanguageFilter = uiPreferences.showLanguageFilter().asStateIn(scope)
    
    // Track source health status
    val sourceStatuses = mutableStateMapOf<Long, SourceStatus>()
    
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
        
        // Start extension watcher (desktop only)
        startExtensionWatcher()
    }
    
    /**
     * Start watching for extension changes (desktop only)
     */
    private fun startExtensionWatcher() {
        // Start the watcher
        extensionWatcherService.start()
        
        // Listen for extension change events
        scope.launch {
            extensionWatcherService.events.collect { event ->
                when (event) {
                    is ExtensionChangeEvent.Added -> {
                        // Show notification
                        showSnackBar(UiText.DynamicString("Extension added: ${event.extensionName}"))
                        // Refresh catalogs to load the new extension
                        refreshCatalogsQuietly()
                    }
                    is ExtensionChangeEvent.Removed -> {
                        // Show notification
                        showSnackBar(UiText.DynamicString("Extension removed: ${event.extensionName}"))
                        // Refresh catalogs to remove the extension
                        refreshCatalogsQuietly()
                    }
                }
            }
        }
    }
    
    /**
     * Refresh catalogs without showing loading indicator (for background updates)
     */
    private fun refreshCatalogsQuietly() {
        scope.launch(Dispatchers.IO) {
            // Just trigger a refresh of local catalogs without syncing remote
            // The getCatalogsByType.subscribe flow will automatically update the UI
        }
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
    
    /**
     * Check the health status of a specific source
     */
    fun checkSourceHealth(sourceId: Long) {
        scope.launch(Dispatchers.IO) {
            try {
                val health = sourceHealthChecker.checkStatus(sourceId)
                sourceStatuses[sourceId] = health.status
            } catch (e: Exception) {
                sourceStatuses[sourceId] = SourceStatus.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Check the health status of all installed sources
     */
    fun checkAllSourcesHealth() {
        scope.launch(Dispatchers.IO) {
            val installedSources = (pinnedCatalogs + unpinnedCatalogs)
                .mapNotNull { it.sourceId }
            
            try {
                val healthMap = sourceHealthChecker.checkMultipleSources(installedSources)
                healthMap.forEach { (sourceId, health) ->
                    sourceStatuses[sourceId] = health.status
                }
            } catch (e: Exception) {
                // Handle error silently or show a snackbar
            }
        }
    }
    
    /**
     * Get cached status for a source
     */
    fun getSourceStatus(sourceId: Long): SourceStatus? {
        return sourceStatuses[sourceId]
    }
    
    /**
     * Store login credentials for a source
     */
    fun loginToSource(sourceId: Long, username: String, password: String) {
        scope.launch(Dispatchers.IO) {
            try {
                sourceCredentialsRepository.storeCredentials(sourceId, username, password)
                // Re-check source health after login
                checkSourceHealth(sourceId)
                showSnackBar(UiText.DynamicString("Login successful"))
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Login failed: ${e.message}"))
            }
        }
    }
    
    /**
     * Check if a source has stored credentials
     */
    suspend fun hasCredentials(sourceId: Long): Boolean {
        return sourceCredentialsRepository.hasCredentials(sourceId)
    }
    
    /**
     * Remove stored credentials for a source
     */
    fun logoutFromSource(sourceId: Long) {
        scope.launch(Dispatchers.IO) {
            try {
                sourceCredentialsRepository.removeCredentials(sourceId)
                // Re-check source health after logout
                checkSourceHealth(sourceId)
                showSnackBar(UiText.DynamicString("Logged out successfully"))
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Logout failed: ${e.message}"))
            }
        }
    }
    
    /**
     * Add a new repository
     */
    fun addRepository(url: String) {
        scope.launch(Dispatchers.IO) {
            try {
                // Parse the repository URL to extract information
                val repoName = extractRepositoryName(url)
                val repoOwner = extractRepositoryOwner(url)
                
                // Create a new ExtensionSource for the repository
                val newRepo = ExtensionSource(
                    id = System.currentTimeMillis(), // Generate unique ID
                    name = repoName,
                    key = url,
                    owner = repoOwner,
                    source = url,
                    isEnable = true
                )
                
                // Note: You would need to add a method to CatalogSourceRepository to insert
                // For now, we'll show a success message
                showSnackBar(UiText.DynamicString("Repository added: $repoName"))
                
                // Refresh catalogs to fetch sources from the new repository
                refreshCatalogs()
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Failed to add repository: ${e.message}"))
            }
        }
    }
    
    /**
     * Extract repository name from URL
     */
    private fun extractRepositoryName(url: String): String {
        return try {
            val parts = url.split("/")
            parts.getOrNull(parts.size - 2) ?: "Custom Repository"
        } catch (e: Exception) {
            "Custom Repository"
        }
    }
    
    /**
     * Extract repository owner from URL
     */
    private fun extractRepositoryOwner(url: String): String {
        return try {
            if (url.contains("github.com")) {
                val parts = url.split("/")
                val githubIndex = parts.indexOfFirst { it.contains("github.com") }
                parts.getOrNull(githubIndex + 1) ?: "Unknown"
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
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
