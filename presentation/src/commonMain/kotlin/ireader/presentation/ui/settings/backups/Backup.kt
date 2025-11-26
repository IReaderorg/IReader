package ireader.presentation.ui.settings.backups

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.ChoicePreference
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.core.theme.LocalGlobalCoroutineScope
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
    val globalScope = requireNotNull(LocalGlobalCoroutineScope.current) { "LocalGlobalCoroutineScope not provided" }
    val onShowRestore = remember { mutableStateOf(false) }
    val onShowBackup = remember { mutableStateOf(false) }
    OnShowRestore(onShowRestore.value, onFileSelected = {
        it?.let {files ->
            globalScope.launchIO {
                vm.restoreBackup.restoreFrom(it, onError = {
                    vm.showSnackBar(it)
                }, onSuccess = {
                    vm.showSnackBar((UiText.MStringResource(Res.string.restoredSuccessfully)))
                })
            }
        }
    })
    OnShowBackup(onShowBackup.value, onFileSelected = {
        it?.let {files ->
            globalScope.launchIO {
                vm.createBackup.saveTo(it, onError = {
                    vm.showSnackBar(it)
                }, onSuccess = {
                    vm.showSnackBar((UiText.MStringResource(Res.string.backup_created_successfully)))
                }, currentEvent = {
                    vm.showSnackBar(UiText.DynamicString(it))
                })
            }
        }
    })


//    val onRestore =
//        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
//            if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
//                val uri = resultIntent.data!!.data!!
//                globalScope.launchIO {
//                    vm.restoreBackup.restoreFrom(Uri(uri), onError = {
//                        vm.showSnackBar(it)
//                    }, onSuccess = {
//                        vm.showSnackBar((UiText.MStringResource(Res.string.restoredSuccessfully)))
//                    })
//                }
//            }
//        }
//    val onBackup =
//        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
//            if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
//                val uri = resultIntent.data!!.data!!
//                Uri(uri)
//                globalScope.launchIO {
//                    val result = vm.createBackup.saveTo(Uri(uri), onError = {
//                        vm.showSnackBar(it)
//                    }, onSuccess = {
//                        vm.showSnackBar((UiText.MStringResource(Res.string.backup_created_successfully)))
//                    })
//                }
//            }
//        }
    val items = remember {
        listOf<Components>(
            Components.Row(
                localizeHelper.localize(Res.string.create_backup), onClick = {
                    onShowBackup.value = true

                }
            ),
            Components.Row(
                localizeHelper.localize(Res.string.restore), onClick = {
                    onShowRestore.value = true

                }
            ),
            Components.Header("Cloud Backup"),
            Components.Row(
                title = "Cloud Storage",
                subtitle = "Backup to Google Drive or Dropbox",
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
