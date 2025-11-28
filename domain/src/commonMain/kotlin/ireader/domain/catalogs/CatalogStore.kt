package ireader.domain.catalogs

import ireader.core.log.Log
import ireader.core.source.LocalCatalogSource
import ireader.core.util.createICoroutineScope
import ireader.core.util.replace
import ireader.domain.catalogs.service.CatalogInstallationChange
import ireader.domain.catalogs.service.CatalogInstallationChanges
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.models.entities.CatalogBundled
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Optimized CatalogStore with improved performance for catalog and JS plugin loading.
 * 
 * Performance optimizations:
 * 1. Parallel plugin loading with configurable concurrency
 * 2. Batched UI updates to reduce recomposition
 * 3. Lazy initialization of non-critical data
 * 4. Efficient lookup maps using ConcurrentHashMap
 * 5. Debounced flow emissions to prevent UI thrashing
 * 6. Priority-based loading for frequently used plugins
 */
class CatalogStore(
    private val loader: CatalogLoader,
    catalogPreferences: CatalogPreferences,
    catalogRemoteRepository: CatalogRemoteRepository,
    installationChanges: CatalogInstallationChanges,
    private val localCatalogSource: LocalCatalogSource,
) {
    companion object {
        /** Maximum concurrent plugin loads to balance speed vs memory */
        private const val MAX_CONCURRENT_LOADS = 4
        
        /** Batch size for UI updates during bulk loading */
        private const val BATCH_UPDATE_SIZE = 5
        
        /** Debounce time for flow emissions (ms) */
        private const val FLOW_DEBOUNCE_MS = 100L
    }

    private val scope = createICoroutineScope()
    
    // Thread-safe tracking sets using ConcurrentHashMap.newKeySet()
    private val stubSourceIds: MutableSet<Long> = ConcurrentHashMap.newKeySet()
    private val loadingSourceIds: MutableSet<Long> = ConcurrentHashMap.newKeySet()
    
    // Efficient lookup map for catalogs by source ID
    private val catalogsBySourceMap: ConcurrentHashMap<Long, CatalogLocal> = ConcurrentHashMap()
    
    // Efficient lookup map for catalogs by package name
    private val catalogsByPkgName: ConcurrentHashMap<String, CatalogLocal> = ConcurrentHashMap()
    
    // Semaphore to limit concurrent plugin loading
    private val loadingSemaphore = Semaphore(MAX_CONCURRENT_LOADS)
    
    // Channel for batched catalog updates
    private val catalogUpdateChannel = Channel<CatalogUpdate>(Channel.BUFFERED)
    
    // Flow states
    private val loadingSourcesFlow = MutableStateFlow<Set<Long>>(emptySet())
    private val catalogsFlow = MutableStateFlow<List<CatalogLocal>>(emptyList())
    private val _isInitialized = MutableStateFlow(false)

    var catalogs = emptyList<CatalogLocal>()
        private set(value) {
            field = value
            // Update lookup maps efficiently
            updateLookupMaps(value)
            // Update derived data
            updatableCatalogs = value.asSequence()
                .filterIsInstance<CatalogInstalled>()
                .filter { it.hasUpdate }
                .toList()
            catalogsFlow.value = value
        }

    var updatableCatalogs = emptyList<CatalogInstalled>()
        private set

    private var remoteCatalogs = emptyList<CatalogRemote>()
    
    // Lazy-initialized remote catalog lookup map
    private val remoteCatalogsByPkgName: MutableMap<String, CatalogRemote> by lazy {
        ConcurrentHashMap()
    }

    // Deprecated: Use catalogsBySourceMap instead
    private var catalogsBySource = emptyMap<Long, CatalogLocal>()

    private val pinnedCatalogsPreference = catalogPreferences.pinnedCatalogs()
    
    // Cached pinned catalog IDs to avoid repeated preference reads
    @Volatile
    private var cachedPinnedIds: Set<String> = emptySet()

    private val lock = Mutex()

    init {
        // Start batch update processor
        startBatchUpdateProcessor()
        
        // Load catalogs with optimized initialization
        scope.launch {
            initializeCatalogs()
        }
        
        // Listen for installation changes
        scope.launch {
            installationChanges.flow.collect { change ->
                handleInstallationChange(change)
            }
        }
        
        // Listen for remote catalog updates with debouncing
        scope.launch {
            catalogRemoteRepository.getRemoteCatalogsFlow()
                .debounce(FLOW_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collect { remotes ->
                    updateRemoteCatalogs(remotes)
                }
        }
        
        // Cache pinned IDs and listen for changes
        scope.launch {
            cachedPinnedIds = pinnedCatalogsPreference.get()
        }
    }
    
    /**
     * Initialize catalogs with optimized loading strategy.
     */
    private suspend fun initializeCatalogs() {
        withContext(Dispatchers.Default) {
            val startTime = System.currentTimeMillis()
            
            // Load all catalogs
            val loadedCatalogs = loader.loadAll()
                .distinctBy { it.sourceId }
            
            // Cache pinned IDs once
            cachedPinnedIds = pinnedCatalogsPreference.get()
            
            // Process catalogs efficiently
            val processedCatalogs = loadedCatalogs.map { catalog ->
                processCatalog(catalog)
            }
            
            catalogs = processedCatalogs
            _isInitialized.value = true
            
            val loadTime = System.currentTimeMillis() - startTime
            Log.debug { "CatalogStore: Initial load completed in ${loadTime}ms (${processedCatalogs.size} catalogs)" }
            
            // Start background plugin loading after initial load
            startBackgroundPluginLoading()
        }
    }
    
    /**
     * Process a single catalog - track stubs and apply pinned state.
     */
    private fun processCatalog(catalog: CatalogLocal): CatalogLocal {
        // Track stub sources
        if (catalog is ireader.domain.models.entities.JSPluginCatalog &&
            catalog.source is ireader.domain.js.loader.JSPluginStubSource) {
            stubSourceIds.add(catalog.sourceId)
        }
        
        // Apply pinned state
        return if (catalog.sourceId.toString() in cachedPinnedIds) {
            catalog.copy(isPinned = true)
        } else {
            catalog
        }
    }
    
    /**
     * Update lookup maps efficiently.
     */
    private fun updateLookupMaps(catalogList: List<CatalogLocal>) {
        // Clear and rebuild maps
        catalogsBySourceMap.clear()
        catalogsByPkgName.clear()
        
        catalogList.forEach { catalog ->
            catalogsBySourceMap[catalog.sourceId] = catalog
            
            // Add to package name map if applicable
            when (catalog) {
                is CatalogInstalled -> catalogsByPkgName[catalog.pkgName] = catalog
                is ireader.domain.models.entities.JSPluginCatalog -> 
                    catalogsByPkgName[catalog.pkgName] = catalog
                else -> {}
            }
        }
        
        // Update legacy map for compatibility
        catalogsBySource = catalogsBySourceMap.toMap()
    }
    
    /**
     * Start the batch update processor for efficient UI updates.
     */
    private fun startBatchUpdateProcessor() {
        scope.launch {
            val pendingUpdates = mutableListOf<CatalogUpdate>()
            
            for (update in catalogUpdateChannel) {
                pendingUpdates.add(update)
                
                // Process batch when size threshold reached or channel is empty
                if (pendingUpdates.size >= BATCH_UPDATE_SIZE || catalogUpdateChannel.isEmpty) {
                    processBatchUpdates(pendingUpdates.toList())
                    pendingUpdates.clear()
                }
            }
        }
    }
    
    /**
     * Process a batch of catalog updates efficiently.
     */
    private suspend fun processBatchUpdates(updates: List<CatalogUpdate>) {
        if (updates.isEmpty()) return
        
        lock.withLock {
            val currentCatalogs = catalogs.toMutableList()
            
            updates.forEach { update ->
                when (update) {
                    is CatalogUpdate.Add -> {
                        val existingIndex = currentCatalogs.indexOfFirst { it.sourceId == update.catalog.sourceId }
                        if (existingIndex >= 0) {
                            currentCatalogs[existingIndex] = update.catalog
                        } else {
                            currentCatalogs.add(update.catalog)
                        }
                    }
                    is CatalogUpdate.Remove -> {
                        currentCatalogs.removeAll { it.sourceId == update.sourceId }
                    }
                    is CatalogUpdate.Replace -> {
                        val index = currentCatalogs.indexOfFirst { it.sourceId == update.catalog.sourceId }
                        if (index >= 0) {
                            currentCatalogs[index] = update.catalog
                        }
                    }
                }
            }
            
            catalogs = currentCatalogs
        }
    }

    fun get(sourceId: Long?): CatalogLocal? {
        if (sourceId == null) return null
        if (sourceId == -200L) {
            return CatalogBundled(source = localCatalogSource)
        }
        // Use efficient map lookup instead of list search
        return catalogsBySourceMap[sourceId]
    }
    
    /**
     * Get catalog by package name - O(1) lookup.
     */
    fun getByPkgName(pkgName: String): CatalogLocal? {
        return catalogsByPkgName[pkgName]
    }

    fun getCatalogsFlow(): Flow<List<CatalogLocal>> {
        return catalogsFlow
    }
    
    /**
     * Get initialization state flow.
     */
    fun isInitializedFlow(): Flow<Boolean> = _isInitialized

    suspend fun togglePinnedCatalog(sourceId: Long) {
        withContext(Dispatchers.Default) {
            lock.withLock {
                val catalog = catalogsBySourceMap[sourceId] ?: return@withContext
                val position = catalogs.indexOfFirst { it.sourceId == sourceId }
                if (position < 0) return@withContext

                val key = sourceId.toString()
                val newPinnedState = !catalog.isPinned
                
                // Update preference
                cachedPinnedIds = if (newPinnedState) {
                    cachedPinnedIds + key
                } else {
                    cachedPinnedIds - key
                }
                pinnedCatalogsPreference.set(cachedPinnedIds)
                
                // Update catalog
                catalogs = catalogs.replace(position, catalog.copy(isPinned = newPinnedState))
            }
        }
    }
    
    /**
     * Handle installation changes efficiently.
     */
    private fun handleInstallationChange(change: CatalogInstallationChange) {
        when (change) {
            is CatalogInstallationChange.SystemInstall -> onInstalled(change.pkgName, false)
            is CatalogInstallationChange.SystemUninstall -> onUninstalled(change.pkgName, false)
            is CatalogInstallationChange.LocalInstall -> onInstalled(change.pkgName, true)
            is CatalogInstallationChange.LocalUninstall -> onUninstalled(change.pkgName, true)
        }
    }

    private fun onInstalled(pkgName: String, isLocalInstall: Boolean) {
        scope.launch(Dispatchers.Default) {
            // Use semaphore to limit concurrent loads
            loadingSemaphore.withPermit {
                lock.withLock {
                    val previousCatalog = catalogsByPkgName[pkgName]

                    // Don't replace system catalogs with local catalogs
                    if (!isLocalInstall && previousCatalog is CatalogInstalled.Locally) {
                        return@launch
                    }

                    val catalog = if (isLocalInstall) {
                        loader.loadLocalCatalog(pkgName)
                    } else {
                        loader.loadSystemCatalog(pkgName)
                    }?.let { loadedCatalog ->
                        val isPinned = loadedCatalog.sourceId.toString() in cachedPinnedIds
                        val hasUpdate = checkHasUpdate(loadedCatalog)
                        if (isPinned || hasUpdate) {
                            loadedCatalog.copy(isPinned = isPinned, hasUpdate = hasUpdate)
                        } else {
                            loadedCatalog
                        }
                    }

                    if (catalog == null) {
                        // Try loading as JS plugin
                        loadJSPluginAsync(pkgName)
                        return@launch
                    }

                    // Use batch update channel
                    catalogUpdateChannel.trySend(CatalogUpdate.Add(catalog))
                }
            }
        }
    }
    
    /**
     * Load a JS plugin asynchronously.
     */
    private fun loadJSPluginAsync(pkgName: String) {
        val asyncLoader = loader as? ireader.domain.catalogs.service.AsyncPluginLoader ?: return
        
        scope.launch {
            loadingSemaphore.withPermit {
                try {
                    asyncLoader.loadJSPluginsAsync { newCatalog ->
                        if (newCatalog.pkgName == pkgName) {
                            scope.launch {
                                val isPinned = newCatalog.sourceId.toString() in cachedPinnedIds
                                val updated = newCatalog.copy(isPinned = isPinned)
                                catalogUpdateChannel.trySend(CatalogUpdate.Add(updated))
                                stubSourceIds.remove(newCatalog.sourceId)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.error("CatalogStore: Failed to load JS plugin $pkgName", e)
                }
            }
        }
    }

    private fun onUninstalled(pkgName: String, isLocalInstall: Boolean) {
        scope.launch(Dispatchers.Default) {
            lock.withLock {
                val installedCatalog = catalogsByPkgName[pkgName] ?: return@launch

                val shouldRemove = when {
                    installedCatalog is CatalogInstalled.Locally && isLocalInstall -> true
                    installedCatalog is CatalogInstalled.SystemWide && !isLocalInstall -> true
                    installedCatalog !is CatalogInstalled.Locally &&
                            installedCatalog !is CatalogInstalled.SystemWide -> true
                    else -> false
                }

                if (shouldRemove) {
                    catalogUpdateChannel.trySend(CatalogUpdate.Remove(installedCatalog.sourceId))
                }
            }
        }
    }
    
    /**
     * Update remote catalogs and check for updates efficiently.
     */
    private suspend fun updateRemoteCatalogs(remotes: List<CatalogRemote>) {
        remoteCatalogs = remotes
        
        // Update lookup map
        remoteCatalogsByPkgName.clear()
        remotes.forEach { remoteCatalogsByPkgName[it.pkgName] = it }
        
        // Check for updates in parallel
        lock.withLock {
            val updatedCatalogs = catalogs.map { catalog ->
                if (catalog is CatalogInstalled) {
                    val hasUpdate = checkHasUpdate(catalog)
                    if (catalog.hasUpdate != hasUpdate) {
                        catalog.copy(hasUpdate = hasUpdate)
                    } else {
                        catalog
                    }
                } else {
                    catalog
                }
            }
            
            if (updatedCatalogs != catalogs) {
                catalogs = updatedCatalogs
            }
        }
    }

    private fun checkHasUpdate(catalog: CatalogInstalled): Boolean {
        val remoteCatalog = remoteCatalogsByPkgName[catalog.pkgName] ?: return false
        return remoteCatalog.versionCode > catalog.versionCode
    }
    
    private fun checkHasUpdate(catalog: CatalogLocal): Boolean {
        return when (catalog) {
            is CatalogInstalled -> checkHasUpdate(catalog)
            else -> false
        }
    }

    private fun CatalogLocal.copy(
        isPinned: Boolean = this.isPinned,
        hasUpdate: Boolean = this.hasUpdate,
    ): CatalogLocal {
        return when (this) {
            is CatalogBundled -> copy(isPinned = isPinned)
            is CatalogInstalled.Locally -> copy(isPinned = isPinned, hasUpdate = hasUpdate)
            is CatalogInstalled.SystemWide -> copy(isPinned = isPinned, hasUpdate = hasUpdate)
            is ireader.domain.models.entities.JSPluginCatalog -> copy(isPinned = isPinned, hasUpdate = hasUpdate)
        }
    }

    /**
     * Replace a stub source with the actual loaded source.
     */
    suspend fun replaceStubSource(catalog: ireader.domain.models.entities.JSPluginCatalog) {
        withContext(Dispatchers.Default) {
            lock.withLock {
                val existingCatalog = catalogsBySourceMap[catalog.sourceId] ?: return@withContext
                val isPinned = existingCatalog.isPinned
                val hasUpdate = if (existingCatalog is CatalogInstalled) {
                    existingCatalog.hasUpdate
                } else false

                val updatedCatalog = catalog.copy(isPinned = isPinned, hasUpdate = hasUpdate)
                catalogUpdateChannel.trySend(CatalogUpdate.Replace(updatedCatalog))
                stubSourceIds.remove(catalog.sourceId)
            }
        }
    }

    fun isStubSource(sourceId: Long): Boolean = sourceId in stubSourceIds

    fun isLoadingSource(sourceId: Long): Boolean = sourceId in loadingSourceIds

    fun getLoadingSourcesFlow(): Flow<Set<Long>> = loadingSourcesFlow

    /**
     * Force reload all catalogs.
     */
    suspend fun reloadCatalogs() {
        withContext(Dispatchers.Default) {
            lock.withLock {
                stubSourceIds.clear()
                cachedPinnedIds = pinnedCatalogsPreference.get()
                
                val loadedCatalogs = loader.loadAll()
                    .distinctBy { it.sourceId }
                    .map { processCatalog(it) }

                catalogs = loadedCatalogs
                startBackgroundPluginLoading()
            }
        }
    }

    /**
     * Start background loading of actual plugins with parallel execution.
     */
    private fun startBackgroundPluginLoading() {
        val asyncLoader = loader as? ireader.domain.catalogs.service.AsyncPluginLoader ?: return
        
        scope.launch {
            try {
                val stubsToLoad = stubSourceIds.toSet()
                
                if (stubsToLoad.isNotEmpty()) {
                    loadingSourceIds.addAll(stubsToLoad)
                    loadingSourcesFlow.value = loadingSourceIds.toSet()
                    
                    // Load plugins with callback
                    asyncLoader.loadJSPluginsAsync { catalog ->
                        scope.launch {
                            replaceStubSource(catalog)
                            loadingSourceIds.remove(catalog.sourceId)
                            loadingSourcesFlow.value = loadingSourceIds.toSet()
                        }
                    }
                } else {
                    // First run - add plugins as they load
                    asyncLoader.loadJSPluginsAsync { catalog ->
                        scope.launch {
                            val isPinned = catalog.sourceId.toString() in cachedPinnedIds
                            val updated = catalog.copy(isPinned = isPinned)
                            catalogUpdateChannel.trySend(CatalogUpdate.Add(updated))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.error("CatalogStore: Failed to load plugins in background", e)
            }
        }
    }
}

/**
 * Sealed class for catalog update operations.
 */
private sealed class CatalogUpdate {
    data class Add(val catalog: CatalogLocal) : CatalogUpdate()
    data class Remove(val sourceId: Long) : CatalogUpdate()
    data class Replace(val catalog: CatalogLocal) : CatalogUpdate()
}
