package ireader.data.catalogrepository

import ireader.common.models.entities.ExtensionSource
import ireader.data.local.DatabaseHandler
import ireader.domain.data.repository.CatalogSourceRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class CatalogSourceRepositoryImpl(val handler: DatabaseHandler): CatalogSourceRepository  {
    override fun subscribe(): Flow<List<ExtensionSource>> {
       return handler.subscribeToList {
           repositoryQueries.findAll(catalogRemoteMapper)
       }
    }


    override suspend fun insert(extensionSource: ExtensionSource) {
        handler.await {
            repositoryQueries.insert(extensionSource.name,extensionSource.key,extensionSource.owner,extensionSource.source,extensionSource.lastUpdate,extensionSource.isEnable)
        }
    }


}