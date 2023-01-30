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
import ireader.domain.catalogs.service.CatalogInstaller
import ireader.domain.models.entities.CatalogRemote
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.i18n.UiText
import ireader.i18n.asString
import kotlinx.coroutines.flow.channelFlow
import java.io.File


/**
 * The installer which installs, updates and uninstalls the extensions.
 *
 * @param context The application context.
 */

class AndroidLocalInstaller(
    private val context: Application,
    private val httpClients: HttpClients,
    private val installationChanges: AndroidCatalogInstallationChanges,
    private val getSimpleStorage: GetSimpleStorage,
    private val uiPreferences: UiPreferences,
    private val simpleStorage: GetSimpleStorage
) : CatalogInstaller {


    private fun savedCatalogLocation(catalog: CatalogRemote): File {
        val savedFromCache= uiPreferences.savedLocalCatalogLocation().get()
        val cacheLocation = File(simpleStorage.cacheExtensionDir(context), catalog.pkgName).apply { mkdirs() }
        val primaryLocation = File(getSimpleStorage.extensionDirectory(), catalog.pkgName).apply { mkdirs() }

        return if (savedFromCache) cacheLocation else primaryLocation
    }

    /**
     * The client used for http requests.
     */
    private val client get() = httpClients.default
    init {
        getSimpleStorage.checkPermission()
    }
    /**
     * Adds the given extension to the downloads queue and returns an observable containing its
     * step in the installation process.
     *
     * @param catalog The catalog to install.
     */
    override fun install(catalog: CatalogRemote) = channelFlow  {
        send(InstallStep.Downloading)
        val tmpApkFile = File(context.codeCacheDir, "${catalog.pkgName}.apk")
        val tmpIconFile = File(context.codeCacheDir, "${catalog.pkgName}.png")
        try {
            val apkResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }.body()
            apkResponse.saveTo(tmpApkFile)

            val iconResponse: ByteReadChannel = client.get(catalog.iconUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }.body()
            iconResponse.saveTo(tmpIconFile)

            send(InstallStep.Downloading)
            val extDir = savedCatalogLocation(catalog)

            val apkFile = File(extDir, tmpApkFile.name).apply { mkdirs() }
            val iconFile = File(extDir, tmpIconFile.name).apply { mkdirs() }


            val apkSuccess = tmpApkFile.copyRecursively(apkFile,true)
            val iconSuccess = tmpIconFile.copyRecursively(iconFile,true)
            val success = apkSuccess && iconSuccess
            if (success) {
                installationChanges.notifyAppInstall(catalog.pkgName)
                send(InstallStep.Success)
            }

            send(InstallStep.Success)
            send(InstallStep.Idle)
        } catch (e: Exception) {
            Log.warn(e, "Error installing package")
            send(InstallStep.Error(UiText.ExceptionString(e).asString(context)))
            send(InstallStep.Idle)
        } finally {
            tmpApkFile.delete()
            tmpIconFile.delete()
            send(InstallStep.Idle)
        }
    }

    /**
     * Starts an intent to uninstall the extension by the given package name.
     *
     * @param pkgName The package name of the extension to uninstall
     */
    override suspend fun uninstall(pkgName: String): InstallStep {
        return try {
        val file = File(getSimpleStorage.extensionDirectory(), pkgName)
        val cacheFile = File(getSimpleStorage.cacheExtensionDir(context), pkgName)
        file.deleteRecursively()
            cacheFile.deleteRecursively()
        installationChanges.notifyAppUninstall(pkgName)
        InstallStep.Success
        } catch (e: Throwable) {
            InstallStep.Error(UiText.ExceptionString(e).asString(context))
        }
    }

}