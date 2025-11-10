package ireader.domain.services.tts_service

import ireader.domain.catalogs.VoiceCatalog
import ireader.domain.models.tts.VoiceModel
import ireader.domain.models.tts.VoiceQuality

/**
 * Service for recommending appropriate voices based on text and preferences
 * Requirements: 7.2, 7.4, 7.5
 */
class VoiceRecommender(
    private val languageDetector: LanguageDetector = LanguageDetector()
) {
    
    /**
     * Recommend voices for a given text
     * @param text Text to analyze
     * @param preferredGender Preferred voice gender (null for any)
     * @param minQuality Minimum voice quality
     * @return List of recommended voices, sorted by relevance
     */
    fun recommendVoices(
        text: String,
        preferredGender: String? = null,
        minQuality: VoiceQuality = VoiceQuality.MEDIUM
    ): List<VoiceModel> {
        val detectedLanguage = languageDetector.detectLanguage(text)
        return recommendVoicesForLanguage(detectedLanguage, preferredGender, minQuality)
    }
    
    /**
     * Recommend voices for a specific language
     * @param language ISO 639-1 language code
     * @param preferredGender Preferred voice gender (null for any)
     * @param minQuality Minimum voice quality
     * @return List of recommended voices, sorted by quality
     */
    fun recommendVoicesForLanguage(
        language: String,
        preferredGender: String? = null,
        minQuality: VoiceQuality = VoiceQuality.MEDIUM
    ): List<VoiceModel> {
        val allVoices = VoiceCatalog.getVoicesByLanguage(language)
        
        return allVoices
            .filter { voice ->
                // Filter by minimum quality
                voice.quality.ordinal >= minQuality.ordinal
            }
            .filter { voice ->
                // Filter by preferred gender if specified
                preferredGender == null || voice.gender.name.equals(preferredGender, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<VoiceModel> { it.quality.ordinal }
                    .thenBy { it.name }
            )
    }
    
    /**
     * Get the best voice for a language
     * @param language ISO 639-1 language code
     * @return Best available voice for the language, or null if none available
     */
    fun getBestVoiceForLanguage(language: String): VoiceModel? {
        return VoiceCatalog.getVoicesByLanguage(language)
            .maxByOrNull { it.quality.ordinal }
    }
    
    /**
     * Get fallback voice when no voice is available for a language
     * @return Default English voice as fallback
     */
    fun getFallbackVoice(): VoiceModel? {
        return VoiceCatalog.getVoicesByLanguage("en")
            .firstOrNull { it.quality == VoiceQuality.HIGH }
            ?: VoiceCatalog.getVoicesByLanguage("en").firstOrNull()
    }
    
    /**
     * Detect language and recommend best voice
     * @param text Text to analyze
     * @return Recommended voice or fallback
     */
    fun detectAndRecommend(text: String): VoiceModel {
        val language = languageDetector.detectLanguage(text)
        return getBestVoiceForLanguage(language) ?: getFallbackVoice()
            ?: throw IllegalStateException("No voices available in catalog")
    }
    
    /**
     * Check if multilingual text requires voice switching
     * @param text Text to analyze
     * @return Map of language codes to text segments if multilingual, empty if single language
     */
    fun detectMultilingualSegments(text: String): Map<String, List<String>> {
        // Split text into sentences
        val sentences = text.split(Regex("[.!?]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        if (sentences.isEmpty()) return emptyMap()
        
        // Detect language for each sentence
        val languageSegments = mutableMapOf<String, MutableList<String>>()
        
        sentences.forEach { sentence ->
            val language = languageDetector.detectLanguage(sentence)
            languageSegments.getOrPut(language) { mutableListOf() }.add(sentence)
        }
        
        // Only return if truly multilingual (more than one language)
        return if (languageSegments.size > 1) {
            languageSegments
        } else {
            emptyMap()
        }
    }
    
    /**
     * Get voice recommendations with installed status
     * @param text Text to analyze
     * @param installedVoiceIds Set of installed voice IDs
     * @return Pair of (installed voices, available voices)
     */
    fun getRecommendationsWithStatus(
        text: String,
        installedVoiceIds: Set<String>
    ): Pair<List<VoiceModel>, List<VoiceModel>> {
        val recommendations = recommendVoices(text)
        
        val installed = recommendations.filter { it.id in installedVoiceIds }
        val available = recommendations.filter { it.id !in installedVoiceIds }
        
        return Pair(installed, available)
    }
}
