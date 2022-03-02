package org.ireader.domain.feature_services.io.coil

import coil.map.Mapper
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.ireader.domain.models.entities.CatalogRemote

class CatalogRemoteMapper : Mapper<CatalogRemote, HttpUrl> {

    override fun map(data: CatalogRemote): HttpUrl {
        return data.iconUrl.toHttpUrl()
    }

}
