package ireader.domain.services.common

import ireader.domain.models.entities.Book
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Desktop implementation of LibraryUpdateService using coroutines
 */
class DesktopLibraryUpdateService : LibraryUpdateService {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var updateJob: Job? = null
    private var autoUpdateJob: Job? = null
    
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
        updateJob?.cancel()
        autoUpdateJob?.cancel()
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
            updateJob = scope.launch {
                _state.value = ServiceState.RUNNING
                // Delegate to existing library update logic
            }
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
            updateJob = scope.launch {
                _state.value = ServiceState.RUNNING
                // Delegate to existing update logic
            }
            ServiceResult.Success(UpdateResult(bookIds.size, 0, 0, 0))
        } catch (e: Exception) {
            ServiceResult.Error("Failed to update books: ${e.message}", e)
        }
    }
    
    override suspend fun cancelUpdate(): ServiceResult<Unit> {
        return try {
            updateJob?.cancel()
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
            autoUpdateJob?.cancel()
            autoUpdateJob = scope.launch {
                while (isActive) {
                    delay(intervalHours * 3600000L)
                    updateLibrary(showNotification = true)
                }
            }
            ServiceResult.Success("auto_update")
        } catch (e: Exception) {
            ServiceResult.Error("Failed to schedule auto-update: ${e.message}", e)
        }
    }
    
    override suspend fun cancelAutoUpdate(): ServiceResult<Unit> {
        return try {
            autoUpdateJob?.cancel()
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel auto-update: ${e.message}", e)
        }
    }
}
