package ireader.domain.catalogs

import ireader.core.source.LocalSource
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class CatalogStore(
    private val loader: CatalogLoader,
    catalogPreferences: CatalogPreferences,
    catalogRemoteRepository: CatalogRemoteRepository,
    installationChanges: CatalogInstallationChanges,
    private val localCatalogSource: LocalCatalogSource,
) {

    private val scope = createICoroutineScope()
    
    // Track which sources are stubs
    private val stubSourceIds = mutableSetOf<Long>()
    
    // Track which sources are currently loading
    private val loadingSourceIds = mutableSetOf<Long>()
    private val loadingSourcesFlow = MutableStateFlow<Set<Long>>(emptySet())

    var catalogs = emptyList<CatalogLocal>()
        private set(value) {
            field = value
            updatableCatalogs = field.asSequence()
                .filterIsInstance<CatalogInstalled>()
                .filter { it.hasUpdate }
                .toList()
            catalogsBySource = field.associateBy { it.sourceId }
            catalogsFlow.value = field
        }

    var updatableCatalogs = emptyList<CatalogInstalled>()
        private set

    private var remoteCatalogs = emptyList<CatalogRemote>()

    private var catalogsBySource = emptyMap<Long, CatalogLocal>()

    private val catalogsFlow = MutableStateFlow(catalogs)

    private val pinnedCatalogsPreference = catalogPreferences.pinnedCatalogs()

    private val lock = Mutex()

    init {
        scope.launch {
            val loadedCatalogs = loader.loadAll().distinctBy { it.sourceId }.toSet().toList()
            val pinnedCatalogIds = pinnedCatalogsPreference.get()
            catalogs = loadedCatalogs.map { catalog ->
                // Track stub sources
                if (catalog is ireader.domain.models.entities.JSPluginCatalog && 
                    catalog.source is ireader.domain.js.loader.JSPluginStubSource) {
                    stubSourceIds.add(catalog.sourceId)
                }
                
                if (catalog.sourceId.toString() in pinnedCatalogIds) {
                    catalog.copy(isPinned = true)
                } else {
                    catalog
                }
            }
            
            // Always trigger background loading for JS plugins
            // On first run (no stubs): loads all plugins from scratch
            // On subsequent runs (with stubs): replaces stubs with actual plugins
            startBackgroundPluginLoading()
        }
        scope.launch {
            installationChanges.flow
                .collect { change ->
                    when (change) {
                        is CatalogInstallationChange.SystemInstall -> onInstalled(
                            change.pkgName,
                            false
                        )
                        is CatalogInstallationChange.SystemUninstall -> onUninstalled(
                            change.pkgName,
                            false
                        )
                        is CatalogInstallationChange.LocalInstall -> onInstalled(
                            change.pkgName,
                            true
                        )
                        is CatalogInstallationChange.LocalUninstall -> onUninstalled(
                            change.pkgName,
                            true
                        )
                    }
                }
        }
        scope.launch {
            catalogRemoteRepository.getRemoteCatalogsFlow()
                .collect {
                    remoteCatalogs = it
                    lock.withLock {
                        catalogs = catalogs.map { catalog ->
                            if (catalog is CatalogInstalled) {
                                val hasUpdate = catalog.checkHasUpdate()
                                if (catalog.hasUpdate != hasUpdate) {
                                    return@map catalog.copy(hasUpdate = hasUpdate)
                                }
                            }
                            catalog
                        }
                    }
                }
        }
    }

    fun get(sourceId: Long?): CatalogLocal? {
        if (sourceId == -200L) {
            return CatalogBundled(source = localCatalogSource)
        }
        return catalogsBySource[sourceId]
    }

    fun getCatalogsFlow(): Flow<List<CatalogLocal>> {
        return catalogsFlow
    }

    suspend fun togglePinnedCatalog(sourceId: Long) {
        withContext(Dispatchers.Default) {
            lock.withLock {
                val position = catalogs.indexOfFirst { it.sourceId == sourceId }.takeIf { it >= 0 }
                    ?: return@withContext

                val catalog = catalogs[position]
                val pinnedCatalogs = pinnedCatalogsPreference.get()
                val key = catalog.sourceId.toString()
                if (catalog.isPinned) {
                    pinnedCatalogsPreference.set(pinnedCatalogs - key)
                } else {
                    pinnedCatalogsPreference.set(pinnedCatalogs + key)
                }
                catalogs = catalogs.replace(position, catalog.copy(isPinned = !catalog.isPinned))
            }
        }
    }

    private fun onInstalled(pkgName: String, isLocalInstall: Boolean) {
        scope.launch(Dispatchers.Default) {
            lock.withLock {
                ireader.core.log.Log.warn("CatalogStore: onInstalled called for $pkgName (isLocal: $isLocalInstall)")
                val previousCatalog =
                    catalogs.find { (it as? CatalogInstalled)?.pkgName == pkgName }

                // Don't replace system catalogs with local catalogs
                if (!isLocalInstall && previousCatalog is CatalogInstalled.Locally) {
                    ireader.core.log.Log.warn("CatalogStore: Skipping - don't replace system with local")
                    return@launch
                }

                val catalog = if (isLocalInstall) {
                    loader.loadLocalCatalog(pkgName)
                } else {
                    loader.loadSystemCatalog(pkgName)
                }?.let { catalog ->
                    val isPinned = catalog.sourceId.toString() in pinnedCatalogsPreference.get()
                    val hasUpdate = catalog.checkHasUpdate()
                    if (isPinned || hasUpdate) {
                        catalog.copy(isPinned = isPinned, hasUpdate = hasUpdate)
                    } else {
                        catalog
                    }
                }
                
                // If catalog is null, it might be a JS plugin - trigger background loading
                if (catalog == null) {
                    ireader.core.log.Log.warn("CatalogStore: Catalog is null (likely JS plugin), triggering background loading")
                    
                    // For JS plugins, we need to load them in the background
                    // Don't reload all catalogs as that would only get stubs
                    // Instead, trigger background loading which will add the new plugin
                    val asyncLoader = loader as? ireader.domain.catalogs.service.AsyncPluginLoader
                    if (asyncLoader != null) {
                        scope.launch {
                            try {
                                // Load just this plugin in background
                                asyncLoader.loadJSPluginsAsync { newCatalog ->
                                    // Check if this is the plugin we're looking for
                                    if (newCatalog.pkgName == pkgName) {
                                        scope.launch {
                                            lock.withLock {
                                                // Check if it already exists (might be a stub)
                                                val existingPosition = catalogs.indexOfFirst { it.sourceId == newCatalog.sourceId }
                                                if (existingPosition >= 0) {
                                                    // Replace existing (stub or old version)
                                                    val existing = catalogs[existingPosition]
                                                    val updated = newCatalog.copy(isPinned = existing.isPinned)
                                                    catalogs = catalogs.replace(existingPosition, updated)
                                                    stubSourceIds.remove(newCatalog.sourceId)
                                                    ireader.core.log.Log.info("CatalogStore: Replaced existing catalog for $pkgName")
                                                } else {
                                                    // Add new plugin
                                                    val pinnedCatalogIds = pinnedCatalogsPreference.get()
                                                    val isPinned = newCatalog.sourceId.toString() in pinnedCatalogIds
                                                    val updated = newCatalog.copy(isPinned = isPinned)
                                                    catalogs = catalogs + updated
                                                    ireader.core.log.Log.info("CatalogStore: Added new JS plugin $pkgName")
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                ireader.core.log.Log.error("CatalogStore: Failed to load new JS plugin", e)
                            }
                        }
                    }
                    return@launch
                }

                val newInstalledCatalogs = catalogs.toMutableList()
                if (previousCatalog != null) {
                    newInstalledCatalogs -= previousCatalog
                }
                newInstalledCatalogs += catalog
                catalogs = newInstalledCatalogs
            }
        }
    }

    private fun onUninstalled(pkgName: String, isLocalInstall: Boolean) {
        scope.launch(Dispatchers.Default) {
            lock.withLock {
                val installedCatalog =
                    catalogs.find { (it as? CatalogInstalled)?.pkgName == pkgName }
                
                // For JS plugins (JSPluginCatalog), just remove by pkgName match
                if (installedCatalog != null) {
                    // Check if it's a traditional installed catalog or JS plugin
                    val shouldRemove = when {
                        installedCatalog is CatalogInstalled.Locally && isLocalInstall -> true
                        installedCatalog is CatalogInstalled.SystemWide && !isLocalInstall -> true
                        // JS plugins are neither Locally nor SystemWide, so remove by pkgName match
                        installedCatalog !is CatalogInstalled.Locally && 
                        installedCatalog !is CatalogInstalled.SystemWide -> true
                        else -> false
                    }
                    
                    if (shouldRemove) {
                        catalogs = catalogs - installedCatalog
                    }
                }
            }
        }
    }

    private fun CatalogInstalled.checkHasUpdate(): Boolean {
        val remoteCatalog = remoteCatalogs.find { it.pkgName == pkgName } ?: return false
        return remoteCatalog.versionCode > versionCode
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
     * Used when background loading completes.
     */
    suspend fun replaceStubSource(catalog: ireader.domain.models.entities.JSPluginCatalog) {
        withContext(Dispatchers.Default) {
            lock.withLock {
                ireader.core.log.Log.info("CatalogStore: Attempting to replace stub for ${catalog.name} (sourceId: ${catalog.sourceId})")
                val position = catalogs.indexOfFirst { it.sourceId == catalog.sourceId }
                if (position >= 0) {
                    val existingCatalog = catalogs[position]
                    val isPinned = existingCatalog.isPinned
                    val hasUpdate = if (existingCatalog is CatalogInstalled) {
                        existingCatalog.hasUpdate
                    } else false
                    
                    // Replace with actual catalog, preserving pinned/update state
                    val updatedCatalog = catalog.copy(isPinned = isPinned, hasUpdate = hasUpdate)
                    catalogs = catalogs.replace(position, updatedCatalog)
                    
                    // Remove from stub tracking
                    stubSourceIds.remove(catalog.sourceId)
                    
                    ireader.core.log.Log.info("CatalogStore: Successfully replaced stub source ${catalog.name} at position $position")
                } else {
                    ireader.core.log.Log.warn("CatalogStore: Could not find stub for ${catalog.name} (sourceId: ${catalog.sourceId}) in catalog list")
                    ireader.core.log.Log.warn("CatalogStore: Current catalog count: ${catalogs.size}, stub count: ${stubSourceIds.size}")
                }
            }
        }
    }
    
    /**
     * Check if a source is currently a stub.
     */
    fun isStubSource(sourceId: Long): Boolean {
        return sourceId in stubSourceIds
    }
    
    /**
     * Check if a source is currently loading.
     */
    fun isLoadingSource(sourceId: Long): Boolean {
        return sourceId in loadingSourceIds
    }
    
    /**
     * Get flow of loading source IDs for UI updates.
     */
    fun getLoadingSourcesFlow(): Flow<Set<Long>> {
        return loadingSourcesFlow
    }
    
    /**
     * Start background loading of actual plugins.
     * On first run: loads all plugins and adds them to catalog list
     * On subsequent runs: replaces stubs with actual loaded plugins
     */
    private fun startBackgroundPluginLoading() {
        scope.launch {
            try {
                // Check if loader supports async loading
                val asyncLoader = loader as? ireader.domain.catalogs.service.AsyncPluginLoader
                
                if (asyncLoader != null) {
                    if (stubSourceIds.isNotEmpty()) {
                        // We have stubs, mark them as loading
                        loadingSourceIds.addAll(stubSourceIds)
                        loadingSourcesFlow.value = loadingSourceIds.toSet()
                        
                        // Replace stubs as plugins load
                        ireader.core.log.Log.info("CatalogStore: Starting background loading to replace ${stubSourceIds.size} stub sources")
                        asyncLoader.loadJSPluginsAsync { catalog ->
                            ireader.core.log.Log.info("CatalogStore: Background loading callback received for ${catalog.name}")
                            scope.launch {
                                replaceStubSource(catalog)
                                
                                // Remove from loading set
                                loadingSourceIds.remove(catalog.sourceId)
                                loadingSourcesFlow.value = loadingSourceIds.toSet()
                                ireader.core.log.Log.info("CatalogStore: Removed ${catalog.name} from loading set")
                            }
                        }
                    } else {
                        // First run, no stubs - add plugins as they load
                        ireader.core.log.Log.info("CatalogStore: Starting background loading for first run")
                        asyncLoader.loadJSPluginsAsync { catalog ->
                            ireader.core.log.Log.info("CatalogStore: Background loading callback received for ${catalog.name} (first run)")
                            scope.launch {
                                lock.withLock {
                                    // Add the newly loaded plugin to the catalog list
                                    val pinnedCatalogIds = pinnedCatalogsPreference.get()
                                    val isPinned = catalog.sourceId.toString() in pinnedCatalogIds
                                    val updatedCatalog = catalog.copy(isPinned = isPinned)
                                    
                                    catalogs = catalogs + updatedCatalog
                                    ireader.core.log.Log.info("CatalogStore: Added plugin ${catalog.name} to catalog list (total: ${catalogs.size})")
                                }
                            }
                        }
                    }
                } else {
                    ireader.core.log.Log.warn("CatalogStore: Async loading not supported on this platform")
                }
            } catch (e: Exception) {
                ireader.core.log.Log.error("CatalogStore: Failed to load plugins in background", e)
            }
        }
    }
}
