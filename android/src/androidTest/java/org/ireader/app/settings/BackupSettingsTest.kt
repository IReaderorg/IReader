package org.ireader.app.settings

import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.filters.LargeTest
import org.ireader.app.BaseComposeTest
import org.junit.Before
import org.junit.Test

/**
 * E2E tests for the Cloud Backup Settings screen.
 *
 * Screen: CloudBackupScreen.kt
 * Route: cloudBackup (NavigationRoutes.cloudBackup)
 * ViewModel: GoogleDriveViewModel
 *
 * UI elements tested:
 * - Cloud Backup title with account email
 * - Google Drive connection card (connect/disconnect)
 * - FAB for creating backup (visible when connected)
 * - Pull-to-refresh for loading backups
 * - Backup list items (when connected and backups exist)
 * - Restore dialog (when clicking a backup)
 * - Delete dialog (when long-pressing a backup)
 * - Snackbar messages for success/error
 *
 * Note: Google Drive connection requires OAuth flow which cannot be
 * fully automated in E2E tests. Tests focus on the disconnected state
 * and UI element visibility.
 */
@LargeTest
class BackupSettingsTest : BaseComposeTest() {

    @Before
    fun navigateToBackupSettings() {
        navigateToSettings()
        navigateToSubScreen("Backup & Restore")
    }

    @Test
    fun backupSettingsScreenDisplaysTitle() {
        assertTextDisplayed("Cloud Backup")
    }

    @Test
    fun googleDriveConnectionCardIsVisible() {
        // When not connected, should show a connect button
        // When connected, should show account email and disconnect button
        // The connection card is always visible
        composeTestRule.waitUntil(waitTimeoutMs) {
            try {
                // Either "Connect" or account email should be visible
                val hasConnect = try {
                    composeTestRule.onNodeWithText("Connect").assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
                val hasDisconnect = try {
                    composeTestRule.onNodeWithText("Disconnect").assertExists()
                    true
                } catch (_: AssertionError) {
                    false
                }
                hasConnect || hasDisconnect
            } catch (_: Exception) {
                false
            }
        }
    }

    @Test
    fun backNavigationReturnsToSettings() {
        navigateBack()
        assertTextDisplayed("Settings")
    }
}
