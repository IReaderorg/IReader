package ireader.domain.models.entities

import kotlinx.serialization.Serializable

/**
 * Represents a book in the explore/browse screen.
 * This is a lightweight model for temporary storage of browsed books.
 * Books are promoted to the main Book table when favorited or viewed in detail.
 */
@Serializable
data class ExploreBook(
    val id: Long = 0,
    val sourceId: Long,
    val url: String,
    val title: String,
    val author: String = "",
    val description: String = "",
    val genres: List<String> = emptyList(),
    val status: Long = 0,
    val cover: String = "",
    val dateAdded: Long = 0
) {
    /**
     * Convert to a full Book entity for promotion to the main book table.
     */
    fun toBook(): Book {
        return Book(
            id = 0, // New book, will get ID on insert
            sourceId = sourceId,
            key = url,
            title = title,
            author = author,
            description = description,
            genres = genres,
            status = status,
            cover = cover,
            customCover = "",
            favorite = false,
            lastUpdate = 0,
            initialized = false,
            dateAdded = dateAdded,
            viewer = 0,
            flags = 0
        )
    }
    
    /**
     * Convert to BookItem for UI display.
     */
    fun toBookItem(column: Long = 0): BookItem {
        return BookItem(
            column = column,
            id = id,
            sourceId = sourceId,
            title = title,
            favorite = false,
            cover = cover,
            customCover = "",
            key = url,
            author = author,
            description = description
        )
    }
    
    companion object {
        /**
         * Create an ExploreBook from a Book entity.
         */
        fun fromBook(book: Book): ExploreBook {
            return ExploreBook(
                id = 0,
                sourceId = book.sourceId,
                url = book.key,
                title = book.title,
                author = book.author,
                description = book.description,
                genres = book.genres,
                status = book.status,
                cover = book.cover,
                dateAdded = book.dateAdded
            )
        }
    }
}
