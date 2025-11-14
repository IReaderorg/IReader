package ireader.presentation.ui.settings.badges.nft

import androidx.lifecycle.viewModelScope
import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.BadgeType
import ireader.domain.models.remote.NFTWallet
import ireader.domain.usecases.badge.GetUserBadgesUseCase
import ireader.domain.usecases.nft.GetNFTMarketplaceUrlUseCase
import ireader.domain.usecases.nft.GetNFTVerificationStatusUseCase
import ireader.domain.usecases.nft.SaveWalletAddressUseCase
import ireader.domain.usecases.nft.VerifyNFTOwnershipUseCase
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.settings.badges.BadgeErrorMapper
import ireader.presentation.ui.settings.badges.RetryConfig
import ireader.presentation.ui.settings.badges.retryWithExponentialBackoff
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NFTBadgeState(
    val walletAddress: String? = null,
    val isVerifying: Boolean = false,
    val verificationStatus: NFTWallet? = null,
    val nftBadge: Badge? = null,
    val error: String? = null,
    val lastVerified: Long? = null,
    val cacheExpiresAt: Long? = null,
    val isLoading: Boolean = true
)

class NFTBadgeViewModel(
    private val saveWalletAddressUseCase: SaveWalletAddressUseCase,
    private val verifyNFTOwnershipUseCase: VerifyNFTOwnershipUseCase,
    private val getNFTVerificationStatusUseCase: GetNFTVerificationStatusUseCase,
    private val getUserBadgesUseCase: GetUserBadgesUseCase,
    private val getNFTMarketplaceUrlUseCase: GetNFTMarketplaceUrlUseCase
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(NFTBadgeState())
    val state: StateFlow<NFTBadgeState> = _state.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // Load cached verification status
            val statusResult = getNFTVerificationStatusUseCase()
            
            if (statusResult.isSuccess) {
                val wallet = statusResult.getOrNull()
                
                if (wallet != null) {
                    _state.update {
                        it.copy(
                            walletAddress = wallet.walletAddress,
                            verificationStatus = wallet,
                            lastVerified = wallet.lastVerified,
                            cacheExpiresAt = wallet.cacheExpiresAt,
                            isLoading = false
                        )
                    }
                    
                    // If user owns NFT, fetch the NFT badge
                    if (wallet.ownsNFT) {
                        fetchNFTBadge()
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = BadgeErrorMapper.toUserMessage(statusResult.exceptionOrNull() ?: Exception("Failed to load verification status"))
                    )
                }
            }
        }
    }
    
    private suspend fun fetchNFTBadge() {
        val badgesResult = getUserBadgesUseCase()
        
        if (badgesResult.isSuccess) {
            val badges = badgesResult.getOrNull() ?: emptyList()
            // Find the NFT badge - we need to get the full Badge object
            // For now, we'll create a placeholder since UserBadge doesn't have all Badge fields
            // In a real implementation, you'd fetch the full Badge from BadgeRepository
            val nftUserBadge = badges.firstOrNull { badge ->
                // Check if this is an NFT badge based on metadata or other indicators
                badge.badgeCategory == "NFT" || badge.badgeName.contains("NFT", ignoreCase = true)
            }
            
            if (nftUserBadge != null) {
                // Convert UserBadge to Badge (simplified - in real app, fetch from repository)
                val nftBadge = Badge(
                    id = nftUserBadge.badgeId,
                    name = nftUserBadge.badgeName,
                    description = nftUserBadge.badgeDescription,
                    icon = nftUserBadge.badgeIcon,
                    category = nftUserBadge.badgeCategory,
                    rarity = nftUserBadge.badgeRarity,
                    type = BadgeType.NFT_EXCLUSIVE,
                    imageUrl = nftUserBadge.badgeIcon
                )
                
                _state.update { it.copy(nftBadge = nftBadge) }
            }
        }
    }
    
    fun onSaveWalletAddress(address: String) {
        viewModelScope.launch {
            _state.update { it.copy(isVerifying = true, error = null) }
            
            // Validate and save wallet address
            val saveResult = saveWalletAddressUseCase(address)
            
            if (saveResult.isSuccess) {
                _state.update { it.copy(walletAddress = address) }
                
                // Trigger verification automatically
                onVerifyOwnership()
            } else {
                _state.update {
                    it.copy(
                        isVerifying = false,
                        error = BadgeErrorMapper.toUserMessage(saveResult.exceptionOrNull() ?: Exception("Failed to save wallet address"))
                    )
                }
            }
        }
    }
    
    fun onVerifyOwnership() {
        val address = _state.value.walletAddress
        
        if (address == null) {
            _state.update { it.copy(error = "Please enter a wallet address first") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isVerifying = true, error = null) }
            
            val verifyResult = retryWithExponentialBackoff(RetryConfig.NFT_VERIFICATION) {
                verifyNFTOwnershipUseCase(address)
            }
            
            if (verifyResult.isSuccess) {
                val result = verifyResult.getOrNull()
                
                if (result != null) {
                    val wallet = NFTWallet(
                        userId = "", // Will be set by backend
                        walletAddress = address,
                        lastVerified = result.verifiedAt,
                        ownsNFT = result.ownsNFT,
                        nftTokenId = result.tokenId,
                        cacheExpiresAt = result.cacheExpiresAt
                    )
                    
                    _state.update {
                        it.copy(
                            isVerifying = false,
                            verificationStatus = wallet,
                            lastVerified = result.verifiedAt,
                            cacheExpiresAt = result.cacheExpiresAt,
                            error = null
                        )
                    }
                    
                    // Fetch NFT badge if user owns NFT
                    if (result.ownsNFT) {
                        fetchNFTBadge()
                    }
                } else {
                    _state.update {
                        it.copy(
                            isVerifying = false,
                            error = "Verification completed but no result returned"
                        )
                    }
                }
            } else {
                _state.update {
                    it.copy(
                        isVerifying = false,
                        error = BadgeErrorMapper.toUserMessage(verifyResult.exceptionOrNull() ?: Exception("NFT verification failed"))
                    )
                }
            }
        }
    }
    
    fun onOpenMarketplace(): String {
        return getNFTMarketplaceUrlUseCase()
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
