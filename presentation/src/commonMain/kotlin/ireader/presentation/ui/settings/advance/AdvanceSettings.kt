package ireader.presentation.ui.settings.advance


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.log.Log
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.core.theme.LocalGlobalCoroutineScope
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlinx.serialization.ExperimentalSerializationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun AdvanceSettings(
    vm: AdvanceSettingViewModel,
    padding: PaddingValues
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    val globalScope = LocalGlobalCoroutineScope.currentOrThrow
    val showImport = remember { mutableStateOf(false) }
    var showDeleteAllDb by remember { mutableStateOf(false) }
    OnShowImportEpub(showImport.value, onFileSelected = {
        try {
            vm.importEpub.parse(it)
            vm.showSnackBar(UiText.MStringResource(MR.strings.success))
        } catch (e: Throwable) {
            Log.error(e, "epub parser throws an exception")
            vm.showSnackBar(UiText.ExceptionString(e))
        }
    })

    val items = remember {
        listOf<Components>(
            Components.Header(localizeHelper.localize(MR.strings.data)),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_all_database),
                onClick = {
                    vm.deleteAllDatabase()
                    vm.showSnackBar(
                        UiText.MStringResource(MR.strings.database_was_cleared)
                    )
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_not_in_library_books),
                onClick = {
                    vm.scope.launchIO {
                        vm.deleteUseCase.deleteNotInLibraryBooks()
                        vm.showSnackBar(
                            UiText.MStringResource(MR.strings.success)
                        )
                    }
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.clear_all_chapters),
                onClick = {
                    vm.deleteAllChapters()
                    vm.showSnackBar(
                        UiText.MStringResource(MR.strings.chapters_was_cleared)
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
                    vm.getSimpleStorage.clearImageCache()
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

                    showImport.value = true

                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.delete_all_database),
                onClick = {
                    showDeleteAllDb = true
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.repair_database),
                onClick = {
                    vm.repairDatabase()
                }
            ),
            Components.Row(
                title = localizeHelper.localize(MR.strings.repair_categories),
                onClick = {
                    vm.repairBookCategories()
                }
            ),
        )
    }


    SetupSettingComponents(scaffoldPadding = padding, items = items)
}
