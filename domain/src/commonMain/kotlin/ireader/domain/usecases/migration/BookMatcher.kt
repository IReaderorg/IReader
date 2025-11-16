package ireader.domain.usecases.migration

import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookItem
import ireader.domain.models.migration.MigrationMatch

/**
 * Matches books between sources using fuzzy string matching
 */
class BookMatcher {
    
    /**
     * Calculate similarity between two books and return ranked candidates
     * @param originalBook The book to match
     * @param candidates List of potential matches
     * @return List of matches with similarity > 0.6 threshold, sorted by confidence
     */
    fun findMatches(
        originalBook: Book,
        candidates: List<BookItem>
    ): List<MigrationMatch> {
        return candidates
            .map { candidate ->
                val confidence = calculateSimilarity(
                    originalTitle = originalBook.title,
                    originalAuthor = originalBook.author,
                    matchTitle = candidate.title,
                    matchAuthor = candidate.author
                )
                
                MigrationMatch(
                    novel = candidate,
                    confidenceScore = confidence,
                    matchReason = buildMatchReason(
                        originalTitle = originalBook.title,
                        matchTitle = candidate.title,
                        originalAuthor = originalBook.author,
                        matchAuthor = candidate.author,
                        confidence = confidence
                    )
                )
            }
            .filter { it.confidenceScore > 0.6f }
            .sortedByDescending { it.confidenceScore }
    }
    
    /**
     * Calculate similarity score using Levenshtein distance
     * Combines title similarity (70%) and author match (30%)
     * @return Similarity score from 0.0 to 1.0
     */
    fun calculateSimilarity(
        originalTitle: String,
        originalAuthor: String,
        matchTitle: String,
        matchAuthor: String
    ): Float {
        val titleSimilarity = calculateTitleSimilarity(originalTitle, matchTitle)
        val authorSimilarity = calculateAuthorSimilarity(originalAuthor, matchAuthor)
        
        // Weight: 70% title similarity, 30% author match
        return (titleSimilarity * 0.7f) + (authorSimilarity * 0.3f)
    }
    
    /**
     * Calculate title similarity using Levenshtein distance
     */
    private fun calculateTitleSimilarity(title1: String, title2: String): Float {
        if (title1.isBlank() || title2.isBlank()) return 0f
        
        val normalized1 = title1.lowercase().trim()
        val normalized2 = title2.lowercase().trim()
        
        // Exact match
        if (normalized1 == normalized2) return 1.0f
        
        // Calculate Levenshtein distance
        val distance = levenshteinDistance(normalized1, normalized2)
        val maxLength = maxOf(normalized1.length, normalized2.length)
        
        // Convert distance to similarity score (0.0 to 1.0)
        return 1.0f - (distance.toFloat() / maxLength)
    }
    
    /**
     * Calculate author similarity
     */
    private fun calculateAuthorSimilarity(author1: String, author2: String): Float {
        // If either author is blank, return neutral score
        if (author1.isBlank() || author2.isBlank()) return 0.5f
        
        val normalized1 = author1.lowercase().trim()
        val normalized2 = author2.lowercase().trim()
        
        // Exact match
        if (normalized1 == normalized2) return 1.0f
        
        // Check if one contains the other (common for author name variations)
        if (normalized1.contains(normalized2) || normalized2.contains(normalized1)) {
            return 0.8f
        }
        
        // No match
        return 0.0f
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     * This measures the minimum number of single-character edits needed to change one string into another
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        
        // Create a matrix to store distances
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        // Initialize first column (deletions from s1)
        for (i in 0..len1) {
            dp[i][0] = i
        }
        
        // Initialize first row (insertions to s1)
        for (j in 0..len2) {
            dp[0][j] = j
        }
        
        // Fill the matrix
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        
        return dp[len1][len2]
    }
    
    /**
     * Build a human-readable match reason
     */
    private fun buildMatchReason(
        originalTitle: String,
        matchTitle: String,
        originalAuthor: String,
        matchAuthor: String,
        confidence: Float
    ): String {
        val reasons = mutableListOf<String>()
        
        val normalizedOriginalTitle = originalTitle.lowercase().trim()
        val normalizedMatchTitle = matchTitle.lowercase().trim()
        
        if (normalizedOriginalTitle == normalizedMatchTitle) {
            reasons.add("Exact title match")
        } else if (confidence > 0.8f) {
            reasons.add("Very similar title")
        } else if (confidence > 0.7f) {
            reasons.add("Similar title")
        } else {
            reasons.add("Partial title match")
        }
        
        if (originalAuthor.isNotBlank() && matchAuthor.isNotBlank()) {
            val normalizedOriginalAuthor = originalAuthor.lowercase().trim()
            val normalizedMatchAuthor = matchAuthor.lowercase().trim()
            
            if (normalizedOriginalAuthor == normalizedMatchAuthor) {
                reasons.add("Same author")
            } else if (normalizedOriginalAuthor.contains(normalizedMatchAuthor) || 
                       normalizedMatchAuthor.contains(normalizedOriginalAuthor)) {
                reasons.add("Similar author")
            }
        }
        
        return reasons.joinToString(", ")
    }
}
