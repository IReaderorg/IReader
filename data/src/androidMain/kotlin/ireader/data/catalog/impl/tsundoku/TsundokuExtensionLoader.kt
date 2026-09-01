package ireader.data.catalog.impl.tsundoku

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import ireader.core.log.Log
import ireader.core.source.Source
import ireader.domain.utils.AndroidCookieJar

/**
 * Handles loading and validation of Tsundoku (Tachiyomi/Mihon) extension APKs.
 *
 * Tsundoku extensions use different Android feature flags and metadata keys
 * compared to IReader extensions:
 *
 * | Aspect          | IReader                    | Tsundoku                                    |
 * |-----------------|----------------------------|---------------------------------------------|
 * | Feature flag    | `ireader`                  | `tachiyomi.extension` / `tachiyomi.novelextension` |
 * | Metadata class  | `source.class`             | `tachiyomi.extension.class`                 |
 * | Lib version     | exactly 2                  | 1.4–1.6                                     |
 * | Source constructor | `Constructor(Dependencies)` | No-arg constructor                       |
 */
object TsundokuExtensionLoader {

    // Tsundoku extension feature flags
    private const val EXTENSION_FEATURE = "tachiyomi.extension"
    private const val EXTENSION_FEATURE_NOVEL = "tachiyomi.novelextension"
    private val EXTENSION_FEATURES = setOf(EXTENSION_FEATURE, EXTENSION_FEATURE_NOVEL)

    // Tsundoku metadata keys
    private const val METADATA_SOURCE_CLASS = "tachiyomi.extension.class"
    private const val METADATA_SOURCE_CLASS_NOVEL = "tachiyomi.novelextension.class"
    private const val METADATA_NSFW = "tachiyomi.extension.nsfw"
    private const val METADATA_NOVEL = "tachiyomi.extension.novel"
    private const val METADATA_FACTORY = "tachiyomi.extension.factory"

    // Tsundoku supported lib versions
    private const val LIB_VERSION_MIN = 1.3
    private const val LIB_VERSION_MAX = 2.0

    /**
     * Check if a package is a Tsundoku extension (has tachiyomi.extension or tachiyomi.novelextension feature or metadata).
     */
    fun isTsundokuExtension(pkgInfo: PackageInfo): Boolean {
        val hasFeature = pkgInfo.reqFeatures.orEmpty().any { it.name in EXTENSION_FEATURES }
        val metadata = pkgInfo.applicationInfo?.metaData
        val hasMetadata = metadata?.containsKey("tachiyomi.extension.class") == true ||
            metadata?.containsKey("tachiyomi.novelextension.class") == true ||
            metadata?.containsKey("tachiyomi.extension") == true ||
            metadata?.containsKey("tachiyomi.novelextension") == true ||
            metadata?.containsKey("tachiyomix.name") == true
        val hasPkgPrefix = pkgInfo.packageName.startsWith("eu.kanade.tachiyomi.extension.") ||
            pkgInfo.packageName.startsWith("eu.kanade.tachiyomi.novelextension.") ||
            pkgInfo.packageName.startsWith("app.tsundoku.extension.") ||
            pkgInfo.packageName.startsWith("app.tsundoku.novelextension.")
        val result = hasFeature || hasMetadata || hasPkgPrefix
        if (result) {
            Log.info { "TsundokuLoader: Package ${pkgInfo.packageName} recognized as Tsundoku (hasFeature=$hasFeature, hasMetadata=$hasMetadata, hasPkgPrefix=$hasPkgPrefix)" }
        }
        return result
    }

    /**
     * Check if a package is a Tsundoku novel extension specifically.
     */
    fun isNovelExtension(pkgInfo: PackageInfo): Boolean {
        val hasFeature = pkgInfo.reqFeatures.orEmpty().any { it.name == EXTENSION_FEATURE_NOVEL }
        val metadata = pkgInfo.applicationInfo?.metaData
        val hasNovelMeta = metadata?.getInt("tachiyomi.novelextension.novel") == 1 ||
            metadata?.getInt("tachiyomi.extension.novel") == 1 ||
            metadata?.containsKey("tachiyomi.novelextension.class") == true
        val hasPkg = pkgInfo.packageName.contains(".novelextension.")
        return hasFeature || hasNovelMeta || hasPkg
    }

    /**
     * Validate Tsundoku extension metadata.
     * Returns null if the package is not a valid Tsundoku extension.
     */
    fun validateMetadata(pkgName: String, pkgInfo: PackageInfo): TsundokuValidatedData? {
        if (!isTsundokuExtension(pkgInfo)) {
            Log.warn { "TsundokuLoader: $pkgName is not a Tsundoku extension (missing features/metadata/prefix)" }
            return null
        }

        val versionName = pkgInfo.versionName ?: run {
            Log.warn { "TsundokuLoader: Missing versionName for $pkgName" }
            return null
        }

        @Suppress("DEPRECATION")
        val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            pkgInfo.longVersionCode.toInt()
        } else {
            pkgInfo.versionCode
        }

        // Validate lib version (format: "1.4.xxx" or "1.6.xxx")
        val libVersion = try {
            versionName.substringBeforeLast('.').toDouble()
        } catch (e: NumberFormatException) {
            Log.warn { "TsundokuLoader: Invalid version format '$versionName' for $pkgName, trying anyway" }
            0.0 // Default to 0 so loading still proceeds
        }

        if (libVersion < LIB_VERSION_MIN || libVersion > LIB_VERSION_MAX) {
            Log.warn { "TsundokuLoader: lib version $libVersion for $pkgName outside range $LIB_VERSION_MIN-$LIB_VERSION_MAX, trying anyway" }
        }

        val appInfo = pkgInfo.applicationInfo ?: run {
            Log.warn { "TsundokuLoader: No applicationInfo for $pkgName" }
            return null
        }

        val metadata = appInfo.metaData ?: run {
            Log.warn { "TsundokuLoader: No metadata for $pkgName" }
            return null
        }

        // Get source class name from the appropriate metadata key
        val isNovel = isNovelExtension(pkgInfo) ||
            metadata.getInt("tachiyomi.novelextension.novel") == 1 ||
            metadata.getInt("tachiyomi.extension.novel") == 1 ||
            metadata.getInt(METADATA_NOVEL) == 1 ||
            metadata.containsKey("tachiyomi.novelextension.class") ||
            pkgName.contains(".novelextension.")

        val metaNs = if (isNovel) "tachiyomi.novelextension" else "tachiyomi.extension"
        val sourceClassName = metadata.getString("tachiyomi.novelextension.class")
            ?: metadata.getString("tachiyomi.extension.class")
            ?: metadata.getString("$metaNs.class")
            ?: metadata.getString(METADATA_SOURCE_CLASS)
            ?: metadata.getString(METADATA_SOURCE_CLASS_NOVEL)
            ?: run {
                Log.warn { "TsundokuLoader: Missing source class metadata for $pkgName. Metadata keys: ${metadata.keySet()}" }
                return null
            }

        val nsfw = metadata.getInt(METADATA_NSFW) == 1 || metadata.getInt("$metaNs.nsfw") == 1
        val factoryClassName = metadata.getString(METADATA_FACTORY) ?: metadata.getString("$metaNs.factory")

        val classToLoad = if (sourceClassName.startsWith(".")) {
            pkgInfo.packageName + sourceClassName
        } else {
            sourceClassName
        }

        Log.info { "TsundokuLoader: Validated $pkgName: isNovel=$isNovel, classToLoad=$classToLoad, libVersion=$libVersion, versionCode=$versionCode" }

        return TsundokuValidatedData(
            versionCode = versionCode,
            versionName = versionName,
            libVersion = libVersion,
            isNovel = isNovel,
            nsfw = nsfw,
            classToLoad = classToLoad,
            factoryClassName = factoryClassName
        )
    }

    /**
     * Load Tsundoku sources from the given ClassLoader.
     * Matches tsundoku's ExtensionLoader.loadExtension() logic exactly:
     * 1. Split source classes by ";"
     * 2. Prepend package name for relative class names (starting with ".")
     * 3. Try namespace fallbacks (app.tsundoku ↔ eu.kanade.tachiyomi)
     * 4. Handle SourceFactory and Source
     */
    fun loadSources(pkgName: String, classLoader: ClassLoader, data: TsundokuValidatedData): List<Source> {
        // Split source classes by ";" (one APK can have multiple sources)
        val classNames = data.classToLoad.split(";").map { it.trim() }

        val allSources = mutableListOf<Any>()

        for (rawClassName in classNames) {
            // Prepend package name for relative class names
            val className = if (rawClassName.startsWith(".")) {
                pkgName + rawClassName
            } else {
                rawClassName
            }

            // Build list of class names to try (original + namespace fallbacks)
            val classesToTry = mutableListOf(className)

            // If uses app.tsundoku namespace, also try eu.kanade.tachiyomi
            if (className.startsWith("app.tsundoku.extension.")) {
                classesToTry.add(className.replace("app.tsundoku.extension.", "eu.kanade.tachiyomi.extension."))
            }
            if (className.startsWith("app.tsundoku.novelextension.")) {
                classesToTry.add(className.replace("app.tsundoku.novelextension.", "eu.kanade.tachiyomi.novelextension."))
            }
            // Reverse: if uses eu.kanade.tachiyomi, also try app.tsundoku
            if (className.startsWith("eu.kanade.tachiyomi.extension.")) {
                classesToTry.add(className.replace("eu.kanade.tachiyomi.extension.", "app.tsundoku.extension."))
            }
            if (className.startsWith("eu.kanade.tachiyomi.novelextension.")) {
                classesToTry.add(className.replace("eu.kanade.tachiyomi.novelextension.", "app.tsundoku.novelextension."))
            }

            var lastError: Throwable? = null
            var found = false

            for (classToTry in classesToTry) {
                try {
                    Log.info { "TsundokuLoader: Attempting to load class: $classToTry in $pkgName" }
                    val clazz = Class.forName(classToTry, false, classLoader)
                    val obj = clazz.getDeclaredConstructor().newInstance()
                    Log.info { "TsundokuLoader: Instantiated $classToTry (${obj.javaClass.name})" }

                    // Check what type of source this is
                    val sources = when {
                        isTsundokuSource(obj) -> {
                            Log.info { "TsundokuLoader: Matched Tsundoku Source: $classToTry" }
                            listOf(obj)
                        }
                        isSourceFactory(obj) -> {
                            Log.info { "TsundokuLoader: Matched Tsundoku SourceFactory: $classToTry" }
                            invokeCreateSources(obj)
                        }
                        else -> {
                            val interfaces = obj.javaClass.interfaces.map { it.name }
                            val superclass = obj.javaClass.superclass?.name
                            Log.error { "TsundokuLoader: Unknown source class type: ${obj.javaClass.name}, superclass=$superclass, interfaces=$interfaces" }
                            emptyList<Any>()
                        }
                    }

                    allSources.addAll(sources)
                    found = true
                    break
                } catch (e: ClassNotFoundException) {
                    lastError = e
                    // Try next class name
                } catch (e: Throwable) {
                    // Unwrap InvocationTargetException to get the real cause
                    val realCause = if (e is java.lang.reflect.InvocationTargetException) e.cause ?: e else e
                    val causeChain = buildString {
                        append("${realCause::class.simpleName}: ${realCause.message}")
                        var cause = realCause.cause
                        var depth = 0
                        while (cause != null && depth < 5) {
                            append(" → ${cause::class.simpleName}: ${cause.message}")
                            cause = cause.cause
                            depth++
                        }
                    }
                    Log.error { "TsundokuLoader: Extension load error: $pkgName ($classToTry): $causeChain" }
                    lastError = e
                }
            }

            if (!found) {
                Log.warn { "TsundokuLoader: Class not found in any namespace: $classesToTry. Last error: ${lastError?.message}" }
            }
        }

        Log.info { "TsundokuLoader: $pkgName finished loading. Total sources found: ${allSources.size}" }
        return allSources.mapNotNull { wrapSourceSafe(it) }
    }

    // ==================== Source Wrapping ====================

    /**
     * Wrap a tsundoku source instance in an IReader CatalogSource.
     */
    private fun wrapSourceSafe(tsundokuSource: Any): Source? {
        return try {
            if (tsundokuSource is eu.kanade.tachiyomi.source.CatalogueSource) {
                TsundokuCatalogSource(tsundokuSource)
            } else {
                Log.warn { "TsundokuLoader: Source ${tsundokuSource.javaClass.name} is not direct CatalogueSource instance, attempting cast" }
                TsundokuCatalogSource(tsundokuSource as eu.kanade.tachiyomi.source.CatalogueSource)
            }
        } catch (e: Throwable) {
            Log.error { "TsundokuLoader: Failed to wrap source ${tsundokuSource.javaClass.name}: ${e.message}" }
            null
        }
    }

    /**
     * Check if an object implements the Tsundoku Source interface.
     */
    private fun isTsundokuSource(obj: Any): Boolean {
        if (obj is eu.kanade.tachiyomi.source.Source) return true
        return try {
            val sourceClass = Class.forName("eu.kanade.tachiyomi.source.Source", false, obj.javaClass.classLoader)
            sourceClass.isInstance(obj)
        } catch (e: Exception) {
            false
        } || hasInterfaceOrSuperclass(obj.javaClass, "eu.kanade.tachiyomi.source.Source")
    }

    /**
     * Check if an object implements the Tsundoku SourceFactory interface.
     */
    private fun isSourceFactory(obj: Any): Boolean {
        if (obj is eu.kanade.tachiyomi.source.SourceFactory) return true
        return try {
            val factoryClass = Class.forName("eu.kanade.tachiyomi.source.SourceFactory", false, obj.javaClass.classLoader)
            factoryClass.isInstance(obj)
        } catch (e: Exception) {
            false
        } || hasInterfaceOrSuperclass(obj.javaClass, "eu.kanade.tachiyomi.source.SourceFactory")
    }

    private fun hasInterfaceOrSuperclass(clazz: Class<*>?, targetName: String): Boolean {
        var current = clazz
        while (current != null && current != Any::class.java) {
            if (current.name == targetName) return true
            if (current.interfaces.any { it.name == targetName || hasInterfaceOrSuperclass(it, targetName) }) return true
            current = current.superclass
        }
        return false
    }

    /**
     * Invoke SourceFactory.createSources() via reflection.
     */
    @Suppress("UNCHECKED_CAST")
    private fun invokeCreateSources(factory: Any): List<Any> {
        val method = factory.javaClass.getMethod("createSources")
        return method.invoke(factory) as? List<Any> ?: emptyList()
    }

    // ==================== Package Query Helpers ====================

    /**
     * Query all installed Tsundoku extension packages.
     */
    fun getInstalledTsundokuExtensions(pkgManager: PackageManager): List<PackageInfo> {
        @Suppress("DEPRECATION")
        val packageFlags = PackageManager.GET_CONFIGURATIONS or PackageManager.GET_META_DATA
        return try {
            val allPackages = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                pkgManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(packageFlags.toLong()))
            } else {
                pkgManager.getInstalledPackages(packageFlags)
            }
            Log.info { "TsundokuLoader: Total installed packages seen by PackageManager: ${allPackages.size}" }
            val tsundokuPackages = allPackages.filter { isTsundokuExtension(it) }
            Log.info { "TsundokuLoader: Matched ${tsundokuPackages.size} Tsundoku package(s): ${tsundokuPackages.map { it.packageName }}" }
            tsundokuPackages
        } catch (e: Exception) {
            Log.error { "TsundokuLoader: Failed to query installed packages: ${e.message}" }
            emptyList()
        }
    }

    // ==================== DI Setup ====================

    private var dependenciesInitialized = false

    /**
     * Initialize dependencies required by Tsundoku extensions.
     * Registers NetworkHelper and Json directly in Injekt.
     */
    fun initializeDependencies(context: Context) {
        if (dependenciesInitialized) return

        try {
            val networkHelper = eu.kanade.tachiyomi.network.NetworkHelper(
                cacheDir = context.cacheDir,
                cookieJar = AndroidCookieJar()
            )
            val json = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            }

            // Register directly in Injekt using addSingletonFactory
            uy.kohesive.injekt.Injekt.addSingletonFactory(
                object : uy.kohesive.injekt.api.FullTypeReference<eu.kanade.tachiyomi.network.NetworkHelper>() {}
            ) { networkHelper }

            uy.kohesive.injekt.Injekt.addSingletonFactory(
                object : uy.kohesive.injekt.api.FullTypeReference<kotlinx.serialization.json.Json>() {}
            ) { json }

            // Register Application for ConfigurableSource
            val app = context.applicationContext as? android.app.Application
            if (app != null) {
                uy.kohesive.injekt.Injekt.addSingletonFactory(
                    object : uy.kohesive.injekt.api.FullTypeReference<android.app.Application>() {}
                ) { app }
            }

            dependenciesInitialized = true
            Log.info { "TsundokuLoader: Dependencies initialized" }
        } catch (e: Exception) {
            Log.warn { "TsundokuLoader: Failed to initialize dependencies: ${e.message}" }
        }
    }
}
