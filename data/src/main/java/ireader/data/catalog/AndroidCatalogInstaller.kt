package ireader.data.catalog

import android.app.Application
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ireader.common.resources.UiText
import ireader.common.resources.asString
import ireader.core.api.http.HttpClients
import ireader.core.api.io.saveTo
import ireader.core.api.log.Log
import ireader.core.api.os.InstallStep
import ireader.core.api.os.PackageInstaller
import ireader.core.catalogs.service.CatalogInstaller
import java.io.File

/**
 * The installer which installs, updates and uninstalls the extensions.
 *
 * @param context The application context.
 */

class AndroidCatalogInstaller(
    private val context: Application,
    private val httpClient: HttpClients,
    private val installationChanges: ireader.data.catalog.AndroidCatalogInstallationChanges,
    private val packageInstaller: PackageInstaller
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
            try {
                val apkResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                    headers.append(HttpHeaders.CacheControl, "no-store")
                }.body()
                apkResponse.saveTo(tmpApkFile)
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
