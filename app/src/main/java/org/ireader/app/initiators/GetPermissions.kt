package org.ireader.app.initiators

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.usecases.files.GetSimpleStorage

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GetPermissions(context: Context, getSimpleStorage: GetSimpleStorage, uiPreferences: UiPreferences) {

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
    LaunchedEffect(key1 = true) {
        if (!useLocalCache) {
            readStoragePermission.launchPermissionRequest()
            writeStoragePermission.launchPermissionRequest()
        }
    }
}
