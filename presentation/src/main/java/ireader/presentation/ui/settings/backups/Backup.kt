package ireader.presentation.ui.settings.backups

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.utils.extensions.findComponentActivity
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.presentation.R
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.component.components.component.ChoicePreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackUpAndRestoreScreen(
    modifier: Modifier = Modifier,
    onBackStack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    vm: BackupScreenViewModel,
    scaffoldPadding: PaddingValues
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val onRestore =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
            if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
                val uri = resultIntent.data!!.data!!
                context.findComponentActivity()?.lifecycleScope?.launchIO {
                    vm.restoreBackup.restoreFrom(uri, context, onError = {
                        vm.showSnackBar(it)
                    }, onSuccess = {
                        vm.showSnackBar((UiText.StringResource(R.string.restoredSuccessfully)))
                    })
                }
            }
        }
    val onBackup =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
            if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
                val uri = resultIntent.data!!.data!!

                context.findComponentActivity()?.lifecycleScope?.launchIO {
                    val result = vm.createBackup.saveTo(uri, context, onError = {
                        vm.showSnackBar(it)
                    }, onSuccess = {
                        vm.showSnackBar((UiText.StringResource(R.string.backup_created_successfully)))
                    })
                }
            }
        }
    val items = remember {
        listOf<Components>(
            Components.Row(
                context.getString(R.string.create_backup), onClick = {
                    context.findComponentActivity()
                        ?.let { activity ->
                            vm.onLocalBackupRequested { intent: Intent ->
                                onBackup.launch(intent)
                            }
                        }
                }
            ),
            Components.Row(
                context.getString(R.string.restore), onClick = {
                    context.findComponentActivity()
                        ?.let { activity ->
                            context.findComponentActivity()
                                ?.let { activity ->
                                    vm.onRestoreBackupRequested { intent: Intent ->
                                        onRestore.launch(intent)
                                    }
                                }
                        }
                }
            ),
            Components.Header(context.getString(R.string.automatic_backup)),
            Components.Dynamic {
                ChoicePreference<PreferenceValues.AutomaticBackup>(
                    preference = vm.automaticBackup,
                    choices = mapOf(
                        PreferenceValues.AutomaticBackup.Off to context.getString(R.string.off),
                        PreferenceValues.AutomaticBackup.Every6Hours to context.resources.getQuantityString(
                            R.plurals.every_hour,
                            6, 6

                        ),
                        PreferenceValues.AutomaticBackup.Every12Hours to context.resources.getQuantityString(
                            R.plurals.every_hour,
                            12, 12
                        ),
                        PreferenceValues.AutomaticBackup.Daily to context.getString(R.string.daily),
                        PreferenceValues.AutomaticBackup.Every2Days to context.resources.getQuantityString(
                            R.plurals.every_day,
                            2, 2
                        ),
                        PreferenceValues.AutomaticBackup.Weekly to context.getString(R.string.weekly),
                    ),
                    title = context.getString(
                        R.string.automatic_backup
                    ),
                    onItemSelected = {

                        context.findComponentActivity()
                            ?.let { activity ->
                                vm.getSimpleStorage.checkPermission()
                            }

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
                    title = context.getString(
                        R.string.maximum_backups
                    ),
                    enable = vm.automaticBackup.value != PreferenceValues.AutomaticBackup.Off
                )
            }

        )
    }
    SetupSettingComponents(items = items, scaffoldPadding = scaffoldPadding)
}
