package ireader.domain.models.entities

import kotlinx.serialization.Serializable

/**
 * Global glossary entry that can exist independently of books in the user's library.
 * This allows users to maintain glossaries for books they haven't added yet,
 * or share glossaries across the community.
 */
@Serializable
data class GlobalGlossary(
    val id: Long = 0,
    val bookKey: String, // Unique identifier for the book (e.g., source_id + book_key)
    val bookTitle: String,
    val sourceTerm: String,
    val targetTerm: String,
    val termType: GlossaryTermType,
    val notes: String? = null,
    val sourceLanguage: String = "auto",
    val targetLanguage: String = "en",
    val createdAt: Long,
    val updatedAt: Long,
    val syncedAt: Long? = null, // When last synced with remote
    val remoteId: String? = null // Remote ID for sync
)

/**
 * Export format for global glossary
 */
@Serializable
data class GlobalGlossaryExport(
    val version: Int = 1,
    val bookKey: String,
    val bookTitle: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val entries: List<GlobalGlossaryEntry>,
    val exportedAt: Long,
    val exportedBy: String? = null
)

@Serializable
data class GlobalGlossaryEntry(
    val sourceTerm: String,
    val targetTerm: String,
    val termType: String,
    val notes: String? = null
)

/**
 * Sync status for glossary
 */
enum class GlossarySyncStatus {
    NOT_SYNCED,
    SYNCING,
    SYNCED,
    SYNC_ERROR
}
