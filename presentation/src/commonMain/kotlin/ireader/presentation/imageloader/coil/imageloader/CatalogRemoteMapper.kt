package ireader.presentation.imageloader.coil.imageloader

import coil3.Uri
import coil3.key.Keyer
import coil3.map.Mapper
import coil3.request.Options
import coil3.toUri
import io.ktor.http.*
import ireader.domain.models.entities.Catalog
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.CatalogRemote
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class CatalogRemoteMapper : Mapper<Catalog,Uri> {
    override fun map(data: Catalog, options: Options): Uri? {
        return when (data) {
            is CatalogRemote -> {
                data.iconUrl.toUri()
            }
            is CatalogInstalled -> {
                data.iconUrl.toUri()
            }
            else -> {
                null
            }
        }
    }
}
class CatalogKeyer : Keyer<Catalog> {
    override fun key(data: Catalog, options: Options): String? {
        return when(data) {
            is CatalogRemote -> data.iconUrl
            is CatalogLocal -> data.sourceId.toString()
            else -> null
        }
    }
}
