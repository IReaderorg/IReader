package ireader.presentation.ui.settings.admin

import androidx.compose.runtime.Immutable
import ireader.domain.models.remote.AdminUser
import ireader.domain.models.remote.Badge
import ireader.domain.usecases.admin.AdminUserUseCases
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class AdminUserPanelState(
    val isLoading: Boolean = false,
    val users: List<AdminUser> = emptyList(),
    val availableBadges: List<Badge> = emptyList(),
    val selectedUser: AdminUser? = null,
    val selectedUserBadges: List<Badge> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val successMessage: String? = null,
    val showAssignBadgeDialog: Boolean = false,
    val showResetPasswordDialog: Boolean = false,
    val isProcessing: Boolean = false,
    val currentPage: Int = 0,
    val hasMoreUsers: Boolean = true
)

class AdminUserPanelViewModel(
    private val adminUserUseCases: AdminUserUseCases
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(AdminUserPanelState())
    val state: StateFlow<AdminUserPanelState> = _state.asStateFlow()
    
    private val pageSize = 50
    
    init {
        loadUsers()
        loadAvailableBadges()
    }
    
    fun loadUsers(refresh: Boolean = false) {
        scope.launch {
            if (refresh) {
                _state.update { it.copy(currentPage = 0, users = emptyList()) }
            }
            
            _state.update { it.copy(isLoading = true, error = null) }
            
            val offset = if (refresh) 0 else _state.value.currentPage * pageSize
            
            adminUserUseCases.getAllUsers(
                limit = pageSize,
                offset = offset,
                searchQuery = _state.value.searchQuery.takeIf { it.isNotBlank() }
            ).onSuccess { newUsers ->
                _state.update { state ->
                    val allUsers = if (refresh) newUsers else state.users + newUsers
                    state.copy(
                        isLoading = false,
                        users = allUsers,
                        hasMoreUsers = newUsers.size == pageSize,
                        currentPage = if (refresh) 0 else state.currentPage
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load users"
                    )
                }
            }
        }
    }
    
    fun loadMoreUsers() {
        if (_state.value.isLoading || !_state.value.hasMoreUsers) return
        
        _state.update { it.copy(currentPage = it.currentPage + 1) }
        loadUsers()
    }
    
    fun searchUsers(query: String) {
        _state.update { it.copy(searchQuery = query) }
        loadUsers(refresh = true)
    }
    
    private fun loadAvailableBadges() {
        scope.launch {
            adminUserUseCases.getAvailableBadges()
                .onSuccess { badges ->
                    _state.update { it.copy(availableBadges = badges) }
                }
        }
    }
    
    fun selectUser(user: AdminUser) {
        _state.update { it.copy(selectedUser = user, selectedUserBadges = emptyList()) }
        loadUserBadges(user.id)
    }
    
    fun clearSelectedUser() {
        _state.update { it.copy(selectedUser = null, selectedUserBadges = emptyList()) }
    }
    
    private fun loadUserBadges(userId: String) {
        scope.launch {
            adminUserUseCases.getUserBadges(userId)
                .onSuccess { badges ->
                    _state.update { it.copy(selectedUserBadges = badges) }
                }
        }
    }
    
    fun showAssignBadgeDialog() {
        _state.update { it.copy(showAssignBadgeDialog = true) }
    }
    
    fun hideAssignBadgeDialog() {
        _state.update { it.copy(showAssignBadgeDialog = false) }
    }
    
    fun assignBadge(badgeId: String) {
        val userId = _state.value.selectedUser?.id ?: return
        
        scope.launch {
            _state.update { it.copy(isProcessing = true, showAssignBadgeDialog = false) }
            
            adminUserUseCases.assignBadgeToUser(userId, badgeId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            successMessage = "Badge assigned successfully"
                        )
                    }
                    loadUserBadges(userId)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Failed to assign badge"
                        )
                    }
                }
        }
    }
    
    fun removeBadge(badgeId: String) {
        val userId = _state.value.selectedUser?.id ?: return
        
        scope.launch {
            _state.update { it.copy(isProcessing = true) }
            
            adminUserUseCases.removeBadgeFromUser(userId, badgeId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            successMessage = "Badge removed successfully"
                        )
                    }
                    loadUserBadges(userId)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Failed to remove badge"
                        )
                    }
                }
        }
    }
    
    fun showResetPasswordDialog() {
        _state.update { it.copy(showResetPasswordDialog = true) }
    }
    
    fun hideResetPasswordDialog() {
        _state.update { it.copy(showResetPasswordDialog = false) }
    }
    
    fun sendPasswordReset() {
        val email = _state.value.selectedUser?.email ?: return
        
        scope.launch {
            _state.update { it.copy(isProcessing = true, showResetPasswordDialog = false) }
            
            adminUserUseCases.sendPasswordReset(email)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            successMessage = "Password reset email sent to $email"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            error = error.message ?: "Failed to send password reset"
                        )
                    }
                }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun clearSuccessMessage() {
        _state.update { it.copy(successMessage = null) }
    }
}
