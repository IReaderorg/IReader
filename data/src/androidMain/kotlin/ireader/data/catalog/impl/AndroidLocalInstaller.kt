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
import okio.FileSystem
import okio.Path.Companion.toPath
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
        val cacheLocation = File(getSimpleStorage.cacheExtensionDir().toFile(), catalog.pkgName).apply { mkdirs() }
        val primaryLocation = File(getSimpleStorage.extensionDirectory().toFile(), catalog.pkgName).apply { mkdirs() }

        return if (savedFromCache) cacheLocation else primaryLocation
    }

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
            apkResponse.saveTo(tmpApkFile.absolutePath.toPath(), FileSystem.SYSTEM)

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
            
            // Use secure storage for JS plugins
            val jsPluginsDir = ireader.domain.storage.SecureStorageHelper.getJsPluginsDir(context)
            Log.info("AndroidLocalInstaller: Installing JS plugin ${catalog.name} to ${jsPluginsDir.absolutePath}")
            Log.info("AndroidLocalInstaller: jsPluginsDir exists=${jsPluginsDir.exists()}, canWrite=${jsPluginsDir.canWrite()}")
            
            // Download the JS plugin file
            val jsFile = File(jsPluginsDir, "${catalog.pkgName}.js")
            val metadataFile = File(jsPluginsDir, "${catalog.pkgName}.meta.json")
            Log.info("AndroidLocalInstaller: Target files - js=${jsFile.absolutePath}, meta=${metadataFile.absolutePath}")
            
            val jsResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                headers.append(HttpHeaders.CacheControl, "no-store")
            }.body()
            jsResponse.saveTo(jsFile.absolutePath.toPath(), FileSystem.SYSTEM)
            
            // Save metadata from remote catalog (including language)
            // This ensures the language from the remote API is preserved
            val metadata = """
                {
                  "id": "${catalog.pkgName}",
                  "name": "${catalog.name.replace("\"", "\\\"")}",
                  "lang": "${catalog.lang}",
                  "version": "${catalog.versionName}",
                  "site": "${catalog.description.replace("\"", "\\\"")}",
                  "icon": "${catalog.iconUrl.replace("\"", "\\\"")}"
                }
            """.trimIndent()
            metadataFile.writeText(metadata)
            
            // No need to download icon - Coil will handle it via iconUrl with caching
            
            // Verify files were created
            Log.info("AndroidLocalInstaller: JS file created=${jsFile.exists()}, size=${if (jsFile.exists()) jsFile.length() else 0}")
            Log.info("AndroidLocalInstaller: Metadata file created=${metadataFile.exists()}, size=${if (metadataFile.exists()) metadataFile.length() else 0}")
            
            // Notify installation complete
            installationChanges.notifyAppInstall(catalog.pkgName)
            Log.info("AndroidLocalInstaller: Successfully installed JS plugin ${catalog.name}")
            
            send(InstallStep.Success)
            send(InstallStep.Idle)
            
        } catch (e: Exception) {
            Log.error("AndroidLocalInstaller: Failed to install JS plugin: ${catalog.name}", e)
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
            val file = File(getSimpleStorage.extensionDirectory().toFile(), pkgName)
            val cacheFile = File(getSimpleStorage.cacheExtensionDir().toFile(), pkgName)
            file.deleteRecursively()
            cacheFile.deleteRecursively()
            
            // Also try to uninstall as JS plugin from secure storage
            val jsPluginsDir = ireader.domain.storage.SecureStorageHelper.getJsPluginsDir(context)
            
            val jsFile = File(jsPluginsDir, "$pkgName.js")
            val metadataFile = File(jsPluginsDir, "$pkgName.meta.json")
            
            if (jsFile.exists()) {
                jsFile.delete()
            }
            
            // Also delete metadata file
            if (metadataFile.exists()) {
                metadataFile.delete()
            }
            
            installationChanges.notifyAppUninstall(pkgName)
            InstallStep.Success
        } catch (e: Throwable) {
            InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper))
        }
    }

}