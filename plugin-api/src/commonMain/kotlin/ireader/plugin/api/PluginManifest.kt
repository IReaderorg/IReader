package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin manifest containing metadata and configuration.
 * Every plugin must provide a manifest describing its capabilities.
 */
@Serializable
data class PluginManifest(
    /** Unique identifier for the plugin (e.g., "com.example.mytheme") */
    val id: String,
    /** Display name of the plugin */
    val name: String,
    /** Semantic version string (e.g., "1.0.0") */
    val version: String,
    /** Numeric version code for update comparison */
    val versionCode: Int,
    /** Brief description of the plugin */
    val description: String,
    /** Plugin author information */
    val author: PluginAuthor,
    /** Type of plugin (THEME, TTS, TRANSLATION, FEATURE) */
    val type: PluginType,
    /** Permissions required by the plugin */
    val permissions: List<PluginPermission>,
    /** Minimum IReader version required */
    val minIReaderVersion: String,
    /** Supported platforms */
    val platforms: List<Platform>,
    /** Fully qualified main class name (e.g., "com.example.MyPlugin") */
    val mainClass: String? = null,
    /** Monetization model (optional) */
    val monetization: PluginMonetization? = null,
    /** URL to plugin icon (optional) */
    val iconUrl: String? = null,
    /** URLs to plugin screenshots (optional) */
    val screenshotUrls: List<String> = emptyList(),
    /** 
     * Native libraries required by this plugin, mapped by platform.
     * Key: platform identifier (e.g., "windows-x64", "macos-arm64", "linux-x64")
     * Value: List of relative paths to native library files within the plugin package
     * 
     * Example:
     * ```
     * "nativeLibraries": {
     *   "windows-x64": ["native/windows-x64/piper.dll", "native/windows-x64/onnxruntime.dll"],
     *   "macos-arm64": ["native/macos-arm64/libpiper.dylib"],
     *   "linux-x64": ["native/linux-x64/libpiper.so"]
     * }
     * ```
     */
    val nativeLibraries: Map<String, List<String>>? = null,
    /**
     * Custom metadata for plugin-specific configuration.
     * A flexible key-value store for plugin-type-specific data.
     * 
     * For GRADIO_TTS plugins, use these keys:
     * - "gradio.spaceUrl": Hugging Face Space URL (required)
     * - "gradio.apiName": API endpoint name (e.g., "/predict")
     * - "gradio.apiType": API type (AUTO, GRADIO_API_CALL, CALL, API_PREDICT, RUN, QUEUE)
     * - "gradio.audioOutputIndex": Output index for audio (default: "0")
     * - "gradio.params": JSON array of parameter definitions
     * - "gradio.languages": Comma-separated language codes (e.g., "en,es,fr")
     * - "gradio.supportsVoiceCloning": "true" if voice cloning is supported
     * 
     * Example for Gradio TTS:
     * ```kotlin
     * metadata = mapOf(
     *     "gradio.spaceUrl" to "https://example.hf.space",
     *     "gradio.apiName" to "/predict",
     *     "gradio.apiType" to "GRADIO_API",
     *     "gradio.params" to """[{"type":"text","name":"text"}]"""
     * )
     * ```
     * 
     * @since 1.1.0
     */
    val metadata: Map<String, String>? = null
)

/**
 * Plugin author information.
 */
@Serializable
data class PluginAuthor(
    /** Author's display name */
    val name: String,
    /** Author's email (optional) */
    val email: String? = null,
    /** Author's website (optional) */
    val website: String? = null
)

/**
 * Supported platforms for plugins.
 */
@Serializable
enum class Platform {
    ANDROID,
    IOS,
    DESKTOP
}
