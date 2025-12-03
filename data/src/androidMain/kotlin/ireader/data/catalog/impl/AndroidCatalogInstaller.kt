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
import okio.Path.Companion.toPath
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
    private val localizeHelper: LocalizeHelper,
    private val uiPreferences: ireader.domain.preferences.prefs.UiPreferences
) : CatalogInstaller {

    /**
     * The client used for http requests.
     */
    private val client get() = httpClient.default
    
    /**
     * Get the JS plugins directory based on user preference.
     * If savedLocalCatalogLocation is true, uses app cache (no permissions needed).
     * Otherwise uses external storage for easier access.
     */
    private fun getJSPluginsDirectory(): File {
        val useCacheDir = uiPreferences.savedLocalCatalogLocation().get()
        
        return if (useCacheDir) {
            // Use app cache directory - no permissions needed
            File(context.cacheDir, "js-plugins").apply { mkdirs() }
        } else {
            // Use external storage for easier access
            val externalDir = context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile
            val ireaderDir = File(externalDir, "ireader")
            val jsPluginsDir = File(ireaderDir, "js-plugins")
            jsPluginsDir.mkdirs()
            jsPluginsDir
        }
    }

    /**
     * Adds the given extension to the downloads queue and returns an observable containing its
     * step in the installation process.
     *
     * @param catalog The catalog to install.
     */
    override fun install(catalog: CatalogRemote): Flow<InstallStep> =
        flow {
            emit(InstallStep.Downloading)
            
            // This installer only handles APK installations using Package Installer
            // JS plugins should be routed to AndroidLocalInstaller by InstallCatalogImpl
            val tmpApkFile = File(context.cacheDir, "${catalog.pkgName}.apk")
            val tmpIconFile = File(context.cacheDir, "${catalog.pkgName}.png")
            try {
                    val apkResponse: ByteReadChannel = client.get(catalog.pkgUrl) {
                        headers.append(HttpHeaders.CacheControl, "no-store")
                    }.body()
                    val iconResponse: ByteReadChannel = client.get(catalog.iconUrl) {
                        headers.append(HttpHeaders.CacheControl, "no-store")
                    }.body()
                    apkResponse.saveTo(tmpApkFile.absolutePath.toPath(), okio.FileSystem.SYSTEM)
                    
                    // Save the icon to storage
                    iconResponse.saveTo(tmpIconFile.absolutePath.toPath(), okio.FileSystem.SYSTEM)
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
            var deleted = false
            
            // Try to uninstall traditional APK
            val apkResult = packageInstaller.uninstall(pkgName)
            if (apkResult is InstallStep.Success) {
                deleted = true
            }
            
            // Try to delete JS plugin files from both possible locations
            // Cache directory
            val cacheJsPluginsDir = File(context.cacheDir, "js-plugins")
            val cacheJsFile = File(cacheJsPluginsDir, "$pkgName.js")
            if (cacheJsFile.exists()) {
                deleted = cacheJsFile.delete() || deleted
            }
            val cacheIconFile = File(cacheJsPluginsDir, "$pkgName.png")
            if (cacheIconFile.exists()) {
                cacheIconFile.delete()
            }
            
            // External storage directory
            val externalDir = context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile
            if (externalDir != null) {
                val externalJsPluginsDir = File(File(externalDir, "ireader"), "js-plugins")
                val externalJsFile = File(externalJsPluginsDir, "$pkgName.js")
                if (externalJsFile.exists()) {
                    deleted = externalJsFile.delete() || deleted
                }
                val externalIconFile = File(externalJsPluginsDir, "$pkgName.png")
                if (externalIconFile.exists()) {
                    externalIconFile.delete()
                }
            }
            
            installationChanges.notifyAppUninstall(pkgName)
            if (deleted) InstallStep.Success else apkResult
        } catch (e: Throwable) {
            InstallStep.Error(UiText.ExceptionString(e).asString(localizeHelper))
        }
    }
}
