package ireader.domain.services.tts_service

/**
 * Simple language detection based on character patterns
 * Requirements: 7.2, 7.4, 7.5
 */
class LanguageDetector {
    
    /**
     * Detect the language of a text sample
     * @param text Text to analyze
     * @return ISO 639-1 language code (e.g., "en", "es", "zh")
     */
    fun detectLanguage(text: String): String {
        if (text.isBlank()) return "en" // Default to English
        
        val cleanText = text.trim().take(500) // Analyze first 500 chars
        
        // Check for specific character ranges
        return when {
            // Chinese (Simplified/Traditional)
            containsCJK(cleanText, 0x4E00, 0x9FFF) -> "zh"
            
            // Japanese (Hiragana, Katakana, Kanji)
            containsCJK(cleanText, 0x3040, 0x309F) || 
            containsCJK(cleanText, 0x30A0, 0x30FF) -> "ja"
            
            // Korean (Hangul)
            containsCJK(cleanText, 0xAC00, 0xD7AF) -> "ko"
            
            // Arabic
            containsScript(cleanText, 0x0600, 0x06FF) -> "ar"
            
            // Hebrew
            containsScript(cleanText, 0x0590, 0x05FF) -> "he"
            
            // Cyrillic (Russian, Ukrainian, etc.)
            containsScript(cleanText, 0x0400, 0x04FF) -> detectCyrillic(cleanText)
            
            // Greek
            containsScript(cleanText, 0x0370, 0x03FF) -> "el"
            
            // Thai
            containsScript(cleanText, 0x0E00, 0x0E7F) -> "th"
            
            // Devanagari (Hindi, Sanskrit)
            containsScript(cleanText, 0x0900, 0x097F) -> "hi"
            
            // Latin-based languages
            else -> detectLatinLanguage(cleanText)
        }
    }
    
    /**
     * Check if text contains characters in a specific Unicode range
     */
    private fun containsCJK(text: String, rangeStart: Int, rangeEnd: Int): Boolean {
        return text.any { char ->
            char.code in rangeStart..rangeEnd
        }
    }
    
    /**
     * Check if text contains characters in a specific script range
     */
    private fun containsScript(text: String, rangeStart: Int, rangeEnd: Int): Boolean {
        val scriptChars = text.count { char ->
            char.code in rangeStart..rangeEnd
        }
        return scriptChars > text.length * 0.3 // At least 30% of characters
    }
    
    /**
     * Detect specific Cyrillic language
     */
    private fun detectCyrillic(text: String): String {
        // Ukrainian-specific characters
        if (text.contains('є') || text.contains('і') || text.contains('ї') || text.contains('ґ')) {
            return "uk"
        }
        // Default to Russian
        return "ru"
    }
    
    /**
     * Detect Latin-based language using common words and patterns
     */
    private fun detectLatinLanguage(text: String): String {
        val lowerText = text.lowercase()
        
        // Spanish indicators
        val spanishScore = countMatches(lowerText, listOf(
            "el ", "la ", "de ", "que ", "en ", "los ", "las ", "del ", "por ", "para ",
            "ción", "ñ"
        ))
        
        // French indicators
        val frenchScore = countMatches(lowerText, listOf(
            "le ", "la ", "les ", "de ", "des ", "un ", "une ", "est ", "dans ", "pour ",
            "ç", "à", "é", "è", "ê"
        ))
        
        // German indicators
        val germanScore = countMatches(lowerText, listOf(
            "der ", "die ", "das ", "und ", "ist ", "den ", "dem ", "des ", "ein ", "eine ",
            "ß", "ä", "ö", "ü"
        ))
        
        // Italian indicators
        val italianScore = countMatches(lowerText, listOf(
            "il ", "la ", "di ", "che ", "per ", "con ", "del ", "della ", "gli ", "le ",
            "zione", "ità"
        ))
        
        // Portuguese indicators
        val portugueseScore = countMatches(lowerText, listOf(
            "o ", "a ", "de ", "que ", "do ", "da ", "em ", "para ", "com ", "os ",
            "ção", "ã", "õ"
        ))
        
        // Dutch indicators
        val dutchScore = countMatches(lowerText, listOf(
            "de ", "het ", "een ", "van ", "in ", "op ", "te ", "voor ", "aan ", "met ",
            "ij", "oe"
        ))
        
        // Polish indicators
        val polishScore = countMatches(lowerText, listOf(
            "w ", "i ", "na ", "z ", "do ", "się ", "że ", "nie ", "jest ", "to ",
            "ł", "ą", "ę", "ć", "ń", "ś", "ź", "ż"
        ))
        
        // Turkish indicators
        val turkishScore = countMatches(lowerText, listOf(
            "bir ", "ve ", "bu ", "için ", "ile ", "da ", "de ", "var ", "mi ", "ne ",
            "ı", "ş", "ğ", "ç"
        ))
        
        // Swedish indicators
        val swedishScore = countMatches(lowerText, listOf(
            "och ", "i ", "att ", "det ", "som ", "på ", "är ", "för ", "en ", "av ",
            "å", "ä", "ö"
        ))
        
        // Norwegian indicators
        val norwegianScore = countMatches(lowerText, listOf(
            "og ", "i ", "det ", "er ", "til ", "på ", "for ", "med ", "av ", "en ",
            "å", "æ", "ø"
        ))
        
        // Danish indicators
        val danishScore = countMatches(lowerText, listOf(
            "og ", "i ", "det ", "er ", "til ", "på ", "for ", "med ", "af ", "en ",
            "å", "æ", "ø"
        ))
        
        // Finnish indicators
        val finnishScore = countMatches(lowerText, listOf(
            "ja ", "on ", "ei ", "se ", "että ", "oli ", "olla ", "hän ", "kun ", "niin ",
            "ä", "ö"
        ))
        
        // Czech indicators
        val czechScore = countMatches(lowerText, listOf(
            "a ", "v ", "na ", "se ", "je ", "s ", "z ", "do ", "o ", "že ",
            "č", "ř", "š", "ž", "ý", "ě"
        ))
        
        // Vietnamese indicators
        val vietnameseScore = countMatches(lowerText, listOf(
            "và ", "của ", "có ", "là ", "được ", "trong ", "cho ", "với ", "này ", "đó ",
            "ă", "â", "đ", "ê", "ô", "ơ", "ư"
        ))
        
        // Find the language with the highest score
        val scores = mapOf(
            "es" to spanishScore,
            "fr" to frenchScore,
            "de" to germanScore,
            "it" to italianScore,
            "pt" to portugueseScore,
            "nl" to dutchScore,
            "pl" to polishScore,
            "tr" to turkishScore,
            "sv" to swedishScore,
            "no" to norwegianScore,
            "da" to danishScore,
            "fi" to finnishScore,
            "cs" to czechScore,
            "vi" to vietnameseScore
        )
        
        val maxScore = scores.maxByOrNull { it.value }
        
        // If no clear winner, default to English
        return if (maxScore != null && maxScore.value > 0) {
            maxScore.key
        } else {
            "en"
        }
    }
    
    /**
     * Count matches of patterns in text
     */
    private fun countMatches(text: String, patterns: List<String>): Int {
        return patterns.sumOf { pattern ->
            var count = 0
            var index = 0
            while (text.indexOf(pattern, index).also { index = it } != -1) {
                count++
                index += pattern.length
            }
            count
        }
    }
}
