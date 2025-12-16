package ireader.data.plugins

import ireader.domain.plugins.PluginRepositoryEntity
import ireader.domain.plugins.PluginRepositoryRepository
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory implementation of PluginRepositoryRepository.
 * For production with SQLDelight, this would use database queries.
 */
class PluginRepositoryRepositoryImpl : PluginRepositoryRepository {

    private val repositories = MutableStateFlow<List<PluginRepositoryEntity>>(emptyList())
    private var nextId = 1L

    init {
        // Initialize with default repository
        val defaultRepo = PluginRepositoryEntity(
            id = nextId++,
            url = OFFICIAL_REPO_URL,
            name = "Official IReader Plugins",
            isEnabled = true,
            isOfficial = true,
            pluginCount = 0,
            lastUpdated = 0,
            lastError = null,
            createdAt = currentTimeToLong()
        )
        repositories.value = listOf(defaultRepo)
    }

    override fun getAll(): Flow<List<PluginRepositoryEntity>> {
        return repositories
    }

    override fun getEnabled(): Flow<List<PluginRepositoryEntity>> {
        return repositories.map { list -> list.filter { it.isEnabled } }
    }

    override suspend fun getByUrl(url: String): PluginRepositoryEntity? {
        return repositories.value.find { it.url == url }
    }

    override suspend fun getById(id: Long): PluginRepositoryEntity? {
        return repositories.value.find { it.id == id }
    }

    override suspend fun add(repository: PluginRepositoryEntity): Long {
        val id = nextId++
        val newRepo = repository.copy(
            id = id,
            createdAt = repository.createdAt.takeIf { it > 0 } ?: currentTimeToLong()
        )
        repositories.value = repositories.value + newRepo
        return id
    }

    override suspend fun update(repository: PluginRepositoryEntity) {
        repositories.value = repositories.value.map { 
            if (it.id == repository.id) repository else it 
        }
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) {
        repositories.value = repositories.value.map { 
            if (it.id == id) it.copy(isEnabled = enabled) else it 
        }
    }

    override suspend fun updatePluginCount(id: Long, count: Int, lastUpdated: Long) {
        repositories.value = repositories.value.map { 
            if (it.id == id) it.copy(pluginCount = count, lastUpdated = lastUpdated) else it 
        }
    }

    override suspend fun updateError(id: Long, error: String?, lastUpdated: Long) {
        repositories.value = repositories.value.map { 
            if (it.id == id) it.copy(lastError = error, lastUpdated = lastUpdated) else it 
        }
    }

    override suspend fun delete(id: Long) {
        repositories.value = repositories.value.filter { it.id != id }
    }

    override suspend fun deleteByUrl(url: String) {
        repositories.value = repositories.value.filter { it.url != url }
    }

    override suspend fun initializeDefaults() {
        if (repositories.value.isEmpty()) {
            add(PluginRepositoryEntity(
                id = 0,
                url = OFFICIAL_REPO_URL,
                name = "Official IReader Plugins",
                isEnabled = true,
                isOfficial = true,
                pluginCount = 0,
                lastUpdated = 0,
                lastError = null,
                createdAt = currentTimeToLong()
            ))
        }
    }

    companion object {
        // Use GitHub raw URL for the official plugin repository
        // The gh-pages branch hosts the built plugin files
        const val OFFICIAL_REPO_URL = "https://raw.githubusercontent.com/IReaderorg/IReader-plugins/refs/heads/gh-pages/repo/index.json"
    }
}
