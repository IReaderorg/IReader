package ireader.data.catalog.impl

import androidx.annotation.Keep
import io.ktor.client.request.*
import io.ktor.client.statement.*
import ireader.common.models.entities.CatalogRemote
import ireader.core.http.HttpClients
import ireader.domain.catalogs.service.CatalogRemoteApi
import ireader.domain.services.extensions_insstaller_service.GetDefaultRepo
import ireader.domain.utils.CatalogNotFoundException
import ireader.i18n.REPO_URL
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class CatalogGithubApi(
    private val httpClient: HttpClients,
    private val getDefaultRepo: GetDefaultRepo
) : CatalogRemoteApi {

    override suspend fun fetchCatalogs(): List<ireader.common.models.entities.CatalogRemote> {
        val repo = getDefaultRepo()
        val response: String =
            httpClient.default
                .get(repo.key)
                .bodyAsText()

        val catalogs = Json.Default.decodeFromString<List<CatalogRemoteApiModel>>(response)
        if (catalogs.isEmpty()) {
            throw CatalogNotFoundException()
        }
        val repoUrl = repo.key.substringBefore("index.min.json","").takeIf { it.isNotBlank() } ?: REPO_URL
        return catalogs.map { catalog ->
            val iconUrl = "$repoUrl/icon/${catalog.apk.replace(".apk", ".png")}"
            val appUrl = "$repoUrl/apk/${catalog.apk}"
            ireader.common.models.entities.CatalogRemote(
                name = catalog.name,
                description = catalog.description,
                sourceId = catalog.id,
                pkgName = catalog.pkg,
                versionName = catalog.version,
                versionCode = catalog.code,
                lang = catalog.lang,
                pkgUrl = appUrl,
                iconUrl = iconUrl,
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
