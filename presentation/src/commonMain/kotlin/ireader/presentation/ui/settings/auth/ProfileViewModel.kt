package ireader.presentation.ui.settings.auth

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import ireader.domain.models.remote.ConnectionStatus
import ireader.domain.models.remote.User
import ireader.domain.usecases.remote.RemoteBackendUseCases
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val remoteUseCases: RemoteBackendUseCases?
) : StateScreenModel<ProfileState>(ProfileState()) {
    
    init {
        loadCurrentUser()
        observeConnectionStatus()
    }
    
    private fun loadCurrentUser() {
        screenModelScope.launch {
            mutableState.update { it.copy(isLoading = true) }
            
            remoteUseCases?.getCurrentUser?.invoke()?.fold(
                onSuccess = { user ->
                    mutableState.update { it.copy(currentUser = user, isLoading = false) }
                },
                onFailure = { error ->
                    mutableState.update { it.copy(error = error.message, isLoading = false) }
                }
            ) ?: run {
                mutableState.update { it.copy(isLoading = false) }
            }
        }
    }
    
    private fun observeConnectionStatus() {
        remoteUseCases?.observeConnectionStatus?.invoke()?.onEach { status ->
            mutableState.update { it.copy(connectionStatus = status) }
        }?.launchIn(screenModelScope)
    }
    
    fun signOut() {
        screenModelScope.launch {
            remoteUseCases?.signOut?.invoke()
            mutableState.update { 
                it.copy(
                    currentUser = null,
                    lastSyncTime = null
                ) 
            }
        }
    }
    
    fun showUsernameDialog() {
        mutableState.update { it.copy(showUsernameDialog = true) }
    }
    
    fun hideUsernameDialog() {
        mutableState.update { it.copy(showUsernameDialog = false) }
    }
    
    fun showWalletDialog() {
        mutableState.update { it.copy(showWalletDialog = true) }
    }
    
    fun hideWalletDialog() {
        mutableState.update { it.copy(showWalletDialog = false) }
    }
    
    fun updateUsername(username: String) {
        screenModelScope.launch {
            mutableState.update { it.copy(isLoading = true, showUsernameDialog = false) }
            
            val userId = state.value.currentUser?.id
            if (userId == null) {
                mutableState.update { it.copy(isLoading = false, error = "User not found") }
                return@launch
            }
            
            remoteUseCases?.updateUsername?.invoke(userId, username)?.fold(
                onSuccess = {
                    loadCurrentUser()
                },
                onFailure = { error ->
                    mutableState.update { 
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
        screenModelScope.launch {
            mutableState.update { it.copy(isLoading = true, showWalletDialog = false) }
            
            val userId = state.value.currentUser?.id
            if (userId == null) {
                mutableState.update { it.copy(isLoading = false, error = "User not found") }
                return@launch
            }
            
            remoteUseCases?.updateEthWalletAddress?.invoke(userId, wallet)?.fold(
                onSuccess = {
                    loadCurrentUser()
                },
                onFailure = { error ->
                    mutableState.update { 
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
        mutableState.update { it.copy(error = null) }
    }
}

data class ProfileState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val lastSyncTime: Long? = null,
    val showUsernameDialog: Boolean = false,
    val showWalletDialog: Boolean = false
)
