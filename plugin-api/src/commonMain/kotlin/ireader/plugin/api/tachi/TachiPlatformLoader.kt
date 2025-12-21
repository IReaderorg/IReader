package ireader.plugin.api.tachi

import ireader.plugin.api.PluginContext
import ireader.plugin.api.TachiValidationResult

/**
 * Type alias for Tachi source wrapper - can be any TachiSource implementation.
 */
typealias TachiSourceWrapper = TachiSource

/**
 * Platform-specific loader for Tachiyomi extensions.
 * 
 * Android uses DexClassLoader to load APKs directly.
 * Desktop uses dex2jar conversion to load as JAR.
 * 
 * Implementations should be provided by the plugin itself.
 */
interface TachiPlatformLoader {
    /**
     * Load an extension from APK path.
     * @return Loaded extension result with sources
     */
    suspend fun loadExtension(apkPath: String): TachiExtensionLoadResult
    
    /**
     * Unload an extension and release resources.
     */
    fun unloadExtension(pkgName: String)
    
    /**
     * Validate an APK before loading.
     */
    suspend fun validateApk(apkPath: String): TachiValidationResult
}

/**
 * Result of loading a Tachi extension.
 */
data class TachiExtensionLoadResult(
    val pkgName: String,
    val name: String,
    val versionName: String,
    val versionCode: Int,
    val lang: String,
    val isNsfw: Boolean,
    val sources: List<TachiSource>,
    val iconUrl: String? = null
)

/**
 * Extension metadata parsed from APK.
 */
data class ExtensionMetadata(
    val pkgName: String,
    val name: String,
    val versionName: String,
    val versionCode: Int,
    val lang: String,
    val isNsfw: Boolean,
    val libVersion: Double,
    val mainClass: String
)
