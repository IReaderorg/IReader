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
import ireader.data.catalog.impl.tsundoku.ChildFirstPathClassLoader
import ireader.data.catalog.impl.tsundoku.TsundokuCatalogSource
import ireader.data.catalog.impl.tsundoku.TsundokuExtensionLoader
import ireader.data.catalog.impl.tsundoku.TsundokuValidatedData
import ireader.domain.catalogs.service.CatalogLoader
import ireader.domain.js.loader.JSPluginLoader
import ireader.domain.models.entities.CatalogBundled
import ireader.domain.models.entities.CatalogInstalled
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.files.GetSimpleStorage
import ireader.domain.utils.extensions.withIOContext
import ireader.i18n.BuildKonfig
import ireader.i18n.LocalizeHelper
import ireader.i18n.resources.Res
import ireader.i18n.resources.unknown
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import okio.Path.Companion.toPath
import java.io.File

// Extension to explicitly convert String to okio.Path
private fun String.toOkioPath(): okio.Path = this.toPath()

/**
 * Typed catalog load errors for diagnostics and retry logic.
 * Stored in [AndroidCatalogLoader.failedCatalogs] for post-load reporting.
 */
enum class CatalogLoadError {
    PACKAGE_NOT_FOUND,
    INVALID_METADATA,
    UNSUPPORTED_LIB_VERSION,
    CLASS_NOT_FOUND,
    INSTANTIATION_FAILED,
    DEX_COMPILATION_FAILED,
    IO_ERROR,
    SECURITY_ERROR,
    UNKNOWN
}

/**
 * Class that handles the loading of the catalogs installed in the system and the app.
 *
 * Redesigned for Android 15 (API 35) compatibility and error resilience:
 * - Uses InMemoryDexClassLoader on API 28+ to avoid DEX-on-disk issues
 * - Implements retry logic with exponential backoff for recoverable failures
 * - Provides detailed error diagnostics via [CatalogLoadError]
 * - Handles Android 15's stricter dynamic code loading restrictions
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
    
    // Track failed catalogs for retry on next load
    private val failedCatalogs = mutableMapOf<String, CatalogLoadError>()
    
    // Maximum retry attempts for recoverable failures
    // ponytail: constants + package flags in one companion, no split
    
    // JavaScript plugin loader - uses converter approach (no JS engine needed!)
    // Uses a lambda to get the directory dynamically, so it picks up storage folder changes
    private val jsPluginLoader: JSPluginLoader by lazy {
        JSPluginLoader(
            pluginsDirectoryProvider = { getJSPluginsDirectory().absolutePath.toPath() },
            httpClient = httpClients.default,
            preferenceStoreFactory = preferenceStore,
            fileSystem = okio.FileSystem.SYSTEM
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
        // No manual DEX cache directories needed with PathClassLoader.
        // Android handles DEX optimization automatically via dexopt.
    }

    /**
     * Return a list of all the installed catalogs initialized concurrently.
     * 
     * Error handling strategy:
     * 1. Each catalog loads independently - one failure doesn't affect others
     * 2. Recoverable failures are retried up to MAX_RETRY_ATTEMPTS times
     * 3. Failed catalogs are tracked for potential future retry
     * 4. All errors are logged with context for debugging
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
        }

        val systemPkgs = try {
            pkgManager.getInstalledPackages(PACKAGE_FLAGS).filter(::isPackageAnExtension)
        } catch (e: Exception) {
            Log.error("AndroidCatalogLoader: Failed to query installed packages", e)
            emptyList()
        }

        val localPkgs = try {
            simpleStorage.extensionDirectory().toFile().listFiles()
                .orEmpty()
                .filter { it.isDirectory }
                .map { File(it, it.name + ".apk") }
                .filter { it.exists() && it.canRead() }
        } catch (e: Exception) {
            Log.error("AndroidCatalogLoader: Failed to list local extensions", e)
            emptyList()
        }

        val cachePkgs = try {
            simpleStorage.cacheExtensionDir().toFile().listFiles()
                .orEmpty()
                .filter { it.isDirectory }
                .map { File(it, it.name + ".apk") }
                .filter { it.exists() && it.canRead() }
        } catch (e: Exception) {
            Log.error("AndroidCatalogLoader: Failed to list cached extensions", e)
            emptyList()
        }

        // Load each catalog concurrently with individual error isolation
        val installedCatalogs = withIOContext {
            val local = if (uiPreferences.showLocalCatalogs().get()) {
                localPkgs.map { file ->
                    async(Dispatchers.Default) {
                        loadCatalogWithRetry(file.nameWithoutExtension) {
                            loadLocalCatalog(file.nameWithoutExtension)
                        }
                    }
                }
            } else emptyList()
            val localCache = if (uiPreferences.showLocalCatalogs().get()) {
                cachePkgs.map { file ->
                    async(Dispatchers.Default) {
                        loadCatalogWithRetry(file.nameWithoutExtension) {
                            loadLocalCatalog(file.nameWithoutExtension)
                        }
                    }
                }
            } else emptyList()
            val system = if (uiPreferences.showSystemWideCatalogs().get()) {
                systemPkgs.map { pkgInfo ->
                    async(Dispatchers.Default) {
                        loadCatalogWithRetry(pkgInfo.packageName) {
                            loadSystemCatalog(pkgInfo.packageName, pkgInfo)
                        }
                    }
                }
            } else emptyList()
            val deferred = (local + localCache + system)
            deferred.awaitAll()
        }.filterNotNull()

        // Deduplicate: prefer system-installed packages over local files
        // (adb sideload updates system package but not local file)
        val systemPkgNames = systemPkgs.map { it.packageName }.toSet()
        val deduplicated = installedCatalogs.filter { catalog ->
            val isLocal = catalog is CatalogInstalled.Locally
            val isDuplicate = isLocal && catalog.pkgName in systemPkgNames
            !isDuplicate
        }

        // Load Tsundoku (Tachiyomi/Mihon) extensions natively
        val tsundokuCatalogs = loadTsundokuExtensions()

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
                Log.error("AndroidCatalogLoader: Failed to load JS plugin stubs", e)
                emptyList()
            }
        } else {
            emptyList()
        }

        val result = (bundled + deduplicated + tsundokuCatalogs + jsPlugins).distinctBy { it.sourceId }.toSet().toList()
        
        // Log diagnostics
        if (failedCatalogs.isNotEmpty()) {
            Log.warn { "AndroidCatalogLoader: ${failedCatalogs.size} catalogs failed to load: ${failedCatalogs.keys}" }
            failedCatalogs.forEach { (pkg, error) ->
                Log.warn { "  - $pkg: $error" }
            }
        }
        
        return result
    }
    
    /**
     * Load a catalog with retry logic for recoverable failures.
     * Returns null if all attempts fail.
     */
    private suspend fun <T : CatalogLocal?> loadCatalogWithRetry(
        pkgName: String,
        loader: suspend () -> T
    ): T? {
        var lastException: Throwable? = null
        
        repeat(MAX_RETRY_ATTEMPTS + 1) { attempt ->
            try {
                val result = loader()
                if (result != null) {
                    // Success - remove from failed list if it was there
                    failedCatalogs.remove(pkgName)
                    return result
                }
                // Null result means non-recoverable (invalid metadata, not an extension, etc.)
                return null
            } catch (e: OutOfMemoryError) {
                // OOM is always fatal - don't retry
                Log.error("AndroidCatalogLoader: OOM loading $pkgName - skipping", e)
                failedCatalogs[pkgName] = CatalogLoadError.IO_ERROR
                return null
            } catch (e: SecurityException) {
                // Security errors are permanent - don't retry
                Log.error("AndroidCatalogLoader: Security error loading $pkgName", e)
                failedCatalogs[pkgName] = CatalogLoadError.SECURITY_ERROR
                return null
            } catch (e: Exception) {
                lastException = e
                val errorType = classifyError(e)
                
                if (attempt < MAX_RETRY_ATTEMPTS && isRecoverableError(errorType)) {
                    Log.warn { "AndroidCatalogLoader: Retry ${attempt + 1}/$MAX_RETRY_ATTEMPTS for $pkgName (${errorType.name})" }
                    kotlinx.coroutines.delay(RETRY_DELAY_MS * (attempt + 1))
                } else {
                    Log.error("AndroidCatalogLoader: Failed to load $pkgName after ${attempt + 1} attempts", e)
                    failedCatalogs[pkgName] = errorType
                    return null
                }
            }
        }
        
        failedCatalogs[pkgName] = classifyError(lastException ?: Exception("Unknown"))
        return null
    }
    
    /**
     * Classify an exception into a typed error for retry logic.
     * Note: OOM and SecurityException are caught before this is called.
     */
    private fun classifyError(e: Throwable): CatalogLoadError {
        return when {
            e is java.io.IOException -> CatalogLoadError.IO_ERROR
            e is ClassNotFoundException || e is NoClassDefFoundError -> CatalogLoadError.CLASS_NOT_FOUND
            e is InstantiationException || e is IllegalAccessException -> CatalogLoadError.INSTANTIATION_FAILED
            e.message?.contains("dex", ignoreCase = true) == true -> CatalogLoadError.DEX_COMPILATION_FAILED
            e.message?.contains("class not found", ignoreCase = true) == true -> CatalogLoadError.CLASS_NOT_FOUND
            else -> CatalogLoadError.UNKNOWN
        }
    }
    
    /**
     * Determines if an error is worth retrying.
     */
    private fun isRecoverableError(error: CatalogLoadError): Boolean {
        return error in listOf(
            CatalogLoadError.DEX_COMPILATION_FAILED,
            CatalogLoadError.IO_ERROR,
            CatalogLoadError.CLASS_NOT_FOUND
        )
    }

    /**
     * Attempts to load a catalog from the given package name.
     * Returns null if the file is not a valid extension (JS plugin, missing, etc.)
     */
    override fun loadLocalCatalog(pkgName: String): CatalogInstalled.Locally? {
        // Check if this is a JS plugin - check both SAF and fallback locations
        val jsFileName = "$pkgName.js"
        if (ireader.domain.storage.SecureStorageHelper.jsPluginExists(context, jsFileName)) {
            // JS plugins are handled by loadAll(), not loadLocalCatalog()
            return null
        }
        
        // Try to load as traditional APK extension
        val file = File(simpleStorage.extensionDirectory().toFile(), "${pkgName}/${pkgName}.apk")
        val cacheFile = File(simpleStorage.cacheExtensionDir().toFile(), "${pkgName}/${pkgName}.apk")
        val finalFile = when {
            file.exists() && file.canRead() && file.length() > 0 -> file
            cacheFile.exists() && cacheFile.canRead() && cacheFile.length() > 0 -> cacheFile
            else -> return null
        }
        
        val pkgInfo = try {
            pkgManager.getPackageArchiveInfo(finalFile.absolutePath, PACKAGE_FLAGS)
        } catch (e: Exception) {
            // APK is corrupted or invalid - clean up if possible
            Log.warn { "AndroidCatalogLoader: Corrupt APK for $pkgName: ${e.message}" }
            null
        } ?: return null

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
     * Loads a catalog given its package name, metadata, and APK file.
     * 
     * Android 15 (API 35) compatibility:
     * - Validates APK readability before attempting class loading
     * - Uses InMemoryDexClassLoader on API 28+ to avoid DEX-on-disk issues
     * - Falls back to DexClassLoader if in-memory loading fails
     * - Cleans up stale DEX output directories
     */
    private fun loadLocalCatalog(
        pkgName: String,
        pkgInfo: PackageInfo,
        file: File,
    ): CatalogInstalled.Locally? {
        // Try IReader extension first
        val data = validateMetadata(pkgName, pkgInfo)

        if (data != null) {
            // Standard IReader extension loading path
            try {
                if (!file.exists() || !file.canRead() || file.length() == 0L) {
                    Log.warn { "AndroidCatalogLoader: APK file not accessible for $pkgName" }
                    return null
                }

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
                    installDir = (file.parentFile ?: file).absolutePath.toOkioPath(),
                    iconUrl = data.icon
                )
            } catch (e: OutOfMemoryError) {
                Log.error("AndroidCatalogLoader: OOM loading catalog $pkgName", e)
                return null
            } catch (e: Exception) {
                Log.error("AndroidCatalogLoader: Failed to load local catalog $pkgName", e)
                return null
            }
        }

        // Fallback: try loading as Tsundoku extension
        val tsundokuData = TsundokuExtensionLoader.validateMetadata(pkgName, pkgInfo)
        if (tsundokuData != null) {
            return loadLocalTsundokuCatalog(pkgName, pkgInfo, file, tsundokuData)
        }

        return null
    }

    /**
     * Load a locally stored Tsundoku extension APK.
     */
    private fun loadLocalTsundokuCatalog(
        pkgName: String,
        pkgInfo: PackageInfo,
        file: File,
        data: TsundokuValidatedData
    ): CatalogInstalled.Locally? {
        try {
            if (!file.exists() || !file.canRead() || file.length() == 0L) {
                Log.warn { "AndroidCatalogLoader: Tsundoku APK file not accessible for $pkgName" }
                return null
            }

            // Use DexClassLoader for local tsundoku APKs
            val readOnlyCopy = copyToReadOnlyCache(file, pkgName)
            val dexOutputDir = File(context.codeCacheDir, "dex_out/${pkgName}_${System.currentTimeMillis()}").apply { mkdirs() }
            val classLoader = DexClassLoader(readOnlyCopy.absolutePath, dexOutputDir.absolutePath, null, context.classLoader)
            val sources = TsundokuExtensionLoader.loadSources(pkgName, classLoader, data)

            if (sources.isEmpty()) {
                Log.warn { "AndroidCatalogLoader: No sources from local tsundoku APK $pkgName" }
                return null
            }

            val source = sources.first()
            return CatalogInstalled.Locally(
                name = source.name,
                description = if (data.isNovel) "Tsundoku novel extension" else "Tsundoku manga extension",
                source = source,
                pkgName = pkgName,
                versionName = data.versionName,
                versionCode = data.versionCode,
                nsfw = data.nsfw,
                installDir = (file.parentFile ?: file).absolutePath.toOkioPath(),
                iconUrl = ""
            )
        } catch (e: Exception) {
            Log.error("AndroidCatalogLoader: Failed to load local tsundoku catalog $pkgName", e)
            return null
        }
    }
    
    /**
     * Creates a ClassLoader for loading an extension from an APK file.
     *
     * ponytail: Android 15 rejects writable DEX files. APKs on external storage
     * (/storage/emulated/0/...) are writable → SecurityException. Fix: copy APK
     * to codeCacheDir and make it read-only before loading. DexClassLoader is the
     * correct API for APK files (not InMemoryDexClassLoader which needs raw DEX).
     */
    private fun createClassLoader(file: File, pkgName: String): ClassLoader {
        val readOnlyCopy = copyToReadOnlyCache(file, pkgName)
        val dexOutputDir = File(context.codeCacheDir, "dex_out/${pkgName}_${System.currentTimeMillis()}").apply { mkdirs() }
        return DexClassLoader(readOnlyCopy.absolutePath, dexOutputDir.absolutePath, null, context.classLoader)
    }

    /**
     * Copy APK to codeCacheDir and set read-only. Android 15 requires DEX files
     * to be non-writable. Reuses existing copy if size matches.
     */
    private fun copyToReadOnlyCache(file: File, pkgName: String): File {
        val cacheDir = File(context.codeCacheDir, "apk_cache").apply { mkdirs() }
        val cached = File(cacheDir, "${pkgName}.apk")

        // Reuse if same size (fast path, avoids re-copy on every load)
        if (cached.exists() && cached.length() == file.length()) {
            return cached
        }

        file.copyTo(cached, overwrite = true)
        cached.setReadOnly()
        return cached
    }

    /**
     * Loads a system-wide catalog given its package name.
     * 
     * For system-installed extensions, we use PathClassLoader which is the
     * standard way to load classes from installed APKs. Android handles
     * DEX optimization automatically for system packages.
     */
    private fun loadSystemCatalog(
        pkgName: String,
        pkgInfo: PackageInfo,
        iconFile: File? = null
    ): CatalogInstalled.SystemWide? {
        val sourceDir = pkgInfo.applicationInfo?.sourceDir ?: run {
            Log.warn { "AndroidCatalogLoader: No sourceDir for system package $pkgName" }
            return null
        }
        val data = validateMetadata(pkgName, pkgInfo) ?: return null

        // For system-installed packages, use PathClassLoader (standard Android approach)
        // This is safe on all Android versions including 15
        val loader = try {
            PathClassLoader(sourceDir, context.classLoader)
        } catch (e: Exception) {
            Log.error("AndroidCatalogLoader: Failed to create PathClassLoader for $pkgName", e)
            return null
        }
        
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

    /**
     * Validates extension metadata and extracts configuration.
     * Returns null if the package is not a valid extension (silently skip).
     */
    private fun validateMetadata(pkgName: String, pkgInfo: PackageInfo): ValidatedData? {
        if (!isPackageAnExtension(pkgInfo)) {
            return null
        }

        if (pkgName != pkgInfo.packageName) {
            Log.warn { "AndroidCatalogLoader: Package name mismatch: requested=$pkgName, actual=${pkgInfo.packageName}" }
            return null
        }

        @Suppress("DEPRECATION")
        val versionCode = pkgInfo.versionCode
        val versionName = pkgInfo.versionName ?: run {
            Log.warn { "AndroidCatalogLoader: Missing versionName for $pkgName" }
            return null
        }

        // Validate lib version
        val majorLibVersion = try {
            versionName.substringBefore('.').toInt()
        } catch (e: NumberFormatException) {
            Log.warn { "AndroidCatalogLoader: Invalid version format '$versionName' for $pkgName" }
            return null
        }
        
        if (majorLibVersion < LIB_VERSION_MIN || majorLibVersion > LIB_VERSION_MAX) {
            Log.warn { "AndroidCatalogLoader: Unsupported lib version $majorLibVersion for $pkgName (need $LIB_VERSION_MIN-$LIB_VERSION_MAX)" }
            return null
        }

        val appInfo = pkgInfo.applicationInfo ?: run {
            Log.warn { "AndroidCatalogLoader: No applicationInfo for $pkgName" }
            return null
        }

        val metadata = appInfo.metaData ?: run {
            Log.warn { "AndroidCatalogLoader: No metadata for $pkgName" }
            return null
        }
        val sourceClassName = metadata.getString(METADATA_SOURCE_CLASS)?.trim()
        if (sourceClassName == null) {
            Log.warn { "AndroidCatalogLoader: Missing source.class metadata for $pkgName" }
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

    /**
     * Load a Source instance from the given ClassLoader.
     * Returns null instead of throwing, so callers can skip broken extensions gracefully.
     */
    private fun loadSource(pkgName: String, loader: ClassLoader, data: ValidatedData): Source? {
        return try {
            val clazz = Class.forName(data.classToLoad, false, loader)
            val constructor = clazz.getConstructor(ireader.core.source.Dependencies::class.java)
            val instance = constructor.newInstance(data.dependencies)
            
            if (instance !is Source) {
                Log.error("AndroidCatalogLoader: Source class ${data.classToLoad} does not implement Source interface")
                return null
            }
            
            instance
        } catch (e: ClassNotFoundException) {
            Log.error("AndroidCatalogLoader: Source class not found: ${data.classToLoad} in $pkgName")
            null
        } catch (e: NoSuchMethodException) {
            Log.error("AndroidCatalogLoader: Source class ${data.classToLoad} missing Dependencies constructor")
            null
        } catch (e: Exception) {
            Log.error("AndroidCatalogLoader: Failed to instantiate source ${data.classToLoad} from $pkgName", e)
            null
        }
    }

    /**
     * Load all installed Tsundoku (Tachiyomi/Mihon) extensions as IReader catalogs.
     *
     * Tsundoku extensions use different feature flags (`tachiyomi.extension` / `tachiyomi.novelextension`)
     * and metadata keys. They are loaded via reflection and wrapped in [TsundokuCatalogSource].
     */
    private fun loadTsundokuExtensions(): List<CatalogLocal> {
        val tsundokuPkgs = try {
            TsundokuExtensionLoader.getInstalledTsundokuExtensions(pkgManager)
        } catch (e: Exception) {
            Log.error("AndroidCatalogLoader: Failed to query tsundoku extensions", e)
            emptyList()
        }

        if (tsundokuPkgs.isEmpty()) return emptyList()

        Log.info("AndroidCatalogLoader: Found ${tsundokuPkgs.size} tsundoku extensions")

        // Initialize tsundoku DI dependencies (Injekt, NetworkHelper, etc.)
        TsundokuExtensionLoader.initializeDependencies(context)

        return tsundokuPkgs.flatMap { pkgInfo ->
            loadTsundokuCatalog(pkgInfo)
        }
    }

    /**
     * Load a single Tsundoku extension as IReader catalogs.
     * Returns a list because one extension APK can contain multiple sources.
     */
    private fun loadTsundokuCatalog(pkgInfo: PackageInfo): List<CatalogInstalled.SystemWide> {
        val pkgName = pkgInfo.packageName
        val data = TsundokuExtensionLoader.validateMetadata(pkgName, pkgInfo) ?: return emptyList()

        val sourceDir = pkgInfo.applicationInfo?.sourceDir ?: run {
            Log.warn { "AndroidCatalogLoader: No sourceDir for tsundoku package $pkgName" }
            return emptyList()
        }

        // Load icon from APK package info
        val iconUrl = pkgInfo.applicationInfo?.icon?.toString() ?: ""

        return try {
            val classLoader = ChildFirstPathClassLoader(sourceDir, null, context.classLoader)
            val sources = TsundokuExtensionLoader.loadSources(pkgName, classLoader, data)

            if (sources.isEmpty()) {
                Log.warn { "AndroidCatalogLoader: No sources loaded from tsundoku extension $pkgName" }
                return emptyList()
            }

            Log.info { "AndroidCatalogLoader: Loaded ${sources.size} source(s) from tsundoku extension $pkgName" }

            sources.map { source ->
                CatalogInstalled.SystemWide(
                    name = source.name,
                    description = if (data.isNovel) "Tsundoku novel extension" else "Tsundoku manga extension",
                    source = source,
                    pkgName = pkgName,
                    versionName = data.versionName,
                    versionCode = data.versionCode,
                    nsfw = data.nsfw,
                    iconUrl = iconUrl,
                    installDir = sourceDir.toOkioPath()
                )
            }
        } catch (e: Exception) {
            Log.error("AndroidCatalogLoader: Failed to load tsundoku catalog $pkgName", e)
            emptyList()
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
            Log.error("AndroidCatalogLoader: Failed to load JS plugins async", e)
        }
    }
    
    /**
     * Get the JS plugin loader for advanced operations.
     */
    fun getJSPluginLoader(): JSPluginLoader = jsPluginLoader
    
    /**
     * Load a single JS plugin by package name.
     * This is much more efficient than loading ALL plugins for a single installation event.
     */
    override suspend fun loadSingleJSPlugin(pkgName: String): ireader.domain.models.entities.JSPluginCatalog? {
        return try {
            val pluginFile = jsPluginLoader.findPluginFile(pkgName) ?: return null
            jsPluginLoader.loadPlugin(pluginFile)
        } catch (e: Exception) {
            Log.error("AndroidCatalogLoader: Failed to load single JS plugin $pkgName", e)
            null
        }
    }
    
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

    /**
     * Clears the cached data for a specific catalog.
     * With PathClassLoader, no manual DEX cache cleanup is needed.
     * Android handles DEX optimization automatically.
     */
    override fun clearCatalogCache(pkgName: String) {
        // With DexClassLoader using fresh output dirs, no manual cache cleanup needed.
    }

    /**
     * Gets the list of failed catalogs for diagnostics and retry.
     */
    fun getFailedCatalogs(): Map<String, CatalogLoadError> = failedCatalogs.toMap()
    
    /**
     * Clears the failed catalogs list, allowing retry on next load.
     */
    fun clearFailedCatalogs() {
        failedCatalogs.clear()
    }

    private companion object {
        const val MAX_RETRY_ATTEMPTS = 2
        const val RETRY_DELAY_MS = 500L
        const val EXTENSION_FEATURE = "ireader"
        const val METADATA_SOURCE_CLASS = "source.class"
        const val METADATA_DESCRIPTION = "source.description"
        const val METADATA_NSFW = "source.nsfw"
        const val METADATA_ICON = "source.icon"
        const val LIB_VERSION_MIN = 2
        const val LIB_VERSION_MAX = 2

        /**
         * Package query flags.
         * Note: GET_CONFIGURATIONS is deprecated on API 33+ but still functional.
         * We keep it for backward compatibility with older extensions.
         */
        const val PACKAGE_FLAGS = PackageManager.GET_CONFIGURATIONS or PackageManager.GET_META_DATA
    }
}
