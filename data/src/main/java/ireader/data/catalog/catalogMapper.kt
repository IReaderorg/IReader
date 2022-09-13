package ireader.data.catalog

import ireader.common.models.entities.CatalogRemote

val catalogMapper = { sourceId: Long, name: String, description: String, pkgName: String, versionName: String, versionCode: Int, lang: String, pkgUrl: String, iconUrl: String, nsfw: Boolean ->

    CatalogRemote(
        sourceId,
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