package ireader.data.catalog.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import dalvik.system.DexClassLoader
import dalvik.system.InMemoryDexClassLoader
import dalvik.system.PathClassLoader
import java.nio.ByteBuffer
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
import okio.Path.Companion.toPath
import java.io.File

// Extension to explicitly convert String to okio.Path
private fun String.toOkioPath(): okio.Path = this.toPath()

/**
 * Class that handles the loading of the catalogs installed in the system and the app.
 */
class AndroidCatalogLoader(
    private val context: Context,
    private val httpClients: HttpClients,
    val uiPreferences: UiPreferences,
    val simpleStorage: GetSimpleStorage,
    val localizeHelper: LocalizeHelper,
    private val preferenceStore: PreferenceStoreFactory,
    private val communitySource: ireader.domain.community.CommunitySource,
    private val pluginManager: ireader.domain.plugins.PluginManager
) : CatalogLoader, ireader.domain.catalogs.service.AsyncPluginLoader {

    private val pkgManager = context.packageManager
    private val catalogPreferences = preferenceStore.create("catalogs_data")
    
    // Create secure directories for extension loading
    private val secureExtensionsDir = File(context.codeCacheDir, "secure_extensions").apply { mkdirs() }
    private val secureDexCacheDir = File(context.codeCacheDir, "dex-cache").apply { mkdirs() }
    
    // JavaScript plugin loader - uses converter approach (no JS engine needed!)
    // Uses a lambda to get the directory dynamically, so it picks up storage folder changes
    private val jsPluginLoader: JSPluginLoader by lazy {
        JSPluginLoader(
            pluginsDirectoryProvider = { getJSPluginsDirectory().absolutePath.toPath() },
            httpClient = httpClients.default,
            preferenceStoreFactory = preferenceStore
        )
    }
    
    /**
     * Get the JS plugins directory using secure storage.
     */
    private fun getJSPluginsDirectory(): File {
        return ireader.domain.storage.SecureStorageHelper.getJsPluginsDir(context)
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
                        // Ignore errors
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore errors
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
        
        // Add Community Source for community-translated content
        communitySource?.let { source ->
            val communityCatalog = ireader.domain.models.entities.CommunityCatalog(
                source = source,
                description = "Browse and read community-translated novels"
            )
            bundled.add(communityCatalog)
        }

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

        val localPkgs= simpleStorage.extensionDirectory().toFile().listFiles()
            .orEmpty()
            .filter { it.isDirectory }
            .map { File(it, it.name + ".apk") }
            .filter { it.exists() }

        val cachePkgs = simpleStorage.cacheExtensionDir().toFile().listFiles()
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
        val jsPlugins = if (uiPreferences.enableJSPlugins().get()) {
            try {
                // Load stub plugins instantly for fast startup
                val stubs = jsPluginLoader.loadStubPlugins()
                
                // Note: Actual plugins will be loaded in background by CatalogStore
                // On first run (no stubs), background loading will load all plugins
                // On subsequent runs, background loading will replace stubs with actual plugins
                stubs
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        return (bundled + installedCatalogs + jsPlugins).distinctBy { it.sourceId }.toSet().toList()
    }

    /**
     * Attempts to load an catalog from the given package name. It checks if the catalog
     * contains the required feature flag before trying to load it.
     */
    override fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally? {
        // Check if this is a JS plugin - check both SAF and fallback locations
        val jsFileName = "$pkgName.js"
        if (ireader.domain.storage.SecureStorageHelper.jsPluginExists(context, jsFileName)) {
            // JS plugins are handled by loadAll(), not loadLocalCatalog()
            // Return null to avoid the "catalog not found" warning
            return null
        }
        
        // Try to load as traditional APK extension
        val file = File(simpleStorage.extensionDirectory().toFile(), "${pkgName}/${pkgName}.apk")
        val cacheFile = File(simpleStorage.cacheExtensionDir().toFile(), "${pkgName}/${pkgName}.apk")
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
                null
            }
        } else {
            null
        }
        if (pkgInfo == null) {
            return null
        }

        return loadLocalCatalog(pkgName, pkgInfo, finalFile)
    }

    /**
     * Attempts to load an catalog from the given package name. It checks if the catalog
     * contains the required feature flag before trying to load it.
     */
    override fun loadSystemCatalog(pkgName: String): CatalogInstalled.SystemWide? {
        val iconFile = File(simpleStorage.extensionDirectory().toFile(), "${pkgName}/${pkgName}.png")
        val secureCache = ireader.domain.storage.SecureStorageHelper.getBaseCacheDir(context)
        val cacheFile = File(secureCache, "${pkgName}/${pkgName}.apk")
        val icon = if (iconFile.exists() && iconFile.canRead()) {
            iconFile
        } else {
            cacheFile
        }
        val pkgInfo = try {
            pkgManager.getPackageInfo(pkgName, PACKAGE_FLAGS)
        } catch (error: NameNotFoundException) {
            // Unlikely, but the package may have been uninstalled at this point
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
            val loader = createClassLoader(file, pkgName)
            val source = loadSource(pkgName, loader, data) ?: return null
            
            return CatalogInstalled.Locally(
                name = source.name,
                description = data.description,
                source = source,
                pkgName = pkgName,
                versionName = data.versionName,
                versionCode = data.versionCode,
                nsfw = data.nsfw,
                installDir = file.parentFile!!.absolutePath.toOkioPath(),
                iconUrl = data.icon
            )
        } catch (e: Exception) {
            Log.error("Failed to load local catalog $pkgName", e)
            return null
        }
    }
    
    /**
     * Creates the appropriate ClassLoader based on Android version.
     * - Android 15+ (API 35+): Uses InMemoryDexClassLoader for better security
     * - Android 14+ (API 34+): Uses DexClassLoader with secure codeCacheDir
     * - Older versions: Uses DexClassLoader with standard approach
     */
    private fun createClassLoader(file: File, pkgName: String): ClassLoader {
        // Android 15+ (API 35): Use InMemoryDexClassLoader for enhanced security
        if (Build.VERSION.SDK_INT >= 35) {
            return try {
                createInMemoryClassLoader(file, pkgName)
            } catch (e: Exception) {
                Log.warn("InMemoryDexClassLoader failed, falling back to DexClassLoader", e)
                createSecureDexClassLoader(file, pkgName)
            }
        }
        
        // Android 14+ (API 34): Use secure DexClassLoader
        return createSecureDexClassLoader(file, pkgName)
    }
    
    /**
     * Creates an InMemoryDexClassLoader for Android 15+.
     * This loads the DEX directly into memory without writing to disk,
     * which is more secure and avoids file permission issues.
     */
    @Suppress("NewApi")
    private fun createInMemoryClassLoader(file: File, pkgName: String): ClassLoader {
        // Read the APK/DEX file into memory
        val dexBytes = file.readBytes()
        val buffer = ByteBuffer.wrap(dexBytes)
        
        return InMemoryDexClassLoader(buffer, context.classLoader)
    }
    
    /**
     * Creates a secure DexClassLoader for Android 14+.
     * Copies the APK to codeCacheDir and sets read-only permissions.
     */
    private fun createSecureDexClassLoader(file: File, pkgName: String): ClassLoader {
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
        return DexClassLoader(secureApkFile.absolutePath, dexOutputDir, null, context.classLoader)
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
            installDir = iconFile?.parentFile?.absolutePath?.toOkioPath(),
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
            return null
        }

        if (pkgName != pkgInfo.packageName) {
            return null
        }

        @Suppress("DEPRECATION")
        val versionCode = pkgInfo.versionCode
        val versionName = pkgInfo.versionName

        // Validate lib version
        val majorLibVersion = versionName!!.substringBefore('.').toInt()
        if (majorLibVersion < LIB_VERSION_MIN || majorLibVersion > LIB_VERSION_MAX) {
            return null
        }

        val appInfo = pkgInfo.applicationInfo

        val metadata = appInfo!!.metaData
        val sourceClassName = metadata.getString(METADATA_SOURCE_CLASS)?.trim()
        if (sourceClassName == null) {
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
            jsPluginLoader.loadPluginsAsync(onPluginLoaded)
        } catch (e: Exception) {
            // Ignore errors
        }
    }
    
    /**
     * Get the JS plugin loader for advanced operations.
     */
    fun getJSPluginLoader(): JSPluginLoader = jsPluginLoader
    
    /**
     * Check if JS engine is missing (plugins installed but can't be loaded).
     * Returns true if there are JS plugin files but no JS engine to run them.
     */
    override fun isJSEngineMissing(): Boolean = jsPluginLoader.jsEngineMissing
    
    /**
     * Get the number of JS plugins pending due to missing engine.
     */
    override fun getPendingJSPluginsCount(): Int = jsPluginLoader.pendingPluginsCount
    
    /**
     * Load engine plugins (.iplugin files like J2V8) before JS plugins.
     * This ensures the JS engine is available when JS plugins are loaded.
     */
    override suspend fun loadEnginePlugins() {
        try {
            Log.info("AndroidCatalogLoader: Loading engine plugins...")
            pluginManager.loadPlugins()
            
            // Check if J2V8 ClassLoader is now available
            val j2v8ClassLoader = ireader.domain.plugins.PluginClassLoader.getClassLoader("io.github.ireaderorg.plugins.j2v8-engine")
            Log.info("AndroidCatalogLoader: J2V8 ClassLoader available: ${j2v8ClassLoader != null}")
            
            // Try to initialize J2V8 now that the plugin is loaded
            if (j2v8ClassLoader != null) {
                Log.info("AndroidCatalogLoader: Attempting to initialize J2V8...")
                val initialized = ireader.domain.js.loader.J2V8EngineHelper.tryInitializeJ2V8()
                Log.info("AndroidCatalogLoader: J2V8 initialized: $initialized")
            }
            
            Log.info("AndroidCatalogLoader: Engine plugins loaded")
        } catch (e: Exception) {
            Log.error("AndroidCatalogLoader: Failed to load engine plugins", e)
        }
    }

    private companion object {
        const val EXTENSION_FEATURE = "ireader"
        const val METADATA_SOURCE_CLASS = "source.class"
        const val METADATA_DESCRIPTION = "source.description"
        const val METADATA_NSFW = "source.nsfw"
        const val METADATA_ICON = "source.icon"
        const val LIB_VERSION_MIN = 2
        const val LIB_VERSION_MAX = 2

        const val PACKAGE_FLAGS = PackageManager.GET_CONFIGURATIONS or PackageManager.GET_META_DATA
    }
}
