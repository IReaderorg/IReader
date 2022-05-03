package org.ireader.domain.use_cases.services


data class ServiceUseCases(
    val startDownloadServicesUseCase: StartDownloadServicesUseCase,
    val startLibraryUpdateServicesUseCase: StartLibraryUpdateServicesUseCase,
    val startTTSServicesUseCase: StartTTSServicesUseCase,
    val stopServicesUseCase: StopServiceUseCase,
)
