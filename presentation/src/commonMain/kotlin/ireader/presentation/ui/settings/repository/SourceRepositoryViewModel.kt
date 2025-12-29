package ireader.presentation.ui.settings.repository

import androidx.compose.runtime.mutableStateOf
import ireader.domain.catalogs.interactor.SyncRemoteCatalogs
import ireader.domain.data.repository.CatalogSourceRepository
import ireader.domain.models.entities.ExtensionSource
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

/**
 * ViewModel for Source Repository management (IReader/LNReader sources)
 * 
 * Features:
 * - Auto-fetches remote catalogs when repositories are added or toggled
 * - Debounces rapid toggle operations to prevent memory issues
 * - Cancels previous fetch requests when new ones are triggered
 */
class SourceRepositoryViewModel(
    val catalogSourceRepository: CatalogSourceRepository,
    val uiPreferences: UiPreferences,
    private val syncRemoteCatalogs: SyncRemoteCatalogs? = null
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {

    companion object {
        const val NAME = "?name="
        const val OWNER = "owner="
        const val SOURCE = "source="
        const val Separator = "?"
        
        /** Debounce delay for auto-fetch after repository changes (ms) */
        private const val AUTO_FETCH_DEBOUNCE_MS = 500L
    }

    val sources = catalogSourceRepository.subscribe().asState(emptyList())
    var showAutomaticSourceDialog = mutableStateOf(false)

    val default = uiPreferences.defaultRepository().asState()
    
    // Job for debounced auto-fetch - cancel previous before starting new
    @Volatile
    private var autoFetchJob: Job? = null

    /**
     * Add a new repository and auto-fetch catalogs
     */
    fun addRepository(extensionSource: ExtensionSource) {
        scope.launch {
            catalogSourceRepository.insert(extensionSource)
            // Auto-fetch catalogs for the newly added repository
            triggerDebouncedAutoFetch()
        }
    }
    
    /**
     * Toggle repository enabled state and auto-fetch catalogs
     */
    fun toggleRepository(source: ExtensionSource, enabled: Boolean) {
        scope.launch {
            catalogSourceRepository.update(source.copy(isEnable = enabled))
            // Auto-fetch catalogs when repository is toggled (debounced)
            triggerDebouncedAutoFetch()
        }
    }
    
    /**
     * Delete a repository
     */
    fun deleteRepository(source: ExtensionSource) {
        scope.launch {
            catalogSourceRepository.delete(source)
        }
    }
    
    /**
     * Triggers a debounced auto-fetch of remote catalogs.
     * 
     * Features:
     * - Cancels any previous pending fetch request (prevents memory issues from rapid toggling)
     * - Debounces by [AUTO_FETCH_DEBOUNCE_MS] to batch rapid changes
     * - Only fetches if syncRemoteCatalogs is available
     */
    private fun triggerDebouncedAutoFetch() {
        if (syncRemoteCatalogs == null) return
        
        // Cancel previous auto-fetch job if still pending
        autoFetchJob?.cancel()
        
        autoFetchJob = scope.launch {
            // Debounce: wait before fetching to batch rapid changes
            delay(AUTO_FETCH_DEBOUNCE_MS)
            
            // Check if job was cancelled (new request came in)
            if (!isActive) return@launch
            
            // Fetch remote catalogs
            syncRemoteCatalogs.await(forceRefresh = true)
        }
    }
    
    /**
     * Cancels any ongoing auto-fetch operation.
     */
    fun cancelAutoFetch() {
        autoFetchJob?.cancel()
        autoFetchJob = null
    }

    /**     https://raw.githubusercontent.com/IReaderorg/IReader-extensions/repov2/index.min.json?name=IReader 2?owner=KazemCodes?source=https://github.com/IReaderorg/IReader"
     *
     */
    fun parseUrl(url:String) : ExtensionSource {
        val key = url.substringBefore(NAME,"").takeIf { it.isNotBlank() } ?: throw Exception()
        val name = url.substringAfter(NAME,"")
            .substringBefore(Separator,"").takeIf { it.isNotBlank() } ?: throw Exception()
        val owner = url.substringAfter(OWNER,"")
            .substringBefore(Separator,"") .takeIf { it.isNotBlank() } ?: throw Exception()
        val source = url.substringAfter(SOURCE,"")
            .takeIf { it.isNotBlank() } ?: throw Exception()
        return ExtensionSource(0,name,key,owner,source,null,null,0,true,"IREADER")
    }
}
