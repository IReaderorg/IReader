package ireader.data.catalog

import ir.kazemcodes.infinityreader.Database
import ireader.common.models.entities.CatalogRemote
import ireader.common.models.entities.History
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.data.catalog.catalogMapper
import ireader.data.local.DatabaseHandler
import kotlinx.coroutines.flow.Flow

class CatalogRemoteRepositoryImpl(
    val handler: DatabaseHandler,
) : CatalogRemoteRepository {
    override suspend fun getRemoteCatalogs(): List<CatalogRemote> {
        return  handler.awaitList {
            catalogQueries.findAll(catalogMapper)
        }
    }

    override fun getRemoteCatalogsFlow(): Flow<List<CatalogRemote>> {
      return  handler.subscribeToList {
            catalogQueries.findAll(catalogMapper)
        }
    }

    override suspend fun insertRemoteCatalogs(catalogs: List<CatalogRemote>) {
        return  handler.await(true) {
            for (i in catalogs) {
                insertBlocking(i)
            }

        }
    }

    override suspend fun deleteAllRemoteCatalogs() {
        handler.await {
            catalogQueries.deleteAll()
        }
    }



    private fun Database.insertBlocking(catalogs: CatalogRemote) {
        catalogQueries.insert(
            catalogs.sourceId,
            catalogs.name,
            catalogs.description,
            catalogs.pkgName,
            catalogs.versionName,
            catalogs.versionCode,
            catalogs.lang,
            catalogs.pkgUrl,
            catalogs.iconUrl,
            catalogs.nsfw,

        )
    }

}
