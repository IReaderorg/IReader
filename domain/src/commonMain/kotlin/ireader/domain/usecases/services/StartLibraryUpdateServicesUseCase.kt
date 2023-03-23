package ireader.domain.usecases.services


expect class StartLibraryUpdateServicesUseCase {
    fun start(
        forceUpdate:Boolean = false
    )
    fun stop()
}
