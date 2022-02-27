package org.ireader.domain.catalog.service

import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ireader.domain.models.entities.CatalogRemote
import tachiyomi.core.http.HttpClients

class CatalogGithubApi(
    private val httpClients: HttpClients,
) : CatalogRemoteApi {

    private val repoUrl =
        "https://raw.githubusercontent.com/tachiyomiorg/tachiyomi-extensions-1.x/repo"

    override suspend fun fetchCatalogs(): List<CatalogRemote> {
        val response = httpClients.default.get<String>("$repoUrl/index.min.json")
        val catalogs = Json.Default.decodeFromString<List<CatalogRemoteApiModel>>(response)
        return catalogs.map { catalog ->
            CatalogRemote(
                name = catalog.name,
                description = catalog.description,
                sourceId = catalog.id,
                pkgName = catalog.pkg,
                versionName = catalog.version,
                versionCode = catalog.code,
                lang = catalog.lang,
                pkgUrl = "$repoUrl/apk/${catalog.apk}",
                iconUrl = "$repoUrl/icon/${catalog.apk.replace(".apk", ".png")}",
                nsfw = catalog.nsfw
            )
        }
    }

    @Serializable
    private data class CatalogRemoteApiModel(
        val name: String,
        val pkg: String,
        val version: String,
        val code: Int,
        val lang: String,
        val apk: String,
        val id: Long,
        val description: String,
        val nsfw: Boolean,
    )

}
