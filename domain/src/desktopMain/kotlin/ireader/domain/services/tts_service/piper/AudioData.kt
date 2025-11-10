package ireader.domain.services.tts_service.piper

/**
 * Represents audio data in PCM format
 * 
 * @property samples Raw PCM audio samples as byte array
 * @property sampleRate Sample rate in Hz (e.g., 22050, 44100)
 * @property channels Number of audio channels (1 for mono, 2 for stereo)
 * @property format PCM format specification
 */
data class AudioData(
    val samples: ByteArray,
    val sampleRate: Int,
    val channels: Int,
    val format: AudioFormat
) {
    /**
     * PCM audio format types
     */
    enum class AudioFormat {
        /** 16-bit PCM */
        PCM_16,
        /** 24-bit PCM */
        PCM_24,
        /** 32-bit PCM */
        PCM_32
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AudioData) return false

        if (!samples.contentEquals(other.samples)) return false
        if (sampleRate != other.sampleRate) return false
        if (channels != other.channels) return false
        if (format != other.format) return false

        return true
    }

    override fun hashCode(): Int {
        var result = samples.contentHashCode()
        result = 31 * result + sampleRate
        result = 31 * result + channels
        result = 31 * result + format.hashCode()
        return result
    }
}
