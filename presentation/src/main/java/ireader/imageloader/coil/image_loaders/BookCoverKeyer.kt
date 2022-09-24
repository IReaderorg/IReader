package ireader.imageloader.coil.image_loaders

import coil.key.Keyer
import coil.request.Options
import ireader.common.models.entities.CatalogInstalled
import ireader.common.models.entities.CatalogRemote
import ireader.domain.models.BookCover

class BookCoverKeyer : Keyer<BookCover> {
    override fun key(data: BookCover, options: Options): String {
           return "${data.cover};${data.lastModified}"
    }
}

class InstalledCatalogKeyer : Keyer<CatalogInstalled> {
    override fun key(data: CatalogInstalled, options: Options): String {
        return data.iconUrl.ifBlank {
            data.pkgName
        }
    }
}
class CatalogRemoteKeyer : Keyer<CatalogRemote> {
    override fun key(data: CatalogRemote, options: Options): String {
        return data.iconUrl.ifBlank {
            data.pkgName
        }
    }
}