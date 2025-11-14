package ireader.presentation.ui.settings.auth


import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.ConnectionStatus
import ireader.domain.models.remote.User
import ireader.domain.usecases.remote.RemoteBackendUseCases
import ireader.domain.data.repository.BadgeRepository
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val remoteUseCases: RemoteBackendUseCases?,
    private val badgeRepository: BadgeRepository?
) : StateViewModel<ProfileState>(ProfileState()) {
    
    init {
        loadCurrentUser()
        observeConnectionStatus()
        loadFeaturedBadges()
    }
    
    private fun loadCurrentUser() {
        scope.launch {
            updateState { it.copy(isLoading = true) }
            
            remoteUseCases?.getCurrentUser?.invoke()?.fold(
                onSuccess = { user ->
                    updateState { it.copy(currentUser = user, isLoading = false) }
                },
                onFailure = { error ->
                    updateState { it.copy(error = error.message, isLoading = false) }
                }
            ) ?: run {
                updateState { it.copy(isLoading = false) }
            }
        }
    }
    
    private fun observeConnectionStatus() {
        remoteUseCases?.observeConnectionStatus?.invoke()?.onEach { status ->
            updateState { it.copy(connectionStatus = status) }
        }?.launchIn(scope)
    }
    
    fun signOut() {
        scope.launch {
            remoteUseCases?.signOut?.invoke()
            updateState { 
                it.copy(
                    currentUser = null,
                    lastSyncTime = null
                ) 
            }
        }
    }
    
    fun showUsernameDialog() {
        updateState { it.copy(showUsernameDialog = true) }
    }
    
    fun hideUsernameDialog() {
        updateState { it.copy(showUsernameDialog = false) }
    }
    
    fun showWalletDialog() {
        updateState { it.copy(showWalletDialog = true) }
    }
    
    fun hideWalletDialog() {
        updateState { it.copy(showWalletDialog = false) }
    }
    
    fun updateUsername(username: String) {
        scope.launch {
            updateState { it.copy(isLoading = true, showUsernameDialog = false) }
            
            val userId = currentState.currentUser?.id
            if (userId == null) {
                updateState { it.copy(isLoading = false, error = "User not found") }
                return@launch
            }
            
            remoteUseCases?.updateUsername?.invoke(userId, username)?.fold(
                onSuccess = {
                    loadCurrentUser()
                },
                onFailure = { error ->
                    updateState { 
                        it.copy(
                            error = error.message ?: "Failed to update username", 
                            isLoading = false
                        ) 
                    }
                }
            )
        }
    }
    
    fun updateWallet(wallet: String) {
        scope.launch {
            updateState { it.copy(isLoading = true, showWalletDialog = false) }
            
            val userId = currentState.currentUser?.id
            if (userId == null) {
                updateState { it.copy(isLoading = false, error = "User not found") }
                return@launch
            }
            
            remoteUseCases?.updateEthWalletAddress?.invoke(userId, wallet)?.fold(
                onSuccess = {
                    loadCurrentUser()
                },
                onFailure = { error ->
                    updateState { 
                        it.copy(
                            error = error.message ?: "Failed to update wallet", 
                            isLoading = false
                        ) 
                    }
                }
            )
        }
    }
    
    fun clearError() {
        updateState { it.copy(error = null) }
    }
    
    private fun loadFeaturedBadges() {
        scope.launch {
            updateState { it.copy(isBadgesLoading = true) }
            
            badgeRepository?.getFeaturedBadges()?.fold(
                onSuccess = { badges ->
                    updateState { it.copy(featuredBadges = badges, isBadgesLoading = false, badgesError = null) }
                },
                onFailure = { error ->
                    updateState { it.copy(badgesError = error.message, isBadgesLoading = false) }
                }
            ) ?: run {
                updateState { it.copy(isBadgesLoading = false) }
            }
        }
    }
    
    fun retryLoadBadges() {
        loadFeaturedBadges()
    }
}

data class ProfileState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val lastSyncTime: Long? = null,
    val showUsernameDialog: Boolean = false,
    val showWalletDialog: Boolean = false,
    val featuredBadges: List<Badge> = emptyList(),
    val isBadgesLoading: Boolean = false,
    val badgesError: String? = null
)
