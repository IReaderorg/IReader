package org.ireader.domain.feature_service.io.coil

import coil.map.Mapper
import coil.request.Options
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.ireader.domain.models.entities.CatalogRemote

class CatalogRemoteMapper : Mapper<CatalogRemote, HttpUrl> {

    override fun map(data: CatalogRemote, options: Options): HttpUrl? {
        return data.iconUrl.toHttpUrl()
    }

}
