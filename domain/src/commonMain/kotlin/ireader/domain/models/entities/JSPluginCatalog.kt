package ireader.domain.models.entities

import ireader.domain.js.bridge.JSPluginSource
import ireader.domain.js.models.PluginMetadata
import java.io.File

/**
 * Catalog implementation for JavaScript plugins.
 * Wraps a JSPluginSource as a CatalogInstalled for integration with IReader's catalog system.
 */
data class JSPluginCatalog(
    override val source: JSPluginSource,
    val metadata: PluginMetadata,
    val pluginFile: File,
    override val name: String = metadata.name,
    override val description: String = metadata.site,
    override val pkgName: String = metadata.id,
    override val versionName: String = metadata.version,
    override val versionCode: Int = metadata.version.replace(".", "").toIntOrNull() ?: 100,
    override val iconUrl: String = metadata.icon,
    override val installDir: File = pluginFile.parentFile,
    override val nsfw: Boolean = false,
    override val isPinned: Boolean = false
) : CatalogInstalled() {
    
    /**
     * Plugin site URL.
     */
    val site: String get() = metadata.site
    
    /**
     * Equality based on source ID (plugin ID hash).
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is JSPluginCatalog) return false
        return sourceId == other.sourceId
    }
    
    /**
     * Hash code based on source ID.
     */
    override fun hashCode(): Int {
        return sourceId.hashCode()
    }
}
