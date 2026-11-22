package ireader.presentation.ui.settings.backups

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Tune
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.usecases.backup.lnreader.ImportLNReaderBackup
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.ChoicePreference
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackUpAndRestoreScreen(
    modifier: Modifier = Modifier,
    onBackStack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    vm: BackupScreenViewModel,
    scaffoldPadding: PaddingValues,
    onNavigateToCloudBackup: () -> Unit = {}
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    // State for showing file pickers and dialogs
    var showLNReaderPicker by remember { mutableStateOf(false) }
    var showBackupSaver by remember { mutableStateOf(false) }
    var showRestorePicker by remember { mutableStateOf(false) }
    var showBackupOptionsDialog by remember { mutableStateOf(false) }
    
    // Backup/Restore progress
    val backupRestoreProgress by vm.backupRestoreProgress.collectAsState()
    val backupOptions by vm.backupOptions.collectAsState()
    
    // LNReader import progress
    val lnReaderProgress by vm.lnReaderImportProgress.collectAsState()
    val lnReaderImportProgressDialog by vm.lnReaderImportProgressDialog.collectAsState()

    val backupDirectorySize by vm.backupDirectorySize
    LaunchedEffect(Unit) { vm.refreshBackupDirectorySize() }

    // Backup options dialog
    if (showBackupOptionsDialog) {
        BackupOptionsDialog(
            options = backupOptions,
            onOptionChanged = { transform -> vm.updateBackupOptions(transform) },
            onSelectAll = { vm.selectAllBackupOptions() },
            onDeselectAll = { vm.deselectAllBackupOptions() },
            onConfirm = {
                showBackupOptionsDialog = false
                showBackupSaver = true
            },
            onDismiss = { showBackupOptionsDialog = false }
        )
    }

    // Progress dialog
    BackupProgressDialog(
        progress = backupRestoreProgress,
        onDismiss = { vm.dismissProgress() }
    )

    // LNReader import progress dialog
    LNReaderImportProgressDialog(
        progress = lnReaderImportProgressDialog,
        onDismiss = { vm.dismissLNReaderImportDialog() }
    )
    
    // Platform-specific file picker for LNReader backup
    OnPickLNReaderBackup(
        show = showLNReaderPicker,
        onFileSelected = { uri ->
            showLNReaderPicker = false
            if (uri != null) {
                vm.importLNReaderBackupFromUri(uri)
            }
        }
    )
    
    // Platform-specific file picker for creating backup
    OnSaveBackupFile(
        show = showBackupSaver,
        defaultFileName = "IReader_backup_${ireader.domain.utils.extensions.currentTimeToLong()}",
        onLocationSelected = { uri ->
            showBackupSaver = false
            if (uri != null) {
                vm.createBackupToUri(uri)
            }
        }
    )
    
    // Platform-specific file picker for restoring backup
    OnPickBackupFile(
        show = showRestorePicker,
        onFileSelected = { uri ->
            showRestorePicker = false
            if (uri != null) {
                vm.restoreBackupFromUri(uri)
            }
        }
    )
    
    // Get import progress text
    val importProgressText = when (val progress = lnReaderProgress) {
        is ImportLNReaderBackup.ImportProgress.Starting -> localizeHelper.localize(Res.string.lnreader_import_starting)
        is ImportLNReaderBackup.ImportProgress.Parsing -> progress.message
        is ImportLNReaderBackup.ImportProgress.ImportingNovels -> 
            "Importing novels: ${progress.current}/${progress.total} - ${progress.novelName}"
        is ImportLNReaderBackup.ImportProgress.ImportingCategories -> 
            "Importing categories: ${progress.current}/${progress.total}"
        is ImportLNReaderBackup.ImportProgress.Complete -> null
        is ImportLNReaderBackup.ImportProgress.Error -> null
        null -> null
    }
    
    val items = androidx.compose.runtime.remember(importProgressText, localizeHelper, backupDirectorySize) {
        listOf<Components>(
            Components.Header("Full Backup & Restore"),
            Components.Row(
                title = localizeHelper.localize(Res.string.create_backup),
                subtitle = "Complete backup: books, chapters, history, categories, tracks, themes, and settings",
                onClick = {
                    vm.selectAllBackupOptions()
                    showBackupSaver = true
                }
            ),
            Components.Row(
                title = "Customize Backup Options",
                subtitle = "Select specific data to include in backup",
                icon = Icons.Filled.Tune,
                onClick = {
                    showBackupOptionsDialog = true
                }
            ),
            Components.Row(
                title = localizeHelper.localize(Res.string.restore),
                subtitle = "Restore all data, history, and settings from backup file",
                onClick = {
                    showRestorePicker = true
                }
            ),
            Components.Header(localizeHelper.localize(Res.string.import_from_other_apps)),
            Components.Row(
                title = localizeHelper.localize(Res.string.import_from_lnreader),
                subtitle = if (importProgressText != null) importProgressText else localizeHelper.localize(Res.string.import_from_lnreader_desc),
                icon = Icons.Filled.FileDownload,
                onClick = {
                    if (importProgressText == null) {
                        showLNReaderPicker = true
                    }
                }
            ),
            Components.Header("Cloud Backup"),
            Components.Row(
                title = localizeHelper.localize(Res.string.cloud_storage),
                subtitle = localizeHelper.localize(Res.string.backup_and_restore_your_library_to_google_drive),
                icon = Icons.Filled.Cloud,
                onClick = {
                    onNavigateToCloudBackup()
                }
            ),
            Components.Header(localizeHelper.localize(Res.string.automatic_backup)),
            Components.Dynamic {
                ChoicePreference<PreferenceValues.AutomaticBackup>(
                    preference = vm.automaticBackup,
                    choices = mapOf(
                        PreferenceValues.AutomaticBackup.Off to localizeHelper.localize(Res.string.off),
                        PreferenceValues.AutomaticBackup.Every6Hours to "Every 6 hours",
                        PreferenceValues.AutomaticBackup.Every12Hours to "Every 12 hours",
                        PreferenceValues.AutomaticBackup.Daily to localizeHelper.localize(Res.string.daily),
                        PreferenceValues.AutomaticBackup.Every2Days to "Every 2 days",
                        PreferenceValues.AutomaticBackup.Weekly to localizeHelper.localize(Res.string.weekly),
                    ),
                    title = localizeHelper.localize(
                        Res.string.automatic_backup
                    ),
                    onValue = { newValue ->
                        vm.storageManager.hasStoragePermission()
                        vm.updateAutomaticBackupFrequency(newValue)
                    }
                )
            },
            Components.Header(localizeHelper.localize(Res.string.backup_retention)),
            Components.Dynamic {
                NumberPreference(
                    title = localizeHelper.localize(Res.string.maximum_backups),
                    subtitle = if (vm.maxAutomaticFiles.value <= 0) {
                        localizeHelper.localize(Res.string.backup_retention_unlimited)
                    } else null,
                    value = vm.maxAutomaticFiles.value,
                    onValueChange = { vm.maxAutomaticFiles.value = it },
                    min = 0,
                    max = 1000,
                )
            },
            Components.Dynamic {
                NumberPreference(
                    title = localizeHelper.localize(Res.string.max_backup_age_days),
                    subtitle = if (vm.maxAutomaticAgeDays.value <= 0) {
                        localizeHelper.localize(Res.string.backup_retention_unlimited)
                    } else null,
                    value = vm.maxAutomaticAgeDays.value,
                    onValueChange = { vm.maxAutomaticAgeDays.value = it },
                    min = 0,
                    max = 3650,
                )
            },
            Components.Dynamic {
                val limitText = if (vm.maxAutomaticSizeMb.value <= 0) {
                    localizeHelper.localize(Res.string.backup_retention_unlimited)
                } else "${vm.maxAutomaticSizeMb.value} MB"
                NumberPreference(
                    title = localizeHelper.localize(Res.string.max_backup_size_mb),
                    subtitle = "${formatBackupSize(backupDirectorySize)} / $limitText",
                    value = vm.maxAutomaticSizeMb.value,
                    onValueChange = {
                        vm.maxAutomaticSizeMb.value = it
                        vm.refreshBackupDirectorySize()
                    },
                    min = 0,
                    max = 1_000_000,
                )
            },
        )
    }
    SetupSettingComponents(items = items, scaffoldPadding = scaffoldPadding)
}

private fun formatBackupSize(bytes: Long): String = when {
    bytes <= 0L -> "0 MB"
    bytes < 1024L * 1024L -> "${bytes / 1024L} KB"
    bytes < 1024L * 1024L * 1024L -> "${bytes / (1024L * 1024L)} MB"
    else -> "${bytes / (1024L * 1024L * 1024L)} GB"
}

/** A preference row that opens a numeric input dialog. `0` means unlimited. */
@Composable
private fun NumberPreference(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    min: Int = 0,
    max: Int = Int.MAX_VALUE,
) {
    var showDialog by remember { mutableStateOf(false) }
    var input by remember(value, showDialog) { mutableStateOf(value.toString()) }

    PreferenceRow(
        title = title,
        subtitle = subtitle,
        action = { Text(value.toString(), modifier = Modifier.padding(end = 16.dp)) },
        enable = enabled,
        onClick = { if (enabled) showDialog = true },
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter(Char::isDigit) },
                    label = { Text(title) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = modifier,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    input.toIntOrNull()?.coerceIn(min, max)?.let(onValueChange)
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            },
        )
    }
}
