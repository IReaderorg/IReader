package ireader.domain.models.entities

import kotlinx.serialization.Serializable

/**
 * Text replacement rule entity.
 * 
 * Stores find-and-replace rules to automatically replace text in chapter content.
 * Example: Replace "khan" with "khaaan"
 * 
 * Replacements can be:
 * - Global (bookId = null): Applied to all books
 * - Book-specific (bookId = someId): Applied only to that book
 */
@Serializable
data class TextReplacement(
    val id: Long = 0,
    val bookId: Long? = null, // null for global replacements
    val name: String,
    val findText: String,
    val replaceText: String,
    val description: String? = null,
    val enabled: Boolean = true,
    val caseSensitive: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Export format for text replacements
 */
@Serializable
data class TextReplacementExport(
    val replacements: List<TextReplacement>,
    val exportedAt: Long
)
