package ireader.presentation.ui.pluginrepository

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.plugins.PluginIndexEntry
import ireader.domain.plugins.PluginRepositoryEntity
import ireader.domain.plugins.PluginRepositoryIndexFetcher
import ireader.domain.plugins.PluginRepositoryRepository
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for Plugin Repository management
 */
class PluginRepositoryViewModel(
    private val repository: PluginRepositoryRepository,
    private val indexFetcher: PluginRepositoryIndexFetcher
) : BaseViewModel() {

    private val _state = mutableStateOf(PluginRepositoryState())
    val state: State<PluginRepositoryState> = _state

    // Cache of fetched plugins per repository
    private val pluginCache = mutableMapOf<String, List<PluginIndexEntry>>()

    init {
        initializeAndLoad()
    }

    private fun initializeAndLoad() {
        scope.launch {
            // Initialize defaults if empty
            repository.initializeDefaults()

            // Observe repositories from database
            repository.getAll().collectLatest { entities ->
                _state.value = _state.value.copy(
                    repositories = entities.map { it.toUiModel() },
                    isLoading = false
                )
            }
        }
    }

    fun addRepository(url: String) {
        scope.launch {
            try {
                val trimmedUrl = url.trim()

                // Check if already exists
                if (repository.getByUrl(trimmedUrl) != null) {
                    _state.value = _state.value.copy(error = "Repository already exists")
                    return@launch
                }

                // Validate URL by fetching index first
                _state.value = _state.value.copy(isRefreshing = true)
                val indexResult = indexFetcher.fetchIndex(trimmedUrl)

                indexResult.onFailure { e ->
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        error = "Invalid repository: ${e.message}"
                    )
                    return@launch
                }

                val index = indexResult.getOrNull()!!
                val repoName = extractRepoName(trimmedUrl, index)

                val entity = PluginRepositoryEntity(
                    url = trimmedUrl,
                    name = repoName,
                    isEnabled = true,
                    isOfficial = false,
                    pluginCount = index.plugins.size,
                    lastUpdated = currentTimeToLong(),
                    createdAt = currentTimeToLong()
                )

                repository.add(entity)
                pluginCache[trimmedUrl] = index.plugins

                _state.value = _state.value.copy(error = null, isRefreshing = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    error = "Failed to add repository: ${e.message}"
                )
            }
        }
    }

    fun removeRepository(url: String) {
        scope.launch {
            try {
                // Check if official (cannot remove)
                val entity = repository.getByUrl(url)
                if (entity?.isOfficial == true) {
                    _state.value = _state.value.copy(error = "Cannot remove official repository")
                    return@launch
                }

                repository.deleteByUrl(url)
                pluginCache.remove(url)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to remove repository: ${e.message}"
                )
            }
        }
    }

    fun toggleRepository(url: String, enabled: Boolean) {
        scope.launch {
            try {
                val entity = repository.getByUrl(url)
                entity?.let {
                    repository.setEnabled(it.id, enabled)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to update repository: ${e.message}"
                )
            }
        }
    }

    fun refreshRepository(url: String) {
        scope.launch {
            _state.value = _state.value.copy(isRefreshing = true)
            try {
                val entity = repository.getByUrl(url) ?: return@launch

                // Fetch repository index
                val indexResult = indexFetcher.fetchIndex(url)

                indexResult.onSuccess { index ->
                    pluginCache[url] = index.plugins

                    repository.updatePluginCount(
                        id = entity.id,
                        count = index.plugins.size,
                        lastUpdated = currentTimeToLong()
                    )

                    _state.value = _state.value.copy(isRefreshing = false)
                }.onFailure { e ->
                    repository.updateError(
                        id = entity.id,
                        error = e.message,
                        lastUpdated = currentTimeToLong()
                    )
                    _state.value = _state.value.copy(
                        isRefreshing = false,
                        error = "Failed to refresh: ${e.message}"
                    )
                }
            } catch (e: Exception) {
                val entity = repository.getByUrl(url)
                entity?.let {
                    repository.updateError(
                        id = it.id,
                        error = e.message,
                        lastUpdated = currentTimeToLong()
                    )
                }
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    error = "Failed to refresh: ${e.message}"
                )
            }
        }
    }

    fun refreshAllRepositories() {
        scope.launch {
            _state.value.repositories.filter { it.enabled }.forEach { repo ->
                refreshRepository(repo.url)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /**
     * Get cached plugins for a repository
     */
    fun getPluginsForRepository(url: String): List<PluginIndexEntry> {
        return pluginCache[url] ?: emptyList()
    }

    /**
     * Get all available plugins from enabled repositories
     */
    fun getAllAvailablePlugins(): List<PluginIndexEntry> {
        return _state.value.repositories
            .filter { it.enabled }
            .flatMap { pluginCache[it.url] ?: emptyList() }
    }

    private fun extractRepoName(url: String, index: ireader.domain.plugins.PluginRepositoryIndex? = null): String {
        // Try to extract a meaningful name from the URL
        return try {
            when {
                url.contains("github.com") -> {
                    val parts = url.removePrefix("https://").removePrefix("http://")
                        .split("/")
                    if (parts.size >= 3) {
                        "${parts[1]}/${parts[2]}"
                    } else {
                        "GitHub Repository"
                    }
                }
                url.contains("raw.githubusercontent.com") -> {
                    val parts = url.removePrefix("https://raw.githubusercontent.com/")
                        .split("/")
                    if (parts.size >= 2) {
                        "${parts[0]}/${parts[1]}"
                    } else {
                        "GitHub Repository"
                    }
                }
                else -> {
                    val domain = url.removePrefix("https://").removePrefix("http://")
                        .substringBefore("/")
                    "Plugins from $domain"
                }
            }
        } catch (e: Exception) {
            "Custom Repository"
        }
    }

    private fun PluginRepositoryEntity.toUiModel(): PluginRepository {
        return PluginRepository(
            id = id,
            url = url,
            name = name,
            enabled = isEnabled,
            pluginCount = pluginCount,
            lastUpdated = lastUpdated,
            isOfficial = isOfficial,
            lastError = lastError
        )
    }
}
