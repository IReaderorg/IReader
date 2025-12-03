package ireader.domain.services.common

/**
 * iOS implementation of ServiceFactory
 * 
 * TODO: Implement using iOS background tasks
 */
actual object ServiceFactory {
    actual fun createBackgroundTaskService(): BackgroundTaskService {
        return IosBackgroundTaskService()
    }
    
    actual fun createDownloadService(): DownloadService {
        return IosDownloadService()
    }
    
    actual fun createLibraryUpdateService(): LibraryUpdateService {
        return IosLibraryUpdateService()
    }
}

private class IosBackgroundTaskService : BackgroundTaskService {
    override fun schedule(taskId: String, delayMillis: Long) {
        // TODO: Implement using BGTaskScheduler
    }
    
    override fun cancel(taskId: String) {
        // TODO: Cancel scheduled task
    }
    
    override fun cancelAll() {
        // TODO: Cancel all tasks
    }
}

private class IosDownloadService : DownloadService {
    override fun startDownload(bookId: Long, chapterIds: List<Long>) {
        // TODO: Implement using URLSession background download
    }
    
    override fun stopDownload() {
        // TODO: Cancel downloads
    }
    
    override fun isRunning(): Boolean = false
}

private class IosLibraryUpdateService : LibraryUpdateService {
    override fun startUpdate(forceUpdate: Boolean) {
        // TODO: Implement using BGTaskScheduler
    }
    
    override fun stopUpdate() {
        // TODO: Cancel update
    }
    
    override fun isRunning(): Boolean = false
}
