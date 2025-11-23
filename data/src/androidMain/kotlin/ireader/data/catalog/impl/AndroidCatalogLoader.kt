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
import ireader.core.prefs.PreferenceStoreFactory
import ireader.core.prefs.PrefixedPreferenceStore
import ireader.core.source.Source
import ireader.core.source.TestSource
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.js.loader.JSPluginLoader
import ireader.domain.js.util.JSPluginLogger
import ireader.domain.models.entities.CatalogBundled
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.utils.extensions.withIOContext
import ireader.i18n.BuildKonfig
import ireader.i18n.LocalizeHelper
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.io.File
/**
 * Class that handles the loading of the catalogs installed in the system and the app.
 */
class AndroidCatalogLoader(
    private val context: Context,
    private val httpClients: HttpClients,
    val uiPreferences: UiPreferences,
    val simpleStorage: GetSimpleStorage,
    val localizeHelper: LocalizeHelper,
    private val preferenceStore: PreferenceStoreFactory
) : CatalogLoader, ireader.domain.catalogs.service.AsyncPluginLoader {

    private val pkgManager = context.packageManager
    private val catalogPreferences = preferenceStore.create("catalogs_data")
    
    // Create secure directories for extension loading
    private val secureExtensionsDir = File(context.codeCacheDir, "secure_extensions").apply { mkdirs() }
    private val secureDexCacheDir = File(context.codeCacheDir, "dex-cache").apply { mkdirs() }
    
    // JavaScript plugin loader - uses converter approach (no JS engine needed!)
    private val jsPluginLoader: JSPluginLoader by lazy {
        // Use the appropriate directory based on user preference
        val jsPluginsDir = getJSPluginsDirectory()
        JSPluginLoader(jsPluginsDir, httpClients.default, preferenceStore)
    }
    
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
    
    init {
        // Initialize secure directories at startup
        createSecureDirectories()
    }
    
    /**
     * Creates secure directories for extension loading
     */
    private fun createSecureDirectories() {
        try {
            // Ensure secure directories exist
            secureExtensionsDir.mkdirs()
            secureDexCacheDir.mkdirs()
            
            // Clean any stale extension files
            secureExtensionsDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".apk")) {
                    try {
                        file.delete()
                    } catch (e: Exception) {
                        Log.warn("Failed to delete stale extension file: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.error("Failed to create secure directories", e)
        }
    }

    /**
     * Return a list of all the installed catalogs initialized concurrently.
     */
    @SuppressLint("QueryPermissionsNeeded")
    override suspend fun loadAll(): List<CatalogLocal> {
        val bundled = mutableListOf<CatalogLocal>()

        // Add Local Source for reading local novels
        val localSourceCatalog = CatalogBundled(
            source = ireader.core.source.LocalSource(),
            description = "Read novels from local storage",
            name = "Local Source"
        )
        bundled.add(localSourceCatalog)

        if (BuildKonfig.DEBUG) {
            val testCatalog = CatalogBundled(
                TestSource(),
                "Source used for testing"
            )
            bundled.add(testCatalog)
            bundled.add(testCatalog)
        }

        val systemPkgs =
            pkgManager.getInstalledPackages(PACKAGE_FLAGS).filter(::isPackageAnExtension)

        val localPkgs= simpleStorage.extensionDirectory().listFiles()
            .orEmpty()
            .filter { it.isDirectory }
            .map { File(it, it.name + ".apk") }
            .filter { it.exists() }

        val cachePkgs = simpleStorage.cacheExtensionDir().listFiles()
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
            val localCache = if (uiPreferences.showLocalCatalogs().get()) {
                cachePkgs.map { file ->
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
            val deferred = (local + localCache + system)
            deferred.awaitAll()
        }.filterNotNull()

        // Load JavaScript plugins if enabled
        val jsPluginsEnabled = uiPreferences.enableJSPlugins().get()
        Log.warn("JS Plugins enabled: $jsPluginsEnabled")
        
        val jsPlugins = if (jsPluginsEnabled) {
            try {
                val jsPluginsDir = getJSPluginsDirectory()
                Log.warn("JS Plugins directory: ${jsPluginsDir.absolutePath}, exists: ${jsPluginsDir.exists()}")
                if (jsPluginsDir.exists()) {
                    val files = jsPluginsDir.listFiles()
                    Log.warn("JS Plugin files found: ${files?.size ?: 0}")
                    files?.forEach { file ->
                        Log.warn("  - ${file.name} (${file.length()} bytes)")
                    }
                }
                
                val startTime = System.currentTimeMillis()
                // Load stub plugins instantly for fast startup
                val stubs = jsPluginLoader.loadStubPlugins()
                val duration = System.currentTimeMillis() - startTime
                
                Log.warn("Loaded ${stubs.size} JS plugin stubs in ${duration}ms")
                JSPluginLogger.logInfo("AndroidCatalogLoader", 
                    "Loaded ${stubs.size} JS plugin stubs in ${duration}ms")
                
                // Note: Actual plugins will be loaded in background by CatalogStore
                // On first run (no stubs), background loading will load all plugins
                // On subsequent runs, background loading will replace stubs with actual plugins
                stubs
            } catch (e: Exception) {
                Log.error(e, "Failed to load JS plugin stubs")
                emptyList()
            }
        } else {
            Log.warn("JS Plugins are disabled in preferences")
            emptyList()
        }

        return (bundled + installedCatalogs + jsPlugins).distinctBy { it.sourceId }.toSet().toList()
    }

    /**
     * Attempts to load an catalog from the given package name. It checks if the catalog
     * contains the required feature flag before trying to load it.
     */
    override fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally? {
        // Check if this is a JS plugin in either location - if so, return null as JS plugins are loaded via loadAll()
        // Check cache directory
        val cacheJsPluginsDir = File(context.cacheDir, "js-plugins")
        val cacheJsFile = File(cacheJsPluginsDir, "$pkgName.js")
        
        // Check external storage directory
        val externalDir = context.getExternalFilesDir(null)?.parentFile?.parentFile?.parentFile
        val externalJsFile = if (externalDir != null) {
            File(File(File(externalDir, "ireader"), "js-plugins"), "$pkgName.js")
        } else null
        
        if ((cacheJsFile.exists() && cacheJsFile.canRead()) || 
            (externalJsFile?.exists() == true && externalJsFile.canRead())) {
            // JS plugins are handled by loadAll(), not loadLocalCatalog()
            // Return null to avoid the "catalog not found" warning
            return null
        }
        
        // Try to load as traditional APK extension
        val file = File(simpleStorage.extensionDirectory(), "${pkgName}/${pkgName}.apk")
        val cacheFile = File(simpleStorage.cacheExtensionDir(), "${pkgName}/${pkgName}.apk")
        val finalFile = if (file.canRead() && file.canRead()) {
            file
        } else {
            cacheFile
        }
        val pkgInfo = if (finalFile.exists() && finalFile.canRead()) {
            try {
                pkgManager.getPackageArchiveInfo(finalFile.absolutePath, PACKAGE_FLAGS)
            } catch (e: Exception) {
                // APK is corrupted or invalid
                Log.warn("Failed to read APK for catalog {}: {}", pkgName, e.message)
                null
            }
        } else {
            null
        }
        if (pkgInfo == null) {
            // Don't log warning for JS plugins - they're handled separately
            // Only log if there's no JS file either
            val jsPluginsDir = getJSPluginsDirectory()
            val jsFile = File(jsPluginsDir, "$pkgName.js")
            if (!jsFile.exists()) {
                Log.warn("The requested catalog {} wasn't found (not an APK or JS plugin)", pkgName)
            }
            return null
        }

        return loadLocalCatalog(pkgName, pkgInfo, finalFile)
    }

    /**
     * Attempts to load an catalog from the given package name. It checks if the catalog
     * contains the required feature flag before trying to load it.
     */
    override fun loadSystemCatalog(pkgName: String): CatalogInstalled.SystemWide? {
        val iconFile = File(simpleStorage.extensionDirectory(), "${pkgName}/${pkgName}.png")
        val cacheFile = File(context.cacheDir, "${pkgName}/${pkgName}.apk")
        val icon = if (iconFile.exists() && iconFile.canRead()) {
            iconFile
        } else {
            cacheFile
        }
        val pkgInfo = try {
            pkgManager.getPackageInfo(pkgName, PACKAGE_FLAGS)
        } catch (error: NameNotFoundException) {
            // Unlikely, but the package may have been uninstalled at this point
            Log.warn("Failed to load catalog: the package {} isn't installed", pkgName)
            return null
        }
        return loadSystemCatalog(pkgName, pkgInfo,icon)
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
    ): CatalogInstalled.Locally? {
        val data = validateMetadata(pkgName, pkgInfo) ?: return null
        
        try {
            // Create a fresh copy to avoid any "writable dex file" issues
            val secureApkFile = File(secureExtensionsDir, "${pkgName}.apk")
            if (secureApkFile.exists()) {
                secureApkFile.delete()
            }
            
            // Copy the APK to the code cache directory which is allowed for DEX loading
            file.copyTo(secureApkFile, overwrite = true)
            
            // Make sure the permissions are correct (readable but not writable)
            secureApkFile.setReadOnly()
            
            // Use the code cache directory for dex output
            val dexOutputDir = File(secureDexCacheDir, pkgName).apply { mkdirs() }.absolutePath
            
            // Now load from the secure location
            val loader = DexClassLoader(secureApkFile.absolutePath, dexOutputDir, null, context.classLoader)
            val source = loadSource(pkgName, loader, data) ?: return null
            
            return CatalogInstalled.Locally(
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
        } catch (e: Exception) {
            Log.warn(e, "Failed to load local catalog $pkgName")
            return null
        }
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
        iconFile: File? = null
    ): CatalogInstalled.SystemWide? {
        val data = validateMetadata(pkgName, pkgInfo) ?: return null

        val loader = PathClassLoader(pkgInfo.applicationInfo!!.sourceDir, null, context.classLoader)
        val source = loadSource(pkgName, loader, data)

        return CatalogInstalled.SystemWide(
            name = source?.name ?: localizeHelper.localize(Res.string.unknown),
            description = data.description,
            source = source,
            pkgName = pkgName,
            versionName = data.versionName,
            versionCode = data.versionCode,
            nsfw = data.nsfw,
            iconUrl = data.icon,
            installDir = iconFile?.parentFile,
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
        val majorLibVersion = versionName!!.substringBefore('.').toInt()
        if (majorLibVersion < LIB_VERSION_MIN || majorLibVersion > LIB_VERSION_MAX) {
            val exception = "Failed to load catalog, the package {} lib version is {}," +
                    "while only versions {} to {} are allowed"
            Log.warn(exception, pkgName, majorLibVersion, LIB_VERSION_MIN, LIB_VERSION_MAX)
            return null
        }

        val appInfo = pkgInfo.applicationInfo

        val metadata = appInfo!!.metaData
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

        return ValidatedData(
            versionCode,
            versionName,
            description,
            icon,
            nsfw,
            classToLoad,
            dependencies
        )
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

    /**
     * Load actual JS plugins in the background, replacing stubs.
     * @param onPluginLoaded Callback when each plugin is loaded
     */
    override suspend fun loadJSPluginsAsync(onPluginLoaded: (ireader.domain.models.entities.JSPluginCatalog) -> Unit) {
        if (!uiPreferences.enableJSPlugins().get()) return
        
        try {
            JSPluginLogger.logInfo("AndroidCatalogLoader", "Starting async JS plugin loading")
            jsPluginLoader.loadPluginsAsync(onPluginLoaded)
        } catch (e: Exception) {
            Log.error("Failed to load JS plugins asynchronously: ${e.message}", e)
        }
    }
    
    /**
     * Get the JS plugin loader for advanced operations.
     */
    fun getJSPluginLoader(): JSPluginLoader = jsPluginLoader

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
