package ireader.domain.models.epub

/**
 * Configuration options for EPUB export
 */
data class ExportOptions(
    val selectedChapters: Set<Long> = emptySet(),
    val includeCover: Boolean = true,
    val paragraphSpacing: Float = 1.0f,
    val chapterHeadingSize: Float = 1.5f,
    val fontFamily: String = "serif",
    val fontSize: Int = 16,
    val includeImages: Boolean = true,
    /** When true, export translated content instead of original content */
    val useTranslatedContent: Boolean = false,
    /** Target language for translated content (e.g., "en", "es") */
    val translationTargetLanguage: String = "en"
)

/**
 * EPUB metadata for content.opf generation
 */
data class EpubMetadata(
    val title: String,
    val author: String,
    val language: String = "en",
    val identifier: String,
    val publisher: String = "IReader",
    val date: String,
    val description: String? = null
)

/**
 * Represents an image embedded in an EPUB chapter
 */
data class EmbeddedImage(
    val fileName: String,
    val mediaType: String,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmbeddedImage) return false
        return fileName == other.fileName
    }
    override fun hashCode(): Int = fileName.hashCode()
}

/**
 * Represents a chapter in the EPUB structure
 */
data class EpubChapter(
    val id: String,
    val title: String,
    val content: String,
    val order: Int,
    val fileName: String = "chapter_${order}.xhtml",
    val images: List<EmbeddedImage> = emptyList()
)

/**
 * Manifest item for content.opf
 */
data class ManifestItem(
    val id: String,
    val href: String,
    val mediaType: String
)
