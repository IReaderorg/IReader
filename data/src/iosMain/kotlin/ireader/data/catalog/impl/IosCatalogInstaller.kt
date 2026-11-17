package ireader.data.catalog.impl

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import ireader.core.http.HttpClients
import ireader.core.io.saveTo
import ireader.core.os.InstallStep
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.models.entities.CatalogRemote
import ireader.i18n.LocalizeHelper
import ireader.i18n.UiText
import ireader.i18n.asString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem

/**
 * iOS implementation of CatalogInstaller
 * 
 * On iOS, we only support JS plugin installation since:
 * - APK/DEX loading is not possible (Android-specific)
 * - JAR loading is not possible (JVM-specific)
 * - Dynamic native code loading is prohibited by App Store
 * 
 * JS plugins are downloaded and executed via JavaScriptCore
 */
class IosCatalogInstaller(
    private val httpClient: HttpClients,
    private val localizeHelper: LocalizeHelper,
    private val installationChanges: IosCatalogInstallationChanges,
) : CatalogInstaller {

    private val client get() = httpClient.default
    private val json = Json { ignoreUnknownKeys = true }

    override fun install(catalog: CatalogRemote): Flow<InstallStep> = flow {
        emit(InstallStep.Downloading)

        val download = resolveDownload(catalog)
        if (download == null) {
            emit(InstallStep.Error("This extension is only published as an Android APK. iOS can install the JavaScript sources from the IReader repository."))
            return@flow
        }

        try {
            val fileSystem = FileSystem.SYSTEM
            val dirPath = iosJsPluginsDir()
            fileSystem.createDirectories(dirPath)

            val fileName = download.url.substringAfterLast('/').substringBefore('?')
            val jsFilePath = dirPath / fileName
            val metadataFilePath = dirPath / "${catalog.pkgName}.meta.json"

            val jsResponse: ByteReadChannel = client.get(download.url) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }.body()
            jsResponse.saveTo(jsFilePath, fileSystem)

            val metadata = json.encodeToString(
                InstalledJsSourceMeta(
                    id = catalog.sourceId,
                    name = catalog.name,
                    lang = catalog.lang,
                    version = catalog.versionName,
                    code = catalog.versionCode,
                    description = catalog.description,
                    icon = catalog.iconUrl,
                    nsfw = catalog.nsfw,
                    fileName = fileName,
                    initFunction = download.initFunction,
                )
            )
            fileSystem.write(metadataFilePath) {
                writeUtf8(metadata)
            }
            
            installationChanges.notifyAppInstall(catalog.pkgName)
            emit(InstallStep.Idle)
            emit(InstallStep.Success)
        } catch (e: Throwable) {
            emit(InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper)))
        }
    }

    override suspend fun uninstall(pkgName: String): InstallStep {
        return try {
            // Notify uninstall first
            installationChanges.notifyAppUninstall(pkgName)
            
            val fileSystem = FileSystem.SYSTEM
            val dirPath = iosJsPluginsDir()
            val metadataFilePath = dirPath / "$pkgName.meta.json"
            val fileName = if (fileSystem.exists(metadataFilePath)) {
                json.decodeFromString<InstalledJsSourceMeta>(
                    fileSystem.read(metadataFilePath) { readUtf8() }
                ).fileName
            } else {
                "$pkgName.js"
            }
            val jsFilePath = dirPath / fileName
            
            // Delete JS file
            if (fileSystem.exists(jsFilePath)) {
                fileSystem.delete(jsFilePath)
            }
            
            // Delete metadata file
            if (fileSystem.exists(metadataFilePath)) {
                fileSystem.delete(metadataFilePath)
            }
            
            InstallStep.Success
        } catch (e: Throwable) {
            InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper))
        }
    }

    private suspend fun resolveDownload(catalog: CatalogRemote): ResolvedJsDownload? {
        val pkgUrl = catalog.pkgUrl
        if (pkgUrl.substringBefore('?').endsWith(".js")) return ResolvedJsDownload(pkgUrl, "")
        if (!pkgUrl.contains("IReader-extensions")) return null
        val indexUrl = pkgUrl.substringBefore("/apk/") + "/js/js-index.min.json"
        val index = client.get(indexUrl).body<String>()
        val match = json.decodeFromString<List<JsRepoEntry>>(index)
            .firstOrNull { it.pkg == catalog.pkgName }
            ?: return null
        val url = indexUrl.substringBefore("/js-index.min.json") + "/" + match.file
        return ResolvedJsDownload(url, match.initFunction)
    }
}

@kotlinx.serialization.Serializable
private data class JsRepoEntry(
    val pkg: String,
    val file: String,
    val initFunction: String = "",
)

private data class ResolvedJsDownload(
    val url: String,
    val initFunction: String,
)
