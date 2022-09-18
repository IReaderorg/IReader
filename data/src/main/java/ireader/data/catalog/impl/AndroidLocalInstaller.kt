package ireader.data.catalog.impl

import android.app.Application
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import ireader.common.models.entities.CatalogRemote
import ireader.i18n.UiText
import ireader.i18n.asString
import ireader.core.http.HttpClients
import ireader.core.io.saveTo
import ireader.core.log.Log
import ireader.core.os.InstallStep
import ireader.domain.catalogs.service.CatalogInstaller
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single
import java.io.File


/**
 * The installer which installs, updates and uninstalls the extensions.
 *
 * @param context The application context.
 */
@Single
class AndroidLocalInstaller(
    private val context: Application,
    private val httpClients: HttpClients,
    private val installationChanges: AndroidCatalogInstallationChanges,
) : CatalogInstaller {

    /**
     * The client used for http requests.
     */
    private val client get() = httpClients.default

    /**
     * Adds the given extension to the downloads queue and returns an observable containing its
     * step in the installation process.
     *
     * @param catalog The catalog to install.
     */
    override fun install(catalog: CatalogRemote) = flow {
        emit(InstallStep.Downloading)
        val tmpApkFile = File(context.cacheDir, "${catalog.pkgName}.apk")
        val tmpIconFile = File(context.cacheDir, "${catalog.pkgName}.png")
        try {
            val apkResponse = client.get(catalog.pkgUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }
            apkResponse.bodyAsChannel().saveTo(tmpApkFile)

            val iconResponse = client.get(catalog.iconUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }
            iconResponse.bodyAsChannel().saveTo(tmpIconFile)

            emit(InstallStep.Downloading)

            val extDir = File(context.filesDir, "catalogs/${catalog.pkgName}").apply { mkdirs() }
            val apkFile = File(extDir, tmpApkFile.name)
            val iconFile = File(extDir, tmpIconFile.name)

            val apkSuccess = tmpApkFile.renameTo(apkFile)
            val iconSuccess = tmpIconFile.renameTo(iconFile)
            val success = apkSuccess && iconSuccess
            if (success) {
                installationChanges.notifyAppInstall(catalog.pkgName)
            }

            emit(InstallStep.Success)
        } catch (e: Exception) {
            Log.warn(e, "Error installing package")
            emit(InstallStep.Error(UiText.ExceptionString(e).asString(context)))
        } finally {
            tmpApkFile.delete()
            tmpIconFile.delete()
        }
    }

    /**
     * Starts an intent to uninstall the extension by the given package name.
     *
     * @param pkgName The package name of the extension to uninstall
     */
    override suspend fun uninstall(pkgName: String): InstallStep {
        return try {
        val file = File(context.filesDir, "catalogs/${pkgName}")
        file.deleteRecursively()
        installationChanges.notifyAppUninstall(pkgName)
        InstallStep.Success
        } catch (e: Throwable) {
            InstallStep.Error(UiText.ExceptionString(e).asString(context))
        }
    }

}