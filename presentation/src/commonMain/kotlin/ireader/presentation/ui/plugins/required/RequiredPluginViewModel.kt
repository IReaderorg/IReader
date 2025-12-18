package ireader.presentation.ui.plugins.required

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginManager
import ireader.domain.plugins.PluginRepositoryIndexFetcher
import ireader.domain.plugins.PluginRepositoryRepository
import ireader.domain.plugins.PluginStatus
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for Required Plugin installation screen
 * Handles downloading and installing required plugins like JS Engine or Piper TTS
 */
class RequiredPluginViewModel(
    private val pluginManager: PluginManager,
    private val repositoryRepository: PluginRepositoryRepository,
    private val indexFetcher: PluginRepositoryIndexFetcher,
    private val catalogStore: ireader.domain.catalogs.CatalogStore
) : BaseViewModel() {

    private val _state = mutableStateOf(RequiredPluginState(pluginType = RequiredPluginType.JS_ENGINE))
    val state: State<RequiredPluginState> = _state

    private var targetPluginInfo: PluginInfo? = null

    /**
     * Initialize the ViewModel with the required plugin type
     */
    fun initialize(pluginType: RequiredPluginType) {
        _state.value = RequiredPluginState(pluginType = pluginType, isLoading = true)
        checkPluginStatus(pluginType)
    }

    /**
     * Check if the required plugin is already installed
     */
    private fun checkPluginStatus(pluginType: RequiredPluginType) {
        scope.launch {
            try {
                val pluginId = getPluginId(pluginType)
                
                // Check if already installed
                val installedPlugins = pluginManager.pluginsFlow.value
                val installedPlugin = installedPlugins.find { it.id == pluginId }
                
                if (installedPlugin != null) {
                    val isEnabled = installedPlugin.status == PluginStatus.ENABLED
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isInstalled = true,
                        isEnabled = isEnabled,
                        pluginInfo = installedPlugin.toDisplayInfo()
                    )
                    return@launch
                }
                
                // Not installed, fetch from repository
                fetchPluginFromRepository(pluginType)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to check plugin status: ${e.message}"
                )
            }
        }
    }

    /**
     * Fetch plugin info from repository
     */
    private suspend fun fetchPluginFromRepository(pluginType: RequiredPluginType) {
        try {
            val pluginId = getPluginId(pluginType)
            val repositories = repositoryRepository.getEnabled().first()
            
            if (repositories.isEmpty()) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "No plugin repositories configured. Please add a repository first."
                )
                return
            }
            
            // Search for the plugin in all repositories
            for (repo in repositories) {
                try {
                    val indexResult = indexFetcher.fetchIndex(repo.url)
                    indexResult.onSuccess { index ->
                        val entry = index.plugins.find { it.id == pluginId }
                        if (entry != null) {
                            // Resolve download URL
                            val baseUrl = repo.url.substringBeforeLast("/")
                            val downloadUrl = if (entry.downloadUrl.startsWith("http")) {
                                entry.downloadUrl
                            } else if (entry.downloadUrl.startsWith("/")) {
                                "$baseUrl${entry.downloadUrl}"
                            } else {
                                "$baseUrl/${entry.downloadUrl}"
                            }
                            
                            targetPluginInfo = PluginInfo(
                                id = entry.id,
                                manifest = ireader.plugin.api.PluginManifest(
                                    id = entry.id,
                                    name = entry.name,
                                    version = entry.version,
                                    versionCode = entry.versionCode,
                                    description = entry.description,
                                    author = ireader.plugin.api.PluginAuthor(
                                        name = entry.author.name,
                                        email = entry.author.email,
                                        website = entry.author.website
                                    ),
                                    type = ireader.domain.plugins.PluginType.JS_ENGINE,
                                    permissions = emptyList(),
                                    minIReaderVersion = entry.minIReaderVersion,
                                    platforms = emptyList(),
                                    iconUrl = entry.iconUrl,
                                    monetization = ireader.plugin.api.PluginMonetization.Free
                                ),
                                status = PluginStatus.NOT_INSTALLED,
                                installDate = null,
                                rating = null,
                                downloadCount = 0,
                                repositoryUrl = repo.url,
                                downloadUrl = downloadUrl,
                                fileSize = entry.fileSize,
                                checksum = entry.checksum
                            )
                            
                            _state.value = _state.value.copy(
                                isLoading = false,
                                pluginInfo = PluginDisplayInfo(
                                    id = entry.id,
                                    name = entry.name,
                                    version = entry.version,
                                    description = entry.description,
                                    fileSize = entry.fileSize,
                                    author = entry.author.name
                                )
                            )
                            return
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next repository
                }
            }
            
            // Plugin not found in any repository
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Plugin not found in any repository. Please check your repository configuration."
            )
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                error = "Failed to fetch plugin info: ${e.message}"
            )
        }
    }

    /**
     * Install the required plugin
     */
    fun installPlugin() {
        val plugin = targetPluginInfo ?: run {
            _state.value = _state.value.copy(error = "Plugin info not available")
            return
        }
        
        _state.value = _state.value.copy(
            isDownloading = true,
            downloadProgress = 0f,
            error = null
        )
        
        scope.launch {
            try {
                // Simulate progress updates (actual progress would come from download)
                // For now, we'll show indeterminate progress
                _state.value = _state.value.copy(downloadProgress = 0.1f)
                
                pluginManager.installPlugin(plugin)
                    .onSuccess { installedPlugin ->
                        _state.value = _state.value.copy(
                            isDownloading = false,
                            downloadProgress = 1f,
                            isInstalled = true,
                            isEnabled = false, // Need to enable after install
                            pluginInfo = installedPlugin.toDisplayInfo()
                        )
                    }
                    .onFailure { error ->
                        _state.value = _state.value.copy(
                            isDownloading = false,
                            downloadProgress = 0f,
                            error = "Installation failed: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isDownloading = false,
                    downloadProgress = 0f,
                    error = "Installation failed: ${e.message}"
                )
            }
        }
    }

    /**
     * Enable the installed plugin
     */
    fun enablePlugin() {
        val pluginId = getPluginId(_state.value.pluginType)
        
        _state.value = _state.value.copy(isLoading = true, error = null)
        
        scope.launch {
            try {
                pluginManager.enablePlugin(pluginId)
                    .onSuccess {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isEnabled = true
                        )
                        
                        // If this is a JS engine plugin, trigger retry of JS plugin loading
                        if (_state.value.pluginType == RequiredPluginType.JS_ENGINE ||
                            _state.value.pluginType == RequiredPluginType.GRAALVM_ENGINE) {
                            catalogStore.retryJSPluginLoading()
                        }
                    }
                    .onFailure { error ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = "Failed to enable plugin: ${error.message}"
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to enable plugin: ${e.message}"
                )
            }
        }
    }

    /**
     * Retry the last failed operation
     */
    fun retry() {
        _state.value = _state.value.copy(error = null)
        
        if (!_state.value.isInstalled) {
            // Retry fetching and installing
            _state.value = _state.value.copy(isLoading = true)
            checkPluginStatus(_state.value.pluginType)
        } else if (!_state.value.isEnabled) {
            // Retry enabling
            enablePlugin()
        }
    }

    /**
     * Get plugin ID for the given type
     */
    private fun getPluginId(pluginType: RequiredPluginType): String {
        return when (pluginType) {
            RequiredPluginType.JS_ENGINE -> "io.github.ireaderorg.plugins.j2v8-engine"
            RequiredPluginType.GRAALVM_ENGINE -> "io.github.ireaderorg.plugins.graalvm-engine"
            RequiredPluginType.PIPER_TTS -> "io.github.ireaderorg.plugins.piper-tts"
        }
    }

    /**
     * Convert PluginInfo to PluginDisplayInfo
     */
    private fun PluginInfo.toDisplayInfo(): PluginDisplayInfo {
        return PluginDisplayInfo(
            id = id,
            name = manifest.name,
            version = manifest.version,
            description = manifest.description,
            fileSize = fileSize ?: 0L,
            author = manifest.author.name
        )
    }
}
