package ireader.presentation.ui.settings.sync

import androidx.compose.runtime.Stable
import ireader.data.backup.GoogleDriveAuthenticator
import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.sync.SyncProviderType
import ireader.domain.models.sync.UnifiedSyncState
import ireader.domain.preferences.prefs.SupabasePreferences
import ireader.domain.preferences.prefs.SyncPreferences
import ireader.domain.services.sync.UnifiedSyncEngine
import ireader.presentation.ui.core.viewmodel.StateViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Stable
data class UnifiedSyncScreenState(
    val selectedProvider: SyncProviderType = SyncProviderType.NONE,
    val syncState: UnifiedSyncState = UnifiedSyncState(),
    val isGoogleDriveConnected: Boolean = false,
    val isGoogleDriveConnecting: Boolean = false,
    val googleDriveEmail: String? = null,
    val googleDriveError: String? = null,
    val showGoogleDriveCredentialsDialog: Boolean = false,
    val customClientId: String = ireader.data.backup.GoogleDriveConfig.clientId ?: "",
    val customClientSecret: String = ireader.data.backup.GoogleDriveConfig.clientSecret ?: "",
    val isSupabaseConnected: Boolean = false,
    val supabaseEmail: String? = null,
    val autoSyncOnLaunch: Boolean = true,
    val autoSyncOnChapterFinish: Boolean = true,
    val syncOnWifiOnly: Boolean = false,
    val lastSyncTimestamp: Long = 0L,
    val showCustomSupabaseDialog: Boolean = false
)

class UnifiedSyncViewModel(
    private val unifiedSyncEngine: UnifiedSyncEngine,
    private val syncPreferences: SyncPreferences,
    private val supabasePreferences: SupabasePreferences,
    private val googleDriveAuthenticator: GoogleDriveAuthenticator? = null,
    private val remoteRepository: RemoteRepository? = null
) : StateViewModel<UnifiedSyncScreenState>(UnifiedSyncScreenState()) {

    init {
        loadSettings()
        observeSyncState()
    }

    private fun loadSettings() {
        val selectedProvider = syncPreferences.getSelectedProviderType()
        val autoOnLaunch = syncPreferences.autoSyncOnLaunch().get()
        val autoOnChapter = syncPreferences.autoSyncOnChapterFinish().get()
        val onWifiOnly = syncPreferences.syncOnWifiOnly().get()
        val lastTimestamp = syncPreferences.lastSyncTimestamp().get()

        updateState {
            it.copy(
                selectedProvider = selectedProvider,
                autoSyncOnLaunch = autoOnLaunch,
                autoSyncOnChapterFinish = autoOnChapter,
                syncOnWifiOnly = onWifiOnly,
                lastSyncTimestamp = lastTimestamp
            )
        }

        checkAuthStatuses()
    }

    private fun observeSyncState() {
        unifiedSyncEngine.syncState
            .onEach { state ->
                updateState {
                    it.copy(
                        syncState = state,
                        lastSyncTimestamp = if (state.lastSyncTimestamp > 0) state.lastSyncTimestamp else it.lastSyncTimestamp
                    )
                }
            }
            .launchIn(scope)
    }

    fun checkAuthStatuses() {
        scope.launch {
            val isDriveAuth = googleDriveAuthenticator?.isAuthenticated() ?: false
            val driveEmail = if (isDriveAuth) {
                googleDriveAuthenticator.getUserEmail() ?: "Google Account"
            } else null

            val isPersonalConfigured = supabasePreferences.isPersonalSupabaseConfigured()
            val user = remoteRepository?.getCurrentUser()?.getOrNull()
            val isSupaAuth = isPersonalConfigured || user != null
            val supaEmail = when {
                isPersonalConfigured -> "Personal Database"
                user != null -> user.email
                else -> null
            }

            updateState {
                it.copy(
                    isGoogleDriveConnected = isDriveAuth,
                    googleDriveEmail = driveEmail,
                    isSupabaseConnected = isSupaAuth,
                    supabaseEmail = supaEmail
                )
            }
        }
    }

    fun connectGoogleDrive() {
        updateState { it.copy(isGoogleDriveConnecting = true, googleDriveError = null) }
        ireader.presentation.core.ui.GoogleDriveOAuthLauncher.launchOAuthFlow(
            onSuccess = { email ->
                updateState {
                    it.copy(
                        isGoogleDriveConnected = true,
                        isGoogleDriveConnecting = false,
                        googleDriveEmail = email,
                        googleDriveError = null,
                        selectedProvider = SyncProviderType.GOOGLE_DRIVE
                    )
                }
                unifiedSyncEngine.setProvider(SyncProviderType.GOOGLE_DRIVE)
            },
            onError = { error ->
                updateState {
                    it.copy(
                        isGoogleDriveConnecting = false,
                        googleDriveError = error
                    )
                }
            }
        )
    }

    fun disconnectGoogleDrive() {
        scope.launch {
            googleDriveAuthenticator?.disconnect()
            updateState {
                it.copy(
                    isGoogleDriveConnected = false,
                    googleDriveEmail = null,
                    selectedProvider = if (it.selectedProvider == SyncProviderType.GOOGLE_DRIVE) SyncProviderType.NONE else it.selectedProvider
                )
            }
            if (syncPreferences.getSelectedProviderType() == SyncProviderType.GOOGLE_DRIVE) {
                unifiedSyncEngine.setProvider(SyncProviderType.NONE)
            }
        }
    }

    fun toggleGoogleDriveCredentialsDialog(show: Boolean) {
        updateState { it.copy(showGoogleDriveCredentialsDialog = show) }
    }

    fun setGoogleDriveCredentials(clientId: String, clientSecret: String) {
        ireader.data.backup.GoogleDriveConfig.setCredentials(clientId, clientSecret)
        updateState {
            it.copy(
                customClientId = clientId,
                customClientSecret = clientSecret,
                showGoogleDriveCredentialsDialog = false
            )
        }
    }

    fun clearGoogleDriveError() {
        updateState { it.copy(googleDriveError = null) }
    }

    fun signOutSupabase() {
        scope.launch {
            try {
                remoteRepository?.signOut()
            } catch (_: Exception) {
            }
            supabasePreferences.userSupabaseUrl().set("")
            supabasePreferences.userSupabaseAnonKey().set("")
            updateState {
                it.copy(
                    isSupabaseConnected = false,
                    supabaseEmail = null,
                    selectedProvider = if (it.selectedProvider == SyncProviderType.SUPABASE) SyncProviderType.NONE else it.selectedProvider
                )
            }
            if (syncPreferences.getSelectedProviderType() == SyncProviderType.SUPABASE) {
                unifiedSyncEngine.setProvider(SyncProviderType.NONE)
            }
        }
    }

    fun setProvider(providerType: SyncProviderType) {
        unifiedSyncEngine.setProvider(providerType)
        updateState { it.copy(selectedProvider = providerType) }
    }

    fun syncNow() {
        scope.launch {
            unifiedSyncEngine.syncNow(force = true)
            loadSettings()
        }
    }

    fun cancelSync() {
        unifiedSyncEngine.cancelSync()
    }

    fun toggleAutoSyncOnLaunch(enabled: Boolean) {
        syncPreferences.autoSyncOnLaunch().set(enabled)
        updateState { it.copy(autoSyncOnLaunch = enabled) }
    }

    fun toggleAutoSyncOnChapterFinish(enabled: Boolean) {
        syncPreferences.autoSyncOnChapterFinish().set(enabled)
        updateState { it.copy(autoSyncOnChapterFinish = enabled) }
    }

    fun toggleSyncOnWifiOnly(enabled: Boolean) {
        syncPreferences.syncOnWifiOnly().set(enabled)
        updateState { it.copy(syncOnWifiOnly = enabled) }
    }

    fun setShowCustomSupabaseDialog(show: Boolean) {
        updateState { it.copy(showCustomSupabaseDialog = show) }
    }
}

