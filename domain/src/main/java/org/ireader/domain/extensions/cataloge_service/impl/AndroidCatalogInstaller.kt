package org.ireader.domain.extensions.cataloge_service.impl

import android.app.Application
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.ireader.core.io.saveTo
import org.ireader.core.okhttp.HttpClients
import org.ireader.domain.extensions.cataloge_service.CatalogInstaller
import org.ireader.domain.models.entities.CatalogRemote
import org.ireader.domain.models.entities.model.InstallStep
import timber.log.Timber
import java.io.File

/**
 * The installer which installs, updates and uninstalls the extensions.
 *
 * @param context The application context.
 */
class AndroidCatalogInstaller(
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
    override fun install(catalog: CatalogRemote): Flow<InstallStep> = flow {
        emit(InstallStep.Downloading)
        val tmpApkFile = File(context.cacheDir, "${catalog.pkgName}.apk")
        val tmpIconFile = File(context.cacheDir, "${catalog.pkgName}.png")
        try {
            val apkResponse = client.get<ByteReadChannel>(catalog.pkgUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }
            apkResponse.saveTo(tmpApkFile)

            val iconResponse = client.get<ByteReadChannel>(catalog.iconUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }
            iconResponse.saveTo(tmpIconFile)

            emit(InstallStep.Installing)

            val extDir = File(context.filesDir, "catalogs/${catalog.pkgName}").apply { mkdirs() }
            val apkFile = File(extDir, tmpApkFile.name)
            val iconFile = File(extDir, tmpIconFile.name)

            val apkSuccess = tmpApkFile.renameTo(apkFile)
            val iconSuccess = tmpIconFile.renameTo(iconFile)
            val success = apkSuccess && iconSuccess
            if (success) {
                installationChanges.notifyAppInstall(catalog.pkgName)
            }

            emit(if (success) InstallStep.Completed else InstallStep.Error)
        } catch (e: Exception) {
            Timber.w(e, "Error installing package")
            emit(InstallStep.Error)
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
    override suspend fun uninstall(pkgName: String): Boolean {
        val file = File(context.filesDir, "catalogs/${pkgName}")
        val deleted = file.deleteRecursively()
        installationChanges.notifyAppUninstall(pkgName)
        return deleted
    }

}
