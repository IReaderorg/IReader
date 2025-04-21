package org.ireader.app.initiators

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import ireader.domain.preferences.prefs.UiPreferences
import ireader.i18n.localize
import ireader.i18n.resources.MR
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GetPermissions(uiPreferences: UiPreferences, context: Context) {
    var showRationale by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val useLocalCache = remember {
        uiPreferences.savedLocalCatalogLocation().get()
    }
    
    // Permission state based on Android version
    val permissionsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13+ uses READ_MEDIA_* permissions
        rememberMultiplePermissionsState(
            listOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.READ_MEDIA_VIDEO,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        ) { results ->
            val allGranted = results.all { it.value }
            uiPreferences.savedLocalCatalogLocation().set(!allGranted)
        }
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Android 11-12 uses scoped storage
        rememberMultiplePermissionsState(
            listOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        ) { results ->
            val allGranted = results.all { it.value }
            uiPreferences.savedLocalCatalogLocation().set(!allGranted)
        }
    } else {
        // Android 10 and below use traditional storage permissions
        rememberMultiplePermissionsState(
            listOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        ) { results ->
            val allGranted = results.all { it.value }
            uiPreferences.savedLocalCatalogLocation().set(!allGranted)
        }
    }
    
    // Request ALL_FILES_ACCESS for Android 11+
    val allFilesAccessLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Check if permission was granted
        val hasAllFilesAccess = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            false
        }
        uiPreferences.savedLocalCatalogLocation().set(!hasAllFilesAccess)
    }
    
    LaunchedEffect(key1 = true) {
        if (!useLocalCache) {
            // Request permissions based on Android version
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // For Android 11+ request MANAGE_EXTERNAL_STORAGE
                if (!Environment.isExternalStorageManager()) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                            addCategory("android.intent.category.DEFAULT")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        allFilesAccessLauncher.launch(intent)
                    } catch (e: Exception) {
                        // Fallback if the specific intent doesn't work
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                            allFilesAccessLauncher.launch(intent)
                        } catch (e: Exception) {
                            showRationale = true
                        }
                    }
                }
            }
            
            // Request media permissions
            if (!permissionsState.allPermissionsGranted) {
                permissionsState.launchMultiplePermissionRequest()
            }
        }
    }
    
    // Show dialog if permissions can't be requested automatically
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(localize(MR.strings.permission_required)) },
            text = { Text(localize(MR.strings.storage_permission_required_explanation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                            showRationale = false
                        }
                    }
                ) {
                    Text(localize(MR.strings.settings))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRationale = false }
                ) {
                    Text(localize(MR.strings.cancel))
                }
            }
        )
    }
}
