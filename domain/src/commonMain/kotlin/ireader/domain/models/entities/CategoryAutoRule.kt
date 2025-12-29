package ireader.domain.models.entities

/**
 * Represents an auto-categorization rule for a category.
 * When enabled, novels matching the rule criteria will be automatically
 * assigned to the associated category.
 *
 * @param id Unique identifier for the rule
 * @param categoryId The category this rule belongs to
 * @param ruleType Type of rule (GENRE or SOURCE)
 * @param value The value to match (genre name or source ID as string)
 * @param isEnabled Whether this rule is active
 */
data class CategoryAutoRule(
    val id: Long = 0,
    val categoryId: Long,
    val ruleType: RuleType,
    val value: String,
    val isEnabled: Boolean = true,
) {
    enum class RuleType {
        /** Match novels by genre name (case-insensitive) */
        GENRE,
        /** Match novels by source ID */
        SOURCE
    }
    
    /**
     * Check if a book matches this rule
     */
    fun matches(book: Book, sourceName: String? = null): Boolean {
        if (!isEnabled) return false
        
        return when (ruleType) {
            RuleType.GENRE -> book.genres.any { genre ->
                genre.equals(value, ignoreCase = true)
            }
            RuleType.SOURCE -> {
                // Match by source ID or source name
                book.sourceId.toString() == value || 
                    sourceName?.equals(value, ignoreCase = true) == true
            }
        }
    }
}

/**
 * Category with its auto-categorization rules
 */
data class CategoryWithRules(
    val category: Category,
    val rules: List<CategoryAutoRule>,
    val bookCount: Int = 0,
)
