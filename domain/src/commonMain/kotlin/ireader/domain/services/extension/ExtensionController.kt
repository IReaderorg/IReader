package ireader.domain.services.extension

import ireader.core.log.Log
import ireader.core.os.InstallStep
import ireader.domain.catalogs.CatalogStore
import ireader.domain.catalogs.interactor.GetCatalogsByType
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.interactor.SyncRemoteCatalogs
import ireader.domain.catalogs.interactor.TogglePinnedCatalog
import ireader.domain.catalogs.interactor.UninstallCatalogs
import ireader.domain.catalogs.interactor.UpdateCatalog
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Extension Controller - The central coordinator for all extension-level operations.
 * 
 * This is the SINGLE SOURCE OF TRUTH for extension-related state across all screens.
 * 
 * Responsibilities:
 * - Owns and manages the ExtensionState (single source of truth)
 * - Processes ExtensionCommands and updates state accordingly
 * - Coordinates between catalog store and use cases for data operations
 * - Emits ExtensionEvents for one-time occurrences
 * 
 * Requirements: 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.3, 4.4, 4.5, 5.1
 */
class ExtensionController(
    private val catalogStore: CatalogStore,
    private val getCatalogsByType: GetCatalogsByType,
    private val installCatalog: InstallCatalog,
    private val uninstallCatalog: UninstallCatalogs,
    private val updateCatalog: UpdateCatalog,
    private val syncRemoteCatalogs: SyncRemoteCatalogs,
    private val togglePinnedCatalog: TogglePinnedCatalog
) {
    companion object {
        private const val TAG = "ExtensionController"
    }
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Mutex to ensure commands are processed sequentially
    private val commandMutex = Mutex()
    
    // State - single source of truth
    // Requirements: 3.5
    private val _state = MutableStateFlow(ExtensionState())
    val state: StateFlow<ExtensionState> = _state.asStateFlow()
    
    // Events - one-time occurrences
    // Requirements: 3.4
    private val _events = MutableSharedFlow<ExtensionEvent>()
    val events: SharedFlow<ExtensionEvent> = _events.asSharedFlow()
    
    // Active subscriptions
    private var catalogSubscriptionJob: Job? = null
    
    // Installation jobs
    private val installerJobs: MutableMap<Long, Job> = mutableMapOf()

    
    /**
     * Process a command - ALL interactions go through here.
     * Commands are processed sequentially using a mutex to prevent race conditions.
     * Requirements: 3.3
     */
    fun dispatch(command: ExtensionCommand) {
        Log.debug { "$TAG: dispatch($command)" }
        
        scope.launch {
            commandMutex.withLock {
                try {
                    processCommand(command)
                } catch (e: Exception) {
                    Log.error(e, "$TAG: Error processing command")
                    handleError(ExtensionError.LoadFailed(e.message ?: "Unknown error"))
                }
            }
        }
    }
    
    private suspend fun processCommand(command: ExtensionCommand) {
        when (command) {
            // Lifecycle
            is ExtensionCommand.LoadExtensions -> loadExtensions()
            is ExtensionCommand.Cleanup -> cleanup()
            
            // Installation
            is ExtensionCommand.InstallExtension -> installExtension(command.catalog)
            is ExtensionCommand.UninstallExtension -> uninstallExtension(command.catalog)
            is ExtensionCommand.UpdateExtension -> updateExtension(command.catalog)
            is ExtensionCommand.CancelInstallation -> cancelInstallation(command.catalog)
            
            // Filters
            is ExtensionCommand.SetFilter -> setFilter(command.filter)
            is ExtensionCommand.SetSearchQuery -> setSearchQuery(command.query)
            is ExtensionCommand.SetRepositoryType -> setRepositoryType(command.repositoryType)
            
            // Updates
            is ExtensionCommand.CheckUpdates -> checkUpdates()
            is ExtensionCommand.RefreshExtensions -> refreshExtensions()
            is ExtensionCommand.BatchUpdateExtensions -> batchUpdateExtensions()
            
            // Catalog operations
            is ExtensionCommand.TogglePinned -> togglePinned(command.catalog)
            is ExtensionCommand.ClearError -> clearError()
        }
    }
    
    // ========== Lifecycle Commands ==========
    
    /**
     * Load extensions and subscribe to reactive updates.
     * Requirements: 3.2
     */
    private suspend fun loadExtensions() {
        Log.debug { "$TAG: loadExtensions()" }
        
        // Cancel existing subscription
        catalogSubscriptionJob?.cancel()
        
        _state.update { it.copy(isLoading = true, error = null) }
        
        try {
            // Subscribe to catalog changes
            catalogSubscriptionJob = scope.launch {
                getCatalogsByType.subscribe(
                    excludeRemoteInstalled = true,
                    repositoryType = _state.value.selectedRepositoryType
                ).collect { catalogs ->
                    val availableLanguages = getAvailableLanguages(catalogs.remote, catalogs.pinned + catalogs.unpinned)
                    val currentState = _state.value
                    
                    // Calculate updatable extensions
                    val updatable = calculateUpdatableExtensions(
                        catalogs.pinned + catalogs.unpinned,
                        catalogs.remote
                    )
                    
                    _state.update { state ->
                        state.copy(
                            allPinnedCatalogs = catalogs.pinned,
                            allUnpinnedCatalogs = catalogs.unpinned,
                            allRemoteCatalogs = catalogs.remote,
                            pinnedCatalogs = filterLocalByLanguageCodes(
                                catalogs.pinned.filteredByQuery(currentState.searchQuery),
                                currentState.selectedLanguageCodes
                            ),
                            unpinnedCatalogs = filterLocalByLanguageCodes(
                                catalogs.unpinned.filteredByQuery(currentState.searchQuery),
                                currentState.selectedLanguageCodes
                            ),
                            remoteCatalogs = filterRemoteByLanguageCodes(
                                catalogs.remote.filteredByQuery(currentState.searchQuery),
                                currentState.selectedLanguageCodes
                            ),
                            installedExtensions = catalogs.pinned + catalogs.unpinned,
                            availableExtensions = catalogs.remote,
                            updatableExtensions = updatable,
                            availableLanguages = availableLanguages,
                            isLoading = false
                        )
                    }
                }
            }
            
            val currentState = _state.value
            _events.emit(ExtensionEvent.ExtensionsLoaded(
                installedCount = currentState.installedCount,
                availableCount = currentState.availableCount
            ))
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to load extensions")
            handleError(ExtensionError.LoadFailed(e.message ?: "Failed to load extensions"))
        }
    }
    
    /**
     * Clean up resources, cancel subscriptions, and reset state.
     */
    private fun cleanup() {
        Log.debug { "$TAG: cleanup()" }
        
        catalogSubscriptionJob?.cancel()
        catalogSubscriptionJob = null
        
        installerJobs.values.forEach { it.cancel() }
        installerJobs.clear()
        
        _state.update { ExtensionState() }
    }
    
    // ========== Installation Commands ==========
    
    /**
     * Install an extension from remote catalog.
     */
    private suspend fun installExtension(catalog: CatalogRemote) {
        Log.debug { "$TAG: installExtension(${catalog.pkgName})" }
        
        if (installerJobs.containsKey(catalog.sourceId)) {
            Log.warn { "$TAG: Installation already in progress for ${catalog.pkgName}" }
            return
        }
        
        installerJobs[catalog.sourceId] = scope.launch {
            try {
                installCatalog.await(catalog).collect { step ->
                    handleInstallStep(catalog.pkgName, step)
                    
                    if (step is InstallStep.Success) {
                        _events.emit(ExtensionEvent.InstallComplete(catalog))
                        refreshCatalogsQuietly()
                    }
                }
            } catch (e: Exception) {
                Log.error(e, "$TAG: Failed to install ${catalog.pkgName}")
                handleError(ExtensionError.InstallFailed(catalog.pkgName, e.message ?: "Unknown error"))
            } finally {
                installerJobs.remove(catalog.sourceId)
            }
        }
    }

    
    /**
     * Uninstall an installed extension.
     */
    private suspend fun uninstallExtension(catalog: CatalogInstalled) {
        Log.debug { "$TAG: uninstallExtension(${catalog.pkgName})" }
        
        try {
            uninstallCatalog.await(catalog)
            
            _state.update { state ->
                state.copy(installSteps = state.installSteps - catalog.pkgName)
            }
            
            _events.emit(ExtensionEvent.UninstallComplete(catalog.pkgName))
            
            // Delay and refresh to allow system to process uninstall
            kotlinx.coroutines.delay(1000)
            refreshCatalogsQuietly()
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to uninstall ${catalog.pkgName}")
            handleError(ExtensionError.UninstallFailed(catalog.pkgName, e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Update an installed extension.
     */
    private suspend fun updateExtension(catalog: CatalogInstalled) {
        Log.debug { "$TAG: updateExtension(${catalog.pkgName})" }
        
        if (installerJobs.containsKey(catalog.sourceId)) {
            Log.warn { "$TAG: Update already in progress for ${catalog.pkgName}" }
            return
        }
        
        installerJobs[catalog.sourceId] = scope.launch {
            try {
                updateCatalog.await(catalog).collect { step ->
                    handleInstallStep(catalog.pkgName, step)
                    
                    if (step is InstallStep.Success) {
                        _events.emit(ExtensionEvent.UpdateComplete(catalog))
                        refreshCatalogsQuietly()
                    }
                }
            } catch (e: Exception) {
                Log.error(e, "$TAG: Failed to update ${catalog.pkgName}")
                handleError(ExtensionError.UpdateFailed(catalog.pkgName, e.message ?: "Unknown error"))
            } finally {
                installerJobs.remove(catalog.sourceId)
            }
        }
    }
    
    /**
     * Cancel an ongoing installation job.
     */
    private fun cancelInstallation(catalog: Catalog) {
        Log.debug { "$TAG: cancelInstallation(${catalog.sourceId})" }
        
        installerJobs[catalog.sourceId]?.cancel()
        installerJobs.remove(catalog.sourceId)
        
        val pkgName = when (catalog) {
            is CatalogRemote -> catalog.pkgName
            is CatalogInstalled -> catalog.pkgName
            else -> return
        }
        
        _state.update { state ->
            state.copy(installSteps = state.installSteps + (pkgName to InstallStep.Idle))
        }
    }
    
    private suspend fun handleInstallStep(pkgName: String, step: InstallStep) {
        if (step is InstallStep.Error) {
            _events.emit(ExtensionEvent.ShowSnackbar(step.error))
        }
        
        _state.update { state ->
            state.copy(
                installSteps = if (step != InstallStep.Success) {
                    state.installSteps + (pkgName to step)
                } else {
                    state.installSteps - pkgName
                }
            )
        }
    }
    
    // ========== Filter Commands ==========
    
    /**
     * Set the filter for extensions.
     */
    private fun setFilter(filter: ExtensionFilter) {
        Log.debug { "$TAG: setFilter($filter)" }
        
        val languageCodes = when (filter) {
            is ExtensionFilter.All -> null
            is ExtensionFilter.ByLanguage -> filter.languageCodes
            is ExtensionFilter.ByRepository -> _state.value.selectedLanguageCodes
            is ExtensionFilter.Combined -> filter.languageCodes ?: _state.value.selectedLanguageCodes
        }
        
        val repositoryType = when (filter) {
            is ExtensionFilter.All -> null
            is ExtensionFilter.ByLanguage -> _state.value.selectedRepositoryType
            is ExtensionFilter.ByRepository -> filter.repositoryType
            is ExtensionFilter.Combined -> filter.repositoryType
        }
        
        _state.update { state ->
            state.copy(
                filter = filter,
                selectedLanguageCodes = languageCodes,
                selectedRepositoryType = repositoryType,
                pinnedCatalogs = filterLocalByLanguageCodes(
                    state.allPinnedCatalogs.filteredByQuery(state.searchQuery),
                    languageCodes
                ),
                unpinnedCatalogs = filterLocalByLanguageCodes(
                    state.allUnpinnedCatalogs.filteredByQuery(state.searchQuery),
                    languageCodes
                ),
                remoteCatalogs = filterRemoteByLanguageCodes(
                    state.allRemoteCatalogs.filteredByQuery(state.searchQuery),
                    languageCodes
                )
            )
        }
    }
    
    /**
     * Set the search query for filtering extensions.
     */
    private fun setSearchQuery(query: String?) {
        Log.debug { "$TAG: setSearchQuery($query)" }
        
        _state.update { state ->
            state.copy(
                searchQuery = query,
                pinnedCatalogs = filterLocalByLanguageCodes(
                    state.allPinnedCatalogs.filteredByQuery(query),
                    state.selectedLanguageCodes
                ),
                unpinnedCatalogs = filterLocalByLanguageCodes(
                    state.allUnpinnedCatalogs.filteredByQuery(query),
                    state.selectedLanguageCodes
                ),
                remoteCatalogs = filterRemoteByLanguageCodes(
                    state.allRemoteCatalogs.filteredByQuery(query),
                    state.selectedLanguageCodes
                )
            )
        }
    }
    
    /**
     * Set the repository type filter.
     */
    private fun setRepositoryType(repositoryType: String?) {
        Log.debug { "$TAG: setRepositoryType($repositoryType)" }
        
        _state.update { it.copy(selectedRepositoryType = repositoryType) }
        
        // Reload extensions with new repository type
        scope.launch {
            loadExtensions()
        }
    }
    
    // ========== Update Commands ==========
    
    /**
     * Check for available extension updates.
     */
    private suspend fun checkUpdates() {
        Log.debug { "$TAG: checkUpdates()" }
        
        _state.update { it.copy(isCheckingUpdates = true, error = null) }
        
        try {
            val currentState = _state.value
            val updatable = calculateUpdatableExtensions(
                currentState.allPinnedCatalogs + currentState.allUnpinnedCatalogs,
                currentState.allRemoteCatalogs
            )
            
            _state.update { it.copy(
                updatableExtensions = updatable,
                isCheckingUpdates = false
            )}
            
            if (updatable.isNotEmpty()) {
                _events.emit(ExtensionEvent.UpdatesAvailable(updatable.size))
            } else {
                _events.emit(ExtensionEvent.AllUpToDate)
            }
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to check for updates")
            handleError(ExtensionError.CheckUpdatesFailed(e.message ?: "Unknown error"))
        }
    }

    
    /**
     * Refresh extensions from remote repositories.
     */
    private suspend fun refreshExtensions() {
        Log.debug { "$TAG: refreshExtensions()" }
        
        _state.update { it.copy(isRefreshing = true, error = null) }
        
        try {
            syncRemoteCatalogs.await(forceRefresh = true, onError = { error ->
                Log.error(error, "$TAG: Error during sync")
            })
            
            _state.update { it.copy(isRefreshing = false) }
            _events.emit(ExtensionEvent.RefreshComplete)
            
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to refresh extensions")
            handleError(ExtensionError.RefreshFailed(e.message ?: "Unknown error"))
        }
    }
    
    /**
     * Batch update all extensions with available updates.
     */
    private suspend fun batchUpdateExtensions() {
        Log.debug { "$TAG: batchUpdateExtensions()" }
        
        val updatable = _state.value.updatableExtensions
        if (updatable.isEmpty()) {
            _events.emit(ExtensionEvent.AllUpToDate)
            return
        }
        
        var successCount = 0
        val totalCount = updatable.size
        
        for (catalog in updatable) {
            try {
                updateCatalog.await(catalog).collect { step ->
                    handleInstallStep(catalog.pkgName, step)
                    if (step is InstallStep.Success) {
                        successCount++
                    }
                }
            } catch (e: Exception) {
                Log.error(e, "$TAG: Failed to update ${catalog.pkgName} during batch update")
            }
        }
        
        refreshCatalogsQuietly()
        _events.emit(ExtensionEvent.BatchUpdateComplete(successCount, totalCount))
    }
    
    // ========== Catalog Operations ==========
    
    /**
     * Toggle pinned status of a catalog.
     */
    private suspend fun togglePinned(catalog: Catalog) {
        Log.debug { "$TAG: togglePinned(${catalog.sourceId})" }
        
        try {
            togglePinnedCatalog.await(catalog)
        } catch (e: Exception) {
            Log.error(e, "$TAG: Failed to toggle pinned status")
        }
    }
    
    /**
     * Refresh catalogs without showing loading indicator.
     */
    private fun refreshCatalogsQuietly() {
        scope.launch {
            try {
                catalogStore.reloadCatalogs()
            } catch (e: Exception) {
                Log.error(e, "$TAG: Failed to refresh catalogs quietly")
            }
        }
    }
    
    // ========== Error Handling ==========
    
    /**
     * Handle errors by updating state and emitting events.
     * Requirements: 4.2, 4.3
     */
    private suspend fun handleError(error: ExtensionError) {
        Log.error { "$TAG: Error - ${error.toUserMessage()}" }
        
        _state.update { 
            it.copy(
                error = error,
                isLoading = false,
                isRefreshing = false,
                isCheckingUpdates = false
            )
        }
        
        _events.emit(ExtensionEvent.Error(error))
    }
    
    /**
     * Clear the current error state.
     * Requirements: 4.5
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    /**
     * Release all resources. Call when the controller is no longer needed.
     */
    fun release() {
        Log.debug { "$TAG: release()" }
        cleanup()
        scope.cancel()
    }
    
    // ========== Utility Functions ==========
    
    private fun calculateUpdatableExtensions(
        installed: List<CatalogLocal>,
        remote: List<CatalogRemote>
    ): List<CatalogInstalled> {
        return installed.filterIsInstance<CatalogInstalled>().filter { local ->
            remote.any { r -> r.pkgName == local.pkgName && r.versionCode > local.versionCode }
        }
    }
    
    /**
     * Get available language codes from catalogs.
     */
    private fun getAvailableLanguages(
        remote: List<CatalogRemote>,
        local: List<CatalogLocal>,
    ): Set<String> {
        val languages = mutableSetOf<String>()
        
        remote.forEach { languages.add(it.lang) }
        local.forEach { it.source?.lang?.let { lang -> languages.add(lang) } }
        
        return languages
    }

    private fun <T : Catalog> List<T>.filteredByQuery(query: String?): List<T> {
        return if (query == null) this else filter { it.name.contains(query, true) }
    }

    private fun filterRemoteByLanguageCodes(list: List<CatalogRemote>, codes: Set<String>?): List<CatalogRemote> {
        return if (codes == null || codes.isEmpty()) list else list.filter { it.lang in codes }
    }

    private fun filterLocalByLanguageCodes(list: List<CatalogLocal>, codes: Set<String>?): List<CatalogLocal> {
        return if (codes == null || codes.isEmpty()) list else list.filter { it.source?.lang in codes }
    }
}
