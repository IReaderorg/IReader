package ireader.domain.js.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Metadata extracted from a JavaScript plugin.
 * Contains plugin identification, configuration, and filter definitions.
 */
@Serializable
data class PluginMetadata(
    val id: String,
    val name: String,
    val icon: String,
    val site: String,
    val version: String,
    val lang: String,
    @Transient
    val imageRequestInit: Map<String, Any>? = null,
    @Transient
    val filters: Map<String, Any>? = null,
    val permissions: List<JSPluginPermission> = listOf(
        JSPluginPermission.NETWORK,
        JSPluginPermission.STORAGE
    )
)
