package ireader.data.catalog

import androidx.annotation.Keep
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ireader.common.resources.REPO_URL
import ireader.core.api.http.HttpClients
import ireader.domain.catalogs.service.CatalogRemoteApi
import ireader.core.ui.CatalogNotFoundException

class CatalogGithubApi(
    private val httpClient: HttpClients,
) : CatalogRemoteApi {

//    private val repoUrl =
//        "https://raw.githubusercontent.com/IReaderorg/IReader-Repo/main"

    override suspend fun fetchCatalogs(): List<ireader.common.models.entities.CatalogRemote> {
        val response: String =
            httpClient.default
                .get("$REPO_URL/index.min.json")
                .bodyAsText()

        val catalogs = Json.Default.decodeFromString<List<CatalogRemoteApiModel>>(response)
        if (catalogs.isEmpty()) {
            throw CatalogNotFoundException()
        }
        return catalogs.map { catalog ->
            ireader.common.models.entities.CatalogRemote(
                name = catalog.name,
                description = catalog.description,
                sourceId = catalog.id,
                pkgName = catalog.pkg,
                versionName = catalog.version,
                versionCode = catalog.code,
                lang = catalog.lang,
                pkgUrl = "$REPO_URL/apk/${catalog.apk}",
                iconUrl = "$REPO_URL/icon/${catalog.apk.replace(".apk", ".png")}",
                nsfw = catalog.nsfw
            )
        }
    }

    @Keep
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
