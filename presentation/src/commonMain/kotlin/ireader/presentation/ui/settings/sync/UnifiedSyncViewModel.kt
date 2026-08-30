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
    val googleDriveEmail: String? = null,
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
            val driveEmail = if (isDriveAuth) "Google Account" else null


            val user = remoteRepository?.getCurrentUser()?.getOrNull()
            val isSupaAuth = user != null
            val supaEmail = user?.email

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

