package ireader.data.catalog

import ir.kazemcodes.infinityreader.Database
import ireader.core.log.Log
import ireader.data.core.DatabaseHandler
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.models.entities.CatalogRemote
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
                kotlin.runCatching {
                    insertBlocking(i)
                }.getOrElse {
                    Log.error(it, "${i.name} Source has a problem")
                }

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
            catalogs.source,
            catalogs.name,
            catalogs.description,
            catalogs.pkgName,
            catalogs.versionName,
            catalogs.versionCode,
            catalogs.lang,
            catalogs.pkgUrl,
            catalogs.iconUrl,
            catalogs.nsfw,
            catalogs.repositoryType
        )
    }

}
