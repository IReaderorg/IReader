package ireader.domain.usecases.services


expect class StartDownloadServicesUseCase {
    operator fun invoke(
        bookIds: LongArray? = null,
        chapterIds: LongArray? = null,
        downloadModes: Boolean = false,
    )
}
