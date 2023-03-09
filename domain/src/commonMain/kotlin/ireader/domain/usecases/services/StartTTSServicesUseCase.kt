package ireader.domain.usecases.services


expect class StartTTSServicesUseCase {
    operator fun invoke(
        command: Int,
        bookId: Long? = null,
        chapterId: Long? = null,
    )
}
