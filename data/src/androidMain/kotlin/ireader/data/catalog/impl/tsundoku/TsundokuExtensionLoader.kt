package ireader.data.catalog.impl.tsundoku

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import ireader.core.log.Log
import ireader.core.source.Source
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

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
     * Check if a package is a Tsundoku extension (has tachiyomi.extension or tachiyomi.novelextension feature).
     */
    fun isTsundokuExtension(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.reqFeatures.orEmpty().any { it.name in EXTENSION_FEATURES }
    }

    /**
     * Check if a package is a Tsundoku novel extension specifically.
     */
    fun isNovelExtension(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.reqFeatures.orEmpty().any { it.name == EXTENSION_FEATURE_NOVEL }
    }

    /**
     * Validate Tsundoku extension metadata.
     * Returns null if the package is not a valid Tsundoku extension.
     */
    fun validateMetadata(pkgName: String, pkgInfo: PackageInfo): TsundokuValidatedData? {
        if (!isTsundokuExtension(pkgInfo)) {
            return null
        }

        val versionName = pkgInfo.versionName ?: run {
            Log.warn { "TsundokuLoader: Missing versionName for $pkgName" }
            return null
        }

        @Suppress("DEPRECATION")
        val versionCode = pkgInfo.versionCode

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
        val isNovel = isNovelExtension(pkgInfo) || metadata.getInt(METADATA_NOVEL) == 1
        val metaNs = if (isNovel) "tachiyomi.novelextension" else "tachiyomi.extension"
        val sourceClassName = metadata.getString("$metaNs.class")
            ?: metadata.getString(METADATA_SOURCE_CLASS)
            ?: metadata.getString(METADATA_SOURCE_CLASS_NOVEL)
            ?: run {
                Log.warn { "TsundokuLoader: Missing source class metadata for $pkgName" }
                return null
            }

        val nsfw = metadata.getInt(METADATA_NSFW) == 1 || metadata.getInt("$metaNs.nsfw") == 1
        val factoryClassName = metadata.getString(METADATA_FACTORY) ?: metadata.getString("$metaNs.factory")

        val classToLoad = if (sourceClassName.startsWith(".")) {
            pkgInfo.packageName + sourceClassName
        } else {
            sourceClassName
        }

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
                    val clazz = Class.forName(classToTry, false, classLoader)
                    val obj = clazz.getDeclaredConstructor().newInstance()

                    // Check what type of source this is
                    val sources = when {
                        isTsundokuSource(obj) -> listOf(obj)
                        isSourceFactory(obj) -> invokeCreateSources(obj)
                        else -> {
                            Log.error { "TsundokuLoader: Unknown source class type: ${obj.javaClass.name}" }
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

        return allSources.map { wrapSource(it) }
    }

    // ==================== Source Wrapping ====================

    /**
     * Wrap a tsundoku source instance in an IReader CatalogSource.
     */
    private fun wrapSource(tsundokuSource: Any): Source {
        return TsundokuCatalogSource(tsundokuSource as eu.kanade.tachiyomi.source.CatalogueSource)
    }

    /**
     * Check if an object implements the Tsundoku Source interface.
     */
    private fun isTsundokuSource(obj: Any): Boolean {
        return try {
            Class.forName("eu.kanade.tachiyomi.source.Source").isInstance(obj)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if an object implements the Tsundoku SourceFactory interface.
     */
    private fun isSourceFactory(obj: Any): Boolean {
        return try {
            Class.forName("eu.kanade.tachiyomi.source.SourceFactory").isInstance(obj)
        } catch (e: Exception) {
            false
        }
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
            pkgManager.getInstalledPackages(packageFlags).filter { isTsundokuExtension(it) }
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
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .callTimeout(2, TimeUnit.MINUTES)
                .build()

            val networkHelper = eu.kanade.tachiyomi.network.NetworkHelper(
                cacheDir = context.cacheDir
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
