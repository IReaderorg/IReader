package org.ireader.data.catalog

import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ireader.core_api.http.HttpClients
import org.ireader.domain.catalog.service.CatalogRemoteApi
import org.ireader.domain.models.entities.CatalogRemote
import javax.inject.Inject

class CatalogGithubApi @Inject constructor(
    private val httpClient: HttpClients,
) : CatalogRemoteApi {

    private val repoUrl =
        "https://raw.githubusercontent.com/kazemcodes/IReader-Sources/main"

    override suspend fun fetchCatalogs(): List<CatalogRemote> {
        val response : String  =
            httpClient.default
            .get("$repoUrl/index.min.json")
                .bodyAsText()

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
