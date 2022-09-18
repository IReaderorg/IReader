package ireader.data.catalog.impl

import androidx.annotation.Keep
import io.ktor.client.request.*
import io.ktor.client.statement.*
import ireader.common.models.entities.CatalogRemote
import ireader.core.http.HttpClients
import ireader.domain.catalogs.service.CatalogRemoteApi
import ireader.domain.data.repository.CatalogSourceRepository
import ireader.domain.models.entities.ExtensionSource
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.utils.CatalogNotFoundException
import ireader.i18n.REPO_URL
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class CatalogGithubApi(
    private val httpClient: HttpClients,
    private val uiPreferences: UiPreferences,
    private val repository: CatalogSourceRepository
) : CatalogRemoteApi {

    override suspend fun fetchCatalogs(): List<ireader.common.models.entities.CatalogRemote> {
        val defaultRepo = uiPreferences.defaultRepository().get()
        val repo = repository.find(defaultRepo)?.takeIf { it.id >= 0 } ?: ExtensionSource.default()
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
            ireader.common.models.entities.CatalogRemote(
                name = catalog.name,
                description = catalog.description,
                sourceId = catalog.id,
                pkgName = catalog.pkg,
                versionName = catalog.version,
                versionCode = catalog.code,
                lang = catalog.lang,
                pkgUrl = "$repoUrl/apk/${catalog.apk}",
                iconUrl = "$repoUrl/icon/${catalog.apk.replace(".apk", ".png")}",
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
