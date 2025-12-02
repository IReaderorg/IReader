package ireader.domain.usecases.backup.lnreader

import ireader.domain.catalogs.CatalogStore
import ireader.domain.models.entities.CatalogLocal

/**
 * Maps LNReader plugin IDs to IReader source IDs
 */
class LNReaderSourceMapper(
    private val catalogStore: CatalogStore
) {
    
    /**
     * Static mapping of known LNReader plugin IDs to IReader source keys
     * Format: LNReader pluginId (lowercase) -> IReader source key pattern
     */
    private val staticMappings = mapOf(
        // English sources
        "novelupdates" to listOf("novelupdates", "en.novelupdates"),
        "readlightnovel" to listOf("readlightnovel", "en.readlightnovel"),
        "boxnovel" to listOf("boxnovel", "en.boxnovel"),
        "novelfull" to listOf("novelfull", "en.novelfull"),
        "lightnovelworld" to listOf("lightnovelworld", "en.lightnovelworld"),
        "wuxiaworld" to listOf("wuxiaworld", "en.wuxiaworld"),
        "webnovel" to listOf("webnovel", "en.webnovel"),
        "royalroad" to listOf("royalroad", "en.royalroad"),
        "scribblehub" to listOf("scribblehub", "en.scribblehub"),
        "freewebnovel" to listOf("freewebnovel", "en.freewebnovel"),
        "lightnovelpub" to listOf("lightnovelpub", "en.lightnovelpub"),
        "novelhall" to listOf("novelhall", "en.novelhall"),
        "novelbin" to listOf("novelbin", "en.novelbin"),
        "allnovel" to listOf("allnovel", "en.allnovel"),
        "mtlnovel" to listOf("mtlnovel", "en.mtlnovel"),
        "novelfire" to listOf("novelfire", "en.novelfire"),
        
        // Chinese sources
        "69shu" to listOf("69shu", "zh.69shu"),
        "biquge" to listOf("biquge", "zh.biquge"),
        "qidian" to listOf("qidian", "zh.qidian"),
        
        // Japanese sources
        "syosetu" to listOf("syosetu", "ja.syosetu"),
        "kakuyomu" to listOf("kakuyomu", "ja.kakuyomu"),
        
        // Korean sources
        "novelpia" to listOf("novelpia", "ko.novelpia"),
        
        // Vietnamese sources
        "truyenfull" to listOf("truyenfull", "vi.truyenfull"),
        "tangthuvien" to listOf("tangthuvien", "vi.tangthuvien"),
        
        // Indonesian sources
        "novelku" to listOf("novelku", "id.novelku"),
        
        // Spanish sources
        "tunovelaligera" to listOf("tunovelaligera", "es.tunovelaligera"),
        
        // Portuguese sources
        "centralnovel" to listOf("centralnovel", "pt.centralnovel"),
    )
    
    /**
     * Map LNReader plugin ID to IReader source ID
     * @return Source ID if found, null otherwise
     */
    fun mapPluginId(pluginId: String): Long? {
        val normalizedId = pluginId.lowercase().trim()
        val allCatalogs = catalogStore.catalogs
        
        // Try static mapping first
        val possibleKeys = staticMappings[normalizedId]
        if (possibleKeys != null) {
            for (key in possibleKeys) {
                val source = allCatalogs.find { catalog ->
                    when (catalog) {
                        is CatalogLocal -> catalog.source?.id?.toString() == key ||
                            catalog.source?.name?.lowercase() == key.lowercase()
                        else -> false
                    }
                }
                if (source is CatalogLocal && source.source != null) {
                    return source.source!!.id
                }
            }
        }
        
        // Try direct key/name match
        val directMatch = allCatalogs.find { catalog ->
            when (catalog) {
                is CatalogLocal -> {
                    val src = catalog.source
                    src != null && (
                        src.id.toString() == normalizedId ||
                        src.name.lowercase() == normalizedId
                    )
                }
                else -> false
            }
        }
        if (directMatch is CatalogLocal && directMatch.source != null) {
            return directMatch.source!!.id
        }
        
        // Try partial match by name
        val partialMatch = allCatalogs.find { catalog ->
            when (catalog) {
                is CatalogLocal -> {
                    val src = catalog.source
                    src != null && (
                        src.name.lowercase().contains(normalizedId) ||
                        normalizedId.contains(src.name.lowercase())
                    )
                }
                else -> false
            }
        }
        if (partialMatch is CatalogLocal && partialMatch.source != null) {
            return partialMatch.source!!.id
        }
        
        return null
    }
    
    /**
     * Get the local/orphaned source ID for novels with unmapped sources
     */
    fun getUnmappedSourceId(): Long = LOCAL_SOURCE_ID
    
    /**
     * Get all known source mappings for debugging/display
     */
    fun getKnownMappings(): Map<String, List<String>> = staticMappings
    
    companion object {
        const val LOCAL_SOURCE_ID = -1L
    }
}
