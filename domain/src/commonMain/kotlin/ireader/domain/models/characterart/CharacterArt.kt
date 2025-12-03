package ireader.domain.models.characterart

import kotlinx.serialization.Serializable
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Represents AI-generated character art for a book character
 */
@Serializable
data class CharacterArt(
    val id: String = "",
    val characterName: String,
    val bookTitle: String,
    val bookAuthor: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val localImagePath: String = "",
    val thumbnailUrl: String = "",
    val submitterId: String = "",
    val submitterUsername: String = "",
    val aiModel: String = "",
    val prompt: String = "",
    val likesCount: Int = 0,
    val isLikedByUser: Boolean = false,
    val status: CharacterArtStatus = CharacterArtStatus.PENDING,
    val submittedAt: Long = currentTimeToLong(),
    val isFeatured: Boolean = false,
    val tags: List<String> = emptyList(),
    val width: Int = 0,
    val height: Int = 0
)

/**
 * Character art approval status
 */
enum class CharacterArtStatus {
    PENDING,
    APPROVED,
    REJECTED
}

/**
 * Request to submit new character art
 */
@Serializable
data class SubmitCharacterArtRequest(
    val characterName: String,
    val bookTitle: String,
    val bookAuthor: String = "",
    val description: String = "",
    val aiModel: String = "",
    val prompt: String = "",
    val tags: List<String> = emptyList(),
    val imageBytes: ByteArray? = null,
    val imagePath: String = ""
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as SubmitCharacterArtRequest
        return characterName == other.characterName &&
                bookTitle == other.bookTitle &&
                imagePath == other.imagePath
    }
    override fun hashCode(): Int {
        var result = characterName.hashCode()
        result = 31 * result + bookTitle.hashCode()
        result = 31 * result + imagePath.hashCode()
        return result
    }
}

/**
 * Available art style filters
 */
enum class ArtStyleFilter(val displayName: String, val icon: String) {
    ALL("All", "🎨"),
    ANIME("Anime", "🎌"),
    REALISTIC("Realistic", "📷"),
    FANTASY("Fantasy", "🧙"),
    MINIMALIST("Minimal", "◻️"),
    WATERCOLOR("Watercolor", "🖌️"),
    DIGITAL("Digital", "💻")
}

/**
 * Sort options for character art gallery
 */
enum class CharacterArtSort(val displayName: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    MOST_LIKED("Most Liked"),
    BOOK_TITLE("Book Title"),
    CHARACTER_NAME("Character")
}

/**
 * Gallery view mode
 */
enum class GalleryViewMode {
    GRID,
    LIST,
    MASONRY
}
