package org.ireader.settings.setting.advance

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import kotlinx.serialization.ExperimentalSerializationApi
import org.ireader.Controller
import org.ireader.common_extensions.findComponentActivity
import org.ireader.common_extensions.getCacheSize
import org.ireader.common_extensions.launchIO
import org.ireader.common_resources.UiText
import org.ireader.components.components.Components
import org.ireader.components.components.SetupSettingComponents
import org.ireader.core_api.log.Log
import org.ireader.settings.setting.AdvanceSettingViewModel
import org.ireader.ui_settings.R

@OptIn(ExperimentalSerializationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdvanceSettings(
    modifier: Modifier = Modifier,
    vm: AdvanceSettingViewModel,
    controller: Controller

) {
    val context = LocalContext.current
    val onEpub =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
            if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
                val uri = resultIntent.data!!.data!!
                context.findComponentActivity()?.lifecycleScope?.launchIO {
                    try {
                        vm.importEpub.parse(uri, context)
                        vm.showSnackBar(UiText.StringResource(R.string.success))
                    } catch (e: Throwable) {
                        Log.error(e, "epub parser throws an exception")
                        vm.showSnackBar(UiText.ExceptionString(e))
                    }
                }
            }
        }

    val items = listOf<Components>(
        Components.Header(stringResource(R.string.data)),
        Components.Row(
            title = stringResource(id = R.string.clear_all_database),
            onClick = {
                vm.deleteAllDatabase()
                vm.showSnackBar(
                    UiText.StringResource(R.string.database_was_cleared)
                )
            }
        ),
        Components.Row(
            title = stringResource(id = R.string.clear_not_in_library_books),
            onClick = {
                vm.viewModelScope.launchIO {
                    vm.deleteUseCase.deleteNotInLibraryBooks()
                    vm.showSnackBar(
                        UiText.StringResource(R.string.success)
                    )
                }
            }
        ),
        Components.Row(
            title = stringResource(id = R.string.clear_all_chapters),
            onClick = {
                vm.deleteAllChapters()
                vm.showSnackBar(
                    UiText.StringResource(R.string.chapters_was_cleared)
                )
            }
        ),
        Components.Row(
            title = stringResource(id = R.string.clear_all_cache),
            subtitle = getCacheSize(context = context),
            onClick = {
                context.cacheDir.deleteRecursively()
                vm.showSnackBar(UiText.DynamicString("Clear was cleared."))
            }
        ),
        Components.Row(
            title = stringResource(id = R.string.clear_all_cover_cache),
            onClick = {
                vm.coverCache.clearMemoryCache()
                vm.showSnackBar(UiText.DynamicString("Clear was cleared."))
            }
        ),
        Components.Header(stringResource(R.string.reset_setting)),
        Components.Row(
            title = stringResource(id = R.string.reset_reader_screen_settings),
            onClick = {
                vm.deleteDefaultSettings()
            }
        ),
        Components.Row(
            title = context.getString(R.string.reset_themes),
            onClick = {
                vm.resetThemes()
            }
        ),
        Components.Row(
            title = context.getString(R.string.reset_categories),
            onClick = {
                vm.resetCategories()
            }
        ),
        Components.Header(stringResource(R.string.epub)),
        Components.Row(
            title = stringResource(id = R.string.import_epub),
            onClick = {
                context.findComponentActivity()
                    ?.let { activity ->
                        vm.onEpubImportRequested { intent: Intent ->
                            onEpub.launch(intent)
                        }
                    }
            }
        )

    )

    SetupSettingComponents(scaffoldPadding = controller.scaffoldPadding, items = items)
}
