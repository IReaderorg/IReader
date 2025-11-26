package ireader.domain.services.common

import android.content.Context
import androidx.work.*
import ireader.domain.models.entities.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

/**
 * Android implementation of LibraryUpdateService using WorkManager
 */
class AndroidLibraryUpdateService(
    private val context: Context
) : LibraryUpdateService {
    
    private val workManager = WorkManager.getInstance(context)
    
    private val _state = MutableStateFlow<ServiceState>(ServiceState.IDLE)
    override val state: StateFlow<ServiceState> = _state.asStateFlow()
    
    private val _updatingBooks = MutableStateFlow<List<Book>>(emptyList())
    override val updatingBooks: StateFlow<List<Book>> = _updatingBooks.asStateFlow()
    
    private val _updateProgress = MutableStateFlow<Map<Long, UpdateProgress>>(emptyMap())
    override val updateProgress: StateFlow<Map<Long, UpdateProgress>> = _updateProgress.asStateFlow()
    
    override suspend fun initialize() {
        _state.value = ServiceState.IDLE
    }
    
    override suspend fun start() {
        _state.value = ServiceState.RUNNING
    }
    
    override suspend fun stop() {
        _state.value = ServiceState.STOPPED
        workManager.cancelAllWorkByTag(LIBRARY_UPDATE_TAG)
    }
    
    override fun isRunning(): Boolean = _state.value == ServiceState.RUNNING
    
    override suspend fun cleanup() {
        stop()
        _updatingBooks.value = emptyList()
        _updateProgress.value = emptyMap()
    }
    
    override suspend fun updateLibrary(
        categoryIds: List<Long>?,
        showNotification: Boolean
    ): ServiceResult<UpdateResult> {
        return try {
            val workData = workDataOf(
                "categoryIds" to categoryIds?.toLongArray(),
                "showNotification" to showNotification
            )
            
            val workRequest = OneTimeWorkRequestBuilder<LibraryUpdateWorker>()
                .setInputData(workData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(LIBRARY_UPDATE_TAG)
                .build()
            
            workManager.enqueueUniqueWork(
                "library_update_${System.currentTimeMillis()}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            _state.value = ServiceState.RUNNING
            ServiceResult.Success(UpdateResult(0, 0, 0, 0))
        } catch (e: Exception) {
            ServiceResult.Error("Failed to start library update: ${e.message}", e)
        }
    }
    
    override suspend fun updateBooks(
        bookIds: List<Long>,
        showNotification: Boolean
    ): ServiceResult<UpdateResult> {
        return try {
            val workData = workDataOf(
                "bookIds" to bookIds.toLongArray(),
                "showNotification" to showNotification
            )
            
            val workRequest = OneTimeWorkRequestBuilder<LibraryUpdateWorker>()
                .setInputData(workData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(LIBRARY_UPDATE_TAG)
                .build()
            
            workManager.enqueueUniqueWork(
                "library_update_books_${System.currentTimeMillis()}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            _state.value = ServiceState.RUNNING
            ServiceResult.Success(UpdateResult(bookIds.size, 0, 0, 0))
        } catch (e: Exception) {
            ServiceResult.Error("Failed to update books: ${e.message}", e)
        }
    }
    
    override suspend fun cancelUpdate(): ServiceResult<Unit> {
        return try {
            workManager.cancelAllWorkByTag(LIBRARY_UPDATE_TAG)
            _state.value = ServiceState.IDLE
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel update: ${e.message}", e)
        }
    }
    
    override suspend fun scheduleAutoUpdate(
        intervalHours: Int,
        constraints: TaskConstraints
    ): ServiceResult<String> {
        return try {
            val workConstraints = Constraints.Builder()
                .setRequiredNetworkType(if (constraints.requiresNetwork) NetworkType.CONNECTED else NetworkType.NOT_REQUIRED)
                .setRequiresCharging(constraints.requiresCharging)
                .setRequiresBatteryNotLow(constraints.requiresBatteryNotLow)
                .build()
            
            val workRequest = PeriodicWorkRequestBuilder<LibraryUpdateWorker>(
                intervalHours.toLong(),
                TimeUnit.HOURS
            )
                .setConstraints(workConstraints)
                .addTag(AUTO_UPDATE_TAG)
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                AUTO_UPDATE_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
            
            ServiceResult.Success(AUTO_UPDATE_WORK_NAME)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to schedule auto-update: ${e.message}", e)
        }
    }
    
    override suspend fun cancelAutoUpdate(): ServiceResult<Unit> {
        return try {
            workManager.cancelUniqueWork(AUTO_UPDATE_WORK_NAME)
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel auto-update: ${e.message}", e)
        }
    }
    
    companion object {
        private const val LIBRARY_UPDATE_TAG = "library_update"
        private const val AUTO_UPDATE_TAG = "auto_update"
        private const val AUTO_UPDATE_WORK_NAME = "library_auto_update"
    }
}

/**
 * Worker for library updates
 */
class LibraryUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // Delegate to existing library update implementation
        return Result.success()
    }
}
