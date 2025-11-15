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
    val includeImages: Boolean = true
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
 * Represents a chapter in the EPUB structure
 */
data class EpubChapter(
    val id: String,
    val title: String,
    val content: String,
    val order: Int,
    val fileName: String = "chapter_${order}.xhtml"
)

/**
 * Manifest item for content.opf
 */
data class ManifestItem(
    val id: String,
    val href: String,
    val mediaType: String
)
