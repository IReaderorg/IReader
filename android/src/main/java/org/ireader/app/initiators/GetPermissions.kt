package org.ireader.app.initiators

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import ireader.domain.preferences.prefs.UiPreferences

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GetPermissions(uiPreferences: UiPreferences) {

    val useLocalCache = remember {
        uiPreferences.savedLocalCatalogLocation().get()
    }
    val readStoragePermission = rememberPermissionState(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
    ) {
        uiPreferences.savedLocalCatalogLocation().set(!it)
    }
    val writeStoragePermission = rememberPermissionState(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    ) {
        uiPreferences.savedLocalCatalogLocation().set(!it)
    }
    val allFileAccessPermission = rememberPermissionState(
        android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
    ) {
        uiPreferences.savedLocalCatalogLocation().set(!it)
    }
    LaunchedEffect(key1 = true) {
        if (!useLocalCache) {
            readStoragePermission.launchPermissionRequest()
            writeStoragePermission.launchPermissionRequest()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                allFileAccessPermission.launchPermissionRequest()
            }
        }
    }
}
