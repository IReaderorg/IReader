package ireader.presentation.ui.settings.backups

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.usecases.backup.lnreader.ImportLNReaderBackup
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.ChoicePreference
import ireader.presentation.ui.component.components.Components
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
    
    // State for showing file pickers
    var showLNReaderPicker by remember { mutableStateOf(false) }
    var showBackupSaver by remember { mutableStateOf(false) }
    var showRestorePicker by remember { mutableStateOf(false) }
    
    // Backup/Restore progress
    val backupRestoreProgress by vm.backupRestoreProgress.collectAsState()
    
    // LNReader import progress
    val lnReaderProgress by vm.lnReaderImportProgress.collectAsState()
    val lnReaderResult by vm.lnReaderImportResult.collectAsState()
    
    // Progress dialog
    BackupProgressDialog(
        progress = backupRestoreProgress,
        onDismiss = { vm.dismissProgress() }
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
    
    val items = androidx.compose.runtime.remember(importProgressText, localizeHelper) {
        listOf<Components>(
            Components.Row(
                localizeHelper.localize(Res.string.create_backup), onClick = {
                    // Show platform-specific file saver
                    showBackupSaver = true
                }
            ),
            Components.Row(
                localizeHelper.localize(Res.string.restore), onClick = {
                    // Show platform-specific file picker
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
                        // Show platform-specific file picker
                        showLNReaderPicker = true
                    }
                }
            ),
            Components.Header("Cloud Backup"),
            Components.Row(
                title = localizeHelper.localize(Res.string.cloud_storage),
                subtitle = localizeHelper.localize(Res.string.backup_to_google_drive_or_dropbox),
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
            Components.Dynamic {
                ChoicePreference<Int>(
                    preference = vm.maxAutomaticFiles,
                    choices = mapOf(
                        1 to "1",
                        2 to "2",
                        3 to "3",
                        4 to "4",
                        5 to "5",
                    ),
                    title = localizeHelper.localize(
                        Res.string.maximum_backups
                    ),
                    enable = vm.automaticBackup.value != PreferenceValues.AutomaticBackup.Off
                )
            }

        )
    }
    SetupSettingComponents(items = items, scaffoldPadding = scaffoldPadding)
}
