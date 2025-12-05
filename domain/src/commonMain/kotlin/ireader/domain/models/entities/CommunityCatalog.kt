package ireader.domain.models.entities

import ireader.core.source.Source
import ireader.domain.community.CommunitySource

/**
 * Catalog implementation for the Community Source.
 * This is a bundled source that connects to Supabase for community-contributed content.
 */
data class CommunityCatalog(
    override val source: CommunitySource,
    override val description: String = "Browse and read community-translated novels",
    override val name: String = CommunitySource.SOURCE_NAME,
    override val nsfw: Boolean = false,
    override val isPinned: Boolean = false
) : CatalogLocal() {
    
    override val sourceId: Long
        get() = CommunitySource.SOURCE_ID
    
    companion object {
        const val ICON_URL = "https://raw.githubusercontent.com/IReaderorg/IReader/master/assets/community_source_icon.png"
    }
}
