package ireader.presentation.imageloader.coil.imageloader

import coil3.Uri
import coil3.key.Keyer
import coil3.map.Mapper
import coil3.request.Options
import coil3.toUri
import io.ktor.http.*
import ireader.domain.models.BookCover
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogRemote

class BookCoverKeyer : Keyer<BookCover> {
    override fun key(data: BookCover, options: Options): String? {
        return "${data.cover};${data.lastModified}"
    }
}
// There is no need for this mapper as we have custom fetcher for book covers
class BookCoverMapper : Mapper<BookCover, Uri> {

    override fun map(data: BookCover, options: Options): Uri? {
        return data.cover?.toUri()
    }
}
class InstalledCatalogKeyer : Keyer<CatalogInstalled> {
    override fun key(data: CatalogInstalled, options: Options): String? {
        return data.iconUrl.ifBlank {
            data.pkgName
        }

    }
}

class CatalogRemoteKeyer : Keyer<CatalogRemote> {
    override fun key(data: CatalogRemote, options: Options): String? {
        return data.iconUrl.ifBlank {
            data.pkgName
        }

    }
}