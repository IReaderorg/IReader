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
                // Only map if iconUrl is not blank
                if (data.iconUrl.isNotBlank()) {
                    data.iconUrl.toUri()
                } else {
                    null
                }
            }
            is CatalogInstalled -> {
                // Only map if iconUrl is not blank
                if (data.iconUrl.isNotBlank()) {
                    data.iconUrl.toUri()
                } else {
                    null
                }
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
            is CatalogRemote -> {
                // Use iconUrl if available, otherwise use pkgName as fallback
                data.iconUrl.ifBlank { "catalog-remote-${data.pkgName}" }
            }
            is CatalogLocal -> data.sourceId.toString()
            else -> null
        }
    }
}
