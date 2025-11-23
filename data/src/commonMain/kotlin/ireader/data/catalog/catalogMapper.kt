package ireader.data.catalog

import ireader.domain.models.entities.CatalogRemote

val catalogMapper = { sourceId: Long,source:Long, name: String, description: String, pkgName: String, versionName: String, versionCode: Int, lang: String, pkgUrl: String, iconUrl: String, nsfw: Boolean, repositoryType: String ->

    CatalogRemote(
        sourceId = sourceId,
        source = source,
        name = name,
        description = description,
        pkgName = pkgName,
        versionName = versionName,
        versionCode = versionCode,
        lang = lang,
        pkgUrl = pkgUrl,
        iconUrl = iconUrl,
        jarUrl = pkgUrl.replace("apk", "jar"),
        nsfw = nsfw,
        repositoryType = repositoryType
    )
}