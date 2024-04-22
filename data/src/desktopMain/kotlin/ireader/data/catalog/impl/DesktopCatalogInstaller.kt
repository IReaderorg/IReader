package ireader.data.catalog.impl

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import ireader.core.http.HttpClients
import ireader.core.io.saveTo
import ireader.core.log.Log
import ireader.core.os.InstallStep
import ireader.core.storage.ExtensionDir
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.models.entities.CatalogRemote
import ireader.i18n.LocalizeHelper
import ireader.i18n.UiText
import ireader.i18n.asString
import ireader.i18n.resources.MR
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class DesktopCatalogInstaller(
    private val httpClient: HttpClients,
    private val localizeHelper: LocalizeHelper,
    private val installationChanges: DesktopCatalogInstallationChanges,
) : CatalogInstaller {

    private val client get() = httpClient.default

    override fun install(catalog: CatalogRemote): Flow<InstallStep> =
        flow {
            emit(InstallStep.Downloading)
            val fileDir = File(ExtensionDir, "${catalog.pkgName}/")
            fileDir.mkdirs()
            val apkFile = File(ExtensionDir, "${catalog.pkgName}/${catalog.pkgName}.apk")
            if (!apkFile.exists()) {
                    apkFile.createNewFile()
            }
            val jarFile = File(ExtensionDir, "${catalog.pkgName}/${catalog.pkgName}.jar")
            if (!jarFile.exists()) {
                jarFile.createNewFile()
            }
            val iconFile = File(ExtensionDir, "${catalog.pkgName}/${catalog.pkgName}.png")
            if (!iconFile.exists()) {
                    iconFile.createNewFile()
            }
            try {
                val apkResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                    headers.append(HttpHeaders.CacheControl, "no-store")
                }.body()
                val jarResponse: ByteReadChannel = client.get(catalog.jarUrl) {
                    headers.append(HttpHeaders.CacheControl, "no-store")
                }.body()
                val iconResponse: ByteReadChannel = client.get(catalog.iconUrl) {
                    headers.append(HttpHeaders.CacheControl, "no-store")
                }.body()
                apkResponse.saveTo(apkFile)
                jarResponse.saveTo(jarFile)
                // copy installed App Icon to the storage
                iconResponse.saveTo(iconFile)
                emit(InstallStep.Idle)
                val result = InstallStep.Success
                installationChanges.notifyAppInstall(catalog.pkgName)
                emit(result)
            } catch (e: Throwable) {
                Log.warn(e, "Error installing package")
                emit(InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper)))
            }
        }

    override suspend fun uninstall(pkgName: String): InstallStep {
        return try {
            val file = File(ExtensionDir,pkgName)
            val deleted = file.deleteRecursively()
            file.deleteOnExit()
            installationChanges.notifyAppUninstall(pkgName)
            if (deleted ) InstallStep.Success else InstallStep.Error(localizeHelper.localize(MR.strings.failed))
        } catch (e: Throwable) {
            InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper))
        }
    }
}