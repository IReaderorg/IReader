package ireader.presentation.ui.settings.backups

import ireader.data.backup.GoogleDriveConfig
import ireader.domain.models.sync.SyncProviderType
import ireader.presentation.ui.settings.sync.UnifiedSyncScreenState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoogleDriveSyncTest {

    @Test
    fun testGoogleDriveConfigSetCredentials() {
        GoogleDriveConfig.setCredentials("test-client-id.apps.googleusercontent.com", "test-client-secret")
        assertEquals("test-client-id.apps.googleusercontent.com", GoogleDriveConfig.clientId)
        assertEquals("test-client-secret", GoogleDriveConfig.clientSecret)
        assertTrue(GoogleDriveConfig.isInitialized())
    }

    @Test
    fun testGoogleDriveViewModelState() {
        GoogleDriveConfig.setCredentials("custom-id", "custom-secret")
        val state = GoogleDriveViewModel.State(
            customClientId = GoogleDriveConfig.clientId ?: "",
            customClientSecret = GoogleDriveConfig.clientSecret ?: ""
        )
        assertEquals("custom-id", state.customClientId)
        assertEquals("custom-secret", state.customClientSecret)
        assertFalse(state.isConnected)
        assertFalse(state.showCredentialsDialog)
    }

    @Test
    fun testUnifiedSyncScreenStateAuthIntegration() {
        GoogleDriveConfig.setCredentials("sync-client-id", "sync-client-secret")
        val state = UnifiedSyncScreenState(
            selectedProvider = SyncProviderType.GOOGLE_DRIVE,
            isGoogleDriveConnected = true,
            googleDriveEmail = "reader-user@gmail.com",
            customClientId = GoogleDriveConfig.clientId ?: "",
            customClientSecret = GoogleDriveConfig.clientSecret ?: ""
        )

        assertEquals(SyncProviderType.GOOGLE_DRIVE, state.selectedProvider)
        assertTrue(state.isGoogleDriveConnected)
        assertEquals("reader-user@gmail.com", state.googleDriveEmail)
        assertEquals("sync-client-id", state.customClientId)
        assertEquals("sync-client-secret", state.customClientSecret)
        assertFalse(state.isGoogleDriveConnecting)
        assertFalse(state.showGoogleDriveCredentialsDialog)
    }
}
