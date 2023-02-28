package ireader.presentation.ui.settings.advance

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.log.Log
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.resources.MR
import ireader.presentation.R
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.core.theme.LocalGlobalCoroutineScope
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.AdvanceSettingViewModel
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdvanceSettings(
    vm: AdvanceSettingViewModel,
    padding: PaddingValues
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    val globalScope = LocalGlobalCoroutineScope.currentOrThrow
    val onEpub =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
            if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
                val uri = resultIntent.data!!.data!!
                globalScope.launchIO {
                    try {
                        vm.importEpub.parse(uri)
                        vm.showSnackBar(UiText.StringResource(R.string.success))
                    } catch (e: Throwable) {
                        Log.error(e, "epub parser throws an exception")
                        vm.showSnackBar(UiText.ExceptionString(e))
                    }
                }
            }
        }

    val items = remember {
        listOf<Components>(
            Components.Header(localizeHelper.localize(MR.strings.data)),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_all_database),
                onClick = {
                    vm.deleteAllDatabase()
                    vm.showSnackBar(
                        UiText.StringResource(R.string.database_was_cleared)
                    )
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_not_in_library_books),
                onClick = {
                    vm.scope.launchIO {
                        vm.deleteUseCase.deleteNotInLibraryBooks()
                        vm.showSnackBar(
                            UiText.StringResource(R.string.success)
                        )
                    }
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_all_chapters),
                onClick = {
                    vm.deleteAllChapters()
                    vm.showSnackBar(
                        UiText.StringResource(R.string.chapters_was_cleared)
                    )
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_all_cache),
                subtitle = vm.importEpub.getCacheSize(),
                onClick = {
                    vm.importEpub.removeCache()
                    vm.showSnackBar(UiText.DynamicString("Clear was cleared."))
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_all_cover_cache),
                onClick = {
                    vm.coverCache.clearMemoryCache()
                    vm.showSnackBar(UiText.DynamicString("Clear was cleared."))
                }
            ),
            Components.Header(localizeHelper.localize(MR.strings.reset_setting)),
            Components.Row(
                title = localizeHelper.localize(MR.strings.reset_reader_screen_settings),
                onClick = {
                    vm.deleteDefaultSettings()
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.reset_themes),
                onClick = {
                    vm.resetThemes()
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.reset_categories),
                onClick = {
                    vm.resetCategories()
                }
            ),
            Components.Header(localizeHelper.localize(MR.strings.epub)),
            Components.Row(
                title = localizeHelper.localize(MR.strings.import_epub),
                onClick = {

                    vm.onEpubImportRequested { intent: Intent ->
                        onEpub.launch(intent)
                    }

                }
            )

        )
    }


    SetupSettingComponents(scaffoldPadding = padding, items = items)
}
