package ireader.domain.models.tts

import kotlinx.serialization.Serializable

/**
 * Configuration for voice synthesis parameters
 * Requirements: 4.1, 4.2
 */
@Serializable
data class SynthesisConfig(
    val speechRate: Float = 1.0f,        // 0.5 - 2.0
    val noiseScale: Float = 0.667f,      // Quality vs speed
    val noiseW: Float = 0.8f,            // Variation in speech
    val lengthScale: Float = 1.0f,       // Phoneme duration
    val sentenceSilence: Float = 0.2f    // Pause between sentences
) {
    init {
        require(speechRate in 0.5f..2.0f) { 
            "Speech rate must be between 0.5 and 2.0, got $speechRate" 
        }
        require(noiseScale in 0.0f..1.0f) { 
            "Noise scale must be between 0.0 and 1.0, got $noiseScale" 
        }
        require(noiseW in 0.0f..1.0f) { 
            "Noise W must be between 0.0 and 1.0, got $noiseW" 
        }
        require(lengthScale > 0.0f) { 
            "Length scale must be positive, got $lengthScale" 
        }
        require(sentenceSilence >= 0.0f) { 
            "Sentence silence must be non-negative, got $sentenceSilence" 
        }
    }
}
