package ireader.domain.plugins.character

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Character Database Plugin System
 * 
 * Track characters across books with:
 * - Character profiles with descriptions, traits, images
 * - Relationship mapping between characters
 * - Character appearances tracking (which chapters)
 * - Character timeline/arc tracking
 * - Cross-book character linking (same universe)
 * - AI-powered character extraction
 */

/**
 * A character entity in the database.
 */
@Serializable
data class Character(
    val id: String,
    val bookId: Long,
    val name: String,
    val aliases: List<String> = emptyList(),
    val description: String = "",
    val imageUrl: String? = null,
    val role: CharacterRole = CharacterRole.UNKNOWN,
    val traits: List<CharacterTrait> = emptyList(),
    val attributes: Map<String, String> = emptyMap(),
    val firstAppearance: ChapterReference? = null,
    val lastAppearance: ChapterReference? = null,
    val appearanceCount: Int = 0,
    val importance: Float = 0.5f,
    val isUserCreated: Boolean = false,
    val isConfirmed: Boolean = false,
    val linkedCharacterId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val notes: String = ""
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

@Serializable
data class CharacterTrait(
    val trait: String,
    val category: TraitCategory,
    val confidence: Float = 1.0f,
    val source: String? = null
)

@Serializable
enum class TraitCategory {
    PERSONALITY,
    PHYSICAL,
    BACKGROUND,
    ABILITY,
    OCCUPATION,
    RELATIONSHIP_STYLE,
    OTHER
}

@Serializable
data class ChapterReference(
    val chapterId: Long,
    val chapterNumber: Int,
    val chapterTitle: String?
)

/**
 * Relationship between two characters.
 */
@Serializable
data class CharacterRelationship(
    val id: String,
    val characterId: String,
    val relatedCharacterId: String,
    val relationshipType: RelationshipType,
    val customType: String? = null,
    val description: String = "",
    val strength: Float = 0.5f,
    val sentiment: RelationshipSentiment = RelationshipSentiment.NEUTRAL,
    val startChapter: ChapterReference? = null,
    val endChapter: ChapterReference? = null,
    val isActive: Boolean = true,
    val isBidirectional: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
enum class RelationshipType {
    FAMILY_PARENT,
    FAMILY_CHILD,
    FAMILY_SIBLING,
    FAMILY_SPOUSE,
    FAMILY_RELATIVE,
    ROMANTIC_PARTNER,
    ROMANTIC_INTEREST,
    ROMANTIC_EX,
    FRIEND,
    BEST_FRIEND,
    ACQUAINTANCE,
    RIVAL,
    ENEMY,
    MENTOR,
    STUDENT,
    COLLEAGUE,
    EMPLOYER,
    EMPLOYEE,
    ALLY,
    SERVANT,
    MASTER,
    CUSTOM,
    UNKNOWN
}

@Serializable
enum class RelationshipSentiment {
    VERY_POSITIVE,
    POSITIVE,
    NEUTRAL,
    NEGATIVE,
    VERY_NEGATIVE,
    COMPLEX,
    UNKNOWN
}

/**
 * Character appearance in a chapter.
 */
@Serializable
data class CharacterAppearance(
    val id: String,
    val characterId: String,
    val bookId: Long,
    val chapterId: Long,
    val chapterNumber: Int,
    val mentionCount: Int = 1,
    val dialogueCount: Int = 0,
    val significantMoments: List<SignificantMoment> = emptyList(),
    val interactsWith: List<String> = emptyList(),
    val sentiment: AppearanceSentiment = AppearanceSentiment.NEUTRAL,
    val createdAt: Long
)

@Serializable
data class SignificantMoment(
    val description: String,
    val type: MomentType,
    val position: Int,
    val quote: String? = null
)

@Serializable
enum class MomentType {
    INTRODUCTION,
    REVELATION,
    CONFLICT,
    RESOLUTION,
    DEVELOPMENT,
    DEATH,
    TRANSFORMATION,
    KEY_DIALOGUE,
    ACTION,
    OTHER
}

@Serializable
enum class AppearanceSentiment {
    TRIUMPHANT,
    HAPPY,
    NEUTRAL,
    SAD,
    TRAGIC,
    TENSE,
    MYSTERIOUS
}

/**
 * Character arc/development tracking.
 */
@Serializable
data class CharacterArc(
    val id: String,
    val characterId: String,
    val bookId: Long,
    val arcType: ArcType,
    val title: String,
    val description: String,
    val startChapter: ChapterReference,
    val endChapter: ChapterReference? = null,
    val milestones: List<ArcMilestone> = emptyList(),
    val status: ArcStatus = ArcStatus.IN_PROGRESS,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
enum class ArcType {
    HERO_JOURNEY,
    REDEMPTION,
    FALL,
    COMING_OF_AGE,
    TRANSFORMATION,
    REVENGE,
    LOVE,
    LOSS,
    DISCOVERY,
    POWER,
    CUSTOM
}

@Serializable
data class ArcMilestone(
    val title: String,
    val description: String,
    val chapter: ChapterReference,
    val type: MilestoneType
)

@Serializable
enum class MilestoneType {
    INCITING_INCIDENT,
    RISING_ACTION,
    CLIMAX,
    FALLING_ACTION,
    RESOLUTION,
    TURNING_POINT,
    SETBACK,
    VICTORY,
    REVELATION
}

@Serializable
enum class ArcStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
}

/**
 * Character group/faction.
 */
@Serializable
data class CharacterGroup(
    val id: String,
    val bookId: Long,
    val name: String,
    val description: String = "",
    val type: GroupType,
    val memberIds: List<String> = emptyList(),
    val leaderId: String? = null,
    val imageUrl: String? = null,
    val attributes: Map<String, String> = emptyMap(),
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
enum class GroupType {
    FAMILY,
    ORGANIZATION,
    FACTION,
    TEAM,
    SPECIES,
    CLASS,
    NATION,
    RELIGION,
    OTHER
}

/**
 * Cross-book character link (same character in different books/series).
 */
@Serializable
data class CharacterLink(
    val id: String,
    val primaryCharacterId: String,
    val linkedCharacterId: String,
    val linkType: LinkType,
    val confidence: Float = 1.0f,
    val notes: String = "",
    val createdAt: Long
)

@Serializable
enum class LinkType {
    SAME_CHARACTER,
    ALTERNATE_VERSION,
    ANCESTOR,
    DESCENDANT,
    REINCARNATION,
    RELATED
}

/**
 * Character search/filter options.
 */
@Serializable
data class CharacterSearchQuery(
    val bookId: Long? = null,
    val searchText: String? = null,
    val roles: List<CharacterRole> = emptyList(),
    val traits: List<String> = emptyList(),
    val minImportance: Float? = null,
    val hasImage: Boolean? = null,
    val isConfirmed: Boolean? = null,
    val sortBy: CharacterSortOption = CharacterSortOption.IMPORTANCE,
    val sortDescending: Boolean = true,
    val limit: Int = 50,
    val offset: Int = 0
)

@Serializable
enum class CharacterSortOption {
    NAME,
    IMPORTANCE,
    APPEARANCE_COUNT,
    FIRST_APPEARANCE,
    LAST_UPDATED,
    CREATED_AT
}

/**
 * Character statistics for a book.
 */
@Serializable
data class BookCharacterStats(
    val bookId: Long,
    val totalCharacters: Int,
    val protagonists: Int,
    val antagonists: Int,
    val supportingCharacters: Int,
    val minorCharacters: Int,
    val totalRelationships: Int,
    val totalGroups: Int,
    val mostMentionedCharacter: String?,
    val characterWithMostRelationships: String?,
    val averageAppearancesPerCharacter: Float,
    val lastUpdated: Long
)
