package ireader.presentation.ui.settings.badges.store

import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.PaymentProof
import ireader.domain.usecases.badge.GetAvailableBadgesUseCase
import ireader.domain.usecases.badge.SubmitPaymentProofUseCase
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.ui.settings.badges.BadgeErrorMapper
import ireader.presentation.ui.settings.badges.RetryConfig
import ireader.presentation.ui.settings.badges.retryWithExponentialBackoff
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BadgeStoreState(
    val badges: List<Badge> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedBadge: Badge? = null,
    val showPurchaseDialog: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val submitError: String? = null
)

class BadgeStoreViewModel(
    private val getAvailableBadgesUseCase: GetAvailableBadgesUseCase,
    private val submitPaymentProofUseCase: SubmitPaymentProofUseCase
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(BadgeStoreState())
    val state: StateFlow<BadgeStoreState> = _state.asStateFlow()
    
    init {
        loadBadges()
    }
    
    fun loadBadges() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            val result = retryWithExponentialBackoff(RetryConfig.BADGE_FETCH) {
                getAvailableBadgesUseCase()
            }
            
            result
                .onSuccess { badges ->
                    _state.update { 
                        it.copy(
                            badges = badges,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = BadgeErrorMapper.toUserMessage(exception)
                        )
                    }
                }
        }
    }
    
    fun onBadgeClick(badge: Badge) {
        _state.update { 
            it.copy(
                selectedBadge = badge,
                showPurchaseDialog = true,
                submitSuccess = false,
                submitError = null
            )
        }
    }
    
    fun onDismissPurchaseDialog() {
        _state.update { 
            it.copy(
                selectedBadge = null,
                showPurchaseDialog = false,
                submitSuccess = false,
                submitError = null
            )
        }
    }
    
    fun onSubmitPaymentProof(proof: PaymentProof) {
        val badgeId = _state.value.selectedBadge?.id ?: return
        
        scope.launch {
            _state.update { it.copy(isSubmitting = true, submitError = null) }
            
            val result = retryWithExponentialBackoff(RetryConfig.PAYMENT_SUBMISSION) {
                submitPaymentProofUseCase(badgeId, proof)
            }
            
            result
                .onSuccess {
                    _state.update { 
                        it.copy(
                            isSubmitting = false,
                            submitSuccess = true,
                            submitError = null,
                            showPurchaseDialog = false,
                            selectedBadge = null
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update { 
                        it.copy(
                            isSubmitting = false,
                            submitError = BadgeErrorMapper.toUserMessage(exception)
                        )
                    }
                }
        }
    }
    
    fun clearSubmitStatus() {
        _state.update { 
            it.copy(
                submitSuccess = false,
                submitError = null
            )
        }
    }
}
