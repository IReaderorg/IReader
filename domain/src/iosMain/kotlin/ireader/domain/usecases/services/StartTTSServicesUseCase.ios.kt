package ireader.domain.usecases.services

/**
 * iOS implementation of StartTTSServicesUseCase
 * 
 * TODO: Full implementation using AVSpeechSynthesizer
 */
actual class StartTTSServicesUseCase {
    actual operator fun invoke(
        command: Int,
        bookId: Long?,
        chapterId: Long?
    ) {
        // TODO: Implement using AVSpeechSynthesizer
    }
}
