package ireader.domain.models.entities

import ireader.core.source.Source
import ireader.domain.community.CommunitySource

/**
 * Catalog implementation for the Community Source.
 * This is a bundled source that connects to Supabase for community-contributed content.
 * 
 * This is a built-in source and should never show as "broken" even if not configured.
 */
data class CommunityCatalog(
    override val source: CommunitySource,
    override val description: String = "Browse and read community-translated novels. Share your AI translations with others!",
    override val name: String = CommunitySource.SOURCE_NAME,
    override val nsfw: Boolean = false,
    override val isPinned: Boolean = false
) : CatalogLocal() {
    
    override val sourceId: Long
        get() = CommunitySource.SOURCE_ID
    
    companion object {
        /** Community Source icon URL */
        const val ICON_URL = "https://raw.githubusercontent.com/IReaderorg/badge-repo/main/app-icon.png"
        
        /** Check if this is the Community Source by ID */
        fun isCommunitySource(sourceId: Long): Boolean = sourceId == CommunitySource.SOURCE_ID
    }
}
