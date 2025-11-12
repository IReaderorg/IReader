package ireader.domain.utils

/**
 * Utility object for normalizing book titles into universal book IDs
 * This ensures books with the same title from different sources share the same identifier
 */
object BookIdNormalizer {
    
    /**
     * Normalize a book title into a universal book ID
     * 
     * The normalization process:
     * 1. Convert to lowercase
     * 2. Remove special characters (keep only alphanumeric and spaces)
     * 3. Replace spaces with hyphens
     * 4. Remove consecutive hyphens
     * 5. Trim leading/trailing hyphens
     * 
     * Examples:
     * - "Lord of the Mysteries" -> "lord-of-the-mysteries"
     * - "Re:Zero - Starting Life in Another World" -> "rezero-starting-life-in-another-world"
     * - "The King's Avatar!!!" -> "the-kings-avatar"
     * 
     * @param title The book title to normalize
     * @return The normalized book ID
     */
    fun normalize(title: String): String {
        return title
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "") // Remove special characters
            .trim()
            .replace(Regex("\\s+"), "-") // Replace spaces with hyphens
            .replace(Regex("-+"), "-") // Remove consecutive hyphens
            .trim('-') // Remove leading/trailing hyphens
    }
}
