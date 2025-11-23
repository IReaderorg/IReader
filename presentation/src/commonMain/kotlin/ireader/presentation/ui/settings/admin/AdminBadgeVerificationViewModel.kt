package ireader.presentation.ui.settings.admin

import androidx.compose.runtime.Immutable
import ireader.domain.models.remote.PaymentProof
import ireader.domain.usecases.admin.GetPendingPaymentProofsUseCase
import ireader.domain.usecases.admin.VerifyPaymentProofUseCase
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class AdminBadgeVerificationState(
    val isLoading: Boolean = false,
    val pendingProofs: List<PaymentProof> = emptyList(),
    val error: String? = null,
    val processingProofId: String? = null,
    val verificationSuccess: Boolean = false,
    val verificationError: String? = null
)

class AdminBadgeVerificationViewModel(
    private val getPendingPaymentProofsUseCase: GetPendingPaymentProofsUseCase,
    private val verifyPaymentProofUseCase: VerifyPaymentProofUseCase,
    private val getCurrentUser: suspend () -> ireader.domain.models.remote.User?
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(AdminBadgeVerificationState())
    val state: StateFlow<AdminBadgeVerificationState> = _state.asStateFlow()
    
    private var currentUserId: String? = null
    
    init {
        scope.launch {
            currentUserId = getCurrentUser()?.id
            loadPendingProofs()
        }
    }
    
    fun loadPendingProofs() {
        scope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            getPendingPaymentProofsUseCase()
                .onSuccess { proofs ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            pendingProofs = proofs,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load pending proofs"
                        )
                    }
                }
        }
    }
    
    fun verifyPaymentProof(proofId: String, approve: Boolean) {
        scope.launch {
            val userId = currentUserId
            if (userId == null) {
                _state.update {
                    it.copy(
                        processingProofId = null,
                        verificationError = "User not authenticated"
                    )
                }
                return@launch
            }
            
            _state.update { it.copy(processingProofId = proofId) }
            
            verifyPaymentProofUseCase(proofId, approve, userId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            processingProofId = null,
                            verificationSuccess = true,
                            // Remove the verified proof from the list
                            pendingProofs = it.pendingProofs.filter { proof -> proof.id != proofId }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            processingProofId = null,
                            verificationError = error.message ?: "Failed to verify payment proof"
                        )
                    }
                }
        }
    }
    
    fun clearVerificationStatus() {
        _state.update {
            it.copy(
                verificationSuccess = false,
                verificationError = null
            )
        }
    }
}
