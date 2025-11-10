package ireader.domain.models.tts

import kotlin.time.Duration

/**
 * Audio output format and data
 * Requirements: 4.1, 4.2
 */
data class AudioData(
    val samples: ByteArray,              // Raw PCM audio data
    val sampleRate: Int,                 // Samples per second
    val channels: Int = 1,               // Mono audio
    val bitsPerSample: Int = 16,         // 16-bit PCM
    val duration: Duration               // Total duration
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AudioData

        if (!samples.contentEquals(other.samples)) return false
        if (sampleRate != other.sampleRate) return false
        if (channels != other.channels) return false
        if (bitsPerSample != other.bitsPerSample) return false
        if (duration != other.duration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = samples.contentHashCode()
        result = 31 * result + sampleRate
        result = 31 * result + channels
        result = 31 * result + bitsPerSample
        result = 31 * result + duration.hashCode()
        return result
    }
}
