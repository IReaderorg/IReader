package ireader.presentation.ui.characterart

import androidx.compose.runtime.Stable
import ireader.domain.data.repository.CharacterArtRepository
import ireader.domain.models.characterart.*
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * State for Character Art Gallery screen
 */
@Stable
data class CharacterArtScreenState(
    val artList: List<CharacterArt> = emptyList(),
    val featuredArt: List<CharacterArt> = emptyList(),
    val userSubmissions: List<CharacterArt> = emptyList(),
    val pendingArt: List<CharacterArt> = emptyList(),
    val selectedArt: CharacterArt? = null,
    val selectedFilter: ArtStyleFilter = ArtStyleFilter.ALL,
    val selectedSort: CharacterArtSort = CharacterArtSort.NEWEST,
    val viewMode: GalleryViewMode = GalleryViewMode.GRID,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val error: String? = null,
    val successMessage: String? = null,
    val isAdmin: Boolean = false,
    val hasMorePages: Boolean = true,
    val currentPage: Int = 0
)

/**
 * ViewModel for Character Art Gallery
 */
@Stable
class CharacterArtViewModel(
    private val repository: CharacterArtRepository,
    private val getCurrentUser: suspend () -> ireader.domain.models.remote.User?
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(CharacterArtScreenState())
    val state: StateFlow<CharacterArtScreenState> = _state.asStateFlow()
    
    private val pageSize = 30
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        scope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Check admin status
            val isAdmin = try {
                getCurrentUser()?.isAdmin == true
            } catch (e: Exception) {
                false
            }
            
            _state.update { it.copy(isAdmin = isAdmin) }
            
            // Load art
            loadArt(refresh = true)
            loadFeaturedArt()
            
            if (isAdmin) {
                loadPendingArt()
            }
        }
    }
    
    fun loadArt(refresh: Boolean = false) {
        scope.launch {
            val currentState = _state.value
            val offset = if (refresh) 0 else currentState.currentPage * pageSize
            
            if (refresh) {
                _state.update { it.copy(isLoading = true, currentPage = 0) }
            }
            
            repository.getApprovedArt(
                filter = currentState.selectedFilter,
                sort = currentState.selectedSort,
                searchQuery = currentState.searchQuery,
                limit = pageSize,
                offset = offset
            ).onSuccess { newArt ->
                _state.update { state ->
                    val updatedList = if (refresh) newArt else state.artList + newArt
                    state.copy(
                        artList = updatedList,
                        isLoading = false,
                        hasMorePages = newArt.size >= pageSize,
                        currentPage = if (refresh) 1 else state.currentPage + 1
                    )
                }
            }.onFailure { error ->
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to load art: ${error.message}"
                    )
                }
            }
        }
    }
    
    fun loadMoreArt() {
        if (_state.value.hasMorePages && !_state.value.isLoading) {
            loadArt(refresh = false)
        }
    }
    
    private fun loadFeaturedArt() {
        scope.launch {
            repository.getFeaturedArt(limit = 5)
                .onSuccess { featured ->
                    _state.update { it.copy(featuredArt = featured) }
                }
        }
    }
    
    private fun loadPendingArt() {
        scope.launch {
            repository.getPendingArt()
                .onSuccess { pending ->
                    _state.update { it.copy(pendingArt = pending) }
                }
        }
    }
    
    fun loadUserSubmissions() {
        scope.launch {
            repository.getUserSubmissions()
                .onSuccess { submissions ->
                    _state.update { it.copy(userSubmissions = submissions) }
                }
        }
    }
    
    fun setFilter(filter: ArtStyleFilter) {
        _state.update { it.copy(selectedFilter = filter) }
        loadArt(refresh = true)
    }
    
    fun setSort(sort: CharacterArtSort) {
        _state.update { it.copy(selectedSort = sort) }
        loadArt(refresh = true)
    }
    
    fun setViewMode(mode: GalleryViewMode) {
        _state.update { it.copy(viewMode = mode) }
    }
    
    fun setSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
        // Debounce search
        scope.launch {
            kotlinx.coroutines.delay(300)
            if (_state.value.searchQuery == query) {
                loadArt(refresh = true)
            }
        }
    }
    
    fun selectArt(art: CharacterArt?) {
        _state.update { it.copy(selectedArt = art) }
    }
    
    fun toggleLike(artId: String) {
        scope.launch {
            repository.toggleLike(artId)
                .onSuccess { isLiked ->
                    _state.update { state ->
                        val updatedList = state.artList.map { art ->
                            if (art.id == artId) {
                                art.copy(
                                    isLikedByUser = isLiked,
                                    likesCount = if (isLiked) art.likesCount + 1 else art.likesCount - 1
                                )
                            } else art
                        }
                        state.copy(artList = updatedList)
                    }
                }
        }
    }
    
    fun submitArt(
        request: SubmitCharacterArtRequest,
        imageBytes: ByteArray
    ) {
        scope.launch {
            _state.update { it.copy(isUploading = true, uploadProgress = 0f) }
            
            // Upload image first
            val fileName = "character_${currentTimeToLong()}.jpg"
            repository.uploadImage(imageBytes, fileName)
                .onSuccess { imageUrl ->
                    _state.update { it.copy(uploadProgress = 0.5f) }
                    
                    // Submit art with image URL
                    val requestWithImage = request.copy(imagePath = imageUrl)
                    repository.submitArt(requestWithImage)
                        .onSuccess { art ->
                            _state.update { state ->
                                state.copy(
                                    isUploading = false,
                                    uploadProgress = 1f,
                                    successMessage = "Art submitted for review! 🎨",
                                    userSubmissions = state.userSubmissions + art
                                )
                            }
                        }
                        .onFailure { error ->
                            _state.update {
                                it.copy(
                                    isUploading = false,
                                    error = "Failed to submit: ${error.message}"
                                )
                            }
                        }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isUploading = false,
                            error = "Failed to upload image: ${error.message}"
                        )
                    }
                }
        }
    }
    
    fun approveArt(artId: String, featured: Boolean = false) {
        scope.launch {
            repository.approveArt(artId, featured)
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            pendingArt = state.pendingArt.filter { it.id != artId },
                            successMessage = "Art approved! ✅"
                        )
                    }
                    loadArt(refresh = true)
                }
                .onFailure { error ->
                    _state.update { it.copy(error = "Failed to approve: ${error.message}") }
                }
        }
    }
    
    fun rejectArt(artId: String, reason: String = "") {
        scope.launch {
            repository.rejectArt(artId, reason)
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            pendingArt = state.pendingArt.filter { it.id != artId },
                            successMessage = "Art rejected"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = "Failed to reject: ${error.message}") }
                }
        }
    }
    
    fun deleteArt(artId: String) {
        scope.launch {
            repository.deleteArt(artId)
                .onSuccess {
                    _state.update { state ->
                        state.copy(
                            artList = state.artList.filter { it.id != artId },
                            userSubmissions = state.userSubmissions.filter { it.id != artId },
                            successMessage = "Art deleted"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = "Failed to delete: ${error.message}") }
                }
        }
    }
    
    fun reportArt(artId: String, reason: String) {
        scope.launch {
            repository.reportArt(artId, reason)
                .onSuccess {
                    _state.update { it.copy(successMessage = "Report submitted. Thank you!") }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = "Failed to report: ${error.message}") }
                }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }
    
    fun refresh() {
        loadArt(refresh = true)
        loadFeaturedArt()
        if (_state.value.isAdmin) {
            loadPendingArt()
        }
    }
}
