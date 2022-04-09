package org.ireader.presentation.feature_settings.presentation.setting.dns

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ireader.core.utils.*
import org.ireader.domain.utils.launchIO
import org.ireader.presentation.R
import org.ireader.presentation.feature_detail.presentation.book_detail.components.AdvanceSettingItem
import org.ireader.presentation.feature_settings.presentation.setting.BackUpBook
import org.ireader.presentation.feature_settings.presentation.setting.SettingViewModel
import org.ireader.presentation.feature_sources.presentation.extension.composables.TextSection
import org.ireader.presentation.presentation.Toolbar
import org.ireader.presentation.presentation.components.ISnackBarHost
import org.ireader.presentation.presentation.reusable_composable.BigSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarBackButton
import java.io.FileInputStream
import java.io.FileOutputStream

@OptIn(ExperimentalSerializationApi::class)
@Composable
fun AdvanceSettings(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    vm: SettingViewModel = hiltViewModel(),

    ) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()


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
                    } catch (e: Exception) {
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
                                    vm.insertBackup(Json.Default.decodeFromString<List<BackUpBook>>(
                                        txt))
                                    vm.showSnackBar(UiText.StringResource(R.string.restoredSuccessfully))
                                }.getOrElse { e ->
                                    vm.showSnackBar(UiText.ExceptionString(e))
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    vm.showSnackBar(UiText.ExceptionString(e))
                }

            }
        }

    LaunchedEffect(key1 = true) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            Toolbar(
                title = {
                    BigSizeTextComposable(text = "Advance Settings")
                },
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.onBackground,
                elevation = Constants.DEFAULT_ELEVATION,
                navigationIcon = { TopAppBarBackButton(navController = navController) }
            )
        },
        snackbarHost = { ISnackBarHost(snackBarHostState = it) },
        scaffoldState = scaffoldState
    ) { padding ->
        Column {
            TextSection(text = "Data", toUpper = false)
            AdvanceSettingItem(title = "Clear All Database") {
                vm.deleteAllDatabase()
                vm.showSnackBar(UiText.DynamicString("Database was cleared."))
            }
            AdvanceSettingItem(title = "Clear All Chapters") {
                vm.deleteAllChapters()
                vm.showSnackBar(UiText.DynamicString("Chapters was cleared."))
            }
            AdvanceSettingItem(title = "Clear All Cache",
                subtitle = getCacheSize(context = context)) {
                context.cacheDir.deleteRecursively()
                vm.showSnackBar(UiText.DynamicString("Clear was cleared."))
            }

            TextSection(text = "Backup", toUpper = false)
            AdvanceSettingItem(title = "Backup") {
                context.findComponentActivity()
                    ?.let { activity ->
                        vm.onLocalBackupRequested { intent: Intent ->
                            onBackup.launch(intent)
                        }
                    }

            }
            AdvanceSettingItem(title = "Restore") {
                context.findComponentActivity()
                    ?.let { activity ->
                        vm.onRestoreBackupRequested { intent: Intent ->
                            onRestore.launch(intent)
                        }
                    }
            }
            TextSection(text = "Reset Setting", toUpper = false)
            AdvanceSettingItem(title = "Reset Reader Screen Settings") {
                vm.deleteDefaultSettings()
            }
        }


    }
}


