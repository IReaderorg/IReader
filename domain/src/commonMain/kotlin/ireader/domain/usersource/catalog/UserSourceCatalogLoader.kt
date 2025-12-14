package ireader.domain.usersource.catalog

import io.ktor.client.HttpClient
import ireader.domain.models.entities.UserSourceCatalog
import ireader.domain.usersource.repository.UserSourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Loads user-defined sources as catalogs for integration with CatalogStore.
 */
class UserSourceCatalogLoader(
    private val userSourceRepository: UserSourceRepository,
    private val httpClient: HttpClient
) {
    
    /**
     * Get all enabled user sources as catalogs.
     */
    suspend fun getEnabledCatalogs(): List<UserSourceCatalog> {
        return userSourceRepository.getEnabled().map { userSource ->
            val catalogSource = UserSourceCatalogSource(userSource, httpClient)
            UserSourceCatalog(
                name = catalogSource.name,
                sourceId = catalogSource.id,
                source = catalogSource,
                userSource = userSource
            )
        }
    }
    
    /**
     * Get all user sources as catalogs (enabled and disabled).
     */
    suspend fun getAllCatalogs(): List<UserSourceCatalog> {
        return userSourceRepository.getAll().map { userSource ->
            val catalogSource = UserSourceCatalogSource(userSource, httpClient)
            UserSourceCatalog(
                name = catalogSource.name,
                sourceId = catalogSource.id,
                source = catalogSource,
                userSource = userSource
            )
        }
    }
    
    /**
     * Get user source catalogs as a flow for reactive updates.
     */
    fun getCatalogsFlow(): Flow<List<UserSourceCatalog>> {
        return userSourceRepository.getAllAsFlow().map { userSources ->
            userSources.map { userSource ->
                val catalogSource = UserSourceCatalogSource(userSource, httpClient)
                UserSourceCatalog(
                    name = catalogSource.name,
                    sourceId = catalogSource.id,
                    source = catalogSource,
                    userSource = userSource
                )
            }
        }
    }
    
    /**
     * Get a specific user source catalog by source ID.
     */
    suspend fun getCatalogById(sourceId: Long): UserSourceCatalog? {
        val userSource = userSourceRepository.getById(sourceId) ?: return null
        val catalogSource = UserSourceCatalogSource(userSource, httpClient)
        return UserSourceCatalog(
            name = catalogSource.name,
            sourceId = catalogSource.id,
            source = catalogSource,
            userSource = userSource
        )
    }
    
    /**
     * Get a specific user source catalog by source URL.
     */
    suspend fun getCatalogByUrl(sourceUrl: String): UserSourceCatalog? {
        val userSource = userSourceRepository.getByUrl(sourceUrl) ?: return null
        val catalogSource = UserSourceCatalogSource(userSource, httpClient)
        return UserSourceCatalog(
            name = catalogSource.name,
            sourceId = catalogSource.id,
            source = catalogSource,
            userSource = userSource
        )
    }
}
