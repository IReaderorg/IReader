package ireader.data.catalog.impl

import androidx.annotation.Keep
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import ireader.common.models.entities.CatalogRemote
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ireader.i18n.REPO_URL
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.core.os.AppState
import ireader.domain.catalogs.service.CatalogRemoteApi
import ireader.domain.utils.CatalogNotFoundException
import org.koin.core.annotation.Single

@Single
class CatalogGithubApi(
    private val httpClient: HttpClients,
) : CatalogRemoteApi {
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
                nsfw = catalog.nsfw,
                source = CatalogRemote.DEFAULT_ID
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
