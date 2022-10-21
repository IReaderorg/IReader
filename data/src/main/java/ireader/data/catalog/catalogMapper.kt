package ireader.data.catalog

import ireader.domain.models.entities.CatalogRemote

val catalogMapper = { sourceId: Long,source:Long, name: String, description: String, pkgName: String, versionName: String, versionCode: Int, lang: String, pkgUrl: String, iconUrl: String, nsfw: Boolean ->

    CatalogRemote(
        sourceId,
        source,
        name,
        description,
        pkgName,
        versionName,
        versionCode,
        lang,
        pkgUrl,
        iconUrl,
        nsfw
    )
}