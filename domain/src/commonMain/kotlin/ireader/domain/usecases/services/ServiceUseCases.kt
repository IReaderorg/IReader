package ireader.domain.usecases.services

data class ServiceUseCases(
    val startDownloadServicesUseCase: StartDownloadServicesUseCase,
    val startLibraryUpdateServicesUseCase: StartLibraryUpdateServicesUseCase,
    val startTTSServicesUseCase: StartTTSServicesUseCase,
    val stopServicesUseCase: StopServiceUseCase,
)