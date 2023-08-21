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

import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.core.theme.LocalGlobalCoroutineScope
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import kotlinx.serialization.ExperimentalSerializationApi
@OptIn(ExperimentalSerializationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdvanceSettings(
    vm: AdvanceSettingViewModel,
    padding: PaddingValues
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    val globalScope = LocalGlobalCoroutineScope.currentOrThrow
    val showImport = remember { mutableStateOf(false) }
    OnShowImportEpub(showImport.value, onFileSelected = {
        try {
            vm.importEpub.parse(it)
            vm.showSnackBar(UiText.MStringResource() { xml ->
                xml.success
            })
        } catch (e: Throwable) {
            Log.error(e, "epub parser throws an exception")
            vm.showSnackBar(UiText.ExceptionString(e))
        }
    })

    val items = remember {
        listOf<Components>(
            Components.Header(localizeHelper.localize { xml -> xml.data }),
            Components.Row(
                title = localizeHelper.localize { xml -> xml.clearAllDatabase },
                onClick = {
                    vm.deleteAllDatabase()
                    vm.showSnackBar(
                        UiText.MStringResource { it.databaseWasCleared }
                    )
                }
            ),
            Components.Row(
                title = localizeHelper.localize { xml -> xml.clearNotInLibraryBooks },
                onClick = {
                    vm.scope.launchIO {
                        vm.deleteUseCase.deleteNotInLibraryBooks()
                        vm.showSnackBar(
                            UiText.MStringResource() { xml ->
                                xml.success
                            })
                    }
                }
            ),
            Components.Row(
                title = localizeHelper.localize { xml -> xml.clearAllChapters },
                onClick = {
                    vm.deleteAllChapters()
                    vm.showSnackBar(
                        UiText.MStringResource { it.chaptersWasCleared }
                    )
                }
            ),
            Components.Row(
                title = localizeHelper.localize { xml -> xml.clearAllCache },
                subtitle = vm.importEpub.getCacheSize(),
                onClick = {
                    vm.importEpub.removeCache()
                    vm.showSnackBar(UiText.DynamicString("Clear was cleared."))
                }
            ),
            Components.Row(
                title = localizeHelper.localize { xml -> xml.clearAllCoverCache },
                onClick = {
                    vm.getSimpleStorage.clearImageCache()
                    vm.showSnackBar(UiText.DynamicString("Clear was cleared."))
                }
            ),
            Components.Header(localizeHelper.localize { xml -> xml.resetSetting }),
            Components.Row(
                title = localizeHelper.localize { xml -> xml.resetReaderScreenSettings },
                onClick = {
                    vm.deleteDefaultSettings()
                }
            ),
            Components.Row(
                title = localizeHelper.localize { xml -> xml.resetThemes },
                onClick = {
                    vm.resetThemes()
                }
            ),
            Components.Row(
                title = localizeHelper.localize { xml -> xml.resetCategories },
                onClick = {
                    vm.resetCategories()
                }
            ),
            Components.Header(localizeHelper.localize { xml -> xml.epub }),
            Components.Row(
                title = localizeHelper.localize { xml -> xml.importEpub },
                onClick = {

                    showImport.value = true

                }
            )

        )
    }


    SetupSettingComponents(scaffoldPadding = padding, items = items)
}
