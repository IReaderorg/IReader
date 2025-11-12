package ireader.presentation.ui.settings.web3

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import ireader.domain.models.remote.ConnectionStatus
import ireader.domain.models.remote.User
import ireader.domain.services.WalletIntegrationManager
import ireader.domain.usecases.remote.RemoteBackendUseCases
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Web3 Profile screen
 */
class Web3ProfileViewModel(
    private val remoteUseCases: RemoteBackendUseCases?,
    private val walletManager: WalletIntegrationManager?
) : StateScreenModel<Web3ProfileState>(Web3ProfileState()) {
    
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
                mutableState.update { it.copy(isLoading = false, error = "Web3 backend not configured") }
            }
        }
    }
    
    private fun observeConnectionStatus() {
        remoteUseCases?.observeConnectionStatus?.invoke()?.onEach { status ->
            mutableState.update { it.copy(connectionStatus = status) }
        }?.launchIn(screenModelScope)
    }
    
    fun showWalletSelection() {
        mutableState.update { it.copy(showWalletSelection = true) }
    }
    
    fun hideWalletSelection() {
        mutableState.update { it.copy(showWalletSelection = false) }
    }
    
    fun connectWallet(selectedWallet: ireader.domain.models.donation.WalletApp? = null) {
        screenModelScope.launch {
            mutableState.update { it.copy(isLoading = true, error = null, showWalletSelection = false) }
            
            try {
                // Check if use cases are available
                if (remoteUseCases?.authenticateWithWallet == null) {
                    mutableState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Web3 backend not configured. Please add Supabase credentials."
                        ) 
                    }
                    return@launch
                }
                
                // Check if wallet manager is available
                if (walletManager == null) {
                    mutableState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "Wallet integration not available"
                        ) 
                    }
                    return@launch
                }
                
                // Get the wallet address from the wallet manager
                // On Desktop, this retrieves the address from the stored key pair
                // On Android with real wallets, this would come from WalletConnect
                val walletAddress = walletManager.getWalletAddress()
                
                if (walletAddress == null) {
                    mutableState.update { 
                        it.copy(
                            isLoading = false, 
                            error = "No wallet address available. Please generate or connect a wallet first."
                        ) 
                    }
                    return@launch
                }
                
                println("🔵 Connecting wallet: $walletAddress")
                println("🔵 Selected wallet: ${selectedWallet?.name ?: "None"}")
                
                // The AuthenticateWithWalletUseCase handles signature generation internally
                println("🔵 Calling authenticateWithWallet use case...")
                val result = remoteUseCases.authenticateWithWallet.invoke(walletAddress)
                
                println("🔵 Authentication result received")
                
                result?.fold(
                    onSuccess = { user ->
                        println("✅ Authentication successful! User: ${user.walletAddress}")
                        mutableState.update { 
                            it.copy(
                                currentUser = user, 
                                isLoading = false,
                                lastSyncTime = System.currentTimeMillis()
                            ) 
                        }
                    },
                    onFailure = { error ->
                        println("❌ Authentication failed: ${error.message}")
                        error.printStackTrace()
                        mutableState.update { 
                            it.copy(
                                error = error.message ?: "Authentication failed", 
                                isLoading = false
                            ) 
                        }
                    }
                ) ?: run {
                    println("❌ Authentication result was null")
                    mutableState.update {
                        it.copy(
                            error = "Authentication returned no result",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                mutableState.update { 
                    it.copy(
                        error = e.message ?: "Unknown error occurred", 
                        isLoading = false
                    ) 
                }
            }
        }
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
    
    fun updateUsername(username: String) {
        screenModelScope.launch {
            mutableState.update { it.copy(isLoading = true, showUsernameDialog = false) }
            
            remoteUseCases?.updateUsername?.invoke(username)?.fold(
                onSuccess = {
                    // Reload user to get updated data
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
    
    fun clearError() {
        mutableState.update { it.copy(error = null) }
    }
}

data class Web3ProfileState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val lastSyncTime: Long? = null,
    val showUsernameDialog: Boolean = false,
    val showWalletSelection: Boolean = false
)
