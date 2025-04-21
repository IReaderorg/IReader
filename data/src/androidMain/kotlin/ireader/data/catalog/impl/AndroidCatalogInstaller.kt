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
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.i18n.LocalizeHelper
import ireader.i18n.UiText
import ireader.i18n.asString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

/**
 * The installer which installs, updates and uninstalls the extensions.
 *
 * @param context The application context.
 */
class AndroidCatalogInstaller(
    private val context: Application,
    private val httpClient: HttpClients,
    private val installationChanges: AndroidCatalogInstallationChanges,
    private val packageInstaller: PackageInstaller,
    private val getSimpleStorage: GetSimpleStorage,
    private val localizeHelper: LocalizeHelper
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
    override fun install(catalog: CatalogRemote): Flow<InstallStep> =
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
                
                // Save the icon to storage
                iconResponse.saveTo(tmpIconFile)
                val extDir = File(context.cacheDir, catalog.pkgName).apply { mkdirs() }
                val iconFile = File(extDir, tmpIconFile.name)
                tmpIconFile.copyRecursively(iconFile, true)
                
                // Create a secure directory in the code cache directory for installation
                val secureApkDir = File(context.codeCacheDir, "secure_installations")
                secureApkDir.mkdirs()
                val secureApkFile = File(secureApkDir, "${catalog.pkgName}.apk")
                
                // Copy the APK to the secure location
                if (secureApkFile.exists()) {
                    secureApkFile.delete()
                }
                tmpApkFile.copyTo(secureApkFile, overwrite = true)
                
                // Make sure the file is readable but not set to read-only
                // Android 14 (API 34) needs the file to be writable
                secureApkFile.setReadable(true)
                secureApkFile.setWritable(true)
                
                emit(InstallStep.Idle)
                // Use the secure file for installation
                val result = packageInstaller.install(secureApkFile, catalog.pkgName)
                if (result is InstallStep.Success) {
                    installationChanges.notifyAppInstall(catalog.pkgName)
                }
                emit(result)
            } catch (e: Throwable) {
                Log.warn(e, "Error installing package")
                emit(InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper)))
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
            InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper))
        }
    }
}
