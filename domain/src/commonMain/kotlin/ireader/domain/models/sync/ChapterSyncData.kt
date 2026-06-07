package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Represents chapter data for synchronization.
 *
 * @property globalId Globally unique identifier (sourceId + "|" + chapterKey)
 * @property bookGlobalId Global ID of the book this chapter belongs to
 * @property key Unique key/URL for the chapter
 * @property name Chapter name/title
 * @property read Whether the chapter has been read
 * @property bookmark Whether the chapter is bookmarked
 * @property lastPageRead Last page read in this chapter
 * @property sourceOrder Order from the source
 * @property number Chapter number
 * @property dateUpload Upload date from source
 * @property dateFetch Date when chapter was fetched
 * @property translator Translator/scanlator name
 * @property content Serialized chapter content (pages) as JSON string
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class ChapterSyncData(
    val globalId: String, // sourceId + "|" + key
    val bookGlobalId: String,
    val key: String,
    val name: String,
    val read: Boolean,
    val bookmark: Boolean,
    val lastPageRead: Long,
    val sourceOrder: Long,
    val number: Float,
    val dateUpload: Long,
    val dateFetch: Long,
    val translator: String,
    val content: String // Serialized List<Page> as JSON string
) {
    init {
        require(globalId.isNotBlank()) { "Global ID cannot be empty or blank" }
        require(bookGlobalId.isNotBlank()) { "Book global ID cannot be empty or blank" }
        require(key.isNotBlank()) { "Key cannot be empty or blank" }
        require(name.isNotBlank()) { "Name cannot be empty or blank" }
        require(lastPageRead >= 0) { "Last page read cannot be negative, got: $lastPageRead" }
        require(sourceOrder >= 0) { "Source order cannot be negative, got: $sourceOrder" }
        require(dateUpload >= 0) { "Date upload cannot be negative, got: $dateUpload" }
        require(dateFetch >= 0) { "Date fetch cannot be negative, got: $dateFetch" }
    }
}
