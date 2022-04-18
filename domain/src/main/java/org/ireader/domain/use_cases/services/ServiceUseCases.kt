package org.ireader.domain.use_cases.services

import androidx.annotation.Keep
import javax.inject.Inject

@Keep
data class ServiceUseCases @Inject constructor(
    val startDownloadServicesUseCase: StartDownloadServicesUseCase,
    val startLibraryUpdateServicesUseCase: StartLibraryUpdateServicesUseCase,
    val startTTSServicesUseCase: StartTTSServicesUseCase,
    val stopServicesUseCase: StopServiceUseCase,
)









