package org.ireader.app.initiators

import android.content.Context
import androidx.compose.runtime.Composable
import ireader.domain.preferences.prefs.UiPreferences

/**
 * Legacy permission handler - no longer needed.
 * 
 * Storage: Handled by SAF (Storage Access Framework) during onboarding.
 * Notifications: Requested when actually needed (e.g., starting a download).
 * 
 * This composable is kept as a no-op for backward compatibility.
 */
@Composable
fun GetPermissions(uiPreferences: UiPreferences, context: Context) {
    // No-op: Permissions are now handled contextually when needed
}
