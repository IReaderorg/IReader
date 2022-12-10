package ireader.data.catalogrepository

import ireader.data.core.DatabaseHandler
import ireader.domain.data.repository.CatalogSourceRepository
import ireader.domain.models.entities.ExtensionSource
import kotlinx.coroutines.flow.Flow



class CatalogSourceRepositoryImpl(val handler: DatabaseHandler): CatalogSourceRepository {
    override fun subscribe(): Flow<List<ExtensionSource>> {
       return handler.subscribeToList {
           repositoryQueries.findAll(catalogRemoteMapper)
       }
    }

    override suspend fun find(id: Long): ExtensionSource? {
        return handler.awaitOneOrNull {
            repositoryQueries.find(id, extensionMapper)
        }
    }


    override suspend fun insert(extensionSource: ExtensionSource) {
        handler.await {
            repositoryQueries.insert(extensionSource.name,extensionSource.key,extensionSource.owner,extensionSource.source,extensionSource.lastUpdate,extensionSource.isEnable)
        }
    }

    override suspend fun delete(extensionSource: ExtensionSource) {
        handler.await {
            repositoryQueries.delete(extensionSource.id)
        }
    }


}

val extensionMapper =  {_id: Long, name: String, key: String, owner: String, source: String, last_update: Long, is_enable: Boolean ->
    ExtensionSource(
        _id,
        name,
        key,
        owner,
        source,
            null,
            null,
        last_update,
        is_enable
    )
}