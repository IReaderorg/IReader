package ireader.domain.plugins.characters

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Character Database Plugin System
 * 
 * Track characters across books with relationships, appearances, and notes.
 * Enables features like:
 * - Character glossary per book
 * - Cross-book character tracking (series)
 * - Relationship mapping
 * - Character timeline
 * - AI-powered character detection
 */

/**
 * Character entity.
 */
@Serializable
data class Character(
    val id: String,
    val name: String,
    val aliases: List<String> = emptyList(),
    val description: String = "",
    val imageUrl: String? = null,
    val role: CharacterRole = CharacterRole.UNKNOWN,
    val traits: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val bookIds: List<Long> = emptyList(),
    val seriesId: String? = null,
    val firstAppearance: CharacterAppearance? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isUserCreated: Boolean = false,
    val confidence: Float = 1.0f,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
enum class CharacterRole {
    PROTAGONIST,
    ANTAGONIST,
    DEUTERAGONIST,
    SUPPORTING,
    MINOR,
    MENTIONED,
    UNKNOWN
}

/**
 * Character appearance in a specific location.
 */
@Serializable
data class CharacterAppearance(
    val id: String,
    val characterId: String,
    val bookId: Long,
    val chapterId: Long,
    val chapterTitle: String,
    val paragraphIndex: Int,
    val textSnippet: String,
    val appearanceType: AppearanceType,
    val timestamp: Long
)

@Serializable
enum class AppearanceType {
    FIRST_MENTION,
    DIALOGUE,
    ACTION,
    DESCRIPTION,
    REFERENCE
}

/**
 * Relationship between characters.
 */
@Serializable
data class CharacterRelationship(
    val id: String,
    val character1Id: String,
    val character2Id: String,
    val relationshipType: RelationshipType,
    val customType: String? = null,
    val description: String = "",
    val strength: Float = 0.5f,
    val isSymmetric: Boolean = true,
    val bookIds: List<Long> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
enum class RelationshipType {
    FAMILY,
    FRIEND,
    ENEMY,
    LOVER,
    MENTOR,
    STUDENT,
    COLLEAGUE,
    RIVAL,
    ALLY,
    SERVANT,
    MASTER,
    CUSTOM,
    UNKNOWN
}

/**
 * Character note/annotation.
 */
@Serializable
data class CharacterNote(
    val id: String,
    val characterId: String,
    val content: String,
    val bookId: Long?,
    val chapterId: Long?,
    val noteType: NoteType,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
enum class NoteType {
    GENERAL,
    SPOILER,
    THEORY,
    QUESTION,
    TIMELINE
}

/**
 * Character timeline event.
 */
@Serializable
data class CharacterTimelineEvent(
    val id: String,
    val characterId: String,
    val bookId: Long,
    val chapterId: Long,
    val eventType: TimelineEventType,
    val title: String,
    val description: String,
    val orderIndex: Int,
    val timestamp: Long
)

@Serializable
enum class TimelineEventType {
    INTRODUCTION,
    MAJOR_EVENT,
    DEVELOPMENT,
    RELATIONSHIP_CHANGE,
    DEATH,
    TRANSFORMATION,
    REVELATION,
    CUSTOM
}

/**
 * Character group/faction.
 */
@Serializable
data class CharacterGroup(
    val id: String,
    val name: String,
    val description: String,
    val characterIds: List<String>,
    val groupType: GroupType,
    val bookIds: List<Long>,
    val imageUrl: String? = null,
    val createdAt: Long
)

@Serializable
enum class GroupType {
    FAMILY,
    ORGANIZATION,
    FACTION,
    TEAM,
    SPECIES,
    CLASS,
    CUSTOM
}

/**
 * Character search/filter options.
 */
@Serializable
data class CharacterFilter(
    val query: String? = null,
    val bookIds: List<Long>? = null,
    val seriesId: String? = null,
    val roles: List<CharacterRole>? = null,
    val tags: List<String>? = null,
    val hasImage: Boolean? = null,
    val isUserCreated: Boolean? = null,
    val sortBy: CharacterSortOption = CharacterSortOption.NAME,
    val sortAscending: Boolean = true
)

@Serializable
enum class CharacterSortOption {
    NAME,
    ROLE,
    FIRST_APPEARANCE,
    LAST_UPDATED,
    APPEARANCE_COUNT
}

/**
 * Character detection result from AI.
 */
@Serializable
data class DetectedCharacter(
    val name: String,
    val aliases: List<String>,
    val description: String,
    val role: CharacterRole,
    val traits: List<String>,
    val appearances: List<DetectedAppearance>,
    val relationships: List<DetectedRelationship>,
    val confidence: Float
)

@Serializable
data class DetectedAppearance(
    val chapterId: Long,
    val paragraphIndex: Int,
    val textSnippet: String,
    val type: AppearanceType
)

@Serializable
data class DetectedRelationship(
    val targetName: String,
    val type: RelationshipType,
    val description: String,
    val confidence: Float
)

/**
 * Character statistics.
 */
@Serializable
data class CharacterStats(
    val characterId: String,
    val totalAppearances: Int,
    val dialogueCount: Int,
    val chaptersAppearedIn: Int,
    val relationshipCount: Int,
    val averageAppearancesPerChapter: Float,
    val mostCommonAppearanceType: AppearanceType,
    val firstAppearanceChapter: Int,
    val lastAppearanceChapter: Int
)

/**
 * Book character summary.
 */
@Serializable
data class BookCharacterSummary(
    val bookId: Long,
    val totalCharacters: Int,
    val protagonists: List<String>,
    val antagonists: List<String>,
    val supportingCharacters: Int,
    val relationshipCount: Int,
    val groupCount: Int
)
