package org.ireader.presentation.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ireader.common_extensions.findComponentActivity
import org.ireader.common_extensions.launchIO
import org.ireader.common_models.BackUpBook
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.UiText
import org.ireader.domain.ui.NavigationArgs
import org.ireader.settings.setting.SettingsSection
import org.ireader.settings.setting.backups.BackUpAndRestoreScreen
import org.ireader.settings.setting.backups.BackupScreenViewModel
import org.ireader.ui_settings.R
import java.io.FileInputStream
import java.io.FileOutputStream

object BackupAndRestoreScreenSpec : ScreenSpec {

    override val navHostRoute: String = "backup_restore"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav
    )

    @OptIn(
        androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
    ) {
        val vm: BackupScreenViewModel = hiltViewModel()
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val snackBarHostState = remember { SnackbarHostState() }

        LaunchedEffect(key1 = true) {
            vm.eventFlow.collectLatest { event ->
                when (event) {
                    is UiEvent.ShowSnackbar -> {
                        snackBarHostState.showSnackbar(
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
                    scope.launchIO {
                        try {
                            val contentResolver = context.findComponentActivity()!!.contentResolver
                            val uri = resultIntent.data!!.data!!
                            val pfd = contentResolver.openFileDescriptor(uri, "w")
                            pfd?.use {
                                FileOutputStream(pfd.fileDescriptor).use { outputStream ->
                                    outputStream.write(vm.getAllBooks().toByteArray())
                                }
                            }
                        } catch (e: SerializationException) {
                            vm.showSnackBar(UiText.ExceptionString(e))
                        } catch (e: Throwable) {
                            vm.showSnackBar(UiText.ExceptionString(e))
                        }
                    }
                }
            }

        val onRestore =
            rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
                if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
                    try {
                        scope.launchIO {
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
                                        vm.insertBackup(
                                            Json.decodeFromString<List<BackUpBook>>(
                                                txt
                                            )
                                        )
                                        vm.showSnackBar(UiText.StringResource(R.string.restoredSuccessfully))
                                    }.getOrElse { e ->
                                        vm.showSnackBar(UiText.ExceptionString(e))
                                    }
                                }
                            }
                        }
                    } catch (e: Throwable) {
                        vm.showSnackBar(UiText.ExceptionString(e))
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
            items = settingItems,
            onBackStack = {
                navController.popBackStack()
            }
        )
    }
}