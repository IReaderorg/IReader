package ireader.imageloader.coil

import coil.map.Mapper
import coil.request.Options
import ireader.common.models.entities.CatalogRemote
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

class CatalogRemoteMapper : Mapper<CatalogRemote, HttpUrl> {

    override fun map(data: CatalogRemote, options: Options): HttpUrl? {
        return data.iconUrl.toHttpUrl()
    }
}
