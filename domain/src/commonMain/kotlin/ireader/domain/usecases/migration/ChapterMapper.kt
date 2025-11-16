package ireader.domain.usecases.migration

import ireader.domain.models.entities.Chapter

/**
 * Maps chapters between sources during migration
 */
class ChapterMapper {
    
    /**
     * Result of chapter mapping
     */
    data class ChapterMapping(
        val oldChapterId: Long,
        val newChapterId: Long,
        val confidence: Float,
        val matchMethod: MatchMethod
    )
    
    enum class MatchMethod {
        EXACT_TITLE,
        CHAPTER_NUMBER,
        FUZZY_TITLE,
        NO_MATCH
    }
    
    /**
     * Map chapters from old source to new source
     * @param oldChapters Chapters from the original source
     * @param newChapters Chapters from the target source
     * @return Map of old chapter IDs to new chapter IDs with confidence scores
     */
    fun mapChapters(
        oldChapters: List<Chapter>,
        newChapters: List<Chapter>
    ): List<ChapterMapping> {
        val mappings = mutableListOf<ChapterMapping>()
        val usedNewChapters = mutableSetOf<Long>()
        
        // First pass: Exact title matches
        for (oldChapter in oldChapters) {
            val exactMatch = newChapters.find { newChapter ->
                newChapter.id !in usedNewChapters &&
                normalizeTitle(oldChapter.name) == normalizeTitle(newChapter.name)
            }
            
            if (exactMatch != null) {
                mappings.add(
                    ChapterMapping(
                        oldChapterId = oldChapter.id,
                        newChapterId = exactMatch.id,
                        confidence = 1.0f,
                        matchMethod = MatchMethod.EXACT_TITLE
                    )
                )
                usedNewChapters.add(exactMatch.id)
            }
        }
        
        // Second pass: Chapter number matches for unmapped chapters
        val unmappedOldChapters = oldChapters.filter { old ->
            mappings.none { it.oldChapterId == old.id }
        }
        
        for (oldChapter in unmappedOldChapters) {
            val oldChapterNum = extractChapterNumber(oldChapter.name)
            if (oldChapterNum != null) {
                val numberMatch = newChapters.find { newChapter ->
                    newChapter.id !in usedNewChapters &&
                    extractChapterNumber(newChapter.name) == oldChapterNum
                }
                
                if (numberMatch != null) {
                    mappings.add(
                        ChapterMapping(
                            oldChapterId = oldChapter.id,
                            newChapterId = numberMatch.id,
                            confidence = 0.8f,
                            matchMethod = MatchMethod.CHAPTER_NUMBER
                        )
                    )
                    usedNewChapters.add(numberMatch.id)
                }
            }
        }
        
        // Third pass: Fuzzy title matching for remaining unmapped chapters
        val stillUnmappedOldChapters = oldChapters.filter { old ->
            mappings.none { it.oldChapterId == old.id }
        }
        
        for (oldChapter in stillUnmappedOldChapters) {
            val fuzzyMatch = findBestFuzzyMatch(
                oldChapter,
                newChapters.filter { it.id !in usedNewChapters }
            )
            
            if (fuzzyMatch != null && fuzzyMatch.second > 0.7f) {
                mappings.add(
                    ChapterMapping(
                        oldChapterId = oldChapter.id,
                        newChapterId = fuzzyMatch.first.id,
                        confidence = fuzzyMatch.second,
                        matchMethod = MatchMethod.FUZZY_TITLE
                    )
                )
                usedNewChapters.add(fuzzyMatch.first.id)
            }
        }
        
        return mappings
    }
    
    /**
     * Normalize chapter title for comparison
     */
    private fun normalizeTitle(title: String): String {
        return title.lowercase()
            .trim()
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[^a-z0-9\\s]"), "")
    }
    
    /**
     * Extract chapter number from title
     * Handles variations like "Chapter 1", "Ch. 1", "1", "Episode 1", etc.
     */
    fun extractChapterNumber(title: String): Double? {
        // Common patterns for chapter numbers
        val patterns = listOf(
            Regex("""chapter\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE),
            Regex("""ch\.?\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE),
            Regex("""episode\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE),
            Regex("""ep\.?\s*(\d+(?:\.\d+)?)""", RegexOption.IGNORE_CASE),
            Regex("""^(\d+(?:\.\d+)?)"""), // Just a number at the start
            Regex("""#(\d+(?:\.\d+)?)"""), // #123 format
        )
        
        for (pattern in patterns) {
            val match = pattern.find(title)
            if (match != null) {
                return match.groupValues[1].toDoubleOrNull()
            }
        }
        
        return null
    }
    
    /**
     * Find best fuzzy match for a chapter
     * @return Pair of (matched chapter, confidence score) or null if no good match
     */
    private fun findBestFuzzyMatch(
        oldChapter: Chapter,
        candidates: List<Chapter>
    ): Pair<Chapter, Float>? {
        if (candidates.isEmpty()) return null
        
        val oldTitle = normalizeTitle(oldChapter.name)
        
        val matches = candidates.map { candidate ->
            val candidateTitle = normalizeTitle(candidate.name)
            val similarity = calculateStringSimilarity(oldTitle, candidateTitle)
            candidate to similarity
        }
        
        return matches.maxByOrNull { it.second }
    }
    
    /**
     * Calculate similarity between two strings using Levenshtein distance
     */
    private fun calculateStringSimilarity(s1: String, s2: String): Float {
        if (s1.isEmpty() || s2.isEmpty()) return 0f
        if (s1 == s2) return 1.0f
        
        val distance = levenshteinDistance(s1, s2)
        val maxLength = maxOf(s1.length, s2.length)
        
        return 1.0f - (distance.toFloat() / maxLength)
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
        
        for (i in 0..len1) {
            dp[i][0] = i
        }
        
        for (j in 0..len2) {
            dp[0][j] = j
        }
        
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        
        return dp[len1][len2]
    }
    
    /**
     * Get mapping statistics
     */
    fun getMappingStatistics(mappings: List<ChapterMapping>): MappingStatistics {
        return MappingStatistics(
            totalMapped = mappings.size,
            exactMatches = mappings.count { it.matchMethod == MatchMethod.EXACT_TITLE },
            numberMatches = mappings.count { it.matchMethod == MatchMethod.CHAPTER_NUMBER },
            fuzzyMatches = mappings.count { it.matchMethod == MatchMethod.FUZZY_TITLE },
            averageConfidence = if (mappings.isNotEmpty()) {
                mappings.map { it.confidence }.average().toFloat()
            } else {
                0f
            }
        )
    }
    
    data class MappingStatistics(
        val totalMapped: Int,
        val exactMatches: Int,
        val numberMatches: Int,
        val fuzzyMatches: Int,
        val averageConfidence: Float
    )
}
