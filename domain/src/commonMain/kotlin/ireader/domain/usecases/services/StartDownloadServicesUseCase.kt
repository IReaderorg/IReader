package ireader.domain.usecases.services


expect class StartDownloadServicesUseCase {
    fun start(
        bookIds: LongArray? = null,
        chapterIds: LongArray? = null,
        downloadModes: Boolean = false,
    )
    fun stop()
}
