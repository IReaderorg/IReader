package ireader.plugin.api

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * API for plugins to access the character database.
 * 
 * This API allows plugins to:
 * - Access character information
 * - Track character appearances
 * - View relationships
 * - Add notes and timeline events
 */
interface CharacterDatabaseApi {
    
    // Character access
    
    /**
     * Get all characters for a book.
     */
    suspend fun getCharactersForBook(bookId: Long): List<CharacterInfo>
    
    /**
     * Get a specific character by ID.
     */
    suspend fun getCharacter(characterId: String): CharacterInfo?
    
    /**
     * Search characters by name.
     */
    suspend fun searchCharacters(query: String, bookId: Long? = null): List<CharacterInfo>
    
    /**
     * Get characters by role.
     */
    suspend fun getCharactersByRole(role: String, bookId: Long): List<CharacterInfo>
    
    // Relationships
    
    /**
     * Get relationships for a character.
     */
    suspend fun getRelationships(characterId: String): List<RelationshipInfo>
    
    /**
     * Get relationship between two characters.
     */
    suspend fun getRelationshipBetween(char1Id: String, char2Id: String): RelationshipInfo?
    
    // Appearances
    
    /**
     * Get character appearances in a book.
     */
    suspend fun getAppearances(characterId: String, bookId: Long): List<AppearanceInfo>
    
    /**
     * Get characters that appear in a chapter.
     */
    suspend fun getCharactersInChapter(chapterId: Long): List<CharacterInfo>
    
    /**
     * Record a character appearance (for plugins that detect characters).
     */
    suspend fun recordAppearance(
        characterId: String,
        bookId: Long,
        chapterId: Long,
        paragraphIndex: Int,
        textSnippet: String,
        appearanceType: String
    ): Boolean
    
    // Notes
    
    /**
     * Get notes for a character.
     */
    suspend fun getNotes(characterId: String): List<NoteInfo>
    
    /**
     * Add a note to a character.
     */
    suspend fun addNote(
        characterId: String,
        content: String,
        noteType: String,
        bookId: Long? = null,
        chapterId: Long? = null
    ): NoteInfo?
    
    // Timeline
    
    /**
     * Get timeline events for a character.
     */
    suspend fun getTimeline(characterId: String): List<TimelineEventInfo>
    
    // Statistics
    
    /**
     * Get character statistics.
     */
    suspend fun getCharacterStats(characterId: String): CharacterStatsInfo?
    
    /**
     * Get book character summary.
     */
    suspend fun getBookSummary(bookId: Long): BookCharacterSummaryInfo
    
    // Events
    
    /**
     * Subscribe to character database events.
     */
    fun subscribeToEvents(): Flow<CharacterDatabaseEventInfo>
}

/**
 * Character information for plugins.
 */
@Serializable
data class CharacterInfo(
    val id: String,
    val name: String,
    val aliases: List<String>,
    val description: String,
    val imageUrl: String?,
    val role: String,
    val traits: List<String>,
    val tags: List<String>,
    val bookIds: List<Long>,
    val isUserCreated: Boolean
)

/**
 * Relationship information for plugins.
 */
@Serializable
data class RelationshipInfo(
    val id: String,
    val character1Id: String,
    val character1Name: String,
    val character2Id: String,
    val character2Name: String,
    val relationshipType: String,
    val description: String,
    val strength: Float
)

/**
 * Appearance information for plugins.
 */
@Serializable
data class AppearanceInfo(
    val id: String,
    val characterId: String,
    val chapterId: Long,
    val chapterTitle: String,
    val paragraphIndex: Int,
    val textSnippet: String,
    val appearanceType: String
)

/**
 * Note information for plugins.
 */
@Serializable
data class NoteInfo(
    val id: String,
    val characterId: String,
    val content: String,
    val noteType: String,
    val bookId: Long?,
    val chapterId: Long?,
    val createdAt: Long
)

/**
 * Timeline event information for plugins.
 */
@Serializable
data class TimelineEventInfo(
    val id: String,
    val characterId: String,
    val eventType: String,
    val title: String,
    val description: String,
    val bookId: Long,
    val chapterId: Long,
    val orderIndex: Int
)

/**
 * Character statistics for plugins.
 */
@Serializable
data class CharacterStatsInfo(
    val characterId: String,
    val totalAppearances: Int,
    val dialogueCount: Int,
    val chaptersAppearedIn: Int,
    val relationshipCount: Int,
    val firstAppearanceChapter: Int,
    val lastAppearanceChapter: Int
)

/**
 * Book character summary for plugins.
 */
@Serializable
data class BookCharacterSummaryInfo(
    val bookId: Long,
    val totalCharacters: Int,
    val protagonists: List<String>,
    val antagonists: List<String>,
    val supportingCharacters: Int,
    val relationshipCount: Int
)

/**
 * Character database event for plugins.
 */
@Serializable
sealed class CharacterDatabaseEventInfo {
    @Serializable
    data class CharacterAdded(val characterId: String, val name: String, val bookId: Long) : CharacterDatabaseEventInfo()
    
    @Serializable
    data class CharacterUpdated(val characterId: String, val name: String) : CharacterDatabaseEventInfo()
    
    @Serializable
    data class CharacterDeleted(val characterId: String) : CharacterDatabaseEventInfo()
    
    @Serializable
    data class RelationshipAdded(val char1Name: String, val char2Name: String, val type: String) : CharacterDatabaseEventInfo()
    
    @Serializable
    data class AppearanceRecorded(val characterId: String, val chapterId: Long) : CharacterDatabaseEventInfo()
    
    @Serializable
    data class NoteAdded(val characterId: String, val noteType: String) : CharacterDatabaseEventInfo()
}
