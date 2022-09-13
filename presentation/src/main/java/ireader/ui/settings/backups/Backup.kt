package ireader.ui.settings.backups

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import ireader.common.extensions.findComponentActivity
import ireader.common.extensions.launchIO
import ireader.common.resources.UiText
import ireader.ui.settings.SettingsSection
import ireader.ui.settings.SetupLayout
import ireader.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackUpAndRestoreScreen(
    modifier: Modifier = Modifier,
    onBackStack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    vm: BackupScreenViewModel,
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

    val settingItems = listOf(
        SettingsSection(
            R.string.create_backup,
        ) {
            context.findComponentActivity()
                ?.let { activity ->
                    vm.onLocalBackupRequested { intent: Intent ->
                        onBackup.launch(intent)
                    }
                }
        },
        SettingsSection(
            R.string.restore,
        ) {
            context.findComponentActivity()
                ?.let { activity ->
                    vm.onRestoreBackupRequested { intent: Intent ->
                        onRestore.launch(intent)
                    }
                }
        },
    )
    SetupLayout(items = settingItems, modifier = modifier)
}
