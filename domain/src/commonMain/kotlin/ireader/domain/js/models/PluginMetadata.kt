package ireader.domain.js.models

/**
 * Metadata extracted from a JavaScript plugin.
 * Contains plugin identification, configuration, and filter definitions.
 */
data class PluginMetadata(
    val id: String,
    val name: String,
    val icon: String,
    val site: String,
    val version: String,
    val lang: String,
    val imageRequestInit: Map<String, Any>? = null,
    val filters: Map<String, Any>? = null,
    val permissions: List<JSPluginPermission> = listOf(
        JSPluginPermission.NETWORK,
        JSPluginPermission.STORAGE
    )
)
