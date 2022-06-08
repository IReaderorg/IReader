package org.ireader.presentation.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import org.ireader.common_extensions.findComponentActivity
import org.ireader.common_extensions.launchIO
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.UiText
import org.ireader.components.components.TitleToolbar
import org.ireader.settings.setting.SettingsSection
import org.ireader.settings.setting.backups.BackUpAndRestoreScreen
import org.ireader.settings.setting.backups.BackupScreenViewModel
import org.ireader.ui_settings.R
import java.io.FileInputStream
import java.io.FileOutputStream

object BackupAndRestoreScreenSpec : ScreenSpec {

    override val navHostRoute: String = "backup_restore"

    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        controller: ScreenSpec.Controller
    ) {
        TitleToolbar(
            title = stringResource(R.string.backup_and_restore),
            navController = controller.navController
        )
    }

    @Composable
    override fun Content(
        controller: ScreenSpec.Controller
    ) {
        val vm: BackupScreenViewModel = hiltViewModel(controller.navBackStackEntry)
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = true) {
            vm.eventFlow.collectLatest { event ->
                when (event) {
                    is UiEvent.ShowSnackbar -> {
                        controller.snackBarHostState.showSnackbar(
                            event.uiText.asString(context)
                        )
                    }
                    else -> {}
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
//                    scope.launchIO {
//                        try {
//                            val contentResolver = context.findComponentActivity()!!.contentResolver
//                            val pfd = contentResolver.openFileDescriptor(uri, "w")
//                            pfd?.use {
//                                FileOutputStream(pfd.fileDescriptor).use { outputStream ->
//                                    outputStream.write(vm.getAllBooks().toByteArray())
//                                }
//                            }
//                        } catch (e: SerializationException) {
//                            vm.showSnackBar(UiText.ExceptionString(e))
//                        } catch (e: Throwable) {
//                            vm.showSnackBar(UiText.ExceptionString(e))
//                        }
//                    }
                }
            }

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


        val settingItems = listOf(
            SettingsSection(
                org.ireader.ui_settings.R.string.create_backup,
            ) {
                context.findComponentActivity()
                    ?.let { activity ->
                        vm.onLocalBackupRequested { intent: Intent ->
                            onBackup.launch(intent)
                        }
                    }
            },
            SettingsSection(
                org.ireader.ui_settings.R.string.restore,
            ) {
                context.findComponentActivity()
                    ?.let { activity ->
                        vm.onRestoreBackupRequested { intent: Intent ->
                            onRestore.launch(intent)
                        }
                    }
            },


        )

        BackUpAndRestoreScreen(
            modifier = Modifier.padding(controller.scaffoldPadding),
            items = settingItems,
            onBackStack =
            {
                controller.navController.popBackStack()
            },
            snackbarHostState = controller.snackBarHostState
        )
    }
}

fun restoreBackup(
    context: Context,
    resultIntent: ActivityResult,
    onSuccess: (String) -> Unit,
    onError: (Throwable) -> Unit
) {
    try {
        val contentResolver = context.findComponentActivity()!!.contentResolver
        val uri = resultIntent.data!!.data!!
        contentResolver
            .takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        val pfd = contentResolver.openFileDescriptor(uri, "r")
        pfd?.use {
            FileInputStream(pfd.fileDescriptor).use { stream ->
                val txt = stream.readBytes().decodeToString()
                kotlin.runCatching {
                    onSuccess(txt)
                }.getOrElse { e ->
                    onError(e)
                }
            }
        }
    } catch (e: Throwable) {
        onError(e)
    }
}

fun makeBackup(
    context: Context,
    resultIntent: ActivityResult,
    text: String,
    onError: (Throwable) -> Unit
) {
    try {
        val contentResolver = context.findComponentActivity()!!.contentResolver
        val uri = resultIntent.data!!.data!!
        val pfd = contentResolver.openFileDescriptor(uri, "w")
        pfd?.use {
            FileOutputStream(pfd.fileDescriptor).use { outputStream ->
                outputStream.write(text.toByteArray())
            }
        }
    } catch (e: Throwable) {
        onError(e)
    }
}