package org.ireader.domain.use_cases.services

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class ServiceUseCases @Inject constructor(
    val startDownloadServicesUseCase: StartDownloadServicesUseCase,
    val startLibraryUpdateServicesUseCase: StartLibraryUpdateServicesUseCase,
    val startTTSServicesUseCase: StartTTSServicesUseCase,
    val stopServicesUseCase: StopServiceUseCase,
)
