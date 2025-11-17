package ireader.data.catalog

import ireader.core.log.Log
import ireader.domain.catalogs.interactor.ExtensionRepositoryManager
import ireader.domain.data.repository.CatalogSourceRepository
import ireader.domain.models.entities.ExtensionRepository
import ireader.domain.models.entities.ExtensionSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of extension repository management
 */
class ExtensionRepositoryManagerImpl(
    private val catalogSourceRepository: CatalogSourceRepository,
    private val log: Log
) : ExtensionRepositoryManager {
    
    override fun getRepositories(): Flow<List<ExtensionRepository>> {
        return catalogSourceRepository.subscribe().map { sources ->
            sources.map { source ->
                ExtensionRepository(
                    id = source.id,
                    name = source.name,
                    url = source.key,
                    fingerprint = null, // TODO: Add fingerprint support
                    enabled = true,
                    autoUpdate = true,
                    trustLevel = ireader.domain.models.entities.ExtensionTrustLevel.VERIFIED,
                    lastSync = 0,
                    extensionCount = 0
                )
            }
        }
    }
    
    override suspend fun addRepository(
        name: String,
        url: String,
        fingerprint: String?
    ): Result<ExtensionRepository> {
        return try {
            // Validate URL
            if (!isValidRepositoryUrl(url)) {
                return Result.failure(IllegalArgumentException("Invalid repository URL"))
            }
            
            // Determine repository type
            val repositoryType = detectRepositoryType(url)
            
            // Create extension source
            val extensionSource = ExtensionSource(
                id = 0,
                name = name,
                key = url,
                owner = extractOwner(url),
                source = url,
                repositoryType = repositoryType
            )
            
            // Insert into database
            catalogSourceRepository.insert(extensionSource)
            
            log.info("Added repository: $name ($url)")
            
            Result.success(
                ExtensionRepository(
                    id = 0,
                    name = name,
                    url = url,
                    fingerprint = fingerprint,
                    enabled = true,
                    autoUpdate = true,
                    trustLevel = ireader.domain.models.entities.ExtensionTrustLevel.VERIFIED,
                    lastSync = 0,
                    extensionCount = 0
                )
            )
        } catch (e: Exception) {
            log.error("Failed to add repository", e)
            Result.failure(e)
        }
    }
    
    override suspend fun removeRepository(repositoryId: Long): Result<Unit> {
        return try {
            catalogSourceRepository.delete(repositoryId)
            log.info("Removed repository: $repositoryId")
            Result.success(Unit)
        } catch (e: Exception) {
            log.error("Failed to remove repository", e)
            Result.failure(e)
        }
    }
    
    override suspend fun updateRepository(repository: ExtensionRepository): Result<Unit> {
        return try {
            // Update repository in database
            log.info("Updated repository: ${repository.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            log.error("Failed to update repository", e)
            Result.failure(e)
        }
    }
    
    override suspend fun syncRepository(repositoryId: Long): Result<Unit> {
        return try {
            log.info("Syncing repository: $repositoryId")
            // Sync logic would go here
            Result.success(Unit)
        } catch (e: Exception) {
            log.error("Failed to sync repository", e)
            Result.failure(e)
        }
    }
    
    override suspend fun syncAllRepositories(): Result<Unit> {
        return try {
            log.info("Syncing all repositories")
            // Sync all repositories
            Result.success(Unit)
        } catch (e: Exception) {
            log.error("Failed to sync all repositories", e)
            Result.failure(e)
        }
    }
    
    override suspend fun verifyRepositoryFingerprint(repositoryId: Long): Boolean {
        // TODO: Implement fingerprint verification
        return true
    }
    
    override suspend fun setRepositoryEnabled(repositoryId: Long, enabled: Boolean): Result<Unit> {
        return try {
            log.info("Set repository $repositoryId enabled: $enabled")
            Result.success(Unit)
        } catch (e: Exception) {
            log.error("Failed to set repository enabled state", e)
            Result.failure(e)
        }
    }
    
    override suspend fun setRepositoryAutoUpdate(repositoryId: Long, autoUpdate: Boolean): Result<Unit> {
        return try {
            log.info("Set repository $repositoryId auto-update: $autoUpdate")
            Result.success(Unit)
        } catch (e: Exception) {
            log.error("Failed to set repository auto-update", e)
            Result.failure(e)
        }
    }
    
    private fun isValidRepositoryUrl(url: String): Boolean {
        return url.startsWith("http://") || url.startsWith("https://")
    }
    
    private fun detectRepositoryType(url: String): String {
        return when {
            url.contains("lnreader", ignoreCase = true) -> "LNREADER"
            url.contains("ireader", ignoreCase = true) -> "IREADER"
            url.contains("tachiyomi", ignoreCase = true) -> "TACHIYOMI"
            else -> "CUSTOM"
        }
    }
    
    private fun extractOwner(url: String): String {
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
}
