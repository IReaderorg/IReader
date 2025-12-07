

package ireader.domain.models.entities



import ireader.core.source.model.ChapterInfo
import ireader.core.source.model.Page
import kotlinx.serialization.Serializable

/**
 * @param number it's number of current chapter
 */
@Serializable
data class Chapter(
    val id: Long = 0,
    val bookId: Long,
    val key: String,
    val name: String,
    val read: Boolean = false,
    val bookmark: Boolean = false,
    val dateUpload: Long = 0,
    val dateFetch: Long = 0,
    val sourceOrder: Long = 0,
    val content: List<Page> = emptyList(),
    val number: Float = -1f,
    val translator: String = "",
    val lastPageRead: Long = 0,
    val type: Long = ChapterInfo.NOVEL
) {

    val isRecognizedNumber get() = number >= 0
    
    /**
     * Check if the chapter has no actual text content.
     * This properly handles placeholder content (empty Text objects) from light queries.
     */
    fun isEmpty(): Boolean {
        if (content.isEmpty()) return true
        
        // Extract actual text content from Text pages
        val textContent = content.mapNotNull { page ->
            when (page) {
                is ireader.core.source.model.Text -> page.text.takeIf { it.isNotBlank() }
                else -> null
            }
        }
        
        return textContent.isEmpty()
    }
}

fun Chapter.toChapterInfo(): ChapterInfo {
    return ChapterInfo(
        key = this.key,
        scanlator = this.translator,
        name = this.name,
        dateUpload = this.dateUpload,
        number = this.number,
    )
}

fun ChapterInfo.toChapter(bookId: Long): Chapter {
    return Chapter(
        name = name,
        key = key,
        bookId = bookId,
        number = number,
        dateUpload = dateUpload,
        translator = this.scanlator,
        type =  this.type,

    )
}
