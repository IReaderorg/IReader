package ireader.domain.usecases.services

/**
 * iOS implementation of StartTTSServicesUseCase
 * 
 * TODO: Implement using AVSpeechSynthesizer
 */
actual class StartTTSServicesUseCase {
    actual operator fun invoke(
        command: Int,
        bookId: Long,
        chapterId: Long,
        voiceId: String?
    ) {
        // TODO: Implement using AVSpeechSynthesizer
    }
}
