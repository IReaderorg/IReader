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
import ireader.domain.services.extension.ExtensionCommand
import ireader.domain.services.extension.ExtensionController
import ireader.domain.services.extension.ExtensionEvent as ControllerEvent
import ireader.domain.usecases.extension.ExtensionUseCases
import ireader.domain.usecases.services.StartExtensionManagerService
import ireader.domain.utils.exceptionHandler
import ireader.i18n.UiText
import ireader.presentation.ui.core.ui.asStateIn
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.IO

/**
 * ViewModel for the Extension screen using sealed state pattern.
 * 
 * Refactored to use ExtensionUseCases aggregate to reduce constructor parameters.
 * Target: ≤10 constructor parameters (Requirements: 1.3, 1.4, 1.5)
 * 
 * Now integrates with ExtensionController for SSOT state management.
 * Requirements: 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1
 * 
 * Uses a single immutable StateFlow<ExtensionScreenState> instead of multiple mutable states.
 * This provides:
 * - Single source of truth for UI state (via ExtensionController)
 * - Atomic state updates
 * - Better Compose performance with @Immutable state
 * - Clear Loading/Success/Error states
 */
class ExtensionViewModel(
    // Use case aggregate - groups 13 related use cases (Requirements: 1.3, 1.4, 1.5)
    private val extensionUseCases: ExtensionUseCases,
    val uiPreferences: UiPreferences,
    val startExtensionManagerService: StartExtensionManagerService,
    private val catalogStore: CatalogStore,
    private val browsePreferences: BrowsePreferences,
    // ExtensionController for SSOT state management (Requirements: 3.2, 3.3, 3.4, 3.5)
    private val extensionController: ExtensionController? = null,
) : BaseViewModel() {
    
    // Convenience accessors for aggregate use cases (backward compatibility)
    private val getCatalogsByType get() = extensionUseCases.getCatalogsByType
    private val updateCatalog get() = extensionUseCases.updateCatalog
    private val installCatalog get() = extensionUseCases.installCatalog
    private val uninstallCatalog get() = extensionUseCases.uninstallCatalog
    private val togglePinnedCatalog get() = extensionUseCases.togglePinnedCatalog
    private val syncRemoteCatalogs get() = extensionUseCases.syncRemoteCatalogs
    private val sourceHealthChecker get() = extensionUseCases.sourceHealthChecker
    private val sourceCredentialsRepository get() = extensionUseCases.sourceCredentialsRepository
    private val extensionWatcherService get() = extensionUseCases.extensionWatcherService
    private val catalogSourceRepository get() = extensionUseCases.catalogSourceRepository
    private val extensionManager: ExtensionManager? get() = extensionUseCases.extensionManager
    private val extensionSecurityManager: ExtensionSecurityManager? get() = extensionUseCases.extensionSecurityManager
    private val extensionRepositoryManager: ExtensionRepositoryManager? get() = extensionUseCases.extensionRepositoryManager

    // ==================== State Management ====================
    
    private val _state = MutableStateFlow(ExtensionScreenState())
    val state: StateFlow<ExtensionScreenState> = _state.asStateFlow()
    
    // Dialog state (separate from main state for simpler updates)
    var currentDialog by mutableStateOf<ExtensionDialog>(ExtensionDialog.None)
        private set
    
    // Helper to update state atomically
    private inline fun updateState(crossinline update: (ExtensionScreenState) -> ExtensionScreenState) {
        _state.update { update(it) }
    }
    
    // ==================== Preferences (exposed as StateFlow) ====================
    
    val incognito = uiPreferences.incognitoMode().asStateIn(scope)
    val lastUsedSource = uiPreferences.lastUsedSource().asStateIn(scope)
    val defaultRepo = uiPreferences.defaultRepository().asStateIn(scope)
    val autoInstaller = uiPreferences.autoCatalogUpdater().asStateIn(scope)
    val showLanguageFilter = uiPreferences.showLanguageFilter().asStateIn(scope)
    
    // ==================== Derived State ====================
    
    /**
     * User sources list with headers (Last Used, Pinned, by Language)
     * This is now a computed property that reads from the current state.
     * The UI should collect state and this will be recalculated on each recomposition.
     */
    val userSources: List<SourceUiModel>
        get() {
            val currentState = _state.value
            val filteredPinned = currentState.pinnedCatalogs.filteredByLanguageChoice(currentState.selectedUserSourceLanguage)
            val filteredUnpinned = currentState.unpinnedCatalogs.filteredByLanguageChoice(currentState.selectedUserSourceLanguage)

            val list = mutableListOf<SourceUiModel>()
            
            // Last used source
            if (lastUsedSource.value != -1L) {
                (currentState.pinnedCatalogs + currentState.unpinnedCatalogs).firstOrNull {
                    it.sourceId == lastUsedSource.value
                }?.let { c ->
                    if (c.matchesLanguageChoice(currentState.selectedUserSourceLanguage)) {
                        list.add(SourceUiModel.Header(SourceKeys.LAST_USED_KEY))
                        list.add(SourceUiModel.Item(c, SourceState.LastUsed))
                    }
                }
            }

            // Pinned sources
            if (filteredPinned.isNotEmpty()) {
                list.add(SourceUiModel.Header(SourceKeys.PINNED_KEY))
                list.addAll(filteredPinned.map { SourceUiModel.Item(it, SourceState.Pinned) })
            }
            
            // Unpinned sources grouped by language
            if (filteredUnpinned.isNotEmpty()) {
                list.addAll(
                    filteredUnpinned.groupBy { it.source?.lang ?: "others" }
                        .flatMap { (lang, sources) ->
                            listOf(SourceUiModel.Header(lang)) + 
                                sources.map { SourceUiModel.Item(it, SourceState.UnPinned) }
                        }
                )
            }
            
            return list
        }
    
    // ==================== Jobs ====================
    
    private var installerJobs: MutableMap<Long, Job> = mutableMapOf()
    
    // ==================== Initialization ====================
    
    init {
        initializeLanguagePreferences()
        subscribeToCatalogs()
        startExtensionWatcher()
        observeLoadingSources()
        observeControllerEvents()
        
        // Initialize controller if available
        extensionController?.dispatch(ExtensionCommand.LoadExtensions)
    }
    
    /**
     * Observe events from the ExtensionController for UI feedback.
     * Requirements: 3.4, 4.2, 4.3
     */
    private fun observeControllerEvents() {
        extensionController?.let { controller ->
            scope.launch {
                controller.events.collect { event ->
                    when (event) {
                        is ControllerEvent.Error -> {
                            showSnackBar(UiText.DynamicString(event.error.toUserMessage()))
                        }
                        is ControllerEvent.ExtensionsLoaded -> {
                            Log.debug { "Extensions loaded: ${event.installedCount} installed, ${event.availableCount} available" }
                        }
                        is ControllerEvent.InstallComplete -> {
                            showSnackBar(UiText.DynamicString("Installed ${event.catalog.name}"))
                        }
                        is ControllerEvent.UninstallComplete -> {
                            showSnackBar(UiText.DynamicString("Uninstalled ${event.pkgName}"))
                        }
                        is ControllerEvent.UpdateComplete -> {
                            showSnackBar(UiText.DynamicString("Updated ${event.catalog.name}"))
                        }
                        is ControllerEvent.UpdatesAvailable -> {
                            showSnackBar(UiText.DynamicString("${event.count} updates available"))
                        }
                        is ControllerEvent.AllUpToDate -> {
                            showSnackBar(UiText.DynamicString("All extensions up to date"))
                        }
                        is ControllerEvent.RefreshComplete -> {
                            Log.debug { "Extensions refreshed" }
                        }
                        is ControllerEvent.BatchUpdateComplete -> {
                            showSnackBar(UiText.DynamicString("Updated ${event.successCount} of ${event.totalCount} extensions"))
                        }
                        is ControllerEvent.InstallProgress -> {
                            // Progress is handled via state
                        }
                        is ControllerEvent.ShowSnackbar -> {
                            showSnackBar(UiText.DynamicString(event.message))
                        }
                    }
                }
            }
        }
    }
    
    private fun initializeLanguagePreferences() {
        val initialSelectedLanguages = browsePreferences.selectedLanguages().get()
        val initialChoice = when {
            initialSelectedLanguages.isEmpty() -> LanguageChoice.All
            initialSelectedLanguages.size == 1 -> LanguageChoice.One(Language(initialSelectedLanguages.first()))
            else -> LanguageChoice.Others(initialSelectedLanguages.map { Language(it) })
        }
        updateState { it.copy(
            selectedUserSourceLanguage = initialChoice,
            selectedLanguage = initialChoice
        )}
        
        // Observe language preference changes
        scope.launch {
            browsePreferences.selectedLanguages().changes().collect { selectedLanguages ->
                val choice = when {
                    selectedLanguages.isEmpty() -> LanguageChoice.All
                    selectedLanguages.size == 1 -> LanguageChoice.One(Language(selectedLanguages.first()))
                    else -> LanguageChoice.Others(selectedLanguages.map { Language(it) })
                }
                // Update both the selected language and re-filter the catalogs
                updateState { state ->
                    state.copy(
                        selectedUserSourceLanguage = choice,
                        selectedLanguage = choice,
                        pinnedCatalogs = state.allPinnedCatalogs.filteredByQuery(state.searchQuery)
                            .filteredByLanguageChoice(choice),
                        unpinnedCatalogs = state.allUnpinnedCatalogs.filteredByQuery(state.searchQuery)
                            .filteredByLanguageChoice(choice),
                        remoteCatalogs = state.allRemoteCatalogs.filteredByQuery(state.searchQuery)
                            .filteredByChoice(choice)
                    )
                }
            }
        }
    }
    
    private fun subscribeToCatalogs() {
        // Subscribe to catalog changes based on repository type filter
        scope.launch {
            snapshotFlow { _state.value.selectedRepositoryType }
                .flatMapLatest { repositoryType: String? ->
                    getCatalogsByType.subscribe(
                        excludeRemoteInstalled = true,
                        repositoryType = repositoryType
                    )
                }
                .collect { catalogs: GetCatalogsByType.Catalogs ->
                    val languageChoices = getLanguageChoices(catalogs.remote, catalogs.pinned + catalogs.unpinned)
                    
                    updateState { state ->
                        state.copy(
                            allPinnedCatalogs = catalogs.pinned,
                            allUnpinnedCatalogs = catalogs.unpinned,
                            allRemoteCatalogs = catalogs.remote,
                            pinnedCatalogs = catalogs.pinned.filteredByQuery(state.searchQuery)
                                .filteredByLanguageChoice(state.selectedLanguage),
                            unpinnedCatalogs = catalogs.unpinned.filteredByQuery(state.searchQuery)
                                .filteredByLanguageChoice(state.selectedLanguage),
                            remoteCatalogs = catalogs.remote.filteredByQuery(state.searchQuery)
                                .filteredByChoice(state.selectedLanguage),
                            languageChoices = languageChoices
                        )
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
            catalogStore.getLoadingSourcesFlow().collect { loadingIds ->
                updateState { it.copy(loadingSources = loadingIds) }
            }
        }
    }
    
    // ==================== Public Actions ====================
    
    /**
     * Set search query and filter catalogs
     */
    fun setSearchQuery(query: String?) {
        updateState { state ->
            state.copy(
                searchQuery = query,
                pinnedCatalogs = state.allPinnedCatalogs.filteredByQuery(query)
                    .filteredByLanguageChoice(state.selectedLanguage),
                unpinnedCatalogs = state.allUnpinnedCatalogs.filteredByQuery(query)
                    .filteredByLanguageChoice(state.selectedLanguage),
                remoteCatalogs = state.allRemoteCatalogs.filteredByQuery(query)
                    .filteredByChoice(state.selectedLanguage)
            )
        }
    }
    
    /**
     * Set selected language filter
     */
    fun setSelectedLanguage(choice: LanguageChoice) {
        updateState { state ->
            state.copy(
                selectedLanguage = choice,
                pinnedCatalogs = state.allPinnedCatalogs.filteredByQuery(state.searchQuery)
                    .filteredByLanguageChoice(choice),
                unpinnedCatalogs = state.allUnpinnedCatalogs.filteredByQuery(state.searchQuery)
                    .filteredByLanguageChoice(choice),
                remoteCatalogs = state.allRemoteCatalogs.filteredByQuery(state.searchQuery)
                    .filteredByChoice(choice)
            )
        }
    }
    
    /**
     * Set user source language filter
     */
    fun setUserSourceLanguage(choice: LanguageChoice) {
        updateState { it.copy(selectedUserSourceLanguage = choice) }
    }
    
    /**
     * Set current pager page
     */
    fun setCurrentPagerPage(page: Int) {
        updateState { it.copy(currentPagerPage = page) }
    }
    
    /**
     * Toggle search mode
     */
    fun toggleSearchMode(enabled: Boolean) {
        updateState { it.copy(isInSearchMode = enabled) }
        if (!enabled) {
            setSearchQuery(null)
        }
    }
    
    /**
     * Set repository type filter
     */
    fun setRepositoryTypeFilter(repositoryType: String?) {
        updateState { it.copy(selectedRepositoryType = repositoryType) }
    }
    
    /**
     * Install or update a catalog.
     * Delegates to ExtensionController when available.
     * Requirements: 3.2, 3.3
     */
    fun installCatalog(catalog: Catalog) {
        // Use controller if available
        if (extensionController != null) {
            when (catalog) {
                is CatalogRemote -> extensionController.dispatch(ExtensionCommand.InstallExtension(catalog))
                is CatalogInstalled -> extensionController.dispatch(ExtensionCommand.UpdateExtension(catalog))
                else -> { /* Unsupported catalog type */ }
            }
            return
        }
        
        // Fallback to direct implementation
        if (!installerJobs.containsKey(catalog.sourceId)) {
            installerJobs[catalog.sourceId] = Job()
        }
        installerJobs[catalog.sourceId] = scope.launch {
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
                
                updateState { state ->
                    state.copy(
                        installSteps = if (step != InstallStep.Success) {
                            state.installSteps + (pkgName to step)
                        } else {
                            refreshCatalogsQuietly()
                            state.installSteps - pkgName
                        }
                    )
                }
            }
        }
    }
    
    /**
     * Toggle pinned status of a catalog
     */
    fun togglePinnedCatalog(catalog: Catalog) {
        scope.launch {
            togglePinnedCatalog.await(catalog)
        }
    }
    
    /**
     * Uninstall a catalog.
     * Delegates to ExtensionController when available.
     * Requirements: 3.2, 3.3
     */
    fun uninstallCatalog(catalog: Catalog) {
        // Use controller if available
        if (extensionController != null && catalog is CatalogInstalled) {
            extensionController.dispatch(ExtensionCommand.UninstallExtension(catalog))
            return
        }
        
        // Fallback to direct implementation
        scope.launch {
            if (catalog is CatalogInstalled) {
                uninstallCatalog.await(catalog)
                kotlinx.coroutines.delay(1000)
                refreshCatalogsQuietly()
            }
        }
    }
    
    /**
     * Cancel installation job for a catalog
     */
    fun cancelCatalogJob(catalog: Catalog) {
        installerJobs[catalog.sourceId]?.cancel()
        installerJobs.remove(catalog.sourceId)
        
        val pkgName = when (catalog) {
            is CatalogRemote -> catalog.pkgName
            is CatalogInstalled -> catalog.pkgName
            else -> return
        }
        
        updateState { state ->
            state.copy(installSteps = state.installSteps + (pkgName to InstallStep.Idle))
        }
    }
    
    /**
     * Refresh catalogs from remote.
     * Delegates to ExtensionController when available.
     * Requirements: 3.2, 3.3
     */
    fun refreshCatalogs() {
        // Use controller if available
        if (extensionController != null) {
            extensionController.dispatch(ExtensionCommand.RefreshExtensions)
            if (autoInstaller.value) {
                startExtensionManagerService.start()
            }
            return
        }
        
        // Fallback to direct implementation
        scope.launch(ioDispatcher) {
            updateState { it.copy(isRefreshing = true) }
            
            syncRemoteCatalogs.await(true, onError = { error ->
                showSnackBar(exceptionHandler(error))
            })
            
            updateState { it.copy(isRefreshing = false) }
            
            if (autoInstaller.value) {
                startExtensionManagerService.start()
            }
        }
    }
    
    // Alias for toolbar compatibility
    fun refreshExtensions() = refreshCatalogs()
    
    /**
     * Refresh catalogs without showing loading indicator
     */
    private fun refreshCatalogsQuietly() {
        scope.launch(ioDispatcher) {
            try {
                catalogStore.reloadCatalogs()
            } catch (e: Exception) {
                Log.error("Failed to refresh catalogs quietly", e)
            }
        }
    }
    
    // ==================== Source Health ====================
    
    /**
     * Check health status of a specific source
     */
    fun checkSourceHealth(sourceId: Long) {
        scope.launch(ioDispatcher) {
            try {
                val health = sourceHealthChecker.checkStatus(sourceId)
                updateState { state ->
                    state.copy(sourceStatuses = state.sourceStatuses + (sourceId to health.status))
                }
            } catch (e: Exception) {
                updateState { state ->
                    state.copy(sourceStatuses = state.sourceStatuses + (sourceId to SourceStatus.Error(e.message ?: "Unknown error")))
                }
            }
        }
    }
    
    /**
     * Check health of all installed sources
     */
    fun checkAllSourcesHealth() {
        scope.launch(Dispatchers.IO) {
            val currentState = _state.value
            val installedSources = (currentState.pinnedCatalogs + currentState.unpinnedCatalogs)
                .mapNotNull { it.sourceId }
            
            try {
                val healthMap = sourceHealthChecker.checkMultipleSources(installedSources)
                updateState { state ->
                    state.copy(sourceStatuses = state.sourceStatuses + healthMap.mapValues { it.value.status })
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    /**
     * Get cached status for a source
     */
    fun getSourceStatus(sourceId: Long): SourceStatus? {
        return _state.value.sourceStatuses[sourceId]
    }
    
    /**
     * Check if a source is currently loading
     */
    fun isSourceLoading(sourceId: Long): Boolean {
        return sourceId in _state.value.loadingSources
    }
    
    // ==================== Source Credentials ====================
    
    /**
     * Login to a source
     */
    fun loginToSource(sourceId: Long, username: String, password: String) {
        scope.launch(ioDispatcher) {
            try {
                sourceCredentialsRepository.storeCredentials(sourceId, username, password)
                checkSourceHealth(sourceId)
                showSnackBar(UiText.DynamicString("Login successful"))
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Login failed: ${e.message ?: "Unknown error"}"))
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
     * Logout from a source
     */
    fun logoutFromSource(sourceId: Long) {
        scope.launch(ioDispatcher) {
            try {
                sourceCredentialsRepository.removeCredentials(sourceId)
                checkSourceHealth(sourceId)
                showSnackBar(UiText.DynamicString("Logged out successfully"))
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Logout failed: ${e.message ?: "Unknown error"}"))
            }
        }
    }
    
    // ==================== Repository Management ====================
    
    /**
     * Add a repository from URL
     */
    fun addRepository(url: String) {
        scope.launch {
            try {
                val repositoryInfo = parseRepositoryUrl(url)
                
                // Disable all existing repositories
                val existingRepos = catalogSourceRepository.subscribe().first()
                existingRepos.filter { it.isEnable }.forEach { repo ->
                    catalogSourceRepository.update(repo.copy(isEnable = false))
                }
                
                val extensionSource = ExtensionSource(
                    id = 0,
                    name = repositoryInfo.name,
                    key = repositoryInfo.url,
                    owner = repositoryInfo.owner,
                    source = repositoryInfo.source,
                    isEnable = true,
                    repositoryType = repositoryInfo.type
                )
                
                catalogSourceRepository.insert(extensionSource)
                
                val insertedRepo = catalogSourceRepository.subscribe().first()
                    .firstOrNull { it.key == repositoryInfo.url }
                if (insertedRepo != null) {
                    uiPreferences.defaultRepository().set(insertedRepo.id)
                }
                
                showSnackBar(UiText.DynamicString("Repository added successfully"))
                refreshCatalogs()
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Failed to add repository: ${e.message ?: "Unknown error"}"))
            }
        }
    }
    
    private fun parseRepositoryUrl(url: String): RepositoryInfo {
        return when {
            url.contains("lnreader-plugins") -> RepositoryInfo(
                name = "LNReader Plugins",
                url = url,
                owner = "LNReader",
                source = "https://github.com/LNReader/lnreader-plugins",
                type = "LNREADER"
            )
            url.contains("IReader-extensions") -> RepositoryInfo(
                name = "IReader Extensions",
                url = url,
                owner = "IReaderorg",
                source = "https://github.com/IReaderorg/IReader-extensions",
                type = "IREADER"
            )
            else -> RepositoryInfo(
                name = "Custom Repository",
                url = url,
                owner = "Unknown",
                source = url,
                type = "IREADER"
            )
        }
    }
    
    private data class RepositoryInfo(
        val name: String,
        val url: String,
        val owner: String,
        val source: String,
        val type: String
    )
    
    /**
     * Get repository type display name
     */
    fun getRepositoryTypeDisplayName(): String {
        return when (_state.value.selectedRepositoryType) {
            "IREADER" -> "IReader"
            "LNREADER" -> "LNReader"
            else -> "All"
        }
    }
    
    // ==================== Dialog Management ====================
    
    fun showDialog(dialog: ExtensionDialog) {
        currentDialog = dialog
    }
    
    fun dismissDialog() {
        currentDialog = ExtensionDialog.None
    }
    
    // ==================== Enhanced Extension Management ====================
    
    fun getExtensionSecurity(catalog: Catalog) {
        scope.launch(ioDispatcher) {
            try {
                extensionSecurityManager?.scanExtension(catalog)
                showSnackBar(UiText.DynamicString("Security scan complete"))
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Security scan failed: ${e.message ?: "Unknown error"}"))
            }
        }
    }
    
    suspend fun getExtensionStatistics(extensionId: Long): ExtensionStatistics? {
        return try {
            extensionManager?.getExtensionStatistics(extensionId)
        } catch (e: Exception) {
            null
        }
    }
    
    fun setExtensionTrustLevel(extensionId: Long, trustLevel: ExtensionTrustLevel) {
        scope.launch(ioDispatcher) {
            try {
                extensionSecurityManager?.setTrustLevel(extensionId, trustLevel)
                showSnackBar(UiText.DynamicString("Trust level updated"))
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Failed to update trust level: ${e.message ?: "Unknown error"}"))
            }
        }
    }
    
    /**
     * Batch update all extensions.
     * Delegates to ExtensionController when available.
     * Requirements: 3.2, 3.3
     */
    fun batchUpdateExtensions() {
        // Use controller if available
        if (extensionController != null) {
            extensionController.dispatch(ExtensionCommand.BatchUpdateExtensions)
            return
        }
        
        // Fallback to direct implementation
        scope.launch(ioDispatcher) {
            try {
                val currentState = _state.value
                val installed = (currentState.pinnedCatalogs + currentState.unpinnedCatalogs)
                    .filterIsInstance<CatalogInstalled>()
                extensionManager?.batchUpdateExtensions(installed)?.onSuccess { results ->
                    val successCount = results.values.count { it.isSuccess }
                    showSnackBar(UiText.DynamicString("Updated $successCount of ${installed.size} extensions"))
                }
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Batch update failed: ${e.message ?: "Unknown error"}"))
            }
        }
    }
    
    /**
     * Check for extension updates.
     * Delegates to ExtensionController when available.
     * Requirements: 3.2, 3.3
     */
    fun checkForExtensionUpdates() {
        // Use controller if available
        if (extensionController != null) {
            extensionController.dispatch(ExtensionCommand.CheckUpdates)
            return
        }
        
        // Fallback to direct implementation
        scope.launch(ioDispatcher) {
            try {
                val updates = extensionManager?.checkForUpdates()
                if (updates != null && updates.isNotEmpty()) {
                    showSnackBar(UiText.DynamicString("${updates.size} updates available"))
                } else {
                    showSnackBar(UiText.DynamicString("All extensions up to date"))
                }
            } catch (e: Exception) {
                showSnackBar(UiText.DynamicString("Update check failed: ${e.message ?: "Unknown error"}"))
            }
        }
    }
    
    fun trackExtensionUsage(extensionId: Long) {
        scope.launch(ioDispatcher) {
            try {
                extensionManager?.trackExtensionUsage(extensionId)
            } catch (e: Exception) {
                // Silent failure
            }
        }
    }
    
    fun reportExtensionError(extensionId: Long, error: Throwable) {
        scope.launch(ioDispatcher) {
            try {
                extensionManager?.reportExtensionError(extensionId, error)
            } catch (e: Exception) {
                // Silent failure
            }
        }
    }
    
    // ==================== Utility Functions ====================
    
    private fun getLanguageChoices(
        remote: List<CatalogRemote>,
        local: List<CatalogLocal>,
    ): List<LanguageChoice> {
        val knownLanguages = mutableListOf<LanguageChoice.One>()
        val unknownLanguages = mutableListOf<Language>()

        val languageComparators = UserLanguagesComparator()
            .then(InstalledLanguagesComparator(local))
            .thenBy { it.code }

        // Combine languages from both remote and local sources
        val allLanguages = (remote.asSequence().map { Language(it.lang) } +
            local.asSequence().mapNotNull { it.source?.lang }.map { Language(it) })
            .distinct()
            .sortedWith(languageComparators)
        
        allLanguages.forEach { code ->
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
        return if (query == null) this else filter { it.name.contains(query, true) }
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
            is LanguageChoice.Others -> source?.lang in choice.languages.map { it.code }
        }
    }
    
}
