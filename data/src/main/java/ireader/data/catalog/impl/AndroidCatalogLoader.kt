package ireader.data.catalog.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import dalvik.system.DexClassLoader
import dalvik.system.PathClassLoader
import ireader.core.http.HttpClients
import ireader.core.log.Log
import ireader.core.prefs.AndroidPreferenceStore
import ireader.core.prefs.PrefixedPreferenceStore
import ireader.core.source.Source
import ireader.core.source.TestSource
import ireader.domain.R
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.utils.extensions.withIOContext
import ireader.i18n.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.koin.core.annotation.Single
import java.io.File

/**
 * Class that handles the loading of the catalogs installed in the system and the app.
 */
@Single
class AndroidCatalogLoader(
    private val context: Context,
    private val httpClients: HttpClients,
    val uiPreferences: UiPreferences
) : CatalogLoader {

    private val pkgManager = context.packageManager

    private val catalogPreferences = AndroidPreferenceStore(context, "catalogs_data", false)

    /**
     * Return a list of all the installed catalogs initialized concurrently.
     */
    @SuppressLint("QueryPermissionsNeeded")
    override suspend fun loadAll(): List<ireader.common.models.entities.CatalogLocal> {
        val bundled = mutableListOf<ireader.common.models.entities.CatalogLocal>()

        if (BuildConfig.DEBUG) {
            val testCatalog = ireader.common.models.entities.CatalogBundled(
                TestSource(),
                "Source used for testing"
            )
            bundled.add(testCatalog)
        }
        val systemPkgs =
            pkgManager.getInstalledPackages(PACKAGE_FLAGS).filter(::isPackageAnExtension)
        val localPkgs = File(context.filesDir, "catalogs").listFiles()
            .orEmpty()
            .filter { it.isDirectory }
            .map { File(it, it.name + ".apk") }
            .filter { it.exists() }

        // Load each catalog concurrently and wait for completion
        val installedCatalogs = withIOContext {
            val local = if (uiPreferences.showLocalCatalogs().get()) {
                localPkgs.map { file ->
                    async(Dispatchers.Default) {
                        loadLocalCatalog(file.nameWithoutExtension)
                    }
                }
            } else emptyList()
            val system = if (uiPreferences.showSystemWideCatalogs().get()) {
                systemPkgs.map { pkgInfo ->
                    async(Dispatchers.Default) {
                        loadSystemCatalog(pkgInfo.packageName, pkgInfo)
                    }
                }
            } else emptyList()
            val deferred = local + system
            deferred.awaitAll()
        }.filterNotNull().distinctBy { it.pkgName }

        return bundled + installedCatalogs
    }

    /**
     * Attempts to load an catalog from the given package name. It checks if the catalog
     * contains the required feature flag before trying to load it.
     */
    override fun loadLocalCatalog(pkgName: String): ireader.common.models.entities.CatalogInstalled.Locally? {
        val file = File(context.filesDir, "catalogs/$pkgName/$pkgName.apk")
        val pkgInfo = if (file.exists()) {
            pkgManager.getPackageArchiveInfo(file.absolutePath, PACKAGE_FLAGS)
        } else {
            null
        }
        if (pkgInfo == null) {
            Log.warn("The requested catalog {} wasn't found", pkgName)
            return null
        }

        return loadLocalCatalog(pkgName, pkgInfo, file)
    }

    /**
     * Attempts to load an catalog from the given package name. It checks if the catalog
     * contains the required feature flag before trying to load it.
     */
    override fun loadSystemCatalog(pkgName: String): ireader.common.models.entities.CatalogInstalled.SystemWide? {
        val pkgInfo = try {
            pkgManager.getPackageInfo(pkgName, PACKAGE_FLAGS)
        } catch (error: NameNotFoundException) {
            // Unlikely, but the package may have been uninstalled at this point
            Log.warn("Failed to load catalog: the package {} isn't installed", pkgName)
            return null
        }
        return loadSystemCatalog(pkgName, pkgInfo)
    }

    /**
     * Loads a catalog given its package name.
     *
     * @param pkgName The package name of the catalog to load.
     * @param pkgInfo The package info of the catalog.
     */
    private fun loadLocalCatalog(
        pkgName: String,
        pkgInfo: PackageInfo,
        file: File,
    ): ireader.common.models.entities.CatalogInstalled.Locally? {
        val data = validateMetadata(pkgName, pkgInfo) ?: return null
        val dexOutputDir = context.codeCacheDir.absolutePath
        val loader = DexClassLoader(file.absolutePath, dexOutputDir, null, context.classLoader)
        val source = loadSource(pkgName, loader, data) ?: return null

        return ireader.common.models.entities.CatalogInstalled.Locally(
            name = source.name,
            description = data.description,
            source = source,
            pkgName = pkgName,
            versionName = data.versionName,
            versionCode = data.versionCode,
            nsfw = data.nsfw,
            installDir = file.parentFile!!,
            iconUrl = data.icon
        )
    }

    /**
     * Loads a catalog given its package name.
     *
     * @param pkgName The package name of the catalog to load.
     * @param pkgInfo The package info of the catalog.
     */
    private fun loadSystemCatalog(
        pkgName: String,
        pkgInfo: PackageInfo,
    ): ireader.common.models.entities.CatalogInstalled.SystemWide? {
        val data = validateMetadata(pkgName, pkgInfo) ?: return null

        val loader = PathClassLoader(pkgInfo.applicationInfo.sourceDir, null, context.classLoader)
        val source = loadSource(pkgName, loader, data)

        return ireader.common.models.entities.CatalogInstalled.SystemWide(
            name = source?.name ?: context.resources.getString(R.string.unknown),
            description = data.description,
            source = source,
            pkgName = pkgName,
            versionName = data.versionName,
            versionCode = data.versionCode,
            nsfw = data.nsfw,
            iconUrl = data.icon
        )
    }

    /**
     * Returns true if the given package is an catalog.
     *
     * @param pkgInfo The package info of the application.
     */
    private fun isPackageAnExtension(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.reqFeatures.orEmpty().any { it.name == EXTENSION_FEATURE }
    }

    private fun validateMetadata(pkgName: String, pkgInfo: PackageInfo): ValidatedData? {
        if (!isPackageAnExtension(pkgInfo)) {
            Log.warn("Failed to load catalog, package {} isn't a catalog", pkgName)
            return null
        }

        if (pkgName != pkgInfo.packageName) {
            Log.warn(
                "Failed to load catalog, package name mismatch: Provided {} Actual {}",
                pkgName, pkgInfo.packageName
            )
            return null
        }

        @Suppress("DEPRECATION")
        val versionCode = pkgInfo.versionCode
        val versionName = pkgInfo.versionName

        // Validate lib version
        val majorLibVersion = versionName.substringBefore('.').toInt()
        if (majorLibVersion < LIB_VERSION_MIN || majorLibVersion > LIB_VERSION_MAX) {
            val exception = "Failed to load catalog, the package {} lib version is {}," +
                "while only versions {} to {} are allowed"
            Log.warn(exception, pkgName, majorLibVersion, LIB_VERSION_MIN, LIB_VERSION_MAX)
            return null
        }

        val appInfo = pkgInfo.applicationInfo

        val metadata = appInfo.metaData
        val sourceClassName = metadata.getString(METADATA_SOURCE_CLASS)?.trim()
        if (sourceClassName == null) {
            Log.warn("Failed to load catalog, the package {} didn't define source class", pkgName)
            return null
        }

        val description = metadata.getString(METADATA_DESCRIPTION).orEmpty()
        val icon = metadata.getString(METADATA_ICON).orEmpty()

        val classToLoad = if (sourceClassName.startsWith(".")) {
            pkgInfo.packageName + sourceClassName
        } else {
            sourceClassName
        }

        val nsfw = metadata.getInt(METADATA_NSFW, 0) == 1

        val preferenceSource = PrefixedPreferenceStore(catalogPreferences, pkgName)
        val dependencies = ireader.core.source.Dependencies(httpClients, preferenceSource)

        return ValidatedData(versionCode, versionName, description,icon, nsfw, classToLoad, dependencies)
    }

    private fun loadSource(pkgName: String, loader: ClassLoader, data: ValidatedData): Source? {
        return try {
            val obj = Class.forName(data.classToLoad, false, loader)
                .getConstructor(ireader.core.source.Dependencies::class.java)
                .newInstance(data.dependencies)

            obj as? Source ?: throw Exception("Unknown source class type! ${obj.javaClass}")
        } catch (e: Throwable) {
            Log.warn(e, "Failed to load catalog {}", pkgName)
            return null
        }
    }

    private data class ValidatedData(
        val versionCode: Int,
        val versionName: String,
        val description: String,
        val icon: String,
        val nsfw: Boolean,
        val classToLoad: String,
        val dependencies: ireader.core.source.Dependencies,
    )

    private companion object {
        const val EXTENSION_FEATURE = "ireader"
        const val METADATA_SOURCE_CLASS = "source.class"
        const val METADATA_DESCRIPTION = "source.description"
        const val METADATA_NSFW = "source.nsfw"
        const val METADATA_ICON = "source.icon"
        const val LIB_VERSION_MIN = 1
        const val LIB_VERSION_MAX = 1

        const val PACKAGE_FLAGS = PackageManager.GET_CONFIGURATIONS or PackageManager.GET_META_DATA
    }
}
