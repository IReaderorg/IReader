package ireader.domain.usecases.services


expect class StartLibraryUpdateServicesUseCase {
    operator fun invoke(
        forceUpdate:Boolean = false
    )
}
