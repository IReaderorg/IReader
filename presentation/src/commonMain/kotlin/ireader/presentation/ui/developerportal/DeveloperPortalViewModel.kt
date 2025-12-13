package ireader.presentation.ui.developerportal

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import ireader.domain.plugins.DeveloperPortalRepository
import ireader.domain.plugins.PluginAccessGrant
import ireader.domain.usecases.remote.GetCurrentUserUseCase
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for Developer Portal
 * Allows plugin developers to manage their plugins and grant access to users
 */
class DeveloperPortalViewModel(
    private val repository: DeveloperPortalRepository,
    private val getCurrentUser: GetCurrentUserUseCase
) : BaseViewModel() {

    private val _state = mutableStateOf(DeveloperPortalState())
    val state: State<DeveloperPortalState> = _state

    private var currentUserId: String? = null

    init {
        checkDeveloperStatus()
    }

    private fun checkDeveloperStatus() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val user = getCurrentUser().getOrNull()
                currentUserId = user?.id

                if (user == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isDeveloper = false,
                        error = "Please sign in to access Developer Portal"
                    )
                    return@launch
                }

                val isDev = repository.isDeveloper(user.id)
                _state.value = _state.value.copy(
                    isDeveloper = isDev,
                    isLoading = false,
                    error = if (!isDev) "Developer badge required to access this feature" else null
                )

                if (isDev) {
                    loadDeveloperPlugins()
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to verify developer status: ${e.message}"
                )
            }
        }
    }

    private fun loadDeveloperPlugins() {
        val userId = currentUserId ?: return
        scope.launch {
            repository.getDeveloperPlugins(userId).collectLatest { plugins ->
                _state.value = _state.value.copy(plugins = plugins)
            }
        }
    }

    fun selectPlugin(plugin: ireader.domain.plugins.DeveloperPlugin) {
        _state.value = _state.value.copy(selectedPlugin = plugin)
        loadPluginDetails(plugin.id)
    }

    private fun loadPluginDetails(pluginId: String) {
        scope.launch {
            // Load grants
            repository.getPluginGrants(pluginId).collectLatest { grants ->
                _state.value = _state.value.copy(pluginGrants = grants)
            }
        }

        scope.launch {
            // Load stats
            try {
                val stats = repository.getPluginStats(pluginId)
                val remaining = repository.getRemainingGrants(pluginId)
                _state.value = _state.value.copy(
                    pluginStats = stats,
                    remainingGrants = remaining
                )
            } catch (e: Exception) {
                // Stats are optional, don't show error
            }
        }
    }

    fun showGrantDialog() {
        _state.value = _state.value.copy(
            showGrantDialog = true,
            grantUsername = "",
            grantReason = "",
            grantError = null,
            grantSuccess = false
        )
    }

    fun hideGrantDialog() {
        _state.value = _state.value.copy(
            showGrantDialog = false,
            grantUsername = "",
            grantReason = "",
            grantError = null
        )
    }

    fun updateGrantUsername(username: String) {
        _state.value = _state.value.copy(grantUsername = username)
    }

    fun updateGrantReason(reason: String) {
        _state.value = _state.value.copy(grantReason = reason)
    }

    fun grantAccess() {
        val plugin = _state.value.selectedPlugin ?: return
        val userId = currentUserId ?: return
        val username = _state.value.grantUsername.trim()
        val reason = _state.value.grantReason.trim()

        if (username.isBlank()) {
            _state.value = _state.value.copy(grantError = "Username is required")
            return
        }

        if (reason.isBlank()) {
            _state.value = _state.value.copy(grantError = "Reason is required")
            return
        }

        if (_state.value.remainingGrants <= 0) {
            _state.value = _state.value.copy(grantError = "No remaining grant slots")
            return
        }

        scope.launch {
            _state.value = _state.value.copy(isGranting = true, grantError = null)

            try {
                val grant = PluginAccessGrant(
                    pluginId = plugin.id,
                    grantedToUserId = "", // Will be resolved by backend
                    grantedToUsername = username,
                    grantedByUserId = userId,
                    grantedByUsername = "", // Will be resolved by backend
                    grantDate = currentTimeToLong(),
                    reason = reason
                )

                repository.grantAccess(grant)
                    .onSuccess {
                        _state.value = _state.value.copy(
                            isGranting = false,
                            grantSuccess = true,
                            showGrantDialog = false,
                            remainingGrants = _state.value.remainingGrants - 1
                        )
                        // Reload grants
                        loadPluginDetails(plugin.id)
                    }
                    .onFailure { e ->
                        _state.value = _state.value.copy(
                            isGranting = false,
                            grantError = e.message ?: "Failed to grant access"
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isGranting = false,
                    grantError = e.message ?: "Failed to grant access"
                )
            }
        }
    }

    fun revokeAccess(grantId: String) {
        val plugin = _state.value.selectedPlugin ?: return

        scope.launch {
            try {
                repository.revokeAccess(grantId)
                    .onSuccess {
                        _state.value = _state.value.copy(
                            remainingGrants = _state.value.remainingGrants + 1
                        )
                        loadPluginDetails(plugin.id)
                    }
                    .onFailure { e ->
                        _state.value = _state.value.copy(
                            error = "Failed to revoke access: ${e.message}"
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to revoke access: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearGrantSuccess() {
        _state.value = _state.value.copy(grantSuccess = false)
    }

    fun goBack() {
        _state.value = _state.value.copy(selectedPlugin = null, pluginGrants = emptyList())
    }
}
