package ireader.presentation.imageloader.coil.imageloader

import com.seiko.imageloader.component.keyer.Keyer
import com.seiko.imageloader.component.mapper.Mapper
import com.seiko.imageloader.option.Options
import io.ktor.http.*
import ireader.domain.models.BookCover
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogRemote

class BookCoverKeyer : Keyer {
    override fun key(data: Any, options: Options): String? {
        return when (data) {
            is BookCover -> {
                return "${data.cover};${data.lastModified}"
            }
            else -> null
        }
    }
}
class BookCoverMapper : Mapper<Url> {

    override fun map(data: Any, options: Options): Url? {
        return when (data) {
            is BookCover -> {
                return Url(data.cover ?: "")
            }
            else -> {
                null
            }
        }
    }
}
class InstalledCatalogKeyer : Keyer {
    override fun key(data: Any, options: Options): String? {
        return if (data is CatalogInstalled) {
            data.iconUrl.ifBlank {
                data.pkgName
            }
        } else {
            null
        }

    }
}

class CatalogRemoteKeyer : Keyer {
    override fun key(data: Any, options: Options): String? {
        return if (data is CatalogRemote) {
            return data.iconUrl.ifBlank {
                data.pkgName
            }
        } else {
            null
        }

    }
}