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
                if (catalog.sourceId.toString() in pinnedCatalogIds) {
                    catalog.copy(isPinned = true)
                } else {
                    catalog
                }
            }
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
                val previousCatalog =
                    catalogs.find { (it as? CatalogInstalled)?.pkgName == pkgName }

                // Don't replace system catalogs with local catalogs
                if (!isLocalInstall && previousCatalog is CatalogInstalled.Locally) {
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
                } ?: return@launch

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
                if (installedCatalog != null &&
                    installedCatalog is CatalogInstalled.Locally == isLocalInstall
                ) {
                    catalogs = catalogs - installedCatalog
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
        }
    }
}
