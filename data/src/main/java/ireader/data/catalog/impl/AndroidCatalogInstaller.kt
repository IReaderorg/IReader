package ireader.data.catalog.impl

import android.app.Application
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import ireader.core.http.HttpClients
import ireader.core.io.saveTo
import ireader.core.log.Log
import ireader.core.os.InstallStep
import ireader.core.os.PackageInstaller
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.i18n.UiText
import ireader.i18n.asString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single
import java.io.File

/**
 * The installer which installs, updates and uninstalls the extensions.
 *
 * @param context The application context.
 */
@Single
class AndroidCatalogInstaller(
    private val context: Application,
    private val httpClient: HttpClients,
    private val installationChanges: AndroidCatalogInstallationChanges,
    private val packageInstaller: PackageInstaller,
    private val getSimpleStorage: GetSimpleStorage,
) : CatalogInstaller {

    /**
     * The client used for http requests.
     */
    private val client get() = httpClient.default

    /**
     * Adds the given extension to the downloads queue and returns an observable containing its
     * step in the installation process.
     *
     * @param catalog The catalog to install.
     */
    override fun install(catalog: ireader.common.models.entities.CatalogRemote): Flow<InstallStep> =
        flow {
            emit(InstallStep.Downloading)
            val tmpApkFile = File(context.cacheDir, "${catalog.pkgName}.apk")
            val tmpIconFile = File(context.cacheDir, "${catalog.pkgName}.png")
            try {
                val apkResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                    headers.append(HttpHeaders.CacheControl, "no-store")
                }.body()
                val iconResponse: ByteReadChannel = client.get(catalog.iconUrl) {
                    headers.append(HttpHeaders.CacheControl, "no-store")
                }.body()
                apkResponse.saveTo(tmpApkFile)

                // copy installed App Icon to the storage
                iconResponse.saveTo(tmpIconFile)
                val extDir = File(getSimpleStorage.extensionDirectory(), catalog.pkgName).apply { mkdirs() }
                val iconFile = File(extDir, tmpIconFile.name)
                tmpIconFile.copyRecursively(iconFile,true)

                emit(InstallStep.Idle)
                val result = packageInstaller.install(tmpApkFile, catalog.pkgName)
                if (result is InstallStep.Success) {
                    installationChanges.notifyAppInstall(catalog.pkgName)
                }
                emit(result)
            } catch (e: Throwable) {
                Log.warn(e, "Error installing package")
                emit(InstallStep.Error(UiText.ExceptionString(e).asString(context)))
            } finally {
                tmpApkFile.delete()
            }
        }

    /**
     * Starts an intent to uninstall the extension by the given package name.
     *
     * @param pkgName The package name of the extension to uninstall
     */
    override suspend fun uninstall(pkgName: String): InstallStep {
        return try {
            val deleted = packageInstaller.uninstall(pkgName)
            installationChanges.notifyAppUninstall(pkgName)
            deleted
        } catch (e: Throwable) {
            InstallStep.Error(UiText.ExceptionString(e).asString(context))
        }
    }
}
