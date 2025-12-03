package ireader.domain.usecases.services

/**
 * iOS implementation of StartDownloadServicesUseCase
 * 
 * TODO: Implement using iOS background tasks (BGTaskScheduler)
 */
actual class StartDownloadServicesUseCase {
    actual fun start(
        bookIds: LongArray?,
        chapterIds: LongArray?,
        downloadModes: Boolean
    ) {
        // TODO: Implement using BGTaskScheduler
    }
    
    actual fun stop() {
        // TODO: Cancel background tasks
    }
}
