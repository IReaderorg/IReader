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
import ireader.i18n.LocalizeHelper
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
    private val localizeHelper: LocalizeHelper
) : CatalogInstaller {


    private fun savedCatalogLocation(catalog: CatalogRemote): File {
        val savedFromCache= uiPreferences.savedLocalCatalogLocation().get()
        val cacheLocation = File(getSimpleStorage.cacheExtensionDir(), catalog.pkgName).apply { mkdirs() }
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
        
        // Check if this is an LNReader plugin (JavaScript-based)
        if (catalog.isLNReaderSource()) {
            installJSPlugin(catalog)
            return@channelFlow
        }
        
        // Traditional APK installation
        val tmpApkFile = File(context.codeCacheDir, "${catalog.pkgName}.apk")
        try {
            val apkResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }.body()
            apkResponse.saveTo(tmpApkFile)

            send(InstallStep.Downloading)
            val extDir = savedCatalogLocation(catalog)

            val apkFile = File(extDir, tmpApkFile.name).apply { mkdirs() }

            val apkSuccess = tmpApkFile.copyRecursively(apkFile, true)
            if (apkSuccess) {
                installationChanges.notifyAppInstall(catalog.pkgName)
                send(InstallStep.Success)
            }

            send(InstallStep.Success)
            send(InstallStep.Idle)
        } catch (e: Exception) {
            Log.warn(e, "Error installing package")
            send(InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper)))
            send(InstallStep.Idle)
        } finally {
            tmpApkFile.delete()
            send(InstallStep.Idle)
        }
    }
    
    /**
     * Install a JavaScript plugin (LNReader format)
     * This is a suspend function that will be called from the channelFlow
     */
    private suspend fun kotlinx.coroutines.channels.ProducerScope<InstallStep>.installJSPlugin(catalog: CatalogRemote) {
        try {
            send(InstallStep.Downloading)
            
            // Determine JS plugins directory based on user preference
            val useCacheDir = uiPreferences.savedLocalCatalogLocation().get()
            val jsPluginsDir = if (useCacheDir) {
                File(context.cacheDir, "js-plugins").apply { mkdirs() }
            } else {
                val externalDir = context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile
                val ireaderDir = File(externalDir, "ireader")
                File(ireaderDir, "js-plugins").apply { mkdirs() }
            }
            
            // Download the JS plugin file
            val jsFile = File(jsPluginsDir, "${catalog.pkgName}.js")
            val jsResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }.body()
            jsResponse.saveTo(jsFile)
            
            // No need to download icon - Coil will handle it via iconUrl with caching
            
            // Notify installation complete
            installationChanges.notifyAppInstall(catalog.pkgName)
            
            send(InstallStep.Success)
            send(InstallStep.Idle)
            
        } catch (e: Exception) {
            Log.error("Failed to install JS plugin: ${catalog.name}", e)
            send(InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper)))
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
            // Try to uninstall as traditional APK extension
            val file = File(getSimpleStorage.extensionDirectory(), pkgName)
            val cacheFile = File(getSimpleStorage.cacheExtensionDir(), pkgName)
            file.deleteRecursively()
            cacheFile.deleteRecursively()
            
            // Also try to uninstall as JS plugin
            val useCacheDir = uiPreferences.savedLocalCatalogLocation().get()
            val jsPluginsDir = if (useCacheDir) {
                File(context.cacheDir, "js-plugins")
            } else {
                val externalDir = context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile
                val ireaderDir = File(externalDir, "ireader")
                File(ireaderDir, "js-plugins")
            }
            
            val jsFile = File(jsPluginsDir, "$pkgName.js")
            
            if (jsFile.exists()) {
                jsFile.delete()
                Log.info("Deleted JS plugin file: ${jsFile.absolutePath}")
            }
            
            installationChanges.notifyAppUninstall(pkgName)
            InstallStep.Success
        } catch (e: Throwable) {
            InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper))
        }
    }

}