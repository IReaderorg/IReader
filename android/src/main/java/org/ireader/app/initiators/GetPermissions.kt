package org.ireader.app.initiators

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import ireader.domain.preferences.prefs.UiPreferences
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GetPermissions(uiPreferences: UiPreferences, context: Context) {
    var showRationale by remember { mutableStateOf(false) }
    var showPermissionExplanation by remember { mutableStateOf(false) }
    var permissionRequested by remember { mutableStateOf(false) }
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
        if (!useLocalCache && !permissionRequested) {
            // Show explanation dialog first
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    showPermissionExplanation = true
                }
            } else if (!permissionsState.allPermissionsGranted) {
                showPermissionExplanation = true
            }
        }
    }
    
    // Permission explanation dialog
    if (showPermissionExplanation) {
        StoragePermissionExplanationDialog(
            onConfirm = {
                showPermissionExplanation = false
                permissionRequested = true
                
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
            },
            onDismiss = {
                showPermissionExplanation = false
                permissionRequested = true
                // Use local cache if user denies
                uiPreferences.savedLocalCatalogLocation().set(true)
            }
        )
    }
    
    // Show dialog if permissions can't be requested automatically
    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(localize(Res.string.permission_required)) },
            text = { Text(localize(Res.string.storage_permission_required_explanation)) },
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
                    Text(localize(Res.string.settings))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRationale = false }
                ) {
                    Text(localize(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun StoragePermissionExplanationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Storage Permission Required",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "IReader needs access to your device storage to:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                
                PermissionReasonItem(
                    icon = Icons.Default.Download,
                    text = "Download and save books for offline reading"
                )
                
                PermissionReasonItem(
                    icon = Icons.Default.Extension,
                    text = "Install and manage source extensions"
                )
                
                PermissionReasonItem(
                    icon = Icons.Default.Backup,
                    text = "Create and restore library backups"
                )
                
                PermissionReasonItem(
                    icon = Icons.Default.Image,
                    text = "Import EPUB files and custom book covers"
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your privacy is important. IReader only accesses files it creates and files you explicitly choose to import.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Not Now")
            }
        }
    )
}

@Composable
private fun PermissionReasonItem(
    icon: ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}
